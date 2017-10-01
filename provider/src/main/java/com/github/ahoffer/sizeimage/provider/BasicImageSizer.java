package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class BasicImageSizer extends AbstractImageSizer {

  public BufferedImage size() throws IOException {
    validateBeforeResizing();
    BufferedImage output =
        Thumbnails.of(inputStream).size(getMaxWidth(), getMaxHeight()).asBufferedImage();

    // TODO: The inputStream doesn't belong to the ImageSizer; it was passed in as a parameter.
    // It doesn't feel right to close it on someone else's behalf.
    // But I can drop the reference to it.
    inputStream = null;

    return output;
  }
}
