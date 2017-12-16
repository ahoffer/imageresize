package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity.ERROR;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.BAD_HEIGHT;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.BAD_WIDTH;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.MISSING_INPUT_STREAM;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SIZER_NAME;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.UNCONFIGURED;

import com.github.ahoffer.sizeimage.BeLittlingMessage;
import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * The primary functionality for all ImageSizers is implemented int this class.
 *
 * <ul>
 *   <li>A configuration map to hold values like width and height, as well as the timeout value
 *   <li>Most keys for configuration values. The keys are defined as class constants
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

  // TODO: Parking these constants here until there is a better place for them
  public static final String PATH_TO_EXECUTABLE = "pathToIExecutable";
  public static final String WINDOWS_EXEC_NAME = "windowsExecName";
  public static final String NIX_EXEC_NAME = "nixExecName";
  public static final String TIMEOUT_SECONDS = "TIMEOUT_SECONDS";
  public static final int DEFAULT_TIMEOUT_SECONDS = 30;
  public static final String MAX_WIDTH = "maxWidth";
  public static final String MAX_HEIGHT = "maxHeight";

  Map<String, String> configuration = new HashMap<>();
  InputStream inputStream;
  List<BeLittlingMessage> messages = new LinkedList<>();
  MessageFactory messageFactory = new MessageFactory();
  BufferedImage output;

  // TODO: Are equals() and hashCode() still needed?
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other != null
        && getClass() == other.getClass()
        && configuration.equals(((AbstractImageSizer) other).configuration);
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
   * <p>The MessageFactory and the ImageReaderShortcuts have no state and there do not need to
   * discarded. *
   */
  void cleanup() {
    inputStream = null;
    output = null;
    messages = Collections.EMPTY_LIST;
  }

  /**
   * Process Input is one stage of the generate() method. Its default implementation is to do
   * nothing. Subclasses that override this method should create anything that that the
   * generateOutput() operation needs to perform its work. This can include writing files to
   * storage, reading the input stream to get metadata, or anything that may encounter an ERROR
   * condition before the long process of resizing the image is started.
   */
  void processInput() {
    // Do nothing. Subclasses may override.
  }

  /**
   * This class defines a pattern for the API method generate(). The pattern is:
   *
   * <ol>
   *   <li>Call prepare()
   *   <li>Call canProceed(). If true, go to the next step.
   *   <li>Call processInput()
   *   <li>Call canProceed(). If true, go to the next step.
   *   <li>Call generateOutput
   *   <li>Create results object
   *   <li>Call cleanup()
   *   <li>Return results object to caller
   * </ol>
   *
   * This logic is wrapped in a lambda and given a certain amount of (wall-clock) time to complete.
   *
   * @return BelittlingResult
   */
  public BeLittlingResult generate() {
    BeLittlingResult result = null;
    try {
      doWithTimeout(
          () -> {
            prepare();

            if (canProceed()) {
              processInput();
            }
            if (canProceed()) {
              generateOutput();
            }
            return output;
          });
    } finally {
      result = new BeLittlingResultImpl(output, messages);
      cleanup();
    }
    return result;
  }

  /**
   * This method should generate a resized BufferedImage and assign it to the field "output". The
   * "output" is used to create the result object.
   *
   * <p>This method is intended to represent the longest running, most resource demanding part of
   * the resize process. It should begin when no other preparation or processing can generate an
   * error without actually reading to the end of the input stream.
   */
  void generateOutput() {
    // Do nothing. Subclasses should override.
  };

  @Override
  public int hashCode() {
    return configuration.hashCode();
  }

  public ImageSizer setOutputSize(int maxWidth, int maxHeight) {
    configuration.put(MAX_WIDTH, Integer.toString(maxWidth));
    configuration.put(MAX_HEIGHT, Integer.toString(maxHeight));
    return this;
  }

  /**
   * Get the configuration. It is a map with string keys and string values. It should either be a
   * copy or an unmodifiable view of the actuall configuration.
   *
   * @return
   */
  public Map<String, String> getConfiguration() {
    return Collections.unmodifiableMap(configuration);
  }

  /**
   * Set the configuration. Copy the input to avoid side-effects.
   *
   * @param configuration
   */
  public void setConfiguration(Map configuration) {
    Map newConfiguration = new HashMap();
    if (configuration != null) {
      newConfiguration = new HashMap<>(configuration);
    }
    this.configuration = newConfiguration;
  }

  /**
   * Public method to allow cooperating classes to add messages
   *
   * @param message
   * @return this
   */
  public ImageSizer addMessage(BeLittlingMessage message) {
    messages.add(message);
    return this;
  }

  /**
   * This message returns true if there are no errors. If there are errors, it returns false.
   * Messages with a severity of ERROR always indicate that sometime unrecoverable has happened and
   * that it makes no sense to continue this object's primary operation.
   *
   * @return true if no errors
   */
  protected boolean canProceed() {
    return messages.stream().noneMatch(message -> message.getSeverity() == ERROR);
  }

  /**
   * The prepare() method is the first step in the generate() process. It's purpose is to validate
   * input, read metadata. It is a place to add ERROR messages to prevent further processing if the
   * input cannot be validated. It is also a place to add informational messages and warnings.
   */
  void prepare() {

    stampNameOnResults();

    // TODO: Create a rule class to encapsulate the rules. It accepts a sizer and can call
    // TODO: the sizer's addMessage method. Probably use visitor pattern.
    if (getMaxWidth() < 1) {
      addMessage(messageFactory.make(BAD_WIDTH, getMaxWidth()));
    }
    if (getMaxHeight() < 1) {
      addMessage(messageFactory.make(BAD_HEIGHT, getMaxHeight()));
    }
    if (Objects.isNull(inputStream)) {
      addMessage(messageFactory.make(MISSING_INPUT_STREAM));
    }
  }

  public int getMaxWidth() {
    return getInteger(configuration.get(MAX_WIDTH));
  }

  int getInteger(String intString) {
    return Integer.valueOf(intString);
  }

  public int getMaxHeight() {
    return getInteger(configuration.get(MAX_HEIGHT));
  }

  /**
   * Clone this prototype object to create a fresh, usable instance of the class. Throws an
   * exception if the cloning fails. It carries over the prototype's configuration, but nothing
   * else. Subclasses are NOT expected this method will be overridden.
   *
   * @return
   */
  public ImageSizer getNew() {
    try {
      // This works in some situations where clone() does not
      ImageSizer newInstance = getClass().newInstance();
      newInstance.setConfiguration(configuration);
      return newInstance;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new CopyObjectException(e);
    }
  }

  public ImageSizer setInput(InputStream inputStream) {
    this.inputStream = inputStream;
    return this;
  }

  /** Adds informational message that gives the class name of the sizer */
  protected void stampNameOnResults() {
    addMessage(messageFactory.make(SIZER_NAME, this.getClass().getSimpleName()));
  }

  /** Custom exception thrown when this object cannot be cloned */
  public class CopyObjectException extends RuntimeException {
    CopyObjectException(Throwable cause) {
      super(cause);
    }
  }

  /**
   * Execute a Consumer function (i.e. a Callable) and return its value. The execution is blocking
   * (synchronous). If the operation does not complete in the give amount of time, a message with
   * severity of ERROR is added to the list of messages on this ImageSizer and control returns to
   * the ImageSizer
   *
   * @param callable
   * @return return value of the callable
   */

  //  TODO
  // This is solution has a lot of overhead because it creates and shutsdown and executor for every
  // task. In an environment where the system is processing thousands of images, this could become
  // an issue. For high processing demands, the worker should d submit task to a thread pool. Not
  // sure how to do that. Simplest thing would be to put a static var on worker class.
  <T> T doWithTimeout(Callable<T> callable) {

    LittleWorker worker = new LittleWorker(this, getTimeoutSeconds(), TimeUnit.SECONDS);
    T result;
    try {
      result = worker.doThis(callable);
    } finally {
      worker.shutdownNow();
    }
    return result;
  }

  /**
   * Return the number of seconds (wall-clock time) the ImageSizer has for it's generate() method.
   * If the configuration object is null, the DEFAULT_TIMEOUT_SECONDS defined by the class is used.
   * If the configuration exists, but the TIMEOUT_SECONDS is not defined, add the default value to
   * the current configuration. Otherwise, take the value from the configuration.
   *
   * @return number of seconds to run the object's generate() method
   */
  @Override
  public int getTimeoutSeconds() {
    if (configuration == null) {
      addMessage(
          messageFactory.make(
              UNCONFIGURED, "Object has no configuration. Using default timeout value."));

      return DEFAULT_TIMEOUT_SECONDS;

    } else if (getConfiguration().containsKey(TIMEOUT_SECONDS)) {
      return Integer.parseInt(getConfiguration().get(TIMEOUT_SECONDS));
    } else {
      addMessage(
          messageFactory.make(
              UNCONFIGURED,
              "Object has configuration, but is missing "
                  + TIMEOUT_SECONDS
                  + ". Adding default value to the object's configuration"));
      setTimeoutSeconds(DEFAULT_TIMEOUT_SECONDS);
    }

    return Integer.parseInt(getConfiguration().get(TIMEOUT_SECONDS));
  }

  /**
   * Set the number of seconds allotted to the object's generate() method.
   *
   * @param seconds
   * @return this
   */
  // TODO: Validate timeout >=0
  @Override
  public ImageSizer setTimeoutSeconds(int seconds) {
    HashMap<String, String> cfg = new HashMap<>(getConfiguration());
    cfg.put(TIMEOUT_SECONDS, String.valueOf(seconds));
    setConfiguration(cfg);
    return this;
  }
}
