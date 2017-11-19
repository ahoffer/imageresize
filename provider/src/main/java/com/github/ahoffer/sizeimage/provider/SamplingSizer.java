package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {

  public static final String SAMPLING_PERIOD = "resizeFactor";

  public Optional<BufferedImage> generate() {
    BufferedImage outputImage;
    try {
      outputImage = getOutputImage();
    } catch (IOException e) {
      outputImage = null;
    }
    return Optional.ofNullable(outputImage);
  }

  BufferedImage getOutputImage() throws IOException {
    SamplingImageReader reader = SamplingImageReader.of(inputStream);
    if (configuration.containsKey(SAMPLING_PERIOD)) {
      reader.samplePeriod(Integer.valueOf(configuration.get(SAMPLING_PERIOD)));
    }
    BufferedImage output =
        Thumbnails.of(reader.read()).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    inputStream.close();
    inputStream = null;
    return output;
  }
}
