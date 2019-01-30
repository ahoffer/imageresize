package com.github.ahoffer.sizeimage.support;

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
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;

public class SaferImageReader implements AutoCloseable {

  // Bytes in a stream will be buffered when read, up the the read limit.
  // The goal is to allow the reader to read an image file's metadata and then
  // allow the entire image to be decoded later.
  // TODO: It seems like 1K should be enough buffer to read metadata, but I really don't know.
  public static final int READLIMIT = 1024 * 4;
  protected int imageIndex;
  protected InputStream inputStream;
  ImageInputStream iis;
  ImageReader reader;
  ImageReaderSpi imageReaderSpi;
  ImageReadParam readParam;

  public SaferImageReader(InputStream inputStream) {

    if (inputStream == null) {
      throw new ImageReaderError("Input stream cannot be null");
    }

    if (!inputStream.markSupported()) {
      throw new ImageReaderError("Input stream must support mark and reset");
    }
    setInputStream(inputStream);
    getInputStream().mark(READLIMIT);
    setImageIndex(0);
  }

  public SaferImageReader(InputStream inputStream, ImageReadParam readParam) {
    this(inputStream);
    this.readParam = readParam;
  }

  public List<String> getMimeTypes() {
    return Arrays.asList(getImageReaderSpi().getMIMETypes());
  }

  public void dispose() {
    //       http://info.michael-simons.eu/2012/01/25/the-dangers-of-javas-imageio/
    //       "ImageReader caches some data in files. It is essential to dispose the reader
    //       and the underlying ImageInputStream if it’s not needed anymore..."
    closeCurrentIis();

    if (reader != null) {
      reader.dispose();
    }
  }

  private void closeCurrentIis() {
    if (iis != null) {
      try {
        iis.close();
      } catch (IOException e) {
        // Failed to close, but nothing can be done about it.
      } finally {
        iis = null;
      }
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
    int subsamplingXOffset = 0;
    int subsamplingYOffset = 0;
    getImageReadParam()
        .setSourceSubsampling(
            sourceXSubsampling, sourceYSubsampling, subsamplingXOffset, subsamplingYOffset);
    return true;
  }

  public ImageReadParam getImageReadParam() {
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
      // Not sure why, but the  IIS stream cannot be used after finding the SPI. The methods reset()
      // and seek(0) do not put the stream in valid state to read height and width.
      closeCurrentIis();
      resetInputStream();
    }
    if (!canDecode) {
      throw new ImageReaderError(
          "Could not find an image reader. "
              + "Stream closed or not at zero postion? "
              + "Missing JAI reader for image type");
    }
    return next;
  }

  ImageInputStream getImageInputStream() throws IOException {
    if (iis == null) {
      File tempDirectory = Files.createTempDirectory(UUID.randomUUID().toString()).toFile();
      iis = new FileCacheImageInputStream(getInputStream(), tempDirectory);
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

  ImageReader getReader() {
    if (reader == null) {
      try {
        reader = getImageReaderSpi().createReaderInstance();
        reader.setInput(ImageIO.createImageInputStream(getInputStream()));
      } catch (IOException e) {
        throw new ImageReaderError(e);
      }
    }
    return reader;
  }

  void resetInputStream() {
    try {
      getInputStream().reset();
    } catch (IOException e) {
      throw new ImageReaderError(e);
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

  protected InputStream getInputStream() {
    return inputStream;
  }

  protected void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }
}
