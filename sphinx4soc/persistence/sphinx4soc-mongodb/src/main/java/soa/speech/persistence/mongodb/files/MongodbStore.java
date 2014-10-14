package soa.speech.persistence.mongodb.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.mongodb.BasicDBObject;

public class MongodbStore {

    private static final Logger logger = Logger.getLogger(MongodbStore.class);

    protected GridFsTemplate gridFsTemplate;
    
    public void storeFile(File file, Map<String, Object> metadata) throws IOException {
        
        InputStream f = new FileInputStream(file);

        String filename = FilenameUtils.getBaseName(file.getAbsolutePath());
        BasicDBObject dbObject = new BasicDBObject(metadata);
        
        gridFsTemplate.store(f, file.getName(), readMediaType(file), dbObject);
        logger.info("Inserted record: " + filename);
    }
    
    public String readMediaType(File file) throws IOException 
    {
        Tika tika = new Tika();
        InputStream f = new FileInputStream(file);
        return tika.detect(f);
    }

    public GridFsTemplate getGridFsTemplate() {
        return gridFsTemplate;
    }

    public void setGridFsTemplate(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

}
