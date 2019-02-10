package belittle.sizers;

import belittle.BeLittleConstants;
import belittle.BeLittleMessage;
import belittle.BeLittleMessage.BeLittleSeverity;
import belittle.BeLittleResult;
import belittle.BeLittleResultImpl;
import belittle.BeLittleSizerSetting;
import belittle.BeLittleSizerSettingImpl;
import belittle.ImageSizer;
import belittle.IoConsumer;
import belittle.support.BeLittleUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * The primary functionality for all ImageSizers is implemented int this class.
 *
 * <ul>
 *   <li>A settings map to hold values like width and height, as well as the timeout value
 *   <li>Most keys for settings values. The keys are defined as class constants
 *   <li>A list of messages to be returned with the results
 *   <li>Ability to add new messages to the message list
 *   <li>Ability to determine if operation is successful up to current point
 *   <li>An instance variable to hold the input stream that represents the input image
 *   <li>References to helper classes like a message factory and others
 *   <li>An instance variable to hold the generated, output image
 *   <li>Ability to limit operations to a configurable amount of time
 * </ul>
 *
 * This class also defines a pattern for the API method resize(). See the documentation for that
 * method.
 *
 * <p>
 */
public abstract class AbstractImageSizer implements ImageSizer {

  BeLittleSizerSetting sizerSetting;
  BeLittleResult result;

  public AbstractImageSizer(BeLittleSizerSetting sizerSettings) {
    this.sizerSetting = new BeLittleSizerSettingImpl(sizerSettings);
    result = new BeLittleResultImpl();
  }

  public BeLittleResult getResult() {
    return result;
  }

  @Override
  public BeLittleResult resize(File file, String mimeType) {
    return resize(file);
  }

  /**
   * This message is called at the end of the resize() method. It exists to close, shutdown, or
   * otherwise put the object into a completed state. It does not close the input stream. The input
   * stream was not created by this object and therefore this object should not be responsible for
   * closing that stream.
   *
   * <p>It is not intended that this object be reused after resize() completes. This object should
   * be cloned using the getNew() method and the new instance should be used for any additional
   * work.
   *
   * <p>The MessageFactory has no state and does not need to discarded.
   */
  void cleanup() {
    result = null;
  }

  void prepare() {
    stampNameOnResults();
  }

  public ImageReader getImageReader(File file, String mimeType) {
    if (BeLittleConstants.UNKNOWN_MIME_TYPE.equals(mimeType)) {
      return getImageReaderByImageContent(file);
    } else {
      return getImageReaderByMIMEType(mimeType);
    }
  }

  public ImageReader getImageReaderByMIMEType(String mimeType) {
    Iterator<ImageReader> it = ImageIO.getImageReadersByMIMEType(mimeType);
    if (it.hasNext()) {
      return it.next();
    } else {
      addInfo("Could not get image reader from MIME type %s".format(mimeType));
      return null;
    }
  }

  public ImageReader getImageReaderByImageContent(File file) {
    AtomicReference<ImageReader> next = new AtomicReference<>();
    try {
      new BeLittleUtil()
          .doWithImageInputStream(
              file,
              (iis) -> {
                Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
                if (it.hasNext()) {
                  next.set(it.next());
                  return;
                }
              });
    } catch (IOException e) {
      addWarning("Could not create image reader from file type", e);
    }
    return next.get();
  }

  public void addMessage(BeLittleMessage message) {
    getResult().addMessage(message);
  }

  public void addError(String description, Throwable throwable) {
    getResult().addMessage(BeLittleSeverity.ERROR, description, throwable);
  }

  public void addWarning(String description, Throwable throwable) {
    getResult().addMessage(BeLittleSeverity.WARNING, description, throwable);
  }

  public void addInfo(String description, Throwable throwable) {
    getResult().addMessage(BeLittleSeverity.INFO, description, throwable);
  }

  public void addError(String description) {
    getResult().addMessage(BeLittleSeverity.ERROR, description, null);
  }

  public void addWarning(String description) {
    getResult().addMessage(BeLittleSeverity.WARNING, description, null);
  }

  public void addInfo(String description) {
    getResult().addMessage(BeLittleSeverity.INFO, description, null);
  }

  /** Adds informational message that gives the class name of the sizer */
  protected void stampNameOnResults() {
    addInfo(this.getClass().getSimpleName());
  }

  protected void closeImageReader(ImageReader reader) {
    if (reader != null) {
      reader.dispose();
    }
  }

  void doWithInputStream(File file, IoConsumer<InputStream> consumer) {
    try {
      new BeLittleUtil().doWithInputStream(file, consumer);
    } catch (IOException e) {
      addWarning("Failed to perform action with input stream", e);
    }
  }

  void doWithImageInputStream(File file, IoConsumer<ImageInputStream> consumer) {
    try {
      new BeLittleUtil()
          .doWithImageInputStream(
              file,
              (istream) -> {
                ImageInputStream iis = null;
                iis = ImageIO.createImageInputStream(istream);
                consumer.accept(iis);
              });
      // The JAI JPEG 2000 library can sometimes uses RuntimeException in place  IOException
    } catch (IOException | RuntimeException e) {
      addWarning("Failed to perform action with image input stream", e);
    }
  }

  public abstract boolean isAvailable();
}
