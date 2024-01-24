package sparx.util;

import org.jetbrains.annotations.NotNull;

public class Require {

  private Require() {
  }

  public static @NotNull <T> T notNull(final T ref, final String name) {
    if (ref == null) {
      throw new NullPointerException("'" + name + "' must not be null!");
    }
    return ref;
  }

  public static int positive(final int value, final String name) {
    if (value < 1) {
      throw new IllegalArgumentException("'" + name + "' must be positive, but is " + value);
    }
    return value;
  }

  public static long positive(final long value, final String name) {
    if (value < 1) {
      throw new IllegalArgumentException("'" + name + "' must be positive, but is " + value);
    }
    return value;
  }
}
