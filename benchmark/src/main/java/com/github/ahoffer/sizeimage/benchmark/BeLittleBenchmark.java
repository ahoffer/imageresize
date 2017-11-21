package com.github.ahoffer.sizeimage.benchmark;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import net.coobird.thumbnailator.Thumbnails;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Stream2BufferedImage;
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

// Website with large ortho images: https://apollomapping.com/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BeLittleBenchmark {

  public static final String ABSOLUTE_PATH_TO_IMAGEMAGICK_CONVERT_EXECUTABLE =
      "/opt/local/bin/convert";

  @Param({"256"})
  public int thumbSize;

  String inputDir = "/Users/aaronhoffer/data/sample-images/";
  String outputDir = inputDir + "output/";
  BufferedImage lastThumbnail;
  String lastDescription;

  // ALL FILES
  //    @Param({"unicorn-rainbow-57kb.gif", "land-100kb.jpg", "parliament-60kb.jpg",
  // "city-300kb.jpg",
  //            "UN-bus-attack.jpg", "militants.jpg", "building-30mb.jpg", "land-8mb.jpg",
  //            "mountains-20mb.jpg", "crowd-3mb.jpg", "australia-250mb.png", "salt-lake-340mb.jpg",
  //            "baghdad-j2k-20mb.jp2", "carrots-j2k-8mb.j2k", "olso-j2k-19mb.jp2"})
  //    String filename;

  // LARGE FILES ( > 1 MB)
  //        @Param({ "building-30mb.jpg", "land-8mb.jpg", "mountains-20mb.jpg",
  //                "crowd-3mb.jpg", "australia-250mb.png", "salt-lake-340mb.jpg"})
  //        String filename;

  //   SINGLE FILE
  @Param({"tank.jpg"})
  String filename;

  // SMALL FILES ( < 1 MB)
  //    @Param({"unicorn-rainbow-57kb.gif", "land-100kb.jpg", "parliment-60kb.jpg",
  // "city-300kb.jpg",
  //            "UN-bus-attack.jpg", "militants.jpg"})
  //    String filename;
  //     JPEG2000 FILES
  //  @Param({"baghdad-j2k-20mb.jp2", "carrots-j2k-8mb.j2k", "olso-j2k-19mb.jp2"})
  //  String filename;

  public static void main(String[] args) throws RunnerException {
    String simpleName = BeLittleBenchmark.class.getSimpleName();
    Options opt =
        new OptionsBuilder()
            .forks(1)
            .warmupIterations(0)
            .measurementIterations(1)
            .include(simpleName)
            .resultFormat(ResultFormatType.NORMALIZED_CSV)
            .addProfiler(NaiveHeapSizeProfiler.class)
            .addProfiler(GCProfiler.class)
            .result("results.csv")
            .build();
    new Runner(opt).run();
  }

  @Setup
  public void setup() throws IOException {
    // Add a JPEG 2000 reader
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  @TearDown
  public void teardown() throws IOException {
    // Save thumbnail as a PNG in the output directory
    ImageIO.write(lastThumbnail, "png", new File(outputDir + lastDescription + "-" + filename));
    lastDescription = null;
    lastThumbnail = null;
  }

  //  @Benchmark
  public BufferedImage scalrSimple() throws IOException {
    lastDescription = "scalr";
    lastThumbnail = Scalr.resize(ImageIO.read(getSoureceFile()), thumbSize);
    return lastThumbnail;
  }

  @Benchmark
  public BufferedImage thumbnailatorSimple() throws IOException {
    lastDescription = "thumbnailator";
    lastThumbnail = Thumbnails.of(getSoureceFile()).height(thumbSize).asBufferedImage();
    return lastThumbnail;
  }

  //  @Benchmark
  //  public BufferedImage subsamplingAutoThumbnailator() throws IOException {
  //    lastThumbnail =
  //        Thumbnails.of(SampledImageReader.of(getSoureceFile()).read())
  //            .height(thumbSize)
  //            .asBufferedImage();
  //    lastDescription = "subsamplingAUTO";
  //    return lastThumbnail;
  //  }

  @Benchmark
  public BufferedImage scalrTikaTransformer() throws IOException {
    lastDescription = "scalrTikaTransformer";
    Image source = ImageIO.read(getSoureceFile());
    BufferedImage output =
        new BufferedImage(
            source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = output.createGraphics();
    graphics.drawImage(source, null, null);
    graphics.dispose();
    lastThumbnail = Scalr.resize(output, thumbSize);
    return lastThumbnail;
  }

  @Benchmark
  public BufferedImage imageMagickStream()
      throws IOException, IM4JavaException, InterruptedException {
    lastDescription = "imageMagickStream";

    Stream2BufferedImage outputConsumer = new Stream2BufferedImage();
    IMOperation op = new IMOperation();
    op.addImage(getSoureceFile().getCanonicalPath());
    op.thumbnail(256);
    op.addImage("png:-");
    ConvertCmd convert = new ConvertCmd();
    convert.setSearchPath("/opt/local/bin/");
    convert.setOutputConsumer(outputConsumer);
    convert.run(op);
    lastThumbnail = outputConsumer.getImage();
    return lastThumbnail;
  }

  //  @Benchmark
  public BufferedImage imageMagick() throws IOException, IM4JavaException, InterruptedException {

    lastDescription = "imageMagick";
    File tempOutput = null;
    Process proc;
    try {
      tempOutput = File.createTempFile("output", "");
      proc =
          new ProcessBuilder(
                  ABSOLUTE_PATH_TO_IMAGEMAGICK_CONVERT_EXECUTABLE,
                  "-sample",
                  "1024x1024",
                  "-thumbnail",
                  "256x256",
                  getSoureceFile().getCanonicalPath(),
                  tempOutput.getCanonicalPath())
              .start();
      proc.waitFor();
      lastThumbnail = ImageIO.read(tempOutput);
    } finally {
      if (Objects.nonNull(tempOutput)) {
        tempOutput.delete();
      }
    }
    return lastThumbnail;
  }

  public File getSoureceFile() {
    return new File(inputDir + filename);
  }

  @SuppressWarnings("unused")
  // Example of fast duplication of a file, or fast copy
  void copyFileUsingFileChannels(File source, File dest) throws IOException {
    FileChannel inputChannel = null;
    FileChannel outputChannel = null;
    try {
      inputChannel = new FileInputStream(source).getChannel();
      outputChannel = new FileOutputStream(dest).getChannel();
      outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
    } finally {
      inputChannel.close();
      outputChannel.close();
    }
  }
}
