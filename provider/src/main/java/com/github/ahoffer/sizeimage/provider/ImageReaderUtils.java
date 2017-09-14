package com.github.ahoffer.sizeimage.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang3.Validate;

public class ImageReaderUtils {
    public static int READLIMIT = 8192;

    public static ImageReader getReader(InputStream inputStream) throws IOException {
        Validate.notNull(inputStream, "Input image stream is null");
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        Iterator iter = ImageIO.getImageReaders(imageInputStream);
        ImageReader reader = (ImageReader) iter.next();
        reader.setInput(imageInputStream);
        return reader;
    }

    public static String getFormat(InputStream inputStream) throws IOException {
        Validate.notNull(inputStream);
        String formatName;
        ImageReader reader = null;
        try {
            if (inputStream.markSupported()) {
                inputStream.mark(READLIMIT);
            }

            reader = getReader(inputStream);
            formatName = reader.getFormatName();
        } finally {
            if (Objects.nonNull(reader)) {
                reader.dispose();
            }
            if (inputStream.markSupported()) {
                inputStream.reset();
            }
        }
        return formatName;
    }
}