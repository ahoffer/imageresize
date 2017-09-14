package com.github.ahoffer.imagesize.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.github.ahoffer.imagesize.api.ImageSizer;

public class PreAllocatingSizerTest {
    ImageSizeTestData data;

    @Before
    public void setup() {
        data = new ImageSizeTestData();
    }

    @Test
    public void testHappyPath() throws IOException {
        ImageSizer sizer = new PreAllocatingSizer();
        sizer.setInput(data.jpeg2000Stream);
        sizer.setOutputSize(256);
        BufferedImage output = sizer.size();
        assertEquals(256, output.getHeight());
    }

    @Test(expected = Exception.class)
    public void testNullInputStream() throws IOException {
        ImageSizer sizer = new PreAllocatingSizer();
        sizer.setInput(null);
        sizer.setOutputSize(256);
        sizer.size();
    }

    @Test(expected = Exception.class)
    public void testBadOutputSize() throws IOException {
        ImageSizer sizer = new PreAllocatingSizer();
        sizer.setOutputSize(0);
        sizer.size();
    }

    @Test(expected = Exception.class)
    public void testNoOutputSize() throws IOException {
        ImageSizer sizer = new PreAllocatingSizer();
        sizer.size();
    }

    @Test
    public void testNewInstance() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("KEY", "VALUE");
        ImageSizer sizer1 = new PreAllocatingSizer();
        sizer1.setConfiguration(configuration);
        ImageSizer sizer2 = sizer1.getNew();
        assertNotSame("The start() method should return a new instance", sizer1, sizer2);
        assertTrue("The start() method did not instantiate the correct class",
                sizer2 instanceof PreAllocatingSizer);

    }
}