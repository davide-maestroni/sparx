package sparx.util;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public interface LiveIterator<E> extends Iterator<E> {

  boolean hasNext(long timeout, @NotNull TimeUnit unit);

  E next(long timeout, @NotNull TimeUnit unit);
}
