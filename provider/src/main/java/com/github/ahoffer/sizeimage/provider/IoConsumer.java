package com.github.ahoffer.sizeimage.provider;

import java.io.IOException;

// Thanks  to http://www.baeldung.com/java-lambda-exceptions
@FunctionalInterface
public interface IoConsumer<T> {

  void accept(T t) throws IOException;
}
