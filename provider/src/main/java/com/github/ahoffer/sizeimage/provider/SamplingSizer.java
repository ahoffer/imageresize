package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.CANNOT_READ_WIDTH_AND_HEIGHT;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.COULD_NOT_READ_IMAGE;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SAMPLE_PERIOD;

import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {

  public static final String SAMPLING_PERIOD_KEY = "samplingPeriod";
  SafeImageReader reader;
  private Integer samplingPeriod;
  private BufferedImage input;

  Optional<Integer> defaultSamplingPeriod() {
    return Optional.ofNullable(configuration.get(SAMPLING_PERIOD_KEY)).map(Integer::parseInt);
  }

  @Override
  void prepare() {
    reader = new SafeImageReader(inputStream);
    Optional<Integer> inputHeight = reader.getHeight();
    Optional<Integer> inputWidth = reader.getWidth();
    if (!(inputHeight.isPresent() && inputWidth.isPresent())) {
      Optional<Integer> defaultSamplingPeriod = defaultSamplingPeriod();
      if (defaultSamplingPeriod.isPresent()) {
        samplingPeriod = defaultSamplingPeriod.get();
        addMessage(
            new BeLittlingMessageImpl(
                CANNOT_READ_WIDTH_AND_HEIGHT,
                BeLittlingSeverity.WARNING,
                String.format(
                    "Width and height of input image cannot be determined. Using configured sampling period of %s pixels",
                    defaultSamplingPeriod.get())));

      } else {
        addMessage(
            new BeLittlingMessageImpl(
                CANNOT_READ_WIDTH_AND_HEIGHT,
                BeLittlingSeverity.ERROR,
                "Width and/or height of input image cannot be determined and no default sampling period is value is configured. Sampling sizer needs these values to compute subsampling period."));
      }
    } else {
      samplingPeriod =
          new ComputeSubSamplingPeriod()
              .setInputWidthHeight(inputWidth.get(), inputHeight.get())
              .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
              .compute();
    }
  }

  @Override
  void processInput() {
    reader.setSourceSubsampling(samplingPeriod, samplingPeriod);
    addMessage(messageFactory.make(SAMPLE_PERIOD, samplingPeriod));
    Optional<BufferedImage> result = reader.read();
    if (result.isPresent()) {
      input = result.get();
    } else {
      addMessage(
          new BeLittlingMessageImpl(
              COULD_NOT_READ_IMAGE,
              BeLittlingSeverity.ERROR,
              String.format("%s could not read input image", reader.getClass().getSimpleName())));
    }
  }

  @Override
  void generateOutput() {
    try {
      output = Thumbnails.of(input).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    } catch (IOException e) {
      addMessage(messageFactory.make(RESIZE_ERROR, e));
    }
  }

  @Override
  void cleanup() {
    if (reader != null) {
      reader.dispose();
    }
  }
}
