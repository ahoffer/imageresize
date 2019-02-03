package belittle;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BeLittleResultImpl implements BeLittleResult {

  List<BeLittleMessage> messages = Collections.EMPTY_LIST;
  BufferedImage output;

  @SuppressWarnings("unused")
  public BeLittleResultImpl() {}

  public BeLittleResultImpl(BufferedImage output, List<BeLittleMessage> messages) {
    this.output = output;
    this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
  }

  public void addMessage(BeLittleMessage message) {
    messages.add(message);
  }

  @Override
  public Optional<BufferedImage> getOutput() {
    return Optional.ofNullable(output);
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
    return getOutput().isPresent();
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
