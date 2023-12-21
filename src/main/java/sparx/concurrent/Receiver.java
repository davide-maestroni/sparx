package sparx.concurrent;

import java.io.Closeable;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface Receiver<V> extends Closeable {

  boolean fail(@NotNull Exception error);

  void set(V value);

  void setBulk(@NotNull Collection<V> values);

  void close();
}
