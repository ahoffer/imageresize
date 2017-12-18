package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.DECODE_JPEG2000;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import javax.imageio.spi.IIORegistry;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

public class IoTest {

  public static final String TEST_PATH_TO_MAGICK_EXEC =
      FilenameUtils.getFullPath("/opt/local/bin/");

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  @Test
  public void testBasicSizer() throws IOException {
    belittleIt(new BasicSizer().setInput(getData().vanillaJpeg_128x80_Stream));
  }

  // NOTE: Switched to getting a reader and letting the sizer manager it manage it.
  // J2K reader tries to read the ENTIRE image to get the header. A simple mark/reset will not
  // work unless the image file is actually mark than the read limit.
  //  @Test(expected = StreamResetException.class)
  @Test
  public void testSamplingSizerWithJP2() throws IOException {
    //    belittleIt(new SamplingSizer().setInput(data.jpeg2000_513x341_Stream));
    belittleIt(new SamplingSizer().setInput(getData().jpeg2000_128x80_Stream));
  }

  @Test
  public void testSamplingSizer() throws IOException {
    belittleIt(new SamplingSizer().setInput(getData().vanillaJpeg_300x200_Stream));
  }

  @Test
  public void testJpeg2000ResolutionSizer() throws IOException {
    belittleIt(new JaiJpeg2000Sizer().setInput(getData().jpeg2000_513x341_Stream));
  }

  // @Test
  public void testWrongImageTypeForJpeg2000Sizer() throws IOException {
    ImageSizer sizer =
        new JaiJpeg2000Sizer()
            .setInput(getData().vanillaJpeg_128x80_Stream)
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
    configuration.put(AbstractImageSizer.PATH_TO_EXECUTABLE, TEST_PATH_TO_MAGICK_EXEC);
    sizer.setConfiguration(configuration);
    sizer
        .setOutputSize(TestData.PIXELS, TestData.PIXELS)
        .setInput(getData().vanillaJpeg_128x80_Stream);

    assertThat(sizer.isAvailable(), equalTo(true));
    belittleIt(sizer);
  }

  @Test
  public void testOpenJpeg2000Sizer() throws IOException {
    ImageSizer sizer = new OpenJpeg2000Sizer();
    HashMap<String, String> configuration = new HashMap<>();
    configuration.put(
        AbstractImageSizer.PATH_TO_EXECUTABLE,
        "/Users/aaronhoffer/bin/openjpeg-v2.3.0-osx-x86_64/bin/");
    sizer.setInput(getData().jpeg2000_513x341_Stream).setConfiguration(configuration);
    belittleIt(sizer);
  }

  void belittleIt(ImageSizer sizer) {
    sizer.setOutputSize(TestData.PIXELS, TestData.PIXELS).setTimeoutSeconds(90);
    BeLittlingResult result = sizer.generate();
    //    System.err.println("\nResult\n" + result);
    BufferedImage output = result.getOutput().get();
    assertThat(output.getWidth(), equalTo(TestData.PIXELS));
    assertThat(output.getHeight(), org.hamcrest.Matchers.lessThanOrEqualTo(TestData.PIXELS));
  }

  @Test
  public void testJpeg2000MetadataReader() throws IOException {
    Jpeg2000MetadataMicroReader smaller =
        new Jpeg2000MetadataMicroReader(getData().jpeg2000_128x80_Stream);
    smaller.read();
    assertThat(smaller.getWidth(), is(128));
    assertThat(smaller.getHeight(), is(80));
    assertThat(smaller.minNumResolutionLevels, is(5));
    Jpeg2000MetadataMicroReader larger =
        new Jpeg2000MetadataMicroReader(getData().jpeg2000_513x341_Stream);
    larger.read();
    assertThat(larger.getWidth(), is(513));
    assertThat(larger.getHeight(), is(341));
    assertThat(smaller.minNumResolutionLevels, is(5));
  }

  @Test
  public void testReader() throws IOException {
    SafeImageReader reader = new SafeImageReader(getData().vanillaJpeg_128x80_Stream);
    Optional<Integer> a = reader.getWidth();
    Optional<Integer> b = reader.getHeight();
    Optional<BufferedImage> c = reader.read();
    assertThat("a", a.isPresent(), is(true));
    assertThat("b", b.isPresent(), is(true));
    assertThat("c", c.isPresent(), is(true));
    reader.read();
    reader.dispose();
  }

  TestData getData() throws IOException {
    return new TestData();
  }
}
