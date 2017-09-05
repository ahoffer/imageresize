package com.github.ahoffer.imageresize.provider;

import static org.junit.Assert.assertEquals;
import static com.github.ahoffer.imageresize.provider.ImageResizeTestData.JPEG;
import static com.github.ahoffer.imageresize.provider.MagickResizer.INPUT_IMAGE_PATH;
import static com.github.ahoffer.imageresize.provider.MagickResizer.OUTPUT_FORMAT;
import static com.github.ahoffer.imageresize.provider.MagickResizer.PATH_TO_IMAGE_MAGICK_EXECUTABLES;
import static junit.framework.TestCase.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class MagickResizerTest {

    ImageResizeTestData data;

    public static final String PATH_TO_MAGICK_EXEC = "/opt/local/bin/";

    @Before
    public void setup() {
        data = new ImageResizeTestData();
    }

    @Test
    public void ioVerifyImageMagickExecutable() {
        //Determine if ImageMagick executable "convert" exists
        File execFile = new File(PATH_TO_MAGICK_EXEC + "convert");
        assertTrue("ImageMagick executable could not be found", execFile.exists());
        assertTrue("ImageMagick is not executable", execFile.canExecute());
    }

    @Test
    public void ioTestHappyPath() throws IOException {
        ImageResizer magick = new MagickResizer();
        Map<String, String> configuration = new HashMap<>();
        configuration.put(OUTPUT_FORMAT, JPEG);
        configuration.put(INPUT_IMAGE_PATH, data.vanillaJpegUrl.getFile());

        //TODO I wonder how this works on Windows? Or Linux?
        configuration.put(PATH_TO_IMAGE_MAGICK_EXECUTABLES, PATH_TO_MAGICK_EXEC);
        magick.setConfiguration(configuration);
        magick.setOutputSize(256);
        BufferedImage output = magick.resize();
        assertEquals(output.getWidth(), 256);
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
