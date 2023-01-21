package de.goeri;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        DefaultCamelContext context=new DefaultCamelContext();
        context.start();
        Endpoint endpoint = context.getEndpoint("rabbitmq://127.0.0.1:5672/test_exchg?username=guest&password=guest&autoDelete=false&routingKey=#&queue=test_queue&channelPoolMaxSize=30&channelPoolMaxWait=1000&publisherAcknowledgements=true&publisherAcknowledgementsTimeout=2000&arg.queue.x-overflow=reject-publish&arg.queue.x-max-length=10");
        final ProducerTemplate p = context.createProducerTemplate();
        p.setDefaultEndpoint(endpoint);
        Thread t = new Thread(new Runnable() {
            public void run() {
                boolean loop=true;
                while (loop && !Thread.currentThread().isInterrupted()) {
                    try {
                        try {
                            p.sendBody("Hallo");
                        } catch (CamelExecutionException e) {
                            System.out.println(e.getCause());
                        }
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        loop = false;
                    }
                }
            }
        });
        t.start();

        Thread.sleep(60000);
        t.interrupt();

        context.stop();
    }
}
