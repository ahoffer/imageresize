package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.jaiimageio.jpeg2000.J2KImageReadParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import net.coobird.thumbnailator.Thumbnails;

@SuppressWarnings("squid:S2160")
public class Jpeg2000Sizer extends AbstractImageSizer {

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  ImageReaderShortcuts shortcuts = new ImageReaderShortcuts();
  int imageIndex = 0;
  ImageReader reader;

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

  // TODO: Maybe I should wrap the input stream in a shielded stream and close the reader?
  // TODO: CHECK FOR RESOURCE LEAKS BEFORE TRYING TO FIX RESOURCE LEAKS
  //  void cleanup() {
  //    if (Objects.nonNull(inputStream)) {
  //      try {
  //        inputStream.close();
  //      } catch (IOException e) {
  //        // There is nothing that can be done about it
  //      }
  //      inputStream = null;
  //    }
  //    if (Objects.nonNull(reader)) {
  //      reader.dispose();
  //    }
  //  }

  BufferedImage getDecodedImage() throws IOException {
    reader = shortcuts.getReader(inputStream);
    J2KImageReadParam param = (J2KImageReadParam) reader.getDefaultReadParam();
    int levels =
        new ComputeResolutionLevel()
            .setOutputWidthHeight(getMaxWidth(), getMaxHeight())
            .setInputWidthHeight(reader.getWidth(imageIndex), reader.getHeight(imageIndex))
            .compute();
    param.setResolution(levels);
    param.setDecodingRate(0.01);
    addMessage(messageFactory.make(MessageConstants.RESOLUTION_LEVELS, levels));
    return reader.read(imageIndex, param);
  }
}
