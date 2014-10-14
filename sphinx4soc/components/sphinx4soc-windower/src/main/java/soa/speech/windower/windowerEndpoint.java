package soa.speech.windower;

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
import org.apache.camel.spi.UriParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a windower endpoint.
 */
public class windowerEndpoint  extends DefaultEndpoint
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    
    private volatile BlockingQueue<Exchange> queue;
    private int size;
    private windowerProducer producer;
    private windowerConsumer consumer;
    /** The property for window size in milliseconds. */
    @UriParam
    private float windowSizeInMs = 25.625f;
    /** The property for window shift in milliseconds, which has a default value of 10F. */
    @UriParam
    private float windowShiftInMs = 10.0f;
    /** The property for the alpha value of the Window, which is the value for the RaisedCosineWindow. */
    @UriParam
    private double alpha = 0.46d;

    public windowerEndpoint ()
    {
    }

    public windowerEndpoint ( String endpointUri, Component component, BlockingQueue<Exchange> queue, Map<String, Object> parameters ){

        super( endpointUri, component );
        this.queue = queue;
        this.size = queue.remainingCapacity();
    }
    
    public windowerEndpoint ( String endpointUri, Component component, BlockingQueue<Exchange> queue ) throws Exception
    {
        super( endpointUri, component );
        this.queue = queue;
        this.size = queue.remainingCapacity();
        
    }

    public Producer createProducer  () throws Exception
    {
        producer = new windowerProducer( this );
        return producer;
    }

    public Consumer createConsumer ( Processor processor ) throws Exception
    {
        consumer = new windowerConsumer( this, processor );
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

    public double getAlpha ()
    {
        return alpha;
    }

    public void setAlpha ( double alpha )
    {
        this.alpha = alpha;
    }

    public float getWindowShiftInMs ()
    {
        return windowShiftInMs;
    }

    public void setWindowShiftInMs ( float windowShiftInMs )
    {
        this.windowShiftInMs = windowShiftInMs;
    }

    public float getWindowSizeInMs ()
    {
        return windowSizeInMs;
    }

    public void setWindowSizeInMs ( float windowSizeInMs )
    {
        this.windowSizeInMs = windowSizeInMs;
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

    void onStarted ( windowerProducer producer )
    {
        this.producer = producer;
    }

    void onStopped ( windowerProducer producer )
    {
        queue.clear();
    }

    void onStarted ( windowerConsumer consumer ) throws Exception
    {
        this.consumer = consumer;
    }

    void onStopped ( windowerConsumer consumer ) throws Exception
    {
        queue.clear();
    }
}