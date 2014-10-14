/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.recogniser.frontend.process;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.loadbalancer.SimpleLoadBalancerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;

/**
 * 
 * @author gorg
 */
public class DecoderDataLoadBalancer extends SimpleLoadBalancerSupport
{
    private final Logger LOG = LoggerFactory.getLogger(this.getClass().getName());	
    private long id = -1;                       // if number of features exceeds the limit send to stop
    private final int skipframes;
    private int framesskiped;

    public DecoderDataLoadBalancer(int skipframes) {
		super();
		this.skipframes = skipframes;
		this.framesskiped = skipframes;
	}

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
    	if ( data instanceof DataStartSignal ) {
    		framesskiped = skipframes;
            return getProcessors().get( 0 );
        } else if ( data instanceof DoubleData||data instanceof FloatData ) {
            if ( framesskiped!=0 ) {
            	framesskiped--;
                return getProcessors().get( 0 );
            }
            id++;
            return getProcessors().get( 1 );
        } else if ( data instanceof DataEndSignal ) {
            id++;
            return getProcessors().get( 1 );
        } else if ( data==null ) {
            return getProcessors().get( 0 );
        } else {
            return null;
        }
    }

}
