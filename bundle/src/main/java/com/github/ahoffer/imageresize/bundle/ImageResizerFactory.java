package com.github.ahoffer.imageresize.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.github.ahoffer.imageresize.api.ImageResizer;
import com.github.ahoffer.imageresize.provider.ImageReaderUtils;

public class ImageResizerFactory {

    private BundleContext bundleContext;

    public ImageResizer getBestResizerFor(InputStream stream) {
        System.out.println("Hello World 1");
        return null;
    }

    public ImageResizer getBestResizerFor(String format) {
        System.out.println("Hello World 1");
        return null;
    }

    public List<ImageResizer> getAllAvailableImageResizers() {

        Collection<ServiceReference<ImageResizer>> serviceReferences = null;
        try {
            serviceReferences = getBundleContext().getServiceReferences(ImageResizer.class, null);
        } catch (InvalidSyntaxException | NullPointerException e) {
            // TODO: ADD SOME LOOGING HERE
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(serviceReferences.stream()
                .map(ref -> getBundleContext().getService(ref))
                .map(ImageResizer::getNew)
                .collect(Collectors.toList()));
    }

    public List<ImageResizer> getRecommendedResizers(InputStream stream) {

        try {
            return getRecommendedResizers(ImageReaderUtils.getFormat(stream));
        } catch (IOException e) {
            // TODO: ADD SOME LOOGING HERE
            return new ArrayList<>();
        }
    }

    public List<ImageResizer> getRecommendedResizers(String format) {

        return getAllAvailableImageResizers().stream()
                .filter(ImageResizer::isAvailable)
                .filter(resizer -> resizer.recommendedFor(format))
                .collect(Collectors.toList());
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}