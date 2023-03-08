package io.hawt.example.spring.boot;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;


@Component
public class SampleCamelRouter extends RouteBuilder {
    private static final long DURATION_MILIS = 10000;
    private static final String SOURCE_FOLDER = "/home/phantomjinx/data-configs";
    private static final String DESTINATION_FOLDER = "/home/phantomjinx/test-dest";

    @Override
    public void configure() throws Exception {
        // Uncomment to enable the Camel plugin Debug tab
        getContext().setDebugging(true);

        from("file://" + SOURCE_FOLDER + "?noop=true").process(
          new FileProcessor()).to("file://" + DESTINATION_FOLDER);
    }

}
