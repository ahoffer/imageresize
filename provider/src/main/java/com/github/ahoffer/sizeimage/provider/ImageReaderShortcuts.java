package com.github.ahoffer.sizeimage.provider;

import static javax.imageio.ImageIO.createImageInputStream;
import static javax.imageio.ImageIO.getImageReaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageReader;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageReaderShortcuts {
  private static final Logger LOGGER = LoggerFactory.getLogger(BeLittle.class);

  public ImageReader getDatalessReader(Object source) throws IOException {
    Iterator<ImageReader> iter = getImageReaders(createImageInputStream(source));
    Validate.isTrue(
        iter.hasNext(),
        "Could not find an image reader. Missing JAI reader for image type or stream already closed");
    return iter.next();
  }

  // TODO Hide the exception by returning an optional?
  public ImageReader getReader(Object source) throws IOException {
    ImageReader reader = getDatalessReader(createImageInputStream(source));
    reader.setInput(createImageInputStream(source));
    return reader;
  }

  public List<String> getMimeTypes(Object source) {
    Validate.notNull(source);
    String[] mimeTypes;
    ImageReader reader = null;
    try {
      reader = getDatalessReader(source);
      mimeTypes = reader.getOriginatingProvider().getMIMETypes();
    } catch (IOException e) {
      LOGGER.debug("Could not read image type from image input stream", e);
      return Collections.emptyList();
    } finally {
      if (Objects.nonNull(reader)) {
        reader.dispose();
      }
    }
    return Arrays.asList(mimeTypes);
  }
}
