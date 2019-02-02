package com.github.ahoffer.sizeimage.sizers;

import static com.github.ahoffer.sizeimage.BeLittleConstants.MIME_TYPE;
import static com.github.ahoffer.sizeimage.support.MessageConstants.SIZER_NAME;

import com.github.ahoffer.sizeimage.BeLittleSizerSetting;
import com.github.ahoffer.sizeimage.BeLittleSizerSettingImpl;
import com.github.ahoffer.sizeimage.BeLittleMessage;
import com.github.ahoffer.sizeimage.BeLittleResult;
import com.github.ahoffer.sizeimage.BeLittleResultImpl;
import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.support.MessageFactory;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

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
 * This class also defines a pattern for the API method generate(). See the documentation for that
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

  public ImageSizer getNew() {
    return getNew(sizerSetting);
  }

  /**
   * This message is called at the end of the generate() method. It exists to close, shutdown, or
   * otherwise put the object into a completed state. It does not close the input stream. The input
   * stream was not created by this object and therefore this object should not be responsible for
   * closing that stream.
   *
   * <p>It is not intended that this object be reused after generate() completes. This object should
   * be cloned using the getNew() method and the new instance should be used for any additional
   * work.
   *
   * <p>The MessageFactory has no state and does not need to discarded.
   */
  void cleanup() {
    result = null;
  }

  /**
   * The prepare() method is the first step in the generate() process. It's purpose is to validate
   * input, read metadata. It is a place to add ERROR messages to prevent further processing if the
   * input cannot be validated. It is also a place to add informational messages and warnings.
   */
  void prepare() {

    stampNameOnResults();

    // TODO MOVE VALIDATION OF SIZER SETTINGS TO THE BeLittleFactory CLASS

    // old TODO: Create a rule class to encapsulate the rules. It accepts a sizer and can call
    // old TODO: the sizer's addMessage method. Probably use visitor pattern.
    //    if (getMaxWidth() < 1) {
    //      addMessage(messageFactory.make(BAD_WIDTH, getMaxWidth()));
    //    }
    //    if (getMaxHeight() < 1) {
    //      addMessage(messageFactory.make(BAD_HEIGHT, getMaxHeight()));
    //    }
    //    if (Objects.isNull(inputStream)) {
    //      addMessage(messageFactory.make(MISSING_INPUT_STREAM));
    //    }
  }

  public ImageReader getImageReaderByMIMEType() {
    Iterator<ImageReader> it =
        ImageIO.getImageReadersByMIMEType(sizerSetting.getProperty(MIME_TYPE));
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
}
