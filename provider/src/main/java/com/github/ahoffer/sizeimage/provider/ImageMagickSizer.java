package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.SystemUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Stream2BufferedImage;
import org.im4java.process.Pipe;

public class ImageMagickSizer extends AbstractImageSizer {

  public static final String PATH_TO_IMAGE_MAGICK_EXECUTABLES = "pathToImageMagickExecutables";

  public static final String EXEC_NAME = "executableName";

  public static final String DEFAULT_OUTPUT_FORMAT = "png";

  public static final String WINDOWS_EXEC_NAME = "convert.exe";

  public static final String NIX_EXEC_NAME = "convert";

  public static final String INPUT_IMAGE_PATH = "inputImagePath";

  public static final String OUTPUT_FORMAT = "outputFormat";

  public boolean recommendedFor(String imageFormat) {
    return true;
  }

  @Override
  public boolean isAvailable() {
    return getImageMagickExecutable().canExecute();
  }

  public File getImageMagickExecutable2() {
    String result;
    ConvertCmd command = new ConvertCmd();
    try {
      result = command.searchForCmd(command.getCommand().get(0), getPath());
      return new File(result);
    } catch (IOException e) {
      return new File("");
    }
  }

  File getImageMagickExecutable() {
    String execName =
        configuration.getOrDefault(
            EXEC_NAME, SystemUtils.IS_OS_WINDOWS ? WINDOWS_EXEC_NAME : NIX_EXEC_NAME);
    File exec;
    if (configuration.containsKey(PATH_TO_IMAGE_MAGICK_EXECUTABLES)) {
      exec = new File(configuration.get(PATH_TO_IMAGE_MAGICK_EXECUTABLES), execName);
    } else {
      exec = new File(execName);
    }
    return exec;
  }

  public BufferedImage size() throws IOException {

    validateBeforeResizing();

    IMOperation op = new IMOperation();
    ConvertCmd command = new ConvertCmd();
    Stream2BufferedImage outputConsumer = new Stream2BufferedImage();
    op.thumbnail(getOutputSize());

    // Read from std in
    op.addImage("-");
    command.setInputProvider(new Pipe(inputStream, null));

    // Write to std out
    String outputFormatDirectedToStandardOut =
        configuration.getOrDefault(OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT) + ":-";
    op.addImage(outputFormatDirectedToStandardOut);
    command.setOutputConsumer(outputConsumer);

    command.setSearchPath(getPath());

    try {
      command.run(op);
    } catch (InterruptedException | IM4JavaException e) {
      throw new RuntimeException("Problem resizing image with ImageMagick", e);
    }
    return outputConsumer.getImage();
  }

  public void validateBeforeResizing() {

    super.validateBeforeResizing();

    if (!isAvailable()) {
      throw new RuntimeException(
          "Cannot size image. ImageMagick executable not found. "
              + "Check executable path. \n "
              + "Process does not inherit a PATH environment variable");
    }
  }

  public String getPath() {
    if (configuration.containsKey(PATH_TO_IMAGE_MAGICK_EXECUTABLES)) {
      return configuration.get(PATH_TO_IMAGE_MAGICK_EXECUTABLES);
    } else {
      return "";
    }
  }
}
