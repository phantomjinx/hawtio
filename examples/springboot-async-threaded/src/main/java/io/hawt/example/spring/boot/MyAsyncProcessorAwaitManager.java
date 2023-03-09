package io.hawt.example.spring.boot;

import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.impl.engine.DefaultAsyncProcessorAwaitManager;

public class MyAsyncProcessorAwaitManager extends DefaultAsyncProcessorAwaitManager {

	@Override
	public void process(AsyncProcessor processor, Exchange exchange) {
		System.out.println("=== Running My AsyncProcessorAwaitManager ===");
		super.process(processor, exchange);	
	}
}
