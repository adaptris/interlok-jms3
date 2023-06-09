package com.adaptris.core.jms3;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
*
* <p>
* <code>AcknowledgementHandler</code> implementation that does nothing.
* </p>
* <p>
* Typically this implementation would be used when you're using managed JMS transactions such as XA or running in AUTO_ACKNOWLEDGE mode.
* </p>
*
* @config jms3-no-op-acknowledgement-handler
* @author amcgrath
*/
@XStreamAlias("jms3-no-op-acknowledgement-handler")
@AdapterComponent
@ComponentProfile(summary = "JMS Acknowledgement handler that skips all acknowledgements, rollbacks and commits.", tag = "jms")
public class NoOpAcknowledgementHandler implements AcknowledgementHandler {

  @Override
  public void acknowledgeMessage(JmsActorConfig actor, Message message) throws JMSException {
    //do nothing
  }

  @Override
  public void rollbackMessage(JmsActorConfig actor, Message message) {
    //do nothing
  }

}
