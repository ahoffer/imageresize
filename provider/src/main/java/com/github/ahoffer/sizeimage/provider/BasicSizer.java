package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class BasicSizer extends AbstractImageSizer {

  public BufferedImage generate() throws IOException {
    validateBeforeResizing();
    BufferedImage output =
        Thumbnails.of(inputStream).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    inputStream.close();
    inputStream = null;

    return output;
  }
}
