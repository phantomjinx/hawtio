package io.hawt.example.spring.boot;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import java.util.Date;
import java.text.SimpleDateFormat;

public class FileProcessor implements Processor {
   private static Object locked = new Object();

  public void process(Exchange exchange) throws Exception {
    String originalFileName = (String) exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);



     synchronized(locked) {
    	 Thread.sleep(120000);
     }

    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    String changedFileName = dateFormat.format(date) + originalFileName;
    exchange.getIn().setHeader(Exchange.FILE_NAME, changedFileName);
  }
}
