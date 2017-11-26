package com.github.ahoffer.sizeimage.provider;

import static java.util.Collections.EMPTY_LIST;
import static javax.imageio.ImageIO.createImageInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageReaderShortcuts {

  private static final Logger LOGGER = LoggerFactory.getLogger(BeLittle.class);
  // Bytes in a stream will be buffered when read, up the the read limit.
  // Those bytes will be restored if something "happens" to the original bytes.
  // The goal is to allow the reader to read an image file's metadata and then
  // allow the entire image to be decoded later.
  // TODO: I have not idea if 8K is big enough. It seems HUGE, iff all the image's metadata
  // todo: is at the begining of the stream.
  public static final int READLIMIT = 1024 * 8;
  public static final String WIDTH = "WIDTH";
  public static final String HEIGHT = "HEIGHT";

  public ImageReader getReader(InputStream inputStream) throws IOException {
    CloseShieldInputStream shieldedStream = new CloseShieldInputStream(inputStream);
    ImageInputStream imageStream = createImageInputStream(shieldedStream);
    Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
    Validate.isTrue(
        readers.hasNext(),
        "Could not find an image reader. "
            + "Stream closed or not at zero postion? "
            + "Missing JAI reader for image type");
    ImageReader reader = readers.next();
    reader.setInput(imageStream);
    return reader;
  }

  public void executeWithReader(InputStream inputStream, Consumer<ImageReader> doWithReader)
      throws IOException {
    // TODO: Profile this to make sure all this stream stuff isn't expensive.
    inputStream.mark(READLIMIT);
    ImageInputStream imageStream = createImageInputStream(new CloseShieldInputStream(inputStream));
    Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
    Validate.isTrue(
        readers.hasNext(),
        "Could not find an image reader. "
            + "Stream closed or not at zero postion? "
            + "Missing JAI reader for image type");
    ImageReader reader = readers.next();
    reader.setInput(imageStream);
    try {
      doWithReader.accept(reader);
    } finally {
      // http://info.michael-simons.eu/2012/01/25/the-dangers-of-javas-imageio/
      // "ImageReader caches some data in files. It is essential to dispose the reader
      // and the underlying ImageInputStream if it’s not needed anymore..."
      if (reader.getInput() != null && reader.getInput() instanceof ImageInputStream) {
        ((ImageInputStream) reader.getInput()).close();
      }
      reader.dispose();
      inputStream.reset();
    }
  }

  public List<String> getMimeTypes(InputStream source) {
    Validate.notNull(source);
    final String[][] mimeTypes = new String[1][];
    try {
      executeWithReader(
          source,
          reader -> {
            mimeTypes[0] = reader.getOriginatingProvider().getMIMETypes();
          });
    } catch (IOException e) {
      return EMPTY_LIST;
    }
    return Arrays.asList(mimeTypes[0]);
  }

  public ImageReadParam getDefaultImageReadParam(InputStream source) throws IOException {
    Validate.notNull(source);
    final ImageReadParam[] param = new ImageReadParam[1];
    executeWithReader(
        source,
        reader -> {
          param[0] = reader.getDefaultReadParam();
        });
    return param[0];
  }

  public Optional<Map<String, Integer>> getWidthAndHeight(InputStream source) {

    Validate.notNull(source);
    final boolean[] failed = new boolean[1];
    final Integer[] sizes = new Integer[2];
    try {
      executeWithReader(
          source,
          reader -> {
            try {
              sizes[0] = reader.getWidth(0);
              sizes[1] = reader.getHeight(0);
            } catch (IOException e) {
              failed[0] = true;
            }
          });
    } catch (IOException e) {
      failed[0] = true;
    }

    if (failed[0]) {
      return Optional.empty();
    }
    Map<String, Integer> map = new HashMap<>();
    map.put(WIDTH, sizes[0]);
    map.put(HEIGHT, sizes[1]);
    return Optional.empty().of(map);
  }
}
