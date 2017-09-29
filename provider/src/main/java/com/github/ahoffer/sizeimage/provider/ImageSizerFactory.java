package com.github.ahoffer.sizeimage.provider;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.spi.IIORegistry;
import org.apache.commons.lang3.Validate;
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
  Optional<List<ImageSizer>> getDefaultSizers() {
    return Optional.ofNullable(configuration.get(MATCH_ANY));
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
            .orElse(
                getDefaultSizers()
                    .orElseThrow(
                        () ->
                            new RuntimeException(
                                "No image sizers configured. Add '*' (wildcard configuration)")));

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
  public Map<String, List<ImageSizer>> getConfiguration() {
    return configuration;
  }

  @SuppressWarnings("unused")
  public void setConfiguration(Map configuration) {
    Map copy = Optional.ofNullable(configuration).map(x -> new HashMap(x)).orElseGet(HashMap::new);
    this.configuration = Collections.unmodifiableMap(copy);
  }
}
