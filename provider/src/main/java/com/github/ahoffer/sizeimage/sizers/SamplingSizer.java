package com.github.ahoffer.sizeimage.sizers;

import static com.github.ahoffer.sizeimage.support.MessageConstants.CANNOT_READ_WIDTH_AND_HEIGHT;
import static com.github.ahoffer.sizeimage.support.MessageConstants.RESIZE_ERROR;

import com.github.ahoffer.sizeimage.BeLittleSizerSetting;
import com.github.ahoffer.sizeimage.BeLittleMessage.BeLittlingSeverity;
import com.github.ahoffer.sizeimage.BeLittleMessageImpl;
import com.github.ahoffer.sizeimage.BeLittleResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.support.ComputeSubSamplingPeriod;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {

  public static final String SAMPLING_PERIOD_KEY = "samplingPeriod";

  private Integer samplingPeriod;
  private ImageReader reader;

  public SamplingSizer(BeLittleSizerSetting sizerSetting) {
    super(sizerSetting);
  }

  void prepare(InputStream inputStream) {}

  void cleanup() {
    if (reader != null) {
      reader.dispose();
    }
  }

  @Override
  public BeLittleResult resize(InputStream inputStream) {

    ImageInputStream iis = null;
    reader = getImageReaderByMIMEType();
    try {
      iis = ImageIO.createImageInputStream(inputStream);
      reader.setInput(iis);
    } catch (IOException e) {
      e.printStackTrace();
    }

    int inputHeight = 0;
    int inputWidth = 0;
    try {
      inputHeight = reader.getHeight(0);
      inputWidth = reader.getWidth(0);
    } catch (IOException e) {
      // A message will be added later
    }
    if (inputHeight <= 0 || inputWidth <= 0) {
      int defaultSamplingPeriod = Integer.valueOf(sizerSetting.getProperty(SAMPLING_PERIOD_KEY));
      if (defaultSamplingPeriod > 0) {
        samplingPeriod = defaultSamplingPeriod;
        addMessage(
            new BeLittleMessageImpl(
                CANNOT_READ_WIDTH_AND_HEIGHT,
                BeLittlingSeverity.WARNING,
                String.format(
                    "Width and height of input image cannot be determined. Using configured sampling period of %s pixels",
                    samplingPeriod)));

      } else {
        addMessage(
            new BeLittleMessageImpl(
                CANNOT_READ_WIDTH_AND_HEIGHT,
                BeLittlingSeverity.ERROR,
                "Width and/or height of input image cannot be determined and no default sampling period is value is configured. Sampling sizer needs these values to compute subsampling period."));
      }
    } else {
      samplingPeriod =
          new ComputeSubSamplingPeriod()
              .setInputSize(inputWidth, inputHeight)
              .setOutputSize(sizerSetting.getWidth(), sizerSetting.getHeight())
              .compute();
    }
    reader.getDefaultReadParam().setSourceSubsampling(samplingPeriod, samplingPeriod, 0, 0);

    BufferedImage image = null;

    try {
      image = reader.read(0);
      result.setOutput(
          Thumbnails.of(image)
              .size(sizerSetting.getWidth(), sizerSetting.getHeight())
              .asBufferedImage());
    } catch (IOException e) {
      addMessage(messageFactory.make(RESIZE_ERROR, e));
    }
    return result;
  }

  @Override
  public ImageSizer getNew(BeLittleSizerSetting sizerSetting) {
    return new SamplingSizer(sizerSetting);
  }
}
