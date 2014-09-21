package soa.speech.recogniser.deltafeature;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class deltafeatureComponentTest extends CamelTestSupport {

    @Test
    public void testdeltafeature() throws Exception {
//        MockEndpoint mock = getMockEndpoint("mock:result");
//        mock.expectedMinimumMessageCount(1);       
//        
//        assertMockEndpointsSatisfied();
    }

//    @Override
//    protected RouteBuilder createRouteBuilder() throws Exception {
//        return new RouteBuilder() {
//            public void configure() {
//                from("deltafeature://foo")
//                  .to("deltafeature://bar")
//                  .to("mock:result");
//            }
//        };
//    }
}
