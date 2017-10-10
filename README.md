# SizeImage

## Introduction
SizeImage is a library for resizing images. It does not implement any algorithm
or decode any image formats. Instead, it provides an interface, `ImageSizer` that
can be implemented developers. The `ImageSizer` interface is simple. It includes 
method declarations: 
* A method to set an input stream that represents an image, such as a JPEG 
or GIF.
* Methods to get and set a configuration. The configuration can be used by 
implementors of `ImageSize`.
* A method to set the desired size of the output image.
* A method to initiate the sizing operation and return a `BufferedImage`


## Image Sizers
The library currently comes with three concrete implementations `ImageSizer`

* BasicSizer
* SamplingSizer
* MagickSizer

#### BasicSizer
 `BasicSizer` calls the library Thumbnailator to resize the image.
 
#### SamplingSizer 
`SamplingSizer` uses the class SamplingImageReader to subsample
 the image before calling Thumbnailator to resize the image. 
 
 For common formats like  GIF, JPEG, or PNG, the `SamplingSizer`
 is typically the fastest `ImageSizer`.
 Perhaps more importantly, it uses
 much less memory than the `BasicSizer`. The subsampling can degrade
 quality, but is usually not noticeable. The `SamplingImageReader` is discussed
 in another section. 
 
#### MagickSizer
**ImageMagick** is a mature and popular image processing tool. It 
 is not written in Java and does not run in the JVM. Because ImageMagick
runs natively, using ImageMagick can be much faster image manipulation inside the JVM.

ImageMagick must be downloaded and installed,
 or built from source. However, most package managers offer ImageMagick. 

The 
 MagickSizer typically needs to be configured with the  path to the ImageMagick
  execution named
 `convert` (or `convert.exe` on Windows). **SizeImage** does not try to read the environments `PATH` 
 variable or set the `MagickSizer`'s path. Users are expected to pass in the absolute 
 path to the ImageMagick's `convert` executable before using the `MagickSizer`.
 
### Understanding Output Size
All concrete sizers are be implemented such that the sizer will attempt to create an image 
the whose width is the same number of pixels as the sizer's `maxWdith` property. Sizing is 
contrained by two rules:
* The aspect ratio of the original image is preserved. 
* If the image to be sized has a portrait orientation, i.e. it is taller than it is wide,
the aspect ration will be preserved, but the output image's height will have the same 
number of pixels as `maxHeight`. 

That is, `maxWdith` and `maxHeight` define a bounding box. the resized image will always
fit inside that box. 
 
 
## ImageSizerFactory
 The ImageSizerFactory is the primary way to interact with this library. It is meant 
 to be instantiated and configured, then used as the entry point to access the concrete
 ImageSizers. 
 

#### ImageSizerFactory API
The public methods fall into two groups:

* Methods for managing configuration
* Methods for getting image sizers
* Facade method

### Configuration API

The ImageSizerFactory is mostly a configurable factory. The configuration associates
the MIME type of an image format to an ordered list of ImageSizer instances. The list is ordered
from the most preferred ImageSizer for a MIME type to the least preferred ImageSizer for
that MIME type. 

The configuration references a _prototype_ instance of an ImageSizer. 
The ImageSizerFactory creates a new instances from the prototypes. 

* Configure the rules for the factory ()
  * `setConfiguration` 
  * `getConfiguration`

* Set size of output image, in pixels
  * `setOutputSize(int maxWidth, int maxHeight)
  * If present, the image sizer will set the values for width and height on image sizers 
  before returning them to the caller. 
  * This functionality exists because when generating thumbnails, one usually expects all 
  thubmnails to be the same size. This method provides a convenient way to set the values
  in a single place, instead of configuring output size for multiple sizers.

* Sizer of output image, in pixels
  * `setMaxHeight`
  * `getMaxHeight`


#### Sample ImageSizerFactory Configuration

Sample configuration

| MIME Type  | Sizers | 
| ---------  | ------ |
| image/tga  | MagicSizer |
| image/jpeg | Magic Sizer, SamplingSizer |
| *          | SamplingSizer, MagicSizer, BasicSizer | 

The configuration above has three rules.
* It associates the MIME type for a TGA image,`image/tga`, with a `MagicSizer`.
* It associates the MIME type for a JPEG image, `imager/jpeg` with a `MagickSizer` 
followed by a `SamplingSizer`
* It associates the wildcard, `*` with:`SamplingSizer`, `MagicSizer`, and `BasicSizer`.

Here is the definition of the sample configuration in Java:

```java
Map<String, List<ImageSizer>> configuration = new HashMap<>();
configurations.put("image/jpeg", 
    Arrays.asList(new SamplingSizer()));
configuration.put("image/tga", 
    Arrays.asList(new MagickSizer()));
configuration.put("*", 
    Arrays.asList(new SamplingSizer(),
        new MagickSizer(),
        new BasicSizer()));
```

### Factory API
The factory methods return either a list of image sizers
(possibly an empty list), or a Java `Optional` that contains a single image sizer. 
This group of methods accepts one of two types of parameters. They 
either accept the MIME type (as a String), or an input stream of an image. 

How do the factory methods behave with the sample configuration? 
Before answering that question, let's assume that the ImageMagick library is not installed
on the machine, so `MagicSizer` is not actually available, even though it appears in the 
configuration.

Here is how the ImageSizerFactory behaves with the sample configuration:

 | Method                 | Input               | Output                                      |
 | ---------------------- | ------------        | ------------------------------------------- |
 | `getRecommendedSizers` | "image\jpeg"        | List with one item, `SamplingSizer`, as configured.
 | `getRecommendedSizers` | "image\tga"         | List of size 2: {`SamplingSizer`, `BasicSizer`}. 
                                                  The factory successfully matches the input to `MagicSizer`, 
                                                  but `MagicSizer` is unavailable and is filtered out of the results. 
                                                  The factory uses the wildcard sizers (`*` in the configuration) |
 | `getRecommendedSizers` | "image\tga", false  | List with one item, `MagickSizer`. 
                                                  The input `false` means "do not to filter out unavailable results". |
 | `getRecommendedSizers` | "image\foo"         | List of size 2: {`SamplingSizer`, `BasicSizer`}. 
                                                  The MIME type "image\foo" is not configured, so the wildcard 
                                                  sizers are used. The `MagicSizer` is filtered out because 
                                                  it is not available |
 | `getRecommendedSizers` | InputStream of JPEG image | List with one item, `SamplingSizer`. 
 | `getRecommendedSizers` | InputStream of TGA image  | List of size 2: {`SamplingSizer`, `BasicSizer`}. 
 | `getRecommendedSizers` | `null`              | List of size 2: {`SamplingSizer`, `BasicSizer`}. 
                                                  The wildcard sizers are selected, but `ImageMagick` 
                                                 is filtered out because it is not available. |

 
 | Method                 | Input               | Output |
 | ---------------------- | ------------        | -------- |
 | `getRecommendedSizer`  | "image\jpeg"        | `Optional` containing instance of `SamplingSizer`, as configured |
 | `getRecommendedSizer`  | "image\foo"         | `Optional` containing instance of `SamplingSizer` because it is the 
                                                   first sizer in the list of wildcard sizers |


### Facade API
 It also has a facade method, `size`. Assuming the `maxWidth` and `maxHeight` are set, 
 factory will find the best sizer and perfrom the resize.
 
#### JPEG 2000
The JPEG 2000 decoder that is freely available for Java is slow, and the 
results are sometimes not what is expected. There is no really good free and open 
JPEG 2000 decoder implemented in Java. 

The best option free and open JPEG 200 decoder is to use the 
[OpenJPEG library](http://www.openjpeg.org/). 
ImageMagick is often packaged with the OpenJPEG 
library and will use it to process JPEG 2000 images. It is at least an order of
magnitude faster than the Java JAI module for JPEG 2000.

#### TODO
1. Write readme
    1.1. Suggest new `ImageSizers` for OpenJPEG
2  Write a sizer for that geo image library Derek is using
2. Create Karaf feature.


