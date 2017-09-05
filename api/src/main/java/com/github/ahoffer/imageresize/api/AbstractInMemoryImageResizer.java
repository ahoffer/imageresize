package com.github.ahoffer.imageresize.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;

import net.coobird.thumbnailator.Thumbnails;

public abstract class AbstractInMemoryImageResizer extends AbstractImageResizer {

    public static final String JPEG_2000_FORMAT_NAME = "jpeg 2000";

    protected InputStream inputStream;

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

    public ImageResizer setInput(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }
}
