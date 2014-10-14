/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package soa.speech.io.process;

import edu.cmu.sphinx.frontend.Data;
import org.apache.camel.Exchange;

/**
 *
 * @author gorg
 */
public class StreamCepstumDynamicRouter
{
    public static final String FINAL_ENDPOINT = "seda://cepstraData";

    /**
     * The method invoked by Dynamic Router EIP to compute where to go next.
     *
     * @param body          the message body
     * @param previous   the previous endpoint, is <tt>null</tt> on the first invocation
     * @return endpoint uri where to go, or <tt>null</tt> to indicate no more
     */
    public String route ( Exchange ex )
    {
        Data data = ex.getIn().getBody( Data.class );
        return next( data );
    }

    /**
     * Method which computes where to go next
     */
    private String next ( Data data )
    {
        if ( data!=null ) {
            return FINAL_ENDPOINT+",bean:streamCepstrumSource?method=getData";
        } else {
            return null;
        }
    }
}
