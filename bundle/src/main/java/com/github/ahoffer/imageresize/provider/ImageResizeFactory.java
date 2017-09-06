package com.github.ahoffer.imageresize.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class ImageResizeFactory {

    BundleContext bundleContext;

    public ImageResizer getBestResizerFor(InputStream stream) {
        System.out.println("Hello World 1");
        return null;
    }


    public List<ServiceReference<ImageResizer>> getAvailableImageResizers() {

        return null;
    }
}