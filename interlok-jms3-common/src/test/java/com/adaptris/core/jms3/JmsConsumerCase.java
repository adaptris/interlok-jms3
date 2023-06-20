package com.adaptris.core.jms3;

import com.adaptris.interlok.junit.scaffolding.ExampleConsumerCase;

public abstract class JmsConsumerCase extends ExampleConsumerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "JmsConsumerExamples.baseDir";

  public JmsConsumerCase() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }
}

