package com.github.ahoffer.imagesize.bundletest;

import com.github.ahoffer.imagesize.bundle.ImageSizerFactory;

public class SizerTest {

    private ImageSizerFactory imageSizerFactory;

    public void init() throws Exception {

        final long start = System.nanoTime();
        System.out.println(getImageSizerFactory().toString());
        final long stop = System.nanoTime();
        System.out.println(String.format("Time of execution in seconds: %d", (stop - start) / 1e6));
    }

    public ImageSizerFactory getImageSizerFactory() {
        return imageSizerFactory;
    }

    public void setImageSizerFactory(ImageSizerFactory imageSizerFactory) {
        this.imageSizerFactory = imageSizerFactory;
    }
}
