/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.io.process;

import edu.cmu.sphinx.frontend.util.StreamCepstrumSource;
import java.io.InputStream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author gorg
 */
public class StreamCepstrumProcessor implements Processor
{
    public void process ( Exchange exchange ) throws Exception
    {
        StreamCepstrumSource stream = exchange.getContext().getRegistry().lookup( "streamCepstrumSource", StreamCepstrumSource.class );
//        stream.initialize();

        InputStream input = exchange.getIn().getBody( InputStream.class );
        String streamName = exchange.getIn().getHeader( "CamelFileName", String.class );

        boolean bigEndian = true;
        stream.setInputStream( input, bigEndian );

        System.out.println( "--------------------------------------------------------------" );
        System.out.println( streamName );
        System.out.println( stream.getName() );
        System.out.println( "--------------------------------------------------------------" );

        exchange.getOut().setHeader( "CamelFileName", streamName );
        exchange.getOut().setBody( stream.getData() );
    }
}
