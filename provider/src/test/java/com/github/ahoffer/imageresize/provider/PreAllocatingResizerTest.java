package com.github.ahoffer.imageresize.provider;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;

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
}