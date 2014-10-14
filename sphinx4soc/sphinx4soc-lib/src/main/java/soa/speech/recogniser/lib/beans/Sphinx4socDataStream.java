package soa.speech.recogniser.lib.beans;

import java.util.Set;

public class Sphinx4socDataStream {

    private String experimentId;
    // stream name can be the filename for example
    private String streamName;
    // endpoint is the 
    private String endpointName;
    private Set<String> dataids;
    
    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String name) {
        this.streamName = name;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentName) {
        this.experimentId = experimentName;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String moduleName) {
        this.endpointName = moduleName;
    }

    public Set<String> getDataids() {
        return dataids;
    }

    public void setDataids(Set<String> dataids) {
        this.dataids = dataids;
    }

    @Override
    public String toString() {
        return "{\"experimentName\":\"" + experimentId + "\",\"streamName\":\"" + streamName + "\",\"endpointName\":\"" + endpointName
                + "\",\"dataUuids\":\"" + dataids + "\"}";
    }
}
