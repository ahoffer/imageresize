package com.github.ahoffer.imageresize.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class GetImageReader {

    public static ImageReader get(InputStream inputStream) throws IOException {
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        Iterator iter = ImageIO.getImageReaders(imageInputStream);
        ImageReader reader = (ImageReader) iter.next();
        reader.setInput(imageInputStream);
        return reader;
    }
}

