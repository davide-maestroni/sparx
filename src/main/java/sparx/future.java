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
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.internal.future.iterator.CollectionToIteratorAsyncMaterializer;
import sparx.internal.future.iterator.ElementToIteratorAsyncMaterializer;
import sparx.internal.future.iterator.EmptyIteratorAsyncMaterializer;
import sparx.internal.future.iterator.IteratorAsyncMaterializer;
import sparx.internal.future.iterator.IteratorToIteratorAsyncMaterializer;
import sparx.internal.future.iterator.ListAsyncMaterializerToIteratorAsyncMaterializer;
import sparx.internal.future.iterator.ListToIteratorAsyncMaterializer;
import sparx.internal.future.list.AbstractListAsyncMaterializer;
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
import sparx.internal.future.list.EachListAsyncMaterializer;
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
import sparx.internal.future.list.FoldLeftListAsyncMaterializer;
import sparx.internal.future.list.FoldRightListAsyncMaterializer;
import sparx.internal.future.list.GroupListAsyncMaterializer;
import sparx.internal.future.list.GroupListAsyncMaterializer.Chunker;
import sparx.internal.future.list.IncludesAllListAsyncMaterializer;
import sparx.internal.future.list.IncludesSliceListAsyncMaterializer;
import sparx.internal.future.list.InsertAfterListAsyncMaterializer;
import sparx.internal.future.list.InsertAllAfterListAsyncMaterializer;
import sparx.internal.future.list.IntersectListAsyncMaterializer;
import sparx.internal.future.list.ListAsyncMaterializer;
import sparx.internal.future.list.ListToListAsyncMaterializer;
import sparx.internal.future.list.MapAfterListAsyncMaterializer;
import sparx.internal.future.list.MapFirstWhereListAsyncMaterializer;
import sparx.internal.future.list.MapLastWhereListAsyncMaterializer;
import sparx.internal.future.list.MapListAsyncMaterializer;
import sparx.internal.future.list.MaxListAsyncMaterializer;
import sparx.internal.future.list.OrElseListAsyncMaterializer;
import sparx.internal.future.list.PrependAllListAsyncMaterializer;
import sparx.internal.future.list.PrependListAsyncMaterializer;
import sparx.internal.future.list.ReduceLeftListAsyncMaterializer;
import sparx.internal.future.list.ReduceRightListAsyncMaterializer;
import sparx.internal.future.list.RemoveAfterListAsyncMaterializer;
import sparx.internal.future.list.RemoveFirstWhereListAsyncMaterializer;
import sparx.internal.future.list.RemoveLastWhereListAsyncMaterializer;
import sparx.internal.future.list.RemoveSliceListAsyncMaterializer;
import sparx.internal.future.list.RemoveWhereListAsyncMaterializer;
import sparx.internal.future.list.ReplaceSliceListAsyncMaterializer;
import sparx.internal.future.list.ResizeListAsyncMaterializer;
import sparx.internal.future.list.ReverseListAsyncMaterializer;
import sparx.internal.future.list.SliceListAsyncMaterializer;
import sparx.internal.future.list.SortedListAsyncMaterializer;
import sparx.internal.future.list.StartsWithListAsyncMaterializer;
import sparx.internal.future.list.SwitchListAsyncMaterializer;
import sparx.internal.future.list.SymmetricDiffListAsyncMaterializer;
import sparx.internal.future.list.TakeListAsyncMaterializer;
import sparx.internal.future.list.TakeRightListAsyncMaterializer;
import sparx.internal.future.list.TakeRightWhileListAsyncMaterializer;
import sparx.internal.future.list.TakeWhileListAsyncMaterializer;
import sparx.internal.future.list.TransformListAsyncMaterializer;
import sparx.util.DeadLockException;
import sparx.util.IndexOverflowException;
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
import sparx.util.function.TernaryFunction;

class future extends Sparx {

  private future() {
  }

  // TODO: move to future.Iterator
  @SuppressWarnings("unchecked")
  private static @NotNull <E> IteratorAsyncMaterializer<E> getElementsMaterializer(
      @NotNull final ExecutionContext context, @NotNull final Iterable<? extends E> elements) {
    if (elements instanceof List) {
      final List<E> list = (List<E>) elements;
      if (context.equals(list.context)) {
        return new ListAsyncMaterializerToIteratorAsyncMaterializer<E>(list.materializer);
      }
      return new ListAsyncMaterializerToIteratorAsyncMaterializer<E>(
          new SwitchListAsyncMaterializer<E>(list.context, list.taskID, context,
              list.materializer));
    }
    if (elements instanceof java.util.List) {
      final java.util.List<E> list = (java.util.List<E>) elements;
      if (list.isEmpty()) {
        return EmptyIteratorAsyncMaterializer.instance();
      }
      if (list.size() == 1) {
        return new ElementToIteratorAsyncMaterializer<E>(list.get(0));
      }
      return new ListToIteratorAsyncMaterializer<E>(list, context);
    }
    if (elements instanceof Collection) {
      return new CollectionToIteratorAsyncMaterializer<E>((Collection<E>) elements, context);
    }
    // TODO: future.Iterator
    if (elements instanceof java.util.Iterator) {
      return new IteratorToIteratorAsyncMaterializer<E>((java.util.Iterator<E>) elements, context);
    }
    return new IteratorToIteratorAsyncMaterializer<E>((java.util.Iterator<E>) elements.iterator(),
        context);
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
      public java.util.List<?> apply(final java.util.List<?> param) {
        return lazy.List.wrap(param).materialized();
      }
    };
    private static final ElementToListAsyncMaterializer<Boolean> FALSE_MATERIALIZER = new ElementToListAsyncMaterializer<Boolean>(
        lazy.List.of(false));
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

    private static @NotNull <T> List<T> emptyList(@NotNull final ExecutionContext context) {
      return new List<T>(context, new AtomicReference<CancellationException>(),
          EmptyListAsyncMaterializer.<T>instance());
    }

    private static @NotNull List<Boolean> falseList(@NotNull final ExecutionContext context) {
      return new List<Boolean>(context, new AtomicReference<CancellationException>(),
          FALSE_MATERIALIZER);
    }

    private static @NotNull <E> Chunker<E, List<E>> getChunker(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      return new Chunker<E, List<E>>() {
        @Override
        public @NotNull List<E> getChunk(@NotNull final ListAsyncMaterializer<E> materializer,
            final int start, final int end) {
          return new List<E>(context, cancelException, materializer).slice(start, end);
        }

        @Override
        public void getElements(@NotNull final List<E> chunk,
            @NotNull final AsyncConsumer<java.util.List<E>> consumer) {
          final ListAsyncMaterializer<E> materializer = chunk.materializer;
          context.scheduleAfter(new Task() {
            @Override
            public void run() {
              materializer.materializeElements(consumer);
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
        }
      };
    }

    private static @NotNull <E> Chunker<E, List<E>> getChunker(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final AtomicReference<CancellationException> cancelException, final int size,
        final E padding) {
      return new Chunker<E, List<E>>() {
        @Override
        public @NotNull List<E> getChunk(@NotNull final ListAsyncMaterializer<E> materializer,
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
            @NotNull final AsyncConsumer<java.util.List<E>> consumer) {
          final ListAsyncMaterializer<E> materializer = chunk.materializer;
          context.scheduleAfter(new Task() {
            @Override
            public void run() {
              materializer.materializeElements(consumer);
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
        }
      };
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> ListAsyncMaterializer<E> getElementsMaterializer(
        @NotNull final ExecutionContext context, @NotNull final Iterable<? extends E> elements) {
      if (elements instanceof List) {
        final List<E> list = (List<E>) elements;
        if (context == list.context) {
          return list.materializer;
        }
        return new SwitchListAsyncMaterializer<E>(list.context, list.taskID, context,
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
        return new ListToListAsyncMaterializer<E>(materialized, context);
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
        return new ListToListAsyncMaterializer<E>(lazy.List.wrap(list), context);
      }
      if (elements instanceof java.util.Collection) {
        final java.util.Collection<E> collection = (java.util.Collection<E>) elements;
        final int size = collection.size();
        if (size == 0) {
          return EmptyListAsyncMaterializer.instance();
        }
        if (size == 1) {
          return new ElementToListAsyncMaterializer<E>(lazy.List.wrap(collection));
        }
        return new ListToListAsyncMaterializer<E>(lazy.List.wrap(collection), context);
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
      return new ListToListAsyncMaterializer<E>(lazy.List.wrap(list), context);
    }

    private static @NotNull <E, F> IndexedFunction<E, IteratorAsyncMaterializer<F>> getElementToIteratorMaterializer(
        @NotNull final ExecutionContext context,
        @NotNull final Function<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, IteratorAsyncMaterializer<F>>() {
        @Override
        public IteratorAsyncMaterializer<F> apply(final int index, final E element)
            throws Exception {
          return future.getElementsMaterializer(context, mapper.apply(element));
        }
      };
    }

    private static @NotNull <E, F> IndexedFunction<E, IteratorAsyncMaterializer<F>> getElementToIteratorMaterializer(
        @NotNull final ExecutionContext context,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, IteratorAsyncMaterializer<F>>() {
        @Override
        public IteratorAsyncMaterializer<F> apply(final int index, final E element)
            throws Exception {
          return future.getElementsMaterializer(context, mapper.apply(index, element));
        }
      };
    }

    private static @NotNull <E> IndexedFunction<E, ListAsyncMaterializer<E>> getElementToMaterializer(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<E, ListAsyncMaterializer<E>>() {
        @Override
        public ListAsyncMaterializer<E> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(context, mapper.apply(element));
        }
      };
    }

    private static @NotNull <E> IndexedFunction<E, ListAsyncMaterializer<E>> getElementToMaterializer(
        @NotNull final ExecutionContext context, @NotNull final String taskID,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<E, ListAsyncMaterializer<E>>() {
        @Override
        public ListAsyncMaterializer<E> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(context, mapper.apply(index, element));
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

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerAppend(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException, final E element) {
      final long knownSize = materializer.knownSize();
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize > 0 ? SizeOverflowException.safeCast(knownSize + 1) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).append(element);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerAppendAll(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements, final int elementsKnownSize) {
      final long knownSize = materializer.knownSize();
      final Iterable<? extends E> appended = elements;
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize > 0 && elementsKnownSize > 0 ? SizeOverflowException.safeCast(
              knownSize + elementsKnownSize) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).appendAll(appended);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Integer> lazyMaterializerCount(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      return new LazyListAsyncMaterializer<E, Integer>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).count();
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Integer> lazyMaterializerCountWhere(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, Integer>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).countWhere(predicate);
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
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).dropWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Boolean> lazyMaterializerEach(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, Boolean>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).each(predicate);
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
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> elements) {
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
        protected @NotNull java.util.List<Integer> transform(
            @NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).findLastIndexOfSlice(otherElements);
        }
      };
    }

    private static @NotNull <E, F> LazyListAsyncMaterializer<E, F> lazyMaterializerFoldLeft(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return new LazyListAsyncMaterializer<E, F>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<F> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).foldLeft(identity, operation);
        }
      };
    }

    private static @NotNull <E, F> LazyListAsyncMaterializer<E, F> lazyMaterializerFoldRight(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException, final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      return new LazyListAsyncMaterializer<E, F>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<F> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).foldRight(identity, operation);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Boolean> lazyMaterializerIncludesAll(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      final Iterable<?> otherElements = elements;
      return new LazyListAsyncMaterializer<E, Boolean>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).includesAll(otherElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Boolean> lazyMaterializerIncludesSlice(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      final Iterable<?> otherElements = elements;
      return new LazyListAsyncMaterializer<E, Boolean>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).includesSlice(otherElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerInsertAfter(
        @NotNull final ListAsyncMaterializer<E> materializer,
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
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, size) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).insertAfter(numElements, element);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerInsertAllAfter(
        @NotNull final ListAsyncMaterializer<E> materializer,
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
      final Iterable<? extends E> inserted = elements;
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, size) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).insertAllAfter(numElements, inserted);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerIntersect(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<?> elements) {
      final Iterable<?> otherElements = elements;
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).intersect(otherElements);
        }
      };
    }

    private static @NotNull <E, F> LazyListAsyncMaterializer<E, F> lazyMaterializerMap(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedFunction<? super E, F> mapper) {
      return new LazyListAsyncMaterializer<E, F>(materializer, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<F> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).map(mapper);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerMapAfter(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElements, @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).mapAfter(numElements, mapper);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerMapFirstWhere(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).mapFirstWhere(predicate, mapper);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerMapLastWhere(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).mapLastWhere(predicate, mapper);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerMax(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Comparator<? super E> comparator) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).max(comparator);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Boolean> lazyMaterializerNone(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, Boolean>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).none(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, Boolean> lazyMaterializerNotExists(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, Boolean>(materializer, cancelException, 1) {
        @Override
        protected @NotNull java.util.List<Boolean> transform(
            @NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).notAll(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerOrElse(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements) {
      final Iterable<? extends E> otherElements = elements;
      final int knownSize = materializer.knownSize();
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize > 0 ? knownSize : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).orElse(otherElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerPrepend(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException, final E element) {
      final long knownSize = materializer.knownSize();
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize > 0 ? SizeOverflowException.safeCast(knownSize + 1) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).prepend(element);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerPrependAll(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements, final int elementsKnownSize) {
      final long knownSize = materializer.knownSize();
      final Iterable<? extends E> prepended = elements;
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize > 0 && elementsKnownSize > 0 ? SizeOverflowException.safeCast(
              knownSize + elementsKnownSize) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).prependAll(prepended);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerReduceLeft(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).reduceLeft(operation);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerReduceRight(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).reduceRight(operation);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerRemoveAfter(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElement) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).removeAfter(numElement);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerRemoveFirstWhere(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).removeFirstWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerRemoveLastWhere(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).removeLastWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerRemoveSlice(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException, final int start,
        final int end) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).removeSlice(start, end);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerRemoveWhere(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).removeWhere(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerReplaceSlice(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException, final int start,
        final int end, @NotNull final Iterable<? extends E> patch) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).replaceSlice(start, end, patch);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerResizeTo(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int numElements, final E padding) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, numElements) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).resizeTo(numElements, padding);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerReverse(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).reverse();
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerSlice(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException, final int start,
        final int end) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).slice(start, end);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerSorted(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Comparator<? super E> comparator) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          materializer.knownSize()) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).sorted(comparator);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerSymmetricDiff(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements) {
      final Iterable<? extends E> otherElements = elements;
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).symmetricDiff(otherElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerTake(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize >= 0 ? Math.min(knownSize, maxElements) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).take(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerTakeRight(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        final int maxElements) {
      final int knownSize = materializer.knownSize();
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException,
          knownSize >= 0 ? Math.min(knownSize, maxElements) : -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).takeRight(maxElements);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerTakeRightWhile(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).takeRightWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerTakeWhile(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).takeWhile(predicate);
        }
      };
    }

    private static @NotNull <E> LazyListAsyncMaterializer<E, E> lazyMaterializerUnion(
        @NotNull final ListAsyncMaterializer<E> materializer,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Iterable<? extends E> elements) {
      final Iterable<? extends E> otherElements = elements;
      return new LazyListAsyncMaterializer<E, E>(materializer, cancelException, -1) {
        @Override
        protected @NotNull java.util.List<E> transform(@NotNull final java.util.List<E> elements) {
          return ((lazy.List<E>) elements).union(otherElements);
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
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new AppendListAsyncMaterializer<E>(materializer, element, context, cancelException,
              List.<E>appendFunction()));
    }

    @Override
    public @NotNull List<E> appendAll(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final ListAsyncMaterializer<E> elementsMaterializer = getElementsMaterializer(context,
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
          new AppendAllListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
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

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      if (!materializer.isDone() && cancelException.compareAndSet(null,
          new CancellationException())) {
        final ExecutionContext context = this.context;
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
        synchronized (cancelException) {
          cancelException.notifyAll();
        }
        return true;
      }
      return false;
    }

    @Override
    public boolean contains(final Object o) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return false;
      }
      final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(new AsyncConsumer<java.util.List<E>>() {
          @Override
          @SuppressWarnings("SuspiciousMethodCalls")
          public void accept(final java.util.List<E> elements) {
            consumer.accept(elements.contains(o));
          }

          @Override
          public void error(@NotNull final Exception error) {
            consumer.error(error);
          }
        });
      } else {
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
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull List<Integer> count() {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return zeroList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
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
    public @NotNull List<Integer> countWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return zeroList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerCountWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Integer>(context, cancelException,
          new CountWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> countWhere(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return zeroList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerCountWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Integer>(context, cancelException,
          new CountWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<E> diff(@NotNull final Iterable<?> elements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<?> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
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
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
      }
      if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
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
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
      }
      if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
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
    public @NotNull List<Boolean> each(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerEach(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<Boolean>(context, cancelException,
          new EachListAsyncMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              false, cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> each(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerEach(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<Boolean>(context, cancelException,
          new EachListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), false, cancelException,
              List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> endsWith(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<Object> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      if (elementsMaterializer.knownSize() == 0) {
        return trueList(context);
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
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
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListAsyncMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              false, cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), false, cancelException,
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
      return new List<E>(context, cancelException,
          new FindFirstListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), cancelException,
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
      return new List<E>(context, cancelException,
          new FindFirstListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findIndexOf(final Object element) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, cancelException, equalsElement(element)));
      }
      return new List<Integer>(context, cancelException,
          new FindIndexListAsyncMaterializer<E>(materializer, equalsElement(element),
              cancelException, List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final ListAsyncMaterializer<Object> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      if (elementsMaterializer.knownSize() == 0) {
        return zeroList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
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
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Integer>(context, cancelException,
          new FindIndexListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findIndexWhere(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindIndexWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Integer>(context, cancelException,
          new FindIndexListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
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
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
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
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, cancelException,
                equalsElement(element)));
      }
      return new List<Integer>(context, cancelException,
          new FindLastIndexListAsyncMaterializer<E>(materializer, equalsElement(element),
              cancelException, List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final ListAsyncMaterializer<Object> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
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
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Integer>(context, cancelException,
          new FindLastIndexListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public @NotNull List<Integer> findLastIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Integer>(context, cancelException,
            lazyMaterializerFindLastIndexWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Integer>(context, cancelException,
          new FindLastIndexListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), cancelException,
              List.<Integer>decorateFunction()));
    }

    @Override
    public E first() {
      final ListAsyncMaterializer<E> materializer = this.materializer;
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
        materializer.materializeElements(new AsyncConsumer<java.util.List<E>>() {
          @Override
          public void accept(final java.util.List<E> elements) {
            consumer.accept(-1, 0, elements.get(0));
          }

          @Override
          public void error(@NotNull final Exception error) {
            consumer.error(error);
          }
        });
      } else {
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
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<F>(context, cancelException,
          new FlatMapListAsyncMaterializer<E, F>(materializer,
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<F>decorateFunction()));
    }

    @Override
    public @NotNull <F> List<F> flatMap(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<F>(context, cancelException,
          new FlatMapListAsyncMaterializer<E, F>(materializer,
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<F>decorateFunction()));
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
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FlatMapFirstWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FlatMapFirstWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FlatMapLastWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FlatMapLastWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToMaterializer(context, taskID, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FlatMapWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"),
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new FlatMapWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              getElementToIteratorMaterializer(context, Require.notNull(mapper, "mapper")), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull <F> List<F> fold(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return foldLeft(identity, operation);
    }

    @Override
    public @NotNull <F> List<F> foldLeft(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<F>(context, cancelException,
            new ElementToListAsyncMaterializer<F>(lazy.List.of(identity)));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerFoldLeft(materializer, cancelException, identity,
                Require.notNull(operation, "operation")));
      }
      return new List<F>(context, cancelException,
          new FoldLeftListAsyncMaterializer<E, F>(materializer, identity,
              Require.notNull(operation, "operation"), cancelException,
              List.<F>decorateFunction()));
    }

    @Override
    public @NotNull <F> List<F> foldRight(final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<F>(context, cancelException,
            new ElementToListAsyncMaterializer<F>(lazy.List.of(identity)));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerFoldRight(materializer, cancelException, identity,
                Require.notNull(operation, "operation")));
      }
      final ExecutionContext context = this.context;
      return new List<F>(context, cancelException,
          new FoldRightListAsyncMaterializer<E, F>(materializer, identity,
              Require.notNull(operation, "operation"), cancelException,
              List.<F>decorateFunction()));
    }

    @Override
    public lazy.List<E> get() throws InterruptedException, ExecutionException {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return lazy.List.of();
      }
      final BlockingConsumer<java.util.List<E>> consumer = new BlockingConsumer<java.util.List<E>>(
          cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(consumer);
      } else {
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
      }
      try {
        return (lazy.List<E>) consumer.get();
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
        final ListAsyncMaterializer<E> materializer = this.materializer;
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(new AsyncConsumer<java.util.List<E>>() {
          @Override
          public void accept(final java.util.List<E> elements) {
            consumer.accept(-1, index, elements.get(index));
          }

          @Override
          public void error(@NotNull final Exception error) {
            consumer.error(error);
          }
        });
      } else {
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
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public lazy.List<E> get(final long timeout, @NotNull final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return lazy.List.of();
      }
      final BlockingConsumer<java.util.List<E>> consumer = new BlockingConsumer<java.util.List<E>>(
          cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(consumer);
      } else {
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
      }
      try {
        return lazy.List.wrap(consumer.get(timeout, unit));
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
    public @NotNull List<? extends List<E>> group(final int maxSize) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<List<E>>(context, cancelException,
          new GroupListAsyncMaterializer<E, List<E>>(materializer,
              Require.positive(maxSize, "maxSize"),
              List.<E>getChunker(context, taskID, cancelException), context, cancelException,
              List.<List<E>>decorateFunction()));
    }

    @Override
    public @NotNull List<? extends List<E>> groupWithPadding(final int size, final E padding) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<List<E>>(context, cancelException,
          new GroupListAsyncMaterializer<E, List<E>>(materializer, size,
              List.getChunker(context, taskID, cancelException, Require.positive(size, "size"),
                  padding), context, cancelException, List.<List<E>>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> includes(final Object element) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerExists(materializer, cancelException, Sparx.<E>equalsElement(element)));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListAsyncMaterializer<E>(materializer, Sparx.<E>equalsElement(element), false,
              cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> includesAll(@NotNull final Iterable<?> elements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final ListAsyncMaterializer<Object> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      if (elementsMaterializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerIncludesAll(materializer, cancelException, elements));
      }
      return new List<Boolean>(context, cancelException,
          new IncludesAllListAsyncMaterializer<E>(materializer, elementsMaterializer,
              cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final ListAsyncMaterializer<Object> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      if (elementsMaterializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerIncludesSlice(materializer, cancelException, elements));
      }
      final ExecutionContext context = this.context;
      return new List<Boolean>(context, cancelException,
          new IncludesSliceListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
              cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public int indexOf(final Object o) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return -1;
      }
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(new AsyncConsumer<java.util.List<E>>() {
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
        context.scheduleAfter(new IndexOfNullAsyncPredicate(consumer));
      } else {
        context.scheduleAfter(new IndexOfElementAsyncPredicate(o, consumer));
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull List<E> insertAfter(final int numElements, final E element) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final long knownSize = materializer.knownSize();
      if (numElements < 0 || numElements == Integer.MAX_VALUE || (knownSize >= 0
          && numElements > knownSize)) {
        return new List<E>(context, cancelException, materializer);
      }
      if (knownSize == 0) {
        return new List<E>(context, cancelException,
            new ElementToListAsyncMaterializer<E>(lazy.List.of(element)));
      }
      if (numElements == 0) {
        if (materializer.isMaterializedAtOnce()) {
          return new List<E>(context, cancelException,
              lazyMaterializerPrepend(materializer, cancelException, element));
        }
        final ExecutionContext context = this.context;
        return new List<E>(context, cancelException,
            new PrependListAsyncMaterializer<E>(materializer, element, context, cancelException,
                List.<E>prependFunction()));
      } else if (numElements == knownSize) {
        if (materializer.isMaterializedAtOnce()) {
          return new List<E>(context, cancelException,
              lazyMaterializerAppend(materializer, cancelException, element));
        }
        final ExecutionContext context = this.context;
        return new List<E>(context, cancelException,
            new AppendListAsyncMaterializer<E>(materializer, element, context, cancelException,
                List.<E>appendFunction()));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerInsertAfter(materializer, cancelException, numElements, element));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new InsertAfterListAsyncMaterializer<E>(materializer, numElements, element, context,
              cancelException, List.<E>insertAfterFunction()));
    }

    @Override
    public @NotNull List<E> insertAllAfter(final int numElements,
        @NotNull final Iterable<? extends E> elements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final long knownSize = materializer.knownSize();
      if (numElements < 0 || numElements == Integer.MAX_VALUE || (knownSize >= 0
          && numElements > knownSize)) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (knownSize == 0) {
        return new List<E>(context, cancelException, elementsMaterializer);
      }
      if (numElements == 0) {
        if (materializer.isMaterializedAtOnce()) {
          return new List<E>(context, cancelException,
              lazyMaterializerPrependAll(materializer, cancelException, elements,
                  elementsMaterializer.knownSize()));
        }
        return new List<E>(context, cancelException,
            new PrependAllListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
                cancelException, List.<E>prependAllFunction()));
      } else if (numElements == knownSize) {
        if (materializer.isMaterializedAtOnce()) {
          return new List<E>(context, cancelException,
              lazyMaterializerAppendAll(materializer, cancelException, elements,
                  elementsMaterializer.knownSize()));
        }
        return new List<E>(context, cancelException,
            new AppendAllListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
                cancelException, List.<E>appendAllFunction()));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerInsertAllAfter(materializer, cancelException, numElements, elements,
                elementsMaterializer.knownSize()));
      }
      return new List<E>(context, cancelException,
          new InsertAllAfterListAsyncMaterializer<E>(materializer, numElements,
              elementsMaterializer, context, cancelException, List.<E>insertAllAfterFunction()));
    }

    @Override
    public @NotNull List<E> intersect(@NotNull final Iterable<?> elements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<?> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      if (elementsMaterializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerIntersect(materializer, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<E>(context, cancelException,
          new IntersectListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
              cancelException, List.<E>decorateFunction()));
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
    public boolean isFailed() {
      return materializer.isFailed();
    }

    @Override
    public boolean isSucceeded() {
      return materializer.isSucceeded();
    }

    @Override
    public boolean isEmpty() {
      final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        final ListAsyncMaterializer<E> materializer = this.materializer;
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(new AsyncConsumer<java.util.List<E>>() {
          @Override
          public void accept(final java.util.List<E> elements) {
            consumer.accept(elements.isEmpty());
          }

          @Override
          public void error(@NotNull final Exception error) {
            consumer.error(error);
          }
        });
      } else {
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
      }
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
      final ListAsyncMaterializer<E> materializer = this.materializer;
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
        materializer.materializeElements(new AsyncConsumer<java.util.List<E>>() {
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
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public int lastIndexOf(final Object o) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
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
        materializer.materializeElements(new AsyncConsumer<java.util.List<E>>() {
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
        context.scheduleAfter(new LastIndexOfNullAsyncPredicate(knownSize, consumer));
      } else {
        context.scheduleAfter(new LastIndexOfElementAsyncPredicate(o, knownSize, consumer));
      }
      try {
        return consumer.get();
      } catch (final InterruptedException e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public @NotNull ListIterator<E> listIterator() {
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context), this);
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
      return new ListIterator<E>(context, List.<E>emptyList(context), this, index);
    }

    @Override
    public @NotNull <F> List<F> map(@NotNull final Function<? super E, F> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerMap(materializer, cancelException,
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      final ExecutionContext context = this.context;
      return new List<F>(context, cancelException, new MapListAsyncMaterializer<E, F>(materializer,
          toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException,
          List.<F>decorateFunction()));
    }

    @Override
    public @NotNull <F> List<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<F>(context, cancelException,
            lazyMaterializerMap(materializer, cancelException, Require.notNull(mapper, "mapper")));
      }
      final ExecutionContext context = this.context;
      return new List<F>(context, cancelException,
          new MapListAsyncMaterializer<E, F>(materializer, Require.notNull(mapper, "mapper"),
              context, cancelException, List.<F>decorateFunction()));
    }

    @Override
    public @NotNull List<E> mapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapAfter(materializer, cancelException, numElements,
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapAfterListAsyncMaterializer<E>(materializer, numElements,
              toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapAfter(materializer, cancelException, numElements,
                Require.notNull(mapper, "mapper")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapAfterListAsyncMaterializer<E>(materializer, numElements,
              Require.notNull(mapper, "mapper"), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapFirstWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper"), context,
              cancelException, List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate")),
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapFirstWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapLastWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper"), context,
              cancelException, List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate")),
                toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapLastWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              toIndexedFunction(Require.notNull(mapper, "mapper")), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> mapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, cancelException,
                filteredMapper(Require.notNull(predicate, "predicate"),
                    Require.notNull(mapper, "mapper"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException, new MapListAsyncMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper")), context, cancelException,
          List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> mapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, cancelException,
                filteredMapper(Require.notNull(predicate, "predicate"),
                    Require.notNull(mapper, "mapper"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException, new MapListAsyncMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper")), context, cancelException,
          List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> max(@NotNull final Comparator<? super E> comparator) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMax(materializer, cancelException,
                Require.notNull(comparator, "comparator")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MaxListAsyncMaterializer<E>(materializer, Require.notNull(comparator, "comparator"),
              context, cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> min(@NotNull final Comparator<? super E> comparator) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMax(materializer, cancelException,
                reversed(Require.notNull(comparator, "comparator"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException, new MaxListAsyncMaterializer<E>(materializer,
          reversed(Require.notNull(comparator, "comparator")), context, cancelException,
          List.<E>decorateFunction()));
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final Consumer<? super E> consumer) {
      return new AsyncForFuture<E>(context, taskID, cancelException, materializer,
          toIndexedConsumer(Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final IndexedConsumer<? super E> consumer) {
      return new AsyncForFuture<E>(context, taskID, cancelException, materializer,
          Require.notNull(consumer, "consumer"));
    }

    @Override
    public @NotNull Future<?> nonBlockingGet() {
      return new AsyncGetFuture<E>(context, taskID, cancelException, materializer);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      return new AsyncWhileFuture<E>(context, taskID, cancelException, materializer,
          Require.notNull(predicate, "predicate"));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      return new AsyncWhileFuture<E>(context, taskID, cancelException, materializer,
          Require.notNull(condition, "condition"), Require.notNull(consumer, "consumer"));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> predicate) {
      return new AsyncWhileFuture<E>(context, taskID, cancelException, materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      return new AsyncWhileFuture<E>(context, taskID, cancelException, materializer,
          toIndexedPredicate(Require.notNull(condition, "condition")),
          toIndexedConsumer(Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull List<Boolean> none(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerNone(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<Boolean>(context, cancelException,
          new EachListAsyncMaterializer<E>(materializer,
              negated(Require.notNull(predicate, "predicate")), true, cancelException,
              List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> none(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerNone(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<Boolean>(context, cancelException,
          new EachListAsyncMaterializer<E>(materializer,
              toNegatedIndexedPredicate(Require.notNull(predicate, "predicate")), true,
              cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> notAll(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerNotExists(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListAsyncMaterializer<E>(materializer,
              negated(Require.notNull(predicate, "predicate")), true, cancelException,
              List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return trueList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<Boolean>(context, cancelException,
            lazyMaterializerNotExists(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      return new List<Boolean>(context, cancelException,
          new ExistsListAsyncMaterializer<E>(materializer,
              toNegatedIndexedPredicate(Require.notNull(predicate, "predicate")), true,
              cancelException, List.<Boolean>decorateFunction()));
    }

    @Override
    public @NotNull List<E> orElse(@NotNull final Iterable<? extends E> elements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        final ExecutionContext context = this.context;
        return new List<E>(context, cancelException,
            getElementsMaterializer(context, Require.notNull(elements, "elements")));
      }
      if (knownSize > 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerOrElse(materializer, cancelException,
                Require.notNull(elements, "elements")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException, new OrElseListAsyncMaterializer<E>(materializer,
          getElementsMaterializer(context, Require.notNull(elements, "elements")), context,
          cancelException));
    }

    @Override
    public @NotNull List<E> orElseGet(
        @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        final ExecutionContext context = this.context;
        return new List<E>(context, cancelException,
            new SuppliedMaterializer<E>(Require.notNull(supplier, "supplier"), context, taskID,
                cancelException));
      }
      if (knownSize > 0) {
        return new List<E>(context, cancelException, materializer);
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException, new OrElseListAsyncMaterializer<E>(materializer,
          new SuppliedMaterializer<E>(Require.notNull(supplier, "supplier"), context, taskID,
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
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final long knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return new List<E>(context, cancelException,
            new ElementToListAsyncMaterializer<E>(lazy.List.of(element)));
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerPrepend(materializer, cancelException, element));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new PrependListAsyncMaterializer<E>(materializer, element, context, cancelException,
              List.<E>prependFunction()));
    }

    @Override
    public @NotNull List<E> prependAll(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final ListAsyncMaterializer<E> elementsMaterializer = getElementsMaterializer(context,
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
            lazyMaterializerPrependAll(materializer, cancelException, elements, elementsKnownSize));
      }
      return new List<E>(context, cancelException,
          new PrependAllListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
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
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerReduceLeft(materializer, cancelException,
                Require.notNull(operation, "operation")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new ReduceLeftListAsyncMaterializer<E>(materializer,
              Require.notNull(operation, "operation"), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> reduceRight(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerReduceRight(materializer, cancelException,
                Require.notNull(operation, "operation")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new ReduceRightListAsyncMaterializer<E>(materializer,
              Require.notNull(operation, "operation"), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> removeAfter(final int numElements) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return this;
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
      }
      if (numElements == 0 && knownSize == 1) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveAfter(materializer, cancelException, numElements));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveAfterListAsyncMaterializer<E>(materializer, numElements, context,
              cancelException, List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeEach(final E element) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveWhere(materializer, cancelException, equalsElement(element)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveWhereListAsyncMaterializer<E>(materializer, equalsElement(element), context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> removeFirst(final E element) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveFirstWhere(materializer, cancelException,
                equalsElement(element)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveFirstWhereListAsyncMaterializer<E>(materializer, equalsElement(element),
              context, cancelException, List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeFirstWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveFirstWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveFirstWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeFirstWhere(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveFirstWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveFirstWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeLast(final E element) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveLastWhere(materializer, cancelException, equalsElement(element)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveLastWhereListAsyncMaterializer<E>(materializer, equalsElement(element), context,
              cancelException, List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeLastWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveLastWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveLastWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeLastWhere(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveLastWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveLastWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>removeAfterFunction()));
    }

    @Override
    public @NotNull List<E> removeSlice(final int start, final int end) {
      if (end >= 0 && start >= end) {
        return this;
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
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
          final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
          return new List<E>(context, cancelException, materializer);
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
            lazyMaterializerRemoveSlice(materializer, cancelException, start, end));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveSliceListAsyncMaterializer<E>(materializer, start, end, context,
              cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> removeWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerRemoveWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new RemoveWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> replaceAfter(final int numElements, final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
      }
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return emptyList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapAfter(materializer, cancelException, numElements,
                replacementMapper(replacement)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapAfterListAsyncMaterializer<E>(materializer, numElements,
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceEach(final E element, final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, cancelException,
                filteredMapper(equalsElement(element), replacementMapper(replacement))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException, new MapListAsyncMaterializer<E, E>(materializer,
          filteredMapper(equalsElement(element), replacementMapper(replacement)), context,
          cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> replaceFirst(final E element, final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, cancelException, equalsElement(element),
                replacementMapper(replacement)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapFirstWhereListAsyncMaterializer<E>(materializer, equalsElement(element),
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate"), replacementMapper(replacement)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapFirstWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), replacementMapper(replacement), context,
              cancelException, List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceFirstWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapFirstWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate")),
                replacementMapper(replacement)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapFirstWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceLast(final E element, final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, cancelException, equalsElement(element),
                replacementMapper(replacement)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapLastWhereListAsyncMaterializer<E>(materializer, equalsElement(element),
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, cancelException,
                Require.notNull(predicate, "predicate"), replacementMapper(replacement)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapLastWhereListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), replacementMapper(replacement), context,
              cancelException, List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceLastWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMapLastWhere(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate")),
                replacementMapper(replacement)));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new MapLastWhereListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")),
              replacementMapper(replacement), context, cancelException,
              List.<E>replaceAfterFunction()));
    }

    @Override
    public @NotNull List<E> replaceSlice(final int start, final int end,
        @NotNull final Iterable<? extends E> patch) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
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
          final ExecutionContext context = this.context;
          final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
          return new List<E>(context, cancelException,
              getElementsMaterializer(context, Require.notNull(patch, "patch")));
        }
      }
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(patch, "patch"));
      if (elementsMaterializer.knownSize() == 0) {
        return removeSlice(start, end);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce() && !isFuture(patch)) {
        return new List<E>(context, cancelException,
            lazyMaterializerReplaceSlice(materializer, cancelException, start, end, patch));
      }
      return new List<E>(context, cancelException,
          new ReplaceSliceListAsyncMaterializer<E>(materializer, start, end, elementsMaterializer,
              context, cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> replaceWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, cancelException,
                filteredMapper(Require.notNull(predicate, "predicate"),
                    replacementMapper(replacement))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException, new MapListAsyncMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"), replacementMapper(replacement)),
          context, cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> replaceWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerMap(materializer, cancelException,
                filteredMapper(Require.notNull(predicate, "predicate"),
                    replacementMapper(replacement))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException, new MapListAsyncMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"), replacementMapper(replacement)),
          context, cancelException, List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> resizeTo(final int numElements, final E padding) {
      Require.notNegative(numElements, "numElements");
      if (numElements == 0) {
        return emptyList(context);
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize >= 0) {
        if (knownSize == 0) {
          final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
          return new List<E>(context, cancelException,
              new ListToListAsyncMaterializer<E>(lazy.List.times(numElements, padding), context));
        }
        if (knownSize == numElements) {
          final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
          return new List<E>(context, cancelException, materializer);
        }
        if (knownSize > numElements) {
          return take(numElements);
        }
        return appendAll(lazy.List.times(numElements - knownSize, padding));
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerResizeTo(materializer, cancelException, numElements, padding));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new ResizeListAsyncMaterializer<E>(materializer, numElements, padding, context,
              cancelException, List.<E>resizeFunction(), List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> reverse() {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerReverse(materializer, cancelException));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new ReverseListAsyncMaterializer<E>(materializer, context, cancelException,
              List.<E>reverseFunction()));
    }

    @Override
    public int size() {
      final BlockingConsumer<Integer> consumer = new BlockingConsumer<Integer>(cancelException);
      final ExecutionContext context = this.context;
      if (context.isCurrent()) {
        final ListAsyncMaterializer<E> materializer = this.materializer;
        if (!materializer.isDone()) {
          throw new DeadLockException("cannot wait on the future own execution context");
        }
        materializer.materializeElements(new AsyncConsumer<java.util.List<E>>() {
          @Override
          public void accept(final java.util.List<E> elements) {
            consumer.accept(elements.size());
          }

          @Override
          public void error(@NotNull final Exception error) {
            consumer.error(error);
          }
        });
      } else {
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
      if ((start == end) || (end >= 0 && start >= end)) {
        return emptyList(context);
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException, materializer);
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
            lazyMaterializerSlice(materializer, cancelException, start, end));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new SliceListAsyncMaterializer<E>(materializer, start, end, context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> sorted(@NotNull final Comparator<? super E> comparator) {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerSorted(materializer, cancelException,
                Require.notNull(comparator, "comparator")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new SortedListAsyncMaterializer<E>(materializer, comparator, context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<Boolean> startsWith(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final IteratorAsyncMaterializer<Object> elementsMaterializer = future.getElementsMaterializer(
          context, Require.notNull(elements, "elements"));
      if (elementsMaterializer.knownSize() == 0) {
        return trueList(context);
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return falseList(context);
      }
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new List<Boolean>(context, cancelException,
          new StartsWithListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
              cancelException, List.<Boolean>decorateFunction()));
    }

    // TODO: stopCancelPropagation + switchMap, mergeMap, concatMap(==flatMap) + flatMapAll(?)

    @Override
    public @NotNull List<E> symmetricDiff(@NotNull final Iterable<? extends E> elements) {
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        final ExecutionContext context = this.context;
        final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
        return new List<E>(context, cancelException,
            getElementsMaterializer(context, Require.notNull(elements, "elements")));
      }
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (elementsMaterializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerSymmetricDiff(materializer, cancelException,
                Require.notNull(elements, "elements")));
      }
      return new List<E>(context, cancelException,
          new SymmetricDiffListAsyncMaterializer<E>(materializer, elementsMaterializer, context,
              cancelException, List.<E>decorateFunction()));
    }

    public @NotNull List<E> switchTo(@NotNull final ExecutionContext context) {
      if (context.equals(this.context)) {
        return this;
      }
      return new List<E>(context, new AtomicReference<CancellationException>(),
          new SwitchListAsyncMaterializer<E>(this.context, taskID, context, materializer));
    }

    @Override
    public @NotNull List<E> take(final int maxElements) {
      if (maxElements <= 0) {
        return emptyList(context);
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTake(materializer, cancelException, maxElements));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new TakeListAsyncMaterializer<E>(materializer, maxElements, context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> takeRight(final int maxElements) {
      if (maxElements <= 0) {
        return emptyList(context);
      }
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeRight(materializer, cancelException, maxElements));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new TakeRightListAsyncMaterializer<E>(materializer, maxElements, context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> takeRightWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeRightWhile(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new TakeRightWhileListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> takeRightWhile(@NotNull Predicate<? super E> predicate) {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeRightWhile(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new TakeRightWhileListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> takeWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeWhile(materializer, cancelException,
                Require.notNull(predicate, "predicate")));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new TakeWhileListAsyncMaterializer<E>(materializer,
              Require.notNull(predicate, "predicate"), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> takeWhile(@NotNull final Predicate<? super E> predicate) {
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      final ListAsyncMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce()) {
        return new List<E>(context, cancelException,
            lazyMaterializerTakeWhile(materializer, cancelException,
                toIndexedPredicate(Require.notNull(predicate, "predicate"))));
      }
      final ExecutionContext context = this.context;
      return new List<E>(context, cancelException,
          new TakeWhileListAsyncMaterializer<E>(materializer,
              toIndexedPredicate(Require.notNull(predicate, "predicate")), context, cancelException,
              List.<E>decorateFunction()));
    }

    @Override
    public @NotNull List<E> union(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final ListAsyncMaterializer<E> materializer = this.materializer;
      final ListAsyncMaterializer<E> elementsMaterializer = getElementsMaterializer(context,
          Require.notNull(elements, "elements"));
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      if (materializer.knownSize() == 0) {
        return new List<E>(context, cancelException, elementsMaterializer);
      }
      if (elementsMaterializer.knownSize() == 0) {
        return new List<E>(context, cancelException, materializer);
      }
      if (materializer.isMaterializedAtOnce() && !isFuture(elements)) {
        return new List<E>(context, cancelException,
            lazyMaterializerUnion(materializer, cancelException, elements));
      }
      return new List<E>(context, cancelException,
          new AppendAllListAsyncMaterializer<E>(materializer,
              new DiffListAsyncMaterializer<E>(elementsMaterializer, materializer, context,
                  cancelException, List.<E>decorateFunction()), context, cancelException,
              List.<E>appendAllFunction()));
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
      protected int knownSize(@NotNull final java.util.List<F> elements) {
        return ((lazy.List<E>) elements).knownSize();
      }

      @Override
      @SuppressWarnings("unchecked")
      protected void materialize(@NotNull final java.util.List<F> elements) {
        ((lazy.List<E>) elements).materialized();
      }
    }

    private static class SuppliedMaterializer<E> extends AbstractListAsyncMaterializer<E> {

      private static final Logger LOGGER = Logger.getLogger(SuppliedMaterializer.class.getName());

      public SuppliedMaterializer(@NotNull final Supplier<? extends Iterable<? extends E>> supplier,
          @NotNull final ExecutionContext context, @NotNull final String taskID,
          @NotNull final AtomicReference<CancellationException> cancelException) {
        super(new AtomicInteger(STATUS_RUNNING));
        setState(new ImmaterialState(supplier, context, cancelException));
      }

      @Override
      public int knownSize() {
        return -1;
      }

      private class ImmaterialState implements ListAsyncMaterializer<E> {

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
            @NotNull final AsyncConsumer<Boolean> consumer) {
          materialized().materializeContains(element, consumer);
        }

        @Override
        public void materializeDone(@NotNull final AsyncConsumer<java.util.List<E>> consumer) {
          safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
        }

        @Override
        public void materializeElement(final int index,
            @NotNull final IndexedAsyncConsumer<E> consumer) {
          if (index < 0) {
            safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)),
                LOGGER);
          } else {
            materialized().materializeElement(index, consumer);
          }
        }

        @Override
        public void materializeElements(@NotNull final AsyncConsumer<java.util.List<E>> consumer) {
          materialized().materializeElements(consumer);
        }

        @Override
        public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
          materialized().materializeEmpty(consumer);
        }

        @Override
        public void materializeHasElement(final int index,
            @NotNull final AsyncConsumer<Boolean> consumer) {
          materialized().materializeHasElement(index, consumer);
        }

        @Override
        public void materializeNextWhile(final int index,
            @NotNull final IndexedAsyncPredicate<E> predicate) {
          materialized().materializeNextWhile(index, predicate);
        }

        @Override
        public void materializePrevWhile(final int index,
            @NotNull final IndexedAsyncPredicate<E> predicate) {
          materialized().materializePrevWhile(index, predicate);
        }

        @Override
        public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
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

        private @NotNull ListAsyncMaterializer<E> materialized() {
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

    private class IndexOfElementAsyncPredicate implements IndexedAsyncPredicate<E>, Task {

      private final AsyncConsumer<Integer> consumer;
      private final Object element;

      private IndexOfElementAsyncPredicate(@NotNull final Object element,
          @NotNull final AsyncConsumer<Integer> consumer) {
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
      public void run() {
        materializer.materializeNextWhile(0, this);
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
    }

    private class IndexOfNullAsyncPredicate implements IndexedAsyncPredicate<E>, Task {

      private final AsyncConsumer<Integer> consumer;

      private IndexOfNullAsyncPredicate(@NotNull final AsyncConsumer<Integer> consumer) {
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
      public void run() {
        materializer.materializeNextWhile(0, this);
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
    }

    private class LastIndexOfElementAsyncPredicate implements IndexedAsyncPredicate<E>, Task {

      private final AsyncConsumer<Integer> consumer;
      private final Object element;
      private final int index;

      private LastIndexOfElementAsyncPredicate(@NotNull final Object element, final int size,
          @NotNull final AsyncConsumer<Integer> consumer) {
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
      public void run() {
        materializer.materializePrevWhile(index, this);
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
    }

    private class LastIndexOfNullAsyncPredicate implements IndexedAsyncPredicate<E>, Task {

      private final AsyncConsumer<Integer> consumer;
      private final int index;

      private LastIndexOfNullAsyncPredicate(final int size,
          @NotNull final AsyncConsumer<Integer> consumer) {
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
      public void run() {
        materializer.materializePrevWhile(index, this);
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
    }
  }

  public static class ListIterator<E> implements itf.Future<E, lazy.ListIterator<E>>,
      itf.ListIterator<E> {

    private static final Function<Integer, Integer> INDEX_IDENTITY = new Function<Integer, Integer>() {
      @Override
      public Integer apply(final Integer param) {
        return param;
      }
    };
    private static final Function<? extends List<?>, ? extends ListIterator<?>> LIST_TO_ITERATOR = new Function<List<?>, ListIterator<?>>() {
      @Override
      public ListIterator<?> apply(final List<?> param) {
        return param.listIterator();
      }
    };

    private final ExecutionContext context;
    private final List<E> left;
    private final List<E> right;
    private final Object posMutex = new Object();

    private int pos;

    ListIterator(@NotNull final ExecutionContext context, @NotNull final List<E> left,
        @NotNull final List<E> right) {
      this.context = context;
      this.left = left;
      this.right = right;
    }

    ListIterator(@NotNull final ExecutionContext context, @NotNull final List<E> left,
        @NotNull final List<E> right, final int pos) {
      this(context, left, right);
      this.pos = pos;
    }

    private static @NotNull <E> IndexedConsumer<E> offsetConsumer(final long offset,
        @NotNull final IndexedConsumer<E> consumer) {
      if (offset == 0) {
        return consumer;
      }
      return new IndexedConsumer<E>() {
        @Override
        public void accept(int index, E param) throws Exception {
          consumer.accept(IndexOverflowException.safeCast(offset + index), param);
        }
      };
    }

    private static @NotNull <E, F> IndexedFunction<E, F> offsetFunction(final long offset,
        @NotNull final IndexedFunction<E, F> function) {
      if (offset == 0) {
        return function;
      }
      return new IndexedFunction<E, F>() {
        @Override
        public F apply(final int index, final E param) throws Exception {
          return function.apply(IndexOverflowException.safeCast(offset + index), param);
        }
      };
    }

    private static @NotNull <E> IndexedPredicate<E> offsetPredicate(final long offset,
        @NotNull final IndexedPredicate<E> predicate) {
      if (offset == 0) {
        return predicate;
      }
      return new IndexedPredicate<E>() {
        @Override
        public boolean test(final int index, final E param) throws Exception {
          return predicate.test(IndexOverflowException.safeCast(offset + index), param);
        }
      };
    }

    private static @NotNull Function<Integer, Integer> offsetMapper(final long offset) {
      if (offset == 0) {
        return INDEX_IDENTITY;
      }
      return new Function<Integer, Integer>() {
        @Override
        public Integer apply(final Integer param) {
          return IndexOverflowException.safeCast(offset + param);
        }
      };
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
      return new ListIterator<E>(context, left, right.append(element), safePos());
    }

    @Override
    public @NotNull ListIterator<E> appendAll(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(context, left, right.appendAll(elements), safePos());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <F> ListIterator<F> as() {
      return (ListIterator<F>) this;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public @NotNull ListIterator<Integer> count() {
      if (atEnd()) {
        return zeroIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().count());
    }

    @Override
    public @NotNull ListIterator<Integer> countWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return zeroIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().countWhere(
              offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<Integer> countWhere(
        @NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return zeroIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().countWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> diff(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final List<?> elementsList = new List<Object>(context,
          new AtomicReference<CancellationException>(),
          List.getElementsMaterializer(context, Require.notNull(elements, "elements")));
      final List<E> left = currentLeft();
      return new ListIterator<E>(context, left.diff(elementsList),
          currentRight().diff(elementsList.diff(left)));
    }

    @Override
    public void doFor(@NotNull final Consumer<? super E> consumer) {
      if (!atEnd()) {
        currentRight().doFor(consumer);
      }
    }

    @Override
    public void doFor(@NotNull final IndexedConsumer<? super E> consumer) {
      if (!atEnd()) {
        currentRight().doFor(offsetConsumer(nextIndex(), Require.notNull(consumer, "consumer")));
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      if (!atEnd()) {
        currentRight().doWhile(
            offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")));
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      if (!atEnd()) {
        final int offset = nextIndex();
        currentRight().doWhile(offsetPredicate(offset, Require.notNull(condition, "condition")),
            offsetConsumer(offset, Require.notNull(consumer, "consumer")));
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> predicate) {
      if (!atEnd()) {
        currentRight().doWhile(predicate);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      if (!atEnd()) {
        currentRight().doWhile(condition, consumer);
      }
    }

    @Override
    public @NotNull ListIterator<E> drop(final int maxElements) {
      if (maxElements <= 0 || atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().drop(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> dropRight(final int maxElements) {
      if (maxElements <= 0 || atEnd()) {
        return copyIterator();
      }
      final int pos = safePos();
      final List<E> right = this.right;
      final int knownSize = right.materializer.knownSize();
      if (knownSize >= 0 && knownSize - Math.max(0, pos) >= maxElements) {
        new ListIterator<E>(context, left, right.dropRight(maxElements), pos);
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().dropRight(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> dropRightWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().dropRightWhile(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().dropRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> dropWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().dropWhile(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().dropWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> each(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return falseIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().each(
              offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<Boolean> each(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return falseIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().each(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> endsWith(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().endsWith(elements));
    }

    @Override
    public @NotNull ListIterator<Boolean> exists(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return falseIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().exists(
              offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return falseIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().exists(predicate));
    }

    @Override
    public @NotNull ListIterator<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft().filter(predicate),
          currentRight().filter(offsetPredicate(nextIndex(), predicate)));
    }

    @Override
    public @NotNull ListIterator<E> filter(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft().filter(predicate),
          currentRight().filter(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findAny(@NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context), currentRight().findAny(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> findAny(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().findAny(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findFirst(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context), currentRight().findFirst(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> findFirst(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().findFirst(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexOf(final Object element) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().findIndexOf(element).map(offsetMapper(nextIndex())));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().findIndexOfSlice(elements).map(offsetMapper(nextIndex())));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().findIndexWhere(
                  offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")))
              .map(offsetMapper(nextIndex())));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().findIndexWhere(predicate).map(offsetMapper(nextIndex())));
    }

    @Override
    public @NotNull ListIterator<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context), currentRight().findLast(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> findLast(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().findLast(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexOf(final Object element) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().findLastIndexOf(element).map(offsetMapper(nextIndex())));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexOfSlice(
        @NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().findLastIndexOfSlice(elements).map(offsetMapper(nextIndex())));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().findLastIndexWhere(
                  offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")))
              .map(offsetMapper(nextIndex())));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Integer>(context, List.<Integer>emptyList(context),
          currentRight().findLastIndexWhere(predicate).map(offsetMapper(nextIndex())));
    }

    @Override
    public E first() {
      final int pos = safePos();
      if (pos >= 0) {
        return right.get(pos);
      }
      final List<E> left = this.left;
      return left.get(left.size() + pos);
    }

    @Override
    public @NotNull <F> ListIterator<F> flatMap(
        @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
      return new ListIterator<F>(context, currentLeft().flatMap(mapper),
          currentRight().flatMap(mapper));
    }

    @Override
    public @NotNull <F> ListIterator<F> flatMap(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      return new ListIterator<F>(context, currentLeft().flatMap(mapper),
          currentRight().flatMap(offsetFunction(nextIndex(), mapper)));
    }

    @Override
    public @NotNull ListIterator<E> flatMapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
        return this;
      }
      final int pos = safePos();
      if (pos >= 0) {
        return new ListIterator<E>(context, left,
            right.flatMapAfter(SizeOverflowException.safeCast((long) numElements + pos), mapper));
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().flatMapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
        return this;
      }
      final int pos = safePos();
      if (pos >= 0) {
        return new ListIterator<E>(context, left,
            right.flatMapAfter(SizeOverflowException.safeCast((long) numElements + pos),
                offsetFunction(nextIndex(), Require.notNull(mapper, "mapper"))));
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().flatMapAfter(numElements,
          offsetFunction(nextIndex(), Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull ListIterator<E> flatMapFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return this;
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().flatMapFirstWhere(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")),
          offsetFunction(nextIndex(), Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull ListIterator<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return this;
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().flatMapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return this;
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().flatMapLastWhere(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")),
          offsetFunction(nextIndex(), Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull ListIterator<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return this;
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().flatMapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return this;
      }
      return new ListIterator<E>(context, currentLeft().flatMapWhere(predicate, mapper),
          currentRight().flatMapWhere(
              offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")),
              offsetFunction(nextIndex(), mapper)));
    }

    @Override
    public @NotNull ListIterator<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return this;
      }
      return new ListIterator<E>(context, currentLeft().flatMapWhere(predicate, mapper),
          currentRight().flatMapWhere(predicate, mapper));
    }

    @Override
    public @NotNull <F> ListIterator<F> fold(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      if (atEnd()) {
        return elementIterator(identity);
      }
      final ExecutionContext context = this.context;
      return new ListIterator<F>(context, List.<F>emptyList(context),
          currentRight().fold(identity, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldLeft(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      if (atEnd()) {
        return elementIterator(identity);
      }
      final ExecutionContext context = this.context;
      return new ListIterator<F>(context, List.<F>emptyList(context),
          currentRight().foldLeft(identity, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldRight(final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      if (atEnd()) {
        return elementIterator(identity);
      }
      final ExecutionContext context = this.context;
      return new ListIterator<F>(context, List.<F>emptyList(context),
          currentRight().foldRight(identity, operation));
    }

    @Override
    public lazy.ListIterator<E> get() throws InterruptedException, ExecutionException {
      final int pos = safePos();
      return new lazy.ListIterator<E>(left.get(), right.get(), pos);
    }

    @Override
    public lazy.ListIterator<E> get(final long timeout, @NotNull final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      final long startTimeMillis = System.currentTimeMillis();
      final int pos = safePos();
      long timeoutMillis = unit.toMillis(timeout);
      final lazy.List<E> leftList = left.get(timeoutMillis, TimeUnit.MILLISECONDS);
      timeoutMillis -= startTimeMillis - System.currentTimeMillis();
      if (timeoutMillis <= 0) {
        throw new TimeoutException();
      }
      final lazy.List<E> rightList = right.get(timeoutMillis, TimeUnit.MILLISECONDS);
      return new lazy.ListIterator<E>(leftList, rightList, pos);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull ListIterator<? extends ListIterator<E>> group(final int maxSize) {
      return new ListIterator<ListIterator<E>>(context,
          currentLeft().group(maxSize).map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR),
          currentRight().group(maxSize).map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull ListIterator<? extends ListIterator<E>> groupWithPadding(final int size,
        final E padding) {
      return new ListIterator<ListIterator<E>>(context,
          currentLeft().groupWithPadding(size, padding)
              .map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR),
          currentRight().groupWithPadding(size, padding)
              .map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR));
    }

    @Override
    public boolean hasNext() {
      final int pos = safePos();
      if (pos >= 0) {
        final AtomicReference<CancellationException> cancelException = right.cancelException;
        final BlockingConsumer<Boolean> consumer = new BlockingConsumer<Boolean>(cancelException);
        final ExecutionContext context = this.context;
        final ListAsyncMaterializer<E> materializer = right.materializer;
        if (context.isCurrent()) {
          if (!materializer.isDone()) {
            throw new DeadLockException("cannot wait on the future own execution context");
          }
          materializer.materializeElements(new AsyncConsumer<java.util.List<E>>() {
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
          final String taskID = right.taskID;
          context.scheduleAfter(new Task() {
            @Override
            public void run() {
              try {
                materializer.materializeHasElement(pos, consumer);
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
              return materializer.weightHasElement();
            }
          });
        }
        try {
          return consumer.get();
        } catch (final InterruptedException e) {
          throw UncheckedException.toUnchecked(e);
        }
      }
      return true;
    }

    @Override
    public boolean hasPrevious() {
      final int pos = safePos();
      if (pos == 0) {
        return !left.isEmpty();
      }
      if (pos < 0) {
        return left.size() + pos >= 0;
      }
      return true;
    }

    @Override
    public @NotNull ListIterator<Boolean> includes(final Object element) {
      if (atEnd()) {
        return falseIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().includes(element));
    }

    @Override
    public @NotNull ListIterator<Boolean> includesAll(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().includesAll(elements));
    }

    @Override
    public @NotNull ListIterator<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().includesSlice(elements));
    }

    @Override
    public @NotNull ListIterator<E> insert(final E element) {
      final int pos = this.pos;
      if (pos >= 0) {
        return new ListIterator<E>(context, left, right.insertAfter(pos, element), pos);
      }
      return new ListIterator<E>(context, left.insertAfter(nextIndex(), element), right, pos);
    }

    @Override
    public @NotNull ListIterator<E> insertAfter(final int numElements, final E element) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
        return this;
      }
      final int pos = safePos();
      if (pos >= 0) {
        return new ListIterator<E>(context, left,
            right.insertAfter(SizeOverflowException.safeCast((long) numElements + pos), element));
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().insertAfter(numElements, element));
    }

    @Override
    public @NotNull ListIterator<E> insertAll(@NotNull final Iterable<? extends E> elements) {
      final int pos = safePos();
      if (pos >= 0) {
        return new ListIterator<E>(context, left, right.insertAllAfter(pos, elements), pos);
      }
      return new ListIterator<E>(context, left.insertAllAfter(nextIndex(), elements), right, pos);
    }

    @Override
    public @NotNull ListIterator<E> insertAllAfter(final int numElements,
        @NotNull final Iterable<? extends E> elements) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
        return this;
      }
      final int pos = safePos();
      if (pos >= 0) {
        return new ListIterator<E>(context, left,
            right.insertAllAfter(SizeOverflowException.safeCast((long) numElements + pos),
                elements));
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().insertAllAfter(numElements, elements));
    }

    @Override
    public @NotNull ListIterator<E> intersect(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      final List<?> elementsList = new List<Object>(context,
          new AtomicReference<CancellationException>(),
          List.getElementsMaterializer(context, Require.notNull(elements, "elements")));
      final List<E> left = currentLeft();
      return new ListIterator<E>(context, left.intersect(elementsList),
          currentRight().intersect(elementsList.diff(left)));
    }

    @Override
    public boolean isDone() {
      return left.isDone() && right.isDone();
    }

    @Override
    public boolean isCancelled() {
      return left.isCancelled() && right.isCancelled();
    }

    @Override
    public boolean isFailed() {
      return left.isFailed() || right.isFailed() || (left.isCancelled() && right.isDone()) || (
          right.isCancelled() && left.isDone());
    }

    @Override
    public boolean isSucceeded() {
      return left.isSucceeded() && right.isSucceeded();
    }

    @Override
    public @NotNull lazy.Iterator<E> iterator() {
      // TODO: future.Iterator
      if (atEnd()) {
        return lazy.Iterator.of();
      }
      return currentRight().iterator();
    }

    @Override
    public boolean isEmpty() {
      return left.isEmpty() && right.isEmpty();
    }

    @Override
    public E last() {
      final List<E> right = this.right;
      return right.isEmpty() ? left.last() : right.last();
    }

    @Override
    public @NotNull <F> ListIterator<F> map(@NotNull final Function<? super E, F> mapper) {
      return new ListIterator<F>(context, currentLeft().map(mapper), currentRight().map(mapper));
    }

    @Override
    public @NotNull <F> ListIterator<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
      return new ListIterator<F>(context, currentLeft().map(mapper),
          currentRight().map(offsetFunction(nextIndex(), mapper)));
    }

    @Override
    public @NotNull ListIterator<E> mapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends E> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().mapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().mapAfter(numElements,
          offsetFunction(nextIndex(), Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull ListIterator<E> mapFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().mapFirstWhere(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")),
          offsetFunction(nextIndex(), Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull ListIterator<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().mapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().mapLastWhere(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")),
          offsetFunction(nextIndex(), Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull ListIterator<E> mapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().mapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final List<E> left = this.left;
      final int offset = left.size();
      return new ListIterator<E>(context, left.mapWhere(predicate, mapper),
          right.mapWhere(offsetPredicate(offset, predicate), offsetFunction(offset, mapper)), pos);
    }

    @Override
    public @NotNull ListIterator<E> mapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      return new ListIterator<E>(context, left.mapWhere(predicate, mapper),
          right.mapWhere(predicate, mapper), safePos());
    }

    @Override
    public @NotNull ListIterator<E> max(@NotNull final Comparator<? super E> comparator) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().max(comparator));
    }

    @Override
    public @NotNull ListIterator<E> min(@NotNull final Comparator<? super E> comparator) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().min(comparator));
    }

    @Override
    public @NotNull ListIterator<E> moveBy(final int maxElements) {
      if (maxElements == 0) {
        return this;
      }
      final long pos = safePos();
      final long newPos = pos + maxElements;
      if (newPos >= Integer.MAX_VALUE || newPos <= Integer.MIN_VALUE) {
        throw new IndexOverflowException(newPos);
      }
      if ((newPos >= 0 && newPos < pos) || (newPos <= 0 && newPos > pos)) {
        return new ListIterator<E>(context, left, right, (int) newPos);
      }
      if (newPos >= 0) {
        final int knownSize = right.materializer.knownSize();
        if (newPos <= knownSize) {
          return new ListIterator<E>(context, left, right, (int) newPos);
        }
      } else {
        final int knownSize = left.materializer.knownSize();
        if (-newPos <= knownSize) {
          return new ListIterator<E>(context, left, right, (int) newPos);
        }
      }
      final List<E> newLeft;
      if (newPos == 0) {
        newLeft = left;
      } else if (newPos > 0) {
        newLeft = left.appendAll(right.take((int) newPos));
      } else {
        final int knownSize = left.materializer.knownSize();
        if (knownSize >= 0) {
          newLeft = left.take((int) (knownSize + newPos));
        } else {
          newLeft = left.dropRight((int) -newPos);
        }
      }
      final List<E> newRight;
      if (newPos == 0) {
        newRight = right;
      } else if (newPos > 0) {
        newRight = right.drop((int) newPos);
      } else {
        final int knownSize = left.materializer.knownSize();
        if (knownSize >= 0) {
          newRight = left.drop((int) (knownSize + newPos)).appendAll(right);
        } else {
          newRight = left.takeRight((int) -newPos).appendAll(right);
        }
      }
      return new ListIterator<E>(context, newLeft, newRight);
    }

    @Override
    public @NotNull ListIterator<E> moveTo(final int index) {
      Require.notNegative(index, "index");
      int knownSize = left.materializer.knownSize();
      if (knownSize >= 0) {
        final int newPos = index - knownSize;
        if (newPos < 0) {
          return new ListIterator<E>(context, left, right, newPos);
        }
        knownSize = right.materializer.knownSize();
        if (knownSize >= 0 && newPos <= knownSize) {
          return new ListIterator<E>(context, left, right, newPos);
        }
      }
      return new ListIterator<E>(context, left.count().flatMap(new Function<Integer, List<E>>() {
        @Override
        public List<E> apply(final Integer size) {
          final int newPos = index - size;
          if (newPos == 0) {
            return left;
          } else if (newPos > 0) {
            return left.appendAll(right.take(newPos));
          } else {
            return left.take(size + newPos);
          }
        }
      }), left.count().flatMap(new Function<Integer, List<E>>() {
        @Override
        public List<E> apply(final Integer size) {
          final int newPos = index - size;
          if (newPos == 0) {
            return right;
          } else if (newPos > 0) {
            return right.drop(newPos);
          } else {
            return left.drop(size + newPos).appendAll(right);
          }
        }
      }));
    }

    @Override
    public E next() {
      try {
        final int index = safeGetAndIncPos();
        return index < 0 ? left.get(left.size() + index) : right.get(index);
      } catch (final IndexOutOfBoundsException ignored) {
        // FIXME: where the exception come from?
        throw new NoSuchElementException();
      }
    }

    @Override
    public int nextIndex() {
      return IndexOverflowException.safeCast((long) left.size() + safePos());
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final Consumer<? super E> consumer) {
      return currentRight().nonBlockingFor(consumer);
    }

    @Override
    public @NotNull Future<?> nonBlockingFor(@NotNull final IndexedConsumer<? super E> consumer) {
      return currentRight().nonBlockingFor(consumer);
    }

    @Override
    public @NotNull Future<?> nonBlockingGet() {
      return left.appendAll(right).nonBlockingGet();
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      return currentRight().nonBlockingWhile(predicate);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      return currentRight().nonBlockingWhile(condition, consumer);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> predicate) {
      return currentRight().nonBlockingWhile(predicate);
    }

    @Override
    public @NotNull Future<?> nonBlockingWhile(@NotNull final Predicate<? super E> condition,
        @NotNull Consumer<? super E> consumer) {
      return currentRight().nonBlockingWhile(condition, consumer);
    }

    @Override
    public @NotNull ListIterator<Boolean> none(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return trueIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().none(
              offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<Boolean> none(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return trueIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().none(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> notAll(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return trueIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().notAll(
              offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return trueIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().notAll(predicate));
    }

    @Override
    public boolean notEmpty() {
      return !left.isEmpty() || !right.isEmpty();
    }

    @Override
    public @NotNull ListIterator<E> orElse(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      if (atEnd()) {
        return new ListIterator<E>(context, List.<E>emptyList(context),
            new List<E>(context, new AtomicReference<CancellationException>(),
                List.getElementsMaterializer(context, Require.notNull(elements, "elements"))));
      }
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().orElse(elements));
    }

    @Override
    public @NotNull ListIterator<E> orElseGet(
        @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().orElseGet(supplier));
    }

    @Override
    public @NotNull ListIterator<E> plus(final E element) {
      return new ListIterator<E>(context, left, right.plus(element), safePos());
    }

    @Override
    public @NotNull ListIterator<E> plusAll(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(context, left, right.plusAll(elements), safePos());
    }

    @Override
    public @NotNull ListIterator<E> reduce(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().reduce(operation));
    }

    @Override
    public @NotNull ListIterator<E> reduceLeft(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().reduceLeft(operation));
    }

    @Override
    public @NotNull ListIterator<E> reduceRight(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().reduceRight(operation));
    }

    @Override
    public E previous() {
      try {
        final int index = safeDecAndGetPos();
        return index < 0 ? left.get(left.size() + index) : right.get(index);
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
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ListIterator<E> removeAfter(final int numElements) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().removeAfter(numElements));
    }

    @Override
    public @NotNull ListIterator<E> removeEach(final E element) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft().removeEach(element),
          currentRight().removeEach(element));
    }

    @Override
    public @NotNull ListIterator<E> removeFirst(final E element) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().removeFirst(element));
    }

    @Override
    public @NotNull ListIterator<E> removeFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().removeFirstWhere(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> removeFirstWhere(
        @NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().removeFirstWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeLast(final E element) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().removeLast(element));
    }

    @Override
    public @NotNull ListIterator<E> removeLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().removeLastWhere(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> removeLastWhere(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().removeLastWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeSlice(final int start, final int end) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().removeSlice(start, end));
    }

    @Override
    public @NotNull ListIterator<E> removeWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().removeWhere(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().removeWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> replaceAfter(final int numElements, final E replacement) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().replaceAfter(numElements, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceEach(final E element, final E replacement) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft().replaceEach(element, replacement),
          currentRight().replaceEach(element, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceFirst(final E element, final E replacement) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().replaceFirst(element, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().replaceFirstWhere(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")), replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceFirstWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().replaceFirstWhere(Require.notNull(predicate, "predicate"), replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceLast(final E element, final E replacement) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().replaceLast(element, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().replaceLastWhere(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate")), replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceLastWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().replaceLastWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceSlice(final int start, final int end,
        @NotNull final Iterable<? extends E> patch) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().replaceSlice(start, end, patch));
    }

    @Override
    public @NotNull ListIterator<E> replaceWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft().replaceWhere(predicate, replacement),
          currentRight().replaceWhere(offsetPredicate(nextIndex(), predicate), replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      if (atEnd()) {
        return copyIterator();
      }
      return new ListIterator<E>(context, currentLeft().replaceWhere(predicate, replacement),
          currentRight().replaceWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> resizeTo(final int numElements, final E padding) {
      Require.notNegative(numElements, "numElements");
      if (atEnd()) {
        return appendAll(lazy.List.times(numElements, padding));
      }
      return new ListIterator<E>(context, currentLeft(),
          currentRight().resizeTo(numElements, padding));
    }

    @Override
    public @NotNull ListIterator<E> reverse() {
      if (atEnd()) {
        final ExecutionContext context = this.context;
        return new ListIterator<E>(context, left.appendAll(right).reverse(),
            List.<E>emptyList(context));
      }
      return new ListIterator<E>(context, currentRight().reverse(), currentLeft().reverse());
    }

    @Override
    public void set(final E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
      return SizeOverflowException.safeCast((long) right.size() - safePos());
    }

    @Override
    public @NotNull ListIterator<E> slice(final int start) {
      return slice(start, Integer.MAX_VALUE);
    }

    @Override
    public @NotNull ListIterator<E> slice(final int start, final int end) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().slice(start, end));
    }

    @Override
    public @NotNull ListIterator<Boolean> startsWith(@NotNull final Iterable<?> elements) {
      final ExecutionContext context = this.context;
      return new ListIterator<Boolean>(context, List.<Boolean>emptyList(context),
          currentRight().startsWith(elements));
    }

    @Override
    public @NotNull ListIterator<E> symmetricDiff(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      final List<E> elementsList = new List<E>(context,
          new AtomicReference<CancellationException>(),
          List.getElementsMaterializer(context, Require.notNull(elements, "elements")));
      final List<E> left = currentLeft();
      return new ListIterator<E>(context, left.symmetricDiff(elementsList),
          currentRight().symmetricDiff(elementsList.diff(left)));
    }

    @Override
    public @NotNull ListIterator<E> take(final int maxElements) {
      if (atEnd()) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().take(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> takeRight(final int maxElements) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().takeRight(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> takeRightWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context), currentRight().takeRightWhile(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> takeRightWhile(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          currentRight().takeRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> takeWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().takeWhile(
          offsetPredicate(nextIndex(), Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull ListIterator<E> takeWhile(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return emptyIterator();
      }
      return new ListIterator<E>(context, currentLeft(), currentRight().takeWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> union(@NotNull final Iterable<? extends E> elements) {
      final ExecutionContext context = this.context;
      return new ListIterator<E>(context, List.<E>emptyList(context),
          left.appendAll(right).union(elements), nextIndex());
    }

    private boolean atEnd() {
      final int pos = safePos();
      return pos >= 0 && pos == right.materializer.knownSize();
    }

    private @NotNull List<E> currentLeft() {
      final int pos = safePos();
      if (pos == 0) {
        return left;
      } else if (pos > 0) {
        return left.appendAll(right.take(pos));
      }
      return left.dropRight(-pos);
    }

    private @NotNull List<E> currentRight() {
      final int pos = safePos();
      if (pos == 0) {
        return right;
      } else if (pos > 0) {
        return right.drop(pos);
      }
      return left.takeRight(-pos).appendAll(right);
    }

    private @NotNull ListIterator<E> copyIterator() {
      return new ListIterator<E>(context, left, right, safePos());
    }

    private @NotNull <T> ListIterator<T> elementIterator(final T element) {
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new ListIterator<T>(context,
          new List<T>(context, cancelException, EmptyListAsyncMaterializer.<T>instance()),
          new List<T>(context, cancelException,
              new ElementToListAsyncMaterializer<T>(lazy.List.of(element))));
    }

    private @NotNull <T> ListIterator<T> emptyIterator() {
      final ExecutionContext context = this.context;
      final List<T> list = List.emptyList(context);
      return new ListIterator<T>(context, list, list);
    }

    private @NotNull ListIterator<Boolean> falseIterator() {
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new ListIterator<Boolean>(context, new List<Boolean>(context, cancelException,
          EmptyListAsyncMaterializer.<Boolean>instance()),
          new List<Boolean>(context, cancelException, List.FALSE_MATERIALIZER));
    }

    private @NotNull ListIterator<Boolean> trueIterator() {
      final ExecutionContext context = this.context;
      final AtomicReference<CancellationException> cancelException = new AtomicReference<CancellationException>();
      return new ListIterator<Boolean>(context, new List<Boolean>(context, cancelException,
          EmptyListAsyncMaterializer.<Boolean>instance()),
          new List<Boolean>(context, cancelException, List.TRUE_MATERIALIZER));
    }

    private int safeDecAndGetPos() {
      synchronized (posMutex) {
        return --pos;
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
      return new ListIterator<Integer>(context, new List<Integer>(context, cancelException,
          EmptyListAsyncMaterializer.<Integer>instance()),
          new List<Integer>(context, cancelException, List.ZERO_MATERIALIZER));
    }
  }

  private static class BlockingConsumer<P> implements AsyncConsumer<P> {

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

  private static class BlockingElementConsumer<P> implements IndexedAsyncConsumer<P> {

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
    public boolean isSucceeded() {
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
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      safeConsumeComplete(predicate, 0, LOGGER);
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      safeConsumeComplete(predicate, 0, LOGGER);
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
    public int weightHasElement() {
      return 1;
    }

    @Override
    public int weightNextWhile() {
      return 1;
    }

    @Override
    public int weightPrevWhile() {
      return 1;
    }

    @Override
    public int weightSize() {
      return 1;
    }
  }
}
