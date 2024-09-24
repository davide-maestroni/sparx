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
import sparx.future.Iterator;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.iterator.IteratorFutureMaterializer;
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
    test(List.of(1, 2, 3), lazy.Iterator::<Integer>of, it -> it.append(1).append(2).append(3));
    test(List.of(1, null, 3), lazy.Iterator::<Integer>of,
        it -> it.append(1).append(null).append(3));
    test(List.of(1, 2, 3), () -> lazy.Iterator.of(1), it -> it.append(2).append(3));
    test(List.of(1, null, 3), () -> lazy.Iterator.of(1), it -> it.append(null).append(3));
    test(List.of(1, 2, 3), () -> lazy.Iterator.of(1, 2), it -> it.append(3));
    test(List.of(1, null, 3), () -> lazy.Iterator.of(1, null), it -> it.append(3));

    testCancel(it -> it.append(null));
  }

  @Test
  public void drop() throws Exception {
    test(List.of(), lazy.Iterator::<Integer>of, it -> it.drop(1));
    test(List.of(), lazy.Iterator::<Integer>of, it -> it.drop(0));
    test(List.of(), lazy.Iterator::<Integer>of, it -> it.drop(-1));
    test(List.of(1, null, 3), () -> lazy.Iterator.of(1, null, 3), it -> it.drop(-1));
    test(List.of(1, null, 3), () -> lazy.Iterator.of(1, null, 3), it -> it.drop(0));
    test(List.of(null, 3), () -> lazy.Iterator.of(1, null, 3), it -> it.drop(1));
    test(List.of(3), () -> lazy.Iterator.of(1, null, 3), it -> it.drop(2));
    test(List.of(), () -> lazy.Iterator.of(1, null, 3), it -> it.drop(3));
    test(List.of(), () -> lazy.Iterator.of(1, null, 3), it -> it.drop(4));

    testCancel(it -> it.drop(1));
  }

  @Test
  public void flatMap() throws Exception {
    assertThrows(NullPointerException.class, () -> lazy.Iterator.of(0).toFuture(context)
        .flatMap((Function<? super Integer, List<Object>>) null));
    assertThrows(NullPointerException.class, () -> lazy.Iterator.of(0).toFuture(context)
        .flatMap((IndexedFunction<? super Integer, List<Object>>) null));
    test(List.of(1, 1, 2, 2), () -> lazy.Iterator.of(1, 2), it -> it.flatMap(i -> List.of(i, i)));
    test(List.of(), () -> lazy.Iterator.of(1, 2), it -> it.flatMap(i -> List.of()));
    test(List.of(null, null), () -> lazy.Iterator.of(1, 2), it -> it.flatMap(i -> List.of(null)));

    testCancel(it -> it.flatMap(e -> List.of(e)));
  }

  @Test
  public void flatMapWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> lazy.Iterator.of(0).toFuture(context).flatMapWhere(null, e -> List.of()));
    assertThrows(NullPointerException.class,
        () -> lazy.Iterator.of(0).toFuture(context).flatMapWhere(null, (i, e) -> List.of()));
    assertThrows(NullPointerException.class,
        () -> lazy.Iterator.of(0).toFuture(context).flatMapWhere(e -> true, null));
    assertThrows(NullPointerException.class,
        () -> lazy.Iterator.of(0).toFuture(context).flatMapWhere((i, e) -> true, null));
    test(List.of(1, null, null, 4), () -> lazy.Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 1, null, null, null, null, 4, 4), () -> lazy.Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 3, 3, 4), () -> lazy.Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(Objects::isNull, i -> List.of(3)));
    test(List.of(1, null, null, 4), () -> lazy.Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(i -> false, i -> List.of()));
    test(List.of(1, 4), () -> lazy.Iterator.of(1, null, null, 4),
        it -> it.flatMapWhere(Objects::isNull, i -> List.of()));
    test(List.of(), lazy.Iterator::of, it -> it.flatMapWhere(i -> false, i -> List.of()));
    test(List.of(), lazy.Iterator::of, it -> it.flatMapWhere(i -> true, i -> List.of()));

    java.util.function.Supplier<Iterator<Integer>> itr = () -> lazy.Iterator.of(1, null, null, 4)
        .toFuture(context);
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
        () -> lazy.Iterator.of(0).toFuture(context).map((Function<? super Integer, Object>) null));
    assertThrows(NullPointerException.class, () -> lazy.Iterator.of(0).toFuture(context)
        .map((IndexedFunction<? super Integer, Object>) null));
    test(List.of(2, 3, 4), () -> lazy.Iterator.of(1, 2, 3), it -> it.map(x -> x + 1));
    test(List.of(), lazy.Iterator::<Integer>of, it -> it.map(x -> x + 1));

    java.util.function.Supplier<Iterator<Integer>> itr = () -> lazy.Iterator.of(1, 2, 3)
        .toFuture(context);
    assertFalse(itr.get().append(null).map(x -> x + 1).isEmpty());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).size());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).drop(2).first());
    assertEquals(2, itr.get().append(null).map(x -> x + 1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).map(x -> x + 1).drop(3).first());

    testCancel(it -> it.map(e -> e));
  }

  private <E> void test(@NotNull final java.util.List<E> expected,
      @NotNull final Supplier<? extends Iterator<? extends E>> actualSupplier) throws Exception {
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
    itr.get();
    assertEquals(expected, itr.toList());
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
      @NotNull final Supplier<? extends lazy.Iterator<E>> baseSupplier,
      @NotNull final Function<future.Iterator<E>, future.Iterator<? extends F>> actualTransformer)
      throws Exception {
    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(context)));
    test(expected, () -> actualTransformer.apply(
        baseSupplier.get().toFuture(context).flatMapWhere(e -> false, e -> lazy.Iterator.of())));
    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(throughputContext)));
    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(throughputContext)
        .flatMapWhere(e -> false, e -> lazy.Iterator.of())));
  }

  private void testCancel(@NotNull final Function<future.Iterator<Object>, Future<?>> transformer)
      throws Exception {
    if (TEST_ASYNC_CANCEL) {
      var f = transformer.apply(lazy.Iterator.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return lazy.Iterator.of(i);
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
    /* materializeEmpty */
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

    var atSize = new AtomicInteger(-1);
    /* materializeSize */
  }
}
