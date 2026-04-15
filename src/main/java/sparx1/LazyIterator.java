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
package sparx1;

import static sparx1.lazy.getKnownSize;
import static sparx1.util.function.Functions.equalsElement;
import static sparx1.util.function.Functions.indexedIdentity;
import static sparx1.util.function.Functions.negated;
import static sparx1.util.function.Functions.reversed;
import static sparx1.util.function.Functions.toIndexedConsumer;
import static sparx1.util.function.Functions.toIndexedFunction;
import static sparx1.util.function.Functions.toIndexedPredicate;
import static sparx1.util.function.Functions.toNegatedIndexedPredicate;

import java.util.Collection;
import java.util.Comparator;
import java.util.RandomAccess;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.internal.lazy.iterator.AfterIteratorMaterializer;
import sparx1.internal.lazy.iterator.AppendAllIteratorMaterializer;
import sparx1.internal.lazy.iterator.AppendIteratorMaterializer;
import sparx1.internal.lazy.iterator.CollectionToIteratorMaterializer;
import sparx1.internal.lazy.iterator.CountIteratorMaterializer;
import sparx1.internal.lazy.iterator.DequeToIteratorMaterializer;
import sparx1.internal.lazy.iterator.DiffIteratorMaterializer;
import sparx1.internal.lazy.iterator.DistinctByIteratorMaterializer;
import sparx1.internal.lazy.iterator.DropFirstIteratorMaterializer;
import sparx1.internal.lazy.iterator.DropFirstWhileIteratorMaterializer;
import sparx1.internal.lazy.iterator.DropLastIteratorMaterializer;
import sparx1.internal.lazy.iterator.ElementToIteratorMaterializer;
import sparx1.internal.lazy.iterator.EmptyIteratorMaterializer;
import sparx1.internal.lazy.iterator.EndsWithIteratorMaterializer;
import sparx1.internal.lazy.iterator.ExistsIteratorMaterializer;
import sparx1.internal.lazy.iterator.FilterIteratorMaterializer;
import sparx1.internal.lazy.iterator.FilterWhileIteratorMaterializer;
import sparx1.internal.lazy.iterator.FinallyIteratorMaterializer;
import sparx1.internal.lazy.iterator.FindFirstIndexIteratorMaterializer;
import sparx1.internal.lazy.iterator.FindFirstIndexOfSequenceIteratorMaterializer;
import sparx1.internal.lazy.iterator.FindFirstIteratorMaterializer;
import sparx1.internal.lazy.iterator.FindLastIndexIteratorMaterializer;
import sparx1.internal.lazy.iterator.FindLastIndexOfSequenceIteratorMaterializer;
import sparx1.internal.lazy.iterator.FindLastIteratorMaterializer;
import sparx1.internal.lazy.iterator.FlatMapIteratorMaterializer;
import sparx1.internal.lazy.iterator.FlatMapWhileIteratorMaterializer;
import sparx1.internal.lazy.iterator.FoldIteratorMaterializer;
import sparx1.internal.lazy.iterator.FoldWhileIteratorMaterializer;
import sparx1.internal.lazy.iterator.IncludesAllIteratorMaterializer;
import sparx1.internal.lazy.iterator.IncludesSequenceIteratorMaterializer;
import sparx1.internal.lazy.iterator.InsertAfterIteratorMaterializer;
import sparx1.internal.lazy.iterator.InsertAllAfterIteratorMaterializer;
import sparx1.internal.lazy.iterator.InsertAllIteratorMaterializer;
import sparx1.internal.lazy.iterator.InsertIteratorMaterializer;
import sparx1.internal.lazy.iterator.InterleaveInnerIteratorMaterializer;
import sparx1.internal.lazy.iterator.InterleaveInnerWithPaddingIteratorMaterializer;
import sparx1.internal.lazy.iterator.InterleaveIteratorMaterializer;
import sparx1.internal.lazy.iterator.InterleaveWithPaddingIteratorMaterializer;
import sparx1.internal.lazy.iterator.IntersectIteratorMaterializer;
import sparx1.internal.lazy.iterator.IteratorToIteratorMaterializer;
import sparx1.internal.lazy.iterator.ListToIteratorMaterializer;
import sparx1.internal.lazy.iterator.MapIteratorMaterializer;
import sparx1.internal.lazy.iterator.MapWhileIteratorMaterializer;
import sparx1.internal.lazy.iterator.MaxIteratorMaterializer;
import sparx1.internal.lazy.iterator.OrElseIteratorMaterializer;
import sparx1.internal.lazy.iterator.PeekExceptionallyIteratorMaterializer;
import sparx1.internal.lazy.iterator.PeekIteratorMaterializer;
import sparx1.internal.lazy.iterator.ReduceIteratorMaterializer;
import sparx1.internal.lazy.iterator.ReduceWhileIteratorMaterializer;
import sparx1.internal.lazy.iterator.RemoveFirstIteratorMaterializer;
import sparx1.internal.lazy.iterator.RemoveFirstSequenceIteratorMaterializer;
import sparx1.internal.lazy.iterator.RemoveLastIteratorMaterializer;
import sparx1.internal.lazy.iterator.RemoveLastSequenceIteratorMaterializer;
import sparx1.internal.lazy.iterator.RemoveSequenceIteratorMaterializer;
import sparx1.internal.lazy.iterator.RemoveSliceIteratorMaterializer;
import sparx1.internal.lazy.iterator.SuppliedIteratorMaterializer;
import sparx1.lazy.Iterator;
import sparx1.util.DequeArrayList;
import sparx1.util.Require;
import sparx1.util.SizeOverflowException;
import sparx1.util.UncheckedException;
import sparx1.util.ZipEntry;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Nullable;
import sparx1.util.annotation.Positive;
import sparx1.util.function.Action;
import sparx1.util.function.BinaryFunction;
import sparx1.util.function.Consumer;
import sparx1.util.function.Function;
import sparx1.util.function.Functions;
import sparx1.util.function.IndexedConsumer;
import sparx1.util.function.IndexedFunction;
import sparx1.util.function.IndexedPredicate;
import sparx1.util.function.Predicate;
import sparx1.util.function.Supplier;

public class LazyIterator<E> extends Iterator<E> {

  private static final LazyIterator<?> EMPTY_ITERATOR = new LazyIterator<Object>(
      EmptyIteratorMaterializer.instance());
  private static final Function<? extends DequeArrayList<?>, ? extends Iterator<?>> FROM_DEQUEUE_LIST = new Function<DequeArrayList<Object>, Iterator<Object>>() {
    @Override
    public Iterator<Object> apply(final DequeArrayList<Object> list) {
      return new LazyIterator<Object>(new DequeToIteratorMaterializer<Object>(list));
    }
  };

  private final IteratorMaterializer<E> materializer;

  LazyIterator(final @NotNull IteratorMaterializer<E> materializer) {
    this.materializer = materializer;
  }

  @SuppressWarnings("unchecked")
  static @NotNull <E> LazyIterator<E> emptyIterator() {
    return (LazyIterator<E>) EMPTY_ITERATOR;
  }

  static @NotNull <E> LazyIterator<E> elementIterator(final @Nullable E element) {
    return new LazyIterator<E>(new ElementToIteratorMaterializer<E>(element));
  }

  @SuppressWarnings("unchecked")
  private static @NotNull <E> IteratorMaterializer<E> getElementsMaterializer(
      final @NotNull java.lang.Iterable<? extends E> elements) {
    if (elements instanceof LazyIterator) {
      return ((LazyIterator<E>) elements).materializer;
    }
//      if (elements instanceof List) {
//        final ListMaterializer<E> materializer = ((List<E>) elements).materializer;
//        if (materializer.knownSize() == 0) {
//          return EmptyIteratorMaterializer.instance();
//        }
//        if (materializer.isRandomAccess()) {
//          return new ListMaterializerToIteratorMaterializer<E>(materializer);
//        }
//        return new IteratorToIteratorMaterializer<E>(materializer.materializeIterator());
//      }
    if (elements instanceof java.util.List) {
      final java.util.List<E> list = (java.util.List<E>) elements;
      if (list.isEmpty()) {
        return EmptyIteratorMaterializer.instance();
      }
      if (list instanceof RandomAccess) {
        return new ListToIteratorMaterializer<E>(list);
      }
      return new IteratorToIteratorMaterializer<E>(elements.iterator());
    }
    if (elements instanceof java.util.Collection) {
      final java.util.Collection<E> collection = (java.util.Collection<E>) elements;
      if (collection.isEmpty()) {
        return EmptyIteratorMaterializer.instance();
      }
      return new CollectionToIteratorMaterializer<E>(collection);
    }
    return new IteratorToIteratorMaterializer<E>(elements.iterator());
  }

  private static @NotNull <E, F> IndexedFunction<E, IteratorMaterializer<F>> getElementToMaterializer(
      final @NotNull Function<? super E, ? extends java.lang.Iterable<? extends F>> mapper) {
    return new IndexedFunction<E, IteratorMaterializer<F>>() {
      @Override
      public IteratorMaterializer<F> apply(final int index, final E element) throws Exception {
        return getElementsMaterializer(Require.notNull(mapper.apply(element), "elements"));
      }
    };
  }

  private static @NotNull <E, F> IndexedFunction<E, IteratorMaterializer<F>> getElementToMaterializer(
      final @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends F>> mapper) {
    return new IndexedFunction<E, IteratorMaterializer<F>>() {
      @Override
      public IteratorMaterializer<F> apply(final int index, final E element) throws Exception {
        return getElementsMaterializer(Require.notNull(mapper.apply(index, element), "elements"));
      }
    };
  }

  private static @NotNull <E, T extends Throwable> IndexedFunction<Throwable, IteratorMaterializer<E>> getExceptionToMaterializer(
      final @NotNull Class<T> exceptionType,
      final @NotNull Function<? super T, ? extends java.lang.Iterable<? extends E>> mapper) {
    return new IndexedFunction<Throwable, IteratorMaterializer<E>>() {
      @Override
      @SuppressWarnings("unchecked")
      public IteratorMaterializer<E> apply(final int index, final Throwable exception)
          throws Exception {
        if (exceptionType.isInstance(exception)) {
          return getElementsMaterializer(Require.notNull(mapper.apply((T) exception), "elements"));
        }
        if (exception instanceof Exception) {
          throw (Exception) exception;
        }
        throw UncheckedException.throwUnchecked(exception);
      }
    };
  }

  private static @NotNull <E, T extends Throwable> IndexedFunction<Throwable, IteratorMaterializer<E>> getExceptionToMaterializer(
      final @NotNull Class<T> exceptionType,
      final @NotNull IndexedFunction<? super T, ? extends java.lang.Iterable<? extends E>> mapper) {
    return new IndexedFunction<Throwable, IteratorMaterializer<E>>() {
      @Override
      @SuppressWarnings("unchecked")
      public IteratorMaterializer<E> apply(final int index, final Throwable exception)
          throws Exception {
        if (exceptionType.isInstance(exception)) {
          return getElementsMaterializer(
              Require.notNull(mapper.apply(index, (T) exception), "elements"));
        }
        if (exception instanceof Exception) {
          throw (Exception) exception;
        }
        if (exception instanceof Error) {
          throw (Error) exception;
        }
        throw UncheckedException.throwUnchecked(exception);
      }
    };
  }

  private static @NotNull <E> Supplier<IteratorMaterializer<E>> getIterableToIteratorMaterializer(
      final @NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier) {
    return new Supplier<IteratorMaterializer<E>>() {
      @Override
      public IteratorMaterializer<E> get() throws Exception {
        return getElementsMaterializer(Require.notNull(supplier.get(), "elements"));
      }
    };
  }

  @Override
  public @NotNull Iterator<E> append(final @Nullable E element) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return new LazyIterator<E>(new ElementToIteratorMaterializer<E>(element));
    }
    return new LazyIterator<E>(new AppendIteratorMaterializer<E>(materializer, element));
  }

  @Override
  public @NotNull Iterator<E> appendAll(final @NotNull java.lang.Iterable<? extends E> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    final IteratorMaterializer<E> elementsMaterializer = getElementsMaterializer(
        Require.notNull(elements, "elements"));
    if (materializer.currentKnownSize() == 0) {
      return new LazyIterator<E>(elementsMaterializer);
    }
    return new LazyIterator<E>(
        new AppendAllIteratorMaterializer<E>(materializer, elementsMaterializer));
  }

  @Override
  public @NotNull <F> Iterator<F> apply(
      final @NotNull Function<? super Iterator<E>, java.lang.Iterable<F>> function) {
    Require.notNull(function, "function");
    return new LazyIterator<F>(
        new SuppliedIteratorMaterializer<F>(new Supplier<IteratorMaterializer<F>>() {
          @Override
          public IteratorMaterializer<F> get() throws Exception {
            return getElementsMaterializer(
                Require.notNull(function.apply(LazyIterator.this), "elements"));
          }
        }));
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <F> Iterator<F> cast() {
    return (Iterator<F>) this;
  }

  @Override
  public @NotNull Iterator<Integer> count() {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return new LazyIterator<Integer>(new ElementToIteratorMaterializer<Integer>(0));
    }
    return new LazyIterator<Integer>(new CountIteratorMaterializer<E>(materializer));
  }

  @Override
  public @NotNull Iterator<E> diff(final @NotNull java.lang.Iterable<?> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    if (getKnownSize(elements) == 0) {
      return iterator();
    }
    return new LazyIterator<E>(new DiffIteratorMaterializer<E>(materializer,
        getElementsMaterializer(Require.notNull(elements, "elements"))));
  }

  @Override
  public @NotNull Iterator<E> distinct() {
    return distinctBy(indexedIdentity());
  }

  @Override
  public @NotNull <K> Iterator<E> distinctBy(final @NotNull Function<? super E, K> keyExtractor) {
    final IteratorMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.currentKnownSize();
    if (knownSize == 0) {
      return emptyIterator();
    }
    if (knownSize == 1) {
      return iterator();
    }
    return new LazyIterator<E>(new DistinctByIteratorMaterializer<E, K>(materializer,
        toIndexedFunction(keyExtractor, "keyExtractor")));
  }

  @Override
  public @NotNull <K> Iterator<E> distinctBy(
      final @NotNull IndexedFunction<? super E, K> keyExtractor) {
    final IteratorMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.currentKnownSize();
    if (knownSize == 0) {
      return emptyIterator();
    }
    if (knownSize == 1) {
      return iterator();
    }
    return new LazyIterator<E>(new DistinctByIteratorMaterializer<E, K>(materializer,
        Require.notNull(keyExtractor, "keyExtractor")));
  }

  @Override
  public void doFor(final @NotNull Consumer<? super E> elementConsumer) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      while (materializer.materializeHasNext()) {
        elementConsumer.accept(materializer.materializeNext());
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doFor(final @NotNull Consumer<? super E> elementConsumer,
      final @NotNull Action endAction) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      while (materializer.materializeHasNext()) {
        elementConsumer.accept(materializer.materializeNext());
      }
      endAction.run();
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doFor(final @NotNull Consumer<? super E> elementConsumer,
      final @NotNull Action endAction, final @NotNull Consumer<? super Throwable> errorConsumer) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      while (materializer.materializeHasNext()) {
        elementConsumer.accept(materializer.materializeNext());
      }
      endAction.run();
    } catch (final Exception e) {
      try {
        errorConsumer.accept(e);
      } catch (final Exception ex) {
        throw UncheckedException.throwUnchecked(ex);
      }
    }
  }

  @Override
  public void doFor(final @NotNull IndexedConsumer<? super E> elementConsumer) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      int i = 0;
      while (materializer.materializeHasNext()) {
        elementConsumer.accept(i++, materializer.materializeNext());
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doFor(final @NotNull IndexedConsumer<? super E> elementConsumer,
      final @NotNull Consumer<? super Integer> endConsumer) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      int i = 0;
      while (materializer.materializeHasNext()) {
        elementConsumer.accept(i++, materializer.materializeNext());
      }
      endConsumer.accept(i);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doFor(final @NotNull IndexedConsumer<? super E> elementConsumer,
      final @NotNull Consumer<? super Integer> endConsumer,
      final @NotNull IndexedConsumer<? super Throwable> errorConsumer) {
    int i = 0;
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      while (materializer.materializeHasNext()) {
        elementConsumer.accept(i++, materializer.materializeNext());
      }
      endConsumer.accept(i);
    } catch (final Exception e) {
      try {
        errorConsumer.accept(i, e);
      } catch (final Exception ex) {
        throw UncheckedException.throwUnchecked(ex);
      }
    }
  }

  @Override
  public void doWhile(final @NotNull IndexedPredicate<? super E> elementPredicate) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      int i = 0;
      while (materializer.materializeHasNext()) {
        if (!elementPredicate.test(i++, materializer.materializeNext())) {
          break;
        }
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doWhile(final @NotNull IndexedPredicate<? super E> elementPredicate,
      final @NotNull Consumer<? super Integer> endConsumer) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      int i = 0;
      while (materializer.materializeHasNext()) {
        if (!elementPredicate.test(i++, materializer.materializeNext())) {
          return;
        }
      }
      endConsumer.accept(i);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doWhile(final @NotNull IndexedPredicate<? super E> elementPredicate,
      final @NotNull Consumer<? super Integer> endConsumer,
      final @NotNull IndexedConsumer<? super Throwable> errorConsumer) {
    int i = 0;
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      while (materializer.materializeHasNext()) {
        if (!elementPredicate.test(i++, materializer.materializeNext())) {
          return;
        }
      }
      endConsumer.accept(i);
    } catch (final Exception e) {
      try {
        errorConsumer.accept(i, e);
      } catch (final Exception ex) {
        throw UncheckedException.throwUnchecked(ex);
      }
    }
  }

  @Override
  public void doWhile(final @NotNull Predicate<? super E> elementPredicate) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      while (materializer.materializeHasNext()) {
        if (!elementPredicate.test(materializer.materializeNext())) {
          break;
        }
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doWhile(final @NotNull Predicate<? super E> elementPredicate,
      final @NotNull Action endAction) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      while (materializer.materializeHasNext()) {
        if (!elementPredicate.test(materializer.materializeNext())) {
          return;
        }
      }
      endAction.run();
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doWhile(final @NotNull Predicate<? super E> elementPredicate,
      final @NotNull Action endAction, final @NotNull Consumer<? super Throwable> errorConsumer) {
    try {
      final IteratorMaterializer<E> materializer = this.materializer;
      while (materializer.materializeHasNext()) {
        if (!elementPredicate.test(materializer.materializeNext())) {
          return;
        }
      }
      endAction.run();
    } catch (final Exception e) {
      try {
        errorConsumer.accept(e);
      } catch (final Exception ex) {
        throw UncheckedException.throwUnchecked(ex);
      }
    }
  }

  @Override
  public @NotNull Iterator<E> doAfter(final @NotNull Action action) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      try {
        action.run();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      return emptyIterator();
    }
    return new LazyIterator<E>(
        new AfterIteratorMaterializer<E>(materializer, Require.notNull(action, "action")));
  }

  @Override
  public @NotNull Iterator<E> doFinally(final @NotNull Action action) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      try {
        action.run();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      return emptyIterator();
    }
    return new LazyIterator<E>(
        new FinallyIteratorMaterializer<E>(materializer, Require.notNull(action, "action")));
  }

  @Override
  public @NotNull Iterator<E> dropFirst(final int maxElements) {
    if (maxElements == Integer.MAX_VALUE) {
      return emptyIterator();
    }
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    if (maxElements <= 0) {
      return iterator();
    }
    return new LazyIterator<E>(new DropFirstIteratorMaterializer<E>(materializer, maxElements));
  }

  @Override
  public @NotNull Iterator<E> dropFirstWhile(final @NotNull IndexedPredicate<? super E> condition) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new DropFirstWhileIteratorMaterializer<E>(materializer,
        Require.notNull(condition, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> dropFirstWhile(final @NotNull Predicate<? super E> condition) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new DropFirstWhileIteratorMaterializer<E>(materializer,
        toIndexedPredicate(condition, "condition")));
  }

  @Override
  public @NotNull Iterator<E> dropLast(final int maxElements) {
    if (maxElements == Integer.MAX_VALUE) {
      return emptyIterator();
    }
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    if (maxElements <= 0) {
      return iterator();
    }
    return new LazyIterator<E>(new DropLastIteratorMaterializer<E>(materializer, maxElements));
  }

  @Override
  public @NotNull Iterator<Boolean> endsWith(final @NotNull java.lang.Iterable<?> elements) {
    if (getKnownSize(elements) == 0) {
      return elementIterator(true);
    }
    return new LazyIterator<Boolean>(new EndsWithIteratorMaterializer<E>(materializer,
        LazyList.getElementsMaterializer(Require.notNull(elements, "elements"))));
  }

  @Override
  public @NotNull Iterator<Boolean> exists(final boolean whenEmpty,
      final @NotNull IndexedPredicate<? super E> predicate) {
    return existsForward(whenEmpty, predicate);
  }

  @Override
  public @NotNull Iterator<Boolean> exists(final boolean whenEmpty,
      final @NotNull Predicate<? super E> predicate) {
    return existsForward(whenEmpty, predicate);
  }

  @Override
  public @NotNull Iterator<Boolean> existsForward(final boolean whenEmpty,
      final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return elementIterator(whenEmpty);
    }
    return new LazyIterator<Boolean>(
        new ExistsIteratorMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
            whenEmpty));
  }

  @Override
  public @NotNull Iterator<Boolean> existsForward(final boolean whenEmpty,
      final @NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return elementIterator(whenEmpty);
    }
    return new LazyIterator<Boolean>(
        new ExistsIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate, "predicate"),
            whenEmpty));
  }

  @Override
  public @NotNull Iterator<E> filter(final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(
        new FilterIteratorMaterializer<E>(materializer, Require.notNull(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> filter(final @NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new FilterIteratorMaterializer<E>(materializer,
        toIndexedPredicate(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> filterWhile(final @NotNull IndexedPredicate<? super E> condition,
      final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new FilterWhileIteratorMaterializer<E>(materializer,
        Require.notNull(condition, "condition"), Require.notNull(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> filterWhile(final @NotNull Predicate<? super E> condition,
      final @NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new FilterWhileIteratorMaterializer<E>(materializer,
        toIndexedPredicate(condition, "condition"), toIndexedPredicate(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> find(final @NotNull IndexedPredicate<? super E> predicate) {
    return findFirst(predicate);
  }

  @Override
  public @NotNull Iterator<E> find(final @NotNull Predicate<? super E> predicate) {
    return findFirst(predicate);
  }

  @Override
  public @NotNull Iterator<E> findFirst(final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new FindFirstIteratorMaterializer<E>(materializer,
        Require.notNull(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> findFirst(final @NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new FindFirstIteratorMaterializer<E>(materializer,
        toIndexedPredicate(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<Integer> findFirstIndex(
      final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<Integer>(new FindFirstIndexIteratorMaterializer<E>(materializer,
        Require.notNull(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<Integer> findFirstIndex(final @NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<Integer>(new FindFirstIndexIteratorMaterializer<E>(materializer,
        toIndexedPredicate(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<Integer> findFirstIndexOf(final @Nullable Object element) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<Integer>(
        new FindFirstIndexIteratorMaterializer<E>(materializer, equalsElement(element)));
  }

  @Override
  public @NotNull Iterator<Integer> findFirstIndexOfSequence(
      final @NotNull java.lang.Iterable<?> elements) {
    if (getKnownSize(elements) == 0) {
      return elementIterator(0);
    }
    return new LazyIterator<Integer>(
        new FindFirstIndexOfSequenceIteratorMaterializer<E>(materializer,
            LazyList.getElementsMaterializer(elements)));
  }

  @Override
  public @NotNull Iterator<Integer> findIndex(
      final @NotNull IndexedPredicate<? super E> predicate) {
    return findFirstIndex(predicate);
  }

  @Override
  public @NotNull Iterator<Integer> findIndex(final @NotNull Predicate<? super E> predicate) {
    return findFirstIndex(predicate);
  }

  @Override
  public @NotNull Iterator<Integer> findIndexOf(final @Nullable Object element) {
    return findFirstIndexOf(element);
  }

  @Override
  public @NotNull Iterator<E> findLast(final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(
        new FindLastIteratorMaterializer<E>(materializer, Require.notNull(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> findLast(final @NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new FindLastIteratorMaterializer<E>(materializer,
        toIndexedPredicate(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<Integer> findLastIndex(
      final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<Integer>(new FindLastIndexIteratorMaterializer<E>(materializer,
        Require.notNull(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<Integer> findLastIndex(@NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<Integer>(new FindLastIndexIteratorMaterializer<E>(materializer,
        toIndexedPredicate(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<Integer> findLastIndexOf(final @Nullable Object element) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<Integer>(
        new FindLastIndexIteratorMaterializer<E>(materializer, equalsElement(element)));
  }

  @Override
  public @NotNull Iterator<Integer> findLastIndexOfSequence(
      final @NotNull java.lang.Iterable<?> elements) {
    if (getKnownSize(elements) == 0) {
      return elementIterator(size());
    }
    return new LazyIterator<Integer>(
        new FindLastIndexOfSequenceIteratorMaterializer<E>(materializer,
            LazyList.getElementsMaterializer(elements)));
  }

  @Override
  public @NotNull <F> Iterator<F> flatMap(
      final @NotNull Function<? super E, ? extends java.lang.Iterable<F>> mapper) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<F>(new FlatMapIteratorMaterializer<E, F>(materializer,
        getElementToMaterializer(Require.notNull(mapper, "mapper"))));
  }

  @Override
  public @NotNull <F> Iterator<F> flatMap(
      final @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<F>> mapper) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<F>(new FlatMapIteratorMaterializer<E, F>(materializer,
        getElementToMaterializer(Require.notNull(mapper, "mapper"))));
  }

  @Override
  public @NotNull Iterator<E> flatMapWhile(final @NotNull IndexedPredicate<? super E> condition,
      final @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends E>> mapper) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new FlatMapWhileIteratorMaterializer<E>(materializer,
        Require.notNull(condition, "condition"),
        getElementToMaterializer(Require.notNull(mapper, "mapper"))));
  }

  @Override
  public @NotNull Iterator<E> flatMapWhile(final @NotNull Predicate<? super E> condition,
      final @NotNull Function<? super E, ? extends java.lang.Iterable<? extends E>> mapper) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new FlatMapWhileIteratorMaterializer<E>(materializer,
        toIndexedPredicate(condition, "condition"),
        getElementToMaterializer(Require.notNull(mapper, "mapper"))));
  }

  @Override
  public @NotNull <F> Iterator<F> fold(final F identity,
      final @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
    return foldForward(identity, operation);
  }

  @Override
  public @NotNull <F> Iterator<F> foldForward(final F identity,
      final @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return elementIterator(identity);
    }
    return new LazyIterator<F>(new FoldIteratorMaterializer<E, F>(materializer, identity,
        Require.notNull(operation, "operation")));
  }

  @Override
  public @NotNull <F> Iterator<F> foldWhile(final F identity,
      final @NotNull Predicate<? super F> condition,
      final @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
    return foldWhileForward(identity, condition, operation);
  }

  @Override
  public @NotNull <F> Iterator<F> foldWhileForward(final F identity,
      final @NotNull Predicate<? super F> condition,
      final @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return new LazyIterator<F>(
          new SuppliedIteratorMaterializer<F>(new Supplier<IteratorMaterializer<F>>() {
            @Override
            public IteratorMaterializer<F> get() throws Exception {
              if (condition.test(identity)) {
                return new ElementToIteratorMaterializer<F>(identity);
              }
              return EmptyIteratorMaterializer.instance();
            }
          }));
    }
    return new LazyIterator<F>(new FoldWhileIteratorMaterializer<E, F>(materializer, identity,
        Require.notNull(condition, "condition"), Require.notNull(operation, "operation")));
  }

  @Override
  public boolean hasNext() {
    return materializer.materializeHasNext();
  }

  @Override
  public @NotNull Iterator<Boolean> includes(final @Nullable Object element) {
    if (materializer.currentKnownSize() == 0) {
      return elementIterator(false);
    }
    return exists(false, Functions.<E>equalsElement(element));
  }

  @Override
  public @NotNull Iterator<Boolean> includesAll(final @NotNull java.lang.Iterable<?> elements) {
    if (getKnownSize(elements) == 0) {
      return elementIterator(true);
    }
    return new LazyIterator<Boolean>(new IncludesAllIteratorMaterializer<E>(materializer,
        Require.notNull(elements, "elements")));
  }

  @Override
  public @NotNull Iterator<Boolean> includesSequence(
      final @NotNull java.lang.Iterable<?> elements) {
    if (getKnownSize(elements) == 0) {
      return elementIterator(false);
    }
    return new LazyIterator<Boolean>(new IncludesSequenceIteratorMaterializer<E>(materializer,
        LazyList.getElementsMaterializer(elements)));
  }

  @Override
  public @NotNull Iterator<E> insert(final @Nullable E element) {
    return new LazyIterator<E>(new InsertIteratorMaterializer<E>(materializer, element));
  }

  @Override
  public @NotNull Iterator<E> insertAfter(final int numElements, final @Nullable E element) {
    if (numElements < 0 || numElements == Integer.MAX_VALUE) {
      return iterator();
    }
    if (numElements == 0) {
      return insert(element);
    }
    final IteratorMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.currentKnownSize();
    if (knownSize == 0) {
      return emptyIterator();
    }
    if (knownSize > 0) {
      if (knownSize < numElements) {
        return iterator();
      }
      if (knownSize == numElements) {
        return append(element);
      }
    }
    return new LazyIterator<E>(
        new InsertAfterIteratorMaterializer<E>(materializer, numElements, element));
  }

  @Override
  public @NotNull Iterator<E> insertAll(final @NotNull java.lang.Iterable<? extends E> elements) {
    if (getKnownSize(elements) == 0) {
      return iterator();
    }
    return new LazyIterator<E>(new InsertAllIteratorMaterializer<E>(materializer,
        getElementsMaterializer(Require.notNull(elements, "elements"))));
  }

  @Override
  public @NotNull Iterator<E> insertAllAfter(final int numElements,
      final @NotNull java.lang.Iterable<? extends E> elements) {
    if (numElements < 0 || numElements == Integer.MAX_VALUE) {
      return iterator();
    }
    if (numElements == 0) {
      return insertAll(elements);
    }
    final IteratorMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.currentKnownSize();
    if (knownSize == 0) {
      return emptyIterator();
    }
    if (knownSize > 0) {
      if (knownSize < numElements) {
        return iterator();
      }
      if (knownSize == numElements) {
        return appendAll(elements);
      }
    }
    return new LazyIterator<E>(new InsertAllAfterIteratorMaterializer<E>(materializer, numElements,
        getElementsMaterializer(Require.notNull(elements, "elements"))));
  }

  @Override
  public @NotNull Iterator<E> interleave(final @NotNull java.lang.Iterable<? extends E> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0 || getKnownSize(elements) == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new InterleaveIteratorMaterializer<E>(materializer,
        getElementsMaterializer(Require.notNull(elements, "elements"))));
  }

  @Override
  public @NotNull Iterator<E> interleaveInner(
      final @NotNull java.lang.Iterable<? extends E> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new InterleaveInnerIteratorMaterializer<E>(materializer,
        getElementsMaterializer(Require.notNull(elements, "elements"))));
  }

  @Override
  public @NotNull Iterator<E> interleaveInnerWithPadding(
      final @NotNull java.lang.Iterable<? extends E> elements, final E paddingLeft,
      final E paddingRight) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0 || getKnownSize(elements) == 0) {
      return elementIterator(paddingLeft);
    }
    return new LazyIterator<E>(new InterleaveInnerWithPaddingIteratorMaterializer<E>(materializer,
        getElementsMaterializer(Require.notNull(elements, "elements")), paddingLeft, paddingRight));
  }

  @Override
  public @NotNull Iterator<E> interleaveWithPadding(
      final @NotNull java.lang.Iterable<? extends E> elements, final E paddingLeft,
      final E paddingRight) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0 || getKnownSize(elements) == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new InterleaveWithPaddingIteratorMaterializer<E>(materializer,
        getElementsMaterializer(Require.notNull(elements, "elements")), paddingLeft, paddingRight));
  }

  @Override
  public @NotNull Iterator<E> intersect(final @NotNull java.lang.Iterable<?> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0 || getKnownSize(elements) == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new IntersectIteratorMaterializer<E>(materializer,
        getElementsMaterializer(Require.notNull(elements, "elements"))));
  }

  @Override
  public boolean isLazy() {
    return true;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public boolean isSorted() {
    return false;
  }

  @Override
  public @NotNull Iterator<E> iterator() {
    return this;
  }

  @Override
  public E last() {
    final IteratorMaterializer<E> materializer = this.materializer;
    E next = null;
    while (materializer.materializeHasNext()) {
      next = materializer.materializeNext();
    }
    return next;
  }

  @Override
  public @NotNull <F> Iterator<F> map(final @NotNull Function<? super E, F> mapper) {
    return mapForward(mapper);
  }

  @Override
  public @NotNull <F> Iterator<F> map(final @NotNull IndexedFunction<? super E, F> mapper) {
    return mapForward(mapper);
  }

  @Override
  public @NotNull <F> Iterator<F> mapForward(final @NotNull Function<? super E, F> mapper) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<F>(
        new MapIteratorMaterializer<E, F>(materializer, toIndexedFunction(mapper, "mapper")));
  }

  @Override
  public @NotNull <F> Iterator<F> mapForward(final @NotNull IndexedFunction<? super E, F> mapper) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<F>(
        new MapIteratorMaterializer<E, F>(materializer, Require.notNull(mapper, "mapper")));
  }

  @Override
  public @NotNull Iterator<E> mapWhile(final @NotNull IndexedPredicate<? super E> condition,
      final @NotNull IndexedFunction<? super E, ? extends E> mapper) {
    return mapWhileForward(condition, mapper);
  }

  @Override
  public @NotNull Iterator<E> mapWhile(final @NotNull Predicate<? super E> condition,
      final @NotNull Function<? super E, ? extends E> mapper) {
    return mapWhileForward(condition, mapper);
  }

  @Override
  public @NotNull Iterator<E> mapWhileForward(final @NotNull IndexedPredicate<? super E> condition,
      final @NotNull IndexedFunction<? super E, ? extends E> mapper) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(
        new MapWhileIteratorMaterializer<E>(materializer, Require.notNull(condition, "condition"),
            Require.notNull(mapper, "mapper")));
  }

  @Override
  public @NotNull Iterator<E> mapWhileForward(final @NotNull Predicate<? super E> condition,
      final @NotNull Function<? super E, ? extends E> mapper) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new MapWhileIteratorMaterializer<E>(materializer,
        toIndexedPredicate(condition, "condition"), toIndexedFunction(mapper, "mapper")));
  }

  @Override
  public @NotNull Iterator<E> materialize() {
    materializer.materializeSkip(Integer.MAX_VALUE);
    return this;
  }

  @Override
  public @NotNull Iterator<E> max(final @NotNull Comparator<? super E> comparator) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(
        new MaxIteratorMaterializer<E>(materializer, Require.notNull(comparator, "comparator")));
  }

  @Override
  public @NotNull Iterator<E> min(final @NotNull Comparator<? super E> comparator) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(
        new MaxIteratorMaterializer<E>(materializer, reversed(comparator, "comparator")));
  }

  @Override
  public @NotNull Iterator<E> minus(final @Nullable E element) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(
        new FilterIteratorMaterializer<E>(materializer, Functions.<E>notEqualsElement(element)));
  }

  @Override
  public @NotNull Iterator<E> minusAll(final @NotNull java.lang.Iterable<? extends E> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    if (getKnownSize(elements) == 0) {
      return iterator();
    }
    final IndexedPredicate<E> predicate;
    if (elements instanceof Collection) {
      final Collection<?> collection = (Collection<?>) elements;
      predicate = new IndexedPredicate<E>() {
        @Override
        public boolean test(final int index, final E element) {
          return !collection.contains(element);
        }
      };
    } else {
      final ListMaterializer<E> elementsMaterializer = LazyList.getElementsMaterializer(elements);
      predicate = new IndexedPredicate<E>() {
        @Override
        public boolean test(final int index, final E element) {
          return !elementsMaterializer.materializeContains(element);
        }
      };
    }
    return new LazyIterator<E>(new FilterIteratorMaterializer<E>(materializer, predicate));
  }

  @Override
  public @NotNull Iterator<E> minusFirst(final @Nullable E element) {
    return removeFirst(Functions.<E>equalsElement(element));
  }

  @Override
  public @NotNull Iterator<E> minusLast(final @Nullable E element) {
    return removeLast(Functions.<E>equalsElement(element));
  }

  @Override
  public E next() {
    return materializer.materializeNext();
  }

  @Override
  public @NotNull Iterator<Boolean> notExists(final boolean whenEmpty,
      final @NotNull IndexedPredicate<? super E> predicate) {
    return notExistsForward(whenEmpty, predicate);
  }

  @Override
  public @NotNull Iterator<Boolean> notExists(final boolean whenEmpty,
      final @NotNull Predicate<? super E> predicate) {
    return notExistsForward(whenEmpty, predicate);
  }

  @Override
  public @NotNull Iterator<Boolean> notExistsForward(final boolean whenEmpty,
      final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return elementIterator(whenEmpty);
    }
    return new LazyIterator<Boolean>(
        new ExistsIteratorMaterializer<E>(materializer, negated(predicate, "predicate"),
            whenEmpty));
  }

  @Override
  public @NotNull Iterator<Boolean> notExistsForward(final boolean whenEmpty,
      final @NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return elementIterator(whenEmpty);
    }
    return new LazyIterator<Boolean>(new ExistsIteratorMaterializer<E>(materializer,
        toNegatedIndexedPredicate(predicate, "predicate"), whenEmpty));
  }

  @Override
  public @NotNull Iterator<E> orElse(final @NotNull java.lang.Iterable<? extends E> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.currentKnownSize();
    final IteratorMaterializer<E> elementsMaterializer = getElementsMaterializer(
        Require.notNull(elements, "elements"));
    if (knownSize == 0) {
      return new LazyIterator<E>(elementsMaterializer);
    }
    return new LazyIterator<E>(
        new OrElseIteratorMaterializer<E>(materializer, elementsMaterializer));
  }

  @Override
  public @NotNull Iterator<E> orElseGet(
      final @NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier) {
    final IteratorMaterializer<E> materializer = this.materializer;
    final SuppliedIteratorMaterializer<E> elementsMaterializer = new SuppliedIteratorMaterializer<E>(
        getIterableToIteratorMaterializer(Require.notNull(supplier, "supplier")));
    if (materializer.currentKnownSize() == 0) {
      return new LazyIterator<E>(elementsMaterializer);
    }
    return new LazyIterator<E>(
        new OrElseIteratorMaterializer<E>(materializer, elementsMaterializer));
  }

  @Override
  public @NotNull Iterator<E> peek(final @NotNull Consumer<? super E> consumer) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return iterator();
    }
    return new LazyIterator<E>(
        new PeekIteratorMaterializer<E>(materializer, toIndexedConsumer(consumer, "consumer")));
  }

  @Override
  public @NotNull Iterator<E> peek(final @NotNull IndexedConsumer<? super E> consumer) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return iterator();
    }
    return new LazyIterator<E>(
        new PeekIteratorMaterializer<E>(materializer, Require.notNull(consumer, "consumer")));
  }

  @Override
  public @NotNull Iterator<E> peekExceptionally(
      final @NotNull Consumer<? super Throwable> consumer) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return iterator();
    }
    return new LazyIterator<E>(new PeekExceptionallyIteratorMaterializer<E>(materializer,
        toIndexedConsumer(consumer, "consumer")));
  }

  @Override
  public @NotNull Iterator<E> peekExceptionally(
      final @NotNull IndexedConsumer<? super Throwable> consumer) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return iterator();
    }
    return new LazyIterator<E>(new PeekExceptionallyIteratorMaterializer<E>(materializer,
        Require.notNull(consumer, "consumer")));
  }

  @Override
  public @NotNull Iterator<E> plus(final @Nullable E element) {
    return append(element);
  }

  @Override
  public @NotNull Iterator<E> plusAll(final @NotNull java.lang.Iterable<? extends E> elements) {
    return appendAll(elements);
  }

  @Override
  public @NotNull Iterator<E> reduce(
      final @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
    return reduceForward(operation);
  }

  @Override
  public @NotNull Iterator<E> reduceForward(
      final @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
    final IteratorMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.currentKnownSize();
    if (knownSize == 0) {
      return emptyIterator();
    }
    if (knownSize == 1) {
      return iterator();
    }
    return new LazyIterator<E>(
        new ReduceIteratorMaterializer<E>(materializer, Require.notNull(operation, "operation")));
  }

  @Override
  public @NotNull Iterator<E> reduceWhile(final @NotNull Predicate<? super E> condition,
      final @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
    return reduceWhileForward(condition, operation);
  }

  @Override
  public @NotNull Iterator<E> reduceWhileForward(@NotNull Predicate<? super E> condition,
      @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
    final IteratorMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.currentKnownSize();
    if (knownSize == 0) {
      return emptyIterator();
    }
    if (knownSize == 1) {
      return iterator();
    }
    return new LazyIterator<E>(new ReduceWhileIteratorMaterializer<E>(materializer,
        Require.notNull(condition, "condition"), Require.notNull(operation, "operation")));
  }

  @Override
  public @NotNull Iterator<E> removeFirst(final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new RemoveFirstIteratorMaterializer<E>(materializer,
        Require.notNull(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> removeFirst(final @NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new RemoveFirstIteratorMaterializer<E>(materializer,
        toIndexedPredicate(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> removeFirstSequence(final @NotNull java.lang.Iterable<?> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    if (getKnownSize(elements) == 0) {
      return iterator();
    }
    final ListMaterializer<Object> elementsMaterializer = LazyList.getElementsMaterializer(
        Require.notNull(elements, "elements"));
    return new LazyIterator<E>(
        new RemoveFirstSequenceIteratorMaterializer<E>(materializer, elementsMaterializer));
  }

  @Override
  public @NotNull Iterator<E> removeLast(final @NotNull IndexedPredicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new RemoveLastIteratorMaterializer<E>(materializer,
        Require.notNull(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> removeLast(final @NotNull Predicate<? super E> predicate) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    return new LazyIterator<E>(new RemoveLastIteratorMaterializer<E>(materializer,
        toIndexedPredicate(predicate, "predicate")));
  }

  @Override
  public @NotNull Iterator<E> removeLastSequence(final @NotNull java.lang.Iterable<?> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    if (getKnownSize(elements) == 0) {
      return iterator();
    }
    final ListMaterializer<Object> elementsMaterializer = LazyList.getElementsMaterializer(
        Require.notNull(elements, "elements"));
    return new LazyIterator<E>(
        new RemoveLastSequenceIteratorMaterializer<E>(materializer, elementsMaterializer));
  }

  @Override
  public @NotNull Iterator<E> removeSequence(final @NotNull java.lang.Iterable<?> elements) {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer.currentKnownSize() == 0) {
      return emptyIterator();
    }
    if (getKnownSize(elements) == 0) {
      return iterator();
    }
    final ListMaterializer<Object> elementsMaterializer = LazyList.getElementsMaterializer(
        Require.notNull(elements, "elements"));
    return new LazyIterator<E>(
        new RemoveSequenceIteratorMaterializer<E>(materializer, elementsMaterializer));
  }

  @Override
  public @NotNull Iterator<E> removeSlice(final int start) {
    return start == -1 ? removeSlice(-1, Integer.MAX_VALUE) : removeSlice(start, start + 1);
  }

  @Override
  public @NotNull Iterator<E> removeSlice(final int start, final int end) {
    if ((end >= 0 || start < 0) && start >= end) {
      return iterator();
    }
    final IteratorMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.currentKnownSize();
    if (knownSize == 0) {
      return this;
    }
    if (start == 0 && end > start) {
      return dropFirst(end);
    }
    if (knownSize > 0) {
      final int knownStart;
      if (start < 0) {
        knownStart = Math.max(0, knownSize + start);
      } else {
        knownStart = Math.min(knownSize, start);
      }
      final int knownEnd;
      if (end < 0) {
        knownEnd = Math.max(0, knownSize + end);
      } else {
        knownEnd = Math.min(knownSize, end);
      }
      if (knownStart >= knownEnd) {
        return iterator();
      }
      final int knownLength = knownEnd - knownStart;
      if (knownLength == knownSize) {
        return Iterator.of();
      }
    }
    return new LazyIterator<E>(new RemoveSliceIteratorMaterializer<E>(materializer, start, end));
  }

  @Override
  public @NotNull Iterator<E> replaceFirstSequence(final @NotNull java.lang.Iterable<?> elements,
      final @NotNull Function<? super java.lang.Iterable<E>, java.lang.Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> replaceLastSequence(@NotNull java.lang.Iterable<?> elements,
      @NotNull Function<? super java.lang.Iterable<E>, java.lang.Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> replaceSequence(@NotNull java.lang.Iterable<?> elements,
      @NotNull Function<? super java.lang.Iterable<E>, java.lang.Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> replaceSequence(@NotNull java.lang.Iterable<?> elements,
      @NotNull IndexedFunction<? super java.lang.Iterable<E>, java.lang.Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> replaceSlice(int start, int end,
      @NotNull java.lang.Iterable<? extends E> patch) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> replaceSlice(int start, @NotNull Iterable<? extends E> patch) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> resizeTo(@NotNegative int numElements, E padding) {
    return null;
  }

  @Override
  public int size() {
    final int knownSize = materializer.currentKnownSize();
    if (knownSize >= 0) {
      return knownSize;
    }
    final int size = materializer.materializeSkip(Integer.MAX_VALUE);
    if (size == Integer.MAX_VALUE && materializer.materializeSkip(1) > 0) {
      throw new SizeOverflowException(1L + Integer.MAX_VALUE);
    }
    return size;
  }

  @Override
  public int skip(final int maxElements) {
    if (maxElements <= 0) {
      return 0;
    }
    return materializer.materializeSkip(maxElements);
  }

  @Override
  public @NotNull Iterator<E> slice(int start) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> slice(int start, int end) {
    return null;
  }

  @Override
  public @NotNull Iterator<? extends Iterator<E>> slidingWindow(@Positive int maxSize,
      @Positive int step) {
    return null;
  }

  @Override
  public @NotNull Iterator<? extends Iterator<E>> slidingWindowWithPadding(@Positive int size,
      @Positive int step, E padding) {
    return null;
  }

  @Override
  public @NotNull Iterator<Boolean> startsWith(@NotNull java.lang.Iterable<?> elements) {
    return null;
  }

  @Override
  public @NotNull <X extends Throwable> Iterator<E> switchExceptionally(
      @NotNull Class<X> exceptionType,
      @NotNull Function<? super X, ? extends java.lang.Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public @NotNull <X extends Throwable> Iterator<E> switchExceptionally(
      @NotNull Class<X> exceptionType,
      @NotNull IndexedFunction<? super X, ? extends java.lang.Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> switchExceptionally(
      @NotNull Function<? super Throwable, ? extends java.lang.Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> switchExceptionally(
      @NotNull IndexedFunction<? super Throwable, ? extends java.lang.Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> symmetricDiff(@NotNull java.lang.Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> takeFirst(int maxElements) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> takeFirstWhile(@NotNull IndexedPredicate<? super E> condition) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> takeFirstWhile(@NotNull Predicate<? super E> condition) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> takeLast(int maxElements) {
    return null;
  }

  @Override
  public @NotNull Object[] toArray() {
    return new Object[0];
  }

  @Override
  public @NotNull <R> R[] toArray(@NotNull R[] array) {
    return null;
  }

  @Override
  public @NotNull Iterator<E> union(@NotNull java.lang.Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public @NotNull <F> Iterator<ZipEntry<E, F>> zip(@NotNull java.lang.Iterable<F> elements) {
    return null;
  }

  @Override
  public @NotNull <F> Iterator<ZipEntry<E, F>> zipWithPadding(
      @NotNull java.lang.Iterable<F> elements, E paddingLeft, F paddingRight) {
    return null;
  }

  int knownSize() {
    return materializer.currentKnownSize();
  }
}
