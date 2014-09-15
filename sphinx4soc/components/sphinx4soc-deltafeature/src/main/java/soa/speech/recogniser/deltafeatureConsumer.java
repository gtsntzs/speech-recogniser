package soa.speech.recogniser;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.impl.LoggingExceptionHandler;
import org.apache.camel.spi.ExceptionHandler;
import org.apache.camel.spi.ShutdownAware;
import org.apache.camel.support.ServiceSupport;
import org.apache.camel.util.AsyncProcessorConverterHelper;
import org.apache.camel.util.AsyncProcessorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The deltafeature consumer.
 */
public class deltafeatureConsumer extends ServiceSupport implements Consumer, Runnable, ShutdownAware
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private deltafeatureEndpoint endpoint;
    private AsyncProcessor processor;
    private ExecutorService executor;
    private ExceptionHandler exceptionHandler;

    public deltafeatureConsumer ( deltafeatureEndpoint endpoint, Processor processor )
    {
        this.endpoint = endpoint;
        this.processor = AsyncProcessorConverterHelper.convert( processor );
    }

    @Override
    public String toString ()
    {
        return "FeatureExtractorConsumer["+endpoint.getEndpointUri()+"]";
    }

    public Endpoint getEndpoint ()
    {
        return endpoint;
    }

    public ExceptionHandler getExceptionHandler ()
    {
        if ( exceptionHandler==null ) {
            exceptionHandler = new LoggingExceptionHandler(endpoint.getCamelContext(), getClass(), LoggingLevel.ERROR);
        }
        return exceptionHandler;
    }

    public void setExceptionHandler ( ExceptionHandler exceptionHandler )
    {
        this.exceptionHandler = exceptionHandler;
    }

    public Processor getProcessor ()
    {
        return processor;
    }

    public boolean deferShutdown ( ShutdownRunningTask shutdownRunningTask )
    {
        // deny stopping on shutdown as we want seda consumers to run in case some other queues
        // depend on this consumer to run, so it can complete its exchanges
        return true;
    }

    public int getPendingExchangesSize ()
    {
        // number of pending messages on the queue
        return endpoint.getQueue().size();
    }

    public void run ()
    {
        BlockingQueue<Exchange> queue = endpoint.getQueue();
        while ( queue!=null&&isRunAllowed() ) {
            final Exchange exchange;

            try {
                exchange = queue.poll( 1000, TimeUnit.MILLISECONDS );
            } catch ( InterruptedException e ) {
                	logger.debug( "Sleep interrupted, are we stopping? "+( isStopping()||isStopped() ) );
                continue;
            }
            if ( exchange!=null ) {
                if ( isRunAllowed() ) {
                    try {
                        sendToConsumer( exchange );
                        
                        // log exception if an exception occurred and was not handled
                        if ( exchange.getException()!=null ) {
                            getExceptionHandler().handleException( "Error processing exchange", exchange, exchange.getException() );
                        }
                    } catch ( Exception e ) {
                        getExceptionHandler().handleException( "Error processing exchange", exchange, e );
                    }
                } else {
                    	logger.warn( "This consumer is stopped during polling an exchange, so putting it back on the seda queue: "+exchange );
                    try {
                        queue.put( exchange );
                    } catch ( InterruptedException e ) {
                        	logger.debug( "Sleep interrupted, are we stopping? "+( isStopping()||isStopped() ) );
                        continue;
                    }
                }
            }
        }
    }

    /**
     * Send the given {@link Exchange} to the consumer(s).
     * <p/>
     * If multiple consumers then they will each receive a copy of the Exchange.
     * A multicast processor will send the exchange in parallel to the multiple consumers.
     * <p/>
     * If there is only a single consumer then its dispatched directly to it using same thread.
     *
     * @param exchange the exchange
     * @throws Exception can be thrown if processing of the exchange failed
     */
    protected void sendToConsumer ( Exchange exchange ) throws Exception
    {
        // use the regular processor and use the asynchronous routing engine to support it
        AsyncProcessorHelper.process( processor, exchange);
    }

    protected void doStart () throws Exception
    {
        int poolSize = 1; //endpoint.getConcurrentConsumers();
        executor = endpoint.getCamelContext().getExecutorServiceManager().newFixedThreadPool( this, endpoint.getEndpointUri(), poolSize );
        for ( int i = 0; i<poolSize; i++ ) {
            executor.execute( this );
        }
        endpoint.onStarted( this );
    }

    protected void doStop () throws Exception
    {
        endpoint.onStopped( this );
        // must shutdown executor on stop to avoid overhead of having them running
        endpoint.getCamelContext().getExecutorServiceManager().shutdown( executor );
        executor = null;
    }

    public void prepareShutdown ()
    {
    }

	@Override
	public void prepareShutdown(boolean forced) {
		
	}

}
