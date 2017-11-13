package com.github.ahoffer.sizeimage.provider;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.unmodifiableMap;

import com.github.ahoffer.sizeimage.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central class for the BeLittle library. Encapsulates the configuration. The configuration
 * controls what sizers are preferred for certain image types. This class can also set a global
 * image generate that overrides anything
 */
public class BeLittle {

  public static final String MATCH_ANY = "*";
  public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";
  private static final Logger LOGGER = LoggerFactory.getLogger(BeLittle.class);
  private int maxWidth;
  private int maxHeight;
  private Map<String, List<ImageSizer>> configuration = new HashMap<>();

  public ImageSizerResults getSizersFor(String inputMimeType) {
    ImageSizerResults coll = new ImageSizerResults();

    List<ImageSizer> all =
        configuration
            .values()
            .stream()
            .flatMap(Collection::stream)
            .map(this::copyAndInitialize)
            .collect(Collectors.toList());
    List<ImageSizer> available =
        all.stream()
            .filter(ImageSizer::isAvailable)
            .map(this::copyAndInitialize)
            .collect(Collectors.toList());
    List<ImageSizer> wildcard =
        configuration
            .getOrDefault(MATCH_ANY, (List<ImageSizer>) EMPTY_LIST)
            .stream()
            .filter(ImageSizer::isAvailable)
            .map(this::copyAndInitialize)
            .collect(Collectors.toList());

    List<ImageSizer> recommendations =
        configuration
            .getOrDefault(inputMimeType, (List<ImageSizer>) EMPTY_LIST)
            .stream()
            .filter(ImageSizer::isAvailable)
            .map(this::copyAndInitialize)
            .collect(Collectors.toList());

    coll.setAll(all);
    coll.setAvailable(available);
    coll.setWildcards(wildcard);
    coll.setRecommendations(recommendations);
    coll.setRecommended(recommendations.stream().findFirst().map(this::copyAndInitialize));
    return coll;
  }

  private ImageSizer copyAndInitialize(ImageSizer sizer) {
    ImageSizer newSizer = sizer.getNew();
    if (isOutputSizeSet()) {
      newSizer.setOutputSize(getMaxWidth(), getMaxHeight());
    }
    return newSizer;
  }

  protected boolean isOutputSizeSet() {
    return getMaxWidth() > 0 && getMaxHeight() > 0;
  }

  @SuppressWarnings("unused")
  public ImageSizerResults getSizersFor(InputStream inputStream) {
    Optional<String> mimeType = ImageReaderUtils.getMimeTypes(inputStream).stream().findFirst();
    return getSizersFor(mimeType.orElse(getUnknownMimeType()));
  }

  private String getUnknownMimeType() {
    return UNKNOWN_MIME_TYPE;
  }

  @SuppressWarnings("unused")
  public Map<String, List<ImageSizer>> getConfiguration() {
    return configuration;
  }

  @SuppressWarnings("unused")
  public synchronized void setConfiguration(Map configuration) {
    Map copy =
        Optional.ofNullable(configuration)
            .map((Function<Map, Map>) HashMap::new)
            .orElseGet(HashMap::new);
    this.configuration = unmodifiableMap(copy);
  }

  public int getMaxWidth() {
    return maxWidth;
  }

  public synchronized void setOutputSize(int maxWidth, int maxHeight) {
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
  }

  public int getMaxHeight() {
    return maxHeight;
  }

  /**
   * Convenience method.
   *
   * @param inputStream
   * @return
   */
  public synchronized Optional<BufferedImage> generate(InputStream inputStream) {

    ImageSizerResults sizers = getSizersFor(inputStream);
    ImageSizer sizer =
        sizers
            .getRecommendations()
            .stream()
            .findFirst()
            .orElse(sizers.getWildcards().stream().findFirst().orElse(null));

    if (Objects.isNull(sizer)) {
      LOGGER.info("No suitable image sizer");
    }
      try {
      return Optional.ofNullable(sizer.setInput(inputStream).generate());

    } catch (IOException e) {
      LOGGER.info("Could not resize image", e);
      return Optional.empty();
    }
  }

  /**
   * Encapsulate information about what resizing techniques are (or are not) currently available.
   */
  public class ImageSizerResults {
    private Optional<ImageSizer> recommended;
    private List<ImageSizer> recommendations;
    private List<ImageSizer> wildcards;
    private List<ImageSizer> all;
    private List<ImageSizer> available;

    public ImageSizerResults() {
      recommendations = Collections.emptyList();
      wildcards = Collections.emptyList();
      all = Collections.emptyList();
      available = Collections.emptyList();
      recommended = Optional.empty();
    }

    public Optional<ImageSizer> getRecommended() {
      return recommended;
    }

    protected void setRecommended(Optional<ImageSizer> recommended) {
      this.recommended = recommended;
    }

    public List<ImageSizer> getRecommendations() {
      return recommendations;
    }

    protected void setRecommendations(List<ImageSizer> recommendations) {
      this.recommendations = recommendations;
    }

    public List<ImageSizer> getWildcards() {
      return wildcards;
    }

    protected void setWildcards(List<ImageSizer> wildcards) {
      this.wildcards = wildcards;
    }

    public List<ImageSizer> getAll() {
      return all;
    }

    protected void setAll(List<ImageSizer> all) {
      this.all = all;
    }

    public List<ImageSizer> getAvailable() {
      return available;
    }

    protected void setAvailable(List<ImageSizer> available) {
      this.available = available;
    }
  }
}
