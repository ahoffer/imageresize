package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.RESIZE_ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SAMPLE_PERIOD;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;

public class SamplingSizer extends AbstractImageSizer {
  public static final String SAMPLING_PERIOD = "samplingPeriod";
  int imageIndex = 0;

  public BeLittlingResult generate() {

    BeLittlingResult results = null;
    BufferedImage output = null;
    stampNameOnResults();
    if (endorse()) {
      try {
        output = getOutputImage();
      } catch (IOException e) {
        addMessage(messageFactory.make(RESIZE_ERROR, e));
      } finally {
        results = new BeLittlingResultImpl(output, messages);
        cleanup();
      }
    }
    return results;
  }

  //  BufferedImage getOutputImage() throws IOException {
  //    ImageReadParam imageParam = shortcuts.getDefaultReadParam(inputStream);
  //    int columnOffset = 0;
  //    int rowOffset = 0;
  //    int period;
  //    if (configuration.containsKey(SAMPLING_PERIOD)) {
  //      period = Integer.valueOf(configuration.get(SAMPLING_PERIOD));
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

  BufferedImage getOutputImage() throws IOException {
    ImageReader reader = shortcuts.getFirstImageReaderSpi(inputStream).createReaderInstance();
    reader.setInput(ImageIO.createImageInputStream(inputStream));
    ImageReadParam imageParam = reader.getDefaultReadParam();
    int columnOffset = 0;
    int rowOffset = 0;
    int period;
    if (configuration.containsKey(SAMPLING_PERIOD)) {
      period = Integer.valueOf(configuration.get(SAMPLING_PERIOD));
    } else {
      final int inputWidth = reader.getWidth(imageIndex);
      final int inputHeight = reader.getHeight(imageIndex);
      period =
          new ComputeSubSamplingPeriod()
              .setInputWidthHeight(inputWidth, inputHeight)
              .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
              .compute();
    }
    imageParam.setSourceSubsampling(period, period, columnOffset, rowOffset);
    addMessage(messageFactory.make(SAMPLE_PERIOD, period));
    BufferedImage input = null;
    try {
      input = doWithTimeout(() -> reader.read(imageIndex, imageParam));
    } catch (Exception e) {
      //     Catch the IOException and debug it
      e.printStackTrace();
    } finally {
      ((ImageInputStream) reader.getInput()).close();
      reader.dispose();
    }
    BufferedImage output = null;
    if (canProceed()) {
      output = Thumbnails.of(input).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    }
    return output;
  }
}
