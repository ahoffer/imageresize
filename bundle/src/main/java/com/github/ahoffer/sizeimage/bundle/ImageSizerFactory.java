package com.github.ahoffer.sizeimage.bundle;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.ImageReaderUtils;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.spi.IIORegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ImageSizerFactory {

    static {
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(new J2KImageReaderSpi());
    }

    private BundleContext bundleContext;

    public ImageSizer getBestSizerFor(String format) {
        System.out.println("Hello World 1");
        return null;
    }

    public List<ImageSizer> getImageSizers() {

        Collection<ServiceReference<ImageSizer>> serviceReferences = null;
        try {
            serviceReferences = getBundleContext().getServiceReferences(ImageSizer.class,
                    null);
        } catch (InvalidSyntaxException | NullPointerException e) {
            // TODO: ADD SOME LOOGING HERE
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(serviceReferences.stream()
                .map(ref -> getBundleContext().getService(ref))
                .map(ImageSizer::getNew)
                .collect(Collectors.toList()));
    }

    public List<ImageSizer> getRecommendedSizers(InputStream stream) {

        try {
            return getRecommendedSizers(ImageReaderUtils.getFormat(stream));
        } catch (IOException e) {
            // TODO: ADD SOME LOGGING HERE
            return new ArrayList<>();
        }
    }

    public List<ImageSizer> getRecommendedSizers(String format) {

        return getImageSizers().stream()
                .filter(ImageSizer::isAvailable)
                .filter(sizer -> sizer.recommendedFor(format))
                .collect(Collectors.toList());
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}