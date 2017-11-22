package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SAMPLE_PERIOD;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {
  public static final int NO_SUBSAMPLING = 1;
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
    ImageReader reader = new ImageReaderShortcuts().getReader(inputStream);
    ImageReadParam imageParam = reader.getDefaultReadParam();
    int columnOffset = 0;
    int rowOffset = 0;
    int period;
    if (configuration.containsKey(SAMPLING_PERIOD)) {
      period = Integer.valueOf(configuration.get(SAMPLING_PERIOD));
    } else {
      try {
        int sourceWidth = reader.getWidth(imageIndex);
        int sourceHeight = reader.getHeight(imageIndex);
        period = new ComputeResizeFactor().setWidthHeight(sourceWidth, sourceHeight).compute();
      } catch (IOException e) {
        period = NO_SUBSAMPLING;
      }
    }
    imageParam.setSourceSubsampling(period, period, columnOffset, rowOffset);
    final BufferedImage input = reader.read(imageIndex, imageParam);
    BufferedImage output =
        Thumbnails.of(input).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    addMessage(messageFactory.make(SAMPLE_PERIOD, period));
    return output;
  }
}
