package com.adaptris.core.jms3;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

class ClientAcknowledgementHandlerTest {

  private ClientAcknowledgementHandler handler;
  private JmsActorConfig actor;
  private Message message;

  @BeforeEach
  void setUp() {
    handler = new ClientAcknowledgementHandler();
    actor = mock(JmsActorConfig.class);
    message = mock(Message.class);
  }

  @Test
  void testAcknowledgeMessage() throws JMSException {
    handler.acknowledgeMessage(actor, message);

    verify(message).acknowledge();
    verifyNoMoreInteractions(message);
    verifyNoInteractions(actor);
  }

  @Test
  void testAcknowledgeMessagePropagatesJmsException() throws JMSException {
    JMSException exception = new JMSException("Acknowledgement failed");
    doThrow(exception).when(message).acknowledge();

    JMSException thrown = assertThrows(JMSException.class,
        () -> handler.acknowledgeMessage(actor, message));

    assertSame(exception, thrown);
    verify(message).acknowledge();
    verifyNoMoreInteractions(message);
    verifyNoInteractions(actor);
  }

  @Test
  void testRollbackMessageDoesNothing() {
    handler.rollbackMessage(actor, message);

    verifyNoInteractions(actor, message);
  }
}
