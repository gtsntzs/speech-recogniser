package soa.speech.recogniser.frontend.converter;

import javax.sound.sampled.AudioFormat;

import org.apache.camel.Exchange;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;

public class AudioSourceDataConverter {

    private static final String AUDIO_BEGIN_TIME = "AUDIO_BEGIN_TIME";
    private static final String AUDIO_END_TIME = "AUDIO_END_TIME";
    private static final String AUDIO_TIMESTAMP = "AUDIO_TIMESTAMP";
    private static final String AUDIO_FORMAT = "AUDIO_FORMAT";
    private static final String AUDIO_DURATION = "AUDIO_DURATION";
    private static final String AUDIO_FIRST_SAMPLE = "AUDIO_FIRST_SAMPLE"; 
    
    public Data toSphinxData(Exchange exchange) throws Exception {
        
        Data data = null;
        Object body = exchange.getIn().getBody();
        AudioFormat format = exchange.getIn().getHeader(AUDIO_FORMAT, AudioFormat.class);
        int sampleRate = (int) Math.round(format.getSampleRate());
        if(body instanceof double[]) {
            long timestamp = exchange.getIn().getHeader(AUDIO_TIMESTAMP, Long.class);
            long firstSampleNumber = exchange.getIn().getHeader(AUDIO_FIRST_SAMPLE, Long.class);
            data = new DoubleData((double[])body, sampleRate, timestamp, firstSampleNumber);
        } else if (body instanceof String) {
            if("StartSignal".equals(body)){
                long timestamp = exchange.getIn().getHeader(AUDIO_BEGIN_TIME, Long.class);
                data = new DataStartSignal(sampleRate, timestamp );
            } else if("EndSignal".equals(body)){
                long timestamp = exchange.getIn().getHeader(AUDIO_END_TIME, Long.class);
                long duration = exchange.getIn().getHeader(AUDIO_DURATION, Long.class);
                data = new DataEndSignal(duration , timestamp);
            }
        }
        return data;
    }
    
    
    
}
