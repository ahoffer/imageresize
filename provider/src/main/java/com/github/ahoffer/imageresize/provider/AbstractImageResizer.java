package com.github.ahoffer.imageresize.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.github.ahoffer.imageresize.api.ImageResizer;

public abstract class AbstractImageResizer implements ImageResizer {

    public ImageResizer setInput(InputStream inputStream) {
        return null;
    }

    public ImageResizer setImageIndex(int imageIndex) {
        return null;
    }

    public ImageResizer setSize(int pixels) {
        return null;
    }

    public BufferedImage resize() {
        return null;
    }

    public boolean recommenedForImageFormat(String imageFormat) {
        return false;
    }

    private InputStream source;

    private ImageReader reader;

    protected void initialize() throws IOException {
        reader = getReader(source);
    }

    public static ImageReader getReader(InputStream inputStream) throws IOException {

        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        Iterator iter = ImageIO.getImageReaders(imageInputStream);
        ImageReader reader = (ImageReader) iter.next();
        reader.setInput(imageInputStream);
        return reader;
    }

    public String getFormatName() throws IOException {

        return reader.getFormatName();

    }
}
