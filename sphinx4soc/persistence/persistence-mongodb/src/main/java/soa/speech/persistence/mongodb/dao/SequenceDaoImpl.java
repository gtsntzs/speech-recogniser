package soa.speech.persistence.mongodb.dao;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import soa.speech.persistence.mongodb.beans.SequenceId;


@Repository
public class SequenceDaoImpl implements SequenceDao {

    private MongoTemplate mongoTemplate;

    @Override
    public long getNextSequenceId(String key) throws Exception {

        // get sequence id
        Query query = new Query(Criteria.where("_id").is(key));

        // increase sequence id by 1
        Update update = new Update();
        update.inc("seq", 1);

        // return new increased id
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);

        // this is the magic happened.
        SequenceId seqId = mongoTemplate.findAndModify(query, update, options, SequenceId.class);

        // if no id, throws SequenceException
        // optional, just a way to tell user when the sequence id is failed to generate.
        if (seqId == null) {
            throw new Exception("Unable to get sequence id for key : " + key);
        }

        return seqId.getSequence();

    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

}