package sparx.concurrent;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.util.LiveIterator;

public class EmptyLiveIterator<E> implements LiveIterator<E> {

  private static final EmptyLiveIterator<?> INSTANCE = new EmptyLiveIterator<Object>();

  @SuppressWarnings("unchecked")
  static @NotNull <E> EmptyLiveIterator<E> instance() {
    return (EmptyLiveIterator<E>) INSTANCE;
  }

  private EmptyLiveIterator() {
  }

  @Override
  public boolean hasNext(final long timeout, @NotNull final TimeUnit unit) {
    return false;
  }

  @Override
  public E next(final long timeout, @NotNull final TimeUnit unit) {
    throw new NoSuchElementException();
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public E next() {
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove");
  }
}
