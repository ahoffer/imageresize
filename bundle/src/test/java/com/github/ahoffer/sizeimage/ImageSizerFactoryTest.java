package com.github.ahoffer.sizeimage;

import com.github.ahoffer.sizeimage.ImageSizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ImageSizerFactoryTest {

    BundleContext bundleContext;

    ServiceReference<ImageSizer> sref;

    ImageSizer sizer;

//    @Before
//    public void setup() throws InvalidSyntaxException {
//        bundleContext = mock(BundleContext.class);
//        doReturn(Collections.singletonList(sref)).when(bundleContext)
//                .getServiceReferences((Class) any(), any());
//        sref = mock(ServiceReference.class);
//        sizer = mock(ImageSizer.class);
//        doReturn(sizer).when(bundleContext)
//                .getService(any());
//        when(sizer.getNew()).thenReturn(sizer);
//    }

//    @Test
//    public void testGetImageSizers() {
//        ImageSizerFactory factory = new ImageSizerFactory();
//        factory.setBundleContext(bundleContext);
//        List<ImageSizer> list = factory.getImageSizers();
//        assertEquals("Expected a single object", 1, list.size());
//        assertSame(sizer, list.get(0));
//    }
}