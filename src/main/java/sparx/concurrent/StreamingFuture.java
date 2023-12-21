package sparx.concurrent;

import org.jetbrains.annotations.NotNull;

public interface StreamingFuture<V> extends StreamableFuture<V, StreamingFuture<V>> {

  @Override
  @NotNull StreamingFuture<V> readOnly();
}
