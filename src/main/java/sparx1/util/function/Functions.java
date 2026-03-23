/*
 * Copyright 2026 Davide Maestroni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sparx1.util.function;

import java.util.Comparator;
import sparx1.util.Require;
import sparx1.util.annotation.NotNull;

public class Functions {

  private static final IndexedPredicate<?> EQUALS_NULL = new IndexedPredicate<Object>() {
    @Override
    public boolean test(final int index, final Object param) {
      return param == null;
    }
  };
  private static final IndexedFunction<?, ?> INDEXED_IDENTITY = new IndexedFunction<Object, Object>() {
    @Override
    public Object apply(final int index, final Object param) {
      return param;
    }
  };
  private static final IndexedPredicate<?> NOT_EQUALS_NULL = new IndexedPredicate<Object>() {
    @Override
    public boolean test(final int index, final Object param) {
      return param != null;
    }
  };
  private static final IndexedPredicate<?> TEST_FALSE = new IndexedPredicate<Object>() {
    @Override
    public boolean test(final int index, final Object param) {
      return false;
    }
  };
  private static final IndexedPredicate<?> TEST_TRUE = new IndexedPredicate<Object>() {
    @Override
    public boolean test(final int index, final Object param) {
      return true;
    }
  };

  private Functions() {
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> IndexedPredicate<E> equalsElement(final Object element) {
    if (element == null) {
      return (IndexedPredicate<E>) EQUALS_NULL;
    }
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) {
        return element.equals(param);
      }
    };
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <P, R> IndexedFunction<P, R> indexedIdentity() {
    return (IndexedFunction<P, R>) INDEXED_IDENTITY;
  }

  public static @NotNull <P> IndexedPredicate<P> negated(
      final @NotNull IndexedPredicate<P> predicate) {
    return negated(predicate, "predicate");
  }

  public static @NotNull <P> IndexedPredicate<P> negated(
      final @NotNull IndexedPredicate<P> predicate, final String name) {
    Require.notNull(predicate, name);
    return new IndexedPredicate<P>() {
      @Override
      public boolean test(final int index, final P param) throws Exception {
        return !predicate.test(index, param);
      }
    };
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> IndexedPredicate<E> notEqualsElement(final Object element) {
    if (element == null) {
      return (IndexedPredicate<E>) NOT_EQUALS_NULL;
    }
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) {
        return !element.equals(param);
      }
    };
  }

  public static @NotNull <T> Comparator<T> reversed(final @NotNull Comparator<T> comparator) {
    return reversed(comparator, "comparator");
  }

  public static @NotNull <T> Comparator<T> reversed(final @NotNull Comparator<T> comparator,
      final String name) {
    Require.notNull(comparator, name);
    return new Comparator<T>() {
      @Override
      public int compare(final T o1, final T o2) {
        return comparator.compare(o2, o1);
      }
    };
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> IndexedPredicate<E> indexedFalse() {
    return (IndexedPredicate<E>) TEST_FALSE;
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> IndexedPredicate<E> indexedTrue() {
    return (IndexedPredicate<E>) TEST_TRUE;
  }

  public static @NotNull <E> IndexedConsumer<E> toIndexedConsumer(
      final @NotNull Consumer<E> consumer) {
    return toIndexedConsumer(consumer, "consumer");
  }

  public static @NotNull <E> IndexedConsumer<E> toIndexedConsumer(
      final @NotNull Consumer<E> consumer, final String name) {
    Require.notNull(consumer, name);
    return new IndexedConsumer<E>() {
      @Override
      public void accept(final int index, final E param) throws Exception {
        consumer.accept(param);
      }
    };
  }

  public static @NotNull <E, F> IndexedFunction<E, F> toIndexedFunction(
      final @NotNull Function<E, F> function) {
    return toIndexedFunction(function, "function");
  }

  public static @NotNull <E, F> IndexedFunction<E, F> toIndexedFunction(
      final @NotNull Function<E, F> function, final String name) {
    Require.notNull(function, name);
    return new IndexedFunction<E, F>() {
      @Override
      public F apply(final int index, final E parma) throws Exception {
        return function.apply(parma);
      }
    };
  }

  public static @NotNull <E> IndexedPredicate<E> toIndexedPredicate(
      final @NotNull Predicate<E> predicate) {
    return toIndexedPredicate(predicate, "predicate");
  }

  public static @NotNull <E> IndexedPredicate<E> toIndexedPredicate(
      final @NotNull Predicate<E> predicate, final String name) {
    Require.notNull(predicate, name);
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) throws Exception {
        return predicate.test(param);
      }
    };
  }

  public static @NotNull <E> IndexedPredicate<E> toNegatedIndexedPredicate(
      final @NotNull Predicate<E> predicate) {
    return toNegatedIndexedPredicate(predicate, "predicate");
  }

  public static @NotNull <E> IndexedPredicate<E> toNegatedIndexedPredicate(
      final @NotNull Predicate<E> predicate, final String name) {
    Require.notNull(predicate, name);
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) throws Exception {
        return !predicate.test(param);
      }
    };
  }
}
