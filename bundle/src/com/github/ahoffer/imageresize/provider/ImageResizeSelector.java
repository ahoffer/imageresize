package com.github.ahoffer.imageresize.provider;

import java.io.File;
import java.io.InputStream;

public class ImageResizeSelector {

    public ImageResizer getBestResizerFor(InputStream stream) {
        System.out.println("Hello World 1");
        return null;
    }

    public ImageResizer getBestResizerFor(File file) {
        if (file.exists() && file.canRead()) {
            System.out.println("Hello World 1");
        } else {
            System.out.println("Goodbye cruel world");
        }
        return null;
    }
}