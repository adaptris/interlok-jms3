package com.adaptris.core.jms3;

import static org.mockito.Mockito.*;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.HashMap;

public class JmsAsyncProducerEventHandlerTest {

    private JmsAsyncProducerEventHandler eventHandler;
    private JmsProducer mockProducer;
    private Logger mockLogger;

    @BeforeEach
    public void setUp() {
        mockProducer = mock(JmsProducer.class);
        eventHandler = new JmsAsyncProducerEventHandler(mockProducer);
        eventHandler.setUnAckedMessages(new HashMap<>());

        // Mock the logger to verify log messages
        mockLogger = mock(Logger.class);
        eventHandler.log = mockLogger;
    }

    @Test
    public void testOnCompletion_UnknownMessage() throws JMSException {
        // Mock a JMS message with an unknown ID
        Message mockMessage = mock(Message.class);
        when(mockMessage.getStringProperty("interlokMessageId")).thenReturn("unknownMessageId");
        eventHandler.setAcceptSuccessCallbacks(true);

        // Call the method under test
        eventHandler.onCompletion(mockMessage);

        // Verify that a warning is logged for the unknown message
        verify(mockLogger).warn("Received success callback for an unknown message {}", "unknownMessageId");
    }
}