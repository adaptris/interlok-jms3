package com.adaptris.core.jms3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.adaptris.core.CoreException;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import org.slf4j.Logger;

public class BaseJmsPollingConsumerImplTest {

    @Test
    public void testDoProcessMessage_breaksOnIllegalStateException() throws Exception {
        MessageConsumer mc = mock(MessageConsumer.class);
        when(mc.receive(anyLong())).thenThrow(new IllegalStateException("session closed"));

        BaseJmsPollingConsumerImpl consumer = new BaseJmsPollingConsumerImpl() {
            @Override
            protected MessageConsumer createConsumer() throws JMSException, CoreException {
                return mc;
            }

            @Override
            protected Session createSession(int acknowledgeMode, boolean transacted) throws JMSException {
                return mock(Session.class);
            }

            @Override
            public Logger currentLogger() {
                return null;
            }

            @Override
            protected int processMessages() {
                return 0;
            }

            @Override
            protected void prepareConsumer() throws CoreException {

            }
        };

        consumer.initConsumer();
        int processed = consumer.doProcessMessage();

        assertEquals(0, processed);
        verify(mc).receive(anyLong());
    }
}
