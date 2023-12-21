package sparx.tuple;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface Tuple<T> {

  @NotNull List<T> asList();
}
