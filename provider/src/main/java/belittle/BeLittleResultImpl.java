package belittle;

import belittle.BeLittleMessage.BeLittleSeverity;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BeLittleResultImpl implements BeLittleResult {

  List<BeLittleMessage> messages = new ArrayList<>();
  BufferedImage output;

  @SuppressWarnings("unused")
  public BeLittleResultImpl() {}

  public void addMessage(BeLittleMessage message) {
    messages.add(message);
  }

  @Override
  public void addMessage(BeLittleSeverity severity, String description, Throwable throwable) {
    addMessage(new BeLittleMessageImpl(severity, description, throwable));
  }

  @Override
  public BufferedImage getOutput() {
    return output;
  }

  @Override
  // This method should only be called by an ImageSizer
  public void setOutput(BufferedImage image) {
    output = image;
  }

  @Override
  public List<BeLittleMessage> getMessages() {
    return messages;
  }

  @Override
  public boolean succeeded() {
    return getOutput() != null;
  }

  public String toString() {
    if (messages.isEmpty()) {
      return "No Messages";
    } else {
      return messages
          .stream()
          // For now, I want to see the messages in the order they were added
          // .sorted(Comparator.comparing(BeLittlingMessage::getSeverity).reversed())
          .map(BeLittleMessage::toString)
          .collect(Collectors.joining(System.lineSeparator()));
    }
  }
}
