package soa.speech.recogniser;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the component that manages {@link deltafeatureEndpoint}.
 */
public class deltafeatureComponent extends DefaultComponent
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    
    public synchronized BlockingQueue<Exchange> createQueue ( String uri, Map<String, Object> parameters )
    {
        getCamelContext().getRegistry();
        // create queue
        BlockingQueue<Exchange> queue;
        Integer size = getAndRemoveParameter( parameters, "size", Integer.class );
        if ( size!=null&&size>0 ) {
            queue = new LinkedBlockingQueue<Exchange>( size );
        } else {
            queue = new LinkedBlockingQueue<Exchange>();
        }
        return queue;
    }

    @Override
    protected Endpoint createEndpoint ( String uri, String remaining, Map<String, Object> parameters ) throws Exception
    {
    	deltafeatureEndpoint answer = new deltafeatureEndpoint( uri, this, createQueue( uri, parameters ) );
    	setProperties(answer, parameters);

    	return answer;
    }

    protected String getQueueKey ( String uri )
    {
        if ( uri.contains( "?" ) ) {
            uri = uri.substring( 0, uri.indexOf( '?' ) );
        }
        return uri;
    }

    @Override
    protected void doStop () throws Exception
    {
        super.doStop();
    }
}
