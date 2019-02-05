package belittle.sizers;

import static belittle.support.MessageConstants.COULD_NOT_READ_METADATA;
import static belittle.support.MessageConstants.EXTERNAL_EXECUTABLE;
import static belittle.support.MessageConstants.OPJ_FAILED;
import static belittle.support.MessageConstants.OS_PROCESS_FAILED;
import static belittle.support.MessageConstants.OS_PROCESS_INTERRUPTED;
import static belittle.support.MessageConstants.REDUCTION_FACTOR;

import belittle.BeLittleMessage.BeLittleSeverity;
import belittle.BeLittleMessageImpl;
import belittle.BeLittleResult;
import belittle.BeLittleSizerSetting;
import belittle.ImageSizer;
import belittle.support.ComputeResolutionLevel;
import belittle.support.FuzzyFile;
import belittle.support.Jpeg2000MetadataMicroReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import net.coobird.thumbnailator.Thumbnails;

public class OpenJpeg2000Sizer extends ExternalProcessSizer {

  // There is a limited number of output formats to choose from.
  // For Open JPEG v2.3.0 they are: PBM|PGM|PPM|PNM|PAM|PGX|PNG|BMP|TIF|RAW|RAWL|TGA
  // Chose BMP. No, seriously! It is much faster than PNG and smaller than PPM.

  public static String OUTPUT_FORMAT_EXT = ".bmp";
  Jpeg2000MetadataMicroReader metadata;
  int reductionFactor;
  File outputFile;
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
              if (getExecutable() == null) {
                addMessage(messageFactory.make(EXTERNAL_EXECUTABLE));
                return null;
              }
              if (!getExecutable().canExecute()) {
                addMessage(
                    new BeLittleMessageImpl(
                        "CANNOT INVOKE EXEC", BeLittleSeverity.ERROR, "File cannot be executed"));
                return null;
              }
              doWithInputStream(file, (istream) -> readMetadata(istream));
              reductionFactor = getReductionFactor();
              addMessage(messageFactory.make(REDUCTION_FACTOR, reductionFactor));

              try {
                outputFile = File.createTempFile("belittle", OUTPUT_FORMAT_EXT);
                ProcessBuilder processBuilder = getProcessBuilderForDecompress(file);
                startProcess(processBuilder);
                result.setOutput(
                    Thumbnails.of(outputFile)
                        .size(sizerSetting.getWidth(), sizerSetting.getHeight())
                        .asBufferedImage());
              } catch (IOException e) {
                // TODO: Add message that could not read file.
                // addMessage(messageFactory.make(, e));
              }
              return null;
            });
    return result;
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
      addMessage(messageFactory.make(COULD_NOT_READ_METADATA));
    }
  }

  void startProcess(ProcessBuilder processBuilder) {
    int returnCode;
    try {
      process = processBuilder.start();
      try {
        returnCode = process.waitFor();
        if (returnCode != 0) {
          addMessage(messageFactory.make(OPJ_FAILED, getStdError(process.getErrorStream())));
        }
      } catch (InterruptedException e) {
        addMessage(messageFactory.make(OS_PROCESS_INTERRUPTED));
      }

    } catch (IOException e) {
      addMessage(messageFactory.make(OS_PROCESS_FAILED, e));
    }
  }

  ProcessBuilder getProcessBuilderForDecompress(File inputFile) {
    // TODO play around with Quality Layers -l "
    // Include space after options
    return new ProcessBuilder(
            getExecutable().getPath(),
            "-r",
            String.valueOf(reductionFactor),
            "-threads",
            "ALL_CPUS",
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
