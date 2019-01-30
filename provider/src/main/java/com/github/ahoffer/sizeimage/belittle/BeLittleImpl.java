package com.github.ahoffer.sizeimage.belittle;

import static java.util.Collections.EMPTY_LIST;

import com.github.ahoffer.sizeimage.BeLittle;
import com.github.ahoffer.sizeimage.BeLittlingResult;
import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.support.ImageReaderError;
import com.github.ahoffer.sizeimage.support.MessageFactory;
import com.github.ahoffer.sizeimage.support.SaferImageReader;
import com.github.ahoffer.sizeimage.support.StreamResetException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central class for the BeLittle library. Encapsulates the configuration. The configuration
 * controls what ImageSizers are preferred for certain image types.
 */
public class BeLittleImpl implements BeLittle {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(BeLittleImpl.class);

  /**
   * The character '*' is the wildcard and will match an image type. Think of wildcard sizers as
   * "defaults" or a "fallback". If there is no configuration for a particular image type, the
   * BeLittle attempts to use a wildcard sizer to resize the image. Alternatively, if the sizer that
   * do match an image type fail to resize the image, the wildcard sizers should be used to generate
   * the image. Therefore, wildcard sizers should be selected to be broadly applicable to many image
   * types. Good choices for wildcard sizers are MagickSizer, SamplingSizer, and BasicSizer.
   */
  public String MATCH_ANY = "*";

  /**
   * TODO: Not sure if we still need this. Can't recall why I wanted this and the wildcard. BeLittle
   * can be configured with sizers to match an unknown image type.
   */
  String UNKNOWN_MIME_TYPE = "application/octet-stream";

  /**
   * Users can set (maximum) desired width and (maximum) desired height of output images by setting
   * both values. If both values are set, BeLittle will override (replace) the desired width and
   * desired height values configured for individual ImageSizer.
   */
  int maxWidth;

  /**
   * Users can set (maximum) desired width and (maximum) desired height of output images by setting
   * both values. If both values are set, BeLittle will override (replace) the desired width and
   * desired height values configured for individual ImageSizer.
   */
  int maxHeight;

  /**
   * The configuration data structure is the heard of the BeLittle class. It represents its
   * data-drive, user-configured behavior.
   */
  Map<String, List<ImageSizer>> configuration = new HashMap<>();

  /** Helper class to generate messages to be added to ImageSizers. */
  MessageFactory messageFactory = new MessageFactory();

  public static InputStream from(InputStream inputStream) {
    return inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
  }

  /**
   * This method is the primary public object for finding the ImageSizers that should be used for a
   * particular image type. See the ImageSizerCollection class for more information.
   */
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

  /**
   * ImageSizer are meant to be used in a single thread, and, unless care is taken, to be used only
   * once. This method clones a prototype ImageSizer and sets its desired width and height, if
   * appropriate.
   *
   * @return copy of an image sizer
   */
  ImageSizer copyAndInitialize(ImageSizer sizer) {
    ImageSizer newSizer = sizer.getNew();
    if (isOutputSizeSet()) {
      newSizer.setOutputSize(getMaxWidth(), getMaxHeight());
    }
    return newSizer;
  }

  /**
   * This object is considered to be configured for a default width and height if BOTH values are
   * greater than zero.
   *
   * @return true is the desired width and height are configured for the BeLittle object.
   */
  boolean isOutputSizeSet() {
    return getMaxWidth() > 0 && getMaxHeight() > 0;
  }

  /**
   * Similar to getSizerFor(mimeType), expect that it accepts an input stream representing an image.
   * It will attempt to extract the MIME type from the stream and reset the stream to its original
   * position.
   */
  public ImageSizerCollection getSizersFor(InputStream inputStream) {
    InputStream iStream = from(inputStream);
    Optional<String> mimeType;
    try (SaferImageReader tempReader = new SaferImageReader(iStream)) {
      mimeType = tempReader.getMimeTypes().stream().findFirst();
    } catch (ImageReaderError e) {
      mimeType = Optional.empty();
    }

    return getSizersFor(mimeType.orElse(getUnknownMimeType()));
  }

  /**
   * Can't remember what this was all about
   *
   * @return the defacto unknown MIME type, application/octet-stream
   */
  String getUnknownMimeType() {
    return UNKNOWN_MIME_TYPE;
  }

  /**
   * Unlike the image sizers themselves, the BeLittle class is a singleton and is expected to be
   * accessed by multiple, perhaps competing threads. Getting and settings its configuration should
   * be atomic operations. For now, it seems sufficient to synchronized the getters and setters.
   * Setting a configuration should be done rarely and is usually done once, at startup, by a
   * dependency injection library.
   *
   * @return map of mime types to lists of ImageSizer prototypes
   */
  @SuppressWarnings("unused")
  public synchronized Map<String, List<ImageSizer>> getConfiguration() {
    return Collections.unmodifiableMap(configuration);
  }

  @SuppressWarnings("unused")
  public synchronized void setConfiguration(Map configuration) {
    Map newConfiguration = new HashMap();
    if (configuration != null) {
      newConfiguration = new HashMap<>(configuration);
    }
    this.configuration = newConfiguration;
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

  /** Convenience method. Attempt to generate an image from the first available ImageSizer. */
  public BeLittlingResult generate(InputStream inputStream) throws StreamResetException {
    InputStream iStream = from(inputStream);
    List<ImageSizer> sizers = getSizersFor(iStream).getRecommendations();
    return null;
  }

  /*
  public BeLittlingResult generate(InputStream inputStream) throws StreamResetException {
    InputStream iStream = from(inputStream);
    Optional<ImageSizer> sizer = getSizersFor(iStream).getRecommended();
    if (sizer.isPresent()) {
      return sizer.get().setInput(iStream).generate();
    } else {
      return new BeLittlingResultImpl(
          null, Collections.singletonList(messageFactory.make(MessageConstants.NO_SIZER)));
    }
  }

  */

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
