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

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import javax.validation.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.NullConnection;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Queue implementation of {@link JmsPollingConsumerImpl}.
 * </p>
 *
 * @config jms3-queue-poller
 *
 */
@XStreamAlias("jms3-queue-poller")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from a JMS Queue by actively polling for them", tag = "consumer,jms",
    recommended = {NullConnection.class})
@DisplayOrder(
    order = {"queue", "messageSelector", "poller", "vendorImplementation", "userName",
        "password", "clientId", "acknowledgeMode", "messageTranslator"})
public class PtpPollingConsumer extends JmsPollingConsumerImpl {

  /**
   * The JMS Topic
   *
   */
  @Getter
  @Setter
  @NotBlank
  private String queue;

  public PtpPollingConsumer() {
    super();
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException {
    return getVendorImplementation().createQueueReceiver(getQueue(), getMessageSelector(), this);
  }

  @Override
  protected String configuredEndpoint() {
    return getQueue();
  }

  public PtpPollingConsumer withQueue(String s) {
    setQueue(s);
    return this;
  }

}
