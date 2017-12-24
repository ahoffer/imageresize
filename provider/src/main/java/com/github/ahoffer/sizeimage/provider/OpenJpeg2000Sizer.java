package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.support.MessageConstants.COULD_NOT_READ_METADATA;
import static com.github.ahoffer.sizeimage.support.MessageConstants.OPJ_FAILED;
import static com.github.ahoffer.sizeimage.support.MessageConstants.OS_PROCESS_FAILED;
import static com.github.ahoffer.sizeimage.support.MessageConstants.OS_PROCESS_INTERRUPTED;
import static com.github.ahoffer.sizeimage.support.MessageConstants.REDUCTION_FACTOR;
import static com.github.ahoffer.sizeimage.support.MessageConstants.UNABLE_TO_CREATE_TEMP_FILE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;
import com.github.ahoffer.sizeimage.support.BeLittlingMessageImpl;
import com.github.ahoffer.sizeimage.support.ComputeResolutionLevel;
import com.github.ahoffer.sizeimage.support.Jpeg2000MetadataMicroReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import net.coobird.thumbnailator.Thumbnails;

public class OpenJpeg2000Sizer extends ExternalProcessSizer {

  // There is a limited number of output formats to choose from.
  // For Open JPEG v2.3.0 they are: PBM|PGM|PPM|PNM|PAM|PGX|PNG|BMP|TIF|RAW|RAWL|TGA
  // Chose BMP. No, seriously! It is much faster than PNG and smaller than PPM.

  public static String OUTPUT_FORMAT_EXT = ".bmp";
  Jpeg2000MetadataMicroReader metadata;
  int reductionFactor;
  File inputFile;
  Path outputFile;
  Process process;

  void prepare() {
    super.prepare();
    readMetaData();
    reductionFactor = getReductionFactor();
    addMessage(messageFactory.make(REDUCTION_FACTOR, reductionFactor));
  }

  void processInput() {

    long bytesWritten;
    try {
      inputFile = File.createTempFile("input", getProperFileExt());
      inputFile.deleteOnExit();

      // Don't create a file for output (e.g. createTempFile). Use a Path instead.
      // The OS might not let it be overwritten by the command line program.
      String tempDir = inputFile.getParent();
      outputFile = Paths.get(tempDir, "out" + UUID.randomUUID() + OUTPUT_FORMAT_EXT);

      // TODO Add check on bytes written. If bytes read < 1, it is an error.
      bytesWritten = java.nio.file.Files.copy(inputStream, inputFile.toPath(), REPLACE_EXISTING);
    } catch (IOException e) {
      addMessage(messageFactory.make(UNABLE_TO_CREATE_TEMP_FILE));
    }
  }

  void generateOutput() {
    ProcessBuilder processBuilder = getProcessBuilderForDecompress();
    startProcess(processBuilder);

    if (canProceed()) {
      try {
        output =
            Thumbnails.of(outputFile.toFile())
                .size(getMaxWidth(), getMaxHeight())
                .asBufferedImage();
      } catch (IOException e) {
        // TODO: Add message that could not read file.
        // addMessage(messageFactory.make(, e));
      }
    }
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

  void readMetaData() {
    try {
      metadata = new Jpeg2000MetadataMicroReader(inputStream);
      metadata.read();
    } catch (IOException e) {
      addMessage(new BeLittlingMessageImpl("IO Exception", BeLittlingSeverity.ERROR, e));
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

  ProcessBuilder getProcessBuilderForDecompress() {
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

  int getReductionFactor() {
    return new ComputeResolutionLevel()
        .setMaxResolutionLevels(metadata.getMinNumResolutionLevels())
        .setInputSize(metadata.getWidth(), metadata.getHeight())
        .setOutputSize(getMaxWidth(), getMaxHeight())
        .compute();
  }

  @Override
  public boolean isAvailable() {
    return getExecutable().canExecute();
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
