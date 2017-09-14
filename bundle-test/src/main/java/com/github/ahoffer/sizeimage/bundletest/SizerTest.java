package com.github.ahoffer.sizeimage.bundletest;

import com.github.ahoffer.sizeimage.bundle.ImageSizerFactory;

public class SizerTest {

    private ImageSizerFactory imageSizerFactory;

    @SuppressWarnings("unused")
    public void init() throws Exception {

        final long start = System.nanoTime();
        System.out.println(getImageSizerFactory().toString());
        final long stop = System.nanoTime();
        double t = (stop - start) / 1.0e6;
        System.out.println(String.format("Time of execution in seconds: %s ms",
                Double.toString(t)));
    }

    public ImageSizerFactory getImageSizerFactory() {
        return imageSizerFactory;
    }

    @SuppressWarnings("unused")
    public void setImageSizerFactory(ImageSizerFactory imageSizerFactory) {
        this.imageSizerFactory = imageSizerFactory;
    }
}
