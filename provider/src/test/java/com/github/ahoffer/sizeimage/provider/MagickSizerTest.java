package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import static com.github.ahoffer.sizeimage.provider.ImageMagickSizer.*;
import static com.github.ahoffer.sizeimage.provider.SizeImageTestData.JPEG;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.TestCase.assertTrue;
import org.apache.commons.io.FilenameUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

public class MagickSizerTest {

  public static final String TEST_PATH_TO_MAGICK_EXEC =
      FilenameUtils.getFullPath("/opt/local/bin/");

  SizeImageTestData data;

  @Before
  public void setup() {
    data = new SizeImageTestData();
  }

  @Test
  public void testGoodPath() {
    ImageMagickSizer magick = new ImageMagickSizer();
    Map<String, String> configuration = new HashMap<>();
    configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, TEST_PATH_TO_MAGICK_EXEC);
    magick.setConfiguration(configuration);
    assertTrue(magick.getImageMagickExecutable().canExecute());
    assertTrue(magick.getImageMagickExecutable2().canExecute());
    assertTrue(magick.isAvailable());
  }

  @Test
  public void testBadPath() {
    ImageMagickSizer magick = new ImageMagickSizer();
    assertFalse(magick.getImageMagickExecutable().canExecute());
    assertFalse(magick.getImageMagickExecutable2().canExecute());
    assertFalse(magick.isAvailable());
  }

  //    @Test
  public void ioTestHappyPathNoExecConfig() throws IOException {
    ImageSizer magick = new ImageMagickSizer();
    Map<String, String> configuration = new HashMap<>();
    configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, TEST_PATH_TO_MAGICK_EXEC);
    configuration.put(OUTPUT_FORMAT, JPEG);
    configuration.put(INPUT_IMAGE_PATH, data.vanillaJpegUrl.getFile());
    magick.setConfiguration(configuration);
    assertTrue("Could not find the Image Magick Executable", magick.isAvailable());
    magick.setOutputSize(256);
    BufferedImage output = magick.size();
    assertEquals(output.getWidth(), 256);
  }

  @Test(expected = RuntimeException.class)
  public void ioTestWithBadExecConfig() throws IOException {
    ImageSizer magick = new ImageMagickSizer();
    Map<String, String> configuration = new HashMap<>();
    configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, TEST_PATH_TO_MAGICK_EXEC);
    configuration.put(EXEC_NAME, "notvalid");
    magick.setConfiguration(configuration);
    magick.size();
  }

  @Test
  public void testSupportedFormats() {
    assertTrue("ImageMacgic should support JPEG", new ImageMagickSizer().recommendedFor(JPEG));
  }
}
