package soa.speech.persistence.mongodb.dao;

public interface SequenceDao {

    long getNextSequenceId(String key) throws Exception;
}
