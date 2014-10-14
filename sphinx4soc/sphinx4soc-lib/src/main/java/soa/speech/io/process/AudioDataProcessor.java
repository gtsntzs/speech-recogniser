/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.recogniser.io.process;

import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;

/**
 *
 * @author gorg
 */
public class AudioDataProcessor implements Processor
{
    public void process ( Exchange exchange ) throws Exception
    {
        AudioFileDataSource audioFileStream = exchange.getContext().getRegistry().lookup( "audioDataSource", AudioFileDataSource.class );

        GenericFile input = (GenericFile)exchange.getIn().getBody();

        AudioInputStream audioStream = null;
        try {
            audioStream = AudioSystem.getAudioInputStream( (File)input.getFile() );
        } catch ( UnsupportedAudioFileException e ) {
            System.err.println( "Audio file format not supported: "+e );
        } catch ( IOException e ) {
        }

        String streamName = (String)exchange.getIn().getHeader( "CamelFileName" );

        audioFileStream.setInputStream( audioStream, streamName );

        System.out.println( "--------------------------------------------------------------" );
        System.out.println( streamName );
        System.out.println( audioFileStream.getName() );
        System.out.println( "--------------------------------------------------------------" );

        exchange.getOut().setBody( audioFileStream.getData() );
    }
}
