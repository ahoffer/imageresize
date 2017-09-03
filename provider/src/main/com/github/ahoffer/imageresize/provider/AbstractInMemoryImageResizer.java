package com.github.ahoffer.imageresize.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang3.Validate;

public abstract class AbstractInMemoryImageResizer extends AbstractImageResizer {

    protected void initialize() throws IOException {
        reader = getReader(source);
    }

    private InputStream source;

    private ImageReader reader;

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

    public void validateBeforeResize() {
        super.validateBeforeResize();
        Validate.notNull(source);
    }
}
