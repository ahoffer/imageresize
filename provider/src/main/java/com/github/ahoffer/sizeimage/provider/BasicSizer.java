package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class BasicSizer extends AbstractImageSizer {

  public BeLittlingResult generate() {
    BufferedImage output = null;
    if (endorse()) {
      try {
        output = getOutputImage();
      } catch (IOException e) {
        addMessage(messageFactory.make(MessageConstants.RESIZE_ERROR));
      }
    }
    return new BeLittlingResultImpl(output, messages);
  }

  BufferedImage getOutputImage() throws IOException {
    return Thumbnails.of(inputStream).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
  }
}
