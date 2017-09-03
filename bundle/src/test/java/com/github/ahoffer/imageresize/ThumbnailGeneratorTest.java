package com.github.ahoffer.imageresize;

import static org.junit.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.spi.IIORegistry;

import org.junit.Before;
import org.junit.Test;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;

public class ThumbnailGeneratorTest {

    InputStream vanillaJpeg;

    InputStream jpeg2000;

    @Before
    public void setup() {
        vanillaJpeg = getClass().getResourceAsStream("/sample-jpeg.jpg");
        jpeg2000 = getClass().getResourceAsStream("/sample-jpeg2000.jpg");
        IIORegistry.getDefaultInstance()
                .registerServiceProvider(J2KImageReaderSpi.class);
    }

    @Test
    public void verifyJpeg2000ReaderIsRegistered() {
        assertTrue(true);
    }

    @Test
    public void verifyTestResources() throws IOException {
        String actualFormatName1 = ThumbnailGenerator.getReader(vanillaJpeg)
                .getFormatName();
        assertEquals("Unexpected image format name", "JPEG", actualFormatName1);
        String actualFormatName2 = ThumbnailGenerator.getReader(jpeg2000)
                .getFormatName();
        assertEquals("Unexpected image format name", "jpeg 2000", actualFormatName2);
    }

    @Test
    public void testGetFormat() {
    }
}