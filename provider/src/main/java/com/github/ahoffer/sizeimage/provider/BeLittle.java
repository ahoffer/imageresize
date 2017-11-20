package com.github.ahoffer.sizeimage.provider;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.unmodifiableMap;

import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(BeLittle.class);

  private int maxWidth;
  private int maxHeight;
  private Map<String, List<ImageSizer>> configuration = new HashMap<>();
  private ImageReaderShortcuts shortcuts = new ImageReaderShortcuts();

  public ImageSizerCollection getSizersFor(String inputMimeType) {
    ImageSizerCollection coll = new ImageSizerCollection();

    Set<ImageSizer> all =
        configuration
            .values()
            .stream()
            .flatMap(Collection::stream)
            .map(this::copyAndInitialize)
            .collect(Collectors.toSet());

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

    List<ImageSizer> matching =
        configuration
            .getOrDefault(inputMimeType, (List<ImageSizer>) EMPTY_LIST)
            .stream()
            .map(this::copyAndInitialize)
            .collect(Collectors.toList());

    // Recommend any sizers configured for the input MIME type. If none, use the wildcard sizers.
    List<ImageSizer> recommendations = new ArrayList<>();
    recommendations.addAll(matching);
    recommendations.addAll(wildcard);
    recommendations =
        recommendations.stream().map(this::copyAndInitialize).collect(Collectors.toList());

    coll.setAll(all);
    coll.setAvailable(available);
    coll.setWildcards(wildcard);
    coll.setMatching(matching);
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
  public ImageSizerCollection getSizersFor(InputStream inputStream) throws StreamResetException {
    Optional<String> mimeType = shortcuts.getMimeTypes(inputStream).stream().findFirst();
    return getSizersFor(mimeType.orElse(getUnknownMimeType()));
  }

  private String getUnknownMimeType() {
    return UNKNOWN_MIME_TYPE;
  }

  @SuppressWarnings("unused")
  public synchronized Map<String, List<ImageSizer>> getConfiguration() {
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

  public synchronized int getMaxWidth() {
    return maxWidth;
  }

  public synchronized void setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public synchronized void setOutputSize(int maxWidth, int maxHeight) {
    setMaxWidth(maxWidth);
    setMaxHeight(maxHeight);
  }

  public synchronized int getMaxHeight() {
    return maxHeight;
  }

  public synchronized void setMaxHeight(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  /**
   * Convenience method.
   *
   * @param inputStream
   * @return
   */
  public synchronized BeLittlingResult generate(InputStream inputStream)
      throws StreamResetException {
    ImageSizerCollection sizers = getSizersFor(inputStream);
    Optional<ImageSizer> sizer = sizers.getRecommended();
    return sizer.flatMap(s -> s.setInput(inputStream).generate());
  }

  public static class StreamResetException extends IOException {}

  /**
   * Encapsulate information about what resizing techniques are (or are not) available and
   * recommended.
   */
  public class ImageSizerCollection {
    private Optional<ImageSizer> recommended;
    private List<ImageSizer> recommendations;
    private List<ImageSizer> wildcards;
    private Set<ImageSizer> all;
    private List<ImageSizer> available;
    private List<ImageSizer> matching;

    public ImageSizerCollection() {
      recommendations = Collections.emptyList();
      wildcards = Collections.emptyList();
      all = Collections.emptySet();
      available = Collections.emptyList();
      matching = Collections.emptyList();
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

    public Set<ImageSizer> getAll() {
      return all;
    }

    protected void setAll(Set<ImageSizer> all) {
      this.all = all;
    }

    public List<ImageSizer> getAvailable() {
      return available;
    }

    protected void setAvailable(List<ImageSizer> available) {
      this.available = available;
    }

    public List<ImageSizer> getMatching() {
      return matching;
    }

    void setMatching(List<ImageSizer> matching) {
      this.matching = matching;
    }
  }
}
