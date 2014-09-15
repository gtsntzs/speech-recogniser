package org.apache.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class micComponentTest extends CamelTestSupport {

    @Test
    public void testmic() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("mic://foo?standardCodec=cd")
                  .to("mock:result");
            }
        };
    }
}
