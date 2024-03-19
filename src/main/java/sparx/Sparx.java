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

public class Sparx {

  private static final EmptyListMaterializer<Object> EMPTY_MATERIALIZER = new EmptyListMaterializer<Object>();
  private static final Predicate<?> EQUALS_NULL = new Predicate<Object>() {
    @Override
    public boolean test(final Object param) {
      return param == null;
    }
  };

  private Sparx() {
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
        if (elements instanceof java.util.List) {
          final java.util.List<E> list = (java.util.List<E>) elements;
          if (list.isEmpty()) {
            return (ListMaterializer<E>) EMPTY_MATERIALIZER;
          }
          return new ListToListMaterializer<E>(list);
        } else if (elements instanceof Collection) {
          final Collection<E> collection = (Collection<E>) elements;
          if (collection.isEmpty()) {
            return (ListMaterializer<E>) EMPTY_MATERIALIZER;
          }
          return new CollectionToListMaterializer<E>(collection);
        } else {
          return new IteratorToListMaterializer<E>((Iterator<E>) elements.iterator());
        }
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
      public <T> T apply(@NotNull Function<? super Sequence<E>, T> mapper) {
        return null;
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
      public @NotNull List<E> appendAll(@NotNull final Iterable<E> elements) {
        final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(elements);
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(elementsMaterializer);
        }
        return new List<E>(
            new AppendAllListMaterializer<E>(materializer, elementsMaterializer));
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
        return new List<Integer>(new CountByListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> countNot(@NotNull final Predicate<? super E> predicate) {
        return count(negated(predicate));
      }

      @Override
      public void doFor(@NotNull final Consumer<? super E> consumer) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return;
        }
        try {
          final Iterator<E> iterator = materializer.materializeIterator();
          while (iterator.hasNext()) {
            consumer.accept(iterator.next());
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }

      @Override
      public void doUntil(@NotNull final Predicate<? super E> predicate,
          @NotNull final Consumer<? super E> consumer) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return;
        }
        try {
          final Iterator<E> iterator = materializer.materializeIterator();
          while (iterator.hasNext()) {
            final E next = iterator.next();
            if (predicate.test(next)) {
              break;
            }
            consumer.accept(next);
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
          final Iterator<E> iterator = materializer.materializeIterator();
          while (iterator.hasNext()) {
            if (predicate.test(iterator.next())) {
              break;
            }
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }

      @Override
      public void doWhile(@NotNull final Predicate<? super E> predicate,
          @NotNull final Consumer<? super E> consumer) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return;
        }
        try {
          final Iterator<E> iterator = materializer.materializeIterator();
          while (iterator.hasNext()) {
            final E next = iterator.next();
            if (!predicate.test(next)) {
              break;
            }
            consumer.accept(next);
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
          final Iterator<E> iterator = materializer.materializeIterator();
          while (iterator.hasNext()) {
            if (!predicate.test(iterator.next())) {
              break;
            }
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }

      @Override
      public @NotNull List<E> drop(final int maxElements) {
        if (Math.max(0, maxElements) == 0) {
          return this;
        }
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropListMaterializer<E>(materializer, maxElements));
      }

      @Override
      public @NotNull List<E> dropUntil(@NotNull final Predicate<? super E> predicate) {
        return dropWhile(negated(predicate));
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
        if (Math.max(0, maxElements) == 0) {
          return this;
        }
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropRightListMaterializer<E>(materializer, maxElements));
      }

      @Override
      public @NotNull List<E> dropRightUntil(@NotNull Predicate<? super E> predicate) {
        return dropRightWhile(negated(predicate));
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
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new FilterListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> filterNot(@NotNull final Predicate<? super E> predicate) {
        return filter(negated(predicate));
      }

      @Override
      public @NotNull List<E> findAny(@NotNull final Predicate<? super E> predicate) {
        return findFirst(predicate);
      }

      @Override
      public @NotNull List<E> findAnyNot(@NotNull final Predicate<? super E> predicate) {
        return findAny(negated(predicate));
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
        return findFirst(negated(predicate));
      }

      @Override
      public @NotNull List<Integer> findIndexOf(final Object element) {
        return findIndexWhere(equalsElement(element));
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
        return findIndexWhere(negated(predicate));
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
        return new List<E>(new FindLastListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> findLastIndexOf(final Object element) {
        return findLastIndexWhere(equalsElement(element));
      }

      @Override
      public @NotNull List<Integer> findLastIndexWhere(
          @NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(
            new FindLastIndexListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> findLastIndexWhereNot(
          @NotNull final Predicate<? super E> predicate) {
        return findLastIndexWhere(negated(predicate));
      }

      @Override
      public @NotNull List<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Integer>(
            new FindLastIndexOfSliceListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> findLastNot(@NotNull final Predicate<? super E> predicate) {
        return findLast(negated(predicate));
      }

      @Override
      public @NotNull <F> List<F> flatMap(
          @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<F>(new FlatMapListMaterializer<E, F>(materializer, mapper));
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
          @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
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
        return new List<List<E>>(
            new GroupListMaterializer<E, List<E>>(materializer, size, filler,
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
      public @NotNull List<E> insertAllAt(final int index,
          @NotNull final Iterable<? extends E> elements) {
        final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(elements);
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(elementsMaterializer);
        }
        return new List<E>(
            new InsertAllAtListMaterializer<E>(materializer, index, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> insertAt(final int index, final E element) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(new ElementToListMaterializer<E>(element));
        }
        return new List<E>(new InsertAtListMaterializer<E>(materializer, index, element));
      }

      @Override
      public @NotNull Iterator<E> iterator() {
        return materializer.materializeIterator();
      }

      @Override
      public @NotNull <F> List<F> map(@NotNull final Function<? super E, F> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<F>(new MapListMaterializer<E, F>(materializer, mapper));
      }

      @Override
      public @NotNull ListSequence<E> mapExceptionally(
          @NotNull Function<? super Throwable, ? extends E> mapper) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> max(@NotNull Comparator<? super E> comparator) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> min(@NotNull Comparator<? super E> comparator) {
        return null;
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
      public @NotNull ListSequence<E> peek(@NotNull Consumer<? super E> consumer) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> plus(final E element) {
        return append(element);
      }

      @Override
      public @NotNull ListSequence<E> plusAll(@NotNull final Iterable<E> elements) {
        return appendAll(elements);
      }

      @Override
      public @NotNull ListSequence<E> prepend(E element) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> prependAll(@NotNull Iterable<E> elements) {
        return null;
      }

      @Override
      public @NotNull <F extends E> ListSequence<F> reduceLeft(
          @NotNull BinaryFunction<? super E, ? super E, F> operation) {
        return null;
      }

      @Override
      public @NotNull <F extends E> ListSequence<F> reduceRight(
          @NotNull BinaryFunction<? super E, ? super E, F> operation) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> removeAt(int index) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> removeEach(E element) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> removeFirst(E element) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> removeLast(E element) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> removeSegment(int from, int maxSize) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> removeSlice(int from, int until) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> replaceAt(int index, E element) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> replaceEach(E element, E replacement) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> replaceFirst(E current, E replacement) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> replaceLast(E current, E replacement) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> replaceSegment(int from, @NotNull Iterable<? extends E> patch,
          int maxSize) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> replaceSlice(int from, @NotNull Iterable<? extends E> patch,
          int until) {
        return null;
      }

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
      public @NotNull <F extends Number> ListSequence<F> sum(
          @NotNull Function<? super E, F> mapper) {
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
    }
  }
}
