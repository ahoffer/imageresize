package com.github.ahoffer.sizeimage.support;

public class ImageReaderError extends RuntimeException {

  public ImageReaderError() {
    super();
  }

  public ImageReaderError(Throwable cause) {
    super(cause);
  }

  public ImageReaderError(String msg) {
    super(msg);
  }
}
