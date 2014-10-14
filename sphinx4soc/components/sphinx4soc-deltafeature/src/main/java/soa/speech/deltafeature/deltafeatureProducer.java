package soa.speech.deltafeature;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.Signal;
import edu.cmu.sphinx.frontend.endpoint.SpeechEndSignal;

/**
 * The deltafeature producer.
 */
public abstract class deltafeatureProducer extends DefaultAsyncProducer  
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final deltafeatureEndpoint endpoint;
    private int window = 3;
    private int bufferPosition;
    private Signal pendingSignal;
    protected int cepstraBufferEdge;
    protected int currentPosition;
    protected int cepstraBufferSize;
    protected DoubleData[] cepstraBuffer;
    private int startcounter = -1;
    private boolean flagStart = true;
    private boolean flagEnd = false;
    private int numberFeatures = 0;
    /////////////////////////////////////////////////////////////
    private Map<String,Object> incomingHeaders;

    public deltafeatureProducer ( deltafeatureEndpoint endpoint, BlockingQueue<Exchange> queue )
    {
        super( endpoint);
        this.endpoint = endpoint;
        this.window = endpoint.getWindow();
    }
    /*
     *
     */

    public void initialize ()
    {
        cepstraBufferSize = 256;
        cepstraBuffer = new DoubleData[ cepstraBufferSize ];
        cepstraBufferEdge = cepstraBufferSize-( window*2+2 );
        reset();
    }

    /** Resets the DeltasFeatureExtractor to be ready to read the next segment of data. */

    private void reset ()
    {
        bufferPosition = 0;
        currentPosition = 0;
    }

    public boolean process ( final Exchange exchange, final AsyncCallback callback ) throws DataProcessingException
    {
        Exchange copy = ExchangeHelper.createCorrelatedCopy( exchange, true );
        incomingHeaders = exchange.getIn().getHeaders();
        // set a new from endpoint to be the seda queue
        copy.setFromEndpoint( endpoint );

        Data input = (Data)copy.getIn().getBody();
        if ( flagStart ) {
            if ( input instanceof DoubleData ) {
                addCepstrum( (DoubleData)input );
                computeFeatures( 1 );
            } else if ( input instanceof DataStartSignal ) {
                pendingSignal = null;
                add2OutputQueue( input );
                flagStart = false;

            } else if ( input instanceof DataEndSignal||input instanceof SpeechEndSignal ) {
                int n = replicateLastCepstrum();
                computeFeatures( n );
                add2OutputQueue( input );
            } else {
                add2OutputQueue( input );
            }
        } else {
            startcounter++;
            int output;
            if ( startcounter==0 ) {
                output = replicateFirstCepstrum( input );
            } else {
                output = processDataStart( input );
            }

            if ( startcounter==window||flagEnd==true ) {
                computeFeatures( output );
                if ( pendingSignal!=null ) {
                    add2OutputQueue( pendingSignal );
                }
                flagEnd = false;
                flagStart = true;
                numberFeatures = 0;
                startcounter = -1;
            }
        }
        return true;
    }

    private void add2OutputQueue ( Data input )
    {
        // add the frame to the output queue
        Exchange exchange = new DefaultExchange( endpoint );
        Message message = new DefaultMessage();
        message.getHeaders().putAll(incomingHeaders);
        message.setBody( input );
        exchange.setIn( message );
        endpoint.getQueue().add( exchange );
    }

    private int replicateFirstCepstrum ( Data cepstrum )
    {
        if ( cepstrum instanceof DataEndSignal ) {
            add2OutputQueue( cepstrum );
        } else if ( cepstrum instanceof DataStartSignal ) {
            throw new Error( "Too many UTTERANCE_START" );
        } else {
            // At the start of an utterance, we replicate the first frame
            // into window+1 frames, and then read the next "window" number
            // of frames. This will allow us to compute the delta-
            // double-delta of the first frame.
            Arrays.fill( cepstraBuffer, 0, window+1, cepstrum );
            bufferPosition = window+1;
            bufferPosition %= cepstraBufferSize;
            currentPosition = window;
            currentPosition %= cepstraBufferSize;
        }
        return 0;
    }

    public int processDataStart ( Data cepstrum )
    {
        numberFeatures++;
        pendingSignal = null;
        if ( cepstrum instanceof DoubleData ) {
            // just a cepstra
            addCepstrum( (DoubleData)cepstrum );
        } else if ( cepstrum instanceof DataEndSignal||cepstrum instanceof SpeechEndSignal ) {
            // end of segment cepstrum
            pendingSignal = (Signal)cepstrum;
            replicateLastCepstrum();
            flagEnd = true;
            return numberFeatures;
        } else if ( cepstrum instanceof DataStartSignal ) {
            logger.error("Too many UTTERANCE_START");
            throw new Error( "Too many UTTERANCE_START" );
        }
        return 1;
    }

    /**
     * Adds the given DoubleData object to the cepstraBuffer.
     *
     * @param cepstrum the DoubleData object to add
     */
    private void addCepstrum ( DoubleData cepstrum )
    {
        cepstraBuffer[bufferPosition++] = cepstrum;
        bufferPosition %= cepstraBufferSize;
    }

    /**
     * Replicate the last frame into the last window number of frames in the cepstraBuffer.
     *
     * @return the number of replicated Cepstrum
     */
    private int replicateLastCepstrum ()
    {
        DoubleData last;
        if ( bufferPosition>0 ) {
            last = cepstraBuffer[bufferPosition-1];
        } else if ( bufferPosition==0 ) {
            last = cepstraBuffer[cepstraBuffer.length-1];
        } else {
            throw new Error( "BufferPosition < 0" );
        }
        for ( int i = 0; i<window; i++ ) {
            addCepstrum( last );
        }
        return window;
    }

    /**
     * Converts the Cepstrum data in the cepstraBuffer into a FeatureFrame.
     *
     * @param totalFeatures the number of Features that will be produced
     */
    private void computeFeatures ( int totalFeatures )
    {
        if ( totalFeatures==1 ) {
            computeFeature();
        } else {
            // create the Features
            for ( int i = 0; i<totalFeatures; i++ ) {
                computeFeature();
            }
        }
    }

    /** Computes the next Feature. */
    private void computeFeature ()
    {
        Data feature = computeNextFeature();

        // add the frame to the output queue
        Exchange exchange = new DefaultExchange( endpoint );
        Message message = new DefaultMessage();
        message.getHeaders().putAll(incomingHeaders);
        message.setBody( feature );
        exchange.setIn( message );

        endpoint.getQueue().add( exchange );
    }

    @Override
    protected void doStart ()
    {
        initialize();
    }

    /**
     * Computes the next feature. Advances the pointers as well.
     *
     * @return the feature Data computed
     */
    protected abstract Data computeNextFeature ();
}
