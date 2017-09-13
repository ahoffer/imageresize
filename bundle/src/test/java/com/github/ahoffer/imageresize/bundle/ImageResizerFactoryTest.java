package com.github.ahoffer.imageresize.bundle;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.github.ahoffer.imageresize.api.ImageResizer;

public class ImageResizerFactoryTest {


    @Mock
    BundleContext bundleContext;

    @Mock
    ServiceReference<ImageResizer> sref;

ImageResizeTestData data;

    @Before
    public void setup() throws InvalidSyntaxException {
        data = new ImageResizeTestData();
        when(bundleContext.getAllServiceReferences(any(), isNull())).thenReturn(new ServiceReference[] {sref});
    }


}