package com.github.ahoffer.sizeimage.provider;

import static javax.imageio.ImageIO.createImageInputStream;

import com.github.ahoffer.sizeimage.provider.BeLittle.StreamResetException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageReaderShortcuts {
  private static final Logger LOGGER = LoggerFactory.getLogger(BeLittle.class);

  public ImageReader getDatalessReader(ImageInputStream imageStream) throws IOException {
    //    ImageInputStream imageStream = ImageIO.createImageInputStream(source);
    Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
    Validate.isTrue(
        readers.hasNext(),
        "Could not find an image reader. Missing JAI reader for image type or stream already closed");
    return readers.next();
  }

  public ImageReader getReader(Object source) throws IOException {
    ImageInputStream imageInputStream = createImageInputStream(source);
    ImageReader reader = getDatalessReader(imageInputStream);
    reader.setInput(imageInputStream);
    return reader;
  }

  public List<String> getMimeTypes(InputStream source) throws StreamResetException {
    Validate.notNull(source);
    String[] mimeTypes;
    ImageReader reader = null;
    try (ImageInputStream imageInputStream = createImageInputStream(source)) {
      reader = getDatalessReader(imageInputStream);
      mimeTypes = reader.getOriginatingProvider().getMIMETypes();
    } catch (IOException e) {
      LOGGER.info("Could not read image type from image input stream", e);
      return Collections.emptyList();
    } finally {
      if (Objects.nonNull(reader)) {
        reader.dispose();
      }
      try {
        source.reset();
      } catch (IOException e) {
        // If the stream cannot be reset, it cannot be used again. Let the caller decide what to do.
        throw new StreamResetException();
      }
    }
    return Arrays.asList(mimeTypes);
  }
}
