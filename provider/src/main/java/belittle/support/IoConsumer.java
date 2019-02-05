package belittle.support;

import java.io.IOException;

@FunctionalInterface
public interface IoConsumer<T> {

  void accept(T t) throws IOException;
}

// static <T> Consumer<T> throwingConsumerWrapper(
//    ThrowingConsumer<T, Exception> throwingConsumer) {
//
//    return i -> {
//    try {
//    throwingConsumer.accept(i);
//    } catch (Exception ex) {
//    throw new RuntimeException(ex);
//    }
//    };
//    }
