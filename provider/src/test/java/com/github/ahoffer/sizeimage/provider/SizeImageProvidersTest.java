package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import java.io.IOException;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class SizeImageProvidersTest {

  SizeImageTestData data;

  @Before
  public void setup() {
    data = new SizeImageTestData();
  }

  @Test
  public void verifyJpeg2000ReaderIsRegistered() {
    assertTrue(true);
  }

  @Test
  public void ioVerifyTestResources() throws IOException {

    String actualFormatName1 = ImageReaderUtils.getReader(data.vanillaJpegStream).getFormatName();
    assertEquals("Unexpected image format name", SizeImageTestData.JPEG, actualFormatName1);
    String actualFormatName2 = ImageReaderUtils.getReader(data.jpeg2000Stream).getFormatName();
    assertEquals("Unexpected image format name", SizeImageTestData.JPEG_2000, actualFormatName2);
  }

  @Test
  public void ioTestInputStreamSafety() throws IOException {
    // Use stream once
    ImageReaderUtils.getFormat(data.vanillaJpegStream);

    // Make sure stream can be used again
    ImageSizer sizer = new SamplingImageSizer();
    sizer.setInput(data.vanillaJpegStream).setOutputSize(250).size();
  }
}
