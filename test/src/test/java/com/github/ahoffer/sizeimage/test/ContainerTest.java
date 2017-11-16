package com.github.ahoffer.sizeimage.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.CoreOptions.when;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.BasicSizer;
import com.github.ahoffer.sizeimage.provider.BeLittle;
import com.github.ahoffer.sizeimage.provider.BeLittle.ImageSizerCollection;
import com.github.ahoffer.sizeimage.provider.MagickSizer;
import com.github.ahoffer.sizeimage.provider.SamplingSizer;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.inject.Inject;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
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
  static String OUTPUTDIR = TestData.INPUT_DIR + "output/";
  static Logger LOGGER = LoggerFactory.getLogger(ContainerTest.class);

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  @Inject protected BeLittle belittler;

  TestData data;

  public static String karafVersion() {
    return "4.1.2";
  }

  @Before
  public void setup() throws IOException {
    data = new TestData();
  }

  @Test
  public void runAllAvailableSizers() throws IOException {
    ImageSizerCollection imageSizers = belittler.getSizersFor((String) null);
    for (ImageSizer imageSizer : imageSizers.getAvailable()) {
      runSizerForEveryImage(imageSizer);
    }
  }

  @Test
  public void testGetSizersByJpegStream() throws ClassNotFoundException {
    ImageSizerCollection sizers = belittler.getSizersFor(data.vanillaJpegStream);
    assertThat("Unexpected different number of unique sizers", sizers.getAll(), hasSize(3));
    assertThat("Unexpected number of unique, available sizers", sizers.getAvailable(), hasSize(3));
    assertThat(
        "Expected second image sizer to be magick",
        sizers.getRecommendations().get(1),
        instanceOf(MagickSizer.class));
    assertThat(
        "Expected third image sizer to be basic",
        sizers.getRecommendations().get(2),
        instanceOf(BasicSizer.class));
  }

  @Test
  public void testGetSizersByJp2Stream() throws ClassNotFoundException {
    ImageSizerCollection sizers = belittler.getSizersFor(data.jpeg2000Stream);
    assertThat("Expect 3 image sizers", sizers.getRecommendations(), hasSize(3));
    assertThat(
        "Expected first image sizer to be magick",
        sizers.getRecommendations().get(0),
        instanceOf(MagickSizer.class));
    assertThat(
        "Expected second image sizer to be sampling",
        sizers.getRecommendations().get(1),
        instanceOf(SamplingSizer.class));
    assertThat(
        "Expected third image sizer to be basic",
        sizers.getRecommendations().get(2),
        instanceOf(BasicSizer.class));
  }

  @Test
  public void testDefaultExtents() {
    int originalWidth = belittler.getMaxWidth();
    int originalHeight = belittler.getMaxHeight();
    int testWidth = 11;
    int testHeight = 12;
    belittler.setOutputSize(testWidth, testHeight);
    ImageSizerCollection sizers = belittler.getSizersFor("");
    MatcherAssert.assertThat(sizers.getRecommended().isPresent(), is(true));
    MatcherAssert.assertThat(sizers.getRecommended().get().getMaxWidth(), is(testWidth));
    MatcherAssert.assertThat(sizers.getRecommended().get().getMaxHeight(), is(testHeight));
    belittler.setOutputSize(originalWidth, originalHeight);
  }

  @Test
  public void testConvenienceMethod() {
    Optional<BufferedImage> output = belittler.generate(data.jpeg2000Stream);
    assertThat(output.isPresent(), is(true));
    assertThat(output.get().getWidth(), equalTo(belittler.getMaxWidth()));
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
      mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.hamcrest")
          .versionAsInProject(),
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
        String.format("\nStarting %s size %.2f MB...", input.getName(), input.length() / 1e6));
    String sizerName = sizer.getClass().getSimpleName();
    LOGGER.info(String.format("Selected %s", sizerName));

    BufferedImage output = sizer.setInput(inputStream).generate();
    final long stop = System.nanoTime();
    java.io.File outputDirObject = new File(OUTPUTDIR);
    outputDirObject.mkdirs();
    java.io.File outputFile = new File(outputDirObject, sizerName + "-" + input.getName() + ".png");
    ImageIO.write(output, "png", outputFile);
    LOGGER.info(String.format("Created thumbnail in %.2f s", (stop - start) / 1.0e9));
  }

  void runSizerForEveryImage(ImageSizer imageSizer) throws IOException {
    for (File inputFile : data.inputFiles) {
      runSizer(imageSizer, inputFile);
    }
  }
}
