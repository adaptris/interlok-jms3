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
import jakarta.jms.Message;
import jakarta.jms.Session;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageTranslator;
import com.adaptris.core.ComponentLifecycle;

// abstract factory pattern

/**
 * <p>
 * Interface that translate <code>AdaptrisMessage</code>s to the various type of
 * <code>jakarta.jms.Message</code>s, and vice versa.
 * </p>
 */
public interface MessageTypeTranslator extends ComponentLifecycle,
    AdaptrisMessageTranslator {

  /**
   * <p>
   * Translates the passed <code>AdaptrisMessage</code> into an instance of a
   * subclass of <code>jakarta.jms.Message</code>.
   * </p>
   * 
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a <code>jakarta.jms.Message</code>
   * @throws JMSException
   */
  Message translate(AdaptrisMessage msg) throws JMSException;

  /**
   * <p>
   * Translates the passed <code>jakarta.jms.Message</code> into an instance of
   * <code>AdaptrisMessage</code>.
   * </p>
   * 
   * @param msg the <code>jakarta.jms.Message</code> to translate
   * @return a <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  AdaptrisMessage translate(Message msg) throws JMSException;

  /**
   * Register the JMS session with this message translator.
   * 
   * @param s the session.
   */
  void registerSession(Session s);

  /**
   * Obtain the JMS session currently registered.
   * 
   * @return the session
   */
  Session currentSession();
}
