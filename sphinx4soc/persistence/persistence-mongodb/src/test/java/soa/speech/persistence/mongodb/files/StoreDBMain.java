package soa.speech.persistence.mongodb.files;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

public class StoreDBMain {

    private static final Logger logger = Logger.getLogger(StoreDBMain.class);

    public static void main(String[] args) throws Exception{

        ApplicationContext ctx = new GenericXmlApplicationContext("META-INF/spring/mongodbTestContext.xml");
        GridFsTemplate gridFsTemplate = (GridFsTemplate) ctx.getBean("gridFsTemplate");
        
        new StoreDBMain().procedure(gridFsTemplate);
    }

    protected void procedure(GridFsTemplate gridFsTemplate) throws Exception {

        String path = "/media/Work/tmp/mongodb/AURORA4";
        
        String trancription = path +"/transcription/wsj-cleantrain-word.mlf";
        String speechFilesDirectory = path + "/db/";

        String[] speechFileExtensions = new String[1];
        speechFileExtensions[0] = "wv1";

        StoreSpeechDatabase speechDatabase = new StoreSpeechDatabase();
        speechDatabase.setGridFsTemplate(gridFsTemplate);
        speechDatabase.setParserClass("soa.speech.lib.trancript.Aurora4Parser");
        speechDatabase.setSpeechFileExtensions(speechFileExtensions);
        speechDatabase.setSpeechFilesDirectory(speechFilesDirectory);
        speechDatabase.setTranscriptionFile(trancription);
        speechDatabase.store();
        logger.info("Speech database created");

    }
}
