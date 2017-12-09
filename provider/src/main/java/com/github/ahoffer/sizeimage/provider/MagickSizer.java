package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.EXTERNAL_EXECUTABLE;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;

import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.SystemUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Stream2BufferedImage;
import org.im4java.process.Pipe;

public class MagickSizer extends AbstractImageSizer {

  /**
   * JPEG compresses well, but is designed for large real world images, not small thumbnails. It
   * also does not allow any form of transparency.
   *
   * <p>GIF works for simple small images, and compresses okay. It has a color limit of 256, but for
   * small images this is rarely noticeable. It has limited ability to handle transparency (a pixel
   * is either transparent or not)
   *
   * <p>PNG is a good format for thumbnails. It has a good compression and internal format styles.
   * It is non-lossy, and can display many colors. It is slow to encode.
   *
   * <p>BMP is fast tot encode and smaller than a pixel map
   */
  public static final String DEFAULT_OUTPUT_FORMAT = "bmp";

  public static final String OUTPUT_FORMAT_KEY = "outputFormat";
  public static final String STD_IN = "-";
  public static final String STD_OUT = ":-";

  File imgMagick;

  void prepare() {
    super.prepare();
    if (!isAvailable()) {
      addMessage(messageFactory.make(EXTERNAL_EXECUTABLE));
    }
  }

  void generateOutput() {
    // TODO if MIME type is JPEG, add this option "-define jpeg:generate=200x200" and substitute a
    // size that is twice the size of the desired thumbnail.
    // TODO use -sample to improve memory usage

    IMOperation op = new IMOperation();
    ConvertCmd command = new ConvertCmd();
    Stream2BufferedImage outputConsumer = new Stream2BufferedImage();
    // TODO: Does "thumbnail" use "sample" option?
    // todo: If not, maybe compose a sample and scale operation to improve performance?
    op.thumbnail(getMaxWidth(), getMaxHeight());

    // Read from std in
    op.addImage(STD_IN);
    command.setInputProvider(new Pipe(inputStream, null));

    // Write to std out
    String outputFormatDirectedToStandardOut =
        configuration.getOrDefault(OUTPUT_FORMAT_KEY, DEFAULT_OUTPUT_FORMAT) + STD_OUT;
    op.addImage(outputFormatDirectedToStandardOut);
    command.setOutputConsumer(outputConsumer);

    command.setSearchPath(getExecPath());
    try {
      command.run(op);
    } catch (InterruptedException | IM4JavaException | IOException e) {
      addMessage(messageFactory.make(RESIZE_ERROR, e));
      output = outputConsumer.getImage();
    }
  }

  @Override
  public boolean isAvailable() {
    return getExecFile().canExecute();
  }

  protected String getExecPath() {
    return configuration.get(PATH_TO_EXECUTABLE);
  }

  protected File getExecFile() {
    if (imgMagick == null) {
      imgMagick = new File(getExecPath(), getExecName());
    }
    return imgMagick;
  }

  protected String getExecName() {
    return SystemUtils.IS_OS_WINDOWS ? "convert.exe" : "convert";
  }
}
