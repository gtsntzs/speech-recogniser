/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.recogniser.decoder.process;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.decoder.search.ActiveList;
import edu.cmu.sphinx.frontend.Data;

/**
 *
 * @author gorg
 */
public class CalculateAggregationStr implements AggregationStrategy {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * Aggregates the messages.
     *
     * @param oldExchange
     *            the existing aggregated message. Is <tt>null</tt> the very
     *            first time as there are no existing message.
     * @param newExchange
     *            the incoming message. This is never <tt>null</tt>.
     * @return the aggregated message.
     */
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        // the first time there are no existing message and therefore
        // the oldExchange is null. In these cases we just return
        // the newExchange
        if (oldExchange == null) {
            // logger.info("new Exchange");
            return newExchange;
        }
        // now we have both an existing message (oldExchange)
        // and a incoming message (newExchange)
        // we want to merge together.

        Object oldBody = oldExchange.getIn().getBody();
        if (oldBody instanceof ActiveList) {
            Data newBody = newExchange.getIn().getBody(Data.class);
            oldExchange.getIn().setHeader("data", newBody);
            oldExchange.getIn().setHeader("CamelFileName", newExchange.getIn().getHeader("CamelFileName"));
            return oldExchange;
        } else if (oldBody instanceof Data) {
            Data data = oldExchange.getIn().getBody(Data.class);
            newExchange.getIn().setHeader("data", data);
            newExchange.getIn().setHeader("CamelFileName", oldExchange.getIn().getHeader("CamelFileName"));
            return newExchange;
        } else {
            logger.info("ERROR");
            return newExchange;
        }
    }
}
