package com.github.ahoffer.imagesize.provider;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class SamplingSizer extends AbstractInMemoryImageSizer {

    public static final String SAMPLING_PERIOD = "samplePeriod";

    public BufferedImage size() throws IOException {
        SampledImageReader reader = SampledImageReader.of(inputStream);
        if (configuration.containsKey(SAMPLING_PERIOD)) {
            reader.samplePeriod(Integer.valueOf(configuration.get(SAMPLING_PERIOD)));
        }
        return this.size(reader.read());
    }

    public boolean recommendedFor(String imageFormat) {
        return !(JPEG_2000_FORMAT_NAME.equalsIgnoreCase(imageFormat));
    }
}
