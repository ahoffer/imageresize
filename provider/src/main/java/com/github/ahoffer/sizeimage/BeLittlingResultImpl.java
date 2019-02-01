package com.github.ahoffer.sizeimage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BeLittlingResultImpl implements BeLittlingResult {

  List<BeLittlingMessage> messages = Collections.EMPTY_LIST;
  BufferedImage output;

  @SuppressWarnings("unused")
  public BeLittlingResultImpl() {}

  public BeLittlingResultImpl(BufferedImage output, List<BeLittlingMessage> messages) {
    this.output = output;
    this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
  }

  public void addMessage(BeLittlingMessage message) {
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
  public List<BeLittlingMessage> getMessages() {
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
          .map(BeLittlingMessage::toString)
          .collect(Collectors.joining(System.lineSeparator()));
    }
  }
}
