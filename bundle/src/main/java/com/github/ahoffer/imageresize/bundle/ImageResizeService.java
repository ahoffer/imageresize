package com.github.ahoffer.imageresize.bundle;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class ImageResizeService {

    private BundleContext bundleContext;

    public ImageResizer getBestResizerFor(InputStream stream) {
        System.out.println("Hello World 1");
        return null;
    }

    public ImageResizer getBestResizerFor(String format) {
        System.out.println("Hello World 1");
        return null;
    }

    public List<ImageResizer> getAllAvailableImageResizers() throws InvalidSyntaxException {

        Collection<ServiceReference<ImageResizer>> serviceReferences =
                getBundleContext().getServiceReferences(ImageResizer.class, null);
        return serviceReferences.stream()
                .map(ref -> getBundleContext().getService(ref))
                .collect(Collectors.toList());
    }

    public List<ImageResizer> getRecommendedResizers(InputStream stream) {
        return null;
    }

    public List<ImageResizer> getRecommendedResizers(String format) {
        return null;
    }

    List<ServiceReference<ImageResizer>> getAvailableImageResizers() {

        return null;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}