##Configuring Sizers

For       |      Property   | Description 
--------- | --------------- | ----------- 
All       | TIMEOUT_SECONDS  | The number of seconds a sizer is allowed to run before it is terminated. If no value is specified, a default value of 30 seconds is used.
All       | MAX_WIDTH        | Desired maximum width, in pixels. 
All       | MAX_HEIGHT       | Desired maximum height, in pixels.
MagicSizer             | WINDOWS_SEARCH_PATH       | Todo: let users set search path like C:\aaa;bloop\foo bar;
MagicSizer             | POSIX_SEARCH_PATH         | Todo: let users set search path like  /foo:/bar/:/usr/local/bin
OpenJpeg200Sizer       | WINDOWS_SEARCH_PATH       | Todo: let users set search path like C:\aaa;bloop\foo bar;
OpenJpeg200Sizer       | POSIX_SEARCH_PATH         | Todo: let users set search path like  /foo:/bar/:/usr/local/bin
JaiJpeg2000Sizer       | DEFAULT_BITS_PER_PIXEL    | Bit rate. Controls quality. Higher values mean higher quality, but more resources consumed. The default value is 0.3 which is reasonable for thumbnails. 
MagickSizer            | OUTPUT_FORMAT_KEY         | MagickSizer creates an output image after processing this controls the type of image it outputs.  writes the image to disk, processes it, sand sends the output to standard out. The default is format is BMP because it is very fast to create and uses lossless compression. 
SamplingSizer          | SAMPLING_PERIOD_KEY       | The SamplingSizer will try to calculate the correct sampling frequency to use based on the width and height of the image. The SamplingSizer gets the width and heigh from image metadata. To override this behavior, set the sampling period and the SamplingSizer will always use that value.
