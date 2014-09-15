package org.apache.camel.component.microphone;

import javax.sound.sampled.AudioFormat;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;

/**
 * Represents a mic endpoint.
 */
@ManagedResource(description = "Managed micEndpoint")
@UriEndpoint(scheme = "mic", consumerClass = micConsumer.class)
public class micEndpoint extends DefaultEndpoint {

    @UriParam
    private String micName;
    // AudioFormat Parameters 
    @UriParam
    private AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    @UriParam
    private int sampleRate = 1600;
    @UriParam
    private int numBitsPerSample = 16;
    @UriParam
    private int numChannels = 1;
    @UriParam
    private boolean bigEndian = true;
    @UriParam
    private String standardCodec;
    @UriParam
    private String supportedFormat;
    // Frame size in Bytes
    @UriParam
    private int msecPerRead = 10;
    // Mixer parameters
    @UriParam
    private String selectMixerIndex = "default";
    @UriParam
    private String stereoToMono = "average";
    @UriParam
    private int selectChannel = 0;
    // TargetDataLine parameters
    @UriParam
    private int audioBufferSize = 6400;
    
    public micEndpoint() {
    }

    public micEndpoint(String uri, micComponent component) {
        super(uri, component);
    }

    public micEndpoint(String uri, micComponent component, String micName) {
        super(uri, component);
        this.micName = micName;
    }
    
    public Producer createProducer() throws Exception {
        throw new RuntimeCamelException("Cannot produce to a micEndpoint: " + getEndpointUri());
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new micConsumer(this, processor);
    }

    public boolean isSingleton() {
        return true;
    }

    @ManagedAttribute(description = "Microphone Name")
    public String getMicName() {
        return micName;
    }

    @ManagedAttribute(description = "Microphone Name")
    public void setMicName(String micName) {
        this.micName = micName;
    }

    @ManagedAttribute(description = "Microphone Encoding")
    public AudioFormat.Encoding getEncoding() {
        return encoding;
    }

    @ManagedAttribute(description = "Microphone Encoding")
    public void setEncoding(AudioFormat.Encoding encoding) {
        this.encoding = encoding;
    }

    @ManagedAttribute(description = "Microphone SampleRate")
    public int getSampleRate() {
        return sampleRate;
    }

    @ManagedAttribute(description = "Microphone SampleRate")
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @ManagedAttribute(description = "Microphone Num of Bits per Sample")
    public int getNumBitsPerSample() {
        return numBitsPerSample;
    }

    @ManagedAttribute(description = "Microphone Num of Bits per Sample")
    public void setNumBitsPerSample(int numBitsPerSample) {
        this.numBitsPerSample = numBitsPerSample;
    }

    @ManagedAttribute(description = "Microphone Num of Channels")
    public int getNumChannels() {
        return numChannels;
    }

    @ManagedAttribute(description = "Microphone Num of Channels")
    public void setNumChannels(int numChannels) {
        this.numChannels = numChannels;
    }

    @ManagedAttribute(description = "Microphone BigEndian")
    public boolean isBigEndian() {
        return bigEndian;
    }

    @ManagedAttribute(description = "Microphone BigEndian")
    public void setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }
    
    @ManagedAttribute(description = "Microphone mSec Per Read")
    public int getMsecPerRead() {
        return msecPerRead;
    }
    @ManagedAttribute(description = "Microphone mSec Per Read")
    public void setMsecPerRead(int msecPerRead) {
        this.msecPerRead = msecPerRead;
    }
    
    @ManagedAttribute(description = "Microphone Mixer Index")
    public String getSelectMixerIndex() {
        return selectMixerIndex;
    }
    @ManagedAttribute(description = "Microphone Mixer Index")
    public void setSelectMixerIndex(String selectMixerIndex) {
        this.selectMixerIndex = selectMixerIndex;
    }

    @ManagedAttribute(description = "Microphone Stereo to Mono")
    public String getStereoToMono() {
        return stereoToMono;
    }

    @ManagedAttribute(description = "Microphone Stereo to Mono")
    public void setStereoToMono(String stereoToMono) {
        this.stereoToMono = stereoToMono;
    }

    @ManagedAttribute(description = "Microphone Select Channel")
    public int getSelectChannel() {
        return selectChannel;
    }
    @ManagedAttribute(description = "Microphone SelectChannel")
    public void setSelectChannel(int selectChannel) {
        this.selectChannel = selectChannel;
    }

    @ManagedAttribute(description = "Microphone Audio Buffer Size")
    public int getAudioBufferSize() {
        return audioBufferSize;
    }

    @ManagedAttribute(description = "Microphone Audio Buffer Size")
    public void setAudioBufferSize(int audioBufferSize) {
        this.audioBufferSize = audioBufferSize;
    }

    @ManagedAttribute(description = "Microphone StandardCodec")
    public String getStandardCodec() {
        return standardCodec;
    }

    @ManagedAttribute(description = "Microphone StandardCodec")
    public void setStandardCodec(String standardCodec) {
        this.standardCodec = standardCodec;
    }

    @ManagedAttribute(description = "Microphone SupportedFormat")
    public String getSupportedFormat() {
        return supportedFormat;
    }

    @ManagedAttribute(description = "Microphone SupportedFormat")
    public void setSupportedFormat(String supportedFormat) {
        this.supportedFormat = supportedFormat;
    }
    
    
}
