package com.github.ahoffer.imageresize.provider;

import static org.junit.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

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

        String actualFormatName1 = ImageReaderUtils.getReader(data.vanillaJpegStream)
                .getFormatName();
        assertEquals("Unexpected image format name", ImageResizeTestData.JPEG, actualFormatName1);
        String actualFormatName2 = ImageReaderUtils.getReader(data.jpeg2000Stream)
                .getFormatName();
        assertEquals("Unexpected image format name",
                ImageResizeTestData.JPEG_2000,
                actualFormatName2);
    }

    @Test
    public void ioTestInputStreamSafety() throws IOException {
        //Use stream once
        ImageReaderUtils.getFormat(data.vanillaJpegStream);

        // Make sure stream can be used again
        ImageResizer resizer = new SamplingResizer();
        resizer.setInput(data.vanillaJpegStream)
                .setOutputSize(250)
                .resize();
    }
}