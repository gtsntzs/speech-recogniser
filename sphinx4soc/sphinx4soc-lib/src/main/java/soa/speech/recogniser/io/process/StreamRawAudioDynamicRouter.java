/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.recogniser.io.process;

import org.apache.camel.Exchange;
import org.apache.log4j.Logger;

import edu.cmu.sphinx.frontend.Data;

/**
 * 
 * @author gorg
 */
public class StreamRawAudioDynamicRouter {

	private static final Logger log = Logger.getLogger( StreamRawAudioDynamicRouter.class );
	
	// private final String outEndpoind;
	//
	// private final String streamSourceEndpoint;
	private final String[] sequence;

	public StreamRawAudioDynamicRouter(String[] sequence) {
		this.sequence = sequence;
	}

	// public StreamRawAudioDynamicRouter(String outEndpoint, String
	// streamSourceEndpoint) {
	// this.outEndpoind = outEndpoint;
	// this.streamSourceEndpoint = streamSourceEndpoint;
	// }

	/**
	 * The method invoked by Dynamic Router EIP to compute where to go next.
	 * 
	 * @param body
	 *            the message body
	 * @param previous
	 *            the previous endpoint, is <tt>null</tt> on the first
	 *            invocation
	 * @return endpoint uri where to go, or <tt>null</tt> to indicate no more
	 */
	public String route(Exchange ex) throws Exception {
		Data data = ex.getIn().getBody(Data.class);

		return next(data);
	}

	private String buildSequence() {
		String out = "";
		for (int i = 0; i < sequence.length; i++) {
			out = out + sequence[i];
			if(i < sequence.length) {
				out = out + ",";
			}
		}
		return out;
	}

	/**
	 * Method which computes where to go next
	 */
	private String next(Data data) {
		if (data != null) {
			String buildSequence = buildSequence();
//			log.info(buildSequence);
			return buildSequence;
		} else {
			return null;
		}
	}
}
