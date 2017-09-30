package com.github.ahoffer.sizeimage.test;

import com.github.ahoffer.sizeimage.ImageSizer;
import static com.github.ahoffer.sizeimage.provider.AbstractImageSizer.OUTPUT_SIZE;
import com.github.ahoffer.sizeimage.provider.SamplingImageSizer;
import static com.github.ahoffer.sizeimage.provider.SamplingImageSizer.SAMPLING_PERIOD;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.TestCase.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class SamplingSizerTest {

  TestData data;

  @Before
  public void setup() {
    data = new TestData();
  }

  @Test(expected = Exception.class)
  public void testNullInputStreamForSampling() throws IOException {
    ImageSizer sizer = new SamplingImageSizer();
    sizer.setInput(null);
    sizer.setOutputSize(256);
    sizer.size();
  }

  @Test(expected = Exception.class)
  public void testBadOutputSize() throws IOException {
    ImageSizer sizer = new SamplingImageSizer();
    sizer.setOutputSize(0);
    sizer.size();
  }

  @Test(expected = RuntimeException.class)
  public void testNoOutputSize() throws IOException {
    ImageSizer sizer = new SamplingImageSizer();
    sizer.setInput(data.vanillaJpegStream).size();
  }

  @Test
  public void happyPath() throws IOException {
    ImageSizer sizer = new SamplingImageSizer();
    sizer.setOutputSize(256);
    sizer.setInput(data.vanillaJpegStream);
    BufferedImage output = sizer.size();
    assertEquals(256, output.getHeight());
  }

  @Test
  public void happyPathWithCustomSamplingPeriod() throws IOException {
    ImageSizer sizer = new SamplingImageSizer();
    Map<String, String> configuration = new HashMap<>();
    configuration.put(OUTPUT_SIZE, "256");
    configuration.put(SAMPLING_PERIOD, "4");
    BufferedImage output =
        sizer.setInput(data.vanillaJpegStream).setConfiguration(configuration).size();
    assertEquals(256, output.getHeight());
  }
}
