package belittle;

import java.io.IOException;

@FunctionalInterface
public interface IoConsumer<T> {

  void accept(T t) throws IOException;
}
