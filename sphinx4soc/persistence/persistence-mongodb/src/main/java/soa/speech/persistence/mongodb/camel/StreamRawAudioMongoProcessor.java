package soa.speech.persistence.mongodb.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.mongodb.gridfs.GridFSDBFile;

import edu.cmu.sphinx.frontend.util.StreamDataSource;

public class StreamRawAudioMongoProcessor implements Processor
{
    private final StreamDataSource streamDataSourceBean;

    public StreamRawAudioMongoProcessor(StreamDataSource streamDataSourceBean){
        this.streamDataSourceBean = streamDataSourceBean;
    }
    
    public void process ( Exchange exchange ) throws Exception
    {
        GridFSDBFile input = exchange.getIn().getBody( GridFSDBFile.class );
        String streamName = input.getFilename();
        long fileLength = input.getLength();
        
        streamDataSourceBean.setInputStream( input.getInputStream(), streamName );
        exchange.getOut().setHeader( "CamelFileName", streamName );
        exchange.getOut().setHeader( "CamelFileLength", fileLength );
        exchange.getOut().setBody( streamDataSourceBean.getData() );
    }
}
