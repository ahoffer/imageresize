package com.github.ahoffer.imagesize.provider;

import static org.junit.Assert.assertEquals;
import static com.github.ahoffer.imagesize.provider.ImageSizeTestData.JPEG;
import static com.github.ahoffer.imagesize.provider.MagickSizer.EXEC_NAME;
import static com.github.ahoffer.imagesize.provider.MagickSizer.INPUT_IMAGE_PATH;
import static com.github.ahoffer.imagesize.provider.MagickSizer.OUTPUT_FORMAT;
import static com.github.ahoffer.imagesize.provider.MagickSizer.PATH_TO_IMAGE_MAGICK_EXECUTABLES;
import static junit.framework.TestCase.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import com.github.ahoffer.imagesize.api.ImageSizer;

public class MagickSizerTest {

    public static final String TEST_PATH_TO_MAGICK_EXEC = FilenameUtils.getFullPath(
            "/opt/local/bin/");

    ImageSizeTestData data;

    @Before
    public void setup() {
        data = new ImageSizeTestData();
    }

    @Test
    public void ioTestHappyPathNoExecConfig() throws IOException {
        ImageSizer magick = new MagickSizer();
        Map<String, String> configuration = new HashMap<>();
        configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, TEST_PATH_TO_MAGICK_EXEC);
        configuration.put(OUTPUT_FORMAT, JPEG);
        configuration.put(INPUT_IMAGE_PATH, data.vanillaJpegUrl.getFile());
        magick.setConfiguration(configuration);
        assertTrue("Could not find the Image Magick Executable", magick.isAvailable());
        magick.setOutputSize(256);
        BufferedImage output = magick.size();
        assertEquals(output.getWidth(), 256);
    }

    @Test(expected = RuntimeException.class)
    public void ioTestWithBadExecConfig() throws IOException {
        ImageSizer magick = new MagickSizer();
        Map<String, String> configuration = new HashMap<>();
        configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, TEST_PATH_TO_MAGICK_EXEC);
        configuration.put(EXEC_NAME, "notvalid");
        magick.setConfiguration(configuration);
        magick.size();
    }

    @Test
    public void testSupportedFormats() {
        assertTrue("ImageMacgic should support JPEG", new MagickSizer().recommendedFor(JPEG));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetInput() {
        new MagickSizer().setInput(null);
    }
}
