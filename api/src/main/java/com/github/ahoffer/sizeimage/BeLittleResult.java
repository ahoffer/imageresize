package com.github.ahoffer.sizeimage;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

/**
 * This interface defines the results of a resize operation. It can include the resized image and a
 * list of messages collected during the operation. The object receiving the reulst owns them and is
 * free to mutate them as it needed or desired.
 */
public interface BeLittleResult {

  /**
   * The result of the resizing the image. If the resizer operation was successful, the Optional is
   * populated with an Image. If the operation was unsuccessful, the Optional is empty.
   *
   * @return resized image
   */
  Optional<BufferedImage> getOutput();

  /**
   * List of messages created while performing (or attempting to perform) the resize operation.
   * Includes informative messages, warnings, and errors. Errors are indicative of failure.
   *
   * @return List of informative messages, warnings, and errors.
   */
  List<BeLittleMessage> getMessages();

  void addMessage(BeLittleMessage message);

  void setOutput(BufferedImage image);

  boolean succeeded();
}
