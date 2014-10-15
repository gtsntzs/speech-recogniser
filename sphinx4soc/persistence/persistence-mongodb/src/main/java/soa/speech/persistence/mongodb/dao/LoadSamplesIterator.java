package soa.speech.persistence.mongodb.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Iterator;
import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import soa.speech.lib.beans.Corpus;
import soa.speech.lib.beans.Experiment;

import com.mongodb.gridfs.GridFSDBFile;

public class LoadSamplesIterator {

    /** Compressed comment. */
    private GridFsTemplate gridFsTemplate;
    /** Compressed comment. */
    private Experiment experiment;

    private Iterator<GridFSDBFile> speechFilesItr;

    public LoadSamplesIterator(GridFsTemplate gridFsTemplate, Experiment experiment) {
        this.gridFsTemplate = gridFsTemplate;
        this.experiment = experiment;
    }

    public void init() {

        Query query = query(where("metadata.type").is("speech"));

        if (experiment.getCorpus().equalsIgnoreCase(Corpus.LIMIT.name())) {
            query.limit(experiment.getNumOfSamples());
        }
        List<GridFSDBFile> speechFiles = gridFsTemplate.find(query);
        speechFilesItr = speechFiles.iterator();
    }

    public boolean hasNext() {
        return speechFilesItr.hasNext();
    }

    public GridFSDBFile next() {
        if (speechFilesItr.hasNext()) {
            return speechFilesItr.next();
        } else {
            return null;
        }
    }
}
