package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class BasicImageSizer extends AbstractImageSizer {

  public BufferedImage size() throws IOException {
    validateBeforeResizing();
    return Thumbnails.of(inputStream).height(getOutputSize()).asBufferedImage();
  }

  public boolean recommendedFor(String imageFormat) {
    return JPEG_2000_FORMAT_NAME.equalsIgnoreCase(imageFormat);
  }
}
