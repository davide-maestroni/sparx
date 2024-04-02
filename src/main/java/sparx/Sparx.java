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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.collection.AbstractListSequence;
import sparx.collection.ListMaterializer;
import sparx.collection.ListSequence;
import sparx.collection.Sequence;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

public class Sparx {

  private static final EmptyListMaterializer<Object> EMPTY_MATERIALIZER = new EmptyListMaterializer<Object>();
  private static final Predicate<?> EQUALS_NULL = new Predicate<Object>() {
    @Override
    public boolean test(final Object param) {
      return param == null;
    }
  };
  private static final Predicate<?> NOT_EQUALS_NULL = new Predicate<Object>() {
    @Override
    public boolean test(final Object param) {
      return param != null;
    }
  };

  private Sparx() {
  }

  @SuppressWarnings("unchecked")
  private static @NotNull <E> Predicate<E> equalsElement(final Object element) {
    if (element == null) {
      return (Predicate<E>) EQUALS_NULL;
    }
    return new Predicate<E>() {
      @Override
      public boolean test(final E param) {
        return element.equals(param);
      }
    };
  }

  private static @NotNull <P> Predicate<P> negated(@NotNull final Predicate<P> predicate) {
    Require.notNull(predicate, "predicate");
    return new Predicate<P>() {
      @Override
      public boolean test(final P param) throws Exception {
        return !predicate.test(param);
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static @NotNull <E> Predicate<E> notEqualsElement(final Object element) {
    if (element == null) {
      return (Predicate<E>) NOT_EQUALS_NULL;
    }
    return new Predicate<E>() {
      @Override
      public boolean test(final E param) {
        return !element.equals(param);
      }
    };
  }

  private static @NotNull <T> Comparator<T> reversed(@NotNull final Comparator<T> comparator) {
    Require.notNull(comparator, "comparator");
    return new Comparator<T>() {
      @Override
      public int compare(final T o1, final T o2) {
        return comparator.compare(o2, o1);
      }
    };
  }

  public static class lazy {

    private lazy() {
    }

    public static class List<E> extends AbstractListSequence<E> {

      private static final List<?> EMPTY_LIST = new List<Object>(EMPTY_MATERIALIZER);
      private static final List<Boolean> FALSE_LIST = new List<Boolean>(
          new ElementToListMaterializer<Boolean>(false));
      private static final List<Boolean> TRUE_LIST = new List<Boolean>(
          new ElementToListMaterializer<Boolean>(false));
      private static final List<Integer> ZERO_LIST = new List<Integer>(
          new ElementToListMaterializer<Integer>(0));
      private static final Function<? extends java.util.List<?>, ? extends List<?>> FROM_JAVA_LIST = new Function<java.util.List<Object>, List<Object>>() {
        @Override
        public List<Object> apply(final java.util.List<Object> param) {
          return new List<Object>(new ListToListMaterializer<Object>(param));
        }
      };

      private final ListMaterializer<E> materializer;

      private List(@NotNull final ListMaterializer<E> materializer) {
        super(materializer);
        this.materializer = materializer;
      }

      public static @NotNull <E> List<E> from(@NotNull final Iterable<E> elements) {
        final ArrayList<E> list = new ArrayList<E>();
        for (final E element : elements) {
          list.add(element);
        }
        return new List<E>(new ListToListMaterializer<E>(list));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of() {
        return (List<E>) EMPTY_LIST;
      }

      public static @NotNull <E> List<E> of(final E first) {
        return new List<E>(new ElementToListMaterializer<E>(first));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second) {
        return new List<E>(new ArrayToListMaterializer<E>(first, second));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third) {
        return new List<E>(new ArrayToListMaterializer<E>(first, second, third));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third,
          final E fourth) {
        return new List<E>(new ArrayToListMaterializer<E>(first, second, third, fourth));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth) {
        return new List<E>(
            new ArrayToListMaterializer<E>(first, second, third, fourth, fifth));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth) {
        return new List<E>(
            new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth, final E seventh) {
        return new List<E>(
            new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth,
                seventh));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth, final E seventh, final E eighth) {
        return new List<E>(
            new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth,
                seventh, eighth));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
          final E ninth) {
        return new List<E>(
            new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth,
                seventh, eighth, ninth));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
          final E ninth, final E tenth) {
        return new List<E>(
            new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth,
                seventh, eighth, ninth, tenth));
      }

      public static @NotNull <E> List<E> wrap(@NotNull final Iterable<E> elements) {
        return new List<E>(getElementsMaterializer(elements));
      }

      @SuppressWarnings("unchecked")
      private static @NotNull <E> ListMaterializer<E> getElementsMaterializer(
          @NotNull final Iterable<? extends E> elements) {
        if (elements instanceof List) {
          return ((List<E>) elements).materializer;
        }
        if (elements instanceof java.util.List) {
          final java.util.List<E> list = (java.util.List<E>) elements;
          if (list.isEmpty()) {
            return (ListMaterializer<E>) EMPTY_MATERIALIZER;
          }
          return new ListToListMaterializer<E>(list);
        }
        if (elements instanceof Collection) {
          final Collection<E> collection = (Collection<E>) elements;
          if (collection.isEmpty()) {
            return (ListMaterializer<E>) EMPTY_MATERIALIZER;
          }
          return new CollectionToListMaterializer<E>(collection);
        }
        return new IteratorToListMaterializer<E>((Iterator<E>) elements.iterator());
      }

      private static @NotNull <E, F> Function<E, ListMaterializer<F>> getElementToMaterializer(
          @NotNull final Function<? super E, ? extends Iterable<? extends F>> mapper) {
        return new Function<E, ListMaterializer<F>>() {
          @Override
          public ListMaterializer<F> apply(final E element) throws Exception {
            return getElementsMaterializer(mapper.apply(element));
          }
        };
      }

      @Override
      public @NotNull List<Boolean> all(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return FALSE_LIST;
        }
        return new List<Boolean>(new AllListMaterializer<E>(materializer, predicate, false));
      }

      @Override
      public @NotNull List<E> append(final E element) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of(element);
        }
        return new List<E>(new AppendListMaterializer<E>(materializer, element));
      }

      @Override
      public @NotNull List<E> appendAll(@NotNull final Iterable<? extends E> elements) {
        final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(elements);
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(elementsMaterializer);
        }
        return new List<E>(new AppendAllListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public <T> T apply(@NotNull final Function<? super Sequence<E>, T> mapper) {
        return null;
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull <F> List<F> as() {
        return (List<F>) this;
      }

      @Override
      public @NotNull List<Integer> count() {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return ZERO_LIST;
        }
        return new List<Integer>(new CountListMaterializer<E>(materializer));
      }

      @Override
      public @NotNull List<Integer> count(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return ZERO_LIST;
        }
        return new List<Integer>(new CountWhereListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> countNot(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return ZERO_LIST;
        }
        return new List<Integer>(
            new CountWhereListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public void doFor(@NotNull final Consumer<? super E> consumer) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return;
        }
        try {
          if (materializer.knownSize() == 1) {
            consumer.accept(materializer.materializeElement(0));
          } else {
            int i = 0;
            while (materializer.canMaterializeElement(i)) {
              consumer.accept(materializer.materializeElement(i));
              ++i;
            }
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }

      @Override
      public void doUntil(@NotNull final Predicate<? super E> condition,
          @NotNull final Consumer<? super E> consumer) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return;
        }
        try {
          if (materializer.knownSize() == 1) {
            final E element = materializer.materializeElement(0);
            if (!condition.test(element)) {
              consumer.accept(element);
            }
          } else {
            int i = 0;
            while (materializer.canMaterializeElement(i)) {
              final E next = materializer.materializeElement(i);
              if (condition.test(next)) {
                break;
              }
              consumer.accept(next);
              ++i;
            }
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }

      @Override
      public void doUntil(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return;
        }
        try {
          if (materializer.knownSize() == 1) {
            predicate.test(materializer.materializeElement(0));
          } else {
            int i = 0;
            while (materializer.canMaterializeElement(i)) {
              if (predicate.test(materializer.materializeElement(i))) {
                break;
              }
              ++i;
            }
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }

      @Override
      public void doWhile(@NotNull final Predicate<? super E> condition,
          @NotNull final Consumer<? super E> consumer) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return;
        }
        try {
          if (materializer.knownSize() == 1) {
            final E element = materializer.materializeElement(0);
            if (condition.test(element)) {
              consumer.accept(element);
            }
          } else {
            int i = 0;
            while (materializer.canMaterializeElement(i)) {
              final E next = materializer.materializeElement(i);
              if (!condition.test(next)) {
                break;
              }
              consumer.accept(next);
              ++i;
            }
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }

      @Override
      public void doWhile(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return;
        }
        try {
          if (materializer.knownSize() == 1) {
            predicate.test(materializer.materializeElement(0));
          } else {
            int i = 0;
            while (materializer.canMaterializeElement(i)) {
              if (!predicate.test(materializer.materializeElement(i))) {
                break;
              }
              ++i;
            }
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }

      @Override
      public @NotNull List<E> drop(final int maxElements) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (maxElements <= 0 || knownSize == 0) {
          return this;
        }
        if (knownSize > 0 && maxElements >= knownSize) {
          return List.of();
        }
        return new List<E>(new DropListMaterializer<E>(materializer, maxElements));
      }

      @Override
      public @NotNull List<E> dropUntil(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropWhileListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull List<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropWhileListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> dropRight(final int maxElements) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (maxElements <= 0 || knownSize == 0) {
          return this;
        }
        if (knownSize > 0 && maxElements >= knownSize) {
          return List.of();
        }
        return new List<E>(new DropRightListMaterializer<E>(materializer, maxElements));
      }

      @Override
      public @NotNull List<E> dropRightUntil(@NotNull Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropRightWhileListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull List<E> dropRightWhile(@NotNull Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropRightWhileListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Boolean> endsWith(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Boolean>(
            new EndsWithListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return FALSE_LIST;
        }
        return new List<Boolean>(
            new ExistsListMaterializer<E>(materializer, predicate, false));
      }

      @Override
      public @NotNull List<E> filter(@NotNull final Predicate<? super E> predicate) {
        return removeWhereNot(predicate);
      }

      @Override
      public @NotNull List<E> filterNot(@NotNull final Predicate<? super E> predicate) {
        return removeWhere(predicate);
      }

      @Override
      public @NotNull List<E> findAny(@NotNull final Predicate<? super E> predicate) {
        return findFirst(predicate);
      }

      @Override
      public @NotNull List<E> findAnyNot(@NotNull final Predicate<? super E> predicate) {
        return findFirstNot(predicate);
      }

      @Override
      public @NotNull List<E> findFirst(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new FindFirstListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> findFirstNot(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new FindFirstListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull List<Integer> findIndexOf(final Object element) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(
            new FindIndexListMaterializer<E>(materializer, equalsElement(element)));
      }

      @Override
      public @NotNull List<Integer> findIndexWhere(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(new FindIndexListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> findIndexWhereNot(
          @NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(
            new FindIndexListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull List<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Integer>(
            new FindIndexOfSliceListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> findLast(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new FindLastListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> findLastIndexOf(final Object element) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(
            new FindLastIndexListMaterializer<E>(materializer, equalsElement(element)));
      }

      @Override
      public @NotNull List<Integer> findLastIndexWhere(
          @NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(new FindLastIndexListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> findLastIndexWhereNot(
          @NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(
            new FindLastIndexListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull List<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Integer>(new FindLastIndexOfSliceListMaterializer<E>(materializer,
            elementsMaterializer));
      }

      @Override
      public @NotNull List<E> findLastNot(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new FindLastListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull <F> List<F> flatMap(
          @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<F>(new SingleFlatMapListMaterializer<E, F>(materializer,
              getElementToMaterializer(mapper)));
        }
        return new List<F>(new FlatMapListMaterializer<E, F>(materializer, mapper));
      }

      @Override
      public @NotNull List<E> flatMapAfter(final int numElements,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        if (numElements < 0) {
          return this;
        }
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
          return this;
        }
        if (numElements == 0 && knownSize == 1) {
          return new List<E>(new SingleFlatMapListMaterializer<E, E>(materializer,
              getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapAfterListMaterializer<E>(materializer, numElements,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull ListSequence<E> flatMapFirstWhere(
          @NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer, predicate,
              getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapFirstWhereListMaterializer<E>(materializer, predicate,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull ListSequence<E> flatMapFirstWhereNot(
          @NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<E>(
              new SingleFlatMapWhereListMaterializer<E>(materializer, negated(predicate),
                  getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapFirstWhereListMaterializer<E>(materializer,
            negated(predicate), getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull ListSequence<E> flatMapLastWhere(
          @NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer, predicate,
              getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapLastWhereListMaterializer<E>(materializer, predicate,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull ListSequence<E> flatMapLastWhereNot(
          @NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<E>(
              new SingleFlatMapWhereListMaterializer<E>(materializer, negated(predicate),
                  getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapLastWhereListMaterializer<E>(materializer,
            negated(predicate), getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull ListSequence<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer, predicate,
              getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapWhereListMaterializer<E>(materializer, predicate, mapper));
      }

      @Override
      public @NotNull ListSequence<E> flatMapWhereNot(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer,
              negated(predicate), getElementToMaterializer(mapper)));
        }
        return new List<E>(
            new FlatMapWhereListMaterializer<E>(materializer, negated(predicate), mapper));
      }

      @Override
      public @NotNull <F> List<F> fold(final F identity,
          @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
        return foldLeft(identity, operation);
      }

      @Override
      public @NotNull <F> List<F> foldLeft(final F identity,
          @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of(identity);
        }
        return new List<F>(
            new FoldLeftListMaterializer<E, F>(materializer, identity, operation));
      }

      @Override
      public @NotNull <F> List<F> foldRight(final F identity,
          @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of(identity);
        }
        return new List<F>(
            new FoldRightListMaterializer<E, F>(materializer, identity, operation));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull List<? extends List<E>> group(final int maxSize) {
        Require.positive(maxSize, "maxSize");
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<List<E>>(new GroupListMaterializer<E, List<E>>(materializer, maxSize,
            (Function<? super java.util.List<E>, ? extends List<E>>) FROM_JAVA_LIST));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull List<? extends List<E>> group(final int size, final E filler) {
        Require.positive(size, "size");
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        if (size == 1) {
          return group(1);
        }
        return new List<List<E>>(new GroupListMaterializer<E, List<E>>(materializer, size, filler,
            (Function<? super java.util.List<E>, ? extends List<E>>) FROM_JAVA_LIST));
      }

      @Override
      public @NotNull List<Boolean> includes(final Object element) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return FALSE_LIST;
        }
        return new List<Boolean>(
            new ExistsListMaterializer<E>(materializer, equalsElement(element), false));
      }

      @Override
      public @NotNull List<Boolean> includesAll(@NotNull final Iterable<?> elements) {
        return new List<Boolean>(new IncludesAllListMaterializer<E>(materializer, elements));
      }

      @Override
      public @NotNull List<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Boolean>(
            new IncludesSliceListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> insertAfter(final int numElements, final E element) {
        if (numElements < 0) {
          return this;
        }
        if (numElements == 0) {
          return prepend(element);
        }
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return this;
        }
        if (knownSize > 0) {
          if (knownSize < numElements) {
            return this;
          }
          if (knownSize == numElements) {
            return append(element);
          }
        }
        return new List<E>(new InsertAfterListMaterializer<E>(materializer, numElements, element));
      }

      @Override
      public @NotNull List<E> insertAllAfter(final int numElements,
          @NotNull final Iterable<? extends E> elements) {
        if (numElements < 0) {
          return this;
        }
        if (numElements == 0) {
          return prependAll(elements);
        }
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return this;
        }
        if (knownSize > 0) {
          if (knownSize < numElements) {
            return this;
          }
          if (knownSize == numElements) {
            return appendAll(elements);
          }
        }
        return new List<E>(new InsertAllAfterListMaterializer<E>(materializer, numElements,
            getElementsMaterializer(elements)));
      }

      @Override
      public @NotNull Iterator<E> iterator() {
        return materializer.materializeIterator();
      }

      @Override
      public @NotNull <F> List<F> map(@NotNull final Function<? super E, F> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<F>(new SingleMapListMaterializer<E, F>(materializer, mapper));
        }
        return new List<F>(new MapListMaterializer<E, F>(materializer, mapper));
      }

      @Override
      public @NotNull List<E> mapAfter(final int numElements,
          @NotNull final Function<? super E, ? extends E> mapper) {
        if (numElements < 0) {
          return this;
        }
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return this;
        }
        if (numElements == 0 && knownSize == 1) {
          return new List<E>(new SingleMapListMaterializer<E, E>(materializer, mapper));
        }
        return new List<E>(new MapAfterListMaterializer<E>(materializer, numElements, mapper));
      }

      @Override
      public @NotNull List<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return this;
        }
        if (knownSize == 1) {
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, predicate, mapper));
        }
        return new List<E>(new MapFirstWhereListMaterializer<E>(materializer, predicate, mapper));
      }

      @Override
      public @NotNull List<E> mapFirstWhereNot(
          @NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return this;
        }
        if (knownSize == 1) {
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, negated(predicate), mapper));
        }
        return new List<E>(
            new MapFirstWhereListMaterializer<E>(materializer, negated(predicate), mapper));
      }

      @Override
      public @NotNull List<E> mapLastWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return this;
        }
        if (knownSize == 1) {
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, predicate, mapper));
        }
        return new List<E>(new MapLastWhereListMaterializer<E>(materializer, predicate, mapper));
      }

      @Override
      public @NotNull List<E> mapLastWhereNot(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return this;
        }
        if (knownSize == 1) {
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, negated(predicate), mapper));
        }
        return new List<E>(
            new MapLastWhereListMaterializer<E>(materializer, negated(predicate), mapper));
      }

      @Override
      public @NotNull List<E> mapWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new MapWhereListMaterializer<E>(materializer, predicate, mapper));
      }

      @Override
      public @NotNull List<E> mapWhereNot(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new MapWhereListMaterializer<E>(materializer, negated(predicate), mapper));
      }

      @Override
      public @NotNull List<E> max(@NotNull final Comparator<? super E> comparator) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new MaxListMaterializer<E>(materializer, comparator));
      }

      @Override
      public @NotNull List<E> min(@NotNull final Comparator<? super E> comparator) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new MaxListMaterializer<E>(materializer, reversed(comparator)));
      }

      @Override
      public @NotNull List<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return TRUE_LIST;
        }
        return new List<Boolean>(
            new ExistsListMaterializer<E>(materializer, negated(predicate), true));
      }

      @Override
      public @NotNull List<Boolean> notExists(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return TRUE_LIST;
        }
        return new List<Boolean>(
            new AllListMaterializer<E>(materializer, negated(predicate), true));
      }

      @Override
      public @NotNull List<E> orElse(@NotNull final Iterable<E> elements) {
        final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(elements);
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(elementsMaterializer);
        }
        return new List<E>(new OrElseListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> orElseGet(
          @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(new SuppliedMeterializer<E>(supplier));
        }
        return new List<E>(
            new OrElseListMaterializer<E>(materializer, new SuppliedMeterializer<E>(supplier)));
      }

      @Override
      public @NotNull List<E> peek(@NotNull final Consumer<? super E> consumer) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new PeekListMaterializer<E>(materializer, consumer));
      }

      @Override
      public @NotNull List<E> plus(final E element) {
        return append(element);
      }

      @Override
      public @NotNull List<E> plusAll(@NotNull final Iterable<E> elements) {
        return appendAll(elements);
      }

      @Override
      public @NotNull List<E> prepend(final E element) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of(element);
        }
        return new List<E>(new PrependListMaterializer<E>(materializer, element));
      }

      @Override
      public @NotNull List<E> prependAll(@NotNull final Iterable<? extends E> elements) {
        final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(elements);
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(elementsMaterializer);
        }
        return new List<E>(new PrependAllListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> reduce(
          @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
        return reduceLeft(operation);
      }

      @Override
      public @NotNull List<E> reduceLeft(
          @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new ReduceLeftListMaterializer<E>(materializer, operation));
      }

      @Override
      public @NotNull List<E> reduceRight(
          @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new ReduceRightListMaterializer<E>(materializer, operation));
      }

      @Override
      public @NotNull List<E> removeAfter(final int numElements) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (numElements < 0 || knownSize == 0) {
          return this;
        }
        if (numElements == 0 && knownSize == 1) {
          return List.of();
        }
        return new List<E>(new RemoveAfterListMaterializer<E>(materializer, numElements));
      }

      @Override
      public @NotNull List<E> removeEach(final E element) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveWhereListMaterializer<E>(materializer, equalsElement(element)));
      }

      @Override
      public @NotNull List<E> removeFirst(final E element) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveFirstWhereListMaterializer<E>(materializer, equalsElement(element)));
      }

      @Override
      public @NotNull List<E> removeFirstWhere(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new RemoveFirstWhereListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> removeFirstWhereNot(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveFirstWhereListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull List<E> removeLast(final E element) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveLastWhereListMaterializer<E>(materializer, equalsElement(element)));
      }

      @Override
      public @NotNull List<E> removeLastWhere(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveLastWhereListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> removeLastWhereNot(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveLastWhereListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull List<E> removeSegment(final int start, final int maxSize) {
        final ListMaterializer<E> materializer = this.materializer;
        if (maxSize <= 0 || materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new RemoveSegmentListMaterializer<E>(materializer, start, maxSize));
      }

      @Override
      public @NotNull List<E> removeSlice(final int start, final int end) {
        return null;
      }

      @Override
      public @NotNull List<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new RemoveWhereListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> removeWhereNot(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new RemoveWhereListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull ListSequence<E> replaceAfter(int numElements, E replacement) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> replaceEach(E element, E replacement) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> replaceFirst(E element, E replacement) {
        return null;
      }

      // TODO: replaceFirstWhere, replaceFirstWhereNot (map)

      @Override
      public @NotNull ListSequence<E> replaceLast(E element, E replacement) {
        return null;
      }

      // TODO: replaceLastWhere, replaceLastWhereNot (map)

      @Override
      public @NotNull ListSequence<E> replaceSegment(int start,
          @NotNull Iterable<? extends E> patch, int maxSize) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> replaceSlice(int start, @NotNull Iterable<? extends E> patch,
          int end) {
        return null;
      }

      // TODO: replaceWhere

      @Override
      public @NotNull ListSequence<E> reverse() {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> slice(int from, int until) {
        return null;
      }

      @Override
      public @NotNull ListSequence<Boolean> startsWith(@NotNull Iterable<?> elements) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> sorted(@NotNull Comparator<? super E> comparator) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> take(int maxElements) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> takeUntil(@NotNull Predicate<? super E> predicate) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> takeWhile(@NotNull Predicate<? super E> predicate) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> takeRight(int maxElements) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> takeRightUntil(@NotNull Predicate<? super E> predicate) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> takeRightWhile(@NotNull Predicate<? super E> predicate) {
        return null;
      }

      private static class SuppliedMeterializer<E> implements ListMaterializer<E> {

        private volatile ListMaterializer<E> state;

        private SuppliedMeterializer(
            @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
          state = new ImmaterialState(Require.notNull(supplier, "supplier"));
        }

        @Override
        public boolean canMaterializeElement(final int index) {
          return state.canMaterializeElement(index);
        }

        @Override
        public int knownSize() {
          return state.knownSize();
        }

        @Override
        public E materializeElement(final int index) {
          return state.materializeElement(index);
        }

        @Override
        public boolean materializeEmpty() {
          return state.materializeEmpty();
        }

        @Override
        public @NotNull Iterator<E> materializeIterator() {
          return state.materializeIterator();
        }

        @Override
        public int materializeSize() {
          return state.materializeSize();
        }

        private class ImmaterialState implements ListMaterializer<E> {

          private final Supplier<? extends Iterable<? extends E>> supplier;

          private ImmaterialState(
              @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
            this.supplier = supplier;
          }

          @Override
          public boolean canMaterializeElement(final int index) {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.canMaterializeElement(index);
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public int knownSize() {
            return -1;
          }

          @Override
          public E materializeElement(final int index) {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.materializeElement(index);
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public boolean materializeEmpty() {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.materializeEmpty();
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public @NotNull Iterator<E> materializeIterator() {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.materializeIterator();
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public int materializeSize() {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.materializeSize();
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }
        }
      }
    }
  }
}
