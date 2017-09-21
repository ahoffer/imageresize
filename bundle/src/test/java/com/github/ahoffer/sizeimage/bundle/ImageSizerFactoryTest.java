package com.github.ahoffer.sizeimage.bundle;

import com.github.ahoffer.sizeimage.ImageSizer;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ImageSizerFactoryTest {

    BundleContext bundleContext;

    ServiceReference<ImageSizer> sref;

    ImageSizer sizer;

    @Before
    public void setup() throws InvalidSyntaxException {
        bundleContext = mock(BundleContext.class);
        doReturn(Collections.singletonList(sref)).when(bundleContext)
                .getServiceReferences((Class) any(), any());
        sref = mock(ServiceReference.class);
        sizer = mock(ImageSizer.class);
        doReturn(sizer).when(bundleContext)
                .getService(any());
        when(sizer.getNew()).thenReturn(sizer);
    }

    @Test
    public void testGetImageSizers() {
        ImageSizerFactory factory = new ImageSizerFactory();
        factory.setBundleContext(bundleContext);
        List<ImageSizer> list = factory.getImageSizers();
        assertEquals("Expected a single object", 1, list.size());
        assertSame(sizer, list.get(0));
    }
}