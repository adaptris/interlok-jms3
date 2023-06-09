package com.adaptris.core.jms3;

import java.util.Arrays;
import java.util.List;

import com.adaptris.interlok.junit.scaffolding.BaseCase;

public abstract class JmsConfig {

  public static final long DEFAULT_TTL = System.currentTimeMillis() + 600000;
  public static final int HIGHEST_PRIORITY = 9;
  public static final int LOWEST_PRIORITY = 1;
  public static final String DEFAULT_PAYLOAD = "aaaaaaaa";

  private static final MessageTypeTranslator[] MESSAGE_TRANSLATORS =
  {
      new TextMessageTranslator(), new BytesMessageTranslator(), new ObjectMessageTranslator(), new MapMessageTranslator("key1"),
      new AutoConvertMessageTranslator()
  };

  public static final List<MessageTypeTranslator> MESSAGE_TRANSLATOR_LIST =
      Arrays
      .asList(MESSAGE_TRANSLATORS);

  public static boolean jmsTestsEnabled() {
    return Boolean.parseBoolean(BaseCase.PROPERTIES.getProperty("jms.tests.enabled", "true"));
  }
}