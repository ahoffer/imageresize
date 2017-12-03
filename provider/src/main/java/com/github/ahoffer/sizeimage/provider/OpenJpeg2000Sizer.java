package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.EXTERNAL_EXECUTABLE;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.OPJ_FAILED;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.UNABLE_TO_CREATE_TEMP_FILE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.SystemUtils;

public class OpenJpeg2000Sizer extends AbstractImageSizer {

  // There is a limited number of output formats to choose from.
  // For Open JPEG v2.3.0 they are: PBM|PGM|PPM|PNM|PAM|PGX|PNG|BMP|TIF|RAW|RAWL|TGA
  // Chose BMP. No, seriously! It is much faster than PNG and smaller than PPM.

  public static final String OUTPUT_FORMAT_EXT = ".bmp";
  Jpeg2000MetadataMicroReader metadata;

  @Override
  public BeLittlingResult generate() {

    readMetaData();

    // TODO Add call to endorse()
    stampNameOnResults();
    BeLittlingResult result = null;
    File inputFile = null;
    Path outputFile = null;
    long bytesWritten;

    int reductionFactor = getReductionFactor();

    try {
      inputFile = File.createTempFile("input", getProperFileExt());
      inputFile.deleteOnExit();

      // Don't create a file for output. The OS might not let it be overwritten by the command line
      // program.
      String tempDir = inputFile.getParent();
      outputFile = Paths.get(tempDir, "out" + UUID.randomUUID() + OUTPUT_FORMAT_EXT);

      // TODO 1: I do not know why yet, but I cannot pass null for the CopyOption.
      // todo: Read indications you can pass null, but will throw exception if file already
      // todo: exists. But existing file shouldn't be an issue in this case.
      // TODO 2: Add check on bytes written. If bytes read < 1, it is an error.
      bytesWritten = java.nio.file.Files.copy(inputStream, inputFile.toPath(), REPLACE_EXISTING);
    } catch (IOException e) {
      addMessage(messageFactory.make(UNABLE_TO_CREATE_TEMP_FILE));
    }

    // TODO: Add call to canProceed

    // TODO Benchmark force-rgb option. Probably won't make a difference.
    // TODO investigate the -l option for the number of quality layers. wtf is a quality layer?
    ProcessBuilder processBuilder =
        getProcessBuilderForDecompress(
            getExecFile().getPath(),
            String.valueOf(reductionFactor),
            inputFile.toString(),
            outputFile.toString());

    BufferedImage output = null;

    try {
      startProcess(processBuilder);
      output = getOutput(outputFile);
    } finally {
      // TODO: Add warning message if either file cannot be deleted
      inputFile.delete();
      outputFile.toFile().delete();
    }

    // TODO Add call to canProceed

    // The image might be smaller than its original size because a reduction factor was passed
    // to the Open JPEG 2000 decoder. It is not the size specified by the sizer's configuration.
    // Use another sizing library to get the exact size.
    BufferedImage finalOutput = null;
    try {
      finalOutput = Thumbnails.of(output).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    } catch (IOException e) {
      addMessage(messageFactory.make(RESIZE_ERROR, e));
    } finally {
      result = new BeLittlingResultImpl(finalOutput, messages);
      cleanup();
    }
    return result;
  }

  private String getProperFileExt() {
    // Open JPEG 2000 is very strict about file extensions. It will refuse to read a file if it has
    // the wrong extension.
    // From Wikipedia:
    // JPEG 2000 defines both a file format and a code stream. Whereas JPEG 2000 entirely describes
    // the image samples, JPEG-1 includes additional meta-information such as the resolution of the
    // image or the color space that has been used to encode the image. JPEG 2000 images should — if
    // stored as files — be boxed in the JPEG 2000 file format, where they get the .jp2 extension.
    // The part-2 extension to JPEG 2000, i.e., ISO/IEC 15444-2, also enriches this file format by
    // including mechanisms for animation or composition of several code streams into one single
    // image. Images in this extended file-format use the .jpx extension.
    //   There is no standardized extension for code-stream data because code-stream data is not to
    // be considered to be stored in files in the first place, though when done for testing
    // purposes, the extension .jpc or .j2k appear frequently.

    if (metadata.isJpeg2000File()) {
      return ".jp2";
    } else {
      return ".j2k";
    }
  }

  void readMetaData() {
    try {
      metadata = new Jpeg2000MetadataMicroReader(inputStream);
      boolean success = metadata.read();
      if (!success) {
        addMessage(messageFactory.make(MessageConstants.COULD_NOT_READ_IMAGE_METADATA));
      }
    } catch (IOException e) {
      addMessage(messageFactory.make(MessageConstants.STREAM_MANGLED, e));
    }
  }

  private BufferedImage getOutput(Path file) {
    BufferedImage image = null;
    try {
      image = ImageIO.read(file.toFile());

    } catch (IOException e) {
      // TODO: Add message that could not read file.
      // addMessage(messageFactory.make(, e));
    }
    return image;
  }

  private void startProcess(ProcessBuilder processBuilder) {
    int returnCode;
    Process process;
    try {
      process = processBuilder.start();
      try {
        returnCode = process.waitFor();
        if (returnCode != 0) {
          addMessage(messageFactory.make(OPJ_FAILED, getStdError(process.getErrorStream())));
        }
      } catch (InterruptedException e) {
        addMessage(messageFactory.make(MessageConstants.OS_PROCESS_INTERRUPTED));
      }

    } catch (IOException e) {
      addMessage(messageFactory.make(MessageConstants.OS_PROCESS_FAILED, e));
    }
  }

  private ProcessBuilder getProcessBuilderForDecompress(
      String execPath, String rFactor, String inputFilePath, String outputFilePath) {
    // TODO play around with Quality Layers -l "
    // Include space after options
    return new ProcessBuilder(
            execPath,
            "-r",
            rFactor,
            "-threads",
            "ALL_CPUS",
            "-i",
            inputFilePath,
            "-o",
            outputFilePath)
        .redirectErrorStream(false);
  }

  int getReductionFactor() {
    return new ComputeResolutionLevel()
        .setMaxResolutionlevels(metadata.getMinNumbeResolutionLevels())
        .setInputWidthHeight(metadata.getWidth(), metadata.getHeight())
        .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
        .compute();
  }

  @Override
  public boolean isAvailable() {
    return getExecFile().canExecute();
  }

  protected File getExecFile() {
    return new File(
        // Pull the "get configuration" code out and add a WARNING message if not present.
        configuration.getOrDefault(PATH_TO_EXECUTABLE, ""),
        SystemUtils.IS_OS_WINDOWS ? "opj_decompress.exe" : "opj_decompress");
  }

  // TODO: Make this configurable?
  // todo: I'd rather have people use the given name of the executable and not rename it.
  //  protected String getExecName() {
  //    return SystemUtils.IS_OS_WINDOWS
  //        ? configuration.get(WINDOWS_EXEC_NAME_KEY)
  //        : configuration.get(NIX_EXEC_NAME_KEY);
  //  }

  // TODO This code copied from MagickSizer. Put it in a common place
  public boolean endorse() {
    super.endorse();
    if (!isAvailable()) {
      addMessage(
          messageFactory.make(
              EXTERNAL_EXECUTABLE,
              "Executable not found. Check executable path. "
                  + "Process does not inherit a PATH environment variable"));
    }
    return canProceed();
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
}
