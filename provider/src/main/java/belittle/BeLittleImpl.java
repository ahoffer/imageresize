package belittle;

import static belittle.BeLittleConstants.UNKNOWN_MIME_TYPE;

import belittle.BeLittleMessage.BeLittleSeverity;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.imageio.stream.ImageInputStream;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeLittleImpl implements BeLittle {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(BeLittleImpl.class);
  // TODO: Make it so a thread pool can be injected.
  //  ExecutorService executorService = Executors.newCachedThreadPool();
  //  ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(5);
  Map<String, List<ImageSizer>> sizers;
  BeLittleSizerSetting sizerSetting;

  public BeLittleImpl(Map<String, List<ImageSizer>> sizers, BeLittleSizerSetting sizerSetting) {
    // The order of these statements matters because sizer settings must be set before
    // calling cloneSizers()
    this.sizerSetting = new BeLittleSizerSettingImpl(sizerSetting);
    this.sizers = Collections.unmodifiableMap(cloneSizers(sizers));
  }

  public List<ImageSizer> getSizersForMimeType(String mimeType) {
    Optional<String> firstMatch =
        sizers.keySet().stream().filter(regex -> mimeType.matches(regex)).findFirst();
    String lookupKey = firstMatch.orElse(UNKNOWN_MIME_TYPE);
    return Collections.unmodifiableList(sizers.get(lookupKey));
  }

  public List<BeLittleResult> resize(ImageInputStream iis) {
    File file = null;
    try {
      file = File.createTempFile("belittle", null);
      write(iis, file);
      return resize(file);
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
  public List<BeLittleResult> resize(File file) {
    String mimeType = readMimeType(file);
    List<ImageSizer> sizerList = getSizersForMimeType(mimeType);
    for (ImageSizer sizer : sizerList) {
      BeLittleResult result = sizer.getResult();
      //      Callable<BeLittleResult> callable = () -> sizer.resize(source.openBufferedStream());
      //      Future<BeLittleResult> future = executorService.submit(callable);
      try {
        sizer.resize(file, mimeType);
        //        future.get(sizerSetting.getTimeoutSeconds(), TimeUnit.SECONDS);
      } catch (
          /*InterruptedException | ExecutionException | TimeoutException |*/ RuntimeException e) {
        sizer.addMessage(new BeLittleMessageImpl("EXP", BeLittleSeverity.ERROR, e));
      }
      if (result.succeeded()) {
        // Return immediately if a sizer succeeds.
        return Collections.singletonList(result);
      }
    }

    // If no sizer succeeds, return results from all sizers.
    return sizerList.stream().map(ImageSizer::getResult).collect(Collectors.toList());
  }

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
        (key, list) ->
            list.stream().map(sizer -> sizer.getNew(sizerSetting)).collect(Collectors.toList()));
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
