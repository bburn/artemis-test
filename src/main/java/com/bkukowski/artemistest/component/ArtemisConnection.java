package com.bkukowski.artemistest.component;

import org.apache.activemq.artemis.api.core.DiscoveryGroupConfiguration;
import org.apache.activemq.artemis.api.core.UDPBroadcastEndpointFactory;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.jms.*;

@Service
public class ArtemisConnection implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {

        final String groupAddress = "231.7.7.7";
        final int groupPort = 9876;
        DiscoveryGroupConfiguration discoveryGroupConfiguration = new DiscoveryGroupConfiguration();
        UDPBroadcastEndpointFactory udpBroadcastEndpointFactory =
                new UDPBroadcastEndpointFactory().setGroupAddress(groupAddress)
                .setGroupPort(groupPort);
        discoveryGroupConfiguration.setBroadcastEndpointFactory(udpBroadcastEndpointFactory);
        ConnectionFactory jmsConnectionFactory =
                ActiveMQJMSClient.createConnectionFactoryWithHA(discoveryGroupConfiguration, JMSFactoryType.CF);
        Connection jmsConnection1 = jmsConnectionFactory.createConnection();
        Connection jmsConnection2 = jmsConnectionFactory.createConnection();
        Connection connection0 = null;
        Connection connection1 = null;

        try {
            // Step 2. Instantiate the Queue
            Queue queue = ActiveMQJMSClient.createQueue("exampleQueue");

            // Step 6. We create a JMS Connection connection0 which is a connection to server 0
            connection0 = jmsConnection1;

            // Step 7. We create a JMS Connection connection1 which is a connection to server 1
            connection1 = jmsConnection2;

            // Step 8. We create a JMS Session on server 0
            Session session0 = connection0.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Step 9. We create a JMS Session on server 1
            Session session1 = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Step 10. We start the connections to ensure delivery occurs on them
            connection0.start();

            connection1.start();

            // Step 11. We create JMS MessageConsumer objects on server 0 and server 1
            MessageConsumer consumer0 = session0.createConsumer(queue);

            MessageConsumer consumer1 = session1.createConsumer(queue);

            Thread.sleep(1000);

            // Step 12. We create a JMS MessageProducer object on server 0
            MessageProducer producer = session0.createProducer(queue);

            // Step 13. We send some messages to server 0

            final int numMessages = 10;

            for (int i = 0; i < numMessages; i++) {
                TextMessage message = session0.createTextMessage("This is text message " + i);

                producer.send(message);

                System.out.println("Sent message: " + message.getText());
            }

            // Step 14. We now consume those messages on *both* server 0 and server 1.
            // We note the messages have been distributed between servers in a round robin fashion
            // JMS Queues implement point-to-point message where each message is only ever consumed by a
            // maximum of one consumer

            for (int i = 0; i < numMessages; i += 2) {
                TextMessage message0 = (TextMessage) consumer0.receive(5000);

                System.out.println("Got message: " + message0.getText() + " from node 0");

                TextMessage message1 = (TextMessage) consumer1.receive(5000);

                System.out.println("Got message: " + message1.getText() + " from node 1");
            }
        } finally {
            // Step 15. Be sure to close our resources!

            if (connection0 != null) {
                connection0.close();
            }

            if (connection1 != null) {
                connection1.close();
            }
        }
    }

}
