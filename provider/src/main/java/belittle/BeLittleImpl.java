package belittle;

import static belittle.BeLittleConstants.MIME_TYPE;
import static belittle.BeLittleConstants.UNKNOWN_MIME_TYPE;

import belittle.BeLittleMessage.BeLittlingSeverity;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.imageio.stream.ImageInputStream;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeLittleImpl implements BeLittle {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(BeLittleImpl.class);
  // TODO: Make it so a thread pool can be injected.
  ExecutorService executorService = Executors.newCachedThreadPool();
  //  ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(5);
  Map<String, List<ImageSizer>> sizers;
  BeLittleSizerSetting sizerSetting;

  public BeLittleImpl(Map<String, List<ImageSizer>> sizers, BeLittleSizerSetting sizerSetting) {
    this.sizers = Collections.unmodifiableMap(cloneSizers(sizers));
    this.sizerSetting = new BeLittleSizerSettingImpl(sizerSetting);
  }

  String getMimeType() {
    return sizerSetting.getProperty(MIME_TYPE);
  }

  void setMimeType(String mimeType) {
    sizerSetting.setProperty(MIME_TYPE, mimeType);
  }

  public List<ImageSizer> getSizersForCurrentMimeType() {
    return Collections.unmodifiableList(sizers.get(getMimeType()));
  }

  public List<BeLittleResult> generate(ImageInputStream iis) {
    File file = null;
    try {
      file = File.createTempFile("belittle", null);
      write(iis, file);
      return generate(file);
    } catch (IOException e) {
      LOGGER.info("Failed to create temporary file {}", file.getAbsolutePath());
    } finally {
      if (file != null) {
        // Best effort, no guarantees.
        file.delete();
      }
    }
    return Collections.emptyList();
  }

  /**
   * TODO - Might want a results container so we can return both the first success AND all the
   * failed attempts. Could also prodive methods like "succeeded?", "getSuccessfulResult", and get
   * "getImage" (from successful result).
   */
  public List<BeLittleResult> generate(File file) {
    setMimeType(readMimeType(file));
    ByteSource source = Files.asByteSource(file);

    List<ImageSizer> sizerList = getSizersForCurrentMimeType();

    for (ImageSizer sizer : sizerList) {
      Callable<BeLittleResult> callable = () -> sizer.resize(source.openBufferedStream());
      Future<BeLittleResult> future = executorService.submit(callable);
      try {
        future.get(sizerSetting.getTimeoutSeconds(), TimeUnit.SECONDS);
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        sizer.addMessage(new BeLittleMessageImpl("EXP", BeLittlingSeverity.ERROR, e));
      }
      BeLittleResult result = sizer.getResult();
      if (result.succeeded()) {
        // Return immediately if a sizer succeeds.
        return Collections.singletonList(result);
      }
    }

    // If no sizer succeeds, return results from all sizers.
    return sizerList.stream().map(ImageSizer::getResult).collect(Collectors.toList());
  }

  // This was some partially implemented bad-ass rock star code that
  // I ditched because I don't think it is necessary. The plan was to have the sizers running
  // in parallel and look for the first one to succeed. I think it is an unnecessary drain on
  // system resources. In retrospect, it was just plain over-engineered.
  /*
  public List<BeLittlingResult> generate(File file) {
      setMimeType(readMimeType(file));
      ByteSource source = Files.asByteSource(file);

      //    Set<Callable<String>>
      List<ImageSizer> sizerList = getSizers();
      List<Callable<BeLittlingResult>> callables;

      try {
        callables = sizerList.stream().<Callable<BeLittlingResult>>map(
            sizer -> () -> sizer.resize(source.openBufferedStream())).collect(Collectors.toList());
        List<Future<BeLittlingResult>> futures = executorService.invokeAll(callables);
        futures.forEach(f -> schedulerService
            .schedule(() -> f.cancel(true), sizerSetting.getTimeoutSeconds(), TimeUnit.SECONDS));
        BeLittlingResult result;
        while (futures.stream.matchAll(f -> !f.isDone() ) {
          for (Future<BeLittlingResult> future : futures) {
            try {
              //Look for sizers that are done
              if (future.isDone()) {
                result = future.get();
              if (result.succeeded()) {
                // Cancel other futures and return first success
                futures.forEach(f -> f.cancel(true));
                return Collections.singletonList(result);
              }
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
              LOGGER.info("Unexpected error resizing an image", e);
            }
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      // If all sizers fail, returns all results
      return sizerList.stream().map(ImageSizer::getResult).collect(Collectors.toList());
    }
    */

  /**
   * Helper method to write an InputImageStream to disk. Although the InputImageStream class is a
   * file-back buffered stream, it is relies on mark and reset functions that can be problematic. An
   * image input stream cannot always be reset. Also, it can only serve one reader (output stream)
   * at a time. It is convenient to open new input streams on a file if multiple threads need to
   * read the data.
   *
   * @param iis InputImageStream that represents an image
   * @param file Destination for the IIS bits
   */
  private void write(ImageInputStream iis, File file) throws IOException {
    try (OutputStream outputStream = Files.asByteSink(file).openBufferedStream()) {
      int size = 8 * 1024;
      int len;
      byte[] buffer = new byte[size];
      while ((len = iis.read(buffer)) > 0) {
        outputStream.write(buffer, 0, len);
      }
      outputStream.flush();
    }
  }

  private Map<String, List<ImageSizer>> cloneSizers(Map<String, List<ImageSizer>> original) {
    Map<String, List<ImageSizer>> copy = new HashMap<>(original);
    // Copy the sizer instances
    copy.replaceAll(
        (key, list) -> list.stream().map(ImageSizer::getNew).collect(Collectors.toList()));
    return copy;
  }

  private String readMimeType(File file) {
    Tika tika = new Tika();
    try {
      return tika.detect(file);
    } catch (IOException e) {
      return UNKNOWN_MIME_TYPE;
    }
  }
}
