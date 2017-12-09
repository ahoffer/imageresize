package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SAMPLE_PERIOD;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {
  public static final String SAMPLING_PERIOD_KEY = "samplingPeriod";
  BufferedImage input;

  //  BufferedImage getOutputImage() throws IOException {
  //    ImageReadParam imageParam = shortcuts.getDefaultReadParam(inputStream);
  //    int columnOffset = 0;
  //    int rowOffset = 0;
  //    int period;
  //    if (configuration.containsKey(SAMPLING_PERIOD_KEY)) {
  //      period = Integer.valueOf(configuration.get(SAMPLING_PERIOD_KEY));
  //    } else {
  //      final int[] sourceWidth = new int[1];
  //      final int[] sourceHeight = new int[1];
  //      shortcuts.executeWithReader(
  //          inputStream,
  //          reader -> sourceWidth[0] = reader.getWidth(imageIndex),
  //          reader -> sourceHeight[0] = reader.getHeight(imageIndex));
  //      period =
  //          new ComputeSubSamplingPeriod()
  //              .setInputWidthHeight(sourceWidth[0], sourceHeight[0])
  //              .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
  //              .compute();
  //    }
  //
  //    imageParam.setSourceSubsampling(period, period, columnOffset, rowOffset);
  //
  //    // This will cause a problem  with JP2 because JP2 reader reads the WHOLE stream when
  //    // it initializes the reader.
  //    BufferedImage source = shortcuts.read(inputStream, imageIndex, imageParam);
  //
  //    BufferedImage output =
  //        Thumbnails.of(source).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
  //    addMessage(messageFactory.make(SAMPLE_PERIOD, period));
  //    return output;
  //  }

  void processInput() {
    ImageReader reader = null;
    try {
      reader = shortcuts.getFirstImageReaderSpi(inputStream).createReaderInstance();
      reader.setInput(ImageIO.createImageInputStream(inputStream));
      final int inputWidth = reader.getWidth(0);
      final int inputHeight = reader.getHeight(0);

      ImageReadParam imageParam = reader.getDefaultReadParam();
      int columnOffset = 0;
      int rowOffset = 0;
      int period;
      if (configuration.containsKey(SAMPLING_PERIOD_KEY)) {
        period = Integer.valueOf(configuration.get(SAMPLING_PERIOD_KEY));
      } else {
        period =
            new ComputeSubSamplingPeriod()
                .setInputWidthHeight(inputWidth, inputHeight)
                .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
                .compute();
      }
      imageParam.setSourceSubsampling(period, period, columnOffset, rowOffset);
      addMessage(messageFactory.make(SAMPLE_PERIOD, period));
      input = reader.read(0, imageParam);
      ((ImageInputStream) reader.getInput()).close();

    } catch (IOException e) {
      addMessage(messageFactory.make(MessageConstants.COULD_NOT_READ_IMAGE, e));
    } finally {
      if (reader != null) {
        reader.dispose();
        // noinspection UnusedAssignment
        reader = null;
      }
    }
  }

  void generateOutput() {
    try {
      output = Thumbnails.of(input).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    } catch (IOException e) {
      addMessage(messageFactory.make(RESIZE_ERROR, e));
    }
  }
}
