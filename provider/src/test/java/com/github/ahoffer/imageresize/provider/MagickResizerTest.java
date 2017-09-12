package com.github.ahoffer.imageresize.provider;

import static org.junit.Assert.assertEquals;
import static com.github.ahoffer.imageresize.provider.ImageResizeTestData.JPEG;
import static com.github.ahoffer.imageresize.provider.MagickResizer.EXEC_NAME;
import static com.github.ahoffer.imageresize.provider.MagickResizer.INPUT_IMAGE_PATH;
import static com.github.ahoffer.imageresize.provider.MagickResizer.OUTPUT_FORMAT;
import static com.github.ahoffer.imageresize.provider.MagickResizer.PATH_TO_IMAGE_MAGICK_EXECUTABLES;
import static junit.framework.TestCase.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class MagickResizerTest {

    public static final String TEST_PATH_TO_MAGICK_EXEC = FilenameUtils.getFullPath(
            "/opt/local/bin/");

    ImageResizeTestData data;

    @Before
    public void setup() {
        data = new ImageResizeTestData();
    }

    @Test
    public void ioTestHappyPathNoExecConfig() throws IOException {
        ImageResizer magick = new MagickResizer();
        Map<String, String> configuration = new HashMap<>();
        configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, TEST_PATH_TO_MAGICK_EXEC);
        configuration.put(OUTPUT_FORMAT, JPEG);
        configuration.put(INPUT_IMAGE_PATH, data.vanillaJpegUrl.getFile());
        magick.setConfiguration(configuration);
        assertTrue("Could not find the Image Magick Executable", magick.isAvailable());
        magick.setOutputSize(256);
        BufferedImage output = magick.resize();
        assertEquals(output.getWidth(), 256);
    }

    @Test(expected = RuntimeException.class)
    public void ioTestWithBadExecConfig() throws IOException {
        ImageResizer magick = new MagickResizer();
        Map<String, String> configuration = new HashMap<>();
        configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, TEST_PATH_TO_MAGICK_EXEC);
        configuration.put(EXEC_NAME, "notvalid");
        magick.setConfiguration(configuration);
        magick.resize();
    }

    @Test
    public void testSupportedFormats() {
        assertTrue("ImageMacgic should support JPEG", new MagickResizer().recommendedFor(JPEG));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetInput() {
        new MagickResizer().setInput(null);
    }
}
