/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.recogniser.evaluator.process;

import edu.cmu.sphinx.result.Result;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author media
 */
public class ResultTranscriptionProcessor implements Processor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public ResultTranscriptionProcessor() {
        
    }

    public void process(Exchange exchange) throws Exception {

        Result body = exchange.getIn().getBody(Result.class);
        logger.info(body.toString());

        String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
        Result result = exchange.getIn().getBody(Result.class);

//        String transcription = ReadRepositoryRoute.utils.getTrascription("/"
//                + RecogniserLarge.REPO_TEST_PATH + "/" + fileName);
//
//        result.setReferenceText(transcription);

    }
}
