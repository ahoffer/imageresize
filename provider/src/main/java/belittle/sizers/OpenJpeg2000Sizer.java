package belittle.sizers;

import belittle.BeLittleMessage.BeLittleSeverity;
import belittle.BeLittleMessageImpl;
import belittle.BeLittleResult;
import belittle.BeLittleSizerSetting;
import belittle.ImageSizer;
import belittle.support.ComputeResolutionLevel;
import belittle.support.FuzzyFile;
import belittle.support.Jpeg2000MetadataMicroReader;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenJpeg2000Sizer extends ExternalProcessSizer {

  // There is a limited number of output formats to choose from.
  // For Open JPEG v2.3.0 they are: PBM|PGM|PPM|PNM|PAM|PGX|PNG|BMP|TIF|RAW|RAWL|TGA
  // Chose BMP. No, seriously! It is much faster than PNG and smaller than PPM.

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenJpeg2000Sizer.class);

  public static String OUTPUT_FORMAT_EXT = ".bmp";
  Jpeg2000MetadataMicroReader metadata;
  int reductionFactor;
  Process process;

  public OpenJpeg2000Sizer(BeLittleSizerSetting sizerSetting) {
    super(sizerSetting);
  }

  public OpenJpeg2000Sizer(BeLittleSizerSetting sizerSetting, FuzzyFile executable) {
    super(sizerSetting);
    this.setExecutable(executable);
  }

  public BeLittleResult resize(File file) {
    AccessController.doPrivileged(
        (PrivilegedAction<Void>)
            () -> {
              if (!isAvailable()) {
                addError(
                    String.format(
                        "%s not available or executable", getExecutable().getExecutableName()));
                return null;
              }

              doWithInputStream(file, (istream) -> readMetadata(istream));
              reductionFactor = getReductionFactor();
              addInfo(String.format("Starting with reduction factor %d", reductionFactor));
              File sourceWithNewExtension = null;
              File outputFile = null;
              try {
                sourceWithNewExtension = File.createTempFile("belittle", getProperFileExt());
                FileUtils.copyFile(file, sourceWithNewExtension);
                outputFile = File.createTempFile("belittle", OUTPUT_FORMAT_EXT);
              } catch (IOException e) {
                addError("Failed to copy input file. Cannot proceed.", e);
                return null;
              }
              do {
                // Open JPEG command line utility is very picky about the extension.
                doIt(sourceWithNewExtension, outputFile);
                reductionFactor--;
              } while (reductionFactor > 0 && isTooSmall(outputFile));

              try {
                result.setOutput(
                    Thumbnails.of(outputFile)
                        .size(sizerSetting.getWidth(), sizerSetting.getHeight())
                        .asBufferedImage());
              } catch (IOException e) {
                addError("Could not resize with Thumbnailator", e);
                throw new RuntimeException(e);
              }
              return null;
            });
    return result;
  }

  boolean isTooSmall(File outputFile) {
    BufferedImage intermediateImage = null;
    try {
      intermediateImage = ImageIO.read(outputFile.getAbsoluteFile());
    } catch (IOException e) {
      addError(
          String.format("Failed to read intermediate image file %s", outputFile.getAbsoluteFile()),
          e);
      return false;
    }

    return intermediateImage.getWidth() < sizerSetting.getWidth()
        || intermediateImage.getHeight() < sizerSetting.getHeight();
  }

  // THIS ONE DOES NOT SEEM TO BLOCK THE WAY I WANT.
  private void doIt(File file, File outputFile) {
    String path = getExecutable().getPath();
    CommandLine cmdLine = new CommandLine(path);
    cmdLine.addArgument("-r");
    cmdLine.addArgument(String.valueOf(reductionFactor));
    cmdLine.addArgument("-i");
    cmdLine.addArgument(file.toString());
    cmdLine.addArgument("-o");
    cmdLine.addArgument(outputFile.toString());
    Executor executor = new DefaultExecutor();
    ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
    executor.setWatchdog(watchdog);
    ExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    executor.setStreamHandler(streamHandler);
    boolean after;
    try {
      boolean before = watchdog.isWatching();
      executor.execute(cmdLine, resultHandler);
      after = watchdog.isWatching();
    } catch (IOException e) {
      e.printStackTrace();
    }

    boolean now = watchdog.isWatching();
  }

  /**
   * Open JPEG 2000 is strict about file extensions. It will refuse to read a file if it has the
   * wrong extension. From Wikipedia: JPEG 2000 defines both a file format and a code stream.
   * Whereas JPEG 2000 entirely describes the image samples, JPEG-1 includes additional
   * meta-information such as the resolution of the image or the color space that has been used to
   * encode the image. JPEG 2000 images should — if stored as files — be boxed in the JPEG 2000 file
   * format, where they get the .jp2 extension. The part-2 extension to JPEG 2000, i.e., ISO/IEC
   * 15444-2, also enriches this file format by including mechanisms for animation or composition of
   * several code streams into one single image. Images in this extended file-format use the .jpx
   * extension. There is no standardized extension for code-stream data because code-stream data is
   * not to be considered to be stored in files in the first place, though when done for testing
   * purposes, the extension .jpc or .j2k appear frequently.
   *
   * @return file extension ("dot" plus three characters
   */
  String getProperFileExt() {

    if (metadata.isJpeg2000File()) {
      return ".jp2";
    } else {
      return ".j2k";
    }
  }

  void readMetadata(InputStream inputStream) {
    try {
      metadata = new Jpeg2000MetadataMicroReader(inputStream);
      metadata.read();
    } catch (IOException e) {
      addMessage(new BeLittleMessageImpl("IO Exception", BeLittleSeverity.ERROR, e));
    }
    if (!metadata.isSucessfullyRead()) {
      addWarning("Failed to read JPEG 2000 metadata");
    }
  }

  void startProcess(ProcessBuilder processBuilder) {
    boolean returnCode;

    try {
      process = processBuilder.start();
      returnCode = process.waitFor(10, TimeUnit.SECONDS);
      if (true) {
        String error = getStdError(process.getErrorStream());
        LOGGER.error(error);
        addError("Return code from OS process is bad");
      }
    } catch (InterruptedException | IOException e) {
      addError("Fatal error. OS process failed.");
      throw new RuntimeException(e);
    }
  }

  ProcessBuilder getProcessBuilderForDecompress(File inputFile, File outputFile) {
    // TODO play around with Quality Layers -l "
    // Include space after options
    return new ProcessBuilder(
            getExecutable().getPath(),
            "-r",
            String.valueOf(reductionFactor),
            "-i",
            inputFile.toString(),
            "-o",
            outputFile.toString())
        .redirectErrorStream(false);
  }

  // A reduction factor greater than zero is CRITICAL.
  // With no reduction factor, the output image is at least as input image.
  // Also, decoding a JPEG 2000 is inversely propotional the the reduction factor.
  // TODO 1: Go ahead and create a huge TIF file or something, then use the Sampling Sizer to carve
  // it down to size.
  // TODO 2: Guess a reduction factor of 4 (done), but back it off and retry if it is too large
  // (todo).
  int getReductionFactor() {
    if (metadata.isSucessfullyRead()) {
      return new ComputeResolutionLevel()
          .setMaxResolutionLevels(metadata.getMinNumResolutionLevels())
          .setInputSize(metadata.getWidth(), metadata.getHeight())
          .setOutputSize(sizerSetting.getWidth(), sizerSetting.getHeight())
          .compute();

    } else {
      return 4;
    }
  }

  @Override
  public ImageSizer getNew(BeLittleSizerSetting sizerSetting) {
    return new OpenJpeg2000Sizer(sizerSetting, getExecutable());
  }

  String getStdError(InputStream inputStream) {
    StringBuilder builder = new StringBuilder();
    BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    try {
      line = stdOutReader.readLine();
      builder.append(line);
      // I grabbed this code off a website. I forget where. It has a funky EOF check and I included
      // it.
      String eof = "--EOF--";
      while (line != null && !line.trim().equals(eof)) {
        line = stdOutReader.readLine();
        if (line != null) {
          builder.append(line);
        }
      }
    } catch (IOException e) {
      addWarning("Could not read output stream", e);
      return "<COULD NOT READ ERR STREAM>";
    }
    return builder.toString();
  }

  @Override
  void cleanup() {
    super.cleanup();
    if (process.isAlive()) {
      process.destroy();
    }
  }
}
