package com.github.ahoffer.imageresize.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang3.Validate;

import net.coobird.thumbnailator.Thumbnails;

public abstract class AbstractInMemoryImageResizer extends AbstractImageResizer {

    public static final String JPEG_2000_FORMAT_NAME = "jpeg 2000";


    protected InputStream inputStream;

    ImageReader reader;

    public static ImageReader getReader(InputStream inputStream) throws IOException {

        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        Iterator iter = ImageIO.getImageReaders(imageInputStream);
        ImageReader reader = (ImageReader) iter.next();
        reader.setInput(imageInputStream);
        return reader;
    }

    public BufferedImage resize(BufferedImage inputImage) throws IOException {
        validateBeforeResize();
        return Thumbnails.of(inputImage)
                .height(getOutputSize())
                .asBufferedImage();

    }

    public void validateBeforeResize() {
        super.validateBeforeResize();
        Validate.notNull(inputStream);
    }


}
