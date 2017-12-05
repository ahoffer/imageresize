package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.BeLittlingMessage;
import com.github.ahoffer.sizeimage.BeLittlingResult;
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
  private BeLittlingResultImpl() {}

  public BeLittlingResultImpl(BufferedImage output, List<BeLittlingMessage> messages) {
    this.output = output;
    this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
  }

  @Override
  public Optional<BufferedImage> getOutput() {
    return Optional.ofNullable(output);
  }

  @Override
  public List<BeLittlingMessage> getMessages() {
    return messages;
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
