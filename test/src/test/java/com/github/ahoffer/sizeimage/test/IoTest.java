package com.github.ahoffer.sizeimage.test;

import com.github.ahoffer.sizeimage.ImageSizer;
import com.github.ahoffer.sizeimage.provider.*;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import javax.imageio.spi.IIORegistry;
import static junit.framework.TestCase.fail;
import org.apache.commons.io.FilenameUtils;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class IoTest {

    static {
        IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
    }

    public static final String TEST_PATH_TO_MAGICK_EXEC =
            FilenameUtils.getFullPath("/opt/local/bin/");

    public static final int PIXELS = 128;
    TestData data;

    @Before
    public void setup() {
        data = new TestData();
    }

    @Test
    public void testBasicSizer() throws IOException {
        doSize(new BasicSizer());
    }

    @Test
    public void testSamplingSizer() throws Exception {
        doSize(new SamplingSizer());
    }

    @Test
    public void testMagickSizer() throws IOException {
        ImageSizer sizer = new MagickSizer();
        HashMap configuration = new HashMap();
        configuration.put(MagickSizer.PATH_TO_IMAGE_MAGICK_EXECUTABLES, TEST_PATH_TO_MAGICK_EXEC);
        sizer.setConfiguration(configuration);
        assertThat(sizer.isAvailable(), equalTo(true));
        doSize(sizer);
    }

    private void doSize(ImageSizer sizer) throws IOException {
        sizer.setInput(data.vanillaJpegStream);
        sizer.setOutputSize(PIXELS, PIXELS);
        BufferedImage output = sizer.size();
        assertThat(output.getWidth(), equalTo(PIXELS));
        assertThat(output.getHeight(), org.hamcrest.Matchers.lessThanOrEqualTo(PIXELS));

        // Test the reference to the input stream is gone.
        try {
            ((AbstractImageSizer) sizer).validateBeforeResizing();
        } catch (NullPointerException e) {
            return;
        }

        fail("Should have thrown NPE");
    }

    @Test
    public void testGetMimeTypes() {
        List<String> mimeTypes = ImageReaderUtils.getMimeTypes(data.vanillaJpegStream);
        assertThat(mimeTypes, hasItem(equalToIgnoringCase("image/jpeg")));

        mimeTypes = ImageReaderUtils.getMimeTypes(data.jpeg2000Stream);
        assertThat(mimeTypes, containsInAnyOrder("image/jp2", "image/jpeg2000"));
    }

    @Test
    public void testPreservingInputStream() {
        // Use the same stream twice
        InputStream inputStream = data.vanillaJpegStream;
        assertThat(
                ImageReaderUtils.getMimeTypes(inputStream),
                equalTo(ImageReaderUtils.getMimeTypes(inputStream)));
    }
}