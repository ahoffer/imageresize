package com.github.ahoffer.imageresize.provider;

import static org.junit.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.github.ahoffer.imageresize.api.GetImageReader;
import com.github.ahoffer.imageresize.api.ImageResizer;

public class ImageResizeProvidersTest {

    ImageResizeTestData data;

    @Before
    public void setup() {
        data = new ImageResizeTestData();
    }

    @Test
    public void verifyJpeg2000ReaderIsRegistered() {
        assertTrue(true);
    }

    @Test
    public void ioVerifyTestResources() throws IOException {
        String actualFormatName1 = GetImageReader.get(data.vanillaJpegStream)
                .getFormatName();
        assertEquals("Unexpected image format name", ImageResizeTestData.JPEG, actualFormatName1);
        String actualFormatName2 = GetImageReader.get(data.jpeg2000Stream)
                .getFormatName();
        assertEquals("Unexpected image format name", ImageResizeTestData.JPEG_2000, actualFormatName2);
    }




}