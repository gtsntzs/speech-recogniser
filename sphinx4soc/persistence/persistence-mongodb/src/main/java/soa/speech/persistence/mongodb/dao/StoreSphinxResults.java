package soa.speech.persistence.mongodb.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import soa.speech.recogniser.lib.beans.Experiment;
import soa.speech.recogniser.lib.beans.Sphinx4socDataStream;

import com.mongodb.BasicDBObject;

import edu.cmu.sphinx.frontend.Data;

/**
 * 
 * @author gorg
 *
 */
public class StoreSphinxResults {

    /** Compressed comment. */
    private MongoTemplate mongoTemplate;

    /** Generates unique _id's for the data sequence. */
    private SequenceDao sequenceDao;

    /**
     * Stores the given Data frame to the database as a document with an additional random UUID and
     * the name of the class. It also adds the entry to the aggregator structure
     * Sphinx4socDataStream.
     * 
     * @param dataFrame
     *            the edu.cmu.sphinx.frontend.Data object
     * @param headers
     *            map with mandatory fields
     *            <p>
     *            <ul>
     *            <li>experimentName is the name of the experiment
     *            <li>streamName
     *            <li>endpointName the name of the endpoint.
     *            <ul>
     *            <p>
     */
    public void storeData(Data dataFrame, Map<String, Object> headers) throws Exception {

        BasicDBObject sphinxData = storeSphinxData(dataFrame, headers);
        storeSphinx4socDataStream(sphinxData.getString("_id"), headers); 
    }
    
    protected void storeSphinx4socDataStream(String sphinxDataId, Map<String, Object> headers) {

        Experiment configuration = (Experiment) headers.get("experiment");
        String streamName = (String) headers.get("streamName");
        String endpointName = (String) headers.get("endpointName");

        //
        String collectionName = configuration.getName() + "-" + configuration.getVersion() + "Result";
        
        Query query = query(where("streamName").is(streamName).and("endpointName").is(endpointName));

        if (mongoTemplate.findOne(query, Sphinx4socDataStream.class, collectionName) == null) {
            Sphinx4socDataStream sphinx4socDataStream = new Sphinx4socDataStream();
            Set<String> dataSet = new HashSet<>();
            dataSet.add(sphinxDataId);
            sphinx4socDataStream.setDataids(dataSet);
            sphinx4socDataStream.setEndpointName(endpointName);
            sphinx4socDataStream.setExperimentId(configuration.getName() + "-" + configuration.getVersion());
            sphinx4socDataStream.setStreamName(streamName);
            mongoTemplate.save(sphinx4socDataStream, collectionName);
        } else {
            Update update = new Update();
            mongoTemplate.findAndModify(query, update.addToSet("dataids", sphinxDataId), Sphinx4socDataStream.class, collectionName);
        }
    }
    
    protected BasicDBObject storeSphinxData(Data dataFrame, Map<String, Object> headers) throws Exception 
    {
        Experiment configuration = (Experiment) headers.get("experiment");
        String streamName = (String) headers.get("streamName");
        //
        String collectionName = configuration.getName() + "-" + configuration.getVersion() + "Result";

        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }

        BasicDBObject dbObject = new BasicDBObject(streamName, dataFrame);
        long nextSequenceId = sequenceDao.getNextSequenceId(collectionName);
        dbObject.put("_id", nextSequenceId);
        dbObject.append("className", dataFrame.getClass().getCanonicalName());
        mongoTemplate.save(dataFrame, collectionName);

        return dbObject;
    }

    public void loadStreamData(Map<String, Object> headers) {

    }
}
