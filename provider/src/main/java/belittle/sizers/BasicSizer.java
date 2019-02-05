package belittle.sizers;

import static belittle.support.MessageConstants.RESIZE_ERROR;

import belittle.BeLittleResult;
import belittle.BeLittleSizerSetting;
import belittle.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class BasicSizer extends AbstractImageSizer {

  public BasicSizer(BeLittleSizerSetting sizerSetting) {
    super(sizerSetting);
  }

  @Override
  public BeLittleResult resize(File file) {
    BufferedImage image = null;
    try {
      image =
          Thumbnails.of(file)
              .size(sizerSetting.getWidth(), sizerSetting.getHeight())
              .asBufferedImage();
    } catch (IOException e) {
      addMessage(messageFactory.make(RESIZE_ERROR, e));
    }

    result.setOutput(image);
    return result;
  }

  @Override
  public ImageSizer getNew(BeLittleSizerSetting sizerSetting) {
    return new BasicSizer(sizerSetting);
  }
}
