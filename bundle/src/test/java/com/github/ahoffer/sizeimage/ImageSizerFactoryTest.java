package com.github.ahoffer.sizeimage;

import com.github.ahoffer.sizeimage.provider.ImageSizerFactory;
import java.util.Collections;
import org.junit.Before;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ImageSizerFactoryTest {

  BundleContext bundleContext;
  ServiceReference<ImageSizer> sref;
  ImageSizer sizer;
  ImageSizerFactory factory;

  @Before
  public void setup() throws InvalidSyntaxException {
    bundleContext = mock(BundleContext.class);
    doReturn(Collections.singletonList(sref))
        .when(bundleContext)
        .getServiceReferences((Class) any(), any());
    sref = mock(ServiceReference.class);
    sizer = mock(ImageSizer.class);
    doReturn(sizer).when(bundleContext).getService(any());
    when(sizer.getNew()).thenReturn(sizer);
    factory = new ImageSizerFactory();
    factory.setBundleContext(bundleContext);
  }
}
