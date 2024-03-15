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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.util.AbstractListSequence;
import sparx.util.CollectionMaterializer;
import sparx.util.ListSequence;
import sparx.util.Require;
import sparx.util.Sequence;
import sparx.util.UncheckedException;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.Predicate;

public class Sparx {

  private static final EmptyCollectionMaterializer<Object> EMPTY_MATERIALIZER = new EmptyCollectionMaterializer<Object>();
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

      private final CollectionMaterializer<E> materializer;

      private List(@NotNull final CollectionMaterializer<E> materializer) {
        super(materializer);
        this.materializer = materializer;
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of() {
        return (List<E>) EMPTY_LIST;
      }

      public static @NotNull <E> List<E> of(final E first) {
        return new List<E>(new ElementToCollectionMaterializer<E>(first));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second) {
        return new List<E>(new ArrayToCollectionMaterializer<E>(first, second));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third) {
        return new List<E>(new ArrayToCollectionMaterializer<E>(first, second, third));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third,
          final E fourth) {
        return new List<E>(new ArrayToCollectionMaterializer<E>(first, second, third, fourth));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth) {
        return new List<E>(
            new ArrayToCollectionMaterializer<E>(first, second, third, fourth, fifth));
      }

      @SuppressWarnings("unchecked")
      private static @NotNull <E> CollectionMaterializer<E> getElementsMaterializer(
          @NotNull final Iterable<E> elements) {
        if (elements instanceof java.util.List) {
          final java.util.List<E> list = (java.util.List<E>) elements;
          if (list.isEmpty()) {
            return (CollectionMaterializer<E>) EMPTY_MATERIALIZER;
          }
          return new ListToCollectionMaterializer<E>(list);
        } else if (elements instanceof Collection) {
          final Collection<E> collection = (Collection<E>) elements;
          if (collection.isEmpty()) {
            return (CollectionMaterializer<E>) EMPTY_MATERIALIZER;
          }
          return new CollectionToCollectionMaterializer<E>(collection);
        } else {
          return new IteratorToCollectionMaterializer<E>(elements.iterator());
        }
      }

      @Override
      public @NotNull List<Boolean> all(@NotNull final Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<Boolean>(new ElementToCollectionMaterializer<Boolean>(false));
        }
        return new List<Boolean>(new AllCollectionMaterializer<E>(materializer, predicate));
      }

      @Override
      public <T> T apply(@NotNull Function<? super Sequence<E>, T> mapper) {
        return null;
      }

      @Override
      public @NotNull List<E> append(final E element) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(new ElementToCollectionMaterializer<E>(element));
        }
        return new List<E>(new AppendCollectionMaterializer<E>(materializer, element));
      }

      @Override
      public @NotNull List<E> appendAll(@NotNull final Iterable<E> elements) {
        final CollectionMaterializer<E> elementsMaterializer = getElementsMaterializer(elements);
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(elementsMaterializer);
        }
        return new List<E>(
            new AppendAllCollectionMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull <F> List<F> as() {
        return (List<F>) this;
      }

      @Override
      public @NotNull List<Integer> count() {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<Integer>(new ElementToCollectionMaterializer<Integer>(0));
        }
        return new List<Integer>(new CountCollectionMaterializer<E>(materializer));
      }

      @Override
      public @NotNull List<Integer> count(@NotNull final Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<Integer>(new ElementToCollectionMaterializer<Integer>(0));
        }
        return new List<Integer>(new CountByCollectionMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> countNot(@NotNull final Predicate<? super E> predicate) {
        return count(negated(predicate));
      }

      @Override
      public void doEach(@NotNull final Consumer<? super E> consumer) {
        final CollectionMaterializer<E> materializer = this.materializer;
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
        final CollectionMaterializer<E> materializer = this.materializer;
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
        final CollectionMaterializer<E> materializer = this.materializer;
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
        final CollectionMaterializer<E> materializer = this.materializer;
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
        final CollectionMaterializer<E> materializer = this.materializer;
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
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropCollectionMaterializer<E>(materializer, maxElements));
      }

      @Override
      public @NotNull List<E> dropUntil(@NotNull final Predicate<? super E> predicate) {
        return dropWhile(negated(predicate));
      }

      @Override
      public @NotNull List<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropWhileCollectionMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> dropRight(final int maxElements) {
        if (Math.max(0, maxElements) == 0) {
          return this;
        }
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropRightCollectionMaterializer<E>(materializer, maxElements));
      }

      @Override
      public @NotNull List<E> dropRightUntil(@NotNull Predicate<? super E> predicate) {
        return dropRightWhile(negated(predicate));
      }

      @Override
      public @NotNull List<E> dropRightWhile(@NotNull Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropRightWhileCollectionMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Boolean> endsWith(@NotNull final Iterable<?> elements) {
        final CollectionMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Boolean>(
            new EndsWithCollectionMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<Boolean>(new ElementToCollectionMaterializer<Boolean>(false));
        }
        return new List<Boolean>(new ExistsCollectionMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> filter(@NotNull final Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new FilterCollectionMaterializer<E>(materializer, predicate));
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
      @SuppressWarnings("unchecked")
      public @NotNull List<E> findFirst(@NotNull final Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return (List<E>) EMPTY_LIST;
        }
        return new List<E>(new FindFirstCollectionMaterializer<E>(materializer, predicate));
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
      @SuppressWarnings("unchecked")
      public @NotNull List<Integer> findIndexWhere(@NotNull final Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return (List<Integer>) EMPTY_LIST;
        }
        return new List<Integer>(new FindIndexCollectionMaterializer<E>(materializer, predicate));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull List<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return (List<Integer>) EMPTY_LIST;
        }
        final CollectionMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Integer>(
            new FindIndexOfSliceCollectionMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull List<E> findLast(@NotNull final Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return (List<E>) EMPTY_LIST;
        }
        return new List<E>(new FindLastCollectionMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> findLastIndexOf(final Object element) {
        return findLastIndexWhere(equalsElement(element));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull List<Integer> findLastIndexWhere(
          @NotNull final Predicate<? super E> predicate) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return (List<Integer>) EMPTY_LIST;
        }
        return new List<Integer>(
            new FindLastIndexCollectionMaterializer<E>(materializer, predicate));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull List<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
        final CollectionMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return (List<Integer>) EMPTY_LIST;
        }
        final CollectionMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Integer>(
            new FindLastIndexOfSliceCollectionMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> findLastNot(@NotNull final Predicate<? super E> predicate) {
        return findLast(negated(predicate));
      }

      @Override
      public @NotNull <F> List<F> flatMap(
          @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
        return new List<F>(new FlatMapCollectionMaterializer<E, F>(materializer, mapper));
      }

      @Override
      public @NotNull <F> ListSequence<F> foldLeft(F identity,
          @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
        return null;
      }

      @Override
      public @NotNull <F> ListSequence<F> foldRight(F identity,
          @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
        return null;
      }

      @Override
      public @NotNull ListSequence<? extends ListSequence<E>> group(int maxSize) {
        return null;
      }

      @Override
      public @NotNull ListSequence<? extends ListSequence<E>> group(int size, E filler) {
        return null;
      }

      @Override
      public @NotNull ListSequence<Boolean> includes(Object element) {
        return null;
      }

      @Override
      public @NotNull ListSequence<Boolean> includesAll(@NotNull Iterable<?> elements) {
        return null;
      }

      @Override
      public @NotNull ListSequence<Boolean> includesSlice(@NotNull Iterable<?> elements) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> insertAllAt(int index, @NotNull Iterable<? extends E> patch) {
        return null;
      }

      @Override
      public @NotNull ListSequence<E> insertAt(int index, E element) {
        return null;
      }

      @Override
      public @NotNull <F> ListSequence<F> map(@NotNull Function<? super E, F> mapper) {
        return null;
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
      public @NotNull ListSequence<Boolean> notAll(@NotNull Predicate<? super E> predicate) {
        return null;
      }

      @Override
      public @NotNull ListSequence<Boolean> nonExists(@NotNull Predicate<? super E> predicate) {
        return null;
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
