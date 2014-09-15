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

/**
 * 
 * @author gorg
 */
public class FrontendDataLoadBalancer extends SimpleLoadBalancerSupport
{
    private transient final Logger LOG = LoggerFactory.getLogger(this.getClass().getName());
	
    public FrontendDataLoadBalancer() {
	}

	public void process ( Exchange exchange ) throws Exception
    {
        Data data = (Data)exchange.getIn().getBody();
        
        Processor target = chooseDataProcessor( data );
        exchange.getIn().setBody( data );
        target.process( exchange );
    }

    protected Processor chooseDataProcessor ( Data data )
    {
        if ( data instanceof DataEndSignal ) {
            return getProcessors().get( 1 );
        } else{
            return getProcessors().get( 0 );
        }
    }
}
