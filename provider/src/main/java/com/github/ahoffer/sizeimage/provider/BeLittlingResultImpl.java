package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.BeLittlingMessage;
import com.github.ahoffer.sizeimage.BeLittlingResult;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
}
