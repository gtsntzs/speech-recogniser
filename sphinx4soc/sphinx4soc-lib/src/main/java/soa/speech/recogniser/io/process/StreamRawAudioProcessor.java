/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.recogniser.io.process;

import edu.cmu.sphinx.frontend.util.StreamDataSource;
import java.io.InputStream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author gorg
 */
public class StreamRawAudioProcessor implements Processor
{
	private final StreamDataSource streamDataSourceBean;

	public StreamRawAudioProcessor(StreamDataSource streamDataSourceBean){
		this.streamDataSourceBean = streamDataSourceBean;
	}
	
    public void process ( Exchange exchange ) throws Exception
    {
        InputStream input = exchange.getIn().getBody( InputStream.class );
        String streamName = exchange.getIn().getHeader( "CamelFileName", String.class );
        String fileLength = exchange.getIn().getHeader( "CamelFileLength", String.class );
        
        streamDataSourceBean.setInputStream( input, streamName );
        exchange.getOut().setHeader( "CamelFileName", streamName );
        exchange.getOut().setHeader( "CamelFileLength", fileLength );
        exchange.getOut().setBody( streamDataSourceBean.getData() );
    }
}
