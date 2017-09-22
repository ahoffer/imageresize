package com.github.ahoffer.sizeimage.it;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.bundle.ImageSizerFactory;
import static com.github.ahoffer.sizeimage.provider.ImageMagickSizer.OUTPUT_FORMAT;
import static com.github.ahoffer.sizeimage.provider.ImageMagickSizer.PATH_TO_IMAGE_MAGICK_EXECUTABLES;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
public class TestBundle {

    private static Logger LOGGER = LoggerFactory.getLogger(TestBundle.class);

    @Inject
    protected ImageSizerFactory imageSizerFactory;

    String inputDir = "/Users/aaronhoffer/data/small-image-set/";
    String outputDir = inputDir + "output/";

    Map<String, String> configuration;

    List<File> inputFiles = new ArrayList<>();

    public static String karafVersion() {
// return new ConfigurationManager().getProperty("pax.exam.karaf.version");
        return "4.1.2";
    }

    @Before
    public void setup() throws IOException {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, "/opt/local/bin/");
        configuration.put(OUTPUT_FORMAT, "png");

        inputFiles = Files.list(Paths.get(inputDir))
                .map(Path::toFile)
                .filter(File::isFile).filter(file -> !file.getName().equals(".DS_Store"))
                .collect(Collectors.toList());
    }

    @Test
    public void testHarness() {
        assertTrue(true);
    }

    @Test
    public void testGetProviders() {
        List<ImageSizer> sizers = imageSizerFactory.getImageSizers();
        assertEquals(sizers.size(), 3);
    }

    @Test
    public void testBasicSizer() {
        for (File inputFile : inputFiles) {
//           runSizer(sizer, inputFile);
        }
    }


    void runSizer(ImageSizer sizer, File input) throws IOException {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, "/opt/local/bin/");
        configuration.put(OUTPUT_FORMAT, "png");

        InputStream inputStream =
                new BufferedInputStream(new FileInputStream(input));
        final long start = System.nanoTime();
        LOGGER.info(String.format("\nStarting file %s of size %.2f MB...",
                input.getName(),
                input.length() / 1e6));
        sizer.setConfiguration(configuration);
        String sizerName = sizer.getClass()
                .getSimpleName();
        LOGGER.info(String.format("Selected %s", sizerName));

        BufferedImage output = sizer.setOutputSize(256)
                .setInput(inputStream)
                .size();
        final long stop = System.nanoTime();
        java.io.File outputDirObject = new File(outputDir);
        outputDirObject.mkdirs();
        java.io.File outputFile = new File(outputDirObject,
                sizerName + "-" + input.getName() + ".png");
        ImageIO.write(output, "png", outputFile);
        LOGGER.info(String.format("...Created %s thumbnail in %.2f s",
                input.getName(),
                (stop - start) / 1.0e9));
    }


    @org.ops4j.pax.exam.Configuration
    public Option[] config() {
        MavenArtifactUrlReference karafUrl = CoreOptions.maven()
                .groupId("org.apache.karaf")
                .artifactId("apache-karaf")
                .version(karafVersion())
                .type("zip");

        MavenUrlReference karafStandardRepo = CoreOptions.maven()
                .groupId("org.apache.karaf.features")
                .artifactId("standard")
                .version(karafVersion())
                .classifier("features")
                .type("xml");

        boolean waitForDebugger = Boolean.getBoolean("isDebugEnabled");
        return new Option[]{
                KarafDistributionOption.debugConfiguration("5005", waitForDebugger),
                KarafDistributionOption.karafDistributionConfiguration()
                        .frameworkUrl(karafUrl)
                        .unpackDirectory(new File("target", "exam"))
                        .useDeployFolder(false),
                KarafDistributionOption.keepRuntimeFolder(),
                KarafDistributionOption.configureConsole().ignoreLocalConsole(),
                KarafDistributionOption.configureConsole().ignoreRemoteShell(),
                KarafDistributionOption.features(karafStandardRepo, "scr"),
                CoreOptions.mavenBundle()
                        .groupId("com.github.ahoffer")
                        .artifactId("sizeimage-bundle")
                        .versionAsInProject().start(),
                CoreOptions.vmOption("-Xmx2g"),
                // avoid integration tests stealing focus on OS X
                CoreOptions.vmOption("-Djava.awt.headless=true"),
                CoreOptions.vmOption("-Dfile.encoding=UTF8"),
                CoreOptions.vmOption("-XX:+UnlockCommercialFeatures"),
                CoreOptions.vmOption("-XX:+FlightRecorder")
        };
    }
}
