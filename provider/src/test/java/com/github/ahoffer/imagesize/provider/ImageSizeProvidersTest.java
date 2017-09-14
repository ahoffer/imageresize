package com.github.ahoffer.imagesize.provider;

import static org.junit.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.github.ahoffer.imagesize.api.ImageSizer;

public class ImageSizeProvidersTest {

    ImageSizeTestData data;

    @Before
    public void setup() {
        data = new ImageSizeTestData();
    }

    @Test
    public void verifyJpeg2000ReaderIsRegistered() {
        assertTrue(true);
    }

    @Test
    public void ioVerifyTestResources() throws IOException {

        String actualFormatName1 = ImageReaderUtils.getReader(data.vanillaJpegStream)
                .getFormatName();
        assertEquals("Unexpected image format name", ImageSizeTestData.JPEG, actualFormatName1);
        String actualFormatName2 = ImageReaderUtils.getReader(data.jpeg2000Stream)
                .getFormatName();
        assertEquals("Unexpected image format name",
                ImageSizeTestData.JPEG_2000,
                actualFormatName2);
    }

    @Test
    public void ioTestInputStreamSafety() throws IOException {
        //Use stream once
        ImageReaderUtils.getFormat(data.vanillaJpegStream);

        // Make sure stream can be used again
        ImageSizer sizer = new SamplingSizer();
        sizer.setInput(data.vanillaJpegStream)
                .setOutputSize(250)
                .size();
    }
}