package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.spi.IIORegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageSizerFactory {

  public static final String MATCH_ANY = "*";
  private static final Logger LOGGER = LoggerFactory.getLogger(ImageSizerFactory.class);

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  private Map<String, List<ImageSizer>> configuration = new HashMap<>();

  @SuppressWarnings("unused")
  List<ImageSizer> getDefaultSizers() {
    List<ImageSizer> defaultObjects = configuration.get(MATCH_ANY);
    if (Objects.isNull(defaultObjects) || defaultObjects.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    return defaultObjects;
  }

  @SuppressWarnings("unused")
  public List<ImageSizer> getRecommendedSizers(
      InputStream inputStream, boolean returnOnlyAvailableImageSizers) {
    if (Objects.isNull(inputStream)) {
      return getRecommendedSizers((String) null, returnOnlyAvailableImageSizers);
    }
    List<String> mimeTypes = ImageReaderUtils.getMimeTypes(inputStream);
    for (String mimeType : mimeTypes) {

      List<ImageSizer> list = getRecommendedSizers(mimeType);
      if (!list.isEmpty()) {
        return list;
      }
    }
    LOGGER.debug("No MIME types for the input stream");
    return getDefaultSizers();
  }

  @SuppressWarnings("unused")
  public List<ImageSizer> getRecommendedSizers(InputStream inputStream) {
    return getRecommendedSizers(inputStream, true);
  }

  @SuppressWarnings("unused")
  public List<ImageSizer> getRecommendedSizers(
      String mimeType, boolean returnOnlyAvailableImageSizers) {

    List<ImageSizer> list =
        configuration
            .entrySet()
            .stream()
            .filter(entry -> !MATCH_ANY.equals(entry.getKey()))
            .filter(entry -> entry.getKey().equalsIgnoreCase(mimeType))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(getDefaultSizers());

    return list.stream()
        .map(ImageSizer::getNew)
        .filter(imageSizer -> returnOnlyAvailableImageSizers ? imageSizer.isAvailable() : true)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  public List<ImageSizer> getRecommendedSizers(String mimeType) {
    return getRecommendedSizers(mimeType, true);
  }

  @SuppressWarnings("unused")
  public Optional<ImageSizer> getRecommendedSizer(String mimeType) {
    return getRecommendedSizers(mimeType).stream().findFirst();
  }

  @SuppressWarnings("unused")
  public Optional<ImageSizer> getRecommendedSizer(InputStream inputStream) {
    return getRecommendedSizers(inputStream).stream().findFirst();
  }

  @SuppressWarnings("unused")
  public Map<String, List<ImageSizer>> getConfiguration() {
    return configuration;
  }

  @SuppressWarnings("unused")
  public void setConfiguration(Map configuration) {
    Map copy = Optional.ofNullable(configuration).map(x -> new HashMap(x)).orElseGet(HashMap::new);
    this.configuration = Collections.unmodifiableMap(copy);
  }
}
