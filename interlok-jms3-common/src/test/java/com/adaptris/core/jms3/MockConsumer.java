package com.adaptris.core.jms3;


import com.adaptris.core.CoreException;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

public class MockConsumer extends JmsConsumerImpl {

  private Session currentSession;;

  @Override
  public String configuredEndpoint() {
    return null;
  }

  @Override
  public MessageConsumer createConsumer() throws JMSException, CoreException {
    return null;
  }

  public void setCurrentSession(Session session) {
    currentSession = session;
  }

  @Override
  public Session currentSession() {
    return currentSession;
  }

}

