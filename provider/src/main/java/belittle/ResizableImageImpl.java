package belittle;

import belittle.BeLittleMessage.BeLittleSeverity;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResizableImageImpl implements ResizableImage, Closeable, AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResizableImageImpl.class);
  ImageInputFile file;
  List<BeLittleResult> results;
  ImageSizerCollection sizerCollection;
  private int width, height, timeoutSeconds;

  public ResizableImageImpl(ImageSizerCollection sizerCollection) {
    this.sizerCollection = sizerCollection;
  }

  public BufferedImage resize() {
    results = new ArrayList<>();
    String mimeType = file.getMimeType();
    List<ImageSizer> sizerList = sizerCollection.getSizersForMimeType(mimeType);
    for (ImageSizer sizer : sizerList) {
      BeLittleResult result = sizer.getResult();
      results.add(result);
      //      Callable<BeLittleResult> callable = () -> sizer.resize(source.openBufferedStream());
      //      Future<BeLittleResult> future = executorService.submit(callable);
      try {
        sizer.resize(width, height, file);
        //        future.get(sizerSettings.getTimeoutSeconds(), TimeUnit.SECONDS);
      } catch (
          /*InterruptedException | ExecutionException | TimeoutException |*/ RuntimeException e) {
        sizer.addMessage(new BeLittleMessageImpl("Uncaught exception", BeLittleSeverity.ERROR, e));
      }
      if (result.succeeded()) {
        // Return immediately if a sizer succeeds.
        return result.getOutput();
      }
    }

    // If no sizer succeeds, return results from all sizers.
    // return sizerList.stream().map(ImageSizer::getResult).collect(Collectors.toList());
    return null;
  }

  @Override
  public List<BeLittleResult> getResults() {
    return results;
  }

  @Override
  public ImageInputFile getInputFile() {
    return file;
  }

  @Override
  public ResizableImage setInputFile(ImageInputFile file) {
    this.file = file;
    return this;
  }

  @Override
  public void close() {
    if (file != null) {
      try {
        file.close();
      } catch (Exception e) {
        LOGGER.info("Maybe have failed to delete temproary file", e);
      }
    }
    file = null;
    results = null;
  }

  @Override
  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }

  @Override
  public ResizableImage setTimeoutSeconds(int seconds) {
    timeoutSeconds = seconds;
    return this;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public ResizableImage setWidth(int width) {
    this.width = width;
    return this;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public ResizableImage setHeight(int height) {
    this.height = height;
    return this;
  }
}
