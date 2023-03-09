package io.hawt.example.spring.boot;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.support.AsyncProcessorHelper;

import com.Ostermiller.util.CircularByteBuffer;

public class AsyncExecutorProcessor implements AsyncProcessor {
  private final ExecutorService executorService = Executors.newFixedThreadPool(2);

  public CompletableFuture<Exchange> processAsync(Exchange exchange) {
    throw new RuntimeException("Should not be called here????");
  }

  @Override
  public void process(Exchange exchange) throws Exception {
	System.out.println("Calling STRAIGHT process(exchange)");
	AsyncProcessorHelper.process(this, exchange); //Wrap synchronous invocations
  }

  @Override
  public boolean process(Exchange exchange, AsyncCallback callback) {
    System.out.println("Calling process()");
    CircularByteBuffer buffer = new CircularByteBuffer(1024 * 100); //Buffer a 100K
    executorService.submit(new AsyncWorker(exchange, callback, buffer.getOutputStream())); //Generate the PDF in a separate thread
    exchange.getIn().setBody(buffer.getInputStream()); //Immediately return the stream for the client to read from

    ExtendedCamelContext extendedCamelContext = exchange.getContext().adapt(ExtendedCamelContext.class);
    long number = extendedCamelContext.getAsyncProcessorAwaitManager().getStatistics().getThreadsBlocked();
    System.out.println("Threads blocked: " + number);

    return false;
  }

  private class AsyncWorker implements Runnable {
    private Exchange exchange;
    private AsyncCallback callback;
    private OutputStream out;
    private Object locked = new Object();

    private AsyncWorker(Exchange exchange, AsyncCallback callback, OutputStream out) {
      this.exchange = exchange;
      this.callback = callback;
      this.out = out;
    }

    @Override
    public void run() {
      try {
        System.out.println("Pretending to do some work");
        this.exchange.setProperty("Response", "Working");
        synchronized(locked) {
          System.out.println("Calling wait on locked");
          locked.wait();
        }
        System.out.println("Moved on from locked.wait()");
        // Thread.sleep(60000);
        // generatePDF(out); //Actual logic for generating the file and writing it to the stream is here
      } catch (Exception e) {
        exchange.setException(e); //Async processors must not throw exceptions and must add them to the Exchange instead
      } finally {
        // callback must be invoked
        System.out.println("Calling done callback");
        callback.done(false);
        //Cleanup, close streams etc.
        try{out.flush(); out.close();} catch(Exception e) {/*ignore*/}
      }
    }
  }
}
