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

package com.adaptris.core.jms3;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms3.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms3.activemq.EmbeddedArtemis;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class PasProducerTest extends BasicJmsProducerCase {

  @Test
  public void testDoProduceDelegatesToProduce() throws Exception {
    final String[] capturedDest = new String[1];
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("payload");

    PasProducer producer = new PasProducer() {
      @Override
      public void produce(AdaptrisMessage m, String dest) throws ProduceException {
        capturedDest[0] = dest;
        assertSame(msg, m);
      }
    };

    String dest = "topic:TestTopic";
    producer.doProduce(msg, dest);

    assertEquals(dest, capturedDest[0]);
  }

  @Test
  public void testDoRequestDelegatesToRequest() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("request-payload");
    AdaptrisMessage cannedReply = AdaptrisMessageFactory.getDefaultInstance().newMessage("reply-payload");

    final Object[] captured = new Object[2]; // [0]=dest, [1]=timeout

    PasProducer producer = new PasProducer() {
      @Override
      public AdaptrisMessage request(AdaptrisMessage m, String dest, long timeout) throws ProduceException {
        captured[0] = dest;
        captured[1] = timeout;
        assertSame(msg, m);
        return cannedReply;
      }
    };

    String dest = "topic:RequestTopic";
    long timeout = 1500L;

    AdaptrisMessage result = producer.doRequest(msg, dest, timeout);

    assertSame(cannedReply, result);
    assertEquals(dest, captured[0]);
    assertEquals(timeout, ((Long) captured[1]).longValue());
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return retrieveSampleConfig();
  }

  @Override
  protected String createBaseFileName(Object object) {
    ((StandaloneProducer) object).getProducer();
    return super.createBaseFileName(object);
  }

  private StandaloneProducer retrieveSampleConfig() {

    PasProducer p = new PasProducer().withTopic("topicName");
    JmsConnection c = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    NullCorrelationIdSource mcs = new NullCorrelationIdSource();
    p.setCorrelationIdSource(mcs);

    StandaloneProducer result = new StandaloneProducer();

    result.setConnection(c);
    result.setProducer(p);

    return result;
  }


  @Override
  protected DefinedJmsProducer createProducer(String dest) {
    return new PasProducer().withTopic(dest);
  }

  @Override
  protected JmsConsumerImpl createConsumer(String dest) {
    PasConsumer pas = new PasConsumer();
    pas.setTopic(dest);
    return pas;
  }

  @Override
  protected TopicLoopback createLoopback(EmbeddedArtemis mq, String dest) {
    return new TopicLoopback(mq, dest);
  }

}
