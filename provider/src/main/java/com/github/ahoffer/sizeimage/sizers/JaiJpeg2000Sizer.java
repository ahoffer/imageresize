package com.github.ahoffer.sizeimage.sizers;

import static com.github.ahoffer.sizeimage.support.MessageConstants.REDUCTION_FACTOR;
import static com.github.ahoffer.sizeimage.support.MessageConstants.SAMPLE_PERIOD;

import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;
import com.github.ahoffer.sizeimage.support.BeLittlingMessageImpl;
import com.github.ahoffer.sizeimage.support.ComputeResolutionLevel;
import com.github.ahoffer.sizeimage.support.ComputeSubSamplingPeriod;
import com.github.ahoffer.sizeimage.support.Jpeg2000MetadataMicroReader;
import com.github.ahoffer.sizeimage.support.MessageConstants;
import com.github.ahoffer.sizeimage.support.SaferImageReader;
import com.github.ahoffer.sizeimage.support.SaferImageReader.ImageReaderError;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReadParamJava;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.imageio.spi.IIORegistry;
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
  BufferedImage decodedImage;
  int reductionFactor;

  void prepare() {
    super.prepare();
    readMetaData();
    reductionFactor = getReductionFactor();
    addMessage(messageFactory.make(REDUCTION_FACTOR, reductionFactor));
  }

  void generateOutput() {
    try {
      output = Thumbnails.of(decodedImage).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    } catch (IOException e) {
      addMessage(messageFactory.make(MessageConstants.RESIZE_ERROR, e));
    }
  }

  void readMetaData() {
    try {
      // TODO: ortho-744mb.jp2 comes back with a reduction factor of 0? Is that really how the thing
      // is encoded or is there something wrong with the metadata reader?
      // UPDATE: There is no dimension information in the header or codestream boxes.
      metadata = new Jpeg2000MetadataMicroReader(inputStream);
      metadata.read();
    } catch (IOException e) {
      addMessage(new BeLittlingMessageImpl("IO Exception", BeLittlingSeverity.ERROR, e));
    }
    if (!metadata.isSucessfullyRead()) {
      addMessage(messageFactory.make(MessageConstants.COULD_NOT_READ_METADATA));
    }
  }

  void processInput() {
    J2KImageReadParamJava param = new J2KImageReadParamJava();
    param.setResolution(reductionFactor);
    param.setDecodingRate(DEFAULT_BITS_PER_PIXEL);
    try (SaferImageReader reader = new SaferImageReader(inputStream, param)) {

      // TODO: this sampling part needs testing.
      if (reductionFactor == 0) {
        int samplingPeriod =
            new ComputeSubSamplingPeriod()
                .setInputSize(reader.getWidth().get(), reader.getHeight().get())
                .setOutputSize(getMaxWidth(), getMaxHeight())
                .compute();
        reader.setSourceSubsampling(samplingPeriod, samplingPeriod);
        addMessage(messageFactory.make(SAMPLE_PERIOD, samplingPeriod));
      }

      try {
        decodedImage = reader.read().get();
      } catch (ClassCastException | NoSuchElementException | ImageReaderError e) {
        addMessage(messageFactory.make(MessageConstants.DECODE_JPEG2000));
      }
    }
  }

  int getReductionFactor() {
    return new ComputeResolutionLevel()
        .setMaxResolutionLevels(metadata.getMinNumResolutionLevels())
        .setInputSize(metadata.getWidth(), metadata.getHeight())
        .setOutputSize(getMaxWidth(), getMaxHeight())
        .compute();
  }
}
