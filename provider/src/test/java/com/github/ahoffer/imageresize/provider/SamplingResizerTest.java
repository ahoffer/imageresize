package com.github.ahoffer.imageresize.provider;

import static com.github.ahoffer.imageresize.provider.AbstractImageResizer.OUTPUT_SIZE_PIXELS;
import static com.github.ahoffer.imageresize.provider.SamplingResizer.SAMPLING_PERIOD;
import static junit.framework.TestCase.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class SamplingResizerTest {

    ImageResizeTestData data;

    @Before
    public void setup() {
        data = new ImageResizeTestData();
    }

    @Test(expected = Exception.class)
    public void testNullInputStreamForSampling() throws IOException {
        ImageResizer resizer = new SamplingResizer();
        resizer.setInput(null);
        resizer.setOutputSize(256);
        resizer.resize();
    }

    @Test(expected = Exception.class)
    public void testBadOutputSize() throws IOException {
        ImageResizer resizer = new SamplingResizer();
        resizer.setOutputSize(0);
        resizer.resize();
    }

    @Test(expected = Exception.class)
    public void testNoOutputSize() throws IOException {
        ImageResizer resizer = new SamplingResizer();
        resizer.resize();
    }

    @Test
    public void happyPath() throws IOException {
        ImageResizer resizer = new SamplingResizer();
        resizer.setOutputSize(256);
        resizer.setInput(data.vanillaJpegStream);
        BufferedImage output = resizer.resize();
        assertEquals(256, output.getHeight());
    }

    @Test
    public void happyPathWithCustomSamplingPeriod() throws IOException {
        ImageResizer resizer = new SamplingResizer();
        resizer.setInput(data.vanillaJpegStream);
        Map<String, String> configuration = new HashMap<>();
        configuration.put(OUTPUT_SIZE_PIXELS, "256");
        configuration.put(SAMPLING_PERIOD, "4");
        resizer.setConfiguration(configuration);
        BufferedImage output = resizer.resize();
        assertEquals(256, output.getHeight());
    }

}