package org.apache.camel.component.microphone;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;

/**
 * Represents the component that manages {@link micEndpoint}.
 */
public class micComponent extends UriEndpointComponent {

    public micComponent() {
        super(micEndpoint.class);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new micEndpoint(uri, this, remaining);

        String standardFormat = parameters.get("standardCodec").toString();

        float sampleRate = -1;
        String format = "";
        int numChannels = -1;

        if (standardFormat != null) {
            if ("phone".equalsIgnoreCase(standardFormat)) {
                sampleRate = 8000.0F;
                format = "u8";
                numChannels = 1;
            } else if ("radio".equalsIgnoreCase(standardFormat)) {
                sampleRate = 22050.0F;
                format = "s16_le";
                numChannels = 1;
            } else if ("cd".equalsIgnoreCase(standardFormat)) {
                sampleRate = 44100.0F;
                format = "s16_le";
                numChannels = 1;
            } else if ("data".equalsIgnoreCase(standardFormat)) {
                sampleRate = 48000.0F;
                format = "s16_le";
                numChannels = 2;
            } else {
                throw new Exception("Unknown standardFormat for the micEndpoint: " + standardFormat);
            }
            parameters.put("sampleRate", sampleRate);
            parameters.put("numChannels", numChannels);
            parameters.put("supportedFormat", format);
        }

        // check if the values exist if so try to get the format 
        if (parameters.containsKey("numChannels") && parameters.containsKey("sampleRate")) {
            format = parameters.get("supportedFormat").toString();
        } else {
            throw new Exception("Unknown standardFormat for the micEndpoint: " + standardFormat);
        }

        // check if a predefined format is being used
        if (format != null) {
            for (int i = 0; i < SupportedFormat.SUPPORTED_FORMATS.length; i++) {
                SupportedFormat sFormat = SupportedFormat.SUPPORTED_FORMATS[i];
                if (format.equalsIgnoreCase(sFormat.getName())) {
                    parameters.put("encoding", sFormat.getEncoding());
                    parameters.put("numBitsPerSample", sFormat.getSampleSize());
                    parameters.put("bigEndian", sFormat.getBigEndian());
                    break;
                }
            }
        }
        
        // check if the values exist 
        if (!(parameters.containsKey("encoding") && parameters.containsKey("bigEndian") && parameters.containsKey("numBitsPerSample"))) {
            throw new Exception("Unknown standardFormat for the micEndpoint: " + standardFormat);
        }

        setProperties(endpoint, parameters);
        return endpoint;
    }
}
