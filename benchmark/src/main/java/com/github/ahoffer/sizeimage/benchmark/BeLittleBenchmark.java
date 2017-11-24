package com.github.ahoffer.sizeimage.benchmark;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.BasicSizer;
import com.github.ahoffer.sizeimage.provider.JaiJpeg2000Sizer;
import com.github.ahoffer.sizeimage.provider.MagickSizer;
import com.github.ahoffer.sizeimage.provider.SamplingSizer;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnailator;
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

// Website with large ortho images: https://apollomapping.com/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BeLittleBenchmark {

  @Param({"128"})
  public int thumbSize;

  String inputDir = "/Users/aaronhoffer/data/sample-images/";
  String outputDir = inputDir + "output/";
  BufferedImage lastThumbnail;
  String lastDescription;

  // LARGE FILES ( > 1 MB)
  @Param({
    "building-30mb.jpg",
    "land-8mb.jpg",
    "mountains-20mb.jpg",
    "crowd-3mb.jpg",
    "australia-250mb.png",
    "salt-lake-340mb.jpg",
    "baghdad-j2k-20mb.jp2",
    "olso-j2k-19mb.jp2",
    "city-and-land-15mb.jpg",
    "gettysburg-6mb.jp2"
  })
  String filename;

  //   SINGLE FILE
  //  @Param({"tank.jpg"})
  //  String filename;

  // SMALL FILES ( < 1 MB)
  //  @Param({
  //    "unicorn-rainbow-57kb.gif",
  //    "land-100kb.jpg",
  //    "city-300kb.jpg",
  //    "parliament-60kb.jpg",
  //    "palace.j2k"
  //  })
  //  String filename;

  //     JPEG2000 FILES
  //  @Param({"baghdad-j2k-20mb.jp2", "carrots-j2k-8mb.j2k", "olso-j2k-19mb.jp2"})
  //  String filename;

  // TODO: Could account for common IO time by allocating a buffer in setup and copying the image
  // todo: file's bits into it.
  // TODO: Or could have a benchmark that does nothing but copy the input stream to get a sense of
  // todo: the IO overhead.
  public static void main(String[] args) throws RunnerException {
    String simpleName = BeLittleBenchmark.class.getSimpleName();
    Options opt =
        new OptionsBuilder()
            .forks(1)
            .warmupIterations(1)
            .measurementIterations(3)
            .include(simpleName)
            .resultFormat(ResultFormatType.NORMALIZED_CSV)
            .addProfiler(NaiveHeapSizeProfiler.class)
            .addProfiler(GCProfiler.class)
            .result("results.csv")
            .build();
    new Runner(opt).run();
  }

  @Setup
  public void setup() throws IOException {}

  @TearDown
  public void teardown() throws IOException {
    // Save thumbnail as a PNG in the output directory
    String ext = FilenameUtils.getExtension(filename);
    String nameWithoutExt = filename.replaceAll("." + ext, "");
    if (lastThumbnail != null) {
      File file = new File(outputDir + nameWithoutExt + "-" + lastDescription + "." + ext);
      file.mkdirs();
      ImageIO.write(lastThumbnail, "png", file);
    }
    lastDescription = null;
    lastThumbnail = null;
  }

  @Benchmark
  public void scalr() throws IOException {
    lastDescription = "scalr";
    lastThumbnail = Scalr.resize(ImageIO.read(getSoureceFile()), thumbSize);
  }

  @Benchmark
  public void basicSizer() throws IOException {
    lastDescription = "basicSizer";
    ImageSizer sizer = new BasicSizer();
    try (InputStream input = new FileInputStream(getSoureceFile())) {
      lastThumbnail =
          sizer.setOutputSize(thumbSize, thumbSize).setInput(input).generate().getOutput().get();
    }
  }

  @Benchmark
  public void thumbnailator() throws IOException {
    lastDescription = "thumbnailator";
    lastThumbnail = Thumbnailator.createThumbnail(getSoureceFile(), thumbSize, thumbSize);
  }

  @Benchmark
  public void scalrTikaTransformer() throws IOException {
    lastDescription = "scalrTikaTransformer";
    Image source = ImageIO.read(getSoureceFile());
    BufferedImage output = null;
    try {
      output =
          new BufferedImage(
              source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = output.createGraphics();
      graphics.drawImage(source, null, null);
      graphics.dispose();
      lastThumbnail = Scalr.resize(output, thumbSize);
    } catch (NullPointerException e) {
      String msg = "Failed to read " + getSoureceFile().getName();
      System.err.println(msg);
      throw new RuntimeException(msg);
    }
  }

  @Benchmark
  public void magickSizer() throws IOException {
    lastDescription = "magickSizer";
    ImageSizer sizer = new MagickSizer();
    Map<String, String> config = new HashMap<>();
    config.put("pathToImageMagickExecutables", "/opt/local/bin/");

    try (InputStream input = new FileInputStream(getSoureceFile())) {
      sizer.setConfiguration(config);
      lastThumbnail =
          sizer.setOutputSize(thumbSize, thumbSize).setInput(input).generate().getOutput().get();
    }
  }

  @Benchmark
  public void samplingSizer() throws IOException {
    lastDescription = "samplingSizer";
    ImageSizer sizer = new SamplingSizer();
    try (InputStream input = new FileInputStream(getSoureceFile())) {
      BeLittlingResult result =
          sizer.setOutputSize(thumbSize, thumbSize).setInput(input).generate();
      lastThumbnail = result.getOutput().get();
    }
  }

  @Benchmark
  public void jpeg2000Sizer() throws IOException {
    lastDescription = "jpeg2000Sizer";
    ImageSizer sizer = new JaiJpeg2000Sizer();
    File soureceFile = getSoureceFile();

    // This sizer works ONLY with JPEG 2000 images. Filter out other image types.
    String ext = FilenameUtils.getExtension(soureceFile.getName());
    if ("jp2".equalsIgnoreCase(ext) || "j2k".equalsIgnoreCase(ext)) {
      try (InputStream input = new FileInputStream(soureceFile)) {
        lastThumbnail =
            sizer.setOutputSize(thumbSize, thumbSize).setInput(input).generate().getOutput().get();
      }
    } else {
      throw new RuntimeException("Do not create metrics for this test");
    }
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
