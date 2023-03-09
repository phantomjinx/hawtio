package io.hawt.example.spring.boot;

import java.io.File;

import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.camel.spi.Tracer;
import org.springframework.stereotype.Component;


@Component
public class SampleCamelRouter extends RouteBuilder {
	/**
	 * Need to create a directory $HOME/test-src and fill with some files
	 */
    private static final String SOURCE_FOLDER = System.getProperty("user.home") + File.separator + "test-src";
    /**
     * Need to create a directory $HOME/test-dest
     */
    private static final String DESTINATION_FOLDER = System.getProperty("user.home") + File.separator + "test-dest";

    @Override
    public void configure() throws Exception {
      // Uncomment to enable the Camel plugin Debug tab
      getContext().setDebugging(true);
      getContext().setTracing(true);

      Tracer tracer = getContext().getTracer();
      tracer.setEnabled(true);

      ThreadPoolProfile profile = getContext().getExecutorServiceManager().getDefaultThreadPoolProfile();
      profile.setPoolSize(5);
      profile.setMaxPoolSize(10);

//      MyAsyncProcessorAwaitManager manager = new MyAsyncProcessorAwaitManager();
      
      ExtendedCamelContext extendedCamelContext = getContext().adapt(ExtendedCamelContext.class);
      extendedCamelContext.getExchangeFactoryManager().setStatisticsEnabled(true);
      extendedCamelContext.getExchangeFactory().setStatisticsEnabled(true);
      extendedCamelContext.getProcessorExchangeFactory().setStatisticsEnabled(true);
//      extendedCamelContext.setAsyncProcessorAwaitManager(manager);
      extendedCamelContext.getAsyncProcessorAwaitManager().getStatistics().setStatisticsEnabled(true);


      // Allow inflight to be browsed
      getContext().getInflightRepository().setInflightBrowseEnabled(true);

      from("file://" + SOURCE_FOLDER + "?noop=true&synchronous=false")
      .threads(5)
//      	.to("seda:processFile");
//      
//      from("seda:processFile")
      	.process(new AsyncFileProcessor())
      	.to("file://" + DESTINATION_FOLDER);
      
    }

}
