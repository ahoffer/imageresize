package com.github.ahoffer.sizeimage.test;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.AbstractImageSizer;
import com.github.ahoffer.sizeimage.provider.BasicImageSizer;
import com.github.ahoffer.sizeimage.provider.MagickSizer;
import com.github.ahoffer.sizeimage.provider.SamplingImageSizer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import static junit.framework.TestCase.fail;
import org.apache.commons.io.FilenameUtils;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class IoTest {

  public static final String TEST_PATH_TO_MAGICK_EXEC =
      FilenameUtils.getFullPath("/opt/local/bin/");

  public static final int PIXELS = 128;
  TestData data;

  @Before
  public void setup() {
    data = new TestData();
  }

  @Test
  public void testBasicSizer() throws IOException {
    doSize(new BasicImageSizer());
  }

  @Test
  public void testSamplingSizer() throws Exception {
    doSize(new SamplingImageSizer());
  }

  @Test
  public void testMagickSizer() throws IOException {
    ImageSizer sizer = new MagickSizer();
    HashMap configuration = new HashMap();
    configuration.put(MagickSizer.PATH_TO_IMAGE_MAGICK_EXECUTABLES, TEST_PATH_TO_MAGICK_EXEC);
    sizer.setConfiguration(configuration);
    assertThat(sizer.isAvailable(), equalTo(true));
    doSize(sizer);
  }

  private void doSize(ImageSizer sizer) throws IOException {
    sizer.setInput(data.vanillaJpegStream);
    sizer.setOutputSize(PIXELS, PIXELS);
    BufferedImage output = sizer.size();
    assertThat(output.getWidth(), equalTo(PIXELS));
    assertThat(output.getHeight(), org.hamcrest.Matchers.lessThanOrEqualTo(PIXELS));

    // Test the reference to the input stream is gone.
    try {
      ((AbstractImageSizer) sizer).validateBeforeResizing();
    } catch (NullPointerException e) {
      return;
    }

    fail("Should have thrown NPE");
  }
}
