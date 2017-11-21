package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.MessageConstants.BAD_HEIGHT;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.BAD_WIDTH;
import static com.github.ahoffer.sizeimage.provider.MessageConstants.SIZER_NAME;

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

public abstract class AbstractImageSizer implements ImageSizer {

  public static final String MAX_WIDTH = "maxWidth";
  public static final String MAX_HEIGHT = "maxHeight";
  protected Map<String, String> configuration = new HashMap<>();
  protected InputStream inputStream;
  protected List<BeLittlingMessage> messages = new LinkedList<>();
  protected MessageFactory messageFactory = new MessageFactory();

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

  public void setConfiguration(Map<String, String> configuration) {

    if (configuration == null) {
      this.configuration = new HashMap();
    } else {
      this.configuration = new HashMap<>(configuration);
    }
    // This was too pedantic to keep, buut it is adorable. So here:
    //    this.configuration =
    //        (Map)
    //            Optional.ofNullable(configuration)
    //                .map((Function<Map<String, String>, HashMap>) HashMap::new)
    //                .orElseGet(HashMap::new);
  }

  protected void addMessage(BeLittlingMessage message) {
    messages.add(message);
  }

  protected boolean canProceedToGenerateImage() {
    return messages.stream().anyMatch(message -> message.getSeverity() == BeLittlingSeverity.ERROR);
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
    return canProceedToGenerateImage();
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

  public class CopyObjectException extends RuntimeException {

    CopyObjectException(Throwable cause) {
      super(cause);
    }
  }
}
