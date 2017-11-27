package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.DECODE_JPEG2000;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertThat;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.BeLittle.StreamResetException;
import com.github.jaiimageio.jpeg2000.J2KImageReadParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IoTest {

  public static final String TEST_PATH_TO_MAGICK_EXEC =
      FilenameUtils.getFullPath("/opt/local/bin/");

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  ImageReaderShortcuts shortcuts = new ImageReaderShortcuts();
  TestData data;

  @Before
  public void setup() throws IOException {
    data = new TestData();
  }

  @Test
  public void testBasicSizer() throws IOException {
    doSize(
        new BasicSizer()
            .setInput(data.vanillaJpeg_128x80Stream)
            .setOutputSize(TestData.PIXELS, TestData.PIXELS));
  }

  @Test
  public void testSamplingSizer() throws Exception {
    doSize(new SamplingSizer());
  }

  @Test
  public void testJpeg2000ResolutionSizer() throws IOException {
    doSize(new JaiJpeg2000Sizer());
  }

  @Test
  public void testWrongImageTypeForJpeg2000Sizer() throws IOException {
    ImageSizer sizer =
        new JaiJpeg2000Sizer()
            .setInput(data.vanillaJpeg_128x80Stream)
            .setOutputSize(TestData.PIXELS, TestData.PIXELS);
    BeLittlingResult result = sizer.generate();
    assertThat(result.getOutput().isPresent(), is(false));
    assertThat(
        result.getMessages().stream().anyMatch(m -> DECODE_JPEG2000.equals(m.getId())), is(true));
  }

  @Test
  public void testMagickSizer() throws IOException {
    ImageSizer sizer = new MagickSizer();
    HashMap configuration = new HashMap();
    configuration.put(AbstractImageSizer.PATH_TO_EXECUTABLE_KEY, TEST_PATH_TO_MAGICK_EXEC);
    sizer.setConfiguration(configuration);
    sizer.setOutputSize(TestData.PIXELS, TestData.PIXELS).setInput(data.vanillaJpeg_128x80Stream);

    assertThat(sizer.isAvailable(), equalTo(true));
    doSize(sizer);
  }

  @Test
  public void testOpenJpeg2000Sizer() throws Exception {
    ImageSizer sizer = new OpenJpeg2000Sizer();
    HashMap<String, String> configuration = new HashMap<>();
    configuration.put(
        AbstractImageSizer.PATH_TO_EXECUTABLE_KEY,
        "/Users/aaronhoffer/bin/openjpeg-v2.3.0-osx-x86_64/bin/");
    sizer.setInput(data.jpeg2000_513x341Stream).setConfiguration(configuration);
    doSize(sizer);
  }

  private void doSize(ImageSizer sizer) {
    sizer.setOutputSize(TestData.PIXELS, TestData.PIXELS);
    BeLittlingResult result = sizer.generate();
    BufferedImage output = result.getOutput().get();
    assertThat(output.getWidth(), equalTo(TestData.PIXELS));
    assertThat(output.getHeight(), org.hamcrest.Matchers.lessThanOrEqualTo(TestData.PIXELS));
  }

  @Test
  public void testGetMimeTypes() throws IOException {
    List<String> mimeTypes = shortcuts.getMimeTypes(data.vanillaJpeg_128x80Stream);
    assertThat(mimeTypes, hasItem(equalToIgnoringCase("image/jpeg")));

    mimeTypes = shortcuts.getMimeTypes(data.jpeg2000_128x80Stream);
    assertThat(mimeTypes, containsInAnyOrder("image/jp2", "image/jpeg2000"));
  }

  @Test
  public void testPreservingInputStream() throws StreamResetException {
    // Use the same stream twice
    InputStream inputStream = data.vanillaJpeg_128x80Stream;
    List<String> mimeTypesFirstTime = shortcuts.getMimeTypes(inputStream);
    List<String> mimeTypesSecondTime = shortcuts.getMimeTypes(inputStream);
    assertThat(mimeTypesFirstTime, equalTo(mimeTypesSecondTime));
  }

  @Test
  public void testImageIO() throws IOException {
    ImageInputStream imgStream = ImageIO.createImageInputStream(data.vanillaJpeg_128x80Stream);
    ImageIO.getImageReaders(imgStream);
    Iterator<ImageReader> readers = ImageIO.getImageReaders(imgStream);
    ImageReader reader = readers.next();
    reader.setInput(imgStream);
    BufferedImage image = reader.read(0);
    int height = image.getHeight();
    BufferedImage image2 = reader.read(0);
  }

  @Ignore
  @Test
  public void experiment() throws IOException {
    ImageInputStream input =
        ImageIO.createImageInputStream(
            new File("/Users/aaronhoffer/data/sample-images/gettysburg.jp2"));
    ImageReader reader = new J2KImageReaderSpi().createReaderInstance(null);
    reader.setInput(input);
    J2KImageReadParam param = (J2KImageReadParam) reader.getDefaultReadParam();
    param.setResolution(10);
    BufferedImage image = reader.read(0, param);
    image.getHeight();
  }
}