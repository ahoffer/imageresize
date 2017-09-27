package com.github.ahoffer.sizeimage.test;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.ImageSizerFactory;
import static com.github.ahoffer.sizeimage.provider.ImageMagickSizer.OUTPUT_FORMAT;
import static com.github.ahoffer.sizeimage.provider.ImageMagickSizer.PATH_TO_IMAGE_MAGICK_EXECUTABLES;
import com.github.ahoffer.sizeimage.provider.SamplingImageSizer;
import com.google.common.collect.Ordering;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import static junit.framework.TestCase.assertEquals;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Assert;
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
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class TestContainer {

  private static final String INPUT_DIR = "/Users/aaronhoffer/data/small-image-set/";
  private static String OUTPUTDIR = INPUT_DIR + "output/";

  private static Logger LOGGER = LoggerFactory.getLogger(TestContainer.class);

  @Inject protected ImageSizerFactory factory;
  Map<String, String> configuration;
  List<File> inputFiles = new ArrayList<>();

  public static String karafVersion() {
    // return new ConfigurationManager().getProperty("pax.exam.karaf.version");
    return "4.1.2";
  }

  @Before
  public void setup() throws IOException {
    configuration = new HashMap<>();
    configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, "/opt/local/bin/");
    configuration.put(OUTPUT_FORMAT, "png");
    inputFiles =
        Files.list(Paths.get(INPUT_DIR))
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(file -> !file.getName().equals(".DS_Store"))
            .collect(Collectors.toList());
  }

  @Test
  public void testGetRecommendedForJpeg() {
    String serviceName = factory.getRecommendedServiceName("jpeg");
    assertThat("Expected different service name", serviceName, equalTo("sampling"));

    ImageSizer sizer = factory.getRecommendedService("jpeg");
    assertThat("Expected the sampling resizer", sizer, instanceOf(SamplingImageSizer.class));
  }

  @Test
  public void testGetServiceReferences() {
    List<ServiceReference<ImageSizer>> list = factory.getServiceReferences(null);
    Assert.assertThat(
        "Not sorted in order of highest to lowest service ranking",
        Ordering.natural().reverse().isOrdered(list),
        CoreMatchers.is(true));
    assertEquals(list.size(), 3);
  }

  @Test
  public void testSamplingSizer() throws IOException {
    runSizerForEveryImage("sampling");
  }

  @Test
  public void testBasicSizer() throws IOException {
    runSizerForEveryImage("basic");
  }

  @Test
  public void testMagickSizer() throws IOException {
    runSizerForEveryImage("magick");
  }

  void runSizerForEveryImage(String name) throws IOException {
    Optional<ImageSizer> optional = factory.getService(name);
    assertThat("Image sizer should be present", optional.isPresent(), is(true));
    ImageSizer imageSizer = optional.get();
    imageSizer.setConfiguration(configuration);
    assertThat("Image sizer should be available", imageSizer.isAvailable(), is(true));
    assertThat(
        "Factory should return a new instance",
        imageSizer,
        not(sameInstance(factory.getService(name).get())));
    for (File inputFile : inputFiles) {
      runSizer(imageSizer, inputFile);
    }
  }

  void runSizer(ImageSizer sizer, File input) throws IOException {

    InputStream inputStream = new BufferedInputStream(new FileInputStream(input));
    final long start = System.nanoTime();
    LOGGER.info(
        String.format(
            "Starting file %s of size %.2f MB...", input.getName(), input.length() / 1e6));
    sizer.setConfiguration(configuration);
    String sizerName = sizer.getClass().getSimpleName();
    LOGGER.info(String.format("Selected %s", sizerName));

    BufferedImage output = sizer.setOutputSize(256).setInput(inputStream).size();
    final long stop = System.nanoTime();
    java.io.File outputDirObject = new File(OUTPUTDIR);
    outputDirObject.mkdirs();
    java.io.File outputFile = new File(outputDirObject, sizerName + "-" + input.getName() + ".png");
    ImageIO.write(output, "png", outputFile);
    LOGGER.info(
        String.format(
            "...Created %s thumbnail in %.2f s", input.getName(), (stop - start) / 1.0e9));
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
      mavenBundle("com.github.ahoffer", "sizeimage-bundle").versionAsInProject().start(),
      vmOption("-Xmx4g"),
      // Avoid focus on OS X
      vmOption("-Djava.awt.headless=true"),
      vmOption("-Dfile.encoding=UTF8")
    };
  }
}
