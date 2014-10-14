package soa.speech.recogniser.evaluator.process;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import edu.cmu.sphinx.util.NISTAlign;

public class WordAccuracyEnrich implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		NISTAlign body = exchange.getIn().getBody(NISTAlign.class);
		exchange.getIn().setHeader("wordAccuracy", body.getTotalWordAccuracy());
	}
}
