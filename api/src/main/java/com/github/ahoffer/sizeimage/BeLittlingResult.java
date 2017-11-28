package com.github.ahoffer.sizeimage;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

public interface BeLittlingResult {

  Optional<BufferedImage> getOutput();

  List<BeLittlingMessage> getMessages();
}
