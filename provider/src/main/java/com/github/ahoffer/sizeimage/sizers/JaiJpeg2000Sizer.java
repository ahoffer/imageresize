package com.github.ahoffer.sizeimage.sizers;

import static com.github.ahoffer.sizeimage.support.MessageConstants.REDUCTION_FACTOR;
import static com.github.ahoffer.sizeimage.support.MessageConstants.SAMPLE_PERIOD;

import com.github.ahoffer.sizeimage.BeLittleSizerSetting;
import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;
import com.github.ahoffer.sizeimage.BeLittlingMessageImpl;
import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.support.ComputeResolutionLevel;
import com.github.ahoffer.sizeimage.support.ComputeSubSamplingPeriod;
import com.github.ahoffer.sizeimage.support.Jpeg2000MetadataMicroReader;
import com.github.ahoffer.sizeimage.support.MessageConstants;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReadParamJava;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;

@SuppressWarnings("squid:S2160")
public class JaiJpeg2000Sizer extends AbstractImageSizer {

  // TODO: Make this configurable.

  /*
    TODO: Do not set bit per pixel for images close to the target size, or else there will be too little
    information and the output image will be very blurry
  */
  public static final double DEFAULT_BITS_PER_PIXEL = 0.3;

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  Jpeg2000MetadataMicroReader metadata;

  public JaiJpeg2000Sizer(BeLittleSizerSetting sizerSetting, BeLittlingResult injectedResult) {
    super(sizerSetting, injectedResult);
  }

  void readMetaData(InputStream inputStream) {
    try {
      // TODO: ortho-744mb.jp2 comes back with a reduction factor of 0? Is that really how the thing
      // is encoded or is there something wrong with the metadata reader?
      // UPDATE: There is no dimension information in the header or codestream boxes.
      metadata = new Jpeg2000MetadataMicroReader(inputStream);
      metadata.read();
    } catch (IOException e) {
      result.addMessage(new BeLittlingMessageImpl("IO Exception", BeLittlingSeverity.ERROR, e));
    }
    if (!metadata.isSucessfullyRead()) {
      result.addMessage(messageFactory.make(MessageConstants.COULD_NOT_READ_METADATA));
    }
  }

  int getReductionFactor() {
    return new ComputeResolutionLevel()
        .setMaxResolutionLevels(metadata.getMinNumResolutionLevels())
        .setInputSize(metadata.getWidth(), metadata.getHeight())
        .setOutputSize(sizerSetting.getWidth(), sizerSetting.getHeight())
        .compute();
  }

  @Override
  public BeLittlingResult resize(InputStream inputStream) {
    readMetaData(inputStream);
    BufferedImage decodedImage = null;
    J2KImageReadParamJava param = new J2KImageReadParamJava();
    int reductionFactor = getReductionFactor();
    param.setResolution(reductionFactor);
    result.addMessage(messageFactory.make(REDUCTION_FACTOR, reductionFactor));
    param.setDecodingRate(DEFAULT_BITS_PER_PIXEL);

    try {
      ImageReader reader = getImageReaderByMIMEType();
      ImageInputStream iis = ImageIO.createImageInputStream(inputStream);
      reader.setInput(iis);

      // TODO: this sampling part needs testing.
      if (reductionFactor == 0) {
        int samplingPeriod = getSamplingPeriod(reader.getWidth(0), reader.getWidth(0));
        param.setSourceSubsampling(samplingPeriod, samplingPeriod, 0, 0);
        result.addMessage(messageFactory.make(SAMPLE_PERIOD, samplingPeriod));
      }
      decodedImage = reader.read(0, param);
    } catch (IOException e) {
      result.addMessage(messageFactory.make(MessageConstants.DECODE_JPEG2000));
    }

    try {
      result.setOutput(
          Thumbnails.of(decodedImage)
              .size(sizerSetting.getWidth(), sizerSetting.getHeight())
              .asBufferedImage());
    } catch (IOException e) {
      result.addMessage(messageFactory.make(MessageConstants.RESIZE_ERROR, e));
    }
    return result;
  }

  private int getSamplingPeriod(int width, int height) {
    return new ComputeSubSamplingPeriod()
        .setInputSize(width, height)
        .setOutputSize(sizerSetting.getWidth(), sizerSetting.getWidth())
        .compute();
  }

  @Override
  public ImageSizer getNew(BeLittleSizerSetting sizerSetting, BeLittlingResult injectedResult) {
    return new JaiJpeg2000Sizer(sizerSetting, injectedResult);
  }
}
