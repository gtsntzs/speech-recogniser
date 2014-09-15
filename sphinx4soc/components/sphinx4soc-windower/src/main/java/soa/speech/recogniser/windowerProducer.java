package soa.speech.recogniser;

import java.util.Arrays;
import java.util.Map;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.util.ExchangeHelper;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.endpoint.SpeechEndSignal;
import edu.cmu.sphinx.frontend.endpoint.SpeechStartSignal;
import edu.cmu.sphinx.frontend.util.DataUtil;

/**
 * The windower producer.
 */
public class windowerProducer extends DefaultAsyncProducer  //extends CollectionProducer
{
    private final windowerEndpoint endpoint;
    private double alpha;
    private float windowSizeInMs;
    private float windowShiftInMs;
    // required to access the DataStartSignal-properties
    public static final String WINDOW_SHIFT_SAMPLES = "windowSize";
    public static final String WINDOW_SIZE_SAMPLES = "windowShift";
    private double[] cosineWindow; // the raised consine window
    private int windowShift; // the window size
    private DoubleBuffer overflowBuffer; // cache for overlapped audio regions
    private long currentCollectTime;
    private long currentFirstSampleNumber;
    private int sampleRate;
    
    private Map<String,Object> incomingHeaders;
    
    public windowerProducer ( windowerEndpoint endpoint )
    {
        super( endpoint);
        this.endpoint = endpoint;
        this.alpha = endpoint.getAlpha();
        this.windowSizeInMs = endpoint.getWindowSizeInMs();
        this.windowShiftInMs = endpoint.getWindowShiftInMs();
    }

    @Override
    public boolean process ( final Exchange exchange, final AsyncCallback callback ) throws DataProcessingException
    {
        Exchange copy = ExchangeHelper.createCorrelatedCopy( exchange, true );
        incomingHeaders = exchange.getIn().getHeaders();
        
        // set a new from endpoint to be the seda queue
        copy.setFromEndpoint( endpoint );

        Data input = (Data)copy.getIn().getBody();

        if ( input instanceof DoubleData ) {
            DoubleData data = (DoubleData)input;
            if ( currentFirstSampleNumber==-1 ) {
                currentFirstSampleNumber = data.getFirstSampleNumber();
            }

            // should not be necesssary if all DataProcessor would forward Signals. Unfortunately this
            // is currently not the case.
            createWindow( data.getSampleRate() );

            currentCollectTime = data.getCollectTime();

            double[] in = data.getValues();
            int length = overflowBuffer.getOccupancy()+in.length;

            log.debug( "CosineWin: "+cosineWindow.length+" windowSize: "+DataUtil.getSamplesPerWindow( sampleRate, windowSizeInMs )+"windowShift: "+windowShift );
            log.debug( "LLLenght: "+length+" OverFlowBuffer: "+overflowBuffer.getOccupancy()+" Input: "+in.length );
            // if data are not enough append them to the overflowBuffer
            if ( length<cosineWindow.length ) {
                overflowBuffer.append( in, 0, in.length );
            } else { // else create the window data
                double[] allSamples = new double[ length ];
                System.arraycopy( overflowBuffer.getBuffer(), 0, allSamples, 0, overflowBuffer.getOccupancy() );
                System.arraycopy( in, 0, allSamples, overflowBuffer.getOccupancy(), in.length );
                // apply Hamming window
                int residual = applyRaisedCosineWindow( allSamples, length );

                // save elements that also belong to the next window
                overflowBuffer.reset();
                if ( length-residual>0 ) {
                    overflowBuffer.append( allSamples, residual, length-residual );
                }
            }
        } else {
            if ( input instanceof DataStartSignal ) {
                DataStartSignal startSignal = (DataStartSignal)input;

                createWindow( startSignal.getSampleRate() );

                // attach the frame-length and the shift-length to the start-signal to allow
                // detection of incorrect frontend settings
                Map<String, Object> props = startSignal.getProps();
                props.put( WINDOW_SHIFT_SAMPLES, windowShift );
                props.put( WINDOW_SIZE_SAMPLES, cosineWindow.length );

                // reset the current first sample number
                currentFirstSampleNumber = -1;
            } else if ( input instanceof SpeechStartSignal ) {
                // reset the current first sample number
                currentFirstSampleNumber = -1;
            } else if ( input instanceof DataEndSignal||input instanceof SpeechEndSignal ) {
                // end of utterance handling
                processUtteranceEnd();
            }

            if ( log.isTraceEnabled() ) {
                log.trace( "Adding Exchange to queue: "+copy );
            }
            endpoint.getQueue().add( exchange );
        }

        callback.done( true );
        return true;
    }

    private void createWindow ( int sampleRate )
    {
        if ( cosineWindow!=null&&sampleRate==this.sampleRate ) {
            return;
        }

        this.sampleRate = sampleRate;

        int windowSize = DataUtil.getSamplesPerWindow( sampleRate, windowSizeInMs );
        cosineWindow = new double[ windowSize ];

        windowShift = DataUtil.getSamplesPerShift( sampleRate, windowShiftInMs );

        if ( cosineWindow.length>1 ) {
            double oneMinusAlpha = ( 1-alpha );
            for ( int i = 0; i<cosineWindow.length; i++ ) {
                cosineWindow[i] = oneMinusAlpha-alpha*Math.cos( 2*Math.PI*i/( (double)cosineWindow.length-1.0 ) );
            }
        }

        overflowBuffer = new DoubleBuffer( windowSize );
    }

    /**
     * What happens when an DataEndSignal is received. Basically pads up to a window of the overflow buffer with zeros,
     * and then apply the Hamming window to it. Checks if buffer has data.
     */
    private void processUtteranceEnd ()
    {
        if ( overflowBuffer.getOccupancy()>0 ) {
            overflowBuffer.padWindow( cosineWindow.length );
            applyRaisedCosineWindow( overflowBuffer.getBuffer(), cosineWindow.length );
            overflowBuffer.reset();
        }
    }

    /**
     * Applies the Hamming window to the given double array. The windows are added to the output queue. Returns the
     * index of the first array element of next window that is not produced because of insufficient data.
     *
     * @param in     the audio data to apply window and the Hamming window
     * @param length the number of elements in the array to apply the RaisedCosineWindow
     * @return the index of the first array element of the next window
     */
    private int applyRaisedCosineWindow ( double[] in, int length )
    {

        int windowCount;

        // if no windows can be created but there is some data,
        // pad it with zeros
        if ( length<cosineWindow.length ) {
            double[] padded = new double[ cosineWindow.length ];
            System.arraycopy( in, 0, padded, 0, length );
            in = padded;
            windowCount = 1;
        } else {
            windowCount = getWindowCount( length, cosineWindow.length, windowShift );
        }

        // create all the windows at once, not individually, saves time
        double[][] windows = new double[ windowCount ][ cosineWindow.length ];

        int windowStart = 0;

        for ( int i = 0; i<windowCount; windowStart += windowShift, i++ ) {

            double[] myWindow = windows[i];

            // apply the Hamming Window function to the window of data
            for ( int w = 0, s = windowStart; w<myWindow.length; s++, w++ ) {
                myWindow[w] = in[s]*cosineWindow[w];
            }

            // add the frame to the output queue
            Exchange exchange = new DefaultExchange( endpoint );
            Message message = new DefaultMessage();
            message.getHeaders().putAll(incomingHeaders);
            
            message.setBody( new DoubleData( myWindow, sampleRate, currentCollectTime, currentFirstSampleNumber ) );
            exchange.setIn( message );
            endpoint.getQueue().add( exchange );
            currentFirstSampleNumber += windowShift;
        }

        return windowStart;
    }

    /**
     * Returns the number of windows in the given array, given the windowSize and windowShift.
     *
     * @param arraySize   the size of the array
     * @param windowSize  the window size
     * @param windowShift the window shift
     * @return the number of windows
     */
    private static int getWindowCount ( int arraySize, int windowSize, int windowShift )
    {
        if ( arraySize<windowSize ) {
            return 0;
        } else {
            int windowCount = 1;
            for ( int windowEnd = windowSize;
                    windowEnd+windowShift<=arraySize;
                    windowEnd += windowShift ) {
                windowCount++;
            }
            return windowCount;
        }
    }

    /**
     * Returns the shift size used to window the incoming speech signal. This value might be used by other components to
     * determine the time resolution of feature vectors.
     * @return the shift of the window
     */
    public float getWindowShiftInMs ()
    {
        if ( windowShiftInMs==0 ) {
            throw new RuntimeException( this+" was not initialized yet!" );
        }

        return windowShiftInMs;
    }

    public int getSampleRate ()
    {
        return sampleRate;
    }

    /**
     * Rounds a given sample-number to the number of samples will be processed by this instance including the padding
     * samples at the end..
     * @param samples
     */
    public long roundToFrames ( long samples )
    {
        int windowSize = DataUtil.getSamplesPerWindow( sampleRate, windowSizeInMs );
        int windowShift = DataUtil.getSamplesPerShift( sampleRate, windowShiftInMs );

        long mxNumShifts = samples/windowShift;

        for ( int i = (int)mxNumShifts;; i-- ) {
            long remainingSamples = samples-windowShift*i;

            if ( remainingSamples>windowSize ) {
                return windowShift*( i+1 )+windowSize;
            }
        }
    }

    class DoubleBuffer
    {
        private final double[] buffer;
        private int occupancy;

        /** Constructs a DoubleBuffer of the given size.
         * @param size*/
        DoubleBuffer ( int size )
        {
            buffer = new double[ size ];
            occupancy = 0;
        }

        /**
         * Returns the number of elements in this DoubleBuffer.
         *
         * @return the number of elements in this DoubleBuffer.
         */
        public int getOccupancy ()
        {
            return occupancy;
        }

        /**
         * Returns the underlying double array used to store the data.
         *
         * @return the underlying double array
         */
        public double[] getBuffer ()
        {
            return buffer;
        }

        /**
         * Appends all the elements in the given array to this DoubleBuffer.
         *
         * @param src the array to copy from
         * @return the resulting number of elements in this DoubleBuffer.
         */
        public int appendAll ( double[] src )
        {
            return append( src, 0, src.length );
        }

        /**
         * Appends the specified elements in the given array to this DoubleBuffer.
         *
         * @param src    the array to copy from
         * @param srcPos where in the source array to start from
         * @param length the number of elements to copy
         * @return the resulting number of elements in this DoubleBuffer
         */
        public int append ( double[] src, int srcPos, int length )
        {
            if ( occupancy+length>buffer.length ) {
                throw new Error( "RaisedCosineWindower: "
                        +"overflow-buffer: attempting to fill "
                        +"buffer beyond its capacity." );
            }
            System.arraycopy( src, srcPos, buffer, occupancy, length );
            occupancy += length;
            return occupancy;
        }

        /**
         * If there are less than windowSize elements in this DoubleBuffer, pad the up to windowSize elements with zero.
         *
         * @param windowSize the window size
         */
        public void padWindow ( int windowSize )
        {
            if ( occupancy<windowSize ) {
                Arrays.fill( buffer, occupancy, windowSize, 0 );
            }
        }

        /** Sets the number of elements in this DoubleBuffer to zero, without actually remove the elements. */
        public void reset ()
        {
            occupancy = 0;
        }
    }
}
