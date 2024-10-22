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

import static sparx.lazy.indexedIdentity;

import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.internal.future.IteratorFutureMaterializerToListFutureMaterializer;
import sparx.internal.future.ListFutureMaterializerToIteratorFutureMaterializer;
import sparx.internal.future.iterator.AppendAllIteratorFutureMaterializer;
import sparx.internal.future.iterator.AppendIteratorFutureMaterializer;
import sparx.internal.future.iterator.CollectionToIteratorFutureMaterializer;
import sparx.internal.future.iterator.CountIteratorFutureMaterializer;
import sparx.internal.future.iterator.CountWhereIteratorFutureMaterializer;
import sparx.internal.future.iterator.DiffIteratorFutureMaterializer;
import sparx.internal.future.iterator.DistinctByIteratorFutureMaterializer;
import sparx.internal.future.iterator.DropIteratorFutureMaterializer;
import sparx.internal.future.iterator.DropRightIteratorFutureMaterializer;
import sparx.internal.future.iterator.DropRightWhileIteratorFutureMaterializer;
import sparx.internal.future.iterator.DropWhileIteratorFutureMaterializer;
import sparx.internal.future.iterator.EachIteratorFutureMaterializer;
import sparx.internal.future.iterator.ElementToIteratorFutureMaterializer;
import sparx.internal.future.iterator.EmptyIteratorFutureMaterializer;
import sparx.internal.future.iterator.EndsWithIteratorFutureMaterializer;
import sparx.internal.future.iterator.ExistsIteratorFutureMaterializer;
import sparx.internal.future.iterator.FilterIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindFirstIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindIndexIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindIndexOfSliceIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindLastIndexIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindLastIndexOfSliceIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindLastIteratorFutureMaterializer;
import sparx.internal.future.iterator.FlatMapAfterIteratorFutureMaterializer;
import sparx.internal.future.iterator.FlatMapFirstWhereIteratorFutureMaterializer;
import sparx.internal.future.iterator.FlatMapIteratorFutureMaterializer;
import sparx.internal.future.iterator.FlatMapLastWhereIteratorFutureMaterializer;
import sparx.internal.future.iterator.FoldLeftIteratorFutureMaterializer;
import sparx.internal.future.iterator.FoldLeftWhileIteratorFutureMaterializer;
import sparx.internal.future.iterator.FoldRightIteratorFutureMaterializer;
import sparx.internal.future.iterator.FoldRightWhileIteratorFutureMaterializer;
import sparx.internal.future.iterator.IncludesAllIteratorFutureMaterializer;
import sparx.internal.future.iterator.IncludesSliceIteratorFutureMaterializer;
import sparx.internal.future.iterator.InsertAfterIteratorFutureMaterializer;
import sparx.internal.future.iterator.InsertAllAfterIteratorFutureMaterializer;
import sparx.internal.future.iterator.InsertAllIteratorFutureMaterializer;
import sparx.internal.future.iterator.InsertIteratorFutureMaterializer;
import sparx.internal.future.iterator.IntersectIteratorFutureMaterializer;
import sparx.internal.future.iterator.IteratorForFuture;
import sparx.internal.future.iterator.IteratorFutureMaterializer;
import sparx.internal.future.iterator.IteratorGetFuture;
import sparx.internal.future.iterator.IteratorToIteratorFutureMaterializer;
import sparx.internal.future.iterator.IteratorWhileFuture;
import sparx.internal.future.iterator.ListToIteratorFutureMaterializer;
import sparx.internal.future.iterator.MapAfterIteratorFutureMaterializer;
import sparx.internal.future.iterator.MapFirstWhereIteratorFutureMaterializer;
import sparx.internal.future.iterator.MapIteratorFutureMaterializer;
import sparx.internal.future.iterator.SwitchIteratorFutureMaterializer;
import sparx.internal.future.iterator.TransformIteratorFutureMaterializer;
import sparx.internal.future.iterator.WrappingIteratorFutureMaterializer;
import sparx.internal.future.list.AbstractListFutureMaterializer;
import sparx.internal.future.list.AppendAllListFutureMaterializer;
import sparx.internal.future.list.AppendListFutureMaterializer;
import sparx.internal.future.list.CountListFutureMaterializer;
import sparx.internal.future.list.CountWhereListFutureMaterializer;
import sparx.internal.future.list.DiffListFutureMaterializer;
import sparx.internal.future.list.DistinctByListFutureMaterializer;
import sparx.internal.future.list.DropListFutureMaterializer;
import sparx.internal.future.list.DropRightListFutureMaterializer;
import sparx.internal.future.list.DropRightWhileListFutureMaterializer;
import sparx.internal.future.list.DropWhileListFutureMaterializer;
import sparx.internal.future.list.EachListFutureMaterializer;
import sparx.internal.future.list.ElementToListFutureMaterializer;
import sparx.internal.future.list.EmptyListFutureMaterializer;
import sparx.internal.future.list.EndsWithListFutureMaterializer;
import sparx.internal.future.list.ExistsListFutureMaterializer;
import sparx.internal.future.list.FilterListFutureMaterializer;
import sparx.internal.future.list.FindFirstListFutureMaterializer;
import sparx.internal.future.list.FindIndexListFutureMaterializer;
import sparx.internal.future.list.FindIndexOfSliceListFutureMaterializer;
import sparx.internal.future.list.FindLastIndexListFutureMaterializer;
import sparx.internal.future.list.FindLastIndexOfSliceListFutureMaterializer;
import sparx.internal.future.list.FindLastListFutureMaterializer;
import sparx.internal.future.list.FlatMapAfterListFutureMaterializer;
import sparx.internal.future.list.FlatMapFirstWhereListFutureMaterializer;
import sparx.internal.future.list.FlatMapLastWhereListFutureMaterializer;
import sparx.internal.future.list.FlatMapListFutureMaterializer;
import sparx.internal.future.list.FlatMapWhereListFutureMaterializer;
import sparx.internal.future.list.FoldLeftListFutureMaterializer;
import sparx.internal.future.list.FoldLeftWhileListFutureMaterializer;
import sparx.internal.future.list.FoldRightListFutureMaterializer;
import sparx.internal.future.list.FoldRightWhileListFutureMaterializer;
import sparx.internal.future.list.IncludesAllListFutureMaterializer;
import sparx.internal.future.list.IncludesSliceListFutureMaterializer;
import sparx.internal.future.list.InsertAfterListFutureMaterializer;
import sparx.internal.future.list.InsertAllAfterListFutureMaterializer;
import sparx.internal.future.list.IntersectListFutureMaterializer;
import sparx.internal.future.list.ListForFuture;
import sparx.internal.future.list.ListFutureMaterializer;
import sparx.internal.future.list.ListGetFuture;
import sparx.internal.future.list.ListToListFutureMaterializer;
import sparx.internal.future.list.ListWhileFuture;
import sparx.internal.future.list.MapAfterListFutureMaterializer;
import sparx.internal.future.list.MapFirstWhereListFutureMaterializer;
import sparx.internal.future.list.MapLastWhereListFutureMaterializer;
import sparx.internal.future.list.MapListFutureMaterializer;
import sparx.internal.future.list.MaxListFutureMaterializer;
import sparx.internal.future.list.OrElseListFutureMaterializer;
import sparx.internal.future.list.PrependAllListFutureMaterializer;
import sparx.internal.future.list.PrependListFutureMaterializer;
import sparx.internal.future.list.ReduceLeftListFutureMaterializer;
import sparx.internal.future.list.ReduceRightListFutureMaterializer;
import sparx.internal.future.list.RemoveAfterListFutureMaterializer;
import sparx.internal.future.list.RemoveFirstWhereListFutureMaterializer;
import sparx.internal.future.list.RemoveLastWhereListFutureMaterializer;
import sparx.internal.future.list.RemoveSliceListFutureMaterializer;
import sparx.internal.future.list.RemoveWhereListFutureMaterializer;
import sparx.internal.future.list.ReplaceSliceListFutureMaterializer;
import sparx.internal.future.list.ResizeListFutureMaterializer;
import sparx.internal.future.list.ReverseListFutureMaterializer;
import sparx.internal.future.list.SliceListFutureMaterializer;
import sparx.internal.future.list.SlidingWindowListFutureMaterializer;
import sparx.internal.future.list.SlidingWindowListFutureMaterializer.Splitter;
import sparx.internal.future.list.SortedListFutureMaterializer;
import sparx.internal.future.list.StartsWithListFutureMaterializer;
import sparx.internal.future.list.StopCancelListFutureMaterializer;
import sparx.internal.future.list.SwitchListFutureMaterializer;
import sparx.internal.future.list.SymmetricDiffListFutureMaterializer;
import sparx.internal.future.list.TakeListFutureMaterializer;
import sparx.internal.future.list.TakeRightListFutureMaterializer;
import sparx.internal.future.list.TakeRightWhileListFutureMaterializer;
import sparx.internal.future.list.TakeWhileListFutureMaterializer;
import sparx.internal.future.list.TransformListFutureMaterializer;
import sparx.internal.future.list.WrappingListFutureMaterializer;
import sparx.itf.Sequence;
import sparx.util.DeadLockException;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.UncheckedException;
import sparx.util.annotation.NotNegative;
import sparx.util.annotation.Positive;
import sparx.util.function.Action;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;
import sparx.util.function.TernaryFunction;

class future extends Sparx {

  private future() {
  }

  static int getKnownSize(final Iterable<?> elements) {
    if (elements instanceof List) {
      return ((List<?>) elements).materializer.knownSize();
    }
    if (elements instanceof Iterator) {
      return ((Iterator<?>) elements).materializer.knownSize();
    }
    return lazy.getKnownSize(elements);
  }

  private static boolean isNotFuture(final Iterable<?> elements) {
    return !(elements instanceof List || elements instanceof Iterator);
  }

  public static class Iterator<E> implements itf.Future<E, Iterator<E>>, itf.Iterator<E> {

    private static final Logger LOGGER = Logger.getLogger(Iterator.class.getName());

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final IteratorFutureMaterializer<E> materializer;
    private final String taskID;

    Iterator(@NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IteratorFutureMaterializer<E> materializer) {
      this.context = context;
      this.materializer = materializer;
      this.cancelException = cancelException;
      taskID = Integer.toHexString(System.identityHashCode(this));
    }

    private static @NotNull <E> Iterator<E> cloneIterator(@NotNull final ExecutionContext context,
        @NotNull final IteratorFutureMaterializer<E> materializer) {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new WrappingIteratorFutureMaterializer<E>(materializer, context, cancelException));
    }

    private static @NotNull <E> Iterator<E> emptyIterator(@NotNull final ExecutionContext context) {
      return new Iterator<E>(context, new AtomicReference<CancellationException>(),
          EmptyIteratorFutureMaterializer.<E>instance());
    }

    private static @NotNull Iterator<Boolean> falseIterator(
        @NotNull final ExecutionContext context) {
      return new Iterator<Boolean>(context, new AtomicReference<CancellationException>(),
          new ElementToIteratorFutureMaterializer<Boolean>(false));
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> IteratorFutureMaterializer<E> getElementsMaterializer(
        @NotNull final ExecutionContext context, @NotNull final Iterable<? extends E> elements) {
      if (elements instanceof List) {
        final List<E> list = (List<E>) elements;
        if (context.equals(list.context)) {
          return new ListFutureMaterializerToIteratorFutureMaterializer<E>(list.materializer);
        }
        return new ListFutureMaterializerToIteratorFutureMaterializer<E>(
            new SwitchListFutureMaterializer<E>(list.context, list.taskID, context,
                list.materializer));
      }
      if (elements instanceof java.util.List) {
        final java.util.List<E> list = (java.util.List<E>) elements;
        if (list.isEmpty()) {
          return EmptyIteratorFutureMaterializer.instance();
        }
        if (list.size() == 1) {
          return new ElementToIteratorFutureMaterializer<E>(list.get(0));
        }
        return new ListToIteratorFutureMaterializer<E>(list, context);
      }
      if (elements instanceof java.util.Collection) {
        return new CollectionToIteratorFutureMaterializer<E>((Collection<E>) elements, context);
      }
      if (elements instanceof Iterator) {
        final Iterator<E> iterator = (Iterator<E>) elements;
        if (context.equals(iterator.context)) {
          return iterator.materializer;
        }
        return new SwitchIteratorFutureMaterializer<E>(iterator.context, iterator.taskID, context,
            iterator.materializer);
      }
      if (elements instanceof java.util.Iterator) {
        return new IteratorToIteratorFutureMaterializer<E>((java.util.Iterator<E>) elements,
            context);
      }
      return new IteratorToIteratorFutureMaterializer<E>(
          (java.util.Iterator<E>) elements.iterator(), context);
    }

    private static @NotNull <E, F> IndexedFunction<E, IteratorFutureMaterializer<F>> getElementToIteratorMaterializer(
        @NotNull final ExecutionContext context,
        @NotNull final Function<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, IteratorFutureMaterializer<F>>() {
        @Override
        public IteratorFutureMaterializer<F> apply(final int index, final E element)
            throws Exception {
          return getElementsMaterializer(context, mapper.apply(element));
        }
      };
    }

    private static @NotNull <E, F> IndexedFunction<E, IteratorFutureMaterializer<F>> getElementToIteratorMaterializer(
        @NotNull final ExecutionContext context,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, IteratorFutureMaterializer<F>>() {
        @Override
        public IteratorFutureMaterializer<F> apply(final int index, final E element)
            throws Exception {
          return getElementsMaterializer(context, mapper.apply(index, element));
        }
      };
    }

    private static @NotNull <E> IndexedFunction<E, IteratorFutureMaterializer<E>> getElementToIteratorMaterializer(
        @NotNull final ExecutionContext context,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<E, IteratorFutureMaterializer<E>>() {
        @Override
        public IteratorFutureMaterializer<E> apply(final int index, final E element)
            throws Exception {
          if (predicate.test(index, element)) {
            return getElementsMaterializer(context, mapper.apply(index, element));
          }
          return new ElementToIteratorFutureMaterializer<E>(element);
        }
      };
    }

    private static @NotNull <E> IndexedFunction<E, IteratorFutureMaterializer<E>> getElementToIteratorMaterializer(
        @NotNull final ExecutionContext context, @NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<E, IteratorFutureMaterializer<E>>() {
        @Override
        public IteratorFutureMaterializer<E> apply(final int index, final E element)
            throws Exception {
          if (predicate.test(element)) {
            return getElementsMaterializer(context, mapper.apply(element));
          }
          return new ElementToIteratorFutureMaterializer<E>(element);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerAppend(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final E element) {
      final long knownSize = materializer.knownSize();
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 ? SizeOverflowException.safeCast(knownSize + 1) : -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).append(element);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerAppendAll(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements, final int elementsKnownSize) {
      final long knownSize = materializer.knownSize();
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 && elementsKnownSize > 0 ? SizeOverflowException.safeCast(
              knownSize + elementsKnownSize) : -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).appendAll(elements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Integer> lazyMaterializerCount(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      return new LazyIteratorFutureMaterializer<E, Integer>(materializer, context, cancelException,
          1) {
        @Override
        protected @NotNull java.util.Iterator<Integer> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).count();
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Integer> lazyMaterializerCountWhere(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, Integer>(materializer, context, cancelException,
          1) {
        @Override
        protected @NotNull java.util.Iterator<Integer> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).countWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerDiff(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).diff(elements);
        }
      };
    }

    private static @NotNull <E, K> LazyIteratorFutureMaterializer<E, E> lazyMaterializerDistinctBy(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedFunction<? super E, K> keyExtractor) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).distinctBy(keyExtractor);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerDrop(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 ? Math.max(0, knownSize - maxElements) : -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).drop(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerDropRight(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 ? Math.max(0, knownSize - maxElements) : -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).dropRight(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerDropRightWhile(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).dropRightWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerDropWhile(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).dropWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Boolean> lazyMaterializerEach(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, Boolean>(materializer, context, cancelException,
          1) {
        @Override
        protected @NotNull java.util.Iterator<Boolean> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).each(predicate);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Boolean> lazyMaterializerEndsWith(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyIteratorFutureMaterializer<E, Boolean>(materializer, context, cancelException,
          1) {
        @Override
        protected @NotNull java.util.Iterator<Boolean> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).endsWith(elements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Boolean> lazyMaterializerExists(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, Boolean>(materializer, context, cancelException,
          1) {
        @Override
        protected @NotNull java.util.Iterator<Boolean> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).exists(predicate);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerFilter(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).filter(predicate);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerFindFirst(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).findFirst(predicate);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Integer> lazyMaterializerFindIndexWhere(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, Integer>(materializer, context, cancelException,
          -1) {
        @Override
        protected @NotNull java.util.Iterator<Integer> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).findIndexWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Integer> lazyMaterializerFindIndexOfSlice(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyIteratorFutureMaterializer<E, Integer>(materializer, context, cancelException,
          -1) {
        @Override
        protected @NotNull java.util.Iterator<Integer> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).findIndexOfSlice(elements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerFindLast(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).findLast(predicate);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Integer> lazyMaterializerFindLastIndexOfSlice(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyIteratorFutureMaterializer<E, Integer>(materializer, context, cancelException,
          -1) {
        @Override
        protected @NotNull java.util.Iterator<Integer> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).findLastIndexOfSlice(elements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Integer> lazyMaterializerFindLastIndexWhere(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyIteratorFutureMaterializer<E, Integer>(materializer, context, cancelException,
          -1) {
        @Override
        protected @NotNull java.util.Iterator<Integer> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).findLastIndexWhere(predicate);
        }
      };
    }

    private static @NotNull <E, F> LazyIteratorFutureMaterializer<E, F> lazyMaterializerFoldLeft(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return new LazyIteratorFutureMaterializer<E, F>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.Iterator<F> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).foldLeft(identity, operation);
        }
      };
    }

    private static @NotNull <E, F> LazyIteratorFutureMaterializer<E, F> lazyMaterializerFoldLeftWhile(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return new LazyIteratorFutureMaterializer<E, F>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.Iterator<F> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).foldLeftWhile(identity, predicate, operation);
        }
      };
    }

    private static @NotNull <E, F> LazyIteratorFutureMaterializer<E, F> lazyMaterializerFoldRight(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      return new LazyIteratorFutureMaterializer<E, F>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.Iterator<F> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).foldRight(identity, operation);
        }
      };
    }

    private static @NotNull <E, F> LazyIteratorFutureMaterializer<E, F> lazyMaterializerFoldRightWhile(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      return new LazyIteratorFutureMaterializer<E, F>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.Iterator<F> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).foldRightWhile(identity, predicate, operation);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Boolean> lazyMaterializerIncludesAll(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyIteratorFutureMaterializer<E, Boolean>(materializer, context, cancelException,
          1) {
        @Override
        protected @NotNull java.util.Iterator<Boolean> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).includesAll(elements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, Boolean> lazyMaterializerIncludesSlice(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyIteratorFutureMaterializer<E, Boolean>(materializer, context, cancelException,
          1) {
        @Override
        protected @NotNull java.util.Iterator<Boolean> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).includesSlice(elements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerInsert(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final E element) {
      final long knownSize = materializer.knownSize();
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 ? SizeOverflowException.safeCast(knownSize + 1) : -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).insert(element);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerInsertAfter(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElements, final E element) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).insertAfter(numElements, element);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerInsertAllAfter(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElements, @NotNull final Iterable<? extends E> elements) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).insertAllAfter(numElements, elements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerInsertAll(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements, final int elementsKnownSize) {
      final long knownSize = materializer.knownSize();
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 && elementsKnownSize > 0 ? SizeOverflowException.safeCast(
              knownSize + elementsKnownSize) : -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).insertAll(elements);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerIntersect(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).intersect(elements);
        }
      };
    }

    private static @NotNull <E, F> LazyIteratorFutureMaterializer<E, F> lazyMaterializerMap(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedFunction<? super E, F> mapper) {
      return new LazyIteratorFutureMaterializer<E, F>(materializer, context, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.Iterator<F> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).map(mapper);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerMapAfter(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElements, @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).mapAfter(numElements, mapper);
        }
      };
    }

    private static @NotNull <E> LazyIteratorFutureMaterializer<E, E> lazyMaterializerMapFirstWhere(
        @NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new LazyIteratorFutureMaterializer<E, E>(materializer, context, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.Iterator<E> transform(
            @NotNull final java.util.Iterator<E> iterator) {
          return lazy.Iterator.ofIterator(iterator).mapFirstWhere(predicate, mapper);
        }
      };
    }

    private static @NotNull Iterator<Boolean> trueIterator(
        @NotNull final ExecutionContext context) {
      return new Iterator<Boolean>(context, new AtomicReference<CancellationException>(),
          new ElementToIteratorFutureMaterializer<Boolean>(true));
    }

    private static @NotNull Iterator<Integer> zeroIterator(
        @NotNull final ExecutionContext context) {
      return new Iterator<Integer>(context, new AtomicReference<CancellationException>(),
          new ElementToIteratorFutureMaterializer<Integer>(0));
    }

    @Override
    public @NotNull Iterator<E> append(final E element) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new Iterator<E>(context, cancelException,
            new ElementToIteratorFutureMaterializer<E>(element));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerAppend(materializer, context, cancelException, element));
      }
      return new Iterator<E>(context, cancelException,
          new AppendIteratorFutureMaterializer<E>(materializer, element, context, cancelException,
              List.<E>appendFunction()));
    }

    @Override
    public @NotNull Iterator<E> appendAll(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final int elementsKnownSize = getKnownSize(elements);
      if (elementsKnownSize == 0) {
        return cloneIterator(context, materializer);
      }
      final long knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return cloneIterator(context,
            getElementsMaterializer(context, Require.notNull(elements, "elements")));
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerAppendAll(materializer, context, cancelException,
                Require.notNull(elements, "elements"), elementsKnownSize));
      }
      return new Iterator<E>(context, cancelException,
          new AppendAllIteratorFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException, List.<E>appendAllFunction()));
    }

    @Override
    public <T> T apply(@NotNull Function<? super Sequence<E>, T> mapper) {
      return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <F> Iterator<F> as() {
      return (Iterator<F>) this;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      if (!materializer.isDone() && cancelException.compareAndSet(null,
          new CancellationException())) {
        final ExecutionContext context = this.context;
        if (mayInterruptIfRunning) {
          context.interruptTask(taskID);
        }
        context.scheduleBefore(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeCancel(cancelException.get());
            } catch (final Exception e) {
              LOGGER.log(Level.SEVERE, "Ignored exception", e);
            }
          }
        });
        synchronized (cancelException) {
          cancelException.notifyAll();
        }
        return true;
      }
      return false;
    }

    @Override
    public @NotNull Iterator<Integer> count() {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return zeroIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerCount(materializer, context, cancelException));
      }
      return new Iterator<Integer>(context, cancelException,
          new CountIteratorFutureMaterializer<E>(materializer, context, cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> countWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return zeroIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerCountWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<Integer>(context, cancelException,
          new CountWhereIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> countWhere(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return zeroIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerCountWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<Integer>(context, cancelException,
          new CountWhereIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> diff(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneIterator(context, materializer);
      }
      if (getKnownSize(elements) == 0) {
        return cloneIterator(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerDiff(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new Iterator<E>(context, cancelException,
          new DiffIteratorFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> distinct() {
      return distinctBy(indexedIdentity());
    }

    @Override
    public @NotNull <K> Iterator<E> distinctBy(@NotNull final Function<? super E, K> keyExtractor) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return cloneIterator(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerDistinctBy(materializer, context, cancelException,
                toIndexedFunction(Require.notNull(keyExtractor, "keyExtractor"))));
      }
      return new Iterator<E>(context, cancelException,
          new DistinctByIteratorFutureMaterializer<E, K>(materializer,
              toIndexedFunction(Require.notNull(keyExtractor, "keyExtractor")), context,
              cancelException));
    }

    @Override
    public @NotNull <K> Iterator<E> distinctBy(
        @NotNull final IndexedFunction<? super E, K> keyExtractor) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return cloneIterator(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerDistinctBy(materializer, context, cancelException,
                Require.notNull(keyExtractor, "keyExtractor")));
      }
      return new Iterator<E>(context, cancelException,
          new DistinctByIteratorFutureMaterializer<E, K>(materializer,
              Require.notNull(keyExtractor, "keyExtractor"), context, cancelException));
    }

    @Override
    public void doFor(@NotNull final Consumer<? super E> consumer) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingFor(consumer).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doFor(@NotNull final IndexedConsumer<? super E> consumer) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingFor(consumer).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingWhile(predicate).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingWhile(condition, consumer).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> predicate) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingWhile(predicate).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingWhile(condition, consumer).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public @NotNull Iterator<E> drop(final int maxElements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        return cloneIterator(context, materializer);
      }
      if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerDrop(materializer, context, cancelException, maxElements));
      }
      return new Iterator<E>(context, cancelException,
          new DropIteratorFutureMaterializer<E>(materializer, maxElements, context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> dropRight(final int maxElements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        return cloneIterator(context, materializer);
      }
      if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerDropRight(materializer, context, cancelException, maxElements));
      }
      return new Iterator<E>(context, cancelException,
          new DropRightIteratorFutureMaterializer<E>(materializer, maxElements, context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> dropRightWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerDropRightWhile(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<E>(context, cancelException,
          new DropRightWhileIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull Iterator<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerDropRightWhile(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<E>(context, cancelException,
          new DropRightWhileIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> dropWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerDropWhile(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<E>(context, cancelException,
          new DropWhileIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>prependFunction()));
    }

    @Override
    public @NotNull Iterator<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerDropWhile(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<E>(context, cancelException,
          new DropWhileIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>prependFunction()));
    }

    @Override
    public @NotNull Iterator<Boolean> each(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Boolean>(context, cancelException,
            lazyMaterializerEach(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<Boolean>(context, cancelException,
          new EachIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), false, context, cancelException));
    }

    @Override
    public @NotNull Iterator<Boolean> each(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Boolean>(context, cancelException,
            lazyMaterializerEach(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<Boolean>(context, cancelException,
          new EachIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), false, context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<Boolean> endsWith(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      if (getKnownSize(elements) == 0) {
        return trueIterator(context);
      }
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new Iterator<Boolean>(context, cancelException,
            lazyMaterializerEndsWith(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new Iterator<Boolean>(context, cancelException,
          new EndsWithIteratorFutureMaterializer<E>(materializer,
              List.getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<Boolean> exists(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<Boolean>(context, cancelException,
          new ExistsIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), false, context, cancelException));
    }

    @Override
    public @NotNull Iterator<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<Boolean>(context, cancelException,
          new ExistsIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), false, context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerFilter(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<E>(context, cancelException,
          new FilterIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull Iterator<E> filter(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerFilter(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<E>(context, cancelException,
          new FilterIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> findAny(@NotNull final IndexedPredicate<? super E> predicate) {
      return findFirst(predicate);
    }

    @Override
    public @NotNull Iterator<E> findAny(@NotNull final Predicate<? super E> predicate) {
      return findFirst(predicate);
    }

    @Override
    public @NotNull Iterator<E> findFirst(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerFindFirst(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<E>(context, cancelException,
          new FindFirstIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull Iterator<E> findFirst(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerFindFirst(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<E>(context, cancelException,
          new FindFirstIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> findIndexOf(final Object element) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, context, cancelException,
                equalsElement(element)));
      }
      return new Iterator<Integer>(context, cancelException,
          new FindIndexIteratorFutureMaterializer<E>(materializer, equalsElement(element), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (getKnownSize(elements) == 0) {
        return zeroIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerFindIndexOfSlice(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new Iterator<Integer>(context, cancelException,
          new FindIndexOfSliceIteratorFutureMaterializer<E>(materializer,
              List.getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> findIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<Integer>(context, cancelException,
          new FindIndexIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> findIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<Integer>(context, cancelException,
          new FindIndexIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerFindLast(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<E>(context, cancelException,
          new FindLastIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull Iterator<E> findLast(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerFindLast(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<E>(context, cancelException,
          new FindLastIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> findLastIndexOf(final Object element) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, context, cancelException,
                equalsElement(element)));
      }
      return new Iterator<Integer>(context, cancelException,
          new FindLastIndexIteratorFutureMaterializer<E>(materializer, equalsElement(element),
              context, cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexOfSlice(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new Iterator<Integer>(context, cancelException,
          new FindLastIndexOfSliceIteratorFutureMaterializer<E>(materializer,
              List.getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> findLastIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new Iterator<Integer>(context, cancelException,
          new FindLastIndexIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull Iterator<Integer> findLastIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new Iterator<Integer>(context, cancelException,
          new FindLastIndexIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public E first() {
      return next();
    }

    @Override
    public @NotNull <F> Iterator<F> flatMap(
        @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<F>(context, cancelException,
          new FlatMapIteratorFutureMaterializer<E, F>(materializer,
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull <F> Iterator<F> flatMap(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<F>(context, cancelException,
          new FlatMapIteratorFutureMaterializer<E, F>(materializer,
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> flatMapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneIterator(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
        return cloneIterator(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new FlatMapAfterIteratorFutureMaterializer<E>(materializer, numElements,
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> flatMapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneIterator(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
        return cloneIterator(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new FlatMapAfterIteratorFutureMaterializer<E>(materializer, numElements,
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> flatMapFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new FlatMapFirstWhereIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>prependAllFunction()));
    }

    @Override
    public @NotNull Iterator<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new FlatMapFirstWhereIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>prependAllFunction()));
    }

    @Override
    public @NotNull Iterator<E> flatMapLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new FlatMapLastWhereIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>prependAllFunction()));
    }

    @Override
    public @NotNull Iterator<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new FlatMapLastWhereIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>prependAllFunction()));
    }

    @Override
    public @NotNull Iterator<E> flatMapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new FlatMapIteratorFutureMaterializer<E, E>(materializer,
              getElementToIteratorMaterializer(context, Require.notNull(predicate, "predicate"),
                  Require.notNull(mapper, "mapper")), context, cancelException));
    }

    @Override
    public @NotNull Iterator<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new FlatMapIteratorFutureMaterializer<E, E>(materializer,
              getElementToIteratorMaterializer(context, Require.notNull(predicate, "predicate"),
                  Require.notNull(mapper, "mapper")), context, cancelException));
    }

    @Override
    public @NotNull <F> Iterator<F> fold(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return foldLeft(identity, operation);
    }

    @Override
    public @NotNull <F> Iterator<F> foldLeft(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new Iterator<F>(context, cancelException,
            new ElementToIteratorFutureMaterializer<F>(identity));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<F>(context, cancelException,
            lazyMaterializerFoldLeft(materializer, context, cancelException, identity,
                Require.notNull(operation, "operation")));
      }
      return new Iterator<F>(context, cancelException,
          new FoldLeftIteratorFutureMaterializer<E, F>(materializer, identity,
              Require.notNull(operation, "operation"), context, cancelException));
    }

    @Override
    public @NotNull <F> Iterator<F> foldLeftWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new Iterator<F>(context, cancelException,
            new ElementToIteratorFutureMaterializer<F>(identity));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<F>(context, cancelException,
            lazyMaterializerFoldLeftWhile(materializer, context, cancelException, identity,
                Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation")));
      }
      return new Iterator<F>(context, cancelException,
          new FoldLeftWhileIteratorFutureMaterializer<E, F>(materializer, identity,
              Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation"),
              context, cancelException));
    }

    @Override
    public @NotNull <F> Iterator<F> foldRight(final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new Iterator<F>(context, cancelException,
            new ElementToIteratorFutureMaterializer<F>(identity));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<F>(context, cancelException,
            lazyMaterializerFoldRight(materializer, context, cancelException, identity,
                Require.notNull(operation, "operation")));
      }
      return new Iterator<F>(context, cancelException,
          new FoldRightIteratorFutureMaterializer<E, F>(materializer, identity,
              Require.notNull(operation, "operation"), context, cancelException));
    }

    @Override
    public @NotNull <F> Iterator<F> foldRightWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new Iterator<F>(context, cancelException,
            new ElementToIteratorFutureMaterializer<F>(identity));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<F>(context, cancelException,
            lazyMaterializerFoldRightWhile(materializer, context, cancelException, identity,
                Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation")));
      }
      return new Iterator<F>(context, cancelException,
          new FoldRightWhileIteratorFutureMaterializer<E, F>(materializer, identity,
              Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation"),
              context, cancelException));
    }

    @Override
    public Iterator<E> get() throws InterruptedException, ExecutionException {
      final BlockingConsumer<java.util.List<E>> consumer = new BlockingConsumer<java.util.List<E>>(
          cancelException);
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeElements(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        consumer.get();
        return this;
      } catch (final InterruptedException e) {
        throw e;
      } catch (final Exception e) {
        if (isCancelled() && e instanceof CancellationException) {
          throw (CancellationException) new CancellationException().initCause(e);
        }
        throw new ExecutionException(e);
      }
    }

    @Override
    public Iterator<E> get(final long timeout, @NotNull final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      final BlockingConsumer<java.util.List<E>> consumer = new BlockingConsumer<java.util.List<E>>(
          cancelException);
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeElements(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        consumer.get(timeout, unit);
        return this;
      } catch (final InterruptedException e) {
        throw e;
      } catch (final Exception e) {
        if (isCancelled() && e instanceof CancellationException) {
          throw (CancellationException) new CancellationException().initCause(e);
        }
        throw new ExecutionException(e);
      }
    }

    @Override
    public boolean hasNext() {
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return false;
      }
      final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeHasNext(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeHasNext(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull Iterator<Boolean> includes(final Object element) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, context, cancelException,
                Sparx.<E>equalsElement(element)));
      }
      return new Iterator<Boolean>(context, cancelException,
          new ExistsIteratorFutureMaterializer<E>(materializer, Sparx.<E>equalsElement(element),
              false, context, cancelException));
    }

    @Override
    public @NotNull Iterator<Boolean> includesAll(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (getKnownSize(elements) == 0) {
        return trueIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new Iterator<Boolean>(context, cancelException,
            lazyMaterializerIncludesAll(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new Iterator<Boolean>(context, cancelException,
          new IncludesAllIteratorFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (getKnownSize(elements) == 0) {
        return trueIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new Iterator<Boolean>(context, cancelException,
            lazyMaterializerIncludesSlice(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new Iterator<Boolean>(context, cancelException,
          new IncludesSliceIteratorFutureMaterializer<E>(materializer,
              List.getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull Iterator<E> insert(final E element) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new Iterator<E>(context, cancelException,
            new ElementToIteratorFutureMaterializer<E>(element));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerInsert(materializer, context, cancelException, element));
      }
      return new Iterator<E>(context, cancelException,
          new InsertIteratorFutureMaterializer<E>(materializer, element, context, cancelException,
              List.<E>prependFunction()));
    }

    @Override
    public @NotNull Iterator<E> insertAfter(final int numElements, final E element) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final long knownSize = materializer.knownSize();
      if (numElements < 0 || numElements == Integer.MAX_VALUE || (knownSize >= 0
          && numElements > knownSize)) {
        return cloneIterator(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (knownSize == 0 && numElements == 0) {
        return new Iterator<E>(context, cancelException,
            new ElementToIteratorFutureMaterializer<E>(element));
      }
      if (numElements == 0) {
        if (materializer.isMaterializedAtOnce()) {
          return new Iterator<E>(context, cancelException,
              lazyMaterializerInsert(materializer, context, cancelException, element));
        }
        return new Iterator<E>(context, cancelException,
            new InsertIteratorFutureMaterializer<E>(materializer, element, context, cancelException,
                List.<E>prependFunction()));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerInsertAfter(materializer, context, cancelException, numElements,
                element));
      }
      return new Iterator<E>(context, cancelException,
          new InsertAfterIteratorFutureMaterializer<E>(materializer, numElements, element, context,
              cancelException, List.<E>insertAfterFunction()));
    }

    @Override
    public @NotNull Iterator<E> insertAll(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final int elementsKnownSize = getKnownSize(elements);
      if (elementsKnownSize == 0) {
        return cloneIterator(context, materializer);
      }
      if (materializer.knownSize() == 0) {
        return cloneIterator(context,
            getElementsMaterializer(context, Require.notNull(elements, "elements")));
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerInsertAll(materializer, context, cancelException,
                Require.notNull(elements, "elements"), elementsKnownSize));
      }
      return new Iterator<E>(context, cancelException,
          new InsertAllIteratorFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException, List.<E>prependAllFunction()));
    }

    @Override
    public @NotNull Iterator<E> insertAllAfter(final int numElements,
        @NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final long knownSize = materializer.knownSize();
      if (numElements < 0 || numElements == Integer.MAX_VALUE || (knownSize >= 0
          && numElements > knownSize)) {
        return cloneIterator(context, materializer);
      }
      final IteratorFutureMaterializer<E> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      if (knownSize == 0 && numElements == 0) {
        return cloneIterator(context, elementsMaterializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (numElements == 0) {
        if (materializer.isMaterializedAtOnce()) {
          return new Iterator<E>(context, cancelException,
              lazyMaterializerInsertAll(materializer, context, cancelException, elements,
                  elementsMaterializer.knownSize()));
        }
        return new Iterator<E>(context, cancelException,
            new InsertAllIteratorFutureMaterializer<E>(materializer, elementsMaterializer, context,
                cancelException, List.<E>prependAllFunction()));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerInsertAllAfter(materializer, context, cancelException, numElements,
                elements));
      }
      return new Iterator<E>(context, cancelException,
          new InsertAllAfterIteratorFutureMaterializer<E>(materializer, numElements,
              elementsMaterializer, context, cancelException));
    }

    @Override
    public @NotNull Iterator<E> intersect(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneIterator(context, materializer);
      }
      if (getKnownSize(elements) == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerIntersect(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new Iterator<E>(context, cancelException,
          new IntersectIteratorFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public boolean isCancelled() {
      return materializer.isCancelled() || cancelException.get() != null;
    }

    @Override
    public boolean isDone() {
      return materializer.isDone() || cancelException.get() != null;
    }

    @Override
    public boolean isEmpty() {
      return !hasNext();
    }

    @Override
    public boolean isFailed() {
      return materializer.isFailed();
    }

    @Override
    public boolean isSucceeded() {
      return materializer.isSucceeded();
    }

    @Override
    public @NotNull Iterator<E> iterator() {
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new Iterator<E>(context, cancelException,
          new WrappingIteratorFutureMaterializer<E>(materializer, context, cancelException));
    }

    @Override
    public E last() {
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        throw new NoSuchElementException();
      }
      final BlockingLastElementConsumer<E> consumer = new BlockingLastElementConsumer<E>(
          cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeNextWhile(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeNextWhile(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull <F> Iterator<F> map(@NotNull final Function<? super E, F> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<F>(context, cancelException,
            lazyMaterializerMap(materializer, context, cancelException,
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new Iterator<F>(context, cancelException,
          new MapIteratorFutureMaterializer<E, F>(materializer,
              toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException));
    }

    @Override
    public @NotNull <F> Iterator<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<F>(context, cancelException,
            lazyMaterializerMap(materializer, context, cancelException,
                Require.notNull(mapper, "mapper")));
      }
      return new Iterator<F>(context, cancelException,
          new MapIteratorFutureMaterializer<E, F>(materializer, Require.notNull(mapper, "mapper"),
              context, cancelException));
    }

    @Override
    public @NotNull Iterator<E> mapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneIterator(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerMapAfter(materializer, context, cancelException, numElements,
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new Iterator<E>(context, cancelException,
          new MapAfterIteratorFutureMaterializer<E>(materializer, numElements,
              toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull Iterator<E> mapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneIterator(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return emptyIterator(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerMapAfter(materializer, context, cancelException, numElements,
                Require.notNull(mapper, "mapper")));
      }
      return new Iterator<E>(context, cancelException,
          new MapAfterIteratorFutureMaterializer<E>(materializer, numElements,
              Require.notNull(mapper, "mapper"), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull Iterator<E> mapFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneIterator(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
      }
      return new Iterator<E>(context, cancelException,
          new MapFirstWhereIteratorFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper"), context,
              cancelException, List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull Iterator<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneIterator(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new Iterator<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate")),
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new Iterator<E>(context, cancelException,
          new MapFirstWhereIteratorFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull Iterator<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> mapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> max(@NotNull Comparator<? super E> comparator) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> min(@NotNull Comparator<? super E> comparator) {
      return null;
    }

    @Override
    public E next() {
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        throw new NoSuchElementException();
      }
      final BlockingNextElementConsumer<E> consumer = new BlockingNextElementConsumer<E>(
          cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeNext(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeNext(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final Consumer<? super E> consumer) {
      return new IteratorForFuture<E>(context, taskID, cancelException, materializer,
          toIndexedConsumer(Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final IndexedConsumer<? super E> consumer) {
      return new IteratorForFuture<E>(context, taskID, cancelException, materializer,
          Require.notNull(consumer, "consumer"));
    }

    @Override
    public @NotNull Future<?> nonBlockingGet() {
      return new IteratorGetFuture<E>(context, taskID, cancelException, materializer);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new IteratorWhileFuture<E>(context, taskID, cancelException, materializer,
          Require.notNull(predicate, "predicate"));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      return new IteratorWhileFuture<E>(context, taskID, cancelException, materializer,
          Require.notNull(condition, "condition"), Require.notNull(consumer, "consumer"));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> predicate) {
      return new IteratorWhileFuture<E>(context, taskID, cancelException, materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      return new IteratorWhileFuture<E>(context, taskID, cancelException, materializer,
          toIndexedPredicate(Require.notNull(condition, "condition")),
          toIndexedConsumer(Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull Iterator<Boolean> none(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<Boolean> none(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<Boolean> notAll(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public boolean notEmpty() {
      return hasNext();
    }

    @Override
    public @NotNull Iterator<E> orElse(@NotNull Iterable<? extends E> elements) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> orElseGet(
        @NotNull Supplier<? extends Iterable<? extends E>> supplier) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> peek(@NotNull Consumer<? super E> consumer) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> peek(@NotNull IndexedConsumer<? super E> consumer) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> peekExceptionally(@NotNull Consumer<? super Throwable> consumer) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> peekExceptionally(
        @NotNull IndexedConsumer<? super Throwable> consumer) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> plus(E element) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> plusAll(@NotNull Iterable<? extends E> elements) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> reduce(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> reduceLeft(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> reduceRight(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return null;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }

    @Override
    public @NotNull Iterator<E> removeAfter(int numElements) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeEach(E element) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeFirst(E element) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeFirstWhere(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeLast(E element) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeLastWhere(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeSlice(int start, int end) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> removeWhere(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceAfter(int numElements, E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceEach(E element, E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceFirst(E element, E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceLast(E element, E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceLastWhere(@NotNull Predicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceSlice(int start, int end,
        @NotNull Iterable<? extends E> patch) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> replaceWhere(@NotNull Predicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> resizeTo(int numElements, E padding) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> runFinally(@NotNull Action action) {
      return null;
    }

    @Override
    public int size() {
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return 0;
      }
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeSkip(Integer.MAX_VALUE, consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeSkip(Integer.MAX_VALUE, consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public int skip(final int maxElements) {
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      if (maxElements <= 0 || materializer.knownSize() == 0) {
        return 0;
      }
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeSkip(maxElements, consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeSkip(maxElements, consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
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
    public @NotNull Iterator<? extends itf.Iterator<E>> slidingWindow(int maxSize, int step) {
      return null;
    }

    @Override
    public @NotNull Iterator<? extends itf.Iterator<E>> slidingWindowWithPadding(int size, int step,
        E padding) {
      return null;
    }

    @Override
    public @NotNull Iterator<Boolean> startsWith(@NotNull Iterable<?> elements) {
      return null;
    }

    @Override
    public @NotNull <T extends Throwable> Iterator<E> switchExceptionally(
        @NotNull Class<T> exceptionType,
        @NotNull Function<? super T, ? extends Iterable<? extends E>> mapper) {
      return null;
    }

    @Override
    public @NotNull <T extends Throwable> Iterator<E> switchExceptionally(
        @NotNull Class<T> exceptionType,
        @NotNull IndexedFunction<? super T, ? extends Iterable<? extends E>> mapper) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> switchExceptionally(
        @NotNull Function<? super Throwable, ? extends Iterable<? extends E>> mapper) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> switchExceptionally(
        @NotNull IndexedFunction<? super Throwable, ? extends Iterable<? extends E>> mapper) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> symmetricDiff(@NotNull Iterable<? extends E> elements) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> take(int maxElements) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> takeRight(int maxElements) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> takeRightWhile(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull Iterator<E> takeWhile(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    // TODO: extra
    public @NotNull List<E> toList() {
      final ExecutionContext context = this.context;
      final IteratorFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer instanceof ListFutureMaterializerToIteratorFutureMaterializer) {
        return new List<E>(context, cancelException, new WrappingListFutureMaterializer<E>(
            ((ListFutureMaterializerToIteratorFutureMaterializer<E>) materializer).materializer(),
            context, cancelException));
      }
      return new List<E>(context, cancelException,
          new IteratorFutureMaterializerToListFutureMaterializer<E>(context, cancelException,
              materializer));
    }

    @Override
    public @NotNull Iterator<E> union(@NotNull Iterable<? extends E> elements) {
      return null;
    }

    private static abstract class LazyIteratorFutureMaterializer<E, F> extends
        TransformIteratorFutureMaterializer<E, F> {

      public LazyIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
          @NotNull final ExecutionContext context,
          @NotNull final AtomicReference<CancellationException> cancelException,
          final int knownSize) {
        super(wrapped, context, cancelException, knownSize);
      }

      @Override
      @SuppressWarnings("unchecked")
      protected int skip(final int count, @NotNull final java.util.Iterator<F> iterator) {
        if (iterator instanceof lazy.Iterator) {
          return ((lazy.Iterator<E>) iterator).skip(count);
        }
        int skipped = 0;
        while (skipped < count && iterator.hasNext()) {
          iterator.next();
          ++skipped;
        }
        return skipped;
      }
    }
  }

  public static class List<E> extends AbstractListSequence<E> implements itf.Future<E, List<E>>,
      itf.List<E> {

    private static final BinaryFunction<? extends java.util.List<?>, ? extends java.util.List<?>, ? extends java.util.List<?>> APPEND_ALL_FUNCTION = new BinaryFunction<java.util.List<?>, java.util.List<?>, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> firstParam,
          final java.util.List<?> secondParam) {
        return lazy.List.wrap(firstParam).appendAll(secondParam).materialized();
      }
    };
    private static final BinaryFunction<? extends java.util.List<?>, ?, ? extends java.util.List<?>> APPEND_FUNCTION = new BinaryFunction<java.util.List<?>, Object, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> firstParam, final Object secondParam) {
        return lazy.List.wrap(firstParam).append(secondParam).materialized();
      }
    };
    private static final ElementToListFutureMaterializer<Boolean> FALSE_MATERIALIZER = new ElementToListFutureMaterializer<Boolean>(
        false);
    private static final TernaryFunction<? extends java.util.List<?>, Integer, ?, ? extends java.util.List<?>> INSERT_AFTER_FUNCTION = new TernaryFunction<java.util.List<?>, Integer, Object, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> firstParam, final Integer secondParam,
          final Object thirdParam) {
        return lazy.List.wrap(firstParam).insertAfter(secondParam, thirdParam).materialized();
      }
    };
    private static final TernaryFunction<? extends java.util.List<?>, Integer, ? extends java.util.List<?>, ? extends java.util.List<?>> INSERT_ALL_AFTER_FUNCTION = new TernaryFunction<java.util.List<?>, Integer, java.util.List<?>, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> firstParam, final Integer secondParam,
          final java.util.List<?> thirdParam) {
        return lazy.List.wrap(firstParam).insertAllAfter(secondParam, thirdParam).materialized();
      }
    };
    private static final BinaryFunction<? extends java.util.List<?>, ? extends java.util.List<?>, ? extends java.util.List<?>> PREPEND_ALL_FUNCTION = new BinaryFunction<java.util.List<?>, java.util.List<?>, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> firstParam,
          final java.util.List<?> secondParam) {
        return lazy.List.wrap(firstParam).prependAll(secondParam).materialized();
      }
    };
    private static final BinaryFunction<? extends java.util.List<?>, ?, ? extends java.util.List<?>> PREPEND_FUNCTION = new BinaryFunction<java.util.List<?>, Object, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> firstParam, final Object secondParam) {
        return lazy.List.wrap(firstParam).prepend(secondParam).materialized();
      }
    };
    private static final BinaryFunction<? extends java.util.List<?>, Integer, ? extends java.util.List<?>> REMOVE_AFTER_FUNCTION = new BinaryFunction<java.util.List<?>, Integer, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> firstParam,
          final Integer secondParam) {
        return lazy.List.wrap(firstParam).removeAfter(secondParam).materialized();
      }
    };
    private static final TernaryFunction<? extends java.util.List<?>, Integer, ?, ? extends java.util.List<?>> REPLACE_AFTER_FUNCTION = new TernaryFunction<java.util.List<?>, Integer, Object, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> firstParam, final Integer secondParam,
          final Object thirdParam) {
        return lazy.List.wrap(firstParam).replaceAfter(secondParam, thirdParam).materialized();
      }
    };
    private static final TernaryFunction<? extends java.util.List<?>, Integer, ?, ? extends java.util.List<?>> RESIZE_FUNCTION = new TernaryFunction<java.util.List<?>, Integer, Object, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> firstParam, final Integer secondParam,
          final Object thirdParam) {
        return lazy.List.wrap(firstParam).resizeTo(secondParam, thirdParam).materialized();
      }
    };
    private static final Function<? extends java.util.List<?>, ? extends java.util.List<?>> REVERSE_FUNCTION = new Function<java.util.List<?>, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(final java.util.List<?> param) {
        return lazy.List.wrap(param).reverse().materialized();
      }
    };
    private static final ElementToListFutureMaterializer<Boolean> TRUE_MATERIALIZER = new ElementToListFutureMaterializer<Boolean>(
        true);
    private static final ElementToListFutureMaterializer<Integer> ZERO_MATERIALIZER = new ElementToListFutureMaterializer<Integer>(
        0);

    private static final Logger LOGGER = Logger.getLogger(List.class.getName());

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ListFutureMaterializer<E> materializer;
    private final String taskID;

    List(@NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final ListFutureMaterializer<E> materializer) {
      this.context = context;
      this.materializer = materializer;
      this.cancelException = cancelException;
      taskID = Integer.toHexString(System.identityHashCode(this));
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> BinaryFunction<java.util.List<E>, java.util.List<E>, java.util.List<E>> appendAllFunction() {
      return (BinaryFunction<java.util.List<E>, java.util.List<E>, java.util.List<E>>) APPEND_ALL_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> BinaryFunction<java.util.List<E>, E, java.util.List<E>> appendFunction() {
      return (BinaryFunction<java.util.List<E>, E, java.util.List<E>>) APPEND_FUNCTION;
    }

    private static @NotNull <E> List<E> cloneList(@NotNull final ExecutionContext context,
        @NotNull final ListFutureMaterializer<E> materializer) {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new WrappingListFutureMaterializer<E>(materializer, context, cancelException));
    }

    private static @NotNull <E> List<E> emptyList(@NotNull final ExecutionContext context) {
      return new List<E>(context, new AtomicReference<CancellationException>(),
          EmptyListFutureMaterializer.<E>instance());
    }

    private static @NotNull List<Boolean> falseList(@NotNull final ExecutionContext context) {
      return new List<Boolean>(context, new AtomicReference<CancellationException>(),
          FALSE_MATERIALIZER);
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> ListFutureMaterializer<E> getElementsMaterializer(
        @NotNull final ExecutionContext context, @NotNull final Iterable<? extends E> elements) {
      if (elements instanceof List) {
        final List<E> list = (List<E>) elements;
        if (context.equals(list.context)) {
          return list.materializer;
        }
        return new SwitchListFutureMaterializer<E>(list.context, list.taskID, context,
            list.materializer);
      }
      if (elements instanceof lazy.List) {
        // TODO: materialized????
        final lazy.List<E> materialized = ((lazy.List<E>) elements).materialized();
        final int size = materialized.size();
        if (size == 0) {
          return EmptyListFutureMaterializer.instance();
        }
        if (size == 1) {
          return new ElementToListFutureMaterializer<E>(materialized.first());
        }
        return new ListToListFutureMaterializer<E>(materialized, context);
      }
      if (elements instanceof java.util.List) {
        final java.util.List<E> list = (java.util.List<E>) elements;
        final int size = list.size();
        if (size == 0) {
          return EmptyListFutureMaterializer.instance();
        }
        if (size == 1) {
          return new ElementToListFutureMaterializer<E>(list.get(0));
        }
        return new ListToListFutureMaterializer<E>(list, context);
      }
      if (elements instanceof java.util.Collection) {
        final java.util.Collection<E> collection = (java.util.Collection<E>) elements;
        final int size = collection.size();
        if (size == 0) {
          return EmptyListFutureMaterializer.instance();
        }
        if (size == 1) {
          return new ElementToListFutureMaterializer<E>(collection.iterator().next());
        }
        return new ListToListFutureMaterializer<E>(lazy.List.wrap(collection), context);
      }
      if (elements instanceof Iterator) {
        final Iterator<E> iterator = (Iterator<E>) elements;
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        if (context.equals(iterator.context)) {
          return new IteratorFutureMaterializerToListFutureMaterializer<E>(context, cancelException,
              iterator.materializer);
        }
        return new IteratorFutureMaterializerToListFutureMaterializer<E>(context, cancelException,
            new SwitchIteratorFutureMaterializer<E>(iterator.context, iterator.taskID, context,
                iterator.materializer));
      }
      final lazy.List<E> list = lazy.List.wrap(elements);
      final int knownSize = list.knownSize();
      if (knownSize == 0) {
        return EmptyListFutureMaterializer.instance();
      }
      if (knownSize == 1) {
        return new ElementToListFutureMaterializer<E>(list.get(0));
      }
      return new ListToListFutureMaterializer<E>(list, context, knownSize);
    }

    private static @NotNull <E> IndexedFunction<E, ListFutureMaterializer<E>> getElementToListMaterializer(
        @NotNull final ExecutionContext context,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<E, ListFutureMaterializer<E>>() {
        @Override
        public ListFutureMaterializer<E> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(context, mapper.apply(element));
        }
      };
    }

    private static @NotNull <E> IndexedFunction<E, ListFutureMaterializer<E>> getElementToListMaterializer(
        @NotNull final ExecutionContext context,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<E, ListFutureMaterializer<E>>() {
        @Override
        public ListFutureMaterializer<E> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(context, mapper.apply(index, element));
        }
      };
    }

    private static @NotNull <E> Splitter<E, List<E>> getSplitter(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      return new Splitter<E, List<E>>() {
        @Override
        public @NotNull List<E> getChunk(@NotNull final ListFutureMaterializer<E> materializer,
            final int start, final int end) {
          return new List<E>(context, cancelException, materializer).slice(start, end);
        }

        @Override
        public void getElements(@NotNull final List<E> chunk,
            @NotNull final FutureConsumer<java.util.List<E>> consumer) {
          final ListFutureMaterializer<E> materializer = chunk.materializer;
          context.scheduleAfter(new ContextTask(context) {
            @Override
            public @NotNull String taskID() {
              return taskID;
            }

            @Override
            public int weight() {
              return materializer.weightElements();
            }

            @Override
            protected void runWithContext() {
              materializer.materializeElements(consumer);
            }
          });
        }
      };
    }

    private static @NotNull <E> Splitter<E, List<E>> getSplitter(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final AtomicReference<CancellationException> cancelException, final int size,
        final E padding) {
      return new Splitter<E, List<E>>() {
        @Override
        public @NotNull List<E> getChunk(@NotNull final ListFutureMaterializer<E> materializer,
            final int start, final int end) {
          final List<E> sliced = new List<E>(context, cancelException, materializer).slice(start,
              end);
          final int paddingSize = size - (end - start);
          if (paddingSize > 0) {
            return sliced.appendAll(lazy.List.times(paddingSize, padding));
          }
          return sliced;
        }

        @Override
        public void getElements(@NotNull final List<E> chunk,
            @NotNull final FutureConsumer<java.util.List<E>> consumer) {
          final ListFutureMaterializer<E> materializer = chunk.materializer;
          context.scheduleAfter(new ContextTask(context) {
            @Override
            public @NotNull String taskID() {
              return taskID;
            }

            @Override
            public int weight() {
              return materializer.weightElements();
            }

            @Override
            protected void runWithContext() {
              materializer.materializeElements(consumer);
            }
          });
        }
      };
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> TernaryFunction<java.util.List<E>, Integer, E, java.util.List<E>> insertAfterFunction() {
      return (TernaryFunction<java.util.List<E>, Integer, E, java.util.List<E>>) INSERT_AFTER_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> TernaryFunction<java.util.List<E>, Integer, java.util.List<E>, java.util.List<E>> insertAllAfterFunction() {
      return (TernaryFunction<java.util.List<E>, Integer, java.util.List<E>, java.util.List<E>>) INSERT_ALL_AFTER_FUNCTION;
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerAppend(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final E element) {
      final long knownSize = materializer.knownSize();
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 ? SizeOverflowException.safeCast(knownSize + 1) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).append(element);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerAppendAll(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements, final int elementsKnownSize) {
      final long knownSize = materializer.knownSize();
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 && elementsKnownSize > 0 ? SizeOverflowException.safeCast(
              knownSize + elementsKnownSize) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).appendAll(elements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Integer> lazyMaterializerCount(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      return new LazyListFutureMaterializer<E, Integer>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).count();
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Integer> lazyMaterializerCountWhere(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, Integer>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).countWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerDiff(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).diff(elements);
        }
      };
    }

    private static @NotNull <E, K> LazyListFutureMaterializer<E, E> lazyMaterializerDistinctBy(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedFunction<? super E, K> keyExtractor) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).distinctBy(keyExtractor);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerDrop(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 ? Math.max(0, knownSize - maxElements) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).drop(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerDropRight(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 ? Math.max(0, knownSize - maxElements) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).dropRight(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerDropRightWhile(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).dropRightWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerDropWhile(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).dropWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Boolean> lazyMaterializerEach(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, Boolean>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).each(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Boolean> lazyMaterializerEndsWith(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyListFutureMaterializer<E, Boolean>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).endsWith(elements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Boolean> lazyMaterializerExists(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, Boolean>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).exists(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerFilter(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).filter(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerFindFirst(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).findFirst(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Integer> lazyMaterializerFindIndexWhere(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, Integer>(materializer, context, cancelException,
          -1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).findIndexWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Integer> lazyMaterializerFindIndexOfSlice(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyListFutureMaterializer<E, Integer>(materializer, context, cancelException,
          -1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).findIndexOfSlice(elements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerFindLast(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).findLast(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Integer> lazyMaterializerFindLastIndexWhere(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, Integer>(materializer, context, cancelException,
          -1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).findLastIndexWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Integer> lazyMaterializerFindLastIndexOfSlice(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyListFutureMaterializer<E, Integer>(materializer, context, cancelException,
          -1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).findLastIndexOfSlice(elements);
        }
      };
    }

    private static @NotNull <E, F> LazyListFutureMaterializer<E, F> lazyMaterializerFoldLeft(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return new LazyListFutureMaterializer<E, F>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<F> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).foldLeft(identity, operation);
        }
      };
    }

    private static @NotNull <E, F> LazyListFutureMaterializer<E, F> lazyMaterializerFoldLeftWhile(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return new LazyListFutureMaterializer<E, F>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<F> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).foldLeftWhile(identity, predicate, operation);
        }
      };
    }

    private static @NotNull <E, F> LazyListFutureMaterializer<E, F> lazyMaterializerFoldRight(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      return new LazyListFutureMaterializer<E, F>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<F> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).foldRight(identity, operation);
        }
      };
    }

    private static @NotNull <E, F> LazyListFutureMaterializer<E, F> lazyMaterializerFoldRightWhile(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      return new LazyListFutureMaterializer<E, F>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<F> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).foldRightWhile(identity, predicate, operation);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Boolean> lazyMaterializerIncludesAll(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyListFutureMaterializer<E, Boolean>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).includesAll(elements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Boolean> lazyMaterializerIncludesSlice(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyListFutureMaterializer<E, Boolean>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).includesSlice(elements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerInsertAfter(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElements, final E element) {
      final long knownSize = materializer.knownSize();
      final int size;
      if (knownSize > 0) {
        if (numElements <= knownSize) {
          size = SizeOverflowException.safeCast(knownSize + 1);
        } else {
          size = (int) knownSize;
        }
      } else {
        size = -1;
      }
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, size) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).insertAfter(numElements, element);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerInsertAllAfter(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElements, @NotNull final Iterable<? extends E> elements,
        final int elementsKnownSize) {
      final long knownSize = materializer.knownSize();
      final int size;
      if (knownSize > 0 && elementsKnownSize >= 0) {
        if (numElements <= knownSize) {
          size = SizeOverflowException.safeCast(knownSize + elementsKnownSize);
        } else {
          size = (int) knownSize;
        }
      } else {
        size = -1;
      }
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, size) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).insertAllAfter(numElements, elements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerIntersect(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).intersect(elements);
        }
      };
    }

    private static @NotNull <E, F> LazyListFutureMaterializer<E, F> lazyMaterializerMap(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedFunction<? super E, F> mapper) {
      return new LazyListFutureMaterializer<E, F>(materializer, context, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<F> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).map(mapper);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerMapAfter(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElements, @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).mapAfter(numElements, mapper);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerMapFirstWhere(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).mapFirstWhere(predicate, mapper);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerMapLastWhere(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).mapLastWhere(predicate, mapper);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerMax(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Comparator<? super E> comparator) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).max(comparator);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Boolean> lazyMaterializerNone(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, Boolean>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).none(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, Boolean> lazyMaterializerNotExists(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, Boolean>(materializer, context, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).notAll(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerOrElse(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements) {
      final int knownSize = materializer.knownSize();
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 ? knownSize : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).orElse(elements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerPrepend(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final E element) {
      final long knownSize = materializer.knownSize();
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 ? SizeOverflowException.safeCast(knownSize + 1) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).prepend(element);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerPrependAll(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements, final int elementsKnownSize) {
      final long knownSize = materializer.knownSize();
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize > 0 && elementsKnownSize > 0 ? SizeOverflowException.safeCast(
              knownSize + elementsKnownSize) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).prependAll(elements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerReduceLeft(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).reduceLeft(operation);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerReduceRight(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).reduceRight(operation);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerRemoveAfter(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElement) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).removeAfter(numElement);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerRemoveFirstWhere(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).removeFirstWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerRemoveLastWhere(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).removeLastWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerRemoveSlice(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final int start,
        final int end) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).removeSlice(start, end);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerRemoveWhere(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).removeWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerReplaceSlice(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final int start,
        final int end, @NotNull final Iterable<? extends E> patch) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).replaceSlice(start, end, patch);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerResizeTo(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNegative final int numElements, final E padding) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          numElements) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).resizeTo(numElements, padding);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerReverse(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).reverse();
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerSlice(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException, final int start,
        final int end) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).slice(start, end);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerSorted(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Comparator<? super E> comparator) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).sorted(comparator);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerSymmetricDiff(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).symmetricDiff(elements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerTake(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize >= 0 ? Math.min(knownSize, maxElements) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).take(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerTakeRight(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException,
          knownSize >= 0 ? Math.min(knownSize, maxElements) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).takeRight(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerTakeRightWhile(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).takeRightWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerTakeWhile(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).takeWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListFutureMaterializer<E, E> lazyMaterializerUnion(
        @NotNull final ListFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements) {
      return new LazyListFutureMaterializer<E, E>(materializer, context, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> list) {
          return lazy.List.wrap(list).union(elements);
        }
      };
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> BinaryFunction<java.util.List<E>, java.util.List<E>, java.util.List<E>> prependAllFunction() {
      return (BinaryFunction<java.util.List<E>, java.util.List<E>, java.util.List<E>>) PREPEND_ALL_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> BinaryFunction<java.util.List<E>, E, java.util.List<E>> prependFunction() {
      return (BinaryFunction<java.util.List<E>, E, java.util.List<E>>) PREPEND_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> BinaryFunction<java.util.List<E>, Integer, java.util.List<E>> removeAfterFunction() {
      return (BinaryFunction<java.util.List<E>, Integer, java.util.List<E>>) REMOVE_AFTER_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> TernaryFunction<java.util.List<E>, Integer, E, java.util.List<E>> replaceAfterFunction() {
      return (TernaryFunction<java.util.List<E>, Integer, E, java.util.List<E>>) REPLACE_AFTER_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> TernaryFunction<java.util.List<E>, Integer, E, java.util.List<E>> resizeFunction() {
      return (TernaryFunction<java.util.List<E>, Integer, E, java.util.List<E>>) RESIZE_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> Function<java.util.List<E>, java.util.List<E>> reverseFunction() {
      return (Function<java.util.List<E>, java.util.List<E>>) REVERSE_FUNCTION;
    }

    private static @NotNull List<Boolean> trueList(@NotNull final ExecutionContext context) {
      return new List<Boolean>(context, new AtomicReference<CancellationException>(),
          TRUE_MATERIALIZER);
    }

    private static @NotNull List<Integer> zeroList(@NotNull final ExecutionContext context) {
      return new List<Integer>(context, new AtomicReference<CancellationException>(),
          ZERO_MATERIALIZER);
    }

    @Override
    public @NotNull List<E> append(final E element) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final long knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return new List<E>(context, cancelException,
            new ElementToListFutureMaterializer<E>(element));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerAppend(materializer, context, cancelException, element));
      }
      return new List<E>(context, cancelException,
          new AppendListFutureMaterializer<E>(materializer, element, context, cancelException,
              List.<E>appendFunction()));
    }

    @Override
    public @NotNull List<E> appendAll(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int elementsKnownSize = getKnownSize(elements);
      if (elementsKnownSize == 0) {
        return cloneList(context, materializer);
      }
      final long knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return cloneList(context,
            getElementsMaterializer(context, Require.notNull(elements, "elements")));
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerAppendAll(materializer, context, cancelException,
                Require.notNull(elements, "elements"), elementsKnownSize));
      }
      return new List<E>(context, cancelException,
          new AppendAllListFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException, List.<E>appendAllFunction()));
    }

    @Override
    public <T> T apply(@NotNull Function<? super itf.Sequence<E>, T> mapper) {
      return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <F> List<F> as() {
      return (List<F>) this;
    }

    // TODO: extra
    public @NotNull lazy.List<E> asLazy() {
      return lazy.List.wrap(this);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      if (!materializer.isDone() && cancelException.compareAndSet(null,
          new CancellationException())) {
        final ExecutionContext context = this.context;
        if (mayInterruptIfRunning) {
          context.interruptTask(taskID);
        }
        context.scheduleBefore(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeCancel(cancelException.get());
            } catch (final Exception e) {
              LOGGER.log(Level.SEVERE, "Ignored exception", e);
            }
          }
        });
        synchronized (cancelException) {
          cancelException.notifyAll();
        }
        return true;
      }
      return false;
    }

    @Override
    public boolean contains(final Object o) {
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return false;
      }
      final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeContains(o, consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightContains();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeContains(o, consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull List<Integer> count() {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return zeroList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (knownSize > 0) {
        return new List<Integer>(context, cancelException,
            new ElementToListFutureMaterializer<Integer>(knownSize));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerCount(materializer, context, cancelException));
      }
      return new List<Integer>(context, cancelException,
          new CountListFutureMaterializer<E>(materializer, context, cancelException));
    }

    @Override
    public @NotNull List<Integer> countWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return zeroList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerCountWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Integer>(context, cancelException,
          new CountWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<Integer> countWhere(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return zeroList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerCountWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Integer>(context, cancelException,
          new CountWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> diff(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      if (getKnownSize(elements) == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerDiff(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<E>(context, cancelException, new DiffListFutureMaterializer<E>(materializer,
          getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
          cancelException));
    }

    @Override
    public @NotNull List<E> distinct() {
      return distinctBy(indexedIdentity());
    }

    @Override
    public @NotNull <K> List<E> distinctBy(@NotNull final Function<? super E, K> keyExtractor) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDistinctBy(materializer, context, cancelException,
                toIndexedFunction(Require.notNull(keyExtractor, "keyExtractor"))));
      }
      return new List<E>(context, cancelException,
          new DistinctByListFutureMaterializer<E, K>(materializer,
              toIndexedFunction(Require.notNull(keyExtractor, "keyExtractor")), context,
              cancelException));
    }

    @Override
    public @NotNull <K> List<E> distinctBy(
        @NotNull final IndexedFunction<? super E, K> keyExtractor) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDistinctBy(materializer, context, cancelException,
                Require.notNull(keyExtractor, "keyExtractor")));
      }
      return new List<E>(context, cancelException,
          new DistinctByListFutureMaterializer<E, K>(materializer,
              Require.notNull(keyExtractor, "keyExtractor"), context, cancelException));
    }

    @Override
    public void doFor(@NotNull final Consumer<? super E> consumer) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingFor(consumer).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doFor(@NotNull final IndexedConsumer<? super E> consumer) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingFor(consumer).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingWhile(predicate).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingWhile(condition, consumer).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> predicate) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingWhile(predicate).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingWhile(condition, consumer).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.throwUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public @NotNull List<E> drop(final int maxElements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        return cloneList(context, materializer);
      }
      if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDrop(materializer, context, cancelException, maxElements));
      }
      return new List<E>(context, cancelException,
          new DropListFutureMaterializer<E>(materializer, maxElements, context, cancelException));
    }

    @Override
    public @NotNull List<E> dropRight(final int maxElements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        return cloneList(context, materializer);
      }
      if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropRight(materializer, context, cancelException, maxElements));
      }
      return new List<E>(context, cancelException,
          new DropRightListFutureMaterializer<E>(materializer, maxElements, context,
              cancelException));
    }

    @Override
    public @NotNull List<E> dropRightWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropRightWhile(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new DropRightWhileListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropRightWhile(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException,
          new DropRightWhileListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> dropWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropWhile(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new DropWhileListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropWhile(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException,
          new DropWhileListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull List<Boolean> each(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerEach(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Boolean>(context, cancelException,
          new EachListFutureMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              false, context, cancelException));
    }

    @Override
    public @NotNull List<Boolean> each(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerEach(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Boolean>(context, cancelException,
          new EachListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), false, context,
              cancelException));
    }

    @Override
    public @NotNull List<Boolean> endsWith(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      if (getKnownSize(elements) == 0) {
        return trueList(context);
      }
      final ListFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerEndsWith(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<Boolean>(context, cancelException,
          new EndsWithListFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull List<Boolean> exists(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListFutureMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              false, context, cancelException));
    }

    @Override
    public @NotNull List<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), false, context,
              cancelException));
    }

    @Override
    public @NotNull List<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFilter(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new FilterListFutureMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              context, cancelException));
    }

    @Override
    public @NotNull List<E> filter(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFilter(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException, new FilterListFutureMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException));
    }

    @Override
    public @NotNull List<E> findAny(@NotNull final IndexedPredicate<? super E> predicate) {
      return findFirst(predicate);
    }

    @Override
    public @NotNull List<E> findAny(@NotNull final Predicate<? super E> predicate) {
      return findFirst(predicate);
    }

    @Override
    public @NotNull List<E> findFirst(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFindFirst(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new FindFirstListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<E> findFirst(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFindFirst(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException,
          new FindFirstListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull List<Integer> findIndexOf(final Object element) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, context, cancelException,
                equalsElement(element)));
      }
      return new List<Integer>(context, cancelException,
          new FindIndexListFutureMaterializer<E>(materializer, equalsElement(element), context,
              cancelException));
    }

    @Override
    public @NotNull List<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (getKnownSize(elements) == 0) {
        return zeroList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexOfSlice(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<Integer>(context, cancelException,
          new FindIndexOfSliceListFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull List<Integer> findIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Integer>(context, cancelException,
          new FindIndexListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<Integer> findIndexWhere(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Integer>(context, cancelException,
          new FindIndexListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFindLast(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new FindLastListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<E> findLast(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFindLast(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException,
          new FindLastListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull List<Integer> findLastIndexOf(final Object element) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, context, cancelException,
                equalsElement(element)));
      }
      return new List<Integer>(context, cancelException,
          new FindLastIndexListFutureMaterializer<E>(materializer, equalsElement(element), context,
              cancelException));
    }

    @Override
    public @NotNull List<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (getKnownSize(elements) == 0) {
        final int knownSize = materializer.knownSize();
        if (knownSize >= 0) {
          return new List<Integer>(context, cancelException,
              new ElementToListFutureMaterializer<Integer>(knownSize));
        }
      }
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexOfSlice(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<Integer>(context, cancelException,
          new FindLastIndexOfSliceListFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull List<Integer> findLastIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Integer>(context, cancelException,
          new FindLastIndexListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<Integer> findLastIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Integer>(context, cancelException,
          new FindLastIndexListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public E first() {
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        throw new IndexOutOfBoundsException("0");
      }
      final BlockingElementConsumer<E> consumer = new BlockingElementConsumer<E>(cancelException,
          0);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElement(0, consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElement();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeElement(0, consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull <F> List<F> flatMap(
        @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<F>(context, cancelException,
          new FlatMapListFutureMaterializer<E, F>(materializer,
              Iterator.getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")),
              context, cancelException));
    }

    @Override
    public @NotNull <F> List<F> flatMap(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<F>(context, cancelException,
          new FlatMapListFutureMaterializer<E, F>(materializer,
              Iterator.getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")),
              context, cancelException));
    }

    @Override
    public @NotNull List<E> flatMapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneList(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new FlatMapAfterListFutureMaterializer<E>(materializer, numElements,
              getElementToListMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> flatMapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneList(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new FlatMapAfterListFutureMaterializer<E>(materializer, numElements,
              getElementToListMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> flatMapFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new FlatMapFirstWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToListMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new FlatMapFirstWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToListMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> flatMapLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new FlatMapLastWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToListMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new FlatMapLastWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToListMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> flatMapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new FlatMapWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              Iterator.getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")),
              context, cancelException));
    }

    @Override
    public @NotNull List<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new FlatMapWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              Iterator.getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")),
              context, cancelException));
    }

    @Override
    public @NotNull <F> List<F> fold(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return foldLeft(identity, operation);
    }

    @Override
    public @NotNull <F> List<F> foldLeft(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<F>(context, cancelException,
            new ElementToListFutureMaterializer<F>(identity));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerFoldLeft(materializer, context, cancelException, identity,
                Require.notNull(operation, "operation")));
      }
      return new List<F>(context, cancelException,
          new FoldLeftListFutureMaterializer<E, F>(materializer, identity,
              Require.notNull(operation, "operation"), context, cancelException));
    }

    @Override
    public @NotNull <F> List<F> foldLeftWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<F>(context, cancelException,
            new ElementToListFutureMaterializer<F>(identity));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerFoldLeftWhile(materializer, context, cancelException, identity,
                Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation")));
      }
      return new List<F>(context, cancelException,
          new FoldLeftWhileListFutureMaterializer<E, F>(materializer, identity,
              Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation"),
              context, cancelException));
    }

    @Override
    public @NotNull <F> List<F> foldRight(final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<F>(context, cancelException,
            new ElementToListFutureMaterializer<F>(identity));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerFoldRight(materializer, context, cancelException, identity,
                Require.notNull(operation, "operation")));
      }
      return new List<F>(context, cancelException,
          new FoldRightListFutureMaterializer<E, F>(materializer, identity,
              Require.notNull(operation, "operation"), context, cancelException));
    }

    @Override
    public @NotNull <F> List<F> foldRightWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<F>(context, cancelException,
            new ElementToListFutureMaterializer<F>(identity));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerFoldRightWhile(materializer, context, cancelException, identity,
                Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation")));
      }
      return new List<F>(context, cancelException,
          new FoldRightWhileListFutureMaterializer<E, F>(materializer, identity,
              Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation"),
              context, cancelException));
    }

    @Override
    public List<E> get() throws InterruptedException, ExecutionException {
      final BlockingConsumer<java.util.List<E>> consumer = new BlockingConsumer<java.util.List<E>>(
          cancelException);
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeElements(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        consumer.get();
        return this;
      } catch (final InterruptedException e) {
        throw e;
      } catch (final Exception e) {
        if (isCancelled() && e instanceof CancellationException) {
          throw (CancellationException) new CancellationException().initCause(e);
        }
        throw new ExecutionException(e);
      }
    }

    @Override
    public E get(final int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final BlockingElementConsumer<E> consumer = new BlockingElementConsumer<E>(cancelException,
          index);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        final ListFutureMaterializer<E> materializer = this.materializer;
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElement(index, consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElement();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeElement(index, consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public List<E> get(final long timeout, @NotNull final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      final BlockingConsumer<java.util.List<E>> consumer = new BlockingConsumer<java.util.List<E>>(
          cancelException);
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeElements(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        consumer.get(timeout, unit);
        return this;
      } catch (final InterruptedException e) {
        throw e;
      } catch (final Exception e) {
        if (isCancelled() && e instanceof CancellationException) {
          throw (CancellationException) new CancellationException().initCause(e);
        }
        throw new ExecutionException(e);
      }
    }

    @Override
    public @NotNull List<Boolean> includes(final Object element) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, context, cancelException,
                Sparx.<E>equalsElement(element)));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListFutureMaterializer<E>(materializer, Sparx.<E>equalsElement(element), false,
              context, cancelException));
    }

    @Override
    public @NotNull List<Boolean> includesAll(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (getKnownSize(elements) == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerIncludesAll(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<Boolean>(context, cancelException,
          new IncludesAllListFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull List<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (getKnownSize(elements) == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerIncludesSlice(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<Boolean>(context, cancelException,
          new IncludesSliceListFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public int indexOf(final Object o) {
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return -1;
      }
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(new FutureConsumer<java.util.List<E>>() {
          @Override
          @SuppressWarnings("SuspiciousMethodCalls")
          public void accept(final java.util.List<E> elements) {
            consumer.accept(elements.indexOf(o));
          }

          @Override
          public void error(@NotNull final Exception error) {
            consumer.error(error);
          }
        });
      } else if (o == null) {
        context.scheduleAfter(new IndexOfNullFuturePredicate(consumer));
      } else {
        context.scheduleAfter(new IndexOfElementFuturePredicate(o, consumer));
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull List<E> insertAfter(final int numElements, final E element) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final long knownSize = materializer.knownSize();
      if (numElements < 0 || numElements == Integer.MAX_VALUE || (knownSize >= 0
          && numElements > knownSize)) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (knownSize == 0 && numElements == 0) {
        return new List<E>(context, cancelException,
            new ElementToListFutureMaterializer<E>(element));
      }
      if (numElements == 0) {
        if (materializer.isMaterializedAtOnce()) {
          return new List<E>(context, cancelException,
              lazyMaterializerPrepend(materializer, context, cancelException, element));
        }
        return new List<E>(context, cancelException,
            new PrependListFutureMaterializer<E>(materializer, element, context, cancelException,
                List.<E>prependFunction()));
      } else if (numElements == knownSize) {
        if (materializer.isMaterializedAtOnce()) {
          return new List<E>(context, cancelException,
              lazyMaterializerAppend(materializer, context, cancelException, element));
        }
        return new List<E>(context, cancelException,
            new AppendListFutureMaterializer<E>(materializer, element, context, cancelException,
                List.<E>appendFunction()));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerInsertAfter(materializer, context, cancelException, numElements,
                element));
      }
      return new List<E>(context, cancelException,
          new InsertAfterListFutureMaterializer<E>(materializer, numElements, element, context,
              cancelException, List.<E>insertAfterFunction()));
    }

    @Override
    public @NotNull List<E> insertAllAfter(final int numElements,
        @NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final long knownSize = materializer.knownSize();
      if (numElements < 0 || numElements == Integer.MAX_VALUE || (knownSize >= 0
          && numElements > knownSize)) {
        return cloneList(context, materializer);
      }
      final ListFutureMaterializer<E> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      if (knownSize == 0 && numElements == 0) {
        return cloneList(context, elementsMaterializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (numElements == 0) {
        if (materializer.isMaterializedAtOnce()) {
          return new List<E>(context, cancelException,
              lazyMaterializerPrependAll(materializer, context, cancelException, elements,
                  elementsMaterializer.knownSize()));
        }
        return new List<E>(context, cancelException,
            new PrependAllListFutureMaterializer<E>(materializer, elementsMaterializer, context,
                cancelException, List.<E>prependAllFunction()));
      } else if (numElements == knownSize) {
        if (materializer.isMaterializedAtOnce()) {
          return new List<E>(context, cancelException,
              lazyMaterializerAppendAll(materializer, context, cancelException, elements,
                  elementsMaterializer.knownSize()));
        }
        return new List<E>(context, cancelException,
            new AppendAllListFutureMaterializer<E>(materializer, elementsMaterializer, context,
                cancelException, List.<E>appendAllFunction()));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerInsertAllAfter(materializer, context, cancelException, numElements,
                elements, elementsMaterializer.knownSize()));
      }
      return new List<E>(context, cancelException,
          new InsertAllAfterListFutureMaterializer<E>(materializer, numElements,
              elementsMaterializer, context, cancelException, List.<E>insertAllAfterFunction()));
    }

    @Override
    public @NotNull List<E> intersect(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      if (getKnownSize(elements) == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerIntersect(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<E>(context, cancelException,
          new IntersectListFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public boolean isCancelled() {
      return materializer.isCancelled() || cancelException.get() != null;
    }

    @Override
    public boolean isDone() {
      return materializer.isDone() || cancelException.get() != null;
    }

    @Override
    public boolean isEmpty() {
      final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        final ListFutureMaterializer<E> materializer = this.materializer;
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeEmpty(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightEmpty();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeEmpty(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public boolean isFailed() {
      return materializer.isFailed();
    }

    @Override
    public boolean isSucceeded() {
      return materializer.isSucceeded();
    }

    @Override
    public @NotNull lazy.Iterator<E> iterator() {
      // TODO: future.Iterator
      return lazy.Iterator.wrap(this);
    }

    @Override
    public E last() {
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        throw new IndexOutOfBoundsException("-1");
      }
      final BlockingElementConsumer<E> consumer = new BlockingElementConsumer<E>(cancelException,
          -1);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(new FutureConsumer<java.util.List<E>>() {
          @Override
          public void accept(final java.util.List<E> elements) {
            final int index = elements.size() - 1;
            consumer.accept(-1, index, elements.get(index));
          }

          @Override
          public void error(@NotNull final Exception error) {
            consumer.error(error);
          }
        });
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            final ListFutureMaterializer<E> materializer = List.this.materializer;
            return (int) Math.min(Integer.MAX_VALUE,
                (long) materializer.weightSize() + materializer.weightElement());
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeSize(new FutureConsumer<Integer>() {
                @Override
                public void accept(final Integer size) {
                  if (size > 0) {
                    materializer.materializeElement(size - 1, consumer);
                  } else {
                    consumer.error(new IndexOutOfBoundsException("0"));
                  }
                }

                @Override
                public void error(@NotNull final Exception error) {
                  consumer.error(error);
                }
              });
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public int lastIndexOf(final Object o) {
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return -1;
      }
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(new FutureConsumer<java.util.List<E>>() {
          @Override
          @SuppressWarnings("SuspiciousMethodCalls")
          public void accept(final java.util.List<E> elements) {
            consumer.accept(elements.lastIndexOf(o));
          }

          @Override
          public void error(@NotNull final Exception error) {
            consumer.error(error);
          }
        });
      } else if (o == null) {
        context.scheduleAfter(new LastIndexOfNullFuturePredicate(knownSize, consumer));
      } else {
        context.scheduleAfter(new LastIndexOfElementFuturePredicate(o, knownSize, consumer));
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull ListIterator<E> listIterator() {
      return new ListIterator<E>(context, this);
    }

    @Override
    public @NotNull ListIterator<E> listIterator(final int index) {
      if (index < 0 || index == Integer.MAX_VALUE) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final int knownSize = materializer.knownSize();
      if (knownSize >= 0 && index > knownSize) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, this, index);
    }

    @Override
    public @NotNull <F> List<F> map(@NotNull final Function<? super E, F> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerMap(materializer, context, cancelException,
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new List<F>(context, cancelException, new MapListFutureMaterializer<E, F>(materializer,
          toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException));
    }

    @Override
    public @NotNull <F> List<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerMap(materializer, context, cancelException,
                Require.notNull(mapper, "mapper")));
      }
      return new List<F>(context, cancelException,
          new MapListFutureMaterializer<E, F>(materializer, Require.notNull(mapper, "mapper"),
              context, cancelException));
    }

    @Override
    public @NotNull List<E> mapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneList(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapAfter(materializer, context, cancelException, numElements,
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(context, cancelException,
          new MapAfterListFutureMaterializer<E>(materializer, numElements,
              toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneList(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapAfter(materializer, context, cancelException, numElements,
                Require.notNull(mapper, "mapper")));
      }
      return new List<E>(context, cancelException,
          new MapAfterListFutureMaterializer<E>(materializer, numElements,
              Require.notNull(mapper, "mapper"), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
      }
      return new List<E>(context, cancelException,
          new MapFirstWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper"), context,
              cancelException, List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate")),
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(context, cancelException,
          new MapFirstWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
      }
      return new List<E>(context, cancelException,
          new MapLastWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper"), context,
              cancelException, List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate")),
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(context, cancelException,
          new MapLastWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, context, cancelException,
                filteredMapper(Require.notNull(predicate, "predicate"),
                    Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(context, cancelException, new MapListFutureMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper")), context, cancelException));
    }

    @Override
    public @NotNull List<E> mapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, context, cancelException,
                filteredMapper(Require.notNull(predicate, "predicate"),
                    Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(context, cancelException, new MapListFutureMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper")), context, cancelException));
    }

    @Override
    public @NotNull List<E> max(@NotNull final Comparator<? super E> comparator) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMax(materializer, context, cancelException,
                Require.notNull(comparator, "comparator")));
      }
      return new List<E>(context, cancelException,
          new MaxListFutureMaterializer<E>(materializer, Require.notNull(comparator, "comparator"),
              context, cancelException));
    }

    @Override
    public @NotNull List<E> min(@NotNull final Comparator<? super E> comparator) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMax(materializer, context, cancelException,
                reversed(Require.notNull(comparator, "comparator"))));
      }
      return new List<E>(context, cancelException, new MaxListFutureMaterializer<E>(materializer,
          reversed(Require.notNull(comparator, "comparator")), context, cancelException));
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final Consumer<? super E> consumer) {
      return new ListForFuture<E>(context, taskID, cancelException, materializer,
          toIndexedConsumer(Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final IndexedConsumer<? super E> consumer) {
      return new ListForFuture<E>(context, taskID, cancelException, materializer,
          Require.notNull(consumer, "consumer"));
    }

    @Override
    public @NotNull Future<?> nonBlockingGet() {
      return new ListGetFuture<E>(context, taskID, cancelException, materializer);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new ListWhileFuture<E>(context, taskID, cancelException, materializer,
          Require.notNull(predicate, "predicate"));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      return new ListWhileFuture<E>(context, taskID, cancelException, materializer,
          Require.notNull(condition, "condition"), Require.notNull(consumer, "consumer"));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> predicate) {
      return new ListWhileFuture<E>(context, taskID, cancelException, materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      return new ListWhileFuture<E>(context, taskID, cancelException, materializer,
          toIndexedPredicate(Require.notNull(condition, "condition")),
          toIndexedConsumer(Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull List<Boolean> none(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerNone(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Boolean>(context, cancelException,
          new EachListFutureMaterializer<E>(materializer,
              negated(Require.notNull(predicate, "predicate")), true, context, cancelException));
    }

    @Override
    public @NotNull List<Boolean> none(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerNone(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Boolean>(context, cancelException,
          new EachListFutureMaterializer<E>(materializer,
              toNegatedIndexedPredicate(Require.notNull(predicate, "predicate")), true, context,
              cancelException));
    }

    @Override
    public @NotNull List<Boolean> notAll(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerNotExists(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListFutureMaterializer<E>(materializer,
              negated(Require.notNull(predicate, "predicate")), true, context, cancelException));
    }

    @Override
    public @NotNull List<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerNotExists(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListFutureMaterializer<E>(materializer,
              toNegatedIndexedPredicate(Require.notNull(predicate, "predicate")), true, context,
              cancelException));
    }

    @Override
    public @NotNull List<E> orElse(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return cloneList(context,
            getElementsMaterializer(context, Require.notNull(elements, "elements")));
      }
      if (knownSize > 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerOrElse(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<E>(context, cancelException, new OrElseListFutureMaterializer<E>(materializer,
          getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
          cancelException));
    }

    @Override
    public @NotNull List<E> orElseGet(
        @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize > 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (knownSize == 0) {
        return new List<E>(context, cancelException,
            new SuppliedMaterializer<E>(Require.notNull(supplier, "supplier"), context,
                cancelException));
      }
      return new List<E>(context, cancelException, new OrElseListFutureMaterializer<E>(materializer,
          new SuppliedMaterializer<E>(Require.notNull(supplier, "supplier"), context,
              cancelException), context, cancelException));
    }

    @Override
    public @NotNull List<E> plus(final E element) {
      return append(element);
    }

    @Override
    public @NotNull List<E> plusAll(@NotNull final Iterable<? extends E> elements) {
      return appendAll(elements);
    }

    @Override
    public @NotNull List<E> prepend(final E element) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final long knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return new List<E>(context, cancelException,
            new ElementToListFutureMaterializer<E>(element));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerPrepend(materializer, context, cancelException, element));
      }
      return new List<E>(context, cancelException,
          new PrependListFutureMaterializer<E>(materializer, element, context, cancelException,
              List.<E>prependFunction()));
    }

    @Override
    public @NotNull List<E> prependAll(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int elementsKnownSize = getKnownSize(elements);
      if (elementsKnownSize == 0) {
        return cloneList(context, materializer);
      }
      final long knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return cloneList(context,
            getElementsMaterializer(context, Require.notNull(elements, "elements")));
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerPrependAll(materializer, context, cancelException,
                Require.notNull(elements, "elements"), elementsKnownSize));
      }
      return new List<E>(context, cancelException,
          new PrependAllListFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException, List.<E>prependAllFunction()));
    }

    @Override
    public @NotNull List<E> reduce(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return reduceLeft(operation);
    }

    @Override
    public @NotNull List<E> reduceLeft(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerReduceLeft(materializer, context, cancelException,
                Require.notNull(operation, "operation")));
      }
      return new List<E>(context, cancelException,
          new ReduceLeftListFutureMaterializer<E>(materializer,
              Require.notNull(operation, "operation"), context, cancelException));
    }

    @Override
    public @NotNull List<E> reduceRight(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerReduceRight(materializer, context, cancelException,
                Require.notNull(operation, "operation")));
      }
      return new List<E>(context, cancelException,
          new ReduceRightListFutureMaterializer<E>(materializer,
              Require.notNull(operation, "operation"), context, cancelException));
    }

    @Override
    public @NotNull List<E> removeAfter(final int numElements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneList(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return cloneList(context, materializer);
      }
      if (numElements == 0 && knownSize == 1) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveAfter(materializer, context, cancelException, numElements));
      }
      return new List<E>(context, cancelException,
          new RemoveAfterListFutureMaterializer<E>(materializer, numElements, context,
              cancelException, List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeEach(final E element) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveWhere(materializer, context, cancelException,
                equalsElement(element)));
      }
      return new List<E>(context, cancelException,
          new RemoveWhereListFutureMaterializer<E>(materializer, equalsElement(element), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> removeFirst(final E element) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveFirstWhere(materializer, context, cancelException,
                equalsElement(element)));
      }
      return new List<E>(context, cancelException,
          new RemoveFirstWhereListFutureMaterializer<E>(materializer, equalsElement(element),
              context, cancelException, List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeFirstWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveFirstWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new RemoveFirstWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeFirstWhere(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveFirstWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException,
          new RemoveFirstWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeLast(final E element) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveLastWhere(materializer, context, cancelException,
                equalsElement(element)));
      }
      return new List<E>(context, cancelException,
          new RemoveLastWhereListFutureMaterializer<E>(materializer, equalsElement(element),
              context, cancelException, List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeLastWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveLastWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new RemoveLastWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeLastWhere(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveLastWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException,
          new RemoveLastWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeSlice(final int start, final int end) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (end >= 0 && start >= end) {
        return cloneList(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return cloneList(context, materializer);
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
          return cloneList(context, materializer);
        }
        final int knownLength = knownEnd - knownStart;
        if (knownLength == 1) {
          return removeAfter(knownStart);
        }
        if (knownLength == knownSize) {
          return emptyList(context);
        }
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveSlice(materializer, context, cancelException, start, end));
      }
      return new List<E>(context, cancelException,
          new RemoveSliceListFutureMaterializer<E>(materializer, start, end, context,
              cancelException));
    }

    @Override
    public @NotNull List<E> removeWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new RemoveWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException,
          new RemoveWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> replaceAfter(final int numElements, final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return cloneList(context, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapAfter(materializer, context, cancelException, numElements,
                replacementMapper(replacement)));
      }
      return new List<E>(context, cancelException,
          new MapAfterListFutureMaterializer<E>(materializer, numElements,
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceEach(final E element, final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, context, cancelException,
                filteredMapper(equalsElement(element), replacementMapper(replacement))));
      }
      return new List<E>(context, cancelException, new MapListFutureMaterializer<E, E>(materializer,
          filteredMapper(equalsElement(element), replacementMapper(replacement)), context,
          cancelException));
    }

    @Override
    public @NotNull List<E> replaceFirst(final E element, final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, context, cancelException,
                equalsElement(element), replacementMapper(replacement)));
      }
      return new List<E>(context, cancelException,
          new MapFirstWhereListFutureMaterializer<E>(materializer, equalsElement(element),
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate"), replacementMapper(replacement)));
      }
      return new List<E>(context, cancelException,
          new MapFirstWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), replacementMapper(replacement), context,
              cancelException, List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceFirstWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate")),
                replacementMapper(replacement)));
      }
      return new List<E>(context, cancelException,
          new MapFirstWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceLast(final E element, final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, context, cancelException,
                equalsElement(element), replacementMapper(replacement)));
      }
      return new List<E>(context, cancelException,
          new MapLastWhereListFutureMaterializer<E>(materializer, equalsElement(element),
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, context, cancelException,
                Require.notNull(predicate, "predicate"), replacementMapper(replacement)));
      }
      return new List<E>(context, cancelException,
          new MapLastWhereListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), replacementMapper(replacement), context,
              cancelException, List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceLastWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate")),
                replacementMapper(replacement)));
      }
      return new List<E>(context, cancelException,
          new MapLastWhereListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceSlice(final int start, final int end,
        @NotNull final Iterable<? extends E> patch) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize >= 0) {
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
          return insertAllAfter(knownStart, patch);
        }
        final int knownLength = knownEnd - knownStart;
        if (knownLength == knownSize) {
          return cloneList(context,
              getElementsMaterializer(context, Require.notNull(patch, "patch")));
        }
      }
      if (getKnownSize(patch) == 0) {
        return removeSlice(start, end);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(patch)) {
        return new List<E>(context, cancelException,
            lazyMaterializerReplaceSlice(materializer, context, cancelException, start, end,
                Require.notNull(patch, "patch")));
      }
      return new List<E>(context, cancelException,
          new ReplaceSliceListFutureMaterializer<E>(materializer, start, end,
              getElementsMaterializer(context, Require.notNull(patch, "patch")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> replaceWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, context, cancelException,
                filteredMapper(Require.notNull(predicate, "predicate"),
                    replacementMapper(replacement))));
      }
      return new List<E>(context, cancelException, new MapListFutureMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"), replacementMapper(replacement)),
          context, cancelException));
    }

    @Override
    public @NotNull List<E> replaceWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, context, cancelException,
                filteredMapper(Require.notNull(predicate, "predicate"),
                    replacementMapper(replacement))));
      }
      return new List<E>(context, cancelException, new MapListFutureMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"), replacementMapper(replacement)),
          context, cancelException));
    }

    @Override
    public @NotNull List<E> resizeTo(@NotNegative final int numElements, final E padding) {
      Require.notNegative(numElements, "numElements");
      if (numElements == 0) {
        return emptyList(context);
      }
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize >= 0) {
        if (knownSize == 0) {
          final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
          return new List<E>(context, cancelException,
              new ListToListFutureMaterializer<E>(lazy.List.times(numElements, padding), context));
        }
        if (knownSize == numElements) {
          return cloneList(context, materializer);
        }
        if (knownSize > numElements) {
          return take(numElements);
        }
        return appendAll(lazy.List.times(numElements - knownSize, padding));
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerResizeTo(materializer, context, cancelException, numElements, padding));
      }
      return new List<E>(context, cancelException,
          new ResizeListFutureMaterializer<E>(materializer, numElements, padding, context,
              cancelException, List.<E>resizeFunction()));
    }

    @Override
    public @NotNull List<E> reverse() {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerReverse(materializer, context, cancelException));
      }
      return new List<E>(context, cancelException,
          new ReverseListFutureMaterializer<E>(materializer, context, cancelException,
              List.<E>reverseFunction()));
    }

    @Override
    public int size() {
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        final ListFutureMaterializer<E> materializer = this.materializer;
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeSize(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightSize();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeSize(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull List<E> slice(final int start) {
      return slice(start, Integer.MAX_VALUE);
    }

    @Override
    public @NotNull List<E> slice(final int start, final int end) {
      if (end == Integer.MAX_VALUE && start >= 0) {
        return drop(start);
      }
      if (start == 0 && end >= 0) {
        return take(end);
      }
      if ((start == end) || (end >= 0 && start >= end)) {
        return emptyList(context);
      }
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return cloneList(context, materializer);
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
          return emptyList(context);
        }
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerSlice(materializer, context, cancelException, start, end));
      }
      return new List<E>(context, cancelException,
          new SliceListFutureMaterializer<E>(materializer, start, end, context, cancelException));
    }

    @Override
    public @NotNull List<? extends List<E>> slidingWindow(@Positive final int maxSize,
        @Positive final int step) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<List<E>>(context, cancelException,
          new SlidingWindowListFutureMaterializer<E, List<E>>(materializer,
              Require.positive(maxSize, "maxSize"), Require.positive(step, "step"),
              List.<E>getSplitter(context, taskID, cancelException), context, cancelException));
    }

    @Override
    public @NotNull List<? extends List<E>> slidingWindowWithPadding(@Positive final int size,
        @Positive final int step, final E padding) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      if (size == 1) {
        return slidingWindow(1, step);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<List<E>>(context, cancelException,
          new SlidingWindowListFutureMaterializer<E, List<E>>(materializer, size,
              Require.positive(step, "step"),
              getSplitter(context, taskID, cancelException, Require.positive(size, "size"),
                  padding), context, cancelException));
    }

    @Override
    public @NotNull List<E> sorted(@NotNull final Comparator<? super E> comparator) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerSorted(materializer, context, cancelException,
                Require.notNull(comparator, "comparator")));
      }
      return new List<E>(context, cancelException,
          new SortedListFutureMaterializer<E>(materializer, comparator, context, cancelException));
    }

    @Override
    public @NotNull List<Boolean> startsWith(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      if (getKnownSize(elements) == 0) {
        return trueList(context);
      }
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<Boolean>(context, cancelException,
          new StartsWithListFutureMaterializer<E>(materializer,
              Iterator.getElementsMaterializer(context, Require.notNull(elements, "elements")),
              context, cancelException));
    }

    // TODO: extra
    public @NotNull List<E> stopCancelPropagation() {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<E>(context, cancelException,
          new StopCancelListFutureMaterializer<E>(materializer));
    }

    // TODO: stopCancelPropagation + switchMap, mergeMap, concatMap(==flatMap) + flatMapAll(?)

    @Override
    public @NotNull List<E> symmetricDiff(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context,
            getElementsMaterializer(context, Require.notNull(elements, "elements")));
      }
      if (getKnownSize(elements) == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerSymmetricDiff(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<E>(context, cancelException,
          new SymmetricDiffListFutureMaterializer<E>(materializer,
              getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> take(final int maxElements) {
      if (maxElements <= 0) {
        return emptyList(context);
      }
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (maxElements == Integer.MAX_VALUE || materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTake(materializer, context, cancelException, maxElements));
      }
      return new List<E>(context, cancelException,
          new TakeListFutureMaterializer<E>(materializer, maxElements, context, cancelException));
    }

    @Override
    public @NotNull List<E> takeRight(final int maxElements) {
      if (maxElements <= 0) {
        return emptyList(context);
      }
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (maxElements == Integer.MAX_VALUE || materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeRight(materializer, context, cancelException, maxElements));
      }
      return new List<E>(context, cancelException,
          new TakeRightListFutureMaterializer<E>(materializer, maxElements, context,
              cancelException));
    }

    @Override
    public @NotNull List<E> takeRightWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeRightWhile(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new TakeRightWhileListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<E> takeRightWhile(@NotNull Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeRightWhile(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException,
          new TakeRightWhileListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    @Override
    public @NotNull List<E> takeWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeWhile(materializer, context, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<E>(context, cancelException,
          new TakeWhileListFutureMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException));
    }

    @Override
    public @NotNull List<E> takeWhile(@NotNull final Predicate<? super E> predicate) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeWhile(materializer, context, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<E>(context, cancelException,
          new TakeWhileListFutureMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context,
              cancelException));
    }

    // TODO: toFuture(context) ???
    // TODO: extra
    public @NotNull List<E> toContext(@NotNull final ExecutionContext context) {
      if (context.equals(this.context)) {
        return this;
      }
      return new List<E>(context, new AtomicReference<CancellationException>(),
          new SwitchListFutureMaterializer<E>(this.context, taskID, context, materializer));
    }

    // TODO: extra
    public @NotNull lazy.List<E> toLazy() {
      final BlockingConsumer<java.util.List<E>> consumer = new BlockingConsumer<java.util.List<E>>(
          cancelException);
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(consumer);
      } else {
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightElements();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeElements(consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return lazy.List.wrap(consumer.get());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public @NotNull List<E> union(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return cloneList(context,
            getElementsMaterializer(context, Require.notNull(elements, "elements")));
      }
      if (getKnownSize(elements) == 0) {
        return cloneList(context, materializer);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && isNotFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerUnion(materializer, context, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<E>(context, cancelException,
          new AppendAllListFutureMaterializer<E>(materializer, new DiffListFutureMaterializer<E>(
              getElementsMaterializer(context, Require.notNull(elements, "elements")), materializer,
              context, cancelException), context, cancelException, List.<E>appendAllFunction()));
    }

    private static abstract class LazyListFutureMaterializer<E, F> extends
        TransformListFutureMaterializer<E, F> {

      public LazyListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
          @NotNull final ExecutionContext context,
          @NotNull final AtomicReference<CancellationException> cancelException,
          final int knownSize) {
        super(wrapped, context, cancelException, knownSize);
      }

      @Override
      protected void materialize(@NotNull final java.util.List<F> list) {
        if (list instanceof lazy.List) {
          ((lazy.List<?>) list).materialized();
        }
      }
    }

    private static class SuppliedMaterializer<E> extends AbstractListFutureMaterializer<E> {

      private static final Logger LOGGER = Logger.getLogger(SuppliedMaterializer.class.getName());

      public SuppliedMaterializer(@NotNull final Supplier<? extends Iterable<? extends E>> supplier,
          @NotNull final ExecutionContext context,
          @NotNull final AtomicReference<CancellationException> cancelException) {
        super(context);
        setState(new ImmaterialState(supplier, context, cancelException));
      }

      @Override
      public int knownSize() {
        return -1;
      }

      private class ImmaterialState implements ListFutureMaterializer<E> {

        private final AtomicReference<CancellationException> cancelException;
        private final ExecutionContext context;
        private final Supplier<? extends Iterable<? extends E>> supplier;

        private ImmaterialState(@NotNull final Supplier<? extends Iterable<? extends E>> supplier,
            @NotNull final ExecutionContext context,
            @NotNull final AtomicReference<CancellationException> cancelException) {
          this.supplier = supplier;
          this.context = context;
          this.cancelException = cancelException;
        }

        @Override
        public boolean isCancelled() {
          return false;
        }

        @Override
        public boolean isDone() {
          return false;
        }

        @Override
        public boolean isFailed() {
          return false;
        }

        @Override
        public boolean isMaterializedAtOnce() {
          return false;
        }

        @Override
        public boolean isSucceeded() {
          return false;
        }

        @Override
        public int knownSize() {
          return -1;
        }

        @Override
        public void materializeCancel(@NotNull final CancellationException exception) {
          setCancelled(exception);
        }

        @Override
        public void materializeContains(final Object element,
            @NotNull final FutureConsumer<Boolean> consumer) {
          materialized().materializeContains(element, consumer);
        }

        @Override
        public void materializeElement(@NotNegative final int index,
            @NotNull final IndexedFutureConsumer<E> consumer) {
          materialized().materializeElement(index, consumer);
        }

        @Override
        public void materializeElements(@NotNull final FutureConsumer<java.util.List<E>> consumer) {
          final ListFutureMaterializer<E> state = materialized();
          state.materializeElements(new FutureConsumer<java.util.List<E>>() {
            @Override
            public void accept(final java.util.List<E> elements) throws Exception {
              setDone(state);
              consumer.accept(elements);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }

        @Override
        public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
          materialized().materializeEmpty(consumer);
        }

        @Override
        public void materializeHasElement(@NotNegative final int index,
            @NotNull final FutureConsumer<Boolean> consumer) {
          materialized().materializeHasElement(index, consumer);
        }

        @Override
        public void materializeNextWhile(@NotNegative final int index,
            @NotNull final IndexedFuturePredicate<E> predicate) {
          materialized().materializeNextWhile(index, predicate);
        }

        @Override
        public void materializePrevWhile(@NotNegative final int index,
            @NotNull final IndexedFuturePredicate<E> predicate) {
          materialized().materializePrevWhile(index, predicate);
        }

        @Override
        public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
          materialized().materializeSize(consumer);
        }

        @Override
        public int weightContains() {
          return weightElements();
        }

        @Override
        public int weightElement() {
          return weightElements();
        }

        @Override
        public int weightElements() {
          return 1;
        }

        @Override
        public int weightEmpty() {
          return weightElements();
        }

        @Override
        public int weightHasElement() {
          return weightElements();
        }

        @Override
        public int weightNextWhile() {
          return weightElements();
        }

        @Override
        public int weightPrevWhile() {
          return weightElements();
        }

        @Override
        public int weightSize() {
          return weightElements();
        }

        private @NotNull ListFutureMaterializer<E> materialized() {
          try {
            return getElementsMaterializer(context, supplier.get());
          } catch (final Exception e) {
            if (e instanceof InterruptedException) {
              Thread.currentThread().interrupt();
            }
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              return setCancelled(exception);
            } else {
              return setFailed(e);
            }
          }
        }
      }
    }

    private class IndexOfElementFuturePredicate extends ContextTask implements
        IndexedFuturePredicate<E> {

      private final FutureConsumer<Integer> consumer;
      private final Object element;

      private IndexOfElementFuturePredicate(@NotNull final Object element,
          @NotNull final FutureConsumer<Integer> consumer) {
        super(context);
        this.element = element;
        this.consumer = consumer;
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.accept(-1);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public boolean test(final int size, final int index, final E element) throws Exception {
        if (this.element.equals(element)) {
          consumer.accept(index);
          return false;
        }
        return true;
      }

      @Override
      public int weight() {
        return materializer.weightNextWhile();
      }

      @Override
      protected void runWithContext() {
        materializer.materializeNextWhile(0, this);
      }
    }

    private class IndexOfNullFuturePredicate extends ContextTask implements
        IndexedFuturePredicate<E> {

      private final FutureConsumer<Integer> consumer;

      private IndexOfNullFuturePredicate(@NotNull final FutureConsumer<Integer> consumer) {
        super(context);
        this.consumer = consumer;
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.accept(-1);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public boolean test(final int size, final int index, final E element) throws Exception {
        if (element == null) {
          consumer.accept(index);
          return false;
        }
        return true;
      }

      @Override
      public int weight() {
        return materializer.weightNextWhile();
      }

      @Override
      protected void runWithContext() {
        materializer.materializeNextWhile(0, this);
      }
    }

    private class LastIndexOfElementFuturePredicate extends ContextTask implements
        IndexedFuturePredicate<E> {

      private final FutureConsumer<Integer> consumer;
      private final Object element;
      private final int index;

      private LastIndexOfElementFuturePredicate(@NotNull final Object element, final int size,
          @NotNull final FutureConsumer<Integer> consumer) {
        super(context);
        this.element = element;
        this.consumer = consumer;
        this.index = size < 0 ? Integer.MAX_VALUE : size - 1;
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.accept(-1);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public boolean test(final int size, final int index, final E element) throws Exception {
        if (this.element.equals(element)) {
          consumer.accept(index);
          return false;
        }
        return true;
      }

      @Override
      public int weight() {
        return materializer.weightPrevWhile();
      }

      @Override
      protected void runWithContext() {
        materializer.materializePrevWhile(index, this);
      }
    }

    private class LastIndexOfNullFuturePredicate extends ContextTask implements
        IndexedFuturePredicate<E> {

      private final FutureConsumer<Integer> consumer;
      private final int index;

      private LastIndexOfNullFuturePredicate(final int size,
          @NotNull final FutureConsumer<Integer> consumer) {
        super(context);
        this.consumer = consumer;
        index = size < 0 ? Integer.MAX_VALUE : size - 1;
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.accept(-1);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public boolean test(final int size, final int index, final E element) throws Exception {
        if (element == null) {
          consumer.accept(index);
          return false;
        }
        return true;
      }

      @Override
      public int weight() {
        return materializer.weightPrevWhile();
      }

      @Override
      protected void runWithContext() {
        materializer.materializePrevWhile(index, this);
      }
    }
  }

  public static class ListIterator<E> implements itf.Future<E, java.util.ListIterator<E>>,
      itf.ListIterator<E> {

    private static final Function<? extends List<?>, ? extends ListIterator<?>> LIST_TO_ITERATOR = new Function<List<?>, ListIterator<?>>() {
      @Override
      public ListIterator<?> apply(final List<?> param) {
        return param.listIterator();
      }
    };

    private final ExecutionContext context;
    private final List<E> list;
    private final Object posMutex = new Object();

    private int pos;

    ListIterator(@NotNull final ExecutionContext context, @NotNull final List<E> list) {
      this.context = context;
      this.list = list;
    }

    ListIterator(@NotNull final ExecutionContext context, @NotNull final List<E> list,
        final int pos) {
      this(context, list);
      this.pos = pos;
    }

    @Override
    public void add(final E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T apply(@NotNull Function<? super itf.Sequence<E>, T> mapper) {
      return null;
    }

    @Override
    public @NotNull ListIterator<E> append(final E element) {
      return new ListIterator<E>(context, nextList().append(element));
    }

    @Override
    public @NotNull ListIterator<E> appendAll(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(context, nextList().appendAll(elements));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <F> ListIterator<F> as() {
      return (ListIterator<F>) this;
    }

    // TODO: extra
    public @NotNull lazy.ListIterator<E> asLazy() {
      final int pos = safePos();
      return list.asLazy().listIterator(pos);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      return list.cancel(mayInterruptIfRunning);
    }

    @Override
    public @NotNull ListIterator<Integer> count() {
      final int pos = safePos();
      if (atEnd(pos)) {
        return zeroIterator();
      }
      return new ListIterator<Integer>(context, nextList(pos).count());
    }

    @Override
    public @NotNull ListIterator<Integer> countWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return zeroIterator();
      }
      return new ListIterator<Integer>(context, nextList(pos).countWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> countWhere(
        @NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return zeroIterator();
      }
      return new ListIterator<Integer>(context, nextList(pos).countWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> diff(@NotNull final Iterable<?> elements) {
      return new ListIterator<E>(context, nextList().diff(elements));
    }

    @Override
    public @NotNull ListIterator<E> distinct() {
      return new ListIterator<E>(context, nextList().distinct());
    }

    @Override
    public @NotNull <K> ListIterator<E> distinctBy(
        @NotNull final Function<? super E, K> keyExtractor) {
      return new ListIterator<E>(context, nextList().distinctBy(keyExtractor));
    }

    @Override
    public @NotNull <K> ListIterator<E> distinctBy(
        @NotNull final IndexedFunction<? super E, K> keyExtractor) {
      return new ListIterator<E>(context, nextList().distinctBy(keyExtractor));
    }

    @Override
    public void doFor(@NotNull final Consumer<? super E> consumer) {
      final int pos = safePos();
      if (!atEnd(pos)) {
        nextList(pos).doFor(consumer);
      }
    }

    @Override
    public void doFor(@NotNull final IndexedConsumer<? super E> consumer) {
      final int pos = safePos();
      if (!atEnd(pos)) {
        nextList(pos).doFor(consumer);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (!atEnd(pos)) {
        nextList(pos).doWhile(predicate);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      final int pos = safePos();
      if (!atEnd(pos)) {
        nextList(pos).doWhile(condition, consumer);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (!atEnd(pos)) {
        nextList(pos).doWhile(predicate);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      final int pos = safePos();
      if (!atEnd(pos)) {
        nextList(pos).doWhile(condition, consumer);
      }
    }

    @Override
    public @NotNull ListIterator<E> drop(final int maxElements) {
      final int pos = safePos();
      if (maxElements <= 0 || atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).drop(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> dropRight(final int maxElements) {
      final int pos = safePos();
      if (maxElements <= 0 || atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).dropRight(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> dropRightWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).dropRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).dropRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> dropWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).dropWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).dropWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> each(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return falseIterator();
      }
      return new ListIterator<Boolean>(context, nextList(pos).each(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> each(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return falseIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, nextList(pos).each(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> endsWith(@NotNull final Iterable<?> elements) {
      return new ListIterator<Boolean>(context, nextList().endsWith(elements));
    }

    @Override
    public @NotNull ListIterator<Boolean> exists(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return falseIterator();
      }
      return new ListIterator<Boolean>(context, nextList(pos).exists(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return falseIterator();
      }
      return new ListIterator<Boolean>(context, nextList(pos).exists(predicate));
    }

    @Override
    public @NotNull ListIterator<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).filter(predicate));
    }

    @Override
    public @NotNull ListIterator<E> filter(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).filter(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findAny(@NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).findAny(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findAny(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).findAny(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findFirst(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).findFirst(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findFirst(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).findFirst(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexOf(final Object element) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<Integer>(context, nextList(pos).findIndexOf(element));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
      return new ListIterator<Integer>(context, nextList().findIndexOfSlice(elements));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<Integer>(context, nextList(pos).findIndexWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<Integer>(context, nextList(pos).findIndexWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).findLast(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findLast(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).findLast(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexOf(final Object element) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<Integer>(context, nextList(pos).findLastIndexOf(element));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexOfSlice(
        @NotNull final Iterable<?> elements) {
      return new ListIterator<Integer>(context, nextList().findLastIndexOfSlice(elements));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<Integer>(context, nextList(pos).findLastIndexWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<Integer>(context, nextList(pos).findLastIndexWhere(predicate));
    }

    @Override
    public E first() {
      return list.get(safePos());
    }

    @Override
    public @NotNull <F> ListIterator<F> flatMap(
        @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
      return new ListIterator<F>(context, nextList().flatMap(mapper));
    }

    @Override
    public @NotNull <F> ListIterator<F> flatMap(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      return new ListIterator<F>(context, nextList().flatMap(mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).flatMapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).flatMapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).flatMapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).flatMapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).flatMapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).flatMapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).flatMapWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).flatMapWhere(predicate, mapper));
    }

    @Override
    public @NotNull <F> ListIterator<F> fold(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return elementIterator(identity);
      }
      return new ListIterator<F>(context, nextList(pos).fold(identity, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldLeft(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return elementIterator(identity);
      }
      return new ListIterator<F>(context, nextList(pos).foldLeft(identity, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldLeftWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return elementIterator(identity);
      }
      return new ListIterator<F>(context,
          nextList(pos).foldLeftWhile(identity, predicate, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldRight(final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return elementIterator(identity);
      }
      return new ListIterator<F>(context, nextList(pos).foldRight(identity, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldRightWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return elementIterator(identity);
      }
      return new ListIterator<F>(context,
          nextList(pos).foldRightWhile(identity, predicate, operation));
    }

    @Override
    public java.util.ListIterator<E> get() throws InterruptedException, ExecutionException {
      return list.get().listIterator(pos);
    }

    @Override
    public java.util.ListIterator<E> get(final long timeout, @NotNull final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      return list.get(timeout, unit).listIterator(pos);
    }

    @Override
    public boolean hasNext() {
      final int pos = safePos();
      final AtomicReference<CancellationException> cancelException = list.cancelException;
      final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>(cancelException);
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = list.materializer;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeHasElement(pos, consumer);
      } else {
        final String taskID = list.taskID;
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightHasElement();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeHasElement(pos, consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public boolean hasPrevious() {
      return safePos() > 0;
    }

    @Override
    public @NotNull ListIterator<Boolean> includes(final Object element) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return falseIterator();
      }
      return new ListIterator<Boolean>(context, nextList(pos).includes(element));
    }

    @Override
    public @NotNull ListIterator<Boolean> includesAll(@NotNull final Iterable<?> elements) {
      return new ListIterator<Boolean>(context, nextList().includesAll(elements));
    }

    @Override
    public @NotNull ListIterator<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
      return new ListIterator<Boolean>(context, nextList().includesSlice(elements));
    }

    @Override
    public @NotNull ListIterator<E> insert(final E element) {
      return new ListIterator<E>(context, nextList().insertAfter(0, element));
    }

    @Override
    public @NotNull ListIterator<E> insertAfter(final int numElements, final E element) {
      final int pos = safePos();
      if (numElements != 0 && atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).insertAfter(numElements, element));
    }

    @Override
    public @NotNull ListIterator<E> insertAll(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(context, nextList().insertAllAfter(0, elements));
    }

    @Override
    public @NotNull ListIterator<E> insertAllAfter(final int numElements,
        @NotNull final Iterable<? extends E> elements) {
      final int pos = safePos();
      if (numElements != 0 && atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).insertAllAfter(numElements, elements));
    }

    @Override
    public @NotNull ListIterator<E> intersect(@NotNull final Iterable<?> elements) {
      return new ListIterator<E>(context, nextList().intersect(elements));
    }

    @Override
    public boolean isCancelled() {
      return list.isCancelled();
    }

    @Override
    public boolean isDone() {
      return list.isDone();
    }

    @Override
    public boolean isEmpty() {
      final int pos = safePos();
      return list.isEmpty() || pos >= list.size();
    }

    @Override
    public boolean isFailed() {
      return list.isFailed();
    }

    @Override
    public boolean isSucceeded() {
      return list.isSucceeded();
    }

    @Override
    public @NotNull lazy.Iterator<E> iterator() {
      // TODO: future.Iterator
      final int pos = safePos();
      if (atEnd(pos)) {
        return lazy.Iterator.of();
      }
      return nextList(pos).iterator();
    }

    @Override
    public E last() {
      return list.last();
    }

    @Override
    public @NotNull <F> ListIterator<F> map(@NotNull final Function<? super E, F> mapper) {
      return new ListIterator<F>(context, nextList().map(mapper));
    }

    @Override
    public @NotNull <F> ListIterator<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
      return new ListIterator<F>(context, nextList().map(mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).mapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).mapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).mapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).mapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).mapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).mapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new ListIterator<E>(context, nextList().mapWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      return new ListIterator<E>(context, nextList().mapWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> max(@NotNull final Comparator<? super E> comparator) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).max(comparator));
    }

    @Override
    public @NotNull ListIterator<E> min(@NotNull final Comparator<? super E> comparator) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).min(comparator));
    }

    @Override
    public @NotNull ListIterator<E> moveBy(final int maxElements) {
      final long pos = safePos();
      if (maxElements != 0) {
        final int knownSize = list.materializer.knownSize();
        synchronized (posMutex) {
          if (knownSize >= 0) {
            this.pos = (int) Math.min(knownSize, Math.max(0, pos + maxElements));
          } else {
            this.pos = (int) Math.min(Integer.MAX_VALUE, Math.max(0, pos + maxElements));
          }
        }
      }
      return this;
    }

    @Override
    public @NotNull ListIterator<E> moveTo(final int index) {
      Require.notNegative(index, "index");
      final int knownSize = list.materializer.knownSize();
      synchronized (posMutex) {
        if (knownSize >= 0) {
          pos = Math.min(knownSize, Math.max(0, index));
        } else {
          pos = Math.max(0, index);
        }
      }
      return this;
    }

    @Override
    public E next() {
      try {
        final int pos = safeGetAndIncPos();
        return list.get(pos);
      } catch (final IndexOutOfBoundsException ignored) {
        // FIXME: where the exception come from?
        throw new NoSuchElementException();
      }
    }

    @Override
    public int nextIndex() {
      final int pos = safePos();
      final List<E> list = this.list;
      final int knownSize = list.materializer.knownSize();
      if (knownSize >= 0) {
        return Math.min(knownSize, pos);
      }
      return Math.min(list.size(), pos);
    }

    @Override
    public @NotNull List<E> nextList() {
      return nextList(safePos());
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final Consumer<? super E> consumer) {
      return nextList().nonBlockingFor(consumer);
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final IndexedConsumer<? super E> consumer) {
      return nextList().nonBlockingFor(consumer);
    }

    @Override
    public @NotNull Future<?> nonBlockingGet() {
      return list.nonBlockingGet();
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      return nextList().nonBlockingWhile(predicate);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      return nextList().nonBlockingWhile(condition, consumer);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> predicate) {
      return nextList().nonBlockingWhile(predicate);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> condition,
        @NotNull Consumer<? super E> consumer) {
      return nextList().nonBlockingWhile(condition, consumer);
    }

    @Override
    public @NotNull ListIterator<Boolean> none(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return trueIterator();
      }
      return new ListIterator<Boolean>(context, nextList(pos).none(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> none(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return trueIterator();
      }
      return new ListIterator<Boolean>(context, nextList(pos).none(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> notAll(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return trueIterator();
      }
      return new ListIterator<Boolean>(context, nextList(pos).notAll(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return trueIterator();
      }
      return new ListIterator<Boolean>(context, nextList(pos).notAll(predicate));
    }

    @Override
    public boolean notEmpty() {
      return safePos() < list.size();
    }

    @Override
    public @NotNull ListIterator<E> orElse(@NotNull final Iterable<? extends E> elements) {
      final int pos = safePos();
      final ExecutionContext context = this.context;
      if (atEnd(pos)) {
        return new ListIterator<E>(context,
            new List<E>(context, new AtomicReference<CancellationException>(),
                List.getElementsMaterializer(context, Require.notNull(elements, "elements"))));
      }
      return new ListIterator<E>(context, nextList(pos).orElse(elements));
    }

    @Override
    public @NotNull ListIterator<E> orElseGet(
        @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
      return new ListIterator<E>(context, nextList().orElseGet(supplier));
    }

    @Override
    public @NotNull ListIterator<E> plus(final E element) {
      return new ListIterator<E>(context, nextList().plus(element));
    }

    @Override
    public @NotNull ListIterator<E> plusAll(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(context, nextList().plusAll(elements));
    }

    @Override
    public E previous() {
      final int pos = safeDecAndGetPos();
      if (pos < 0) {
        throw new NoSuchElementException();
      }
      final List<E> list = this.list;
      final AtomicReference<CancellationException> cancelException = list.cancelException;
      final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>(cancelException);
      final ExecutionContext context = this.context;
      final ListFutureMaterializer<E> materializer = list.materializer;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(new FutureConsumer<java.util.List<E>>() {
          @Override
          public void accept(final java.util.List<E> elements) {
            consumer.accept(pos < elements.size());
          }

          @Override
          public void error(@NotNull final Exception error) {
            consumer.error(error);
          }
        });
      } else {
        final String taskID = list.taskID;
        context.scheduleAfter(new ContextTask(context) {
          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightHasElement();
          }

          @Override
          protected void runWithContext() {
            try {
              materializer.materializeHasElement(pos, consumer);
            } catch (final Exception e) {
              consumer.error(e);
            }
          }
        });
      }
      try {
        int currPos = pos;
        if (!consumer.get()) {
          currPos = Math.min(list.size(), pos);
        }
        synchronized (posMutex) {
          this.pos = currPos - 1;
        }
        return list.get(currPos);
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      } catch (final IndexOutOfBoundsException ignored) {
        // FIXME: where the exception come from?
        throw new NoSuchElementException();
      }
    }

    @Override
    public int previousIndex() {
      return nextIndex() - 1;
    }

    @Override
    public @NotNull List<E> previousList() {
      return list.take(safePos());
    }

    @Override
    public @NotNull ListIterator<E> reduce(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).reduce(operation));
    }

    @Override
    public @NotNull ListIterator<E> reduceLeft(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).reduceLeft(operation));
    }

    @Override
    public @NotNull ListIterator<E> reduceRight(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).reduceRight(operation));
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ListIterator<E> removeAfter(final int numElements) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeAfter(numElements));
    }

    @Override
    public @NotNull ListIterator<E> removeEach(final E element) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeEach(element));
    }

    @Override
    public @NotNull ListIterator<E> removeFirst(final E element) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeFirst(element));
    }

    @Override
    public @NotNull ListIterator<E> removeFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeFirstWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeFirstWhere(
        @NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeFirstWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeLast(final E element) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeLast(element));
    }

    @Override
    public @NotNull ListIterator<E> removeLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeLastWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeLastWhere(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeLastWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeSlice(final int start, final int end) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeSlice(start, end));
    }

    @Override
    public @NotNull ListIterator<E> removeWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).removeWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> replaceAfter(final int numElements, final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceAfter(numElements, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceEach(final E element, final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceEach(element, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceFirst(final E element, final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceFirst(element, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceFirstWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceFirstWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceFirstWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceLast(final E element, final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceLast(element, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceLastWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceLastWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceLastWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceSlice(final int start, final int end,
        @NotNull final Iterable<? extends E> patch) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceSlice(start, end, patch));
    }

    @Override
    public @NotNull ListIterator<E> replaceWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).replaceWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> resizeTo(@NotNegative final int numElements, final E padding) {
      Require.notNegative(numElements, "numElements");
      final int pos = safePos();
      if (atEnd(pos)) {
        return appendAll(lazy.List.times(numElements, padding));
      }
      return new ListIterator<E>(context, nextList(pos).resizeTo(numElements, padding));
    }

    @Override
    public int skip(final int maxElements) {
      if (maxElements > 0) {
        final int currPos = safePos();
        final int newPos = (int) Math.min(list.size(), (long) currPos + maxElements);
        synchronized (posMutex) {
          return (pos = newPos) - currPos;
        }
      }
      return 0;
    }

    @Override
    public @NotNull ListIterator<E> reverse() {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).reverse());
    }

    @Override
    public void set(final E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
      final int pos = safePos();
      return Math.min(0, list.size() - pos);
    }

    @Override
    public @NotNull ListIterator<E> slice(final int start) {
      return slice(start, Integer.MAX_VALUE);
    }

    @Override
    public @NotNull ListIterator<E> slice(final int start, final int end) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).slice(start, end));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull ListIterator<? extends ListIterator<E>> slidingWindow(
        @Positive final int maxSize, @Positive final int step) {
      return new ListIterator<ListIterator<E>>(context, nextList().slidingWindow(maxSize, step)
          .map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull ListIterator<? extends ListIterator<E>> slidingWindowWithPadding(
        @Positive final int size, @Positive final int step, final E padding) {
      return new ListIterator<ListIterator<E>>(context,
          nextList().slidingWindowWithPadding(size, step, padding)
              .map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR));
    }

    @Override
    public @NotNull ListIterator<Boolean> startsWith(@NotNull final Iterable<?> elements) {
      return new ListIterator<Boolean>(context, nextList().startsWith(elements));
    }

    // TODO: extra
    public @NotNull ListIterator<E> stopCancelPropagation() {
      return new ListIterator<E>(context, nextList().stopCancelPropagation());
    }

    @Override
    public @NotNull ListIterator<E> symmetricDiff(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(context, nextList().symmetricDiff(elements));
    }

    @Override
    public @NotNull ListIterator<E> take(final int maxElements) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).take(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> takeRight(final int maxElements) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).takeRight(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> takeRightWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).takeRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> takeRightWhile(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).takeRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> takeWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).takeWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> takeWhile(@NotNull final Predicate<? super E> predicate) {
      final int pos = safePos();
      if (atEnd(pos)) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, nextList(pos).takeWhile(predicate));
    }

    // TODO: extra
    public @NotNull lazy.ListIterator<E> toLazy() {
      final int pos = safePos();
      return list.toLazy().listIterator(pos);
    }

    @Override
    public @NotNull ListIterator<E> union(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(context, nextList().union(elements));
    }

    private boolean atEnd(final int pos) {
      final int knownSize = list.materializer.knownSize();
      return knownSize >= 0 && pos >= knownSize;
    }

    private @NotNull <T> ListIterator<T> elementIterator(final T element) {
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new ListIterator<T>(context,
          new List<T>(context, cancelException, new ElementToListFutureMaterializer<T>(element)));
    }

    private @NotNull <T> ListIterator<T> emptyIterator() {
      final ExecutionContext context = this.context;
      return new ListIterator<T>(context, List.<T>emptyList(context));
    }

    private @NotNull ListIterator<Boolean> falseIterator() {
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new ListIterator<Boolean>(context,
          new List<Boolean>(context, cancelException, List.FALSE_MATERIALIZER));
    }

    private @NotNull List<E> nextList(final int pos) {
      return list.drop(pos);
    }

    private @NotNull ListIterator<Boolean> trueIterator() {
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new ListIterator<Boolean>(context,
          new List<Boolean>(context, cancelException, List.TRUE_MATERIALIZER));
    }

    private int safeDecAndGetPos() {
      synchronized (posMutex) {
        if (pos > 0) {
          return --pos;
        }
        return -1;
      }
    }

    private int safeGetAndIncPos() {
      synchronized (posMutex) {
        return pos++;
      }
    }

    private int safePos() {
      synchronized (posMutex) {
        return pos;
      }
    }

    private @NotNull ListIterator<Integer> zeroIterator() {
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new ListIterator<Integer>(context,
          new List<Integer>(context, cancelException, List.ZERO_MATERIALIZER));
    }
  }

  private static class BlockingConsumer<P> implements FutureConsumer<P> {

    private final AtomicReference<CancellationException> cancelException;

    private Exception error;
    private boolean isDone;
    private P param;

    private BlockingConsumer(
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.cancelException = cancelException;
    }

    @Override
    public void accept(final P param) {
      this.param = param;
      release();
    }

    @Override
    public void error(@NotNull final Exception error) {
      this.error = error;
      release();
    }

    private P get() throws InterruptedException {
      synchronized (cancelException) {
        while (!isDone) {
          cancelException.wait();
          final CancellationException exception = cancelException.get();
          if (exception != null) {
            throw (CancellationException) new CancellationException().initCause(exception);
          }
        }
      }
      final Exception error = this.error;
      if (error != null) {
        throw UncheckedException.throwUnchecked(error);
      }
      return param;
    }

    private P get(final long timeout, @NotNull final TimeUnit unit)
        throws InterruptedException, TimeoutException {
      final long startTimeMillis = System.currentTimeMillis();
      long timeoutMillis = unit.toMillis(timeout);
      synchronized (cancelException) {
        while (!isDone) {
          cancelException.wait(timeoutMillis);
          timeoutMillis -= System.currentTimeMillis() - startTimeMillis;
          final CancellationException exception = cancelException.get();
          if (exception != null) {
            throw (CancellationException) new CancellationException().initCause(exception);
          }
          if (timeoutMillis <= 0) {
            throw new TimeoutException();
          }
        }
      }
      final Exception error = this.error;
      if (error != null) {
        throw UncheckedException.throwUnchecked(error);
      }
      return param;
    }

    private void release() {
      synchronized (cancelException) {
        isDone = true;
        cancelException.notifyAll();
      }
    }
  }

  private static class BlockingElementConsumer<P> implements IndexedFutureConsumer<P> {

    private final AtomicReference<CancellationException> cancelException;
    private final int index;

    private Exception error;
    private boolean isDone;
    private P param;

    private BlockingElementConsumer(
        @NotNull final AtomicReference<CancellationException> cancelException, final int index) {
      this.cancelException = cancelException;
      this.index = index;
    }

    @Override
    public void accept(final int size, final int index, final P param) {
      this.param = param;
      release();
    }

    @Override
    public void complete(final int size) {
      this.error = new IndexOutOfBoundsException(Integer.toString(index));
      release();
    }

    @Override
    public void error(@NotNull final Exception error) {
      this.error = error;
      release();
    }

    private P get() throws InterruptedException {
      synchronized (cancelException) {
        while (!isDone) {
          cancelException.wait();
          final CancellationException exception = cancelException.get();
          if (exception != null) {
            throw (CancellationException) new CancellationException().initCause(exception);
          }
        }
      }
      final Exception error = this.error;
      if (error != null) {
        throw UncheckedException.throwUnchecked(error);
      }
      return param;
    }

    private void release() {
      synchronized (cancelException) {
        isDone = true;
        cancelException.notifyAll();
      }
    }
  }

  private static class BlockingNextElementConsumer<P> implements IndexedFutureConsumer<P> {

    private final AtomicReference<CancellationException> cancelException;

    private Exception error;
    private boolean isDone;
    private P param;

    private BlockingNextElementConsumer(
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.cancelException = cancelException;
    }

    @Override
    public void accept(final int size, final int index, final P param) {
      this.param = param;
      release();
    }

    @Override
    public void complete(final int size) {
      this.error = new NoSuchElementException();
      release();
    }

    @Override
    public void error(@NotNull final Exception error) {
      this.error = error;
      release();
    }

    private P get() throws InterruptedException {
      synchronized (cancelException) {
        while (!isDone) {
          cancelException.wait();
          final CancellationException exception = cancelException.get();
          if (exception != null) {
            throw (CancellationException) new CancellationException().initCause(exception);
          }
        }
      }
      final Exception error = this.error;
      if (error != null) {
        throw UncheckedException.throwUnchecked(error);
      }
      return param;
    }

    private void release() {
      synchronized (cancelException) {
        isDone = true;
        cancelException.notifyAll();
      }
    }
  }

  private static class BlockingLastElementConsumer<P> implements IndexedFuturePredicate<P> {

    private final AtomicReference<CancellationException> cancelException;

    private Exception error;
    private boolean hasElement;
    private boolean isDone;
    private P param;

    private BlockingLastElementConsumer(
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.cancelException = cancelException;
    }

    @Override
    public void complete(final int size) {
      if (!hasElement) {
        this.error = new NoSuchElementException();
      }
      release();
    }

    @Override
    public void error(@NotNull final Exception error) {
      this.error = error;
      release();
    }

    @Override
    public boolean test(final int size, final int index, final P param) {
      hasElement = true;
      this.param = param;
      return true;
    }

    private P get() throws InterruptedException {
      synchronized (cancelException) {
        while (!isDone) {
          cancelException.wait();
          final CancellationException exception = cancelException.get();
          if (exception != null) {
            throw (CancellationException) new CancellationException().initCause(exception);
          }
        }
      }
      final Exception error = this.error;
      if (error != null) {
        throw UncheckedException.throwUnchecked(error);
      }
      return param;
    }

    private void release() {
      synchronized (cancelException) {
        isDone = true;
        cancelException.notifyAll();
      }
    }
  }
}
