/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.recogniser.frontend.process;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.endpoint.SpeechStartSignal;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.loadbalancer.SimpleLoadBalancerSupport;
import org.apache.log4j.Logger;

/**
 *
 * @author gorg
 */
public class SpeechLoadBalancer extends SimpleLoadBalancerSupport
{
    static final Logger logger = Logger.getLogger( SpeechLoadBalancer.class );
    private static Boolean useSpeechSignals;
    private int id;                       // if number of features exceeds the limit send to stop
    private boolean dataStart = false;
    private boolean speechStart = false;

    public void process ( Exchange exchange ) throws Exception
    {
        Data data = (Data)exchange.getIn().getBody();

        Processor target = chooseDataProcessor( data );
        exchange.getIn().setHeader( "id", id );
        exchange.getIn().setBody( data );
        target.process( exchange );
    }

    protected Processor chooseDataProcessor ( Data data )
    {
        if ( data instanceof DataStartSignal&&useSpeechSignals==null ) {
            handleDataStartSignal( (DataStartSignal)data );
            dataStart = true;
            id = -1;
            return getProcessors().get( 1 );
        } else if ( dataStart ) {
            if ( useSpeechSignals ) {
                if ( data instanceof SpeechStartSignal ) {
                    speechStart = true;
                    return getProcessors().get( 0 );
                } else if ( speechStart&&( data instanceof DoubleData||data instanceof FloatData ) ) {
                    id++;
                    return getProcessors().get( 1 );
                } else {
                    return getProcessors().get( 0 );
                }
            } else {
                if ( data instanceof DoubleData||data instanceof FloatData ) {
                    speechStart = true;
                    id++;
                    return getProcessors().get( 1 );
                } else {
                    return getProcessors().get( 1 );
                }
            }
        } else if ( data==null ) {
            return getProcessors().get( 0 );
        } else {
            //error first should be DataStartSignal
            assert data instanceof DataStartSignal :
                    "The first element in an sphinx4-feature stream must be a DataStartSignal but was a "+data.getClass().getSimpleName();
            return getProcessors().get( 0 );
        }
    }

    public void handleDataStartSignal ( DataStartSignal dataStartSignal )
    {
        Map<String, Object> dataProps = dataStartSignal.getProps();
        useSpeechSignals = dataProps.containsKey( DataStartSignal.SPEECH_TAGGED_FEATURE_STREAM )
                &&(Boolean)dataProps.get( DataStartSignal.SPEECH_TAGGED_FEATURE_STREAM );
    }
}
