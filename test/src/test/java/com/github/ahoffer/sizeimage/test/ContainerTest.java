package com.github.ahoffer.sizeimage.test;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.BasicSizer;
import com.github.ahoffer.sizeimage.provider.BeLittle;
import com.github.ahoffer.sizeimage.provider.MagickSizer;
import com.github.ahoffer.sizeimage.provider.SamplingSizer;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.inject.Inject;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.MatcherAssert;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class ContainerTest {

  static final String ARTIFACT_ID = "bundle";
  static final String GROUP_ID = "com.github.ahoffer";
  static final String INPUT_DIR = "/Users/aaronhoffer/data/small-image-set/";
  static String OUTPUTDIR = INPUT_DIR + "output/";
  static Logger LOGGER = LoggerFactory.getLogger(ContainerTest.class);

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  @Inject protected BeLittle factory;
  List<File> inputFiles = new ArrayList<>();

  public static String karafVersion() {
    // return new ConfigurationManager().getProperty("pax.exam.karaf.version");
    return "4.1.2";
  }

  @Before
  public void setup() throws IOException {
    inputFiles =
        Files.list(Paths.get(INPUT_DIR))
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(file -> !file.getName().equals(".DS_Store"))
            .collect(Collectors.toList());
  }

  //  @Test
  public void runAllDefaultSizers() throws IOException {
    List<ImageSizer> imageSizers = factory.getSizersFor((String) null, false, true);
    for (ImageSizer imageSizer : imageSizers) {
      runSizerForEveryImage(imageSizer);
    }
  }

  @Test
  public void testGetSizersByJpegStream() throws ClassNotFoundException {
    InputStream vanillaJpegStream = getResourceAsStream("/sample-jpeg.jpg");
    List<ImageSizer> list = factory.getSizerFor(vanillaJpegStream);
    assertThat("Expect 3 image sizers", list.size(), equalTo(3));
    assertThat(
        "Expected first image sizer to be sampler",
        list.get(0),
        instanceOf(SamplingSizer.class));
    assertThat(
        "Expected second image sizer to be magick", list.get(1), instanceOf(MagickSizer.class));
    assertThat(
        "Expected third image sizer to be basic", list.get(2), instanceOf(BasicSizer.class));
  }

  @Test
  public void testGetSizersByJp2Stream() throws ClassNotFoundException {
    InputStream vanillaJpegStream = getResourceAsStream("/sample-jpeg2000.jpg");
    List<ImageSizer> list = factory.getSizerFor(vanillaJpegStream);
    assertThat("Expect 3 image sizers", list.size(), equalTo(3));
    assertThat(
        "Expected first image sizer to be magick", list.get(0), instanceOf(MagickSizer.class));
    assertThat(
        "Expected second image sizer to be sampling",
        list.get(1),
        instanceOf(SamplingSizer.class));
    assertThat(
        "Expected third image sizer to be basic", list.get(2), instanceOf(BasicSizer.class));
  }

  @Test
  public void testDefaultExtents() {
    int int1 = factory.getMaxWidth();
    int int2 = factory.getMaxHeight();
    factory.setMaxWidth(1);
    factory.setMaxHeight(2);
    Optional<ImageSizer> optionalSizer = factory.getSizerFor((String) null);
    MatcherAssert.assertThat(optionalSizer.isPresent(), is(true));
    MatcherAssert.assertThat(optionalSizer.get().getMaxWidth(), is(1));
    MatcherAssert.assertThat(optionalSizer.get().getMaxHeight(), is(2));
    factory.setMaxWidth(int1);
    factory.setMaxHeight(int2);
  }

  @Test
  public void testConvenienceMethod() {
    Optional<BufferedImage> output = factory.size(getResourceAsStream("/sample-jpeg.jpg"));
    assertThat(output.isPresent(), is(true));
    assertThat(output.get().getWidth(), equalTo(factory.getMaxWidth()));
  }

  private InputStream getResourceAsStream(String filename) {
    InputStream unresetableStream = getClass().getResourceAsStream(filename);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      org.apache.commons.io.IOUtils.copy(unresetableStream, outputStream);
    } catch (IOException e) {
      fail("Could not get test resource");
    }
    return new ByteArrayInputStream(outputStream.toByteArray());
  }

  @org.ops4j.pax.exam.Configuration
  public Option[] config() {
    MavenArtifactUrlReference karafUrl =
        CoreOptions.maven()
            .groupId("org.apache.karaf")
            .artifactId("apache-karaf")
            .version(karafVersion())
            .type("zip");

    MavenUrlReference karafStandardRepo =
        CoreOptions.maven()
            .groupId("org.apache.karaf.features")
            .artifactId("standard")
            .version(karafVersion())
            .classifier("features")
            .type("xml");

    return new Option[] {
      when(Boolean.getBoolean("isDebugEnabled"))
          .useOptions(KarafDistributionOption.debugConfiguration("8000", true)),
      KarafDistributionOption.karafDistributionConfiguration()
          .frameworkUrl(karafUrl)
          .unpackDirectory(new File("target", "exam"))
          .useDeployFolder(false),
      KarafDistributionOption.keepRuntimeFolder(),
      KarafDistributionOption.configureConsole().ignoreLocalConsole(),
      KarafDistributionOption.configureConsole().ignoreRemoteShell(),
      KarafDistributionOption.features(karafStandardRepo, "scr"),
      KarafDistributionOption.logLevel(LogLevelOption.LogLevel.INFO),
      mavenBundle("com.google.guava", "guava").versionAsInProject().start(),
      mavenBundle("commons-io", "commons-io").versionAsInProject(),
      mavenBundle(GROUP_ID, ARTIFACT_ID).versionAsInProject().start(),
      vmOption("-Xmx4g"),
      // Avoid focus on OS X
      vmOption("-Djava.awt.headless=true"),
      vmOption("-Dfile.encoding=UTF8")
    };
  }

  void runSizer(ImageSizer sizer, File input) throws IOException {

    InputStream inputStream = new BufferedInputStream(new FileInputStream(input));
    final long start = System.nanoTime();
    LOGGER.info(
        String.format(
            "Starting file %s of generate %.2f MB...", input.getName(), input.length() / 1e6));
    String sizerName = sizer.getClass().getSimpleName();
    LOGGER.info(String.format("\tSelected %s", sizerName));

    BufferedImage output = sizer.setOutputSize(128, 128).setInput(inputStream).generate();
    final long stop = System.nanoTime();
    java.io.File outputDirObject = new File(OUTPUTDIR);
    outputDirObject.mkdirs();
    java.io.File outputFile = new File(outputDirObject, sizerName + "-" + input.getName() + ".png");
    ImageIO.write(output, "png", outputFile);
    LOGGER.info(
        String.format("\tCreated %s thumbnail in %.2f s", input.getName(), (stop - start) / 1.0e9));
  }

  void runSizerForEveryImage(ImageSizer imageSizer) throws IOException {
    for (File inputFile : inputFiles) {
      runSizer(imageSizer, inputFile);
    }
  }
}
