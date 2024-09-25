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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sparx.concurrent.ExecutorContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.internal.future.iterator.AppendIteratorFutureMaterializer;
import sparx.internal.future.iterator.IteratorFutureMaterializer;
import sparx.internal.future.iterator.ListToIteratorFutureMaterializer;
import sparx.lazy.Iterator;
import sparx.lazy.List;
import sparx.util.UncheckedException.UncheckedInterruptedException;
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;
import sparx.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public class FutureIteratorTests {

  private static final boolean TEST_ASYNC_CANCEL = true;

  private ExecutorContext context;
  private ExecutorService executor;
  private ExecutorContext throughputContext;

  @BeforeEach
  public void setUp() {
    executor = Executors.newCachedThreadPool();
    context = ExecutorContext.of(executor);
    throughputContext = ExecutorContext.of(executor, 2);
  }

  @AfterEach
  public void tearDown() {
    executor.shutdownNow();
  }

  @Test
  public void append() throws Exception {
    test(List.of(1, 2, 3), Iterator::<Integer>of, it -> it.append(1).append(2).append(3));
    test(List.of(1, null, 3), Iterator::<Integer>of, it -> it.append(1).append(null).append(3));
    test(List.of(1, 2, 3), () -> Iterator.of(1), it -> it.append(2).append(3));
    test(List.of(1, null, 3), () -> Iterator.of(1), it -> it.append(null).append(3));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2), it -> it.append(3));
    test(List.of(1, null, 3), () -> Iterator.of(1, null), it -> it.append(3));

    testMaterializer(List.of(1, 2, 3), c -> new AppendIteratorFutureMaterializer<>(
        new ListToIteratorFutureMaterializer<>(List.of(1, 2), c), 3, c, new AtomicReference<>(),
        (l, e) -> List.wrap(l).append(e)));

    testCancel(it -> it.append(null));
  }

  @Test
  public void drop() throws Exception {
    test(List.of(), Iterator::<Integer>of, it -> it.drop(1));
    test(List.of(), Iterator::<Integer>of, it -> it.drop(0));
    test(List.of(), Iterator::<Integer>of, it -> it.drop(-1));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3), it -> it.drop(-1));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3), it -> it.drop(0));
    test(List.of(null, 3), () -> Iterator.of(1, null, 3), it -> it.drop(1));
    test(List.of(3), () -> Iterator.of(1, null, 3), it -> it.drop(2));
    test(List.of(), () -> Iterator.of(1, null, 3), it -> it.drop(3));
    test(List.of(), () -> Iterator.of(1, null, 3), it -> it.drop(4));

    testCancel(it -> it.drop(1));
  }

  @Test
  public void flatMap() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .flatMap((Function<? super Integer, List<Object>>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .flatMap((IndexedFunction<? super Integer, List<Object>>) null));
    test(List.of(1, 1, 2, 2), () -> Iterator.of(1, 2), it -> it.flatMap(i -> List.of(i, i)));
    test(List.of(), () -> Iterator.of(1, 2), it -> it.flatMap(i -> List.of()));
    test(List.of(null, null), () -> Iterator.of(1, 2), it -> it.flatMap(i -> List.of(null)));

    testCancel(it -> it.flatMap(e -> List.of(e)));
  }

  @Test
  public void flatMapWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapWhere(null, e -> List.of()));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapWhere(null, (i, e) -> List.of()));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapWhere(e -> true, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapWhere((i, e) -> true, null));
    test(List.of(1, null, null, 4), () -> Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 1, null, null, null, null, 4, 4), () -> Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 3, 3, 4), () -> Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(Objects::isNull, i -> List.of(3)));
    test(List.of(1, null, null, 4), () -> Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(i -> false, i -> List.of()));
    test(List.of(1, 4), () -> Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(Objects::isNull, i -> List.of()));
    test(List.of(), Iterator::of, it -> it.flatMapWhere(i -> false, i -> List.of()));
    test(List.of(), Iterator::of, it -> it.flatMapWhere(i -> true, i -> List.of()));

    Supplier<future.Iterator<Integer>> itr = () -> Iterator.of(1, null, null, 4).toFuture(context);
    assertFalse(itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).isEmpty());
    assertEquals(1, itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).first());
    assertEquals(1, itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).drop(2).first());
    assertFalse(itr.get().flatMapWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, itr.get().flatMapWhere(i -> i > 2, i -> List.of(i, i)).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapWhere(i -> i > 2, i -> List.of(i, i)).drop(1).first());

    testCancel(it -> it.flatMapWhere(e -> true, List::of));
  }

  @Test
  public void map() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).map((Function<? super Integer, Object>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .map((IndexedFunction<? super Integer, Object>) null));
    test(List.of(2, 3, 4), () -> Iterator.of(1, 2, 3), it -> it.map(x -> x + 1));
    test(List.of(), Iterator::<Integer>of, it -> it.map(x -> x + 1));

    Supplier<future.Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3).toFuture(context);
    assertFalse(itr.get().append(null).map(x -> x + 1).isEmpty());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).size());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).drop(2).first());
    assertEquals(2, itr.get().append(null).map(x -> x + 1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).map(x -> x + 1).drop(3).first());

    testCancel(it -> it.map(e -> e));
  }

  private <E> void test(@NotNull final java.util.List<E> expected,
      @NotNull final Supplier<? extends future.Iterator<? extends E>> actualSupplier)
      throws Exception {
    assertEquals(expected.isEmpty(), actualSupplier.get().isEmpty());
    assertEquals(!expected.isEmpty(), actualSupplier.get().notEmpty());
    assertEquals(expected.size(), actualSupplier.get().size());
    assertEquals(expected, actualSupplier.get().toList());
    var itr = actualSupplier.get();
    for (final E element : expected) {
      assertTrue(itr.hasNext());
      assertEquals(element, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
    }
    assertFalse(itr.hasNext());
    assertThrows(NoSuchElementException.class, itr::next);
    itr = actualSupplier.get();
    assertFalse(itr.isCancelled());
    assertFalse(itr.isFailed());
    assertEquals(expected, Iterator.wrap(itr.get()).toList());
    assertTrue(itr.isDone());
    assertFalse(itr.isCancelled());
    assertFalse(itr.isFailed());
    assertTrue(itr.isSucceeded());
    itr = actualSupplier.get();
    itr.nonBlockingGet().get();
    assertTrue(itr.isDone());
    assertFalse(itr.isCancelled());
    assertFalse(itr.isFailed());
    assertTrue(itr.isSucceeded());
    itr = actualSupplier.get();
    itr.skip(1);
    for (int i = 1; i < expected.size(); i++) {
      assertTrue(itr.hasNext());
      assertEquals(expected.get(i), itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
    }
    assertFalse(itr.hasNext());
    assertThrows(NoSuchElementException.class, itr::next);
  }

  private <E, F> void test(@NotNull final java.util.List<F> expected,
      @NotNull final Supplier<? extends Iterator<E>> baseSupplier,
      @NotNull final Function<future.Iterator<E>, future.Iterator<? extends F>> actualTransformer)
      throws Exception {
    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(context)));
    test(expected, () -> actualTransformer.apply(
        baseSupplier.get().toFuture(context).flatMapWhere(e -> false, e -> Iterator.of())));
    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(throughputContext)));
    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(throughputContext)
        .flatMapWhere(e -> false, e -> Iterator.of())));
  }

  private void testCancel(@NotNull final Function<future.Iterator<Object>, Future<?>> transformer)
      throws Exception {
    if (TEST_ASYNC_CANCEL) {
      var f = transformer.apply(Iterator.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return Iterator.of(i);
      }));
      executor.submit(() -> {
        try {
          Thread.sleep(1000);
        } catch (final InterruptedException e) {
          throw UncheckedInterruptedException.toUnchecked(e);
        }
        f.cancel(true);
      });
      assertThrows(CancellationException.class, f::get);
      assertTrue(f.isDone());
      assertTrue(f.isCancelled());
      if (f instanceof future.List) {
        assertFalse(((future.List<?>) f).isFailed());
        assertFalse(((future.List<?>) f).isSucceeded());
      }
    }
  }

  private <E> void testMaterializer(@NotNull final java.util.List<E> expected,
      @NotNull final Function<ExecutorContext, ? extends IteratorFutureMaterializer<E>> factory)
      throws Exception {
    var trampoline = ExecutorContext.trampoline();
    var atError = new AtomicReference<Exception>();
    var atHasNext = new AtomicBoolean();
    /* materializeHasNext */
    factory.apply(trampoline).materializeHasNext(new FutureConsumer<>() {
      @Override
      public void accept(final Boolean hasNext) {
        atHasNext.set(hasNext);
      }

      @Override
      public void error(@NotNull final Exception error) {
        atError.set(error);
      }
    });
    assertNull(atError.get());
    assertEquals(!expected.isEmpty(), atHasNext.get());
    atHasNext.set(expected.isEmpty());

    var atCalled = new AtomicBoolean();
    /* materializeNext */
    var atSize = new AtomicInteger(-1);
    var atIndex = new AtomicInteger(-1);
    var atElement = new AtomicReference<E>();
    var materializer = factory.apply(trampoline);
    for (int i = 0; i < expected.size(); i++) {
      materializer.materializeNext(new IndexedFutureConsumer<>() {
        @Override
        public void accept(final int size, final int index, final E element) {
          atSize.set(size);
          atIndex.set(index);
          atElement.set(element);
        }

        @Override
        public void complete(final int size) {
          atSize.set(size);
          atCalled.set(true);
        }

        @Override
        public void error(@NotNull final Exception error) {
          atError.set(error);
        }
      });
      assertNull(atError.get());
      assertFalse(atCalled.get());
      assertEquals(i, atIndex.get());
      assertEquals(expected.size(), atSize.get() + atIndex.get());
      assertEquals(expected.get(i), atElement.get());
      atSize.set(-1);
      atIndex.set(-1);
    }
    materializer.materializeNext(new IndexedFutureConsumer<>() {
      @Override
      public void accept(final int size, final int index, final E element) {
        atSize.set(size);
        atIndex.set(index);
        atElement.set(element);
      }

      @Override
      public void complete(final int size) {
        atSize.set(size);
        atCalled.set(true);
      }

      @Override
      public void error(@NotNull final Exception error) {
        atError.set(error);
      }
    });
    assertNull(atError.get());
    assertTrue(atCalled.get());
    assertEquals(0, atSize.get());
    assertEquals(-1, atIndex.get());
    atSize.set(-1);
    atCalled.set(false);

    var atSkipped = new AtomicInteger(-1);
    /* materializeNextWhile (stop) */
    for (int i = 0; i < expected.size(); i++) {
      materializer = factory.apply(trampoline);
      materializer.materializeSkip(i, new FutureConsumer<>() {
        @Override
        public void accept(final Integer skipped) {
          atSkipped.set(skipped);
        }

        @Override
        public void error(@NotNull final Exception error) {
          atError.set(error);
        }
      });
      assertNull(atError.get());
      assertEquals(i, atSkipped.get());
      atSkipped.set(-1);
      materializer.materializeNextWhile(new IndexedFuturePredicate<>() {
        @Override
        public void complete(final int size) {
          atSize.set(size);
          atCalled.set(true);
        }

        @Override
        public void error(@NotNull final Exception error) {
          atError.set(error);
        }

        @Override
        public boolean test(final int size, final int index, final E element) {
          atSize.set(size);
          atIndex.set(index);
          atElement.set(element);
          return false;
        }
      });
      assertNull(atError.get());
      assertFalse(atCalled.get());
      assertEquals(i, atIndex.get());
      assertEquals(expected.size(), atSize.get() + atIndex.get());
      assertEquals(expected.get(i), atElement.get());
      atSize.set(-1);
      atIndex.set(-1);
    }
    materializer = factory.apply(trampoline);
    materializer.materializeSkip(expected.size(), new FutureConsumer<>() {
      @Override
      public void accept(final Integer skipped) {
        atSkipped.set(skipped);
      }

      @Override
      public void error(@NotNull final Exception error) {
        atError.set(error);
      }
    });
    assertNull(atError.get());
    assertEquals(expected.size(), atSkipped.get());
    atSkipped.set(-1);
    materializer.materializeNextWhile(new IndexedFuturePredicate<>() {
      @Override
      public void complete(final int size) {
        atSize.set(size);
        atCalled.set(true);
      }

      @Override
      public void error(@NotNull final Exception error) {
        atError.set(error);
      }

      @Override
      public boolean test(final int size, final int index, final E element) {
        atSize.set(size);
        atIndex.set(index);
        atElement.set(element);
        return false;
      }
    });
    assertNull(atError.get());
    assertTrue(atCalled.get());
    assertEquals(-1, atIndex.get());
    assertEquals(0, atSize.get());
    atSize.set(-1);
    atCalled.set(false);

    var elementList = new ArrayList<E>();
    var indexList = new ArrayList<Integer>();
    /* materializeNextWhile (continue) */
  }
}
