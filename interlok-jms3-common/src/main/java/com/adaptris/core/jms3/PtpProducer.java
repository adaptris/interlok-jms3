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

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import javax.validation.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link com.adaptris.core.AdaptrisMessageProducer} implementation for Queue based JMS.
 * <p>
 * This implementation baselines to JMS 1.1 for minimum API support and its behaviour and support
 * for standard JMS Headers can be controlled in a number of ways.
 * </p>
 * <p>
 * When converting from an {@link com.adaptris.core.AdaptrisMessage} into a standard {@link jakarta.jms.Message}; you
 * should choose an implementation of {@link MessageTypeTranslator}. The more common types are
 * {@link BytesMessageTranslator}, {@link TextMessageTranslator}, {@link ObjectMessageTranslator}
 * and {@link MapMessageTranslator} which correspond to {@link jakarta.jms.BytesMessage},
 * {@link jakarta.jms.TextMessage}, {@link jakarta.jms.ObjectMessage} and {@link jakarta.jms.MapMessage}
 * respectively. Each {@link MessageTypeTranslator} will allow you to move some or all metadata from
 * the AdaptrisMessage to the JMS Message. Of course, there are other vendor specific JMS message
 * types can be used.
 * </p>
 * <p>
 * The {@link jakarta.jms.Message#getJMSCorrelationID()} field is generally used for linking one
 * message to with another. It typically links a reply message with the originating request message.
 * If you need to handle the correlation id in some fashion then typically you would choose an
 * implementation of {@link CorrelationIdSource} that explicitly handles the correlation ID; for
 * instance {@link MetadataCorrelationIdSource}.
 * </p>
 * <p>
 * Synchronous request/reply messaging behaviour is available for this producer and relies heavily
 * on the {@link jakarta.jms.Message#getJMSReplyTo()} field. Normally if the adapter is initiating the
 * request then a temporary destination is created and this is used as the JMSReplyTo field.
 * Sometimes you may wish to specify your own JMSReplyTo field (where the JMS Vendor doesn't play
 * nice with temporary queues). To do this, then you need to ensure that the metadata key
 * {@value com.adaptris.core.jms3.JmsConstants#JMS_ASYNC_STATIC_REPLY_TO} is set with the appropriate
 * queue name; this will cause that message to be produced to the queue with a JMSReplyTo set to
 * that queue. You may also use
 * {@value com.adaptris.core.jms3.JmsConstants#JMS_ASYNC_STATIC_REPLY_TO} in an asynchronous
 * workflow; if the metadata key exists in the message then it will be used to populate the
 * JMSReplyTo Field.
 * </p>
 * <p>
 * By convention, the {@link jakarta.jms.Message#getJMSPriority()},
 * {@link jakarta.jms.Message#getJMSDeliveryMode()}, and {@link jakarta.jms.Message#getJMSExpiration()}
 * are configured directly (expiration here is semantically equivalent to the element
 * {@link #setTtl(Long)} on the producer. It is possible to control it dynamically on a per message
 * basis using the element {@link #setPerMessageProperties(Boolean)}. If you opt to control these
 * fields on a per message basis then the following metadata keys are used :
 * </p>
 * <ul>
 * <li>{@value com.adaptris.core.jms3.JmsConstants#JMS_PRIORITY} - This overrides the priority of the
 * message and should be an integer value.</li>
 * <li>{@value com.adaptris.core.jms3.JmsConstants#JMS_DELIVERY_MODE} - This overrides the delivery
 * mode of the message, and can either be an integer or a string value understood by
 * {@link DeliveryMode}</li>
 * <li>{@value com.adaptris.core.jms3.JmsConstants#JMS_EXPIRATION} - This overrides the expiration of
 * the message, and can either be an long value specifying when the message expires, or a string
 * value in the form "yyyy-MM-dd'T'HH:mm:ssZ". It will be used to calculate the correct TTL.</li>
 * </ul>
 *
 * @config jms3-queue-producer
 *
 */
@XStreamAlias("jms3-queue-producer")
@AdapterComponent
@ComponentProfile(summary = "Place message on a JMS Queue", tag = "producer,jms", recommended = {JmsConnection.class})
@DisplayOrder(order = {"queue", "destination", "messageTranslator", "deliveryMode", "priority",
    "ttl", "acknowledgeMode"})
@NoArgsConstructor
public class PtpProducer extends DefinedJmsProducer {

  /**
   * The JMS Queue
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  @NotBlank
  private String queue;

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getQueue(), "queue");
    super.prepare();
  }


  @Override
  protected Queue createDestination(String name) throws JMSException {
    return this.retrieveConnection(JmsConnection.class).configuredVendorImplementation().createQueue(name, this);
  }

  @Override
  protected Destination createTemporaryDestination() throws JMSException {
    return JmsDestination.DestinationType.QUEUE.createTemporaryDestination(currentSession());
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
    return request(msg, endpoint(msg), timeout);
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    produce(msg, endpoint(msg));
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return msg.resolve(getQueue());
  }

  public PtpProducer withQueue(String s) {
    setQueue(s);
    return this;
  }
}
