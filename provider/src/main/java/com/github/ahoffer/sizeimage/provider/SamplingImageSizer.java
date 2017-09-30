package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingImageSizer extends AbstractImageSizer {

  public static final String SAMPLING_PERIOD = "samplePeriod";

  public BufferedImage size() throws IOException {
    SampledImageReader reader = SampledImageReader.of(inputStream);
    if (configuration.containsKey(SAMPLING_PERIOD)) {
      reader.samplePeriod(Integer.valueOf(configuration.get(SAMPLING_PERIOD)));
    }
    return Thumbnails.of(reader.read()).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
  }
}
