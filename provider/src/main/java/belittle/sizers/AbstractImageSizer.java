package belittle.sizers;

import belittle.BeLittleMessage;
import belittle.BeLittleMessage.BeLittleSeverity;
import belittle.BeLittleResult;
import belittle.BeLittleResultImpl;
import belittle.ImageInputFile;
import belittle.ImageSizer;
import java.awt.image.BufferedImage;

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

  BeLittleResult result;
  int height, width;

  public BufferedImage resize(int width, int height, ImageInputFile file) {
    result = new BeLittleResultImpl();
    this.width = width;
    this.height = height;
    return resize(file);
  }

  public abstract BufferedImage resize(ImageInputFile file);

  public BeLittleResult getResult() {
    return result;
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

  public abstract boolean isAvailable();
}
