package com.github.ahoffer.sizeimage;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.imageio.spi.IIORegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageSizerFactory {

  private static final Logger logger = LoggerFactory.getLogger(ImageSizerFactory.class);

  static {
    IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
  }

  private BundleContext bundleContext;

  //    public ImageSizer getSizerNamed(String name) {
  //        try {
  //            return getBundleContext().getServiceReferences(ImageSizer.class,
  //                    String.format("(name=%s)", name));
  //        } catch (InvalidSyntaxException e) {
  //            return null;
  //        }
  //        return null;
  //    }

  //    public ImageSizer getBestSizerFor(String format) {
  //        System.out.println("Hello World 1");
  //        return null;
  //    }

  //    public List<ImageSizer> getImageSizers() {
  //        return  getServiceReferences(null);
  //        return Collections.unmodifiableList(serviceReferences.stream()
  //                .map(ref -> getBundleContext().getService(ref))
  //                .map(ImageSizer::getNew)
  //                .collect(Collectors.toList()));
  //        return null;
  //    }

  //    public List<ImageSizer> getRecommendedSizers(InputStream stream) {
  //
  //        try {
  //            return getRecommendedSizers(ImageReaderUtils.getFormat(stream));
  //        } catch (IOException e) {
  //            // TODO: ADD SOME LOGGING HERE
  //            return new ArrayList<>();
  //        }
  //    }

  //    public List<ImageSizer> getRecommendedSizers(String format) {
  //
  //        return getImageSizers().stream()
  //                .filter(ImageSizer::isAvailable)
  //                .filter(sizer -> sizer.recommendedFor(format))
  //                .collect(Collectors.toList());
  //    }

  public BundleContext getBundleContext() {
    return bundleContext;
  }

  @SuppressWarnings("unused")
  public void setBundleContext(BundleContext bundleContext) {
    this.bundleContext = bundleContext;
  }

  public List<ServiceReference<ImageSizer>> getServiceReferences(String filter) {
    List<ServiceReference<ImageSizer>> list = Collections.EMPTY_LIST;
    if (Objects.nonNull(getBundleContext())) {
      try {
        list = new ArrayList<>(getBundleContext().getServiceReferences(ImageSizer.class, filter));
        Collections.sort(list);
        Collections.reverse(list);
        Collections.unmodifiableList(list);
      } catch (InvalidSyntaxException e) {
        logger.warn("Invalid OSGi service filter", e);
      }
    }
    return list;
  }
}
