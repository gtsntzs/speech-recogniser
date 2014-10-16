package soa.speech.persistence.mongodb.test.files;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import soa.speech.persistence.mongodb.files.StoreSpeechDatabase;

import com.mongodb.gridfs.GridFSDBFile;


@ContextConfiguration("file:src/test/resources/META-INF/spring/mongodbTestContext.xml")
public class StoreSpeechDatabaseIT extends AbstractTestNGSpringContextTests {

    private static final Logger logger = Logger.getLogger(StoreSpeechDatabaseIT.class);

    @Autowired
    private GridFsTemplate gridFsTemplate;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private StoreSpeechDatabase speechDatabase;
    
    @Test
    public void storeTest() throws Exception {

        List<GridFSDBFile> speechFiles = gridFsTemplate.find(query(where("metadata.type").is("speech")));
        Assert.assertEquals(speechFiles.size(), 3, "failed :(" );
    }
    
    @BeforeClass
    public void importSpeechFiles() throws Exception{

        String trancription = getClass().getResource("/data/transcription/wordTranscription.mlf").getFile();
        String speechFilesDirectory = getClass().getResource("/data/db/").getFile();

        String[] speechFileExtensions = new String[1];
        speechFileExtensions[0] = "wv1";
        
        speechDatabase = new StoreSpeechDatabase();
        speechDatabase.setGridFsTemplate(gridFsTemplate);
        speechDatabase.setParserClass("soa.speech.lib.trancript.Aurora4Parser");
        speechDatabase.setSpeechFileExtensions(speechFileExtensions);
        speechDatabase.setSpeechFilesDirectory(speechFilesDirectory);
        speechDatabase.setTranscriptionFile(trancription);
        speechDatabase.store();
        logger.info("Speech database created"); 
    }

    @AfterClass
    public void cleanUp() {
        mongoTemplate.getDb().dropDatabase();
        logger.info("Speech database droped");
    }
}
