package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.ComputeResolutionLevel.FULL_RESOLUTION;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.CANNOT_READ_WIDTH_AND_HEIGHT;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.EXTERNAL_EXECUTABLE;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.OPJ_FAILED;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.UNABLE_TO_CREATE_TEMP_FILE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.SystemUtils;

public class OpenJpeg2000Sizer extends AbstractImageSizer {

  @Override
  public BeLittlingResult generate() {

    // TODO Add call to endorse()

    BeLittlingResult result;
    BufferedImage output = null;
    File inputFile = null;
    File outputFile = null;
    int returnCode;
    long bytesWritten;
    long bytesRead;

    int reductionFactor = getReductionFactor();

    try {
      inputFile = File.createTempFile("input", ".jp2");
      inputFile.deleteOnExit();
      // There is a limited number of output formats to choose from.
      // For Open JPEG v2.3.0 they are: PBM|PGM|PPM|PNM|PAM|PGX|PNG|BMP|TIF|RAW|RAWL|TGA
      // Choose PNG given that is an excellent format for thumbnails. Also it is compressed which
      // should cut down on JVM-OS IO times. However, it is possible that another format could
      // lower overall image generation time. TODO: Something to experiment with...
      outputFile = File.createTempFile("output", ".png");
      outputFile.deleteOnExit();

      // TODO 1: I do not know why yet, but I cannot pass null for the CopyOption.
      // todo: Read indications you can pass null, but will throw exception if file already
      // todo: exists. But existing file shouldn't be an issue in this case.
      // TODO 2: Add check on bytes written. If bytes read < 1, it is an error.
      bytesWritten = java.nio.file.Files.copy(inputStream, inputFile.toPath(), REPLACE_EXISTING);
    } catch (IOException e) {
      addMessage(messageFactory.make(UNABLE_TO_CREATE_TEMP_FILE));
    }

    // TODO: Add call to canProceedToGenerateImage

    // TODO Benchmark force-rgb option. Probably won't make a difference.
    // TODO investigate the -l option for the number of quality layers. wtf is a quality layer?
    String execPath = getExecFile().getPath();
    String rFactor = String.valueOf(reductionFactor);
    String inputFilePath = inputFile.toString();
    String outputFilePath = outputFile.toString();
    ProcessBuilder processBuilder =
        getProcessBuilderForDecompress(execPath, rFactor, inputFilePath, outputFilePath);
    startProcess(processBuilder);

    // TODO Add call to canProceedToGenerateImage

    output = getImage(outputFile);
    result = new BeLittlingResultImpl(output, messages);
    cleanup();
    return result;
  }

  private BufferedImage getImage(File file) {
    BufferedImage image = null;
    try {
      image = ImageIO.read(file);

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
    return new ProcessBuilder(
            execPath,
            "-r",
            rFactor,
            "-threads",
            "ALL_CPUS",
            "-force-rgb",
            "-i",
            inputFilePath,
            "-o",
            outputFilePath)
        .redirectErrorStream(false);
  }

  private int getReductionFactor() {

    Jpeg2000SizeExtractor extractor = new Jpeg2000SizeExtractor(inputStream);
    try {
      extractor.extract();
    } catch (IOException e) {
      addMessage(messageFactory.make(CANNOT_READ_WIDTH_AND_HEIGHT));
      return FULL_RESOLUTION;
    }
    return new ComputeResolutionLevel()
        .setInputWidthHeight(extractor.getWidth(), extractor.getHeight())
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
        configuration.getOrDefault(PATH_TO_EXECUTABLE_KEY, ""),
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
    return canProceedToGenerateImage();
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
        builder.append(line);
      }
    } catch (IOException e) {
      return "<COULD NOT READ ERR STREAM>";
    }
    return builder.toString();
  }
}