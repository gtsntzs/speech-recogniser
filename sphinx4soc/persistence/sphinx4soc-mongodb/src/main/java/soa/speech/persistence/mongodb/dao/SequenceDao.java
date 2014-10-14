package soa.speech.recogniser.mongodb.dao;

public interface SequenceDao {

    long getNextSequenceId(String key) throws Exception;
}
