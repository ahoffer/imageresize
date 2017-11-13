package com.github.ahoffer.sizeimage.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {

  public static final String SAMPLING_PERIOD = "samplePeriod";

  public BufferedImage generate() throws IOException {
    SamplingImageReader reader = SamplingImageReader.of(inputStream);
    if (configuration.containsKey(SAMPLING_PERIOD)) {
      reader.samplePeriod(Integer.valueOf(configuration.get(SAMPLING_PERIOD)));
    }
    BufferedImage output =
        Thumbnails.of(reader.read()).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    // TODO: The inputStream doesn't belong to the ImageSizer; it was passed in as a parameter.
    // It doesn't feel right to close it on someone else's behalf.
    // But I can drop the reference to it.
    inputStream = null;
    return output;
  }
}
