# Camel-RabbitMQ High Channel Churn - Demo

This is a demo application to show a behavior that exists in the camel-rabbitmq component.

## What it does

It will use camel 3.x and the camel-rabbitmq component to open a queue with limited size and overflow behavior "reject-publish" on RabbitMQ.
Then it will publish a lot of messages with confirmation by RabbitMQ. 

When the queue is full, RabbitMQ will confirm each additional message with a "nack" (not acknowledged).
Because of the internal implementation in the [RabbitMQMessagePublisher](https://github.com/apache/camel/blob/camel-3.20.x/components/camel-rabbitmq/src/main/java/org/apache/camel/component/rabbitmq/RabbitMQMessagePublisher.java#L165) every channel that was used to publish a message with negative acknowledgement will be closed.
The closed channel is than returned to the session pool.

Next time the channel is taken from the pool, it is detected as closed and a new channel will be opened.
If the queue is still full this opening and closing of new channels will continue.

This behavior is described as [High Channel Churn](https://www.rabbitmq.com/channels.html#high-channel-churn).

It's suboptimal and can lead to high resource usage and performance decrease.

## How to test

1. Start a local RabbitMQ instance in docker with
    `docker run -d --name rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management`
2. Open a browser with [RabbitMQ UI](http://localhost:15672) and login as guest/guest.
3. Start this demo application
4. Watch the "Channel operations" under "Churn statistics" in the "Overview" page - it will show a lot of opening and closing channels

## How to fix

This behavior can be fixed with a change (see [Pull Request](https://github.com/apache/camel/pull/9181)) for the RabbitMQMessagePublisher.

Repeating the test after this fix will show a flat line under the "Channel operations" statistics.
Channels will not be closed after a RabbitMQ "nack" anymore and will be reusable from the channel pool.
