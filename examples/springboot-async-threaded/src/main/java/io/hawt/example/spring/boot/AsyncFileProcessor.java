package io.hawt.example.spring.boot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.support.AsyncProcessorHelper;

public class AsyncFileProcessor implements AsyncProcessor {
	private static Object locked = new Object();

	public void process(Exchange exchange) throws Exception {
		AsyncProcessorHelper.process(this, exchange); // Wrap synchronous invocations
	}

	@Override
	public boolean process(Exchange exchange, AsyncCallback callback) {
		String originalFileName = (String) exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);

		synchronized (locked) {
			try {
				Thread.sleep(120000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		String changedFileName = dateFormat.format(date) + originalFileName;
		exchange.getIn().setHeader(Exchange.FILE_NAME, changedFileName);

		return false;
	}

	@Override
	public CompletableFuture<Exchange> processAsync(Exchange exchange) {
		throw new UnsupportedOperationException("processAsync not implemented");
	}
}
