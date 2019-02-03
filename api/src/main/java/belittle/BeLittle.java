package belittle;

import java.io.File;
import java.util.List;
import javax.imageio.stream.ImageInputStream;

public interface BeLittle {

  /*
        public static InputStream from(InputStream inputStream) {
          return inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
        }

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


    ImageSizer copyAndInitialize(ImageSizer sizer) {
      ImageSizer newSizer = sizer.getNew();
      if (isOutputSizeSet()) {
        newSizer.setOutputSize(getMaxWidth(), getMaxHeight());
      }
      return newSizer;
    }

    boolean isOutputSizeSet() {
      return getMaxWidth() > 0 && getMaxHeight() > 0;
    }


    public ImageSizerCollection getSizersFor(InputStream inputStream) {
      InputStream iStream = from(inputStream);
      Optional<String> mimeType;
      try (SaferImageReader tempReader = new SaferImageReader(iStream)) {
        mimeType = tempReader.getMimeTypes().stream().findFirst();
      }

      return getSizersFor(mimeType.orElse(getUnknownMimeType()));
    }


    String getUnknownMimeType() {
      return UNKNOWN_MIME_TYPE;
    }


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
  */

  /** Convenience method. Attempt to generate an image from the first available ImageSizer. */
  List<BeLittleResult> generate(ImageInputStream iis) throws RuntimeException;

  List<BeLittleResult> generate(File file) throws RuntimeException;

  /*
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
  */

}
