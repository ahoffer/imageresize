package com.github.ahoffer.sizeimage.provider;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ProviderUnitTest {

  BasicSizer sizer;

  @Before
  public void setup() {
    sizer = new BasicSizer();
  }

  @Test
  public void testSetAndGetConfiguration() {
    HashMap configuration = new HashMap();
    configuration.put("key", "value");
    sizer.setConfiguration(configuration);
    Map<String, String> actualConfig = sizer.getConfiguration();
    assertThat(actualConfig.get("key"), equalTo("value"));
  }

  @Test
  public void testImmutableConfiguration() {
    HashMap configuration = new HashMap();
    configuration.put("key", "value");
    sizer.setConfiguration(configuration);
    configuration.put("key", "bad");
    Map<String, String> actualConfig = sizer.getConfiguration();
    assertThat(actualConfig.get("key"), not(equalTo("bad")));
  }

  @Test
  public void testCloning() {
    HashMap configuration = new HashMap();
    int testInt = 42;
    String key = "key";
    String value = "value";
    configuration.put(key, value);

    BasicSizer original = new BasicSizer();
    original.setConfiguration(configuration);
    BufferedInputStream inputStream = new BufferedInputStream(null);
    original.setInput(inputStream);
    original.setOutputSize(testInt, testInt);
    assertThat(original.inputStream, notNullValue());
    assertThat(original.getConfiguration(), notNullValue());
    BasicSizer copy = (BasicSizer) original.getNew();
    assertThat(copy.inputStream, nullValue());
    assertThat(copy.getConfiguration(), hasEntry(key, value));
    assertThat(copy.getMaxHeight(), is(testInt));
    assertThat(copy.getMaxWidth(), is(testInt));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtentsTooSmall() {
    sizer.setOutputSize(0, 1);
  }

  @Test(expected = RuntimeException.class)
  public void testNotNumber() {
    HashMap configuration = new HashMap();
    configuration.put(AbstractImageSizer.MAX_HEIGHT, "");
    sizer.setConfiguration(configuration);
    sizer.getMaxHeight();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateExtentsSetter() {
    sizer.setOutputSize(0, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidatingExtentsConfiguration() {
    HashMap<String, String> config = new HashMap<>();
    config.put(AbstractImageSizer.MAX_HEIGHT, "1");
    config.put(AbstractImageSizer.MAX_WIDTH, "");
    sizer.setConfiguration(config);
    sizer.validateBeforeResizing();
  }
}
