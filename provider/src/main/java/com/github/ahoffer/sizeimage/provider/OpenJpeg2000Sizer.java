package com.github.ahoffer.sizeimage.provider;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.SystemUtils;

public class OpenJpeg2000Sizer extends AbstractImageSizer {

  @Override
  public BeLittlingResult generate() {
    BeLittlingResult result;
    BufferedImage output = null;
Optional<Map<String, Integer>> widthAndHeight = shortcuts.getWidthAndHeight(inputStream);
    int reduceFactor;
if(widthAndHeight.isPresent()) {
  Map<String, Integer> map = widthAndHeight.get();
  int width = map.get(ImageReaderShortcuts.WIDTH);
  int height = map.get(ImageReaderShortcuts.HEIGHT);
   reduceFactor =
      new ComputeResolutionLevel()
          .setInputWidthHeight(width, height)
          .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
          .compute();
} else
    {
      addMessage(messageFactory.make(CANNOT_READ_WIDTH_AND_HEIGHT));
      reduceFactor = 0;
    }

    Path inputImageFile = null;
    Path outputImageFile = null;
    try {
       inputImageFile = Files.createTempFile("source", ".jp2");
      outputImageFile = Files.createTempFile("output", ".png");
      // TODO 1: I do not know why yet, but I cannot pass null for the CopyOption.
      // todo: Read indications you can pass null, but will throw exception if file already
      // todo: exists. But existing file shouldn't be an issue in this case.
      // TODO 2: Add check on bytes written. If bytes read < 1, it is an error.
      long bytesWritten = java.nio.file.Files.copy(inputStream, inputImageFile, REPLACE_EXISTING);
    }
    catch (IOException e) {
  addMessage(messageFactory.make(UNABLE_TO_CREATE_TEMP_FILE));
    }



      // TODO Add call to endorse()


      // TODO Benchmark without the force-rgb option and see how that works. Probably won't make a
      // difference.
      // TODO investigate the -l option for the number of quality layers. wtf is a quality layer?
      ProcessBuilder processBuilder =
          new ProcessBuilder(
              getExecFile().getPath(),
              "-r",
              String.valueOf(reduceFactor),
              "-threads",
              "ALL_CPUS",
              "-force-rgb",
              "-i",
              inputImageFile.toString(),
              "-o",
              outputImageFile.toString());

      processBuilder.redirectErrorStream(true);

      Process process = processBuilder.start();
      // TODO: If the return code is not 0, add an error message AND read the first line from
      // todo: stdout/stderr as part of a message.
      int returnCode = process.waitFor();
      InputStream stdout = process.getInputStream();
      BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(stdout));

      String line = stdOutReader.readLine();
      //      while (line != null && !line.trim().equals("--EOF--")) {
      //        System.out.println("Stdout: " + line);
      //        line = stdOutReader.readLine();
      //      }

      output = ImageIO.read(outputImageFile.toFile());

    } catch (IOException | InterruptedException e) {
      addMessage(messageFactory.make(OPJ_FAILED, e));
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
        // Pull the "get configuration" code out and add a WARNING message if not present.
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
