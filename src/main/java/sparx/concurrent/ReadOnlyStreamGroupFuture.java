package sparx.concurrent;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import sparx.function.Function;

abstract class ReadOnlyStreamGroupFuture<V, F extends SignalFuture<V>> extends
    StreamGroupFuture<V, F> {

  private static <R> R fail() {
    throw new ReadOnlyException();
  }

  @Override
  public void close() {
    fail();
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    return fail();
  }

  @Override
  public void set(final V value) {
    fail();
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    fail();
  }

  @Override
  public void clear() {
    fail();
  }

  @Override
  public void compute(@NotNull final Function<? super V, ? extends V> function) {
    fail();
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }
}
