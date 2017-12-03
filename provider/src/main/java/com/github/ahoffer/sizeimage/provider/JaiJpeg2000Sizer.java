package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.BeLittlingResult;
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

  public static final double DEFAULT_BIT_PER_PIXEL = 0.3;

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  Jpeg2000MetadataMicroReader metadata;

  public BeLittlingResult generate() {
    BufferedImage outputImage = null;
    BufferedImage decodedImage;
    BeLittlingResult result;
    stampNameOnResults();

    readMetaData();

    endorse();
    try {
      decodedImage = getDecodedImage();
      try {
        outputImage =
            Thumbnails.of(decodedImage).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
      } catch (IOException e) {
        addMessage(messageFactory.make(MessageConstants.RESIZE_ERROR, e));
      }
    } catch (IOException | ClassCastException e) {
      addMessage(messageFactory.make(MessageConstants.DECODE_JPEG2000));
    } finally {
      result = new BeLittlingResultImpl(outputImage, messages);
      cleanup();
    }
    return result;
  }

  void readMetaData() {
    try {
      metadata = new Jpeg2000MetadataMicroReader(inputStream);
      boolean success = metadata.read();
      if (!success) {
        addMessage(messageFactory.make(MessageConstants.COULD_NOT_READ_IMAGE_METADATA));
      }
    } catch (IOException e) {
      addMessage(messageFactory.make(MessageConstants.STREAM_MANGLED, e));
    }
  }

  BufferedImage getDecodedImage() throws IOException {

    int levels = getReductionFactor();
    addMessage(messageFactory.make(MessageConstants.RESOLUTION_LEVELS, levels));
    final ImageReader reader = ImageIO.getImageReadersByMIMEType("image/jpeg2000").next();
    final J2KImageReadParam param = (J2KImageReadParam) reader.getDefaultReadParam();
    param.setResolution(levels);
    param.setDecodingRate(DEFAULT_BIT_PER_PIXEL);
    final ImageInputStream iis = ImageIO.createImageInputStream(inputStream);
    reader.setInput(iis, true, true);
    final BufferedImage img = reader.read(0, param);
    return img;

    //    return shortcuts.read(inputStream, 0, param);
  }

  int getReductionFactor() {

    Jpeg2000MetadataMicroReader reader;

    try {
      reader = new Jpeg2000MetadataMicroReader(inputStream);
      boolean success = reader.read();
      if (!success) {
        return ComputeResolutionLevel.FULL_RESOLUTION;
      }
      return new ComputeResolutionLevel()
          .setMaxResolutionlevels(reader.getMinNumbeResolutionLevels())
          .setInputWidthHeight(reader.getWidth(), reader.getHeight())
          .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
          .compute();

    } catch (IOException e) {
      addMessage(messageFactory.make(MessageConstants.STREAM_MANGLED, e));
      return ComputeResolutionLevel.FULL_RESOLUTION;
    }
  }
}
