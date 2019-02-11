package belittle;

import com.google.common.io.Files;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;

public class ImageInputFileImpl implements ImageInputFile, Closeable, AutoCloseable {

  public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";
  protected File file;
  protected String mimeType;
  protected boolean manageTempFile = false;

  public ImageInputFileImpl(File sourceFile) {
    this.file = sourceFile;
    initialize();
  }

  public ImageInputFileImpl(ImageInputStream iis, File newFile) throws IOException {
    // Caller's responsibility to delete temp files.
    this.file = newFile;
    write(iis);
  }

  public ImageInputFileImpl(ImageInputStream iis) throws IOException {
    this.file = File.createTempFile("belittle", null);
    write(iis);
    manageTempFile = true;
  }

  protected void initialize() {
    mimeType = readFileMimeType();
  }

  protected String readFileMimeType() {
    Tika tika = new Tika();
    try {
      return tika.detect(file);
    } catch (IOException e) {
      return UNKNOWN_MIME_TYPE;
    }
  }

  public String getMimeType() {
    return mimeType;
  }

  public File getFile() {
    return file;
  }

  @Override
  public boolean copyTo(File destination) {
    try {
      FileUtils.copyFile(file, destination);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean doWithImageInputStream(IoConsumer<ImageInputStream> consumer) {

    AtomicReference<ImageInputStream> iis = new AtomicReference<>();
    try {
      return doWithImageInputStream(
          (istream) -> {
            iis.set(ImageIO.createImageInputStream(istream));
            consumer.accept(iis.get());
          });
      // CAUTION: The JAI JPEG 2000 library sometimes throws a RuntimeException
      // in place of an IOException
    } finally {
      if (iis.get() != null) {
        try {
          iis.get().close();
        } catch (IOException e) {
          // Return false?
        }
      }
    }
  }

  @Override
  public boolean doWithInputStream(IoConsumer<InputStream> consumer) {
    try (InputStream istream = Files.asByteSource(file).openStream()) {
      consumer.accept(istream);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean doWithImageReader(IoConsumer<ImageReader> consumer) {
    ImageReader reader = getImageReaderByMIMEType();
    if (reader == null) {
      reader = getImageReaderByImageContent();
    }
    if (reader == null) {
      return false;
    }
    try {
      AtomicReference<ImageReader> readerAtom = new AtomicReference<>(reader);
      doWithImageInputStream(
          (iis) -> {
            ImageReader thisReader = readerAtom.get();
            thisReader.setInput(iis);
            consumer.accept(thisReader);
          });
    } finally {
      reader.dispose();
    }
    return true;
  }

  /**
   * Helper method to write an InputImageStream to disk. Although the InputImageStream class is a
   * file-back buffered stream, it is relies on mark and reset functions that can be problematic. An
   * image input stream, cannot always be reset. Also, it can only serve one reader (output stream)
   * at a time. It is convenient to open new input streams on a file if multiple threads need to
   * read the data.
   *
   * @param iis InputImageStream that represents an image
   */
  protected void write(ImageInputStream iis) throws IOException {
    try (OutputStream outputStream = Files.asByteSink(file).openBufferedStream()) {
      int size = 8 * 1024;
      int len;
      byte[] buffer = new byte[size];
      while ((len = iis.read(buffer)) > 0) {
        outputStream.write(buffer, 0, len);
      }
      outputStream.flush();
    }
  }

  protected ImageReader getImageReaderByMIMEType() {
    Iterator<ImageReader> it = ImageIO.getImageReadersByMIMEType(mimeType);
    if (it.hasNext()) {
      return it.next();
    } else {
      return null;
    }
  }

  protected ImageReader getImageReaderByImageContent() {
    AtomicReference<ImageReader> next = new AtomicReference<>();
    doWithImageInputStream(
        (iis) -> {
          Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
          if (it.hasNext()) {
            next.set(it.next());
          }
        });
    return next.get();
  }

  @Override
  public void close() {
    if (manageTempFile && file != null) {
      // Best effort, no guarantees
      file.delete();
    }
  }
}
