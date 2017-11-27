package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SAMPLE_PERIOD;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageReadParam;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {
  public static final String SAMPLING_PERIOD = "samplingPeriod";
  int imageIndex = 0;

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
    ImageReadParam imageParam = shortcuts.getDefaultImageReadParam(inputStream);
    int columnOffset = 0;
    int rowOffset = 0;
    final BufferedImage[] inputImage = new BufferedImage[1];
    int period;
    if (configuration.containsKey(SAMPLING_PERIOD)) {
      period = Integer.valueOf(configuration.get(SAMPLING_PERIOD));
    } else {
      final int[] sourceWidth = new int[1];
      final int[] sourceHeight = new int[1];
      shortcuts.executeWithReader(
          inputStream,
          reader -> sourceWidth[0] = reader.getWidth(imageIndex),
          reader -> sourceHeight[0] = reader.getHeight(imageIndex));
      period =
          new ComputeSubSamplingPeriod()
              .setInputWidthHeight(sourceWidth[0], sourceHeight[0])
              .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
              .compute();
    }

    imageParam.setSourceSubsampling(period, period, columnOffset, rowOffset);
    shortcuts.executeWithReader(
        inputStream, reader -> inputImage[0] = reader.read(imageIndex, imageParam));

    BufferedImage output =
        Thumbnails.of(inputImage[0]).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    addMessage(messageFactory.make(SAMPLE_PERIOD, period));
    return output;
  }
}
