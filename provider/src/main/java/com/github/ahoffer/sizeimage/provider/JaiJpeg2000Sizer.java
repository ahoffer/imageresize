package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.REDUCTION_FACTOR;

import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;
import com.github.jaiimageio.jpeg2000.J2KImageReadParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;

@SuppressWarnings("squid:S2160")
public class JaiJpeg2000Sizer extends AbstractImageSizer {

  // TODO: Make this configurable.
  // TODO: Do not set it for images close to the target size, or else there will be too little
  // information and the output image will be very blurry
  public static final double DEFAULT_BIT_PER_PIXEL = 0.3;

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

    addMessage(messageFactory.make(MessageConstants.RESOLUTION_LEVELS, reductionFactor));
    final ImageReader reader = ImageIO.getImageReadersByMIMEType("image/jpeg2000").next();
    final J2KImageReadParam param = (J2KImageReadParam) reader.getDefaultReadParam();
    param.setResolution(reductionFactor);
    param.setDecodingRate(DEFAULT_BIT_PER_PIXEL);
    try {
      final ImageInputStream iis = ImageIO.createImageInputStream(inputStream);
      reader.setInput(iis, true, true);
      decodedImage = reader.read(0, param);
    } catch (IOException | ClassCastException e) {
      addMessage(messageFactory.make(MessageConstants.DECODE_JPEG2000));
    }
  }

  int getReductionFactor() {
    return new ComputeResolutionLevel()
        .setMaxResolutionlevels(metadata.getMinNumbeResolutionLevels())
        .setInputWidthHeight(metadata.getWidth(), metadata.getHeight())
        .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
        .compute();
  }
}
