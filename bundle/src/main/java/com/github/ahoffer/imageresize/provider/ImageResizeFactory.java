package com.github.ahoffer.imageresize.provider;

import java.io.InputStream;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class ImageResizeFactory {

    BundleContext bundleContext;

    public ImageResizer getBestResizerFor(InputStream stream) {
        System.out.println("Hello World 1");
        return null;
    }

    public ImageResizer getBestResizerFor(String format) {
        System.out.println("Hello World 1");
        return null;
    }

    public List<ImageResizer> getAllAvailableImageResizers() {
        return null;
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
}