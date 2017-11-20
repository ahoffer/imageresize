package com.github.ahoffer.sizeimage;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

public interface BeLittlingResult {

  public Optional<BufferedImage> getOutput();

  public List<BeLittlingMessage> getMessages();
}
