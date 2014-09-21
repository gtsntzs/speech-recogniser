package soa.speech.recogniser.windower;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class windowerComponentTest extends CamelTestSupport {

    @Test
    public void testwindower() throws Exception {
//        MockEndpoint mock = getMockEndpoint("mock:result");
//        mock.expectedMinimumMessageCount(1);       
//        
//        assertMockEndpointsSatisfied();
    }

//    @Override
//    protected RouteBuilder createRouteBuilder() throws Exception {
//        return new RouteBuilder() {
//            public void configure() {
//                from("window://foo")
//                  .to("window://bar")
//                  .to("mock:result");
//            }
//        };
//    }
}
