package soa.speech.recogniser.mongodb.files;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.mongodb.gridfs.GridFSDBFile;

@ContextConfiguration("file:src/test/resources/META-INF/spring/mongodbTestContext.xml")
public class StoreModelDataTest extends AbstractTestNGSpringContextTests {

    private static final Logger logger = Logger.getLogger(StoreModelDataTest.class);

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private StoreModelData storeModelData;

    @Test
    public void storeTest() {

        List<GridFSDBFile> meanFile = gridFsTemplate.find(query(where("filename").is("meansTest").and("metadata.databaseName")
                .is(mongoTemplate.getDb().getName()).and("metadata.modelName").is("WSJ_clean_13dCep_16k_40mel_130Hz_6800Hz")));

        Assert.assertEquals(meanFile.size(), 1, "failed :(");

    }

    @BeforeClass
    public void initiallise() throws Exception{

        String means = getClass().getResource("/data/models/binaries/meansTest").getFile();
        String mixtureWeightsTest = getClass().getResource("/data/models/binaries/mixtureWeightsTest").getFile();
        String transitionsMatricesTest = getClass().getResource("/data/models/binaries/transitionsMatricesTest").getFile();
        String variancesTest = getClass().getResource("/data/models/binaries/variancesTest").getFile();

        String[] modelParameterPaths = new String[4];
        modelParameterPaths[0] = means;
        modelParameterPaths[1] = mixtureWeightsTest;
        modelParameterPaths[2] = transitionsMatricesTest;
        modelParameterPaths[3] = variancesTest;

        storeModelData = new StoreModelData();
        String dictionaryPath = getClass().getResource("/data/models/dict/cmudict").getFile();
        storeModelData.setDictionaryPath(dictionaryPath);
        String fillerdictionaryPath = getClass().getResource("/data/models/dict/fillerdict").getFile();
        storeModelData.setFillerdictionaryPath(fillerdictionaryPath);
        String modeldefPath = getClass().getResource("/data/models/etc/modelDefinitions.mdef").getFile();
        storeModelData.setModeldefPath(modeldefPath);
        storeModelData.setModelParameterPaths(modelParameterPaths);
        storeModelData.setGridFsTemplate(gridFsTemplate);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("databaseName", mongoTemplate.getDb().getName());
        metadata.put("modelName", "WSJ_clean_13dCep_16k_40mel_130Hz_6800Hz");
        metadata.put("featureType", "1s_c_d_dd");
        metadata.put("statesperhmm", "3");
        metadata.put("skipstate", "false");
        metadata.put("gaussiansPerState", "8");
        metadata.put("nTiedStates", "4000");
        metadata.put("agc", "none");
        metadata.put("cmn", "current");
        metadata.put("varnorm", "false");

        storeModelData.setMetadata(metadata);
        
        storeModelData.storeModelFiles();
    }

    @AfterClass
    public void cleanUp() {
        mongoTemplate.getDb().dropDatabase();
        logger.info("Speech database droped");
    }
}
