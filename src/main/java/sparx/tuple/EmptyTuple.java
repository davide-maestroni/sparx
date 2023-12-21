package sparx.tuple;

import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import sparx.util.Nothing;

class EmptyTuple extends GenericTuple<Nothing> implements Empty<Nothing> {

  private static final EmptyTuple INSTANCE = new EmptyTuple();

  static @NotNull EmptyTuple instance() {
    return INSTANCE;
  }

  private EmptyTuple() {
    super(Collections.<Nothing>emptyList());
  }
}
