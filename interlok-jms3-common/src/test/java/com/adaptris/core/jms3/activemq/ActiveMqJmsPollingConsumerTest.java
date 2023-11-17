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

import static com.adaptris.core.jms3.activemq.ActiveMqPasPollingConsumerTest.shutdownQuietly;
import static com.adaptris.core.jms3.activemq.ActiveMqPasPollingConsumerTest.startAndStop;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.start;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.waitForMessages;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase.assertMessages;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase.createMessage;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.Poller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms3.JmsConnection;
import com.adaptris.core.jms3.JmsPollingConsumer;
import com.adaptris.core.jms3.JmsProducer;
import com.adaptris.core.jms3.activemq.ActiveMqPasPollingConsumerTest.Sometime;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class ActiveMqJmsPollingConsumerTest {

  
  
  
  private static EmbeddedArtemis activeMqBroker;

  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedArtemis();
    activeMqBroker.start();
  }
  
  @AfterAll
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }

  @Test
  public void testTopic_NoSubscriptionId(TestInfo info) throws Exception {
    String rfc6167 = "jms:topic:" + info.getDisplayName();
    final StandaloneConsumer consumer =
        createStandaloneConsumer(activeMqBroker, info.getDisplayName(), rfc6167);
    try {
      consumer.registerAdaptrisMessageListener(new MockMessageListener());
      // This won't fail, but... there will be errors in the log file...
      start(consumer);
    } finally {
      shutdownQuietly(null, consumer);
    }
  }

  @Test
  public void testQueue_ProduceConsume(TestInfo info) throws Exception {
    int msgCount = 5;
    String rfc6167 = "jms:queue:" + info.getDisplayName();
    final StandaloneProducer sender =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), new JmsProducer().withEndpoint(rfc6167));
    final StandaloneConsumer receiver =
        createStandaloneConsumer(activeMqBroker, info.getDisplayName(), rfc6167);
    try {
      MockMessageListener jms = new MockMessageListener();
      receiver.registerAdaptrisMessageListener(jms);
      // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
      LifecycleHelper.prepare(sender);
      LifecycleHelper.prepare(receiver);
      start(receiver);
      start(sender);
      for (int i = 0; i < msgCount; i++) {
        sender.doService(createMessage());
      }
      waitForMessages(jms, msgCount);
      assertMessages(jms, msgCount);
    } finally {
      shutdownQuietly(sender, receiver);
    }
  }

  @Test
  public void testTopic_ProduceConsume(TestInfo info) throws Exception {
    int msgCount = 5;
    String rfc6167 =
        "jms:topic:" + info.getDisplayName() + "?subscriptionId=" + info.getDisplayName();
    final StandaloneProducer sender =
        new StandaloneProducer(activeMqBroker.getJmsConnection(), new JmsProducer().withEndpoint(rfc6167));
    Sometime poller = new Sometime();
    JmsPollingConsumer consumer = createConsumer(activeMqBroker, info.getDisplayName(), rfc6167, poller);
    final StandaloneConsumer receiver = new StandaloneConsumer(consumer);
    try {
      MockMessageListener jms = new MockMessageListener();
      receiver.registerAdaptrisMessageListener(jms);

      startAndStop(receiver);
      start(receiver);
      start(sender);
      for (int i = 0; i < msgCount; i++) {
        sender.doService(createMessage());
      }
      waitForMessages(jms, msgCount);
      assertMessages(jms, msgCount);
    } finally {
      shutdownQuietly(sender, receiver);
    }
  }

  private StandaloneConsumer createStandaloneConsumer(EmbeddedArtemis broker, String threadName, String destinationName)
      throws Exception {
    return new StandaloneConsumer(createConsumer(broker, threadName, destinationName));
  }

  private JmsPollingConsumer createConsumer(EmbeddedArtemis broker, String threadName, String destinationName, Poller poller) {
    JmsPollingConsumer consumer = new JmsPollingConsumer().withEndpoint(destinationName);
    consumer.setPoller(poller);
    JmsConnection c = broker.getJmsConnection();
    consumer.setClientId(c.configuredClientId());
    consumer.setUserName(c.configuredUserName());
    consumer.setPassword(c.configuredPassword());
    consumer.setReacquireLockBetweenMessages(true);
    consumer.setVendorImplementation(c.getVendorImplementation());
    return consumer;
  }

  private JmsPollingConsumer createConsumer(EmbeddedArtemis broker, String threadName, String destinationName) {
    return createConsumer(broker, threadName, destinationName, new FixedIntervalPoller(
        new TimeInterval(500L, TimeUnit.MILLISECONDS)));
  }

}
