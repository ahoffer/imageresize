package com.github.ahoffer.imageresize.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.github.ahoffer.imageresize.api.ImageResizer;

public abstract class AbstractImageResizer implements ImageResizer {

Map<String, String> configuration = new HashMap<>();

    public ImageResizer setInput(InputStream inputStream) {
        return null;
    }

    public ImageResizer setImageIndex(int imageIndex) {
        return null;
    }

    public ImageResizer setSize(int pixels) {
        return null;
    }

    public boolean recommenedForImageFormat(String imageFormat) {
        return false;
    }


    public void validateBeforeResize() {
        Validate.notNull(source);
        Validate.inclusiveBetween(1, Integer.MAX_VALUE, pixels);

    };



}
