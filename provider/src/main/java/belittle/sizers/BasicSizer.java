package belittle.sizers;

import belittle.ImageInputFile;
import belittle.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class BasicSizer extends AbstractImageSizer {

  public BasicSizer() {
    super();
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public BufferedImage resize(ImageInputFile file) {
    BufferedImage image = null;
    try {
      image = Thumbnails.of(file.getFile()).size(width, height).asBufferedImage();
      result.setOutput(image);
    } catch (IOException e) {
      addError("Thumnailator error", e);
    }
    return image;
  }

  @Override
  public ImageSizer getNew() {
    return new BasicSizer();
  }
}
