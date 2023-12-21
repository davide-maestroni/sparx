package sparx.concurrent;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import sparx.function.Function;

public class ReadOnlyFuture<V> extends DecoratedFuture<V> {

  private static <R> R fail() {
    throw new ReadOnlyException();
  }

  public ReadOnlyFuture(@NotNull final StreamingFuture<V> future) {
    super(future);
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
  public @NotNull StreamingFuture<V> readOnly() {
    return this;
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
  public void close() {
    fail();
  }
}
