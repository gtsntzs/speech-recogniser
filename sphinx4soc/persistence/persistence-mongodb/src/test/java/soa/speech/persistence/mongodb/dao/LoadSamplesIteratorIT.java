package soa.speech.persistence.mongodb.dao;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import soa.speech.lib.beans.Corpus;
import soa.speech.lib.beans.Experiment;

import com.mongodb.gridfs.GridFSDBFile;

@ContextConfiguration("file:src/test/resources/META-INF/spring/mongodbTestContext.xml")
public class LoadSamplesIteratorIT extends AbstractTestNGSpringContextTests {

    private static final Logger logger = Logger.getLogger(LoadSamplesIteratorIT.class);

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private LoadSamplesIterator loadSamplesIterator;
    private final int numOfsamples=2;

    /**
     * test query limit 
     * 
     * @throws IOException
     */
    @Test
    public void limitTest() throws IOException {
        
        int counter=0;
        while (loadSamplesIterator.hasNext()) {
            GridFSDBFile file = loadSamplesIterator.next();
            counter++;
            logger.info("File " +file.getFilename() + " Length " +file.getLength()); 
        }
        AssertJUnit.assertEquals(counter, numOfsamples);
    }
    
    @BeforeMethod
    public void initiallise() throws Exception {
        Experiment experiment = new Experiment();
        experiment.setCorpus(Corpus.LIMIT.name());
        experiment.setNumOfSamples(numOfsamples);
        experiment.setName("testExp");
        experiment.setSpeechDatabase(mongoTemplate.getDb().getName());
        loadSamplesIterator = new LoadSamplesIterator(gridFsTemplate, experiment);
        loadSamplesIterator.init();
    }
}