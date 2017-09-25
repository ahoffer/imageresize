package com.github.ahoffer.sizeimage.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;
import static javax.imageio.ImageIO.createImageInputStream;
import static javax.imageio.ImageIO.getImageReaders;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.lang3.Validate;

public class ImageReaderUtils {
  public static int READLIMIT = 8192;

  public static ImageReader getReader(InputStream inputStream) throws IOException {
    ImageInputStream imageInputStream = createImageInputStream(inputStream);
    Iterator<ImageReader> iter = getImageReaders(imageInputStream);
    Validate.isTrue(iter.hasNext(), "Could not find an image reader. Is stream already closed?");
    ImageReader reader = iter.next();
    reader.setInput(imageInputStream);
    return reader;
  }

  public static String getFormat(InputStream inputStream) throws IOException {
    Validate.notNull(inputStream);
    String formatName;
    ImageReader reader = null;
    try {
      if (inputStream.markSupported()) {
        inputStream.mark(READLIMIT);
      }

      reader = getReader(inputStream);
      formatName = reader.getFormatName();

    } finally {
      if (Objects.nonNull(reader)) {
        reader.dispose();
      }
      if (inputStream.markSupported()) {
        inputStream.reset();
      }
    }
    return formatName;
  }
}
