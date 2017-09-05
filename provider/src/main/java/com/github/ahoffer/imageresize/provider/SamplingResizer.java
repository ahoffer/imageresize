package com.github.ahoffer.imageresize.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.github.ahoffer.imageresize.api.AbstractInMemoryImageResizer;
import com.github.ahoffer.imageresize.provider.internal.SampledImageReader;

public class SamplingResizer extends AbstractInMemoryImageResizer {

    public static final String SAMPLE_PERIOD = "samplePeriod";

    public BufferedImage resize() throws IOException {
        SampledImageReader reader = SampledImageReader.of(inputStream);
        if (configuration.containsKey(SAMPLE_PERIOD)) {
            reader.samplePeriod(Integer.valueOf(configuration.get(SAMPLE_PERIOD)));
        }

        return super.resize(reader.read());
    }

    public boolean recommendedFor(String imageFormat) {
        return !(JPEG_2000_FORMAT_NAME.equalsIgnoreCase(imageFormat));
    }
}
