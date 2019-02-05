package belittle.sizers;

import static belittle.support.MessageConstants.SIZER_NAME;

import belittle.BeLittleMessage;
import belittle.BeLittleMessage.BeLittleSeverity;
import belittle.BeLittleMessageImpl;
import belittle.BeLittleResult;
import belittle.BeLittleResultImpl;
import belittle.BeLittleSizerSetting;
import belittle.BeLittleSizerSettingImpl;
import belittle.ImageSizer;
import belittle.support.IoConsumer;
import belittle.support.MessageFactory;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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

  MessageFactory messageFactory = new MessageFactory();
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

  public ImageReader getImageReaderByMIMEType(String mimeType) {
    Iterator<ImageReader> it = ImageIO.getImageReadersByMIMEType(mimeType);
    if (it.hasNext()) {
      return it.next();
    } else {
      return null;
    }
  }

  public void addMessage(BeLittleMessage message) {
    getResult().addMessage(message);
  }

  /** Adds informational message that gives the class name of the sizer */
  protected void stampNameOnResults() {
    addMessage(messageFactory.make(SIZER_NAME, this.getClass().getSimpleName()));
  }

  protected void closeImageInputStream(ImageInputStream iis) {
    if (iis != null) {
      try {
        iis.close();
      } catch (IOException e) {
        addMessage(new BeLittleMessageImpl("", BeLittleSeverity.WARNING, e));
      }
    }
  }

  protected void closeImageReader(ImageReader reader) {
    if (reader != null) {
      reader.dispose();
    }
  }

  void doWithInputStream(File file, IoConsumer<InputStream> consumer) {
    try (InputStream istream = Files.asByteSource(file).openStream()) {
      consumer.accept(istream);
    } catch (IOException e) {
      addMessage(new BeLittleMessageImpl("IO", BeLittleSeverity.ERROR, e));
    }
  }

  void doWithImageInputStream(File file, IoConsumer<ImageInputStream> consumer) {
    doWithInputStream(
        file,
        (istream) -> {
          ImageInputStream iis = null;
          try {
            iis = ImageIO.createImageInputStream(istream);
            consumer.accept(iis);
            // The JAI JPEG 2000 library can sometimes uses RuntimeException in place  IOException
          } catch (IOException | RuntimeException e) {
            addMessage(new BeLittleMessageImpl("IO", BeLittleSeverity.ERROR, e));
          } finally {
            closeImageInputStream(iis);
          }
        });
  }
}
