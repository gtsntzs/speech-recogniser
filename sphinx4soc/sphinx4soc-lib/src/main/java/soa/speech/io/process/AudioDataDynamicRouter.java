/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soa.speech.io.process;

import edu.cmu.sphinx.frontend.Data;
import org.apache.camel.Body;

/**
 *
 * @author gorg
 */
public class AudioDataDynamicRouter
{
    /**
     * The method invoked by Dynamic Router EIP to compute where to go next.
     *
     * @param body          the message body
     * @param previous   the previous endpoint, is <tt>null</tt> on the first invocation
     * @return endpoint uri where to go, or <tt>null</tt> to indicate no more
     */
    public String route ( @Body Data data )
    {
        return next( data );
    }

    /**
     * Method which computes where to go next
     */
    private String next ( Data data )
    {
        if ( data!=null ) {
            // 1st time
//            return "seda:data,bean:streamDataSource?method=getData";
            return "seda:data,bean:audioDataSource?method=getData";
        } else {
            // no more, so return null to indicate end of dynamic router
            return null;
        }
    }
}
