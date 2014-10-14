package soa.speech.deltafeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;


/**
 * Represents a deltafeature endpoint.
 */
public class deltafeatureEndpoint extends DefaultEndpoint
{
    private volatile BlockingQueue<Exchange> queue;
    private int size;
    private deltafeatureProducer producer;
    private deltafeatureConsumer consumer;
    /** The property for the window of the DeltasFeatureExtractor. */
    private int window = 4;

    public deltafeatureEndpoint ()
    {
    }

    public deltafeatureEndpoint ( String endpointUri, Component component, BlockingQueue<Exchange> queue )
    {
        super( endpointUri, component );
        this.queue = queue;
        this.size = queue.remainingCapacity();

    }

    public deltafeatureEndpoint ( String endpointUri, Component component, BlockingQueue<Exchange> queue, Map<String, Object> parameters )
    {
        super( endpointUri, component );
        this.queue = queue;
        this.size = queue.remainingCapacity();

    }

    public Producer createProducer () throws Exception
    {
        producer = new DeltasFeatureExtractor( this, getQueue() );
        return producer;
    }

    public Consumer createConsumer ( Processor processor ) throws Exception
    {
        consumer = new deltafeatureConsumer( this, processor );
        return consumer;
    }

    public synchronized BlockingQueue<Exchange> getQueue ()
    {
        if ( queue==null ) {
            if ( size>0 ) {
                queue = new LinkedBlockingQueue<Exchange>( size );
            } else {
                queue = new LinkedBlockingQueue<Exchange>();
            }
        }
        return queue;
    }

    public void setQueue ( BlockingQueue<Exchange> queue )
    {
        this.queue = queue;
        this.size = queue.remainingCapacity();
    }

    public int getWindow ()
    {
        return window;
    }

    public void setWindow ( int window )
    {
        this.window = window;
    }

    public int getSize ()
    {
        return size;
    }

    public void setSize ( int size )
    {
        this.size = size;
    }

    public boolean isSingleton ()
    {
        return true;
    }

    public void getObject ()
    {
    }

    /**
     * Returns the current pending exchanges
     */
    public List<Exchange> getExchanges ()
    {
        return new ArrayList<Exchange>( getQueue() );
    }

    void onStarted ( deltafeatureProducer producer )
    {
        this.producer = producer;
    }

    void onStopped ( deltafeatureProducer producer )
    {
        //noop
    }

    void onStarted ( deltafeatureConsumer consumer ) throws Exception
    {
        this.consumer = consumer;
    }

    void onStopped ( deltafeatureConsumer consumer ) throws Exception
    {
        // noop
    }
}
