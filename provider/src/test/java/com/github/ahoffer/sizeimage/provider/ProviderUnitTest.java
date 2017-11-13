package com.github.ahoffer.sizeimage.provider;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ProviderUnitTest {

  AbstractImageSizer sizer;

  @Before
  public void setup() {
    sizer =
        new AbstractImageSizer() {
          @Override
          public BufferedImage generate() throws IOException {
            return null;
          }
        };
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
    configuration.put("key", "value");

    // Cannot use an abstract class for this test.
    BasicSizer realSizer = new BasicSizer();
    realSizer.setConfiguration(configuration);
    realSizer.setInput(new BufferedInputStream(null));

    /* TODO The method under test, "getNew" throws a CloneNotSupportedException because JUnit changes "this" to point to the unit test. */
    //    AbstractImageSizer sizerClone = (AbstractImageSizer) sizer.getNew();
    //    assertThat(sizerClone.getConfiguration().isEmpty(), equalTo(false));
    //    assertThat(sizerClone.inputStream, nullValue());
  }

  @Test(expected = RuntimeException.class)
  public void testCloningAbstractOrInterface() {
    sizer.getNew();
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

  @Test(expected = NullPointerException.class)
  public void testValidateInputStream() {
    sizer.setOutputSize(1, 1);
    sizer.validateBeforeResizing();
  }
}
