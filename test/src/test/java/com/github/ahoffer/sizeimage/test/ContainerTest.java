package com.github.ahoffer.sizeimage.test;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.ImageSizerFactory;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.inject.Inject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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

    static {
        IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
    }

  static final String ARTIFACT_ID = "sizeimage-bundle";
  static final String GROUP_ID = "com.github.ahoffer";
  static final String INPUT_DIR = "/Users/aaronhoffer/data/small-image-set/";
  static String OUTPUTDIR = INPUT_DIR + "output/";

  static Logger LOGGER = LoggerFactory.getLogger(ContainerTest.class);

  @Inject protected ImageSizerFactory factory;
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

  @Test
  public void runAllDefaultSizers() throws IOException {
    List<ImageSizer> imageSizers = factory.getRecommendedSizers((String) null, false);
    assertThat("Expect 3 image sizers", imageSizers.size(), equalTo(3));
    for (ImageSizer imageSizer : imageSizers) {
      assertThat("Image sizer should be available", imageSizer.isAvailable(), is(true));
      runSizerForEveryImage(imageSizer);
    }
  }

  @Test
  public void testGetSizersByImageStream() throws ClassNotFoundException {
    InputStream vanillaJpegStream = getClass().getResourceAsStream("/sample-jpeg.jpg");
    List<ImageSizer> list = factory.getRecommendedSizers(vanillaJpegStream);
    // TODO Add assertion.
  }

  void runSizerForEveryImage(ImageSizer imageSizer) throws IOException {
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
    String sizerName = sizer.getClass().getSimpleName();
    LOGGER.info(String.format("\tSelected %s", sizerName));

    BufferedImage output = sizer.setOutputSize(128, 128).setInput(inputStream).size();
    final long stop = System.nanoTime();
    java.io.File outputDirObject = new File(OUTPUTDIR);
    outputDirObject.mkdirs();
    java.io.File outputFile = new File(outputDirObject, sizerName + "-" + input.getName() + ".png");
    ImageIO.write(output, "png", outputFile);
    LOGGER.info(
        String.format("\tCreated %s thumbnail in %.2f s", input.getName(), (stop - start) / 1.0e9));
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
      mavenBundle(GROUP_ID, ARTIFACT_ID).versionAsInProject().start(),
      vmOption("-Xmx4g"),
      // Avoid focus on OS X
      vmOption("-Djava.awt.headless=true"),
      vmOption("-Dfile.encoding=UTF8")
    };
  }
}
