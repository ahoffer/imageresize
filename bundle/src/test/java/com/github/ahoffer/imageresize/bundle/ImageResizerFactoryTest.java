package com.github.ahoffer.imageresize.bundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class ImageResizerFactoryTest {

    BundleContext bundleContext;

    ServiceReference<ImageResizer> sref;

    ImageResizer resizer;

    ImageResizeTestData data;

    @Before
    public void setup() throws InvalidSyntaxException {
        data = new ImageResizeTestData();
        bundleContext = mock(BundleContext.class);
        doReturn(Collections.singletonList(sref)).when(bundleContext)
                .getServiceReferences((Class) any(), any());
        sref = mock(ServiceReference.class);
        resizer = mock(ImageResizer.class);
        doReturn(resizer).when(bundleContext)
                .getService(any());
        when(resizer.getNew()).thenReturn(resizer);
    }

    @Test
    public void testGetImageResizers() {
        ImageResizerFactory factory = new ImageResizerFactory();
        factory.setBundleContext(bundleContext);
        List<ImageResizer> list = factory.getImageResizers();
        assertEquals("Expected a single object", 1, list.size());
        assertSame(resizer, list.get(0));
    }
}