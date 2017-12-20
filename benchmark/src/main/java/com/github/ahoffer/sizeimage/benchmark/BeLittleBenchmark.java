package com.github.ahoffer.sizeimage.benchmark;

import static com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity.INFO;
import static com.github.ahoffer.sizeimage.provider.AbstractImageSizer.PATH_TO_EXECUTABLE;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.BasicSizer;
import com.github.ahoffer.sizeimage.provider.BeLittlingMessageImpl;
import com.github.ahoffer.sizeimage.provider.JaiJpeg2000Sizer;
import com.github.ahoffer.sizeimage.provider.LittleWorker;
import com.github.ahoffer.sizeimage.provider.MagickSizer;
import com.github.ahoffer.sizeimage.provider.OpenJpeg2000Sizer;
import com.github.ahoffer.sizeimage.provider.SamplingSizer;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.NaiveHeapSizeProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

// Website with large ortho images: https://apollomapping.com/
// Also: The Land Management Information Center, MN Planning
// https://earthexplorer.usgs.gov/
// https://glovis.usgs.gov/

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BeLittleBenchmark {

  public static final int TIMEOUT_SECONDS = 300;
  String inputDir;
  String magickPath;
  String outputDir;
  String opjPath;
  BufferedImage lastThumbnail;
  String lastDescription;

  //  static String inputDir = "/Users/aaronhoffer/data/jpeg2000-compliance/";
  //  static String inputDir = "/Users/aaronhoffer/data/sample-images/";

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  @Param({"128"})
  public int thumbSize;
  // LARGE FILES ( > 1 MB)
  // Mixed
  @Param({
    "land-8mb.jpg",
    "building-30mb.jpg",
    "gettysburg-6mb.jp2",
    "airplane-4mb.jp2",
    "building-30mb.jpg",
    "britain-108mb.jpg",
    "paris-201mb.tiff",
    "oslo-j2k-19mb.jp2",
    "ortho-744mb.jp2",
    "seattle-300mb.tif",
    "salt-lake-1gb.png"
  })

  // LARGE FILES ( > 1 MB)
  // JPEG 2000
  //  @Param({
  //    "carrots-j2k-8mb.j2k",
  //    "gettysburg-6mb.jp2",
  //    "oslo-j2k-19mb.jp2",
  //    "airplane-4mb.jp2",
  //    "ortho-744mb.jp2",
  //    "old-map-60mb.jp2"
  //  })

  // HUGE FILES > 100 MB
  // JPEG, JPEG 2000, PNG
  //  @Param({
  //    "australia-250mb.png",
  //    "salt-lake-340mb.jpg",
  //    "salt-lake-1gb.png",
  //    "britain-108mb.jpg",
  //    "seattle-300mb.tif"
  //  //        "mars-crater-456mb.JP2" // NEVER FINISHES. VERY MESSED FILE!
  //  })

  //   COMPLIANCE TESTS
  //  @Param({
  //    "p1_04.j2k",
  //    "file9.jp2",
  //    "file8.jp2",
  //    "p1_05.j2k",
  //    "p1_07.j2k",
  //    "p1_06.j2k",
  //    "p1_02.j2k",
  //    "p1_03.j2k",
  //    "p1_01.j2k",
  //    "p0_09.j2k",
  //    "p0_08.j2k",
  //    "p0_06.j2k",
  //    "p0_12.j2k",
  //    "p0_13.j2k",
  //    "p0_07.j2k",
  //    "p0_11.j2k",
  //    "p0_05.j2k",
  //    "p0_04.j2k",
  //    "p0_10.j2k",
  //    "p0_14.j2k",
  //    "p0_01.j2k",
  //    "p0_15.j2k",
  //    "p0_03.j2k",
  //    "p0_16.j2k",
  //    "p0_02.j2k",
  //    "file1.jp2",
  //    "file3.jp2",
  //    "file2.jp2",
  //    "file6.jp2",
  //    "file7.jp2",
  //    "file5.jp2",
  //    "file4.jp2"
  //  })

  //  @Param({"p1_05.j2k"}) //Evil file that causes the JAI JPEG 2000 to process indefinitely
  // without using up the heap
  String filename;
  //  })
  //    "salt-lake-340mb.jpg"
  //    "australia-250mb.png",
  //    "building-30mb.jpg",
  //    "mountains-20mb.jpg",
  //    "city-and-land-15mb.jpg",
  //    "land-8mb.jpg",
  //    "crowd-3mb.jpg",
  //  @Param({
  // LARGE FILES ( > 1 MB)
  // Vanilla JPEG FILES

  //  "mars-crater-456mb.JP2"
  //    "salm-1gb.jp2",
  //    "salt-lake-340mb.jpg",
  //    "australia-250mb.png",
  // HUGE FILES > 100 MB

  //  String filename;
  //  @Param({"tank.jpg"})
  //   SINGLE FILE

  //  String filename;
  //  })
  //    "palace.j2k"
  //    "parliament-60kb.jpg",
  //    "city-300kb.jpg",
  //    "land-100kb.jpg",
  //    "unicorn-rainbow-57kb.gif",
  //  @Param({
  // SMALL FILES ( < 1 MB)

  // TODO:Could have a benchmark that just copies input stream to get a sense of IO overhead
  public static void main(String[] args) throws RunnerException, IOException {

    //    String names = getFilenames(inputDir);

    String simpleName = BeLittleBenchmark.class.getSimpleName();
    Options opt =
        new OptionsBuilder()
            .forks(1)
            .warmupIterations(1)
            .measurementIterations(4)
            .include(simpleName)
            .resultFormat(ResultFormatType.NORMALIZED_CSV)
            .addProfiler(NaiveHeapSizeProfiler.class)
            .addProfiler(GCProfiler.class)
            .result("results.csv")
            .timeout(TimeValue.seconds(40))
            .build();
    new Runner(opt).run();
  }

  @Setup
  public void setup() {

    inputDir = System.getProperty("inputDir", "./sample-images/");
    outputDir = inputDir + "output/";
    opjPath =
        System.getProperty("opjPath", "/Users/aaronhoffer/bin/openjpeg-v2.3.0-osx-x86_64/bin/");
    magickPath = System.getProperty("magickPath", "/opt/local/bin");
  }

  @SuppressWarnings("unused")
  public static String getFilenames(String dir) throws IOException {
    return Files.list(Paths.get(dir))
        .map(Path::toFile)
        .filter(File::isFile)
        .filter(file -> !file.getName().equals(".DS_Store"))
        .map(File::getName)
        .map(s -> String.format("\"%s\"", s))
        .collect(Collectors.joining(","));
  }

  //  @Benchmark
  public void basicSizer() throws IOException {
    runBenchmark(new BasicSizer());
  }

  @Benchmark
  public void jaiJpeg2000Sizer() throws IOException {
    // This sizer works ONLY with JPEG 2000 images. Filter out other image types.
    if (isJpeg2000(getSoureceFile())) {
      ImageSizer sizer = new JaiJpeg2000Sizer();
      runBenchmark(sizer);
    } else {
      throw new RuntimeException("JPEG 2000 images only");
    }
  }

  @Benchmark
  public void magickSizer() throws IOException {
    lastDescription = "magickSizer";
    ImageSizer sizer = new MagickSizer();
    Map<String, String> config = new HashMap<>();
    config.put(PATH_TO_EXECUTABLE, magickPath);
    sizer.setConfiguration(config);
    runBenchmark(sizer);
  }

  @Benchmark
  public void openJeg2000Sizer() throws IOException {

    ImageSizer sizer = new OpenJpeg2000Sizer();
    HashMap<String, String> configuration = new HashMap<>();
    configuration.put(PATH_TO_EXECUTABLE, opjPath);
    sizer.setConfiguration(configuration);

    // This sizer works ONLY with JPEG 2000 images. Filter out other image types.
    if (isJpeg2000(getSoureceFile())) {
      runBenchmark(sizer);
    } else {
      throw new RuntimeException("JPEG 2000 images only");
    }
  }

  @Benchmark
  public void samplingSizer() throws IOException {
    runBenchmark(new SamplingSizer());
  }

  @Benchmark
  public void scalr() throws Exception {
    lastDescription = "scalr";
    BufferedInputStream source = getSourceStream();
    try (LittleWorker worker = getWorker()) {
      worker.doThis(
          () -> {
            lastThumbnail = Scalr.resize(ImageIO.read(source), thumbSize);
            return null;
          });
    }
  }

  @Benchmark
  public void thumbnailator() throws Exception {
    lastDescription = "thumbnailator";
    try (LittleWorker worker = getWorker()) {
      worker.doThis(
          () -> {
            lastThumbnail =
                Thumbnails.of(getSourceStream())
                    .height(thumbSize)
                    .width(thumbSize)
                    .asBufferedImage();
            return null;
          });
    }
  }

  @TearDown
  public void teardown() throws IOException {
    saveThumbnailToOutputDir();
    lastDescription = null;
    lastThumbnail = null;
  }

  void saveThumbnailToOutputDir() throws IOException {
    String ext = FilenameUtils.getExtension(filename);
    String nameWithoutExt = filename.replaceAll("." + ext, "");
    if (lastThumbnail != null) {
      File file =
          new File(String.format("%s%s-%s.%s", outputDir, nameWithoutExt, lastDescription, ext));
      file.mkdirs();
      ImageIO.write(lastThumbnail, "png", file);
    }
  }

  @Benchmark
  public void scalrTikaTransformer() throws IOException {
    lastDescription = "scalrTikaTransformer";
    Image input = ImageIO.read(getSourceStream());
    BufferedImage output;
    try {
      output =
          new BufferedImage(
              input.getWidth(null), input.getHeight(null), BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = output.createGraphics();
      graphics.drawImage(input, null, null);
      graphics.dispose();
      lastThumbnail = Scalr.resize(output, thumbSize);
    } catch (NullPointerException e) {
      String msg = "Failed to read " + getSoureceFile().getName();
      throw new RuntimeException(msg);
    }
  }

  BufferedInputStream getSourceStream() throws FileNotFoundException {
    return new BufferedInputStream(new FileInputStream(getSoureceFile()));
  }

  boolean isJpeg2000(File file) {
    String ext = FilenameUtils.getExtension(file.getName());
    return "jp2".equalsIgnoreCase(ext) || "j2k".equalsIgnoreCase(ext);
  }

  void runBenchmark(ImageSizer sizer) throws FileNotFoundException {
    lastDescription = sizer.getClass().getSimpleName();
    String fileName = getSoureceFile().getName();
    sizer
        .setOutputSize(thumbSize, thumbSize)
        .setInput(getSourceStream())
        .addMessage(new BeLittlingMessageImpl("FILE", INFO, fileName))
        .setTimeoutSeconds(TIMEOUT_SECONDS);
    BeLittlingResult result = null;
    try {
      result = sizer.generate();
      if (result.getOutput().isPresent()) {
        lastThumbnail = result.getOutput().get();
      }
    } catch (Exception e) {
      // Write out messages something even if heap overflow or other fault
      System.err.println(String.format("\nSIZER, %s\nFILE, %s", lastDescription, fileName));
      throw e;
    }
    System.err.println(System.lineSeparator() + result);
  }

  File getSoureceFile() {
    return new File(inputDir + filename);
  }

  @SuppressWarnings("unused")
  // Example of fast duplication of a file, or fast copy
  void copyFileUsingFileChannels(File source, File dest) throws IOException {
    try (FileChannel inputChannel = new FileInputStream(source).getChannel();
        FileChannel outputChannel = new FileOutputStream(dest).getChannel()) {
      outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
    }
  }

  LittleWorker getWorker() {
    return new LittleWorker(TIMEOUT_SECONDS, TimeUnit.SECONDS);
  }
}
