/*
 * Copyright 2024 Davide Maestroni
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
package sparx;

import java.util.Comparator;
import org.jetbrains.annotations.NotNull;
import sparx.internal.lazy.list.ListMaterializer;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;

class Sparx {

  private static final IndexedPredicate<?> EQUALS_NULL = new IndexedPredicate<Object>() {
    @Override
    public boolean test(final int index, final Object param) {
      return param == null;
    }
  };
  private static final IndexedPredicate<?> NOT_EQUALS_NULL = new IndexedPredicate<Object>() {
    @Override
    public boolean test(final int index, final Object param) {
      return param != null;
    }
  };

  static @NotNull <E> IndexedPredicate<E> elementsContains(
      @NotNull final ListMaterializer<E> elementsMaterializer) {
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) {
        return elementsMaterializer.materializeContains(param);
      }
    };
  }

  static @NotNull <E> IndexedPredicate<E> elementsNotContains(
      @NotNull final ListMaterializer<E> elementsMaterializer) {
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) {
        return !elementsMaterializer.materializeContains(param);
      }
    };
  }

  @SuppressWarnings("unchecked")
  static @NotNull <E> IndexedPredicate<E> equalsElement(final Object element) {
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

  static @NotNull <E> IndexedFunction<E, E> filteredMapper(
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
    return new IndexedFunction<E, E>() {
      @Override
      public E apply(final int index, final E element) throws Exception {
        if (predicate.test(index, element)) {
          return mapper.apply(index, element);
        }
        return element;
      }
    };
  }

  static @NotNull <E> IndexedFunction<E, E> filteredMapper(
      @NotNull final Predicate<? super E> predicate,
      @NotNull final Function<? super E, ? extends E> mapper) {
    return new IndexedFunction<E, E>() {
      @Override
      public E apply(final int index, final E element) throws Exception {
        if (predicate.test(element)) {
          return mapper.apply(element);
        }
        return element;
      }
    };
  }

  static @NotNull <E> IndexedFunction<E, E> filteredMapper(
      @NotNull final Predicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
    return new IndexedFunction<E, E>() {
      @Override
      public E apply(final int index, final E element) throws Exception {
        if (predicate.test(element)) {
          return mapper.apply(index, element);
        }
        return element;
      }
    };
  }

  static @NotNull <P> IndexedPredicate<P> negated(@NotNull final IndexedPredicate<P> predicate) {
    return new IndexedPredicate<P>() {
      @Override
      public boolean test(final int index, final P param) throws Exception {
        return !predicate.test(index, param);
      }
    };
  }

  @SuppressWarnings("unchecked")
  static @NotNull <E> IndexedPredicate<E> notEqualsElement(final Object element) {
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

  static @NotNull <E> IndexedFunction<E, E> replacementMapper(final E element) {
    return new IndexedFunction<E, E>() {
      @Override
      public E apply(final int index, final E ignored) {
        return element;
      }
    };
  }

  static @NotNull <T> Comparator<T> reversed(@NotNull final Comparator<T> comparator) {
    return new Comparator<T>() {
      @Override
      public int compare(final T o1, final T o2) {
        return comparator.compare(o2, o1);
      }
    };
  }

  static @NotNull <E> IndexedConsumer<E> toIndexedConsumer(@NotNull final Consumer<E> consumer) {
    return new IndexedConsumer<E>() {
      @Override
      public void accept(final int index, final E param) throws Exception {
        consumer.accept(param);
      }
    };
  }

  static @NotNull <E, F> IndexedFunction<E, F> toIndexedFunction(
      @NotNull final Function<E, F> function) {
    return new IndexedFunction<E, F>() {
      @Override
      public F apply(final int index, final E parma) throws Exception {
        return function.apply(parma);
      }
    };
  }

  static @NotNull <E> IndexedPredicate<E> toIndexedPredicate(
      @NotNull final Predicate<E> predicate) {
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) throws Exception {
        return predicate.test(param);
      }
    };
  }

  static @NotNull <E> IndexedPredicate<E> toNegatedIndexedPredicate(
      @NotNull final Predicate<E> predicate) {
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) throws Exception {
        return !predicate.test(param);
      }
    };
  }
}
