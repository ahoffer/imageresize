package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SAMPLE_PERIOD;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {

  public static final String SAMPLING_PERIOD = "resizeFactor";

  public BeLittlingResult generate() {
    BufferedImage output = null;
    stampNameOnResults();
    if (endorse()) {
      try {
        output = getOutputImage();
      } catch (IOException e) {
        addMessage(messageFactory.make(RESIZE_ERROR, e));
      } finally {
        cleanup();
      }
    }
    return new BeLittlingResultImpl(output, messages);
  }

  BufferedImage getOutputImage() throws IOException {
    SamplingImageReader reader = SamplingImageReader.of(inputStream);
    if (configuration.containsKey(SAMPLING_PERIOD)) {
      reader.samplePeriod(Integer.valueOf(configuration.get(SAMPLING_PERIOD)));
    }
    BufferedImage output =
        Thumbnails.of(reader.read()).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    addMessage(messageFactory.make(SAMPLE_PERIOD, reader.getSamplingPeriod()));
    return output;
  }
}
