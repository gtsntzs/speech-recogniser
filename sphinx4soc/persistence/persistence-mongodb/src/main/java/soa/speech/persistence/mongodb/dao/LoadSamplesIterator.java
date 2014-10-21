package soa.speech.persistence.mongodb.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import soa.speech.lib.beans.Corpus;
import soa.speech.lib.beans.Experiment;

import com.mongodb.gridfs.GridFSDBFile;

public class LoadSamplesIterator {

    private static final Logger logger = Logger.getLogger(LoadSamplesIterator.class);
    /** Compressed comment. */
    private GridFsTemplate gridFsTemplate;
    /** Compressed comment. */
    private Experiment experiment;

    private Iterator<GridFSDBFile> speechFilesItr;

    public LoadSamplesIterator(GridFsTemplate gridFsTemplate, Experiment experiment) {
        this.gridFsTemplate = gridFsTemplate;
        this.experiment = experiment;
    }

    /**
     * Method initialization Using GridFSDBFile to make queries unfortunately limit predicate isn't
     * supported https://jira.spring.io/browse/DATAMONGO-765
     */
    public void init() {
        Query query = query(where("metadata.type").is("speech"));
        List<GridFSDBFile> speechFiles = gridFsTemplate.find(query);

        logger.info("Found number of speech Files " + speechFiles.size());
        if (Corpus.LIMIT.name().equalsIgnoreCase(experiment.getCorpus())) {
            logger.info(" limit to " + experiment.getNumOfSamples() +" samples"); 
            speechFilesItr = Collections.synchronizedList(speechFiles.subList(0, experiment.getNumOfSamples())).iterator();
        } else {
            speechFilesItr = Collections.synchronizedList(speechFiles).iterator();
        }
    }

    public boolean hasNext() {
        return speechFilesItr.hasNext();
    }

    public GridFSDBFile next() {
        if (speechFilesItr.hasNext()) {
            GridFSDBFile next = speechFilesItr.next();
            logger.info("Streaming file " + next.getFilename());
            return next;
        } else {
            return null;
        }
    }
}
