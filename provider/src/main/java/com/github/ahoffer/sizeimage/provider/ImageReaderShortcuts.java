package com.github.ahoffer.sizeimage.provider;

import static javax.imageio.ImageIO.createImageInputStream;

import com.github.ahoffer.sizeimage.provider.BeLittle.ImageReaderException;
import com.github.ahoffer.sizeimage.provider.BeLittle.StreamResetException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
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

  /**
   * Keep the cleanup code here so the client does not have to worry about it. Does not attempt to
   * mark, reset, or close the stream passed to it.
   *
   * @param inputStream
   * @param consumers
   * @throws IOException
   */
  public void executeWithReader(InputStream inputStream, IoConsumer<ImageReader>... consumers) {
    try {
      ImageInputStream iis = createImageInputStream(inputStream);
      ImageReader reader = getFirstImageReaderSpi(inputStream).createReaderInstance();
      reader.setInput(iis);
      try {
        Arrays.asList(consumers)
            .forEach(
                consumer -> {
                  try {
                    inputStream.mark(READLIMIT);
                    consumer.accept(reader);
                    inputStream.reset();
                  } catch (IOException e) {
                    throw new BeLittle.ImageReaderException(e);
                  }
                });
      } finally {
        //       http://info.michael-simons.eu/2012/01/25/the-dangers-of-javas-imageio/
        //       "ImageReader caches some data in files. It is essential to dispose the reader
        //       and the underlying ImageInputStream if it’s not needed anymore..."
        ((ImageInputStream) reader.getInput()).close();
        reader.dispose();
      }
    } catch (IOException e) {
      // Do throw a checked exception like IOException because it wreaks havoc with lambdas.
      throw new ImageReaderException(e);
    }
  }

  /**
   * Returns the MIME types as read decoding the image file header
   *
   * @param source
   * @return
   * @throws StreamResetException
   */
  public List<String> getMimeTypes(InputStream source) throws StreamResetException {
    return Arrays.asList(getFirstImageReaderSpi(source).getMIMETypes());
  }

  /**
   * Return an ImageReaderSpi or null if none could be found. Some formats, like TIFF are not part
   * of the base installation. More commonly, there is something wrong with the input stream.
   *
   * @param inputStream
   * @return
   */
  public ImageReaderSpi getFirstImageReaderSpi(InputStream inputStream)
      throws StreamResetException {
    Validate.notNull(inputStream);
    Validate.isTrue(inputStream.markSupported());
    int readlimit = 256;
    inputStream.mark(readlimit);
    ImageReaderSpi next = null;
    try {
      // Mem cache image input stream should be faster than file cached for this use -- we are only
      // reading the first few bytes. Also, the clean up should be easier because there are no temp
      // files.
      MemoryCacheImageInputStream iis = new MemoryCacheImageInputStream(inputStream);

      boolean canDecode = false;
      try {
        Iterator<ImageReaderSpi> imageServiceProviders =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageReaderSpi.class, true);
        while (imageServiceProviders.hasNext()) {
          next = imageServiceProviders.next();
          try {
            iis.reset();
            canDecode = next.canDecodeInput(iis);
            iis.reset();
          } catch (IOException e) {
            // Why would a method called "canDecodeInput" declare a checked exception?
            // I guess an IOException here means "I cannot decode the input"
          }

          if (canDecode) {
            break;
          }
        }

        Validate.isTrue(
            canDecode,
            "Could not find an image reader. "
                + "Stream closed or not at zero postion? "
                + "Missing JAI reader for image type");

      } finally {
        if (iis != null) {
          try {
            iis.close();
          } catch (IOException e) {
            // Should not matter one whit.
          }
        }
      }
    } finally {
      try {
        inputStream.reset();
      } catch (IOException e) {
        // We mangled your input stream and we are very sorry.
        throw new StreamResetException(e);
      }
    }
    return next;
  }

  public ImageReadParam getDefaultImageReadParam(InputStream source) throws IOException {
    return getFirstImageReaderSpi(source).createReaderInstance().getDefaultReadParam();
  }
}
