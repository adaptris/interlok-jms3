package com.adaptris.core.jms3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

class AsyncAcknowledgementHandlerTest {

  private AsyncAcknowledgementHandler handler;
  private JmsActorConfig actor;
  private Message message;

  @BeforeEach
  void setUp() {
    handler = new AsyncAcknowledgementHandler();
    actor = mock(JmsActorConfig.class);
    message = mock(Message.class);
  }

  @Test
  void testAcknowledgeMessageDoesNothing() throws JMSException {
    handler.acknowledgeMessage(actor, message);

    verifyNoInteractions(actor, message);
  }

  @Test
  void testRollbackMessageDoesNothing() {
    handler.rollbackMessage(actor, message);

    verifyNoInteractions(actor, message);
  }

  @Test
  void testOnCompletionDoesNothing() {
    handler.onCompletion(message);

    verifyNoInteractions(message);
  }

  @Test
  void testOnExceptionDoesNothing() {
    Exception exception = mock(Exception.class);

    handler.onException(message, exception);

    verifyNoInteractions(message, exception);
  }
}
