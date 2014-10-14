package soa.speech.persistence.mongodb.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import soa.speech.lib.beans.Experiment;

public class ExperimentDao {

    private static final Logger logger = Logger.getLogger(ExperimentDao.class);

    public final static String EXPERIMENTS_COLLECTION_NAME = "experiments";

    private MongoTemplate mongoTemplate;

    public Experiment storeInputExperiment( String name, String version, int corpus) {

        Experiment experiment = new Experiment();
        experiment.setName(name);
        experiment.setVersion(version);
        experiment.setSpeechDatabase(mongoTemplate.getDb().getName());
        experiment.setCorpus(corpus);

        mongoTemplate.save(experiment, EXPERIMENTS_COLLECTION_NAME);

        return experiment;
    }

    public void storeInputExperiment( Experiment experiment) {
        mongoTemplate.save(experiment, EXPERIMENTS_COLLECTION_NAME);
    }
    
    public Experiment loadExperiment(String name, String version) throws Exception {

        Query query = query(where("name").is(name).and("version").is(version));
        List<Experiment> find = mongoTemplate.find(query, Experiment.class, EXPERIMENTS_COLLECTION_NAME);
        if (find == null) {
            logger.error("No experiment found with name: " + name + " and version" + version);
            throw new Exception("No experiment found with name: " + name + " and version" + version);
        } else if (find.size() > 1) {
            logger.error("Found duplicate experiment!");
            throw new Exception("duplicate experiment!");
        }

        return find.get(0);
    }
    
    public boolean existsExperiment(String name, String version) {

        Query query = query(where("name").is(name).and("version").is(version));
        Experiment find = mongoTemplate.findOne(query, Experiment.class, EXPERIMENTS_COLLECTION_NAME);

        return (find == null) ? false : true;
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

}
