/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.recogniser.decoder.process;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import edu.cmu.sphinx.decoder.search.ActiveList;
import edu.cmu.sphinx.decoder.search.WordPruningBreadthFirstSearchManager;

/**
 *
 * @author gorg
 */
public class DecoderExchangeEnrich implements Processor
{
    static final Logger log = Logger.getLogger( DecoderExchangeEnrich.class );
    private long id = -1;
    private WordPruningBreadthFirstSearchManager searchManager;
    
    public DecoderExchangeEnrich(WordPruningBreadthFirstSearchManager searchManager) {
		this.searchManager = searchManager;
	}
    
    public void process ( Exchange exchange ) throws Exception
    {
        id++;
        ActiveList activeList = searchManager.getActiveList();
        exchange.getIn().setHeader( "id", id );
        exchange.getIn().setBody( activeList );
    }
}
