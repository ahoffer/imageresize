package belittle.sizers;

import belittle.ImageInputFile;
import belittle.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
   * <p>BMP is fast to encode and smaller than a pixel map
   */
  public static final String STD_IN = "-";

  public static final String STD_OUT = ":-";
  File executable;

  private String outputFormatExtension = "bmp";

  public MagickSizer(File executable) {
    setExecutable(executable);
  }

  @Override
  public BufferedImage resize(ImageInputFile file) {
    // TODO if MIME type is JPEG, add this option "-define
    // jpeg:generate=200x200" and substitute a
    // size that is twice the size of the desired thumbnail.
    // TODO use -sample to improve memory usage
    // TODO: Does "thumbnail" use "sample" option?
    // todo: If not, maybe compose a sample and scale operation to improve performance?

    IMOperation op = new IMOperation();
    ConvertCmd command = new ConvertCmd();
    AccessController.doPrivileged(
        (PrivilegedAction<Void>)
            () -> {
              Stream2BufferedImage outputConsumer = new Stream2BufferedImage();
              op.thumbnail(width, height);

              // Read the image from std in
              op.addImage(STD_IN);
              file.doWithInputStream(
                  (inputStream) -> {
                    command.setInputProvider(new Pipe(inputStream, null));
                    String outputFormatDirectedToStandardOut = getOutputFormatExtension() + STD_OUT;
                    op.addImage(outputFormatDirectedToStandardOut);
                    command.setOutputConsumer(outputConsumer);
                    command.setSearchPath(getExecutable().getParent());
                    try {
                      command.run(op);
                      result.setOutput(outputConsumer.getImage());
                    } catch (InterruptedException | IM4JavaException | IOException e) {
                      addError("Failed to run Image Magick", e);
                    }
                  });
              return null;
            });
    return result.getOutput();
  }

  public ImageSizer getNew() {
    return new MagickSizer(this.executable);
  }

  public String getOutputFormatExtension() {
    return outputFormatExtension;
  }

  public void setOutputFormatExtension(String outputFormatExtension) {
    this.outputFormatExtension = outputFormatExtension;
  }

  public File getExecutable() {
    return executable;
  }

  public void setExecutable(File executable) {
    this.executable = executable;
  }

  @Override
  public boolean isAvailable() {
    return getExecutable().canExecute();
  }
}
