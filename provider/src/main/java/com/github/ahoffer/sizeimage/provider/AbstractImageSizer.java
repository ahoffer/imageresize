package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.BAD_HEIGHT;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.BAD_WIDTH;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SIZER_NAME;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.UNCONFIGURED;

import com.github.ahoffer.sizeimage.BeLittlingMessage;
import com.github.ahoffer.sizeimage.BeLittlingMessage.BeLittlingSeverity;
import com.github.ahoffer.sizeimage.ImageSizer;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public abstract class AbstractImageSizer implements ImageSizer {

  // TODO: Temporarily parking these constants until there is a better place for them
  public static final String PATH_TO_EXECUTABLE = "pathToIExecutable";
  public static final String WINDOWS_EXEC_NAME = "windowsExecName";
  public static final String NIX_EXEC_NAME = "nixExecName";
  public static final String TIMEOUT_SECONDS = "TIMEOUT_SECONDS";

  public static final String MAX_WIDTH = "maxWidth";
  public static final String MAX_HEIGHT = "maxHeight";
  protected Map<String, String> configuration = new HashMap<>();
  protected InputStream inputStream;
  protected List<BeLittlingMessage> messages = new LinkedList<>();
  protected MessageFactory messageFactory = new MessageFactory();
  ImageReaderShortcuts shortcuts = new ImageReaderShortcuts();
  protected LittleWorker worker;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    return configuration.equals(((AbstractImageSizer) other).configuration);
  }

  protected void cleanup() {
    inputStream = null;
    messages = Collections.EMPTY_LIST;
  }

  @Override
  public int hashCode() {
    return configuration.hashCode();
  }

  public ImageSizer setOutputSize(int maxWidth, int maxHeight) {
    configuration.put(MAX_WIDTH, Integer.toString(maxWidth));
    configuration.put(MAX_HEIGHT, Integer.toString(maxHeight));
    return this;
  }

  public Map<String, String> getConfiguration() {
    return Collections.unmodifiableMap(configuration);
  }

  public void setConfiguration(Map configuration) {
    Map newConfiguration = new HashMap();
    if (configuration != null) {
      newConfiguration = new HashMap<>(configuration);
    }
    this.configuration = newConfiguration;
  }

  public ImageSizer addMessage(BeLittlingMessage message) {
    messages.add(message);
    return this;
  }

  protected boolean canProceed() {
    return messages
        .stream()
        .noneMatch(message -> message.getSeverity() == BeLittlingSeverity.ERROR);
  }

  protected boolean endorse() {
    // TODO: Create a rule class to encapsulate the rules. It accepts a sizer and can call
    // TODO: the sizer's addMessage method. Probably use visitor pattern.
    if (getMaxWidth() < 1) {
      addMessage(messageFactory.make(BAD_WIDTH, getMaxWidth()));
    }
    if (getMaxHeight() < 1) {
      addMessage(messageFactory.make(BAD_HEIGHT, getMaxHeight()));
    }
    if (Objects.isNull(inputStream)) {
      addMessage(messageFactory.make(MessageConstants.MISSING_INPUT_STREAM));
    }
    return canProceed();
  }

  public int getMaxWidth() {
    return getInteger(configuration.get(MAX_WIDTH));
  }

  private int getInteger(String intString) {
    return Integer.valueOf(intString);
  }

  public int getMaxHeight() {
    return getInteger(configuration.get(MAX_HEIGHT));
  }

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

  protected void stampNameOnResults() {
    addMessage(messageFactory.make(SIZER_NAME, this.getClass().getSimpleName()));
  }

  LittleWorker getWorker() {
    if (worker == null) {
      worker = new LittleWorker(this, getTimeoutSeconds(), TimeUnit.SECONDS);
    }
    return worker;
  }

  public class CopyObjectException extends RuntimeException {

    CopyObjectException(Throwable cause) {
      super(cause);
    }
  }

  <T> T doWithTimeout(Callable<T> callable) {
    return getWorker().doThis(callable);
  }

  long getTimeoutSeconds() {
    if (configuration == null) {
      addMessage(
          messageFactory.make(
              MessageConstants.UNCONFIGURED,
              "Object has no configuration. Using default timeout value."));

      return 60L;
    } else if (getConfiguration().containsKey(TIMEOUT_SECONDS)) {
      return Long.parseLong(getConfiguration().get(TIMEOUT_SECONDS));
    } else {
      addMessage(
          messageFactory.make(
              UNCONFIGURED,
              "Object has configuration, but is missing "
                  + TIMEOUT_SECONDS
                  + ". Adding default value to the object's configuration"));
      HashMap<String, String> cfg = new HashMap<>(getConfiguration());
      cfg.put(TIMEOUT_SECONDS, "60");
      setConfiguration(cfg);
    }

    String timeout = getConfiguration().getOrDefault(TIMEOUT_SECONDS, "60");
    return Long.parseLong(timeout);
  }
}
