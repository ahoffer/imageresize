package com.github.ahoffer.imageresize.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class PreAllocatingResizerTest {
    ImageResizeTestData data;

    @Before
    public void setup() {
        data = new ImageResizeTestData();
    }

    @Test
    public void testHappyPath() throws IOException {
        ImageResizer resizer = new PreAllocatingResizer();
        resizer.setInput(data.jpeg2000Stream);
        resizer.setOutputSize(256);
        BufferedImage output = resizer.resize();
        assertEquals(256, output.getHeight());
    }

    @Test(expected = Exception.class)
    public void testNullInputStream() throws IOException {
        ImageResizer resizer = new PreAllocatingResizer();
        resizer.setInput(null);
        resizer.setOutputSize(256);
        resizer.resize();
    }

    @Test(expected = Exception.class)
    public void testBadOutputSize() throws IOException {
        ImageResizer resizer = new PreAllocatingResizer();
        resizer.setOutputSize(0);
        resizer.resize();
    }

    @Test(expected = Exception.class)
    public void testNoOutputSize() throws IOException {
        ImageResizer resizer = new PreAllocatingResizer();
        resizer.resize();
    }

    @Test
    public void testNewInstance() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("KEY", "VALUE");
        ImageResizer resizer1 = new PreAllocatingResizer();
        resizer1.setConfiguration(configuration);
        ImageResizer resizer2 = resizer1.start();
        assertNotSame("The start() method should return a new instance", resizer1, resizer2);
        assertTrue("The start() method did not instantiate the correct class",
                resizer2 instanceof PreAllocatingResizer);

    }
}