package de.goeri;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.ArrayList;

public class App 
{
    public static void main( String[] args ) throws Exception
    {
        DefaultCamelContext context=new DefaultCamelContext();
        context.start();
        // Create an exchange and a queue with overflow behavior "reject-publish" and max. length of 10 entries
        // Also use publish acknowledgements for this demo
        Endpoint endpoint = context.getEndpoint("rabbitmq://127.0.0.1:5672/test_exchg?username=guest&password=guest&autoDelete=false&routingKey=#&queue=test_queue&channelPoolMaxSize=100&channelPoolMaxWait=1000&publisherAcknowledgements=true&publisherAcknowledgementsTimeout=2000&arg.queue.x-overflow=reject-publish&arg.queue.x-max-length=10");
        final ProducerTemplate p = context.createProducerTemplate();
        p.setDefaultEndpoint(endpoint);
        // Publish a lot of messages in an endless loop to the exchange/queue

        ArrayList<Thread> workers = new ArrayList<>();
        for (int i=0; i<5; i++) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    boolean loop = true;
                    while (loop && !Thread.currentThread().isInterrupted()) {
                        try {
                            try {
                                p.sendBody("Hello");
                            } catch (CamelExecutionException e) {
                                System.out.println(e.getCause());
                            }
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            loop = false;
                        }
                    }
                }
            });
            workers.add(t);
        }
        workers.forEach(w -> w.start());

        // Send messages in background for 1 minute and then quit
        Thread.sleep(60000);
        workers.forEach(w -> w.interrupt());

        Thread.sleep(5000);

        context.stop();
    }
}
