package com.github.ahoffer.imageresize.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.github.ahoffer.imageresize.api.AbstractInMemoryImageResizer;

public class SamplingResizer extends AbstractInMemoryImageResizer {

    public static final String SAMPLING_PERIOD = "samplePeriod";

    public BufferedImage resize() throws IOException {
        SampledImageReader reader = SampledImageReader.of(inputStream);
        if (configuration.containsKey(SAMPLING_PERIOD)) {
            reader.samplePeriod(Integer.valueOf(configuration.get(SAMPLING_PERIOD)));
        }
        return super.resize(reader.read());
    }

    public boolean recommendedFor(String imageFormat) {
        return !(JPEG_2000_FORMAT_NAME.equalsIgnoreCase(imageFormat));
    }
}
