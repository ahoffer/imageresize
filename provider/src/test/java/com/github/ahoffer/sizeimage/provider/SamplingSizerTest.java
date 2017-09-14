package com.github.ahoffer.sizeimage.provider;

import static com.github.ahoffer.sizeimage.provider.AbstractSizeImageService.OUTPUT_SIZE_PIXELS;
import static com.github.ahoffer.sizeimage.provider.SamplingServiceSize.SAMPLING_PERIOD;
import static junit.framework.TestCase.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.github.ahoffer.sizeimage.SizeImageService;

public class SamplingSizerTest {

    SizeImageTestData data;

    @Before
    public void setup() {
        data = new SizeImageTestData();
    }

    @Test(expected = Exception.class)
    public void testNullInputStreamForSampling() throws IOException {
        SizeImageService sizer = new SamplingServiceSize();
        sizer.setInput(null);
        sizer.setOutputSize(256);
        sizer.size();
    }

    @Test(expected = Exception.class)
    public void testBadOutputSize() throws IOException {
        SizeImageService sizer = new SamplingServiceSize();
        sizer.setOutputSize(0);
        sizer.size();
    }

    @Test(expected = RuntimeException.class)
    public void testNoOutputSize() throws IOException {
        SizeImageService sizer = new SamplingServiceSize();
        sizer.setInput(data.vanillaJpegStream)
                .size();
    }

    @Test
    public void happyPath() throws IOException {
        SizeImageService sizer = new SamplingServiceSize();
        sizer.setOutputSize(256);
        sizer.setInput(data.vanillaJpegStream);
        BufferedImage output = sizer.size();
        assertEquals(256, output.getHeight());
    }

    @Test
    public void happyPathWithCustomSamplingPeriod() throws IOException {
        SizeImageService sizer = new SamplingServiceSize();
        Map<String, String> configuration = new HashMap<>();
        configuration.put(OUTPUT_SIZE_PIXELS, "256");
        configuration.put(SAMPLING_PERIOD, "4");
        BufferedImage output = sizer.setInput(data.vanillaJpegStream)
                .setConfiguration(configuration)
                .size();
        assertEquals(256, output.getHeight());
    }

}