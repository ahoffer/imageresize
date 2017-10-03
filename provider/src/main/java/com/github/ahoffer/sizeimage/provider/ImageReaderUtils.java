package com.github.ahoffer.sizeimage.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import static javax.imageio.ImageIO.createImageInputStream;
import static javax.imageio.ImageIO.getImageReaders;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageReaderUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImageReaderUtils.class);
  public static int READLIMIT = 8192;

  public static ImageReader getReader(InputStream inputStream) throws IOException {
    ImageInputStream imageInputStream = createImageInputStream(inputStream);
    Iterator<ImageReader> iter = getImageReaders(imageInputStream);
    Validate.isTrue(iter.hasNext(), "Could not find an image reader. Is stream already closed?");
    ImageReader reader = iter.next();
    reader.setInput(imageInputStream);
    return reader;
  }

  public static List<String> getMimeTypes(InputStream inputStream) {
    Validate.notNull(inputStream);
    String[] mimeTypes;
    ImageReader reader = null;
    boolean markSupported = inputStream.markSupported();
    try {
      if (markSupported) {
        inputStream.mark(READLIMIT);
      }

      reader = getReader(inputStream);
      mimeTypes = reader.getOriginatingProvider().getMIMETypes();

    } catch (IOException e) {
      LOGGER.debug("Could not read image type from image input stream", e);
      return Collections.emptyList();
    } finally {
      if (Objects.nonNull(reader)) {
        reader.dispose();
      }
      if (markSupported) {
        try {
          inputStream.reset();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return Arrays.asList(mimeTypes);
  }
}
