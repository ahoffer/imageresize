package com.github.ahoffer.sizeimage.bundle;

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

import com.github.ahoffer.sizeimage.SizeImageService;
import com.github.ahoffer.sizeimage.provider.ImageReaderUtils;

public class ImageSizerFactory {

    private BundleContext bundleContext;

    public SizeImageService getBestSizerFor(InputStream stream) {
        System.out.println("Hello World 1");
        return null;
    }

    public SizeImageService getBestSizerFor(String format) {
        System.out.println("Hello World 1");
        return null;
    }

    public List<SizeImageService> getImageSizers() {

        Collection<ServiceReference<SizeImageService>> serviceReferences = null;
        try {
            serviceReferences = getBundleContext().getServiceReferences(SizeImageService.class, null);
        } catch (InvalidSyntaxException | NullPointerException e) {
            // TODO: ADD SOME LOOGING HERE
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(serviceReferences.stream()
                .map(ref -> getBundleContext().getService(ref))
                .map(SizeImageService::getNew)
                .collect(Collectors.toList()));
    }

    public List<SizeImageService> getRecommendedSizers(InputStream stream) {

        try {
            return getRecommendedSizers(ImageReaderUtils.getFormat(stream));
        } catch (IOException e) {
            // TODO: ADD SOME LOOGING HERE
            return new ArrayList<>();
        }
    }

    public List<SizeImageService> getRecommendedSizers(String format) {

        return getImageSizers().stream()
                .filter(SizeImageService::isAvailable)
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