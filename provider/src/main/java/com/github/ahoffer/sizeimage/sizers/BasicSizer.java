package com.github.ahoffer.sizeimage.sizers;

import static com.github.ahoffer.sizeimage.support.MessageConstants.RESIZE_ERROR;

import com.github.ahoffer.sizeimage.BeLittleSizerSetting;
import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import net.coobird.thumbnailator.Thumbnails;

public class BasicSizer extends AbstractImageSizer {

  public BasicSizer(BeLittleSizerSetting sizerSetting) {
    super(sizerSetting);
  }

  @Override
  public BeLittlingResult resize(InputStream inputStream) {
    BufferedImage image = null;
    try {
      image =
          Thumbnails.of(inputStream)
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
