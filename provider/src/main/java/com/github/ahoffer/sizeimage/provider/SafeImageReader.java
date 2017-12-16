package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.provider.BeLittle.StreamResetException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.lang3.Validate;

public class SafeImageReader implements AutoCloseable {
  // Bytes in a stream will be buffered when read, up the the read limit.
  // Those bytes will be restored if something "happens" to the original bytes.
  // The goal is to allow the reader to read an image file's metadata and then
  // allow the entire image to be decoded later.
  // TODO: I have not idea if 8K is big enough. It seems HUGE, iff all the image's metadata
  // todo: is at the begining of the stream.
  public static final int READLIMIT = 1024 * 8;
  ImageInputStream iis;
  ImageReader reader;
  ImageReaderSpi imageReaderSpi;
  InputStream inputStream;
  ImageReadParam readParam;
  protected int imageIndex;

  public SafeImageReader(InputStream inputStream) {
    Validate.notNull(inputStream);
    Validate.isTrue(inputStream.markSupported());
    this.inputStream = inputStream;
    inputStream.mark(READLIMIT);
    setImageIndex(0);
  }

  public List<String> getMimeTypes() {
    return Arrays.asList(getImageReaderSpi().getMIMETypes());
  }

  public void dispose() {
    //       http://info.michael-simons.eu/2012/01/25/the-dangers-of-javas-imageio/
    //       "ImageReader caches some data in files. It is essential to dispose the reader
    //       and the underlying ImageInputStream if it’s not needed anymore..."
    if (iis != null) {
      try {
        iis.close();
      } catch (IOException e) {
        // Failed to close, but nothing can be done about it.
      }
    }

    if (reader != null) {
      reader.dispose();
    }
  }

  public Optional<Integer> getWidth() {
    try {
      return Optional.of(getReader().getWidth(getImageIndex()));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public Optional<Integer> getHeight() {
    try {
      return Optional.of(getReader().getHeight(getImageIndex()));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public Optional<BufferedImage> read() {
    BufferedImage img = null;
    try {
      img = getReader().read(getImageIndex(), getImageReadParam());
    } catch (IOException e) {
      // Bummer
    }
    return Optional.ofNullable(img);
  }

  public boolean setSourceSubsampling(int sourceXSubsampling, int sourceYSubsampling) {
    try {
      int subsamplingXOffset = 0;
      int subsamplingYOffset = 0;
      getImageReadParam()
          .setSourceSubsampling(
              sourceXSubsampling, sourceYSubsampling, subsamplingXOffset, subsamplingYOffset);
    } catch (IOException e) {
      return false;
      // Do nothing. If there is a hard failure, it will happen when a getter is called.
    }
    return true;
  }

  ImageReadParam getImageReadParam() throws IOException {
    if (readParam == null) {
      readParam = getReader().getDefaultReadParam();
    }
    return readParam;
  }

  ImageReaderSpi createImageReaderSpi() {

    ImageReaderSpi next = null;
    boolean canDecode = false;
    try {
      Iterator<ImageReaderSpi> imageServiceProviders =
          IIORegistry.getDefaultInstance().getServiceProviders(ImageReaderSpi.class, true);

      while (imageServiceProviders.hasNext()) {
        next = imageServiceProviders.next();
        try {
          getImageInputStream().reset();
          canDecode = next.canDecodeInput(getImageInputStream());
          getImageInputStream().reset();
        } catch (IOException e) {
          // Why would a method called "canDecodeInput" declare a checked exception?
          // I guess an IOException here means "I cannot decode the input"
        }

        if (canDecode) {
          break;
        }
      }

    } finally {
      resetInputStream();
    }
    Validate.isTrue(
        canDecode,
        "Could not find an image reader. "
            + "Stream closed or not at zero postion? "
            + "Missing JAI reader for image type");

    return next;
  }

  ImageInputStream getImageInputStream() throws IOException {
    if (iis == null) {
      File tempDirectory = Files.createTempDirectory(UUID.randomUUID().toString()).toFile();
      iis = new FileCacheImageInputStream(inputStream, tempDirectory);
    }
    return iis;
  }

  /**
   * Return an ImageReaderSpi or null if none could be found. Some formats, are not part of the base
   * installation. More commonly, there is something wrong with the input stream.
   *
   * @return an SPI compatible with the input image
   */
  ImageReaderSpi getImageReaderSpi() {
    if (imageReaderSpi == null) {
      imageReaderSpi = createImageReaderSpi();
    }
    return imageReaderSpi;
  }

  ImageReader getReader() throws IOException {
    if (reader == null) {
      reader = getImageReaderSpi().createReaderInstance();
      reader.setInput(getImageInputStream());
    }
    return reader;
  }

  void resetInputStream() {
    try {
      inputStream.reset();
    } catch (IOException e) {
      throw new StreamResetException(e);
    }
  }

  @Override
  public void close() {
    dispose();
  }

  public int getImageIndex() {
    return imageIndex;
  }

  public void setImageIndex(int imageIndex) {
    this.imageIndex = imageIndex;
  }
}
