package soa.speech.persistence.mongodb.files;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import soa.speech.lib.trancript.TranscriptionParser;

import com.mongodb.gridfs.GridFSDBFile;

public class StoreSpeechDatabase extends MongodbStore {

    static final Logger logger = Logger.getLogger(StoreSpeechDatabase.class);

    private String parserClass;
    private String transcriptionFile;
    private String speechFilesDirectory;
    private String[] speechFileExtensions;

    public void store() throws Exception {

        Map<String, String> transcripts = loadTranscript();
        Iterator<File> iterateFiles = FileUtils.iterateFiles(new File(speechFilesDirectory), speechFileExtensions, true);

        while (iterateFiles.hasNext()) {
            File file = (File) iterateFiles.next();

            if (!fileExists(file)) {
                String filename = FilenameUtils.getBaseName(file.getAbsolutePath());
                if (transcripts.get(filename) != null) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("type", "speech");
                    metadata.put("name", FilenameUtils.getBaseName(file.getAbsolutePath()));
                    metadata.put("group", file.getParentFile().getName());
                    metadata.put("transcription", transcripts.get(filename));
                    logger.info("FileName: " + file.getName());
                    storeFile(file, metadata);
                } else {
                    logger.warn("Transcript not found for fileName: " + filename + " skipping.");
                }
            }
        }
    }

    protected boolean fileExists(File file) {
        
        Query query = new Query(Criteria.where("fileName").is(file.getName()));
        boolean exists = mongoTemplate.exists(query, "fs.files");
        return exists == false ? false : true;
    }

    protected Map<String, String> loadTranscript() throws Exception {
        Class<?> clazz = Class.forName(parserClass);
        TranscriptionParser parser = (TranscriptionParser) clazz.newInstance();
        return parser.parse(transcriptionFile);
    }

    public String getParserClass() {
        return parserClass;
    }

    public void setParserClass(String parserClass) {
        this.parserClass = parserClass;
    }

    public String getTranscriptionFile() {
        return transcriptionFile;
    }

    public void setTranscriptionFile(String transcriptionFile) {
        this.transcriptionFile = transcriptionFile;
    }

    public String getSpeechFilesDirectory() {
        return speechFilesDirectory;
    }

    public void setSpeechFilesDirectory(String speechFilesDirectory) {
        this.speechFilesDirectory = speechFilesDirectory;
    }

    public String[] getSpeechFileExtensions() {
        return speechFileExtensions;
    }

    public void setSpeechFileExtensions(String[] speechFileExtensions) {
        this.speechFileExtensions = speechFileExtensions;
    }

}
