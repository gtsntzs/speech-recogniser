package soa.speech.persistence.mongodb.dao;

import org.testng.annotations.Test;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import soa.speech.lib.beans.Corpus;
import soa.speech.lib.beans.Experiment;
import soa.speech.persistence.mongodb.beans.SequenceId;

import com.mongodb.gridfs.GridFSDBFile;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.util.StreamDataSource;

@ContextConfiguration("file:src/test/resources/META-INF/spring/mongodbTestContext.xml")
public class StoreSphinxResultsIT extends AbstractTestNGSpringContextTests {

    private static final Logger logger = Logger.getLogger(StoreSphinxResultsIT.class);
    
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private final int numOfsamples=2;
    
    @Test
    public void name() throws Exception {
        int sampleRate = 16000; 
        int bytesPerRead = 3200;
        int bitsPerSample = 16;
        boolean bigEndian = true;
        boolean signedData = true;
        StreamDataSource streamDataSource = new StreamDataSource(sampleRate, bytesPerRead, bitsPerSample, bigEndian, signedData);
        
        Query query = null;
        query = query(where("metadata.type").is("speech"));
        GridFSDBFile speechFile = gridFsTemplate.findOne(query);
        
        streamDataSource.setInputStream(speechFile.getInputStream(), speechFile.getFilename());
        
        Experiment experiment = new Experiment();
        experiment.setCorpus(Corpus.LIMIT.name());
        experiment.setNumOfSamples(numOfsamples);
        experiment.setName("testExp");
        experiment.setVersion("1.0");
        experiment.setSpeechDatabase(mongoTemplate.getDb().getName());
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("experiment", experiment);
        headers.put("streamName", speechFile.getFilename());
        headers.put("endpointName", "endpointTest");
        
        // init db
        mongoTemplate.createCollection("testExp-1.0-Result");
        SequenceId sequenceId = new SequenceId();
        sequenceId.setSequence(0);
        sequenceId.setId("testExp-1.0-Result");
        mongoTemplate.save(sequenceId, "testExp-1.0-Result");  
        //
        StoreSphinxResults results = new StoreSphinxResults();
        results.setMongoTemplate(mongoTemplate);
        SequenceDaoImpl sequenceDaoImpl = new SequenceDaoImpl();
        sequenceDaoImpl.setMongoTemplate(mongoTemplate);
        results.setSequenceDao(sequenceDaoImpl);
        
        while ( true ) {
            Data data = streamDataSource.getData();
            results.storeData(data, headers);
            if(data instanceof DataEndSignal){
                break;
            }
        }
    }
}
