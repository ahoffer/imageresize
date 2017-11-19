package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import net.coobird.thumbnailator.Thumbnails;

public class BasicSizer extends AbstractImageSizer {

  public Optional<BufferedImage> generate() {
    validateBeforeResizing();
    BufferedImage output;
    try {
      output = getOutputImage();
      inputStream.close();
    } catch (IOException e) {
      output = null;
    }
    inputStream = null;
    return Optional.ofNullable(output);
  }

  BufferedImage getOutputImage() throws IOException {
    return Thumbnails.of(inputStream).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
  }
}
