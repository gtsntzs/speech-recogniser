package soa.speech.recogniser.mongodb.beans;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sequence")
public class SequenceId {

    @Id
    private String id;

    private long sequence;

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "{\"id\":\"" + id + "\",\"sequence\":\"" + sequence + "\"}";
    }
}