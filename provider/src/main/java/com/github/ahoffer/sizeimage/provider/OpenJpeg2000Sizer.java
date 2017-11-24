package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.EXTERNAL_EXECUTABLE;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.apache.commons.lang3.SystemUtils;

public class OpenJpeg2000Sizer extends AbstractImageSizer {

  @Override
  public BeLittlingResult generate() {
    BeLittlingResult result = null;
    BufferedImage output = null;
    try {
      Path inputImageFile = Files.createTempFile("source", "jp2");
      Path outputImageFile = Files.createTempFile("output", "png");

      // writeToDiskFile = File.createTempFile(fnameWrite, "jp2");
      //   writeToDiskFile.deleteOnExit();
      java.nio.file.Files.copy(inputStream, inputImageFile, null);
      //     readFromDiskFile = File.createTempFile(fnameRead, "");
      //   } catch (IOException e) {
      //      addMessage(messageFactory.make(UNABLE_TO_CREATE_TEMP_FILE));
      //   }

      ImageReader reader = new ImageReaderShortcuts().getReader(inputStream);

      int height = reader.getHeight(0);
      int width = reader.getWidth(0);
      int reduceFactor =
          new ComputeResolutionLevel()
              .setInputWidthHeight(width, height)
              .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
              .compute();

      // TODO Add call to endorse()

      // TODO Benchmark without the force-rgb option and see how that works. Probably won't make a
      // difference.
      // TODO investigate teh -l option for the number of quality layers. wtf is a quality layer?
      ProcessBuilder processBuilder =
          new ProcessBuilder(
              getExecFile().getPath(),
              "-r " + reduceFactor,
              "-threads ALL_CPUS",
              "-force-rgb",
              "-i " + inputImageFile,
              "-o " + outputImageFile);

      Process process = processBuilder.start();
      // TODO Do something if the return code is not 0
      int returnCode = process.waitFor();

      output = ImageIO.read(outputImageFile.toFile());

    } catch (IOException | InterruptedException e) {
      addMessage(messageFactory.make(MessageConstants.OPJ_FAILED));
    }
    result = new BeLittlingResultImpl(output, messages);
    cleanup();
    return result;
  }

  @Override
  public boolean isAvailable() {
    return getExecFile().canExecute();
  }

  protected File getExecFile() {
    return new File(
        configuration.getOrDefault(PATH_TO_EXECUTABLE_KEY, ""),
        SystemUtils.IS_OS_WINDOWS ? "opj_decompress.exe" : "opj_decompress");
  }

  // TODO Make this configurable?
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
}
