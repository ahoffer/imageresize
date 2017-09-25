package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BasicSizerTest {
  public static final int PIXELS = 256;
  SizeImageTestData data;

  @Before
  public void setup() {
    data = new SizeImageTestData();
  }

  @Test
  public void testHappyPath() throws IOException {
    ImageSizer sizer = new BasicImageSizer();
    sizer.setInput(data.jpeg2000Stream);
    sizer.setOutputSize(PIXELS);
    BufferedImage output = sizer.size();
    assertEquals(PIXELS, output.getHeight());
  }

  @Test(expected = Exception.class)
  public void testNullInputStream() throws IOException {
    ImageSizer sizer = new BasicImageSizer();
    sizer.setInput(null);
    sizer.setOutputSize(PIXELS);
    sizer.size();
  }

  @Test(expected = Exception.class)
  public void testBadOutputSize() throws IOException {
    ImageSizer sizer = new BasicImageSizer();
    sizer.setOutputSize(0);
    sizer.size();
  }

  @Test(expected = Exception.class)
  public void testNoOutputSize() throws IOException {
    ImageSizer sizer = new BasicImageSizer();
    sizer.size();
  }

  @Test
  public void testNewInstance() {
    Map<String, String> configuration = new HashMap<>();
    configuration.put("KEY", "VALUE");
    ImageSizer sizer1 = new BasicImageSizer();
    sizer1.setConfiguration(configuration);
    ImageSizer sizer2 = sizer1.getNew();
    assertNotSame("The start() method should return a new instance", sizer1, sizer2);
    assertTrue(
        "The start() method did not instantiate the correct class",
        sizer2 instanceof BasicImageSizer);
  }
}
