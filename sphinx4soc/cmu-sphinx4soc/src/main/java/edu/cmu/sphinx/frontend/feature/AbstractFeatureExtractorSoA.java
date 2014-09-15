/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.sphinx.frontend.feature;

import edu.cmu.sphinx.frontend.*;
import edu.cmu.sphinx.frontend.endpoint.*;
import edu.cmu.sphinx.util.props.*;

import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author gorg
 */
public abstract class AbstractFeatureExtractorSoA extends BaseDataProcessor
{
    static final Logger loger = Logger.getLogger( AbstractFeatureExtractorSoA.class );
    /** The property for the window of the DeltasFeatureExtractor. */
    @S4Integer(defaultValue = 3)
    public static final String PROP_FEATURE_WINDOW = "windowSize";
    private int bufferPosition;
    private Signal pendingSignal;
    private LinkedList<Data> outputQueue;
    protected int cepstraBufferEdge;
    protected int window;
    protected int currentPosition;
    protected int cepstraBufferSize;
    protected DoubleData[] cepstraBuffer;
    private int numOfFeuture = 0;

    /**
     *
     * @param window
     */
    public AbstractFeatureExtractorSoA ( int window )
    {
        initLogger();
        this.window = window;
    }

    public AbstractFeatureExtractorSoA ()
    {
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.cmu.sphinx.util.props.Configurable#newProperties(edu.cmu.sphinx.util.props.PropertySheet)
     */
    @Override
    public void newProperties ( PropertySheet ps ) throws PropertyException
    {
        super.newProperties( ps );
        window = ps.getInt( PROP_FEATURE_WINDOW );
    }


    /*
     * (non-Javadoc)
     *
     * @see edu.cmu.sphinx.frontend.DataProcessor#initialize(edu.cmu.sphinx.frontend.CommonConfig)
     */
    @Override
    public void initialize ()
    {
        super.initialize();
        cepstraBufferSize = 256;
        cepstraBuffer = new DoubleData[ cepstraBufferSize ];
        cepstraBufferEdge = cepstraBufferSize-( window*2+2 );
        outputQueue = new LinkedList<Data>();
        reset();
    }

    /** Resets the DeltasFeatureExtractor to be ready to read the next segment of data. */
    private void reset ()
    {
        bufferPosition = 0;
        currentPosition = 0;
    }
    int startcounter = -1;
    boolean flagStart = true;
    boolean flagEnd = false;
    int numberFeatures = 0;

    public boolean process ( Data input ) throws DataProcessingException
    {
//        loger.info( input );
        if ( flagStart ) {
            if ( input instanceof DoubleData ) {
                loger.info( "DoubleData time: "+( (DoubleData)input ).getCollectTime() );
                addCepstrum( (DoubleData)input );
                computeFeatures( 1 );
            } else if ( input instanceof DataStartSignal ) {
                loger.info( "DataStartSignal time: "+( (DataStartSignal)input ).getTime() );
                pendingSignal = null;
                outputQueue.add( input );
                flagStart = false;

            } else if ( input instanceof DataEndSignal||input instanceof SpeechEndSignal ) {
                loger.info( "DataEndSignal time: "+( (DataEndSignal)input ).getTime() );
                // when the DataEndSignal is right at the boundary
                int n = replicateLastCepstrum();
                computeFeatures( n );
                outputQueue.add( input );
            } else {
                outputQueue.add( input );
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
                    outputQueue.add( pendingSignal );
                }
                flagEnd = false;
                flagStart = true;
                numberFeatures = 0;
                startcounter = -1;
            }
        }
        return true;
    }

    @Override
    public Data getData () throws DataProcessingException
    {
        return null;
    }

    private int replicateFirstCepstrum ( Data cepstrum )
    {
        if ( cepstrum instanceof DataEndSignal ) {
            outputQueue.add( cepstrum );
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
        getTimer().start();
        if ( totalFeatures==1 ) {
            computeFeature();
        } else {
            // create the Features
            for ( int i = 0; i<totalFeatures; i++ ) {
                computeFeature();
            }
        }
        getTimer().stop();
    }

    /** Computes the next Feature. */
    private void computeFeature ()
    {
        Data feature = computeNextFeature();
        String str = "";
        for ( float f : ( (FloatData)feature ).getValues() ) {
            str = str+" "+f;
        }
//        loger.info( str+" "+numOfFeuture++ );
//        loger.info( numOfFeuture++ );
        outputQueue.add( feature );
    }

    /**
     * Computes the next feature. Advances the pointers as well.
     *
     * @return the feature Data computed
     */
    protected abstract Data computeNextFeature ();
}
