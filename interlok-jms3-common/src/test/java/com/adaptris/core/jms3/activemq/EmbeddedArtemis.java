/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.jms3.activemq;

import static com.adaptris.core.jms3.JmsConfig.DEFAULT_PAYLOAD;
import static com.adaptris.core.jms3.JmsConfig.DEFAULT_TTL;
import static com.adaptris.core.jms3.JmsConfig.HIGHEST_PRIORITY;
import static com.adaptris.interlok.junit.scaffolding.util.PortManager.nextUnusedPort;
import static com.adaptris.interlok.junit.scaffolding.util.PortManager.release;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnection;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQSession;
import org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assumptions;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms3.FailoverJmsConnection;
import com.adaptris.core.jms3.JmsConnection;
import com.adaptris.core.jms3.JmsConstants;
import com.adaptris.core.jms3.VendorImplementationImp;
import com.adaptris.core.jms3.jndi.StandardJndiImplementation;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.jms3.JmsConfig;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.PlainIdGenerator;
import com.adaptris.util.TimeInterval;

import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.Topic;

public class EmbeddedArtemis {
  private static final long MAX_WAIT = 20000;
  private static final int DEFAULT_WAIT_INTERVAL = 100;
  private static final String DEF_URL_PREFIX = "tcp://127.0.0.1:";

  // Found in the src/test/resources/broker.xml
  private static final String ARTEMIS_BROKER_NAME = "artemis-embedded-junit";

  private static final String QUEUE_OBJECT_NAME = "org.apache.activemq.artemis:broker=\"" + ARTEMIS_BROKER_NAME
      + "\",component=addresses,address=";

  private File brokerDataDir;
  private EmbeddedActiveMQ embeddedJMS;
  private Integer port;

  private static IdGenerator nameGenerator;
  static {
    try {
      nameGenerator = new GuidGenerator();
    } catch (Exception e) {
      nameGenerator = new PlainIdGenerator("-");
    }
  }

  public EmbeddedArtemis() throws Exception {
    Assumptions.assumeTrue(JmsConfig.jmsTestsEnabled());
    port = nextUnusedPort(51616);
  }

  public String getName() {
    return ARTEMIS_BROKER_NAME;
  }

  public void start() throws Exception {
    brokerDataDir = createTempFile(true);
    embeddedJMS = createBroker();
    try {
      embeddedJMS.start();
    } catch (Throwable t) {
      throw new Exception(t);
    }
    waitFor(embeddedJMS, MAX_WAIT);
  }

  private static void waitFor(EmbeddedActiveMQ broker, long maxWaitMs) throws Exception {
    long totalWaitTime = 0;
    while (!broker.getActiveMQServer().isStarted() && totalWaitTime < maxWaitMs) {
      Thread.sleep(DEFAULT_WAIT_INTERVAL);
      totalWaitTime += DEFAULT_WAIT_INTERVAL;
    }
    if (!broker.getActiveMQServer().isStarted()) {
      throw new Exception("Got Tired of waiting for broker to start; waited for " + totalWaitTime + "ms");
    }
  }

  private File createTempFile(boolean isDir) throws IOException {
    File result = File.createTempFile("ARTEMIS-", "");
    result.delete();
    if (isDir) {
      result.mkdirs();
    }
    return result;
  }
  
  public static AdaptrisMessage createMessage(AdaptrisMessageFactory fact) {
    AdaptrisMessage msg = fact == null ? AdaptrisMessageFactory.getDefaultInstance().newMessage(DEFAULT_PAYLOAD) : fact
        .newMessage(DEFAULT_PAYLOAD);
    msg.addMetadata(JmsConstants.JMS_PRIORITY, String.valueOf(HIGHEST_PRIORITY));
    msg.addMetadata(JmsConstants.JMS_DELIVERY_MODE, String.valueOf(DeliveryMode.NON_PERSISTENT));
    msg.addMetadata(JmsConstants.JMS_EXPIRATION, String.valueOf(DEFAULT_TTL));
    return msg;
  }
  
  public Queue createQueue(String queueName) throws Exception {
    Queue queue = null;
    try (ActiveMQConnection conn = createConnection();
        ActiveMQSession session =
            (ActiveMQSession) conn.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
      queue = session.createQueue(queueName);
    }
    return queue;
  }


  public Topic createTopic(String topicName) throws Exception {
    Topic topic = null;
    try (ActiveMQConnection conn = createConnection();
        ActiveMQSession session =
            (ActiveMQSession) conn.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
      topic = session.createTopic(topicName);
    }
    return topic;
  }
  
  public ActiveMQConnection createConnection() throws JMSException {
    ActiveMQJMSConnectionFactory fact = new ActiveMQJMSConnectionFactory("vm://" + getName());
    return (ActiveMQConnection) fact.createConnection();
  }
  
  public FailoverJmsConnection getFailoverJmsConnection(boolean isPtp) throws Exception {
    FailoverJmsConnection result = new FailoverJmsConnection();
    if (!isPtp) {
      JmsConnection c = new JmsConnection(new BasicActiveMqImplementation(DEF_URL_PREFIX + "9999"));
      result.addConnection(c);
      result.addConnection(getJmsConnection());
    }
    else {
      JmsConnection c = new JmsConnection(new BasicActiveMqImplementation(DEF_URL_PREFIX + "9999"));
      result.addConnection(c);
      result.addConnection(getJmsConnection());
    }
    return result;
  }

  public EmbeddedActiveMQ createBroker() throws Exception {
    Configuration config = new ConfigurationImpl();

    config.setName(ARTEMIS_BROKER_NAME);
    config.setSecurityEnabled(false);
    config.addAcceptorConfiguration("in-vm", "vm://0");
    config.addAcceptorConfiguration("tcp", "tcp://127.0.0.1:" + port);
    config.setPersistenceEnabled(false);
    config.setBrokerInstance(brokerDataDir);

    EmbeddedActiveMQ embeddedArtemis = new EmbeddedActiveMQ();
    embeddedArtemis.setConfiguration(config);
    // EmbeddedArtemis.setConfigResourcePath("junit-broker.xml");
    return embeddedArtemis;
  }

  public void destroy() {
    new Thread(new Runnable() {

      @Override
      public void run() {
        release(port);
        try {
          stop();
        } catch (Exception e) {

        }
      }
    }).start();
  }

  public void stop() throws Exception {
    if (embeddedJMS != null) {
      embeddedJMS.stop();
      FileUtils.deleteDirectory(brokerDataDir);
    }
  }

  public JmsConnection getJmsConnection() {
    StandardJndiImplementation standardJndiImplementation = new StandardJndiImplementation("ConnectionFactory");
    standardJndiImplementation.getJndiParams().addKeyValuePair(new KeyValuePair("java.naming.factory.initial",
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"));
    standardJndiImplementation.getJndiParams().addKeyValuePair(new KeyValuePair("java.naming.provider.url", "vm:/1"));

    return applyCfg(new JmsConnection(), standardJndiImplementation, false);
  }
  
  public JmsConnection getJmsConnection(BasicActiveMqImplementation impl) {
    return applyCfg(new JmsConnection(), impl, false);
  }

  public JmsConnection getJndiPasConnection(StandardJndiImplementation jv, boolean useJndiOnly, String queueName,
      String topicName) {
    JmsConnection result = new JmsConnection();
    StandardJndiImplementation jndi = applyCfg(jv, useJndiOnly, queueName, topicName);
    jndi.setJndiName("topicConnectionFactory");
    result.setVendorImplementation(jndi);
    result.setClientId(createSafeUniqueId(jndi));
    result.setConnectionRetryInterval(new TimeInterval(3L, TimeUnit.SECONDS));
    result.setConnectionAttempts(1);
    return result;
  }

  public JmsConnection getJndiPtpConnection(StandardJndiImplementation jv, boolean useJndiOnly, String queueName,
      String topicName) {
    JmsConnection result = new JmsConnection();
    StandardJndiImplementation jndi = applyCfg(jv, useJndiOnly, queueName, topicName);
    jndi.setJndiName("queueConnectionFactory");
    result.setVendorImplementation(jndi);
    result.setClientId(createSafeUniqueId(jndi));
    result.setConnectionRetryInterval(new TimeInterval(3L, TimeUnit.SECONDS));
    result.setConnectionAttempts(1);
    return result;
  }

  private JmsConnection applyCfg(JmsConnection con, VendorImplementationImp impl, boolean useTcp) {
    con.setClientId(createSafeUniqueId(impl));
    con.setVendorImplementation(impl);
    con.setConnectionRetryInterval(new TimeInterval(3L, TimeUnit.SECONDS));
    con.setConnectionAttempts(1);

    return con;
  }

  private StandardJndiImplementation applyCfg(StandardJndiImplementation jndi, boolean useJndiOnly, String queueName,
      String topicName) {
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair(Context.PROVIDER_URL, DEF_URL_PREFIX + port));
    jndi.getJndiParams().addKeyValuePair(
        new KeyValuePair(Context.INITIAL_CONTEXT_FACTORY, ActiveMQInitialContextFactory.class.getName()));
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair("connectionFactory.connectionFactory", "vm:/1"));
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair("connectionFactory.topicConnectionFactory", "vm:/1"));
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair("connectionFactory.queueConnectionFactory", "vm:/1"));
    
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair("queue." + queueName, queueName));
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair("topic." + topicName, topicName));
    if (useJndiOnly) {
      jndi.setUseJndiForQueues(true);
      jndi.setUseJndiForTopics(true);
    }
    return jndi;
  }

  public long messagesOnQueue(String queueName) throws Exception {
    String fullQueueObjectName = QUEUE_OBJECT_NAME + "\"" + queueName + "\"";
    MBeanServer mBeanServer = JmxHelper.findMBeanServer();

    return (long) mBeanServer.getAttribute(new ObjectName(fullQueueObjectName), "NumberOfMessages");
  }

  static String createSafeUniqueId(Object o) {
    return nameGenerator.create(o).replaceAll(":", "").replaceAll("-", "");
  }

}
