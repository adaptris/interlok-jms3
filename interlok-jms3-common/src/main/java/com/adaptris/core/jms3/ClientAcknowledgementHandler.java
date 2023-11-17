package com.adaptris.core.jms3;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

/**
*
* <p>
* <code>AcknowledgementHandler</code> implementation that handles acknowledging messages when in CLIENT_ACKNOWLEDGE mode.
* </p>
*
* @config jms3-client-acknowledgement-handler
* @author amcgrath
*/
@XStreamAlias("jms3-client-acknowledgement-handler")
@AdapterComponent
@ComponentProfile(summary = "JMS Acknowledgement handler that handles CLIENT_ACKNOWLEDGE mode.", tag = "jms")
public class ClientAcknowledgementHandler implements AcknowledgementHandler {

  @Override
  public void acknowledgeMessage(JmsActorConfig actor, Message message) throws JMSException {
    message.acknowledge();
  }

  @Override
  public void rollbackMessage(JmsActorConfig actor, Message message) {
    //do nothing
  }

}
