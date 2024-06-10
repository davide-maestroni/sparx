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

import static sparx.internal.future.AsyncConsumers.safeConsume;
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.iterator.CollectionToIteratorAsyncMaterializer;
import sparx.internal.future.iterator.ElementToIteratorAsyncMaterializer;
import sparx.internal.future.iterator.IteratorAsyncMaterializer;
import sparx.internal.future.iterator.IteratorToIteratorAsyncMaterializer;
import sparx.internal.future.iterator.ListAsyncMaterializerToIteratorAsyncMaterializer;
import sparx.internal.future.iterator.ListToIteratorAsyncMaterializer;
import sparx.internal.future.list.AllListAsyncMaterializer;
import sparx.internal.future.list.AppendAllListAsyncMaterializer;
import sparx.internal.future.list.AppendListAsyncMaterializer;
import sparx.internal.future.list.AsyncForFuture;
import sparx.internal.future.list.AsyncGetFuture;
import sparx.internal.future.list.AsyncWhileFuture;
import sparx.internal.future.list.CountListAsyncMaterializer;
import sparx.internal.future.list.CountWhereListAsyncMaterializer;
import sparx.internal.future.list.DiffListAsyncMaterializer;
import sparx.internal.future.list.DropListAsyncMaterializer;
import sparx.internal.future.list.DropRightListAsyncMaterializer;
import sparx.internal.future.list.DropRightWhileListAsyncMaterializer;
import sparx.internal.future.list.DropWhileListAsyncMaterializer;
import sparx.internal.future.list.ElementToListAsyncMaterializer;
import sparx.internal.future.list.EndsWithListAsyncMaterializer;
import sparx.internal.future.list.ExistsListAsyncMaterializer;
import sparx.internal.future.list.FilterListAsyncMaterializer;
import sparx.internal.future.list.FindFirstListAsyncMaterializer;
import sparx.internal.future.list.FindIndexListAsyncMaterializer;
import sparx.internal.future.list.FindIndexOfSliceListAsyncMaterializer;
import sparx.internal.future.list.FindLastIndexListAsyncMaterializer;
import sparx.internal.future.list.FindLastIndexOfSliceListAsyncMaterializer;
import sparx.internal.future.list.FindLastListAsyncMaterializer;
import sparx.internal.future.list.FlatMapAfterListAsyncMaterializer;
import sparx.internal.future.list.FlatMapFirstWhereListAsyncMaterializer;
import sparx.internal.future.list.FlatMapLastWhereListAsyncMaterializer;
import sparx.internal.future.list.FlatMapListAsyncMaterializer;
import sparx.internal.future.list.FlatMapWhereListAsyncMaterializer;
import sparx.internal.future.list.ListAsyncMaterializer;
import sparx.internal.future.list.ListToListAsyncMaterializer;
import sparx.internal.future.list.SwitchListAsyncMaterializer;
import sparx.internal.future.list.TransformListAsyncMaterializer;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.UncheckedException;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

class future extends Sparx {

  private future() {
  }

  // TODO: move to future.Iterator
  @SuppressWarnings("unchecked")
  private static @NotNull <E> IteratorAsyncMaterializer<E> getElementsMaterializer(
      @NotNull final ExecutionContext context, @NotNull final String taskID,
      @NotNull final Iterable<? extends E> elements) {
    if (elements instanceof List) {
      final List<E> list = (List<E>) elements;
      if (context == list.context && taskID.equals(list.taskID)) {
        return new ListAsyncMaterializerToIteratorAsyncMaterializer<E>(list.materializer);
      }
      return new ListAsyncMaterializerToIteratorAsyncMaterializer<E>(
          new SwitchListAsyncMaterializer<E>(list.context, list.taskID, context, taskID,
              list.materializer));
    }
    if (elements instanceof java.util.List) {
      // TODO: empty
      final java.util.List<E> list = (java.util.List<E>) elements;
      if (list.size() == 1) {
        return new ElementToIteratorAsyncMaterializer<E>(list.get(0));
      }
      return new ListToIteratorAsyncMaterializer<E>(list);
    }
    if (elements instanceof Collection) {
      return new CollectionToIteratorAsyncMaterializer<E>((Collection<E>) elements);
    }
    // TODO: future.Iterator
    if (elements instanceof java.util.Iterator) {
      return new IteratorToIteratorAsyncMaterializer<E>((java.util.Iterator<E>) elements);
    }
    return new IteratorToIteratorAsyncMaterializer<E>((java.util.Iterator<E>) elements.iterator());
  }

  private static boolean isFuture(final Iterable<?> elements) {
    // TODO: future.Iterator
    return elements instanceof List;
  }

  public static class List<E> extends AbstractListSequence<E> implements
      itf.Future<E, lazy.List<E>>, itf.List<E> {

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
    private static final Function<? extends java.util.List<?>, ? extends java.util.List<?>> DECORATE_FUNCTION = new Function<java.util.List<?>, java.util.List<?>>() {
      @Override
      public java.util.List<?> apply(java.util.List<?> param) {
        return lazy.List.wrap(param).materialized();
      }
    };
    private static final ElementToListAsyncMaterializer<Boolean> FALSE_MATERIALIZER = new ElementToListAsyncMaterializer<Boolean>(
        lazy.List.of(false));
    private static final ElementToListAsyncMaterializer<Boolean> TRUE_MATERIALIZER = new ElementToListAsyncMaterializer<Boolean>(
        lazy.List.of(true));
    private static final ElementToListAsyncMaterializer<Integer> ZERO_MATERIALIZER = new ElementToListAsyncMaterializer<Integer>(
        lazy.List.of(0));

    private static final Logger LOGGER = Logger.getLogger(List.class.getName());

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ListAsyncMaterializer<E> materializer;
    private final String taskID;

    List(@NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final ListAsyncMaterializer<E> materializer) {
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

    @SuppressWarnings("unchecked")
    private static @NotNull <E> Function<java.util.List<E>, java.util.List<E>> decorateFunction() {
      return (Function<java.util.List<E>, java.util.List<E>>) DECORATE_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> ListAsyncMaterializer<E> getElementsMaterializer(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final Iterable<? extends E> elements) {
      if (elements instanceof List) {
        final List<E> list = (List<E>) elements;
        if (context == list.context && taskID.equals(list.taskID)) {
          return list.materializer;
        }
        return new SwitchListAsyncMaterializer<E>(list.context, list.taskID, context, taskID,
            list.materializer);
      }
      if (elements instanceof lazy.List) {
        final lazy.List<E> materialized = ((lazy.List<E>) elements).materialized();
        final int size = materialized.size();
        if (size == 0) {
          return EmptyListAsyncMaterializer.instance();
        }
        if (size == 1) {
          return new ElementToListAsyncMaterializer<E>(materialized);
        }
        return new ListToListAsyncMaterializer<E>(materialized);
      }
      if (elements instanceof java.util.List) {
        final java.util.List<E> list = (java.util.List<E>) elements;
        final int size = list.size();
        if (size == 0) {
          return EmptyListAsyncMaterializer.instance();
        }
        if (size == 1) {
          return new ElementToListAsyncMaterializer<E>(lazy.List.wrap(list));
        }
        return new ListToListAsyncMaterializer<E>(lazy.List.wrap(list));
      }
      // TODO: future.Iterator
      final ArrayList<E> list = new ArrayList<E>();
      for (final E element : elements) {
        list.add(element);
      }
      final int size = list.size();
      if (size == 0) {
        return EmptyListAsyncMaterializer.instance();
      }
      if (size == 1) {
        return new ElementToListAsyncMaterializer<E>(lazy.List.wrap(list));
      }
      return new ListToListAsyncMaterializer<E>(list);
    }

    private static @NotNull <E, F> IndexedFunction<E, IteratorAsyncMaterializer<F>> getElementToIteratorMaterializer(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final Function<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, IteratorAsyncMaterializer<F>>() {
        @Override
        public IteratorAsyncMaterializer<F> apply(final int index, final E element)
            throws Exception {
          return future.getElementsMaterializer(context, taskID, mapper.apply(element));
        }
      };
    }

    private static @NotNull <E, F> IndexedFunction<E, IteratorAsyncMaterializer<F>> getElementToIteratorMaterializer(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, IteratorAsyncMaterializer<F>>() {
        @Override
        public IteratorAsyncMaterializer<F> apply(final int index, final E element)
            throws Exception {
          return future.getElementsMaterializer(context, taskID, mapper.apply(index, element));
        }
      };
    }

    private static @NotNull <E> IndexedFunction<E, ListAsyncMaterializer<E>> getElementToMaterializer(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<E, ListAsyncMaterializer<E>>() {
        @Override
        public ListAsyncMaterializer<E> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(context, taskID, mapper.apply(element));
        }
      };
    }

    private static @NotNull <E> IndexedFunction<E, ListAsyncMaterializer<E>> getElementToMaterializer(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<E, ListAsyncMaterializer<E>>() {
        @Override
        public ListAsyncMaterializer<E> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(context, taskID, mapper.apply(index, element));
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Boolean> lazyMaterializerAll(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, Boolean>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).all(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerAppend(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException, final E element) {
      final int knownSize = materializer.knownSize();
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize > 0 ? SizeOverflowException.safeCast(knownSize + 1) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).append(element);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerAppendAll(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements, final int elementsKnownSize) {
      final int knownSize = materializer.knownSize();
      final Iterable<? extends E> appended = elements;
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize > 0 && elementsKnownSize > 0 ? SizeOverflowException.safeCast(
              knownSize + elementsKnownSize) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).appendAll(appended);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Integer> lazyMaterializerCount(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      return new LazyListAsyncMaterializer<E, Integer>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).count();
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Integer> lazyMaterializerCount(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, Integer>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).count(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerDiff(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      final Iterable<?> otherElements = elements;
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).diff(otherElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerDrop(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize > 0 ? Math.max(0, knownSize - maxElements) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).drop(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerDropRight(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize > 0 ? Math.max(0, knownSize - maxElements) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).dropRight(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerDropRightWhile(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).dropRightWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerDropWhile(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).dropWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Boolean> lazyMaterializerEndsWith(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      final Iterable<?> otherElements = elements;
      return new LazyListAsyncMaterializer<E, Boolean>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).endsWith(otherElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Boolean> lazyMaterializerExists(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, Boolean>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).exists(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerFilter(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).filter(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerFindFirst(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).findFirst(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Integer> lazyMaterializerFindIndexWhere(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, Integer>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).findIndexWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Integer> lazyMaterializerFindIndexOfSlice(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      final Iterable<?> otherElements = elements;
      return new LazyListAsyncMaterializer<E, Integer>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).findIndexOfSlice(otherElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerFindLast(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).findLast(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Integer> lazyMaterializerFindLastIndexWhere(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, Integer>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).findLastIndexWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Integer> lazyMaterializerFindLastIndexOfSlice(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      final Iterable<?> otherElements = elements;
      return new LazyListAsyncMaterializer<E, Integer>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(@NotNull java.util.List<E> elements) {
          return ((lazy.List<E>) elements).findLastIndexOfSlice(otherElements);
        }
      };
    }

    @Override
    public @NotNull List<Boolean> all(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Boolean>(context, cancelException, TRUE_MATERIALIZER);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerAll(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<Boolean>(context, cancelException,
          new AllListAsyncMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              context, cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> all(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Boolean>(context, cancelException, TRUE_MATERIALIZER);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerAll(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<Boolean>(context, cancelException,
          new AllListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<E> append(final E element) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final long knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return new List<E>(context, cancelException,
            new ElementToListAsyncMaterializer<E>(lazy.List.of(element)));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerAppend(materializer, cancelException, element));
      }
      return new List<E>(context, cancelException,
          new AppendListAsyncMaterializer<E>(materializer, element, cancelException,
              List.<E>appendFunction()));
    }

    @Override
    public @NotNull List<E> appendAll(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final ListAsyncMaterializer<E> elementsMaterializer = getElementsMaterializer(context, taskID,
          Require.notNull(elements, "elements"));
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final int elementsKnownSize = elementsMaterializer.knownSize();
      if (elementsKnownSize == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      final long knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return new List<E>(context, cancelException, elementsMaterializer);
      }
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerAppendAll(materializer, cancelException, elements, elementsKnownSize));
      }
      return new List<E>(context, cancelException,
          new AppendAllListAsyncMaterializer<E>(materializer, elementsMaterializer, cancelException,
              List.<E>appendAllFunction()));
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

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      if (!materializer.isDone() && cancelException.compareAndSet(null,
          new CancellationException())) {
        if (mayInterruptIfRunning) {
          context.interruptTask(taskID);
        }
        context.scheduleBefore(new Task() {
          @Override
          public void run() {
            try {
              materializer.materializeCancel(cancelException.get());
            } catch (final Exception e) {
              LOGGER.log(Level.SEVERE, "Ignored exception", e);
            }
          }

          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return 1;
          }
        });
        return true;
      }
      return false;
    }

    @Override
    public boolean contains(final Object o) {
      if (materializer.knownSize() == 0) {
        return false;
      }
      final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>();
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          try {
            materializer.materializeContains(o, consumer);
          } catch (final Exception e) {
            consumer.error(e);
          }
        }

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightContains();
        }
      });
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull List<Integer> count() {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return new List<Integer>(context, cancelException, ZERO_MATERIALIZER);
      }
      if (knownSize > 0) {
        return new List<Integer>(context, cancelException,
            new ElementToListAsyncMaterializer<Integer>(lazy.List.of(knownSize)));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerCount(materializer, cancelException));
      }
      return new List<Integer>(context, cancelException,
          new CountListAsyncMaterializer<E>(materializer, cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> count(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Integer>(context, cancelException, ZERO_MATERIALIZER);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerCount(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<Integer>(context, cancelException,
          new CountWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> count(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Integer>(context, cancelException, ZERO_MATERIALIZER);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerCount(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<Integer>(context, cancelException,
          new CountWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<E> diff(@NotNull final Iterable<?> elements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<?> elementsMaterializer = getElementsMaterializer(context, taskID,
          Require.notNull(elements, "elements"));
      if (elementsMaterializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerDiff(materializer, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<E>(context, cancelException,
          new DiffListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public void doFor(@NotNull final Consumer<? super E> consumer) {
      if (materializer.knownSize() == 0) {
        return;
      }
      try {
        nonBlockingFor(consumer).get();
      } catch (final ExecutionException e) {
        throw UncheckedException.toUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.toUnchecked(e);
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
        throw UncheckedException.toUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.toUnchecked(e);
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
        throw UncheckedException.toUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.toUnchecked(e);
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
        throw UncheckedException.toUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.toUnchecked(e);
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
        throw UncheckedException.toUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.toUnchecked(e);
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
        throw UncheckedException.toUnchecked(e.getCause());
      } catch (final Exception e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull List<E> drop(final int maxElements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
        return new List<E>(context, cancelException, EmptyListAsyncMaterializer.<E>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDrop(materializer, cancelException, maxElements));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new DropListAsyncMaterializer<E>(materializer, maxElements, context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> dropRight(final int maxElements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
        return new List<E>(context, cancelException, EmptyListAsyncMaterializer.<E>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropRight(materializer, cancelException, maxElements));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new DropRightListAsyncMaterializer<E>(materializer, maxElements, context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> dropRightWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropRightWhile(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new DropRightWhileListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropRightWhile(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new DropRightWhileListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> dropWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropWhile(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new DropWhileListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerDropWhile(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new DropWhileListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> endsWith(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<Object> elementsMaterializer = getElementsMaterializer(context,
          taskID, Require.notNull(elements, "elements"));
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (elementsMaterializer.knownSize() == 0) {
        return new List<Boolean>(context, cancelException, TRUE_MATERIALIZER);
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return new List<Boolean>(context, cancelException, FALSE_MATERIALIZER);
      }
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerEndsWith(materializer, cancelException, elements));
      }
      return new List<Boolean>(context, cancelException,
          new EndsWithListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
              cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> exists(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Boolean>(context, cancelException, FALSE_MATERIALIZER);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<Boolean>(context, cancelException,
          new ExistsListAsyncMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              context, cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Boolean>(context, cancelException, FALSE_MATERIALIZER);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<Boolean>(context, cancelException,
          new ExistsListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFilter(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FilterListAsyncMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              context, cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> filter(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFilter(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException, new FilterListAsyncMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
          List.<E>decorateFunction()));
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
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFindFirst(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FindFirstListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> findFirst(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFindFirst(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FindFirstListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findIndexOf(final Object element) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Integer>(context, cancelException,
            EmptyListAsyncMaterializer.<Integer>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, cancelException, equalsElement(element)));
      }
      final ExecutionContext context = this.context;
      return new List<Integer>(context, cancelException,
          new FindIndexListAsyncMaterializer<E>(materializer, equalsElement(element), context,
              cancelException, List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final ListAsyncMaterializer<Object> elementsMaterializer = getElementsMaterializer(context,
          taskID, Require.notNull(elements, "elements"));
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (elementsMaterializer.knownSize() == 0) {
        return new List<Integer>(context, cancelException, ZERO_MATERIALIZER);
      }
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexOfSlice(materializer, cancelException, elements));
      }
      return new List<Integer>(context, cancelException,
          new FindIndexOfSliceListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
              cancelException, List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Integer>(context, cancelException,
            EmptyListAsyncMaterializer.<Integer>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<Integer>(context, cancelException,
          new FindIndexListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findIndexWhere(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Integer>(context, cancelException,
            EmptyListAsyncMaterializer.<Integer>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<Integer>(context, cancelException,
          new FindIndexListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, EmptyListAsyncMaterializer.<E>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFindLast(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FindLastListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> findLast(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, EmptyListAsyncMaterializer.<E>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerFindLast(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FindLastListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findLastIndexOf(final Object element) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Integer>(context, cancelException,
            EmptyListAsyncMaterializer.<Integer>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, cancelException,
                equalsElement(element)));
      }
      final ExecutionContext context = this.context;
      return new List<Integer>(context, cancelException,
          new FindLastIndexListAsyncMaterializer<E>(materializer, equalsElement(element), context,
              cancelException, List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final ListAsyncMaterializer<Object> elementsMaterializer = getElementsMaterializer(context,
          taskID, Require.notNull(elements, "elements"));
      if (elementsMaterializer.knownSize() == 0) {
        final int knownSize = materializer.knownSize();
        if (knownSize >= 0) {
          return new List<Integer>(context, cancelException,
              new ElementToListAsyncMaterializer<Integer>(lazy.List.of(knownSize)));
        }
      }
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexOfSlice(materializer, cancelException, elements));
      }
      return new List<Integer>(context, cancelException,
          new FindLastIndexOfSliceListAsyncMaterializer<E>(materializer, elementsMaterializer,
              context, cancelException, List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findLastIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Integer>(context, cancelException,
            EmptyListAsyncMaterializer.<Integer>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<Integer>(context, cancelException,
          new FindLastIndexListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findLastIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<Integer>(context, cancelException,
            EmptyListAsyncMaterializer.<Integer>instance());
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<Integer>(context, cancelException,
          new FindLastIndexListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public E first() {
      if (materializer.knownSize() == 0) {
        throw new IndexOutOfBoundsException("0");
      }
      final BlockingElementConsumer<E> consumer = new BlockingElementConsumer<E>(0);
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          try {
            materializer.materializeElement(0, consumer);
          } catch (final Exception e) {
            consumer.error(e);
          }
        }

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightElement();
        }
      });
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull <F> List<F> flatMap(
        @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<F>(context, cancelException, EmptyListAsyncMaterializer.<F>instance());
      }
      final ExecutionContext context = this.context;
      return new List<F>(context, cancelException,
          new FlatMapListAsyncMaterializer<E, F>(materializer,
              getElementToIteratorMaterializer(context, taskID, Require.notNull(mapper, "mapper")),
              context, cancelException, List.<F>decorateFunction()));
    }

    @Override
    public @NotNull <F> List<F> flatMap(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<F>(context, cancelException, EmptyListAsyncMaterializer.<F>instance());
      }
      final ExecutionContext context = this.context;
      return new List<F>(context, cancelException,
          new FlatMapListAsyncMaterializer<E, F>(materializer,
              getElementToIteratorMaterializer(context, taskID, Require.notNull(mapper, "mapper")),
              context, cancelException, List.<F>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return new List<E>(context, cancelException, materializer);
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FlatMapAfterListAsyncMaterializer<E>(materializer, numElements,
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return new List<E>(context, cancelException, materializer);
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FlatMapAfterListAsyncMaterializer<E>(materializer, numElements,
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      return new List<E>(context, cancelException,
          new FlatMapFirstWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      return new List<E>(context, cancelException,
          new FlatMapFirstWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      return new List<E>(context, cancelException,
          new FlatMapLastWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      return new List<E>(context, cancelException,
          new FlatMapLastWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      return new List<E>(context, cancelException,
          new FlatMapWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToIteratorMaterializer(context, taskID, Require.notNull(mapper, "mapper")),
              context, cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      return new List<E>(context, cancelException,
          new FlatMapWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToIteratorMaterializer(context, taskID, Require.notNull(mapper, "mapper")),
              context, cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull <F> List<F> fold(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return foldLeft(identity, operation);
    }

    @Override
    public @NotNull <F> List<F> foldLeft(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return null;
    }

    @Override
    public @NotNull <F> List<F> foldRight(final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      return null;
    }

    @Override
    public lazy.List<E> get() throws InterruptedException, ExecutionException {
      if (materializer.knownSize() == 0) {
        return lazy.List.of();
      }
      final BlockingConsumer<java.util.List<E>> consumer = new BlockingConsumer<java.util.List<E>>();
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          try {
            materializer.materializeDone(consumer);
          } catch (final Exception e) {
            consumer.error(e);
          }
        }

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightElements();
        }
      });
      try {
        return (lazy.List<E>) consumer.get();
      } catch (final InterruptedException e) {
        throw e;
      } catch (final Exception e) {
        if (isCancelled() && e instanceof CancellationException) {
          throw (CancellationException) e;
        }
        throw new ExecutionException(e);
      }
    }

    @Override
    public E get(final int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final BlockingElementConsumer<E> consumer = new BlockingElementConsumer<E>(index);
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          try {
            materializer.materializeElement(index, consumer);
          } catch (final Exception e) {
            consumer.error(e);
          }
        }

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightElement();
        }
      });
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public lazy.List<E> get(final long timeout, @NotNull final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      if (materializer.knownSize() == 0) {
        return lazy.List.of();
      }
      final BlockingConsumer<java.util.List<E>> consumer = new BlockingConsumer<java.util.List<E>>();
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          try {
            materializer.materializeDone(consumer);
          } catch (final Exception e) {
            consumer.error(e);
          }
        }

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightElements();
        }
      });
      try {
        return lazy.List.wrap(consumer.get(timeout, unit));
      } catch (final InterruptedException e) {
        throw e;
      } catch (final Exception e) {
        if (isCancelled() && e instanceof CancellationException) {
          throw (CancellationException) e;
        }
        throw new ExecutionException(e);
      }
    }

    @Override
    public @NotNull List<? extends List<E>> group(int maxSize) {
      // TODO: implement slice
      return null;
    }

    @Override
    public @NotNull List<? extends List<E>> group(int size, E padding) {
      return null;
    }

    @Override
    public @NotNull List<Boolean> includes(Object element) {
      return null;
    }

    @Override
    public @NotNull List<Boolean> includesAll(@NotNull Iterable<?> elements) {
      return null;
    }

    @Override
    public @NotNull List<Boolean> includesSlice(@NotNull Iterable<?> elements) {
      return null;
    }

    @Override
    public int indexOf(final Object o) {
      if (materializer.knownSize() == 0) {
        return -1;
      }
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>();
      if (o == null) {
        context.scheduleAfter(new IndexOfNullAsyncConsumer(consumer));
      } else {
        context.scheduleAfter(new IndexOfElementAsyncConsumer(o, consumer));
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull List<E> insertAfter(int numElements, E element) {
      return null;
    }

    @Override
    public @NotNull List<E> insertAllAfter(int numElements,
        @NotNull Iterable<? extends E> elements) {
      return null;
    }

    @Override
    public @NotNull List<E> intersect(@NotNull Iterable<?> elements) {
      return null;
    }

    @Override
    public boolean isCancelled() {
      return materializer.isCancelled() || cancelException.get() != null;
    }

    @Override
    public boolean isDone() {
      return materializer.isDone();
    }

    @Override
    public boolean isFailed() {
      return materializer.isFailed();
    }

    @Override
    public boolean isEmpty() {
      final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>();
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          try {
            materializer.materializeEmpty(consumer);
          } catch (final Exception e) {
            consumer.error(e);
          }
        }

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightEmpty();
        }
      });
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull lazy.Iterator<E> iterator() {
      // TODO: future.Iterator
      return lazy.Iterator.wrap(this);
    }

    @Override
    public E last() {
      if (materializer.knownSize() == 0) {
        throw new IndexOutOfBoundsException("-1");
      }
      final BlockingElementConsumer<E> consumer = new BlockingElementConsumer<E>(-1);
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          try {
            materializer.materializeSize(new AsyncConsumer<Integer>() {
              @Override
              public void accept(final Integer size) {
                materializer.materializeElement(size - 1, consumer);
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

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          final ListAsyncMaterializer<E> materializer = List.this.materializer;
          return (int) Math.min(Integer.MAX_VALUE,
              (long) materializer.weightSize() + materializer.weightElement());
        }
      });
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public int lastIndexOf(final Object o) {
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return -1;
      }
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>();
      if (knownSize > 0) {
        if (o == null) {
          context.scheduleAfter(new LastIndexOfNullAsyncConsumer(knownSize, consumer));
        } else {
          context.scheduleAfter(new LastIndexOfElementAsyncConsumer(o, knownSize, consumer));
        }
      } else {
        context.scheduleAfter(new Task() {
          @Override
          public void run() {
            materializer.materializeSize(new AsyncConsumer<Integer>() {
              @Override
              public void accept(final Integer size) {
                if (o == null) {
                  context.scheduleAfter(new LastIndexOfNullAsyncConsumer(size, consumer));
                } else {
                  context.scheduleAfter(new LastIndexOfElementAsyncConsumer(o, size, consumer));
                }
              }

              @Override
              public void error(@NotNull final Exception error) {
                consumer.error(error);
              }
            });
          }

          @Override
          public @NotNull String taskID() {
            return taskID;
          }

          @Override
          public int weight() {
            return materializer.weightSize();
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
    public @NotNull lazy.ListIterator<E> listIterator() {
      // TODO: future.ListIterator
      return lazy.ListIterator.wrap(this);
    }

    @Override
    public @NotNull lazy.ListIterator<E> listIterator(final int index) {
      // TODO: future.ListIterator
      return lazy.ListIterator.wrap(this);
    }

    @Override
    public @NotNull <F> List<F> map(@NotNull Function<? super E, F> mapper) {
      return null;
    }

    @Override
    public @NotNull <F> List<F> map(@NotNull IndexedFunction<? super E, F> mapper) {
      return null;
    }

    @Override
    public @NotNull List<E> mapAfter(int numElements,
        @NotNull Function<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull List<E> mapAfter(int numElements,
        @NotNull IndexedFunction<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull List<E> mapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull List<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull List<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull List<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull List<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull List<E> mapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper) {
      return null;
    }

    @Override
    public @NotNull List<E> max(@NotNull Comparator<? super E> comparator) {
      return null;
    }

    @Override
    public @NotNull List<E> min(@NotNull Comparator<? super E> comparator) {
      return null;
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final Consumer<? super E> consumer) {
      return new AsyncForFuture<E>(context, taskID, materializer,
          toIndexedConsumer(Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final IndexedConsumer<? super E> consumer) {
      return new AsyncForFuture<E>(context, taskID, materializer,
          Require.notNull(consumer, "consumer"));
    }

    @Override
    public @NotNull Future<?> nonBlockingGet() {
      return new AsyncGetFuture<E>(context, taskID, materializer);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new AsyncWhileFuture<E>(context, taskID, materializer,
          Require.notNull(predicate, "predicate"));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      return new AsyncWhileFuture<E>(context, taskID, materializer,
          Require.notNull(condition, "condition"), Require.notNull(consumer, "consumer"));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> predicate) {
      return new AsyncWhileFuture<E>(context, taskID, materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      return new AsyncWhileFuture<E>(context, taskID, materializer,
          toIndexedPredicate(Require.notNull(condition, "condition")),
          toIndexedConsumer(Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull List<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<Boolean> notAll(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<Boolean> notExists(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<Boolean> notExists(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> orElse(@NotNull Iterable<E> elements) {
      return null;
    }

    @Override
    public @NotNull List<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier) {
      return null;
    }

    @Override
    public @NotNull List<E> plus(E element) {
      return null;
    }

    @Override
    public @NotNull List<E> plusAll(@NotNull Iterable<E> elements) {
      return null;
    }

    @Override
    public @NotNull List<E> prepend(E element) {
      return null;
    }

    @Override
    public @NotNull List<E> prependAll(@NotNull Iterable<? extends E> elements) {
      return null;
    }

    @Override
    public @NotNull List<E> reduce(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return null;
    }

    @Override
    public @NotNull List<E> reduceLeft(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return null;
    }

    @Override
    public @NotNull List<E> reduceRight(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return null;
    }

    @Override
    public @NotNull List<E> removeAfter(int numElements) {
      return null;
    }

    @Override
    public @NotNull List<E> removeEach(E element) {
      return null;
    }

    @Override
    public @NotNull List<E> removeFirst(E element) {
      return null;
    }

    @Override
    public @NotNull List<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> removeFirstWhere(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> removeLast(E element) {
      return null;
    }

    @Override
    public @NotNull List<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> removeLastWhere(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> removeSlice(int start, int end) {
      return null;
    }

    @Override
    public @NotNull List<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> removeWhere(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceAfter(int numElements, E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceEach(E element, E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceFirst(E element, E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceLast(E element, E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceLastWhere(@NotNull Predicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate,
        E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement) {
      return null;
    }

    @Override
    public @NotNull List<E> resizeTo(int numElements, E padding) {
      return null;
    }

    @Override
    public @NotNull List<E> reverse() {
      return null;
    }

    @Override
    public int size() {
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>();
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          try {
            materializer.materializeSize(consumer);
          } catch (final Exception e) {
            consumer.error(e);
          }
        }

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightSize();
        }
      });
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull List<E> slice(int start, int end) {
      return null;
    }

    @Override
    public @NotNull List<Boolean> startsWith(@NotNull Iterable<?> elements) {
      return null;
    }

    // TODO: stopCancelPropagation + switchMap, mergeMap, concatMap(==flatMap) + flatMapAll(?)

    @Override
    public @NotNull List<E> sorted(@NotNull Comparator<? super E> comparator) {
      return null;
    }

    @Override
    public @NotNull List<E> take(int maxElements) {
      return null;
    }

    @Override
    public @NotNull List<E> takeRight(int maxElements) {
      return null;
    }

    @Override
    public @NotNull List<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> takeRightWhile(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> takeWhile(@NotNull Predicate<? super E> predicate) {
      return null;
    }

    @Override
    public @NotNull List<E> union(@NotNull Iterable<? extends E> elements) {
      return null;
    }

    private static abstract class LazyListAsyncMaterializer<E, F> extends
        TransformListAsyncMaterializer<E, F> {

      public LazyListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
          @NotNull final AtomicReference<CancellationException> cancelException,
          final int knownSize) {
        super(wrapped, cancelException, knownSize);
      }

      @Override
      @SuppressWarnings("unchecked")
      protected int knownSize(@NotNull java.util.List<F> elements) {
        return ((lazy.List<E>) elements).knownSize();
      }

      @Override
      @SuppressWarnings("unchecked")
      protected void materialize(@NotNull java.util.List<F> elements) {
        ((lazy.List<E>) elements).materialized();
      }
    }

    private class IndexOfElementAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final AsyncConsumer<Integer> consumer;
      private final Object element;

      private int index;

      private IndexOfElementAsyncConsumer(@NotNull final Object element,
          @NotNull final AsyncConsumer<Integer> consumer) {
        this.element = element;
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (this.element.equals(element)) {
          consumer.accept(index);
        } else {
          this.index = index + 1;
          context.scheduleAfter(this);
        }
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
      public void run() {
        materializer.materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return materializer.weightElement();
      }
    }

    private class IndexOfNullAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final AsyncConsumer<Integer> consumer;

      private int index;

      private IndexOfNullAsyncConsumer(@NotNull final AsyncConsumer<Integer> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (element == null) {
          consumer.accept(index);
        } else {
          this.index = index + 1;
          context.scheduleAfter(this);
        }
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
      public void run() {
        materializer.materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return materializer.weightElement();
      }
    }

    private class LastIndexOfElementAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final AsyncConsumer<Integer> consumer;
      private final Object element;

      private int index;

      private LastIndexOfElementAsyncConsumer(@NotNull final Object element, final int size,
          @NotNull final AsyncConsumer<Integer> consumer) {
        this.element = element;
        this.consumer = consumer;
        index = size - 1;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (this.element.equals(element)) {
          consumer.accept(index);
        } else {
          this.index = index - 1;
          context.scheduleAfter(this);
        }
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
      public void run() {
        materializer.materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return materializer.weightElement();
      }
    }

    private class LastIndexOfNullAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final AsyncConsumer<Integer> consumer;

      private int index;

      private LastIndexOfNullAsyncConsumer(final int size,
          @NotNull final AsyncConsumer<Integer> consumer) {
        this.consumer = consumer;
        index = size - 1;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (element == null) {
          consumer.accept(index);
        } else {
          this.index = index - 1;
          context.scheduleAfter(this);
        }
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
      public void run() {
        materializer.materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return materializer.weightElement();
      }
    }
  }

  private static class BlockingConsumer<P> extends Semaphore implements AsyncConsumer<P> {

    private Exception error;
    private P param;

    private BlockingConsumer() {
      super(0);
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
      acquire();
      if (error != null) {
        throw UncheckedException.throwUnchecked(error);
      }
      return param;
    }

    private P get(final long timeout, @NotNull final TimeUnit unit)
        throws InterruptedException, TimeoutException {
      if (!tryAcquire(1, timeout, unit)) {
        throw new TimeoutException();
      }
      if (error != null) {
        throw UncheckedException.throwUnchecked(error);
      }
      return param;
    }
  }

  private static class BlockingElementConsumer<P> extends Semaphore implements
      IndexedAsyncConsumer<P> {

    private final int index;

    private Exception error;
    private P param;

    private BlockingElementConsumer(final int index) {
      super(0);
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
      acquire();
      if (error != null) {
        throw UncheckedException.throwUnchecked(error);
      }
      return param;
    }
  }

  private static class EmptyListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

    private static final EmptyListAsyncMaterializer<?> INSTANCE = new EmptyListAsyncMaterializer<Object>();
    private static final Logger LOGGER = Logger.getLogger(
        EmptyListAsyncMaterializer.class.getName());

    @SuppressWarnings("unchecked")
    public static @NotNull <E> EmptyListAsyncMaterializer<E> instance() {
      return (EmptyListAsyncMaterializer<E>) INSTANCE;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return true;
    }

    @Override
    public boolean isFailed() {
      return false;
    }

    @Override
    public boolean isMaterializedAtOnce() {
      return true;
    }

    @Override
    public int knownSize() {
      return 0;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<java.util.List<E>> consumer) {
      materializeElements(consumer);
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      safeConsumeComplete(consumer, 0, LOGGER);
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        safeConsumeComplete(consumer, 0, LOGGER);
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<java.util.List<E>> consumer) {
      safeConsume(consumer, lazy.List.<E>of(), LOGGER);
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, true, LOGGER);
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      safeConsume(consumer, 0, LOGGER);
    }

    @Override
    public int weightContains() {
      return 1;
    }

    @Override
    public int weightElement() {
      return 1;
    }

    @Override
    public int weightElements() {
      return 1;
    }

    @Override
    public int weightEmpty() {
      return 1;
    }

    @Override
    public int weightSize() {
      return 1;
    }
  }
}
