package soa.speech.persistence.mongodb.files;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.gridfs.GridFSDBFile;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Document
public class StoreModelData extends MongodbStore {

    static final Logger logger = Logger.getLogger(StoreModelData.class);
    
    /** lala */
    private String modeldefPath;
    /** lala */
    private String dictionaryPath;
    /** lala */
    private String fillerdictionaryPath;
    /** lala */
    private String[] modelParameterPaths;
    /** lala */
    private Map<String, Object> metadata;

    public void storeModelFiles() throws IOException {

        File dictFile = new File(dictionaryPath);
        if (!fileExists(dictFile)) {
            storeFile(dictFile, metadata);
        }
        
        File fillerdictionaryFile = new File(fillerdictionaryPath);
        if (!fileExists(fillerdictionaryFile)) {
            storeFile(fillerdictionaryFile, metadata);
        }
        
        File modeldefFile = new File(modeldefPath);
        if (!fileExists(modeldefFile)) {
            storeFile(modeldefFile, metadata);
        }

        for (String modelPath : modelParameterPaths) {
            File modelPathFile = new File(modelPath);
            if (!fileExists(modelPathFile)) {
                storeFile(modelPathFile, metadata);
            }
        }
    }

    protected boolean fileExists(File file) {
        GridFSDBFile results = gridFsTemplate.findOne(query(where("modelName").is(metadata.get("modelName")).and("fileName").is(file.getName())));
        return results == null ? false : true;
    }

    public String getModeldefPath() {
        return modeldefPath;
    }

    public void setModeldefPath(String modeldefPath) {
        this.modeldefPath = modeldefPath;
    }

    public String getDictionaryPath() {
        return dictionaryPath;
    }

    public void setDictionaryPath(String dictionaryPath) {
        this.dictionaryPath = dictionaryPath;
    }

    public String getFillerdictionaryPath() {
        return fillerdictionaryPath;
    }

    public void setFillerdictionaryPath(String fillerdictionaryPath) {
        this.fillerdictionaryPath = fillerdictionaryPath;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String[] getModelParameterPaths() {
        return modelParameterPaths;
    }

    public void setModelParameterPaths(String[] modelParameterPaths) {
        this.modelParameterPaths = modelParameterPaths;
    }

    
}
