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

import java.util.concurrent.TimeUnit;

import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms3.activemq.BasicActiveMqImplementation;
import com.adaptris.interlok.junit.scaffolding.ExampleConsumerCase;
import com.adaptris.util.TimeInterval;

public class BasicPtpConsumerActiveErorHandlerTest
    extends ExampleConsumerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "JmsConsumerExamples.baseDir";

  static final String DEFAULT_XML_COMMENT = "\n<!--"
      + "\nThis is just an example consumer with an ActiveJmsConnectionErrorHandler"
      + "\nThis type of error handler simply uses a temporary queue (or topic) and periodically produces a"
      + "\nnon-persistent message with a TTL of 5000ms; if this fails, then the connection"
      + "\nis deemed to have failed and the connection restarted." + "\n-->\n";
  
  public BasicPtpConsumerActiveErorHandlerTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JmsConnection p = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:61616"));
    ActiveJmsConnectionErrorHandler erHandler = new ActiveJmsConnectionErrorHandler();
    erHandler.setCheckInterval(new TimeInterval(30L, TimeUnit.SECONDS));
    p.setConnectionErrorHandler(erHandler);
    return new StandaloneConsumer(p, new PtpConsumer().withQueue("TheQueueToConsumeFrom"));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-" + ActiveJmsConnectionErrorHandler.class.getSimpleName();
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }
}
