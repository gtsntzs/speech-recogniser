package soa.speech.recogniser.evaluator.process;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import edu.cmu.sphinx.result.Result;

public class ResultTranscriptionEnrich implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Result body = exchange.getIn().getBody(Result.class);
		String ref = exchange.getIn().getHeader("transcription", String.class);
		body.setReferenceText(ref );
	}
}
