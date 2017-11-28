package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.jaiimageio.jpeg2000.J2KImageReadParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.spi.IIORegistry;
import net.coobird.thumbnailator.Thumbnails;

@SuppressWarnings("squid:S2160")
public class JaiJpeg2000Sizer extends AbstractImageSizer {

  public static final double DEFAULT_BIT_PER_PIXEL = 0.3;

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  public BeLittlingResult generate() {
    BufferedImage outputImage = null;
    BufferedImage decodedImage;
    BeLittlingResult result;
    stampNameOnResults();
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

  BufferedImage getDecodedImage() throws IOException {

    J2KImageReadParam param = (J2KImageReadParam) shortcuts.getDefaultImageReadParam(inputStream);
    Jpeg2000SizeExtractor extractor = new Jpeg2000SizeExtractor(inputStream);
    int levels =
        new ComputeResolutionLevel()
            .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
            .setInputWidthHeight(extractor.getWidth(), extractor.getHeight())
            .compute();
    param.setResolution(levels);
    param.setDecodingRate(DEFAULT_BIT_PER_PIXEL);
    addMessage(messageFactory.make(MessageConstants.RESOLUTION_LEVELS, levels));
    return shortcuts.read(inputStream, 0, param);
  }
}
