package belittle.sizers;

import belittle.BeLittleResult;
import belittle.BeLittleSizerSetting;
import belittle.ImageInputFile;
import belittle.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class BasicSizer extends AbstractImageSizer {

  public BasicSizer(BeLittleSizerSetting sizerSetting) {
    super(sizerSetting);
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public BeLittleResult resize(ImageInputFile file) {
    BufferedImage image = null;
    try {
      image =
          Thumbnails.of(file.getFile())
              .size(sizerSetting.getWidth(), sizerSetting.getHeight())
              .asBufferedImage();
    } catch (IOException e) {
      addError("Thumnailator error", e);
      throw new RuntimeException(e);
    }

    result.setOutput(image);
    return result;
  }

  @Override
  public ImageSizer getNew(BeLittleSizerSetting sizerSetting) {
    return new BasicSizer(sizerSetting);
  }
}
