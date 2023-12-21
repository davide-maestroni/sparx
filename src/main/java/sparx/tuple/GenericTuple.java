package sparx.tuple;

import java.util.List;
import org.jetbrains.annotations.NotNull;

class GenericTuple<T> implements Tuple<T> {

  private final List<T> values;

  GenericTuple(@NotNull final List<T> values) {
    this.values = values;
  }

  @Override
  public @NotNull List<T> asList() {
    return values;
  }
}
