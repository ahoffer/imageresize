package com.github.ahoffer.imageresize.provider;

import java.io.IOException;

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

}