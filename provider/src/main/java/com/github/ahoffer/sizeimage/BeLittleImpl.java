package com.github.ahoffer.sizeimage;

import static com.github.ahoffer.sizeimage.BeLittleConstants.MIME_TYPE;
import static com.github.ahoffer.sizeimage.BeLittleConstants.UNKNOWN_MIME_TYPE;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.stream.ImageInputStream;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeLittleImpl implements BeLittle {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(BeLittleImpl.class);

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

  public List<ImageSizer> getSizers() {
    return Collections.unmodifiableList(sizers.get(getMimeType()));
  }

  public List<BeLittlingResult> generate(ImageInputStream iis) throws RuntimeException {
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
  public List<BeLittlingResult> generate(File file) {
    setMimeType(readMimeType(file));
    ByteSource source = Files.asByteSource(file);
    List<BeLittlingResult> results = new ArrayList<>();

    List<ImageSizer> sizerList = getSizers();
    for (ImageSizer sizer : sizerList) {
      try {
        BeLittlingResult result = sizer.resize(source.openBufferedStream());
        if (result.succeeded()) {
          // Return first success
          return Collections.singletonList(result);
        }
        results.add(result);
      } catch (IOException e) {
        LOGGER.info("Unexpected error resizing an image", e);
      }
    }
    // If all sizers failed, returns all results
    return results;
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
