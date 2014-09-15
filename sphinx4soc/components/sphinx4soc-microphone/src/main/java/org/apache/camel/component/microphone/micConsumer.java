package org.apache.camel.component.microphone;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.SuspendableService;
import org.apache.camel.impl.LoggingExceptionHandler;
import org.apache.camel.spi.ExceptionHandler;
import org.apache.camel.support.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mic consumer.
 */
public class micConsumer extends ServiceSupport implements Consumer, Runnable, SuspendableService {

    private static final transient Logger LOG = LoggerFactory.getLogger(micConsumer.class);
    
    private static final String AUDIO_BEGIN_TIME = "AUDIO_BEGIN_TIME";
    private static final String AUDIO_END_TIME = "AUDIO_END_TIME";
    private static final String AUDIO_FRAME = "AUDIO_FRAME";
    private static final String AUDIO_TIMESTAMP = "AUDIO_TIMESTAMP";
    private static final String AUDIO_FORMAT = "AUDIO_FORMAT";
    private static final String AUDIO_SOURCE = "AUDIO_SOURCE";
    private static final String AUDIO_SOURCE_ID = "AUDIO_SOURCE_ID";
    private static final String AUDIO_DURATION = "AUDIO_DURATION";
    private static final String AUDIO_FIRST_SAMPLE = "AUDIO_FIRST_SAMPLE";
    // consumer
    private final micEndpoint endpoint;
    private Processor processor;
    private ExecutorService executor;
    private ExceptionHandler exceptionHandler;
    private String identifier;
    private int frameNumber;
    // microphone
    private boolean doConversion;
    private boolean signed;
    private int frameSizeInBytes;
    private long totalSamplesRead;
    private AudioFormat finalFormat;
    private AudioInputStream audioStream;
    private TargetDataLine audioLine;
    private AudioFormat desiredFormat;
    private Mixer mixer;
    // Thread
    private volatile boolean recording;
    private volatile boolean done;
    private volatile boolean started;
    private final Object lock = new Object();

    public micConsumer(micEndpoint endpoint, Processor processor) {
        this.endpoint = endpoint;
        this.processor = processor;

        this.exceptionHandler = new LoggingExceptionHandler(endpoint.getCamelContext(), getClass());
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        return "MicrophoneConsumer[" + endpoint + "]";
    }

    @Override
    protected void doSuspend() throws Exception {
        endpoint.suspend();
    }

    @Override
    protected void doResume() throws Exception {
        endpoint.resume();
    }

    public boolean isRunAllowed() {
        if (isSuspending() || isSuspended()) {
            // allow to run even if we are suspended as we want to
            // keep the thread task running
            return true;
        }
        return super.isRunAllowed();
    }

    private void initialize() throws Exception {

        frameNumber = 0;
        identifier = UUID.randomUUID().toString();
        // TODO check other format validity.
        if (endpoint.getEncoding() == Encoding.PCM_SIGNED) {
            signed = true;
        } else if (endpoint.getEncoding() == Encoding.PCM_FLOAT) {
            signed = true;
        } else {
            signed = false;
        }

        desiredFormat = new AudioFormat((float) endpoint.getSampleRate(), endpoint.getNumBitsPerSample(), endpoint.getNumChannels(), signed,
                endpoint.isBigEndian());
        mixer = MicrophoneHelper.getSelectedMixer(endpoint.getSelectMixerIndex());
        checkFormatSupport(desiredFormat, mixer);

        float sec = ((float) endpoint.getMsecPerRead()) / 1000.f;
        frameSizeInBytes = (endpoint.getNumBitsPerSample() / 8) * (int) (sec * endpoint.getSampleRate());
        LOG.info("Number of bits per sample: " + endpoint.getNumBitsPerSample() + " sample rate: " + endpoint.getSampleRate());
        LOG.info("Frame size: " + frameSizeInBytes + " bytes");
    }

    protected void checkFormatSupport(AudioFormat desiredFormat, Mixer selectedMixer) throws Exception {

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, desiredFormat);
        /*
         * If we cannot get an audio line that matches the desired characteristics, shoot for one
         * that matches almost everything we want, but has a higher sample rate.
         */
        if (!AudioSystem.isLineSupported(info)) {
            LOG.info(desiredFormat + " not supported");
            AudioFormat nativeFormat = MicrophoneHelper.getNativeAudioFormat(desiredFormat, selectedMixer);
            if (nativeFormat == null) {
                LOG.error("couldn't find suitable target audio format");
                throw new Exception("couldn't find suitable target audio format");
            } else {
                finalFormat = nativeFormat;

                /* convert from native to the desired format if supported */
                doConversion = AudioSystem.isConversionSupported(desiredFormat, nativeFormat);

                if (doConversion) {
                    LOG.debug("Converting from " + finalFormat.getSampleRate() + "Hz to " + desiredFormat.getSampleRate() + "Hz");
                } else {
                    LOG.debug("Using native format: Cannot convert from " + finalFormat.getSampleRate() + "Hz to " + desiredFormat.getSampleRate()
                            + "Hz");
                }
            }
        } else {
            LOG.debug("Desired format: " + desiredFormat + " supported.");
            finalFormat = desiredFormat;
        }
    }

    /**
     * Opens the audio capturing device so that it will be ready for capturing audio. Attempts to
     * create a converter if the requested audio format is not directly available.
     *
     * @return true if the audio capturing device is opened successfully; false otherwise
     */
    protected boolean open() {
        TargetDataLine audioLineTmp = getAudioLine();
        if (audioLineTmp != null) {
            if (!audioLineTmp.isOpen()) {
                LOG.info("open");
                try {
                    audioLineTmp.open(finalFormat, endpoint.getAudioBufferSize());
                } catch (LineUnavailableException e) {
                    LOG.error("Can't open microphone " + e.getMessage());
                    return false;
                }

                audioStream = new AudioInputStream(audioLineTmp);
                if (doConversion) {
                    audioStream = AudioSystem.getAudioInputStream(desiredFormat, audioStream);
                    assert (audioStream != null);
                }
            }
            return true;
        } else {
            LOG.error("Can't find microphone");
            return false;
        }
    }

    private TargetDataLine getAudioLine() {
        if (audioLine != null) {
            return audioLine;
        }

        /*
         * Obtain and open the line and stream.
         */
        try {
            /*
             * The finalFormat was decided in the initialize() method and is based upon the
             * capabilities of the underlying audio system. The final format will have all the
             * desired audio characteristics, but may have a sample rate that is higher than
             * desired. The idea here is that we'll let the processors in the front end (e.g., the
             * FFT) handle some form of downsampling for us.
             */
            LOG.info("Final format: " + finalFormat);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, finalFormat);

            /*
             * We either get the audio from the AudioSystem (our default choice), or use a specific
             * Mixer if the selectedMixerIndex property has been set.
             */
            Mixer selectedMixer = MicrophoneHelper.getSelectedMixer(endpoint.getSelectMixerIndex());
            if (selectedMixer == null) {
                audioLine = (TargetDataLine) AudioSystem.getLine(info);
            } else {
                audioLine = (TargetDataLine) selectedMixer.getLine(info);
            }

            /*
             * Add a line listener that just traces the line states.
             */
            audioLine.addLineListener(new LineListener() {

                public void update(LineEvent event) {
                    LOG.info("line listener " + event);
                }
            });
        } catch (LineUnavailableException e) {
            LOG.error("microphone unavailable " + e.getMessage());
        }

        return audioLine;
    }

    @Override
    public void run() {
        totalSamplesRead = 0;

        LOG.info("started recording");
        createSignalExchange("StartSignal");
        LOG.info("DataStartSignal sended");

        try {
            audioLine.start();
            while (!done) {
                double[] data = readData();
                if (data == null) {
                    done = true;
                    break;
                }
                createDataExchange(data);
            }
            audioLine.flush();
            /*
             * Closing the audio stream *should* (we think) also close the audio line, but it
             * doesn't appear to do this on the Mac. In addition, once the audio line is closed,
             * re-opening it on the Mac causes some issues. The Java sound spec is also kind of
             * ambiguous about whether a closed line can be re-opened. So...we'll go for the
             * conservative route and never attempt to re-open a closed line.
             */
            audioStream.close();
            audioLine.close();
            audioLine = null;
        } catch (Exception ioe) {
            LOG.warn("Exception " + ioe.getMessage());
        }

        createSignalExchange("EndSignal");
        LOG.info("DataEndSignal ended");
        LOG.info("stopped recording");

        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * Reads one frame of audio data, and adds it to the given Utterance.
     *
     * @param utterance
     * @return an Data object containing the audio data
     * @throws java.io.IOException
     */
    private double[] readData() throws Exception {

        // Read the next chunk of data from the TargetDataLine.
        byte[] data = new byte[frameSizeInBytes];

        int channels = audioStream.getFormat().getChannels();

        LOG.debug("Read " + frameSizeInBytes + " bytes from audio stream.");
        int numBytesRead = audioStream.read(data, 0, data.length);

        // notify the waiters upon start
        if (!started) {
            synchronized (this) {
                started = true;
                notifyAll();
            }
        }

        LOG.debug("Read " + numBytesRead + " bytes from audio stream.");

        if (numBytesRead <= 0) {
            return null;
        }
        int sampleSizeInBytes = audioStream.getFormat().getSampleSizeInBits() / 8;
        totalSamplesRead += (numBytesRead / sampleSizeInBytes);

        if (numBytesRead != frameSizeInBytes) {

            if (numBytesRead % sampleSizeInBytes != 0) {
                throw new Exception("Incomplete sample read.");
            }

            data = Arrays.copyOf(data, numBytesRead);
        }

        double[] samples;

        if (endpoint.isBigEndian()) {
            samples = MicrophoneHelper.bytesToValues(data, 0, data.length, sampleSizeInBytes, signed);
        } else {
            samples = MicrophoneHelper.littleEndianBytesToValues(data, 0, data.length, sampleSizeInBytes, signed);
        }

        if (channels > 1) {
            samples = MicrophoneHelper.convertStereoToMono(samples, channels, endpoint.getStereoToMono(), endpoint.getSelectChannel());
        }

        return samples;
    }

    protected void createSignalExchange(String signal) {
        Exchange exchange = endpoint.createExchange();
        exchange.getIn().setHeader(AUDIO_SOURCE, "Microphone");
        exchange.getIn().setHeader(AUDIO_SOURCE_ID, identifier);
        if ("StartSignal".equals(signal)) {
            exchange.getIn().setHeader(AUDIO_BEGIN_TIME, System.currentTimeMillis());
            exchange.getIn().setHeader(AUDIO_FORMAT, finalFormat);
        } else if ("EndSignal".equals(signal)) {
            exchange.getIn().setHeader(AUDIO_END_TIME, System.currentTimeMillis());
            long duration = (long) (((double) totalSamplesRead / (double) audioStream.getFormat().getSampleRate()) * 1000.0);
            exchange.getIn().setHeader(AUDIO_DURATION, duration);
        }

        exchange.getIn().setBody(signal);
        try {
            processor.process(exchange);
        } catch (Exception e) {
            exchange.setException(e);
        }

        // handle any thrown exception
        if (exchange.getException() != null) {
            exceptionHandler.handleException("Error processing exchange", exchange, exchange.getException());
        }
    }

    protected void createDataExchange(double[] values) {
        Exchange exchange = endpoint.createExchange();
        exchange.getIn().setHeader(AUDIO_SOURCE, "Microphone");
        exchange.getIn().setHeader(AUDIO_SOURCE_ID, identifier);
        exchange.getIn().setHeader(AUDIO_TIMESTAMP, System.currentTimeMillis());
        exchange.getIn().setHeader(AUDIO_FRAME, frameNumber++);
        exchange.getIn().setHeader(AUDIO_FORMAT, finalFormat);
        long firstSampleNumber = totalSamplesRead / audioStream.getFormat().getChannels();
        exchange.getIn().setHeader(AUDIO_FIRST_SAMPLE, firstSampleNumber);
        exchange.getIn().setBody(values);
        try {
            processor.process(exchange);
        } catch (Exception e) {
            exchange.setException(e);
        }

        // handle any thrown exception
        if (exchange.getException() != null) {
            exceptionHandler.handleException("Error processing exchange", exchange, exchange.getException());
        }
    }


    @Override
    protected void doStart() throws Exception {
        
        super.start();

        if (recording) {
            throw new Exception("Still in recording State.");
        }

        initialize();

        if (!open()) {
            throw new Exception("Could not open microphone.");
        }
        if (audioLine.isRunning()) {
            LOG.warn("Whoops: audio line is running");
        }
        startRecording();

        recording = true;
    }

    public void startRecording() {
        started = false;
        done = false;
        if (executor == null) {
            executor = endpoint.getCamelContext().getExecutorServiceManager().newFixedThreadPool(this, endpoint.getEndpointUri(), 1);
        }

        executor.execute(this);

        waitForStart();
    }

    private synchronized void waitForStart() {
        // note that in theory we could use a LineEvent START
        // to tell us when the microphone is ready, but we have
        // found that some javasound implementations do not always
        // issue this event when a line is opened, so this is a
        // WORKAROUND.

        try {
            while (!started) {
                wait();
            }
        } catch (InterruptedException ie) {
            LOG.warn("wait was interrupted");
        }
    }

    @Override
    protected void doStop() throws Exception {

        super.stop();
        if (audioLine != null) {
            stopRecording();
            recording = false;
        }

        if (executor != null) {
            endpoint.getCamelContext().getExecutorServiceManager().shutdownNow(executor);
            executor = null;
        }
    }

    /**
     * Stops the thread. This method does not return until recording has actually stopped, and all
     * the data has been read from the audio line.
     */
    public void stopRecording() {
        audioLine.stop();
        try {
            synchronized (lock) {
                while (!done) {
                    lock.wait();
                }
            }
        } catch (InterruptedException e) {
        }
    }
}
