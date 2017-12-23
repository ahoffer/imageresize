package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.support.MessageConstants.RESIZE_ERROR;

import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class BasicSizer extends AbstractImageSizer {

  void generateOutput() {
    try {
      output = Thumbnails.of(inputStream).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    } catch (IOException e) {
      addMessage(messageFactory.make(RESIZE_ERROR, e));
    }
  }
}
