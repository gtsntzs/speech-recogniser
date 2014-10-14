/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.frontend.process;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;

/**
 *
 * @author gorg
 */
public class FeatureDumper
{
    private FrontEnd frontEnd;
    private AudioFileDataSource audioSource;
    private StreamDataSource streamsource;
    private List<float[]> allFeatures;
    private int featureLength = -1;
    /** The logger for this class */
    private static final Logger logger = Logger.getLogger( "org.apache.servicemix.sphinx.audiostreamer.FeatureDumper" );

    public FeatureDumper ()
    {
    }

    public FeatureDumper ( String frontEndName, String configFile )
    {
        try {
            URL url;
            if ( configFile!=null ) {
                url = new File( configFile ).toURI().toURL();
            } else {
                url = FeatureDumper.class.getResource( "config.xml" );
            }
            ConfigurationManager cm = new ConfigurationManager( url );

            frontEnd = (FrontEnd)cm.lookup( frontEndName );
            audioSource = (AudioFileDataSource)cm.lookup( "audioFileDataSource" );

//            processFile( inputStream, outputFile, format );
//            processFile( inputStream );
        } catch ( IOException ioe ) {
            System.err.println( "I/O Error "+ioe );
        } catch ( PropertyException p ) {
            System.err.println( "Bad configuration "+p );
        }
    }

    public void initiallizeStreamDataSourceConfig ( File configFile )
    {
//        String frontEndName = "epFrontEnd";
        String frontEndName = "noSilSpFrontEnd";
        try {
            URL url = configFile.toURI().toURL();

            System.out.println( "\n Received Object Type: "+url.toString() );

            ConfigurationManager cm = new ConfigurationManager( url );

            frontEnd = (FrontEnd)cm.lookup( frontEndName );
//            streamsource = (StreamDataSource)cm.lookup( "streamDataSource" );
        } catch ( IOException ioe ) {
            System.err.println( "I/O Error "+ioe );
        } catch ( PropertyException p ) {
            System.err.println( "Bad configuration "+p );
        }
    }

    public void setStreamer ( StreamDataSource stream )
    {
        frontEnd.setDataSource( stream );
    }

    public void initiallizeAudioFileDataSourceConfig ( File configFile )
    {
//        String frontEndName = "epFrontEnd";
        String frontEndName = "noSilSpFrontEnd";
        try {
            URL url = configFile.toURI().toURL();

            System.out.println( "\n Received Object Type: "+url.toString() );

            ConfigurationManager cm = new ConfigurationManager( url );

            frontEnd = (FrontEnd)cm.lookup( frontEndName );
            audioSource = (AudioFileDataSource)cm.lookup( "audioFileDataSource" );
        } catch ( IOException ioe ) {
            System.err.println( "I/O Error "+ioe );
        } catch ( PropertyException p ) {
            System.err.println( "Bad configuration "+p );
        }
    }

    /**
     * Process the file and store the features
     *
     * @param inputAudioFile
     *            the input audio file
     * @throws FileNotFoundException
     */
    public void processBinaryFile ( File inputFile ) throws FileNotFoundException
    {
        streamsource.setInputStream( new FileInputStream( inputFile ), "streamData" );
        allFeatures = new LinkedList<float[]>();
        getAllFeatures();

        for ( float[] feature : allFeatures ) {
            String str = "";
            for ( float val : feature ) {
                str = str+Float.toString( val )+" ";
            }
            System.out.println( str );
        }
//        logger.log( Level.INFO, "Frames: ", allFeatures.size() );
    }

    /**
     * Process the file and store the features
     *
     * @param inputAudioFile
     *            the input audio file
     * @throws FileNotFoundException
     */
    public void processWavFile ( AudioInputStream inputStream ) throws FileNotFoundException
    {
        audioSource.setInputStream( (AudioInputStream)inputStream, "" );
        allFeatures = new LinkedList<float[]>();
        getAllFeatures();

        for ( float[] feature : allFeatures ) {
            String str = "";
            for ( float val : feature ) {
                str = str+Float.toString( val )+" ";
            }
            System.out.println( str );
        }
//        logger.log( Level.INFO, "Frames: ", allFeatures.size() );
    }

    /**
     * Retrieve all Features from the frontend, and cache all those with actual
     * feature data.
     */
    private void getAllFeatures ()
    {
        /*
         * Run through all the data and produce feature.
         */
        try {
            assert ( allFeatures!=null );
            Data feature = frontEnd.getData();
            while ( !( feature instanceof DataEndSignal ) ) {
                if ( feature instanceof DoubleData ) {
                    double[] featureData = ( (DoubleData)feature ).getValues();
                    if ( featureLength<0 ) {
                        featureLength = featureData.length;
//                        logger.log( Level.INFO, "Feature length: {0}", featureLength );
                    }
                    float[] convertedData = new float[ featureData.length ];
                    for ( int i = 0; i<featureData.length; i++ ) {
                        convertedData[i] = (float)featureData[i];
                    }
                    allFeatures.add( convertedData );
                    System.out.print( "size: "+featureData.length );
                    for ( double i : featureData ) {
                        System.out.print( "  "+i );
                    }
                    System.out.println();
                } else if ( feature instanceof FloatData ) {
                    float[] featureData = ( (FloatData)feature ).getValues();
                    if ( featureLength<0 ) {
                        featureLength = featureData.length;
//                        logger.log( Level.INFO, "Feature length: {0}", featureLength );
                    }

                    allFeatures.add( featureData );
                }
                feature = frontEnd.getData();
            }
        } catch ( Exception e ) {
        }
    }

    public void getAllFeatures ( Data feature )
    {
        /*
         * Run through all the data and produce feature.
         */
        try {

            allFeatures = new LinkedList<float[]>();
//            assert ( allFeatures!=null );
//            Data feature = frontEnd.getData();
            while ( !( feature instanceof DataEndSignal ) ) {
                if ( feature instanceof DoubleData ) {
                    double[] featureData = ( (DoubleData)feature ).getValues();
                    if ( featureLength<0 ) {
                        featureLength = featureData.length;
//                        logger.log( Level.INFO, "Feature length: {0}", featureLength );
                    }
                    float[] convertedData = new float[ featureData.length ];
                    for ( int i = 0; i<featureData.length; i++ ) {
                        convertedData[i] = (float)featureData[i];
                    }
                    allFeatures.add( convertedData );
                    System.out.print( "size: "+featureData.length );
                    for ( double i : featureData ) {
                        System.out.print( "  "+i );
                    }
                    System.out.println();
                } else if ( feature instanceof FloatData ) {
                    float[] featureData = ( (FloatData)feature ).getValues();
                    if ( featureLength<0 ) {
                        featureLength = featureData.length;
//                        logger.log( Level.INFO, "Feature length: {0}", featureLength );
                    }

                    allFeatures.add( featureData );
                }
//                feature = frontEnd.getData();
            }
        } catch ( Exception e ) {
        }
    }
}
