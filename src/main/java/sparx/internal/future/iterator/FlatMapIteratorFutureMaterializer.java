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
package sparx.internal.future.iterator;

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;
import sparx.util.function.IndexedFunction;

public class FlatMapIteratorFutureMaterializer<E, F> extends AbstractIteratorFutureMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapIteratorFutureMaterializer.class.getName());

  public FlatMapIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<F>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, mapper, context, cancelException));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<F>>> elementsConsumers = new ArrayList<FutureConsumer<List<F>>>(
        2);
    private final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<F>> mapper;
    private final IteratorFutureMaterializer<E> wrapped;

    private IteratorFutureMaterializer<F> elementsMaterializer;
    private ArrayList<FutureConsumer<IteratorFutureMaterializer<F>>> elementsMaterializerConsumers;
    private int index;
    private int wrappedIndex;

    private ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<F>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.mapper = mapper;
      this.context = context;
      this.cancelException = cancelException;
      initConsumers();
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
      final IteratorFutureMaterializer<F> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeCancel(exception);
      }
      wrapped.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        new MaterializingFutureConsumer().run();
      }
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      new HasNextFutureConsumer(consumer).run();
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<F>> consumer) {
      materializeElements(new CancellableFutureConsumer<List<F>>() {
        @Override
        public void cancellableAccept(final List<F> elements) {
          getState().materializeIterator(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<F> consumer) {
      new NextFutureConsumer(consumer).run();
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<F> predicate) {
      new NextWhileFutureConsumer(predicate).run();
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      new SkipFutureConsumer(count, consumer).run();
    }

    @Override
    public int weightElements() {
      if (elementsConsumers.isEmpty()) {
        final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
        return elementsMaterializer != null ? elementsMaterializer.weightNextWhile()
            : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
      }
      return 1;
    }

    @Override
    public int weightHasNext() {
      final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
      return elementsMaterializer != null ? elementsMaterializer.weightHasNext()
          : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
    }

    @Override
    public int weightNext() {
      final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
      return elementsMaterializer != null ? elementsMaterializer.weightNext()
          : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
    }

    @Override
    public int weightNextWhile() {
      final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
      return elementsMaterializer != null ? elementsMaterializer.weightNextWhile()
          : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
    }

    @Override
    public int weightSkip() {
      final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
      return elementsMaterializer != null ? elementsMaterializer.weightSkip()
          : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
    }

    private void consumeElements(@NotNull final List<F> elements) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<F>> consumer : elementsConsumers) {
        safeConsume(consumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<F>> consumer : elementsConsumers) {
        safeConsumeError(consumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void initConsumers() {
      elementsMaterializerConsumers = new ArrayList<FutureConsumer<IteratorFutureMaterializer<F>>>(
          2);
    }

    private void materializeElementsMaterializer(
        @NotNull final FutureConsumer<IteratorFutureMaterializer<F>> consumer) {
      final ArrayList<FutureConsumer<IteratorFutureMaterializer<F>>> materializerConsumers = this.elementsMaterializerConsumers;
      materializerConsumers.add(consumer);
      if (materializerConsumers.size() == 1) {
        wrapped.materializeNext(new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            elementsMaterializer = mapper.apply(wrappedIndex++, element);
            // Must be reentrant
            initConsumers();
            for (final FutureConsumer<IteratorFutureMaterializer<F>> materializerConsumer : materializerConsumers) {
              safeConsume(materializerConsumer, elementsMaterializer, LOGGER);
            }
          }

          @Override
          public void cancellableComplete(final int size) {
            // Must be reentrant
            initConsumers();
            for (final FutureConsumer<IteratorFutureMaterializer<F>> materializerConsumer : materializerConsumers) {
              safeConsume(materializerConsumer, null, LOGGER);
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            // Must be reentrant
            initConsumers();
            for (final FutureConsumer<IteratorFutureMaterializer<F>> materializerConsumer : materializerConsumers) {
              safeConsumeError(materializerConsumer, error, LOGGER);
            }
          }
        });
      }
    }

    private void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        consumeError(exception);
      } else {
        setFailed(error);
        consumeError(error);
      }
    }

    private class HasNextFutureConsumer extends CancellableFutureConsumer<Boolean> {

      private final FutureConsumer<Boolean> consumer;

      private String taskID;

      private final CancellableFutureConsumer<IteratorFutureMaterializer<F>> elementsMaterializerConsumer = new CancellableFutureConsumer<IteratorFutureMaterializer<F>>() {
        @Override
        public void cancellableAccept(final IteratorFutureMaterializer<F> materializer)
            throws Exception {
          if (materializer == null) {
            consumer.accept(false);
          } else {
            materializer.materializeHasNext(HasNextFutureConsumer.this);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      };

      private HasNextFutureConsumer(@NotNull final FutureConsumer<Boolean> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final Boolean hasNext) throws Exception {
        if (hasNext) {
          consumer.accept(true);
        } else {
          elementsMaterializer = null;
          schedule();
        }
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
      public int weight() {
        final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
        return elementsMaterializer != null ? elementsMaterializer.weightHasNext()
            : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
      }

      @Override
      protected void runWithContext() {
        if (elementsMaterializer != null) {
          elementsMaterializer.materializeHasNext(this);
        } else {
          materializeElementsMaterializer(elementsMaterializerConsumer);
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class MaterializingFutureConsumer extends CancellableIndexedFuturePredicate<F> {

      private final DequeueList<F> elements = new DequeueList<F>();

      private String taskID;

      private final CancellableFutureConsumer<IteratorFutureMaterializer<F>> elementsMaterializerConsumer = new CancellableFutureConsumer<IteratorFutureMaterializer<F>>() {
        @Override
        public void cancellableAccept(final IteratorFutureMaterializer<F> materializer) {
          if (materializer == null) {
            if (elements.isEmpty()) {
              setDone(EmptyIteratorFutureMaterializer.<F>instance());
              consumeElements(Collections.<F>emptyList());
            } else {
              setDone(new DequeueToIteratorFutureMaterializer<F>(elements, context, index));
              consumeElements(elements);
            }
          } else {
            materializer.materializeNextWhile(MaterializingFutureConsumer.this);
          }
        }

        @Override
        public void error(@NotNull final Exception error) {
          setError(error);
        }
      };

      @Override
      public void cancellableComplete(final int size) {
        elementsMaterializer = null;
        schedule();
      }

      @Override
      public boolean cancellableTest(final int size, final int index, final F element) {
        elements.add(element);
        return true;
      }

      @Override
      public void error(@NotNull final Exception error) {
        setError(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
        return elementsMaterializer != null ? elementsMaterializer.weightNextWhile()
            : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
      }

      @Override
      protected void runWithContext() {
        if (elementsMaterializer != null) {
          elementsMaterializer.materializeNextWhile(this);
        } else {
          materializeElementsMaterializer(elementsMaterializerConsumer);
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class NextFutureConsumer extends CancellableIndexedFutureConsumer<F> {

      private final IndexedFutureConsumer<F> consumer;

      private String taskID;

      private final CancellableFutureConsumer<IteratorFutureMaterializer<F>> elementsMaterializerConsumer = new CancellableFutureConsumer<IteratorFutureMaterializer<F>>() {
        @Override
        public void cancellableAccept(final IteratorFutureMaterializer<F> materializer)
            throws Exception {
          if (materializer == null) {
            consumer.complete(0);
          } else {
            materializer.materializeNext(NextFutureConsumer.this);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      };

      private NextFutureConsumer(@NotNull final IndexedFutureConsumer<F> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final F element)
          throws Exception {
        consumer.accept(-1, ImmaterialState.this.index++, element);
      }

      @Override
      public void cancellableComplete(final int size) {
        elementsMaterializer = null;
        schedule();
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
      public int weight() {
        final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
        return elementsMaterializer != null ? elementsMaterializer.weightNext()
            : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
      }

      @Override
      protected void runWithContext() {
        if (elementsMaterializer != null) {
          elementsMaterializer.materializeNext(this);
        } else {
          materializeElementsMaterializer(elementsMaterializerConsumer);
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class NextWhileFutureConsumer extends CancellableIndexedFuturePredicate<F> {

      private final IndexedFuturePredicate<F> predicate;

      private String taskID;

      private final CancellableFutureConsumer<IteratorFutureMaterializer<F>> elementsMaterializerConsumer = new CancellableFutureConsumer<IteratorFutureMaterializer<F>>() {
        @Override
        public void cancellableAccept(final IteratorFutureMaterializer<F> materializer)
            throws Exception {
          if (materializer == null) {
            predicate.complete(0);
          } else {
            materializer.materializeNextWhile(NextWhileFutureConsumer.this);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      };

      private NextWhileFutureConsumer(@NotNull final IndexedFuturePredicate<F> predicate) {
        this.predicate = predicate;
      }

      @Override
      public void cancellableComplete(final int size) {
        elementsMaterializer = null;
        schedule();
      }

      @Override
      public boolean cancellableTest(final int size, final int index, final F element)
          throws Exception {
        return predicate.test(-1, ImmaterialState.this.index++, element);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        predicate.error(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
        return elementsMaterializer != null ? elementsMaterializer.weightNextWhile()
            : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
      }

      @Override
      protected void runWithContext() {
        if (elementsMaterializer != null) {
          elementsMaterializer.materializeNextWhile(this);
        } else {
          materializeElementsMaterializer(elementsMaterializerConsumer);
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class SkipFutureConsumer extends CancellableFutureConsumer<Integer> {

      private final FutureConsumer<Integer> consumer;
      private final int count;

      private String taskID;
      private int toSkip;

      private final CancellableFutureConsumer<IteratorFutureMaterializer<F>> elementsMaterializerConsumer = new CancellableFutureConsumer<IteratorFutureMaterializer<F>>() {
        @Override
        public void cancellableAccept(final IteratorFutureMaterializer<F> materializer)
            throws Exception {
          if (materializer == null) {
            consumer.accept(count - toSkip);
          } else {
            materializer.materializeSkip(toSkip, SkipFutureConsumer.this);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      };

      private SkipFutureConsumer(final int count, @NotNull final FutureConsumer<Integer> consumer) {
        this.count = toSkip = count;
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final Integer skipped) throws Exception {
        index += skipped;
        final int remaining = toSkip -= skipped;
        if (remaining <= 0) {
          consumer.accept(count - remaining);
        } else {
          elementsMaterializer = null;
          schedule();
        }
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
      public int weight() {
        final IteratorFutureMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
        return elementsMaterializer != null ? elementsMaterializer.weightSkip()
            : elementsMaterializerConsumers.isEmpty() ? wrapped.weightNext() : 1;
      }

      @Override
      protected void runWithContext() {
        if (elementsMaterializer != null) {
          elementsMaterializer.materializeSkip(toSkip, this);
        } else {
          materializeElementsMaterializer(elementsMaterializerConsumer);
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }
  }
}
