package com.github.ahoffer.imagesize.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;

import com.github.ahoffer.imagesize.api.ImageSizer;

import net.coobird.thumbnailator.Thumbnails;

public abstract class AbstractInMemoryImageSizer extends AbstractImageSizer {

    public static final String JPEG_2000_FORMAT_NAME = "jpeg 2000";

    protected InputStream inputStream;

    public BufferedImage size(BufferedImage inputImage) throws IOException {
        validateBeforeSize();
        return Thumbnails.of(inputImage)
                .height(getOutputSize())
                .asBufferedImage();
    }

    public void validateBeforeSize() {
        super.validateBeforeSize();
        Validate.notNull(inputStream);
    }

    public ImageSizer setInput(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

}
