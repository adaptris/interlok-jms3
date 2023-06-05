package com.adaptris.core.jms3;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

@FunctionalInterface
public interface ConsumerCreator {

  public MessageConsumer createConsumer(Session session, JmsDestination destination, String filterExpression) throws JMSException;
  
}
