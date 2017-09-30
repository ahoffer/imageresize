package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class BasicImageSizer extends AbstractImageSizer {

  public BufferedImage size() throws IOException {
    validateBeforeResizing();
    return Thumbnails.of(inputStream).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
  }
}
