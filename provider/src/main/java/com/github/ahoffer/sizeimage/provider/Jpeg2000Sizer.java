package com.github.ahoffer.sizeimage.provider;

import com.github.jaiimageio.jpeg2000.J2KImageReadParam;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageReader;
import net.coobird.thumbnailator.Thumbnails;

@SuppressWarnings("squid:S2160")
public class Jpeg2000Sizer extends AbstractImageSizer {

  ImageReaderShortcuts shortcuts = new ImageReaderShortcuts();
  /**
   * Diego Santa Cruz, Touradj Ebrahimi, and Charilaos Christopoulos
   *
   * <p>Dr. Dobb's Journal, April 01, 2001
   *
   * <p>... by reconstructing only some of the decomposition levels, a lower resolution version of
   * the image can be obtained. Each of these resolutions is called a "resolution level." Typically,
   * five decomposition levels are used, which results in six resolution levels, all related by a
   * factor of two."
   */
  int assumedMaximumNumberOfResolutionLayers = 6;

  int imageIndex = 0;
  ImageReader reader;

  public Optional<BufferedImage> generate() {
    BufferedImage outputImage;
    endorse();
    try {
      BufferedImage decodedImage = getDecodedImage();
      outputImage =
          Thumbnails.of(decodedImage).size(getMaxWidth(), getMaxHeight()).asBufferedImage();
    } catch (IOException e) {
      outputImage = null;
    } finally {
      cleanup();
    }

    return Optional.ofNullable(outputImage);
  }

  void cleanup() {
    if (Objects.nonNull(inputStream)) {
      try {
        inputStream.close();
      } catch (IOException e) {
        // There is nothing that can be done about it
      }
      inputStream = null;
    }
    if (Objects.nonNull(reader)) {
      reader.dispose();
    }
  }

  BufferedImage getDecodedImage() throws IOException {
    reader = shortcuts.getReader(inputStream);
    J2KImageReadParam param = (J2KImageReadParam) reader.getDefaultReadParam();
    param.setResolution(getResolutionLayersToDecode(reader));
    imageIndex = 0;
    return reader.read(imageIndex, param);
  }

  int getResolutionLayersToDecode(ImageReader reader) throws IOException {
    int resizeFactor =
        new ComputeResizeFactor()
            .setWidthHeight(reader.getWidth(imageIndex), reader.getHeight(imageIndex))
            .compute();
    return Math.min(resizeFactor, assumedMaximumNumberOfResolutionLayers);
  }
}
