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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sparx.concurrent.ExecutorContext;
import sparx.lazy.List;
import sparx.util.UncheckedException.UncheckedInterruptedException;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public class FutureListTests {

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
    test(List.of(1, 2, 3), List::<Integer>of, ll -> ll.append(1).append(2).append(3));
    test(List.of(1, null, 3), List::<Integer>of, ll -> ll.append(1).append(null).append(3));
    test(List.of(1, 2, 3), () -> List.of(1), ll -> ll.append(2).append(3));
    test(List.of(1, null, 3), () -> List.of(1), ll -> ll.append(null).append(3));
    test(List.of(1, 2, 3), () -> List.of(1, 2), ll -> ll.append(3));
    test(List.of(1, null, 3), () -> List.of(1, null), ll -> ll.append(3));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).append(4);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void appendAll() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of().toFuture(context).appendAll(null));
    test(List.of(1, 2, 3), List::<Integer>of, ll -> ll.appendAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), List::<Integer>of, ll -> ll.appendAll(List.of(1, null, 3)));
    test(List.of(1, 2, 3), () -> List.of(1),
        ll -> ll.appendAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(1, null, 3), () -> List.of(1), ll -> ll.appendAll(List.of(null, 3)));
    test(List.of(1, 2, 3), () -> List.of(1, 2), ll -> ll.appendAll(Set.of(3)));
    test(List.of(1, null, 3), () -> List.of(1, null), ll -> ll.appendAll(Set.of(3)));
    test(List.of(1, null), () -> List.of(1, null), ll -> ll.appendAll(List.of()));
    test(List.of(1, null), () -> List.of(1, null), ll -> ll.appendAll(Set.of()));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).appendAll(List.of(4, 5));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void count() throws Exception {
    test(List.of(0), List::of, future.List::count);
    test(List.of(3), () -> List.of(1, 2, 3), future.List::count);
    test(List.of(3), () -> List.of(1, null, 3), future.List::count);

    if (TEST_ASYNC_CANCEL) {
      // TODO
    }
  }

  @Test
  public void countWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).countWhere((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).countWhere((IndexedPredicate<? super Object>) null));
    test(List.of(0), List::of, ll -> ll.countWhere(Objects::nonNull));
    test(List.of(2), () -> List.of(1, 2, 3), ll -> ll.countWhere(i -> i < 3));
    test(List.of(3), () -> List.of(1, 2, 3), ll -> ll.countWhere(i -> i > 0));
    var l = List.of(1, null, 3).toFuture(context).countWhere(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      // TODO
//      var itr = l.iterator();
//      assertTrue(itr.hasNext());
//      assertThrows(NullPointerException.class, itr::next);
    }
    l = List.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e)).countWhere(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      // TODO
//      var itr = l.iterator();
//      assertTrue(itr.hasNext());
//      assertThrows(NullPointerException.class, itr::next);
    }
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 2, 1).toFuture(context).countWhere((n, i) -> {
      indexes.add(n);
      return i < 2;
    }).first();
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 2, 1).toFuture(context).flatMap(e -> List.of(e)).countWhere((n, i) -> {
      indexes.add(n);
      return i < 2;
    }).first();
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).countWhere(i -> {
        Thread.sleep(60000);
        return true;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void diff() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).diff(null));
    test(List.of(2, 4), () -> List.of(1, 2, null, 4), ll -> ll.diff(List.of(1, null)));
    test(List.of(2, null), () -> List.of(1, 2, null, 4), ll -> ll.diff(List.of(1, 4)));
    test(List.of(2, null), () -> List.of(1, 2, null, 4), ll -> ll.diff(List.of(1, 3, 4)));
    test(List.of(2, null, 4), () -> List.of(1, 2, null, 4), ll -> ll.diff(List.of(3, 1, 3)));
    test(List.of(1, 2, 4), () -> List.of(1, 2, null, 4), ll -> ll.diff(List.of(null, null)));
    test(List.of(1, 2, 4), () -> List.of(1, 1, 2, null, 4), ll -> ll.diff(List.of(null, null, 1)));
    test(List.of(), () -> List.of(1, null), ll -> ll.diff(List.of(1, 2, null, 4)));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4), ll -> ll.diff(List.of()));
    test(List.of(1, 1, 2, null, 4), () -> List.of(1, 1, 2, null, 4), ll -> ll.diff(List.of()));
    test(List.of(), List::of, ll -> ll.diff(List.of(1, 2, null, 4)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).diff(List.of(false, null));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void doFor() throws ExecutionException, InterruptedException {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doFor((Consumer<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doFor((IndexedConsumer<? super Object>) null));
    var list = new ArrayList<>();
    List.of(1, 2, 3).toFuture(context).doFor(e -> list.add(e));
    assertEquals(List.of(1, 2, 3), list);
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 2, 1).toFuture(context).doFor((n, i) -> indexes.add(n));
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 2, 1).toFuture(context).nonBlockingFor((n, i) -> indexes.add(n)).get();
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).nonBlockingFor(i -> Thread.sleep(60000));
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
    }
  }

  @Test
  public void doWhile() throws ExecutionException, InterruptedException {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doWhile((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doWhile((IndexedPredicate<? super Object>) null));
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).doWhile(null, i -> {
    }));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doWhile(i -> true, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doWhile(null, (n, i) -> {
        }));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doWhile((n, i) -> true, null));
    var list = new ArrayList<>();
    List.of(1, 2, 3).toFuture(context).doWhile(e -> e < 3, list::add);
    assertEquals(List.of(1, 2), list);
    list.clear();
    List.of(1, 2, 3).toFuture(context).doWhile(e -> {
      list.add(e);
      return e < 2;
    });
    assertEquals(List.of(1, 2), list);
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).doWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    });
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).nonBlockingWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).get();
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).doWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }, (n, i) -> indexes.add(n));
    assertEquals(List.of(0, 0, 1, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).nonBlockingWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }, (n, i) -> indexes.add(n)).get();
    assertEquals(List.of(0, 0, 1, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).nonBlockingWhile(i -> {
        Thread.sleep(60000);
        return true;
      });
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
    }
  }

  @Test
  public void drop() throws Exception {
    test(List.of(), List::<Integer>of, ll -> ll.drop(1));
    test(List.of(), List::<Integer>of, ll -> ll.drop(0));
    test(List.of(), List::<Integer>of, ll -> ll.drop(-1));
    test(List.of(null, 3), () -> List.of(1, null, 3), ll -> ll.drop(1));
    test(List.of(3), () -> List.of(1, null, 3), ll -> ll.drop(2));
    test(List.of(), () -> List.of(1, null, 3), ll -> ll.drop(3));
    test(List.of(), () -> List.of(1, null, 3), ll -> ll.drop(4));
    test(List.of(1, null, 3), () -> List.of(1, null, 3), ll -> ll.drop(0));
    test(List.of(1, null, 3), () -> List.of(1, null, 3), ll -> ll.drop(-1));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).drop(1);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void dropRight() throws Exception {
    test(List.of(), List::<Integer>of, ll -> ll.dropRight(1));
    test(List.of(), List::<Integer>of, ll -> ll.dropRight(0));
    test(List.of(), List::<Integer>of, ll -> ll.dropRight(-1));
    test(List.of(1, null), () -> List.of(1, null, 3), ll -> ll.dropRight(1));
    test(List.of(1), () -> List.of(1, null, 3), ll -> ll.dropRight(2));
    test(List.of(), () -> List.of(1, null, 3), ll -> ll.dropRight(3));
    test(List.of(), () -> List.of(1, null, 3), ll -> ll.dropRight(4));
    test(List.of(1, null, 3), () -> List.of(1, null, 3), ll -> ll.dropRight(0));
    test(List.of(1, null, 3), () -> List.of(1, null, 3), ll -> ll.dropRight(-1));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).dropRight(1);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void dropRightWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).dropRightWhile((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).dropRightWhile((IndexedPredicate<? super Object>) null));
    test(List.of(), List::<Integer>of, ll -> ll.dropRightWhile(e -> e > 0));
    test(List.of(1, null, 3), () -> List.of(1, null, 3), ll -> ll.dropRightWhile(Objects::isNull));
    test(List.of(1, null), () -> List.of(1, null, 3), ll -> ll.dropRightWhile(Objects::nonNull));
    test(List.of(1, null, 3), () -> List.of(1, null, 3), ll -> ll.dropRightWhile(e -> e < 1));
    test(List.of(), () -> List.of(1, 2, 3), ll -> ll.dropRightWhile(e -> e > 0));
    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).toFuture(context).dropRightWhile(e -> e > 0).size());
    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e))
            .dropRightWhile(e -> e > 0).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).dropRightWhile((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 1), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).dropRightWhile((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 1), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).dropRightWhile(i -> {
        Thread.sleep(60000);
        return true;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void dropWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).dropWhile((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).dropWhile((IndexedPredicate<? super Object>) null));
    test(List.of(), List::<Integer>of, ll -> ll.dropWhile(e -> e > 0));
    test(List.of(1, null, 3), () -> List.of(1, null, 3), ll -> ll.dropWhile(Objects::isNull));
    test(List.of(null, 3), () -> List.of(1, null, 3), ll -> ll.dropWhile(Objects::nonNull));
    test(List.of(1, null, 3), () -> List.of(1, null, 3), ll -> ll.dropWhile(e -> e < 1));
    test(List.of(), () -> List.of(1, 2, 3), ll -> ll.dropWhile(e -> e > 0));
    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).toFuture(context).dropWhile(e -> e > 0).size());
    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e)).dropWhile(e -> e > 0)
            .size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).dropWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).dropWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).dropWhile(i -> {
        Thread.sleep(60000);
        return true;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void each() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).each((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).each((IndexedPredicate<? super Object>) null));
    test(List.of(false), List::of, ll -> ll.each(Objects::nonNull));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.each(i -> i > 3));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.each(i -> i < 3));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.each(i -> i > 0));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.each(i -> i > 0));
    var l = List.of(1, null, 3).toFuture(context).each(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    l = List.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e)).each(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      // TODO
//      var itr = l.iterator();
//      assertTrue(itr.hasNext());
//      assertThrows(NullPointerException.class, itr::next);
    }
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).each((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).each((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).each(i -> {
        Thread.sleep(60000);
        return true;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void endsWith() throws Exception {
    test(List.of(true), List::<Integer>of, ll -> ll.endsWith(List.of()));
    test(List.of(false), List::<Integer>of, ll -> ll.endsWith(List.of(1)));
    test(List.of(true), () -> List.of(1, null, 3), ll -> ll.endsWith(List.of()));
    test(List.of(true), () -> List.of(1, null, 3), ll -> ll.endsWith(List.of(3)));
    test(List.of(false), () -> List.of(1, null, 3), ll -> ll.endsWith(List.of(null)));
    test(List.of(true), () -> List.of(1, null, 3), ll -> ll.endsWith(List.of(null, 3)));
    test(List.of(false), () -> List.of(1, null, 3), ll -> ll.endsWith(List.of(1, null)));
    test(List.of(true), () -> List.of(1, null, 3), ll -> ll.endsWith(List.of(1, null, 3)));
    test(List.of(false), () -> List.of(1, null, 3), ll -> ll.endsWith(List.of(null, null, 3)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).endsWith(List.of(false));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void exists() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).exists((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).exists((IndexedPredicate<? super Object>) null));
    test(List.of(false), List::of, ll -> ll.exists(Objects::nonNull));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.exists(i -> i > 3));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.exists(i -> i > 3));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.exists(i -> i > 0));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.exists(i -> i > 0));
    var l = List.of(1, null, 3).toFuture(context).exists(i -> i > 1);
    assertThrows(NullPointerException.class, l::first);
    List.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e)).exists(i -> i > 1);
    assertThrows(NullPointerException.class, l::first);
    {
      // TODO
//      var itr = l.iterator();
//      assertThrows(NullPointerException.class, itr::hasNext);
//      assertThrows(NullPointerException.class, itr::next);
    }
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).exists((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).exists((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).exists(i -> {
        Thread.sleep(60000);
        return false;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void filter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).filter((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).filter((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4);
    test(List.of(null), () -> l, ll -> ll.filter(Objects::isNull));
    test(List.of(1, 2, 4), () -> l, ll -> ll.filter(Objects::nonNull));
    test(List.of(1, 2), () -> l, ll -> ll.filter(Objects::nonNull).filter(i -> i < 3));
    test(List.of(4), () -> l, ll -> ll.filter(Objects::nonNull).filter(i -> i > 3));
    test(List.of(), () -> l, ll -> ll.filter(Objects::nonNull).filter(i -> i > 4));
    test(List.of(), List::of, ll -> ll.filter(Objects::isNull));
    assertThrows(NullPointerException.class, () -> l.toFuture(context).filter(i -> i > 4).size());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMap(e -> List.of(e)).filter(i -> i > 4).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).filter((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).filter((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).filter(i -> {
        Thread.sleep(60000);
        return false;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void findAny() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findAny((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findAny((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4);
    test(List.of(null), () -> l, ll -> ll.findAny(Objects::isNull));
    test(List.of(1), () -> l, ll -> ll.findAny(i -> i < 4));
    test(List.of(), List::of, ll -> ll.findAny(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).findAny((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).findAny((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).findAny(i -> {
        Thread.sleep(60000);
        return false;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void findIndexOf() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l, ll -> ll.findIndexOf(null));
    test(List.of(3), () -> l, ll -> ll.findIndexOf(4));
    test(List.of(), () -> l, ll -> ll.findIndexOf(3));
    test(List.of(), List::of, ll -> ll.findIndexOf(null));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).findAny((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).findAny((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).findIndexOf(false);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void findIndexOfSlice() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findIndexOfSlice(null));
    var l = List.of(1, 2, null, 4);
    test(List.of(1), () -> l, ll -> ll.findIndexOfSlice(List.of(2, null)));
    test(List.of(2), () -> l, ll -> ll.findIndexOfSlice(List.of(null)));
    test(List.of(), () -> l, ll -> ll.findIndexOfSlice(List.of(null, 2)));
    test(List.of(0), () -> l, ll -> ll.findIndexOfSlice(List.of()));
    test(List.of(2), () -> List.of(1, 1, 1, 1, 2, 1), ll -> ll.findIndexOfSlice(List.of(1, 1, 2)));
    test(List.of(), List::of, ll -> ll.findIndexOfSlice(List.of(null)));
    test(List.of(0), List::of, ll -> ll.findIndexOfSlice(List.of()));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).findIndexOfSlice(List.of(false));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void findIndexWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findIndexWhere((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findIndexWhere((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l, ll -> ll.findIndexWhere(Objects::isNull));
    test(List.of(1), () -> l, ll -> ll.findIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMap(e -> List.of(e)).findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findIndexWhere(i -> i > 3).first());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMap(e -> List.of(e)).findIndexWhere(i -> i > 3).first());
    test(List.of(), List::of, ll -> ll.findIndexWhere(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).findIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).findIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).findIndexWhere(i -> {
        Thread.sleep(60000);
        return false;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void findLast() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findLast((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findLast((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4, 5);
    test(List.of(null), () -> l, ll -> ll.findLast(Objects::isNull));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findLast(i -> i < 4).first());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMap(e -> List.of(e)).findLast(i -> i < 4).first());
    test(List.of(4), () -> l, ll -> ll.findLast(i -> i < 5));
    test(List.of(), () -> l, ll -> ll.findLast(i -> i != null && i > 5));
    test(List.of(), List::of, ll -> ll.findLast(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).findLast((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(3, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).findLast((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(3, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).findLast(i -> {
        Thread.sleep(60000);
        return false;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void findLastIndexOf() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l, ll -> ll.findLastIndexOf(null));
    test(List.of(3), () -> l, ll -> ll.findLastIndexOf(4));
    test(List.of(), () -> l, ll -> ll.findLastIndexOf(3));
    test(List.of(), List::of, ll -> ll.findLastIndexOf(null));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).findLastIndexOf(false);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void findLastIndexOfSlice() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findLastIndexOfSlice(null));
    var l = List.of(1, 2, null, 4);
    test(List.of(1), () -> l, ll -> ll.findLastIndexOfSlice(List.of(2, null)));
    test(List.of(2), () -> l, ll -> ll.findLastIndexOfSlice(List.of(null)));
    test(List.of(), () -> l, ll -> ll.findLastIndexOfSlice(List.of(null, 2)));
    test(List.of(4), () -> l, ll -> ll.findLastIndexOfSlice(List.of()));
    test(List.of(2), () -> List.of(1, 1, 1, 1, 2, 1),
        ll -> ll.findLastIndexOfSlice(List.of(1, 1, 2)));
    test(List.of(), List::of, ll -> ll.findLastIndexOfSlice(List.of(null)));
    test(List.of(0), List::of, ll -> ll.findLastIndexOfSlice(List.of()));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).findLastIndexOfSlice(List.of(false));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void findLastIndexWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findLastIndexWhere((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context)
        .findLastIndexWhere((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l, ll -> ll.findLastIndexWhere(Objects::isNull));
    test(List.of(3), () -> l, ll -> ll.findLastIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findLastIndexWhere(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMap(e -> List.of(e)).findLastIndexWhere(i -> i < 3)
            .isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findLastIndexWhere(i -> i < 3).first());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMap(e -> List.of(e)).findLastIndexWhere(i -> i < 3).first());
    test(List.of(), List::of, ll -> ll.findLastIndexWhere(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).findLastIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(3, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).findLastIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(3, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).findLastIndexWhere(i -> {
        Thread.sleep(60000);
        return false;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void flatMap() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context)
        .flatMap((Function<? super Integer, ? extends Iterable<Object>>) null));
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context)
        .flatMap((IndexedFunction<? super Integer, ? extends Iterable<Object>>) null));
    var l = List.of(1, 2);
    test(List.of(1, 1, 2, 2), () -> l, ll -> ll.flatMap(i -> List.of(i, i)));
    test(List.of(), () -> l, ll -> ll.flatMap(i -> List.of()));
    test(List.of(null, null), () -> l, ll -> ll.flatMap(i -> List.of(null)));
    assertNull(l.toFuture(context).flatMap(i -> List.of(null)).get(1));
    assertNull(l.toFuture(context).flatMap(e -> List.of(e)).flatMap(i -> List.of(null)).get(1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.toFuture(context).flatMap(i -> List.of(null)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.toFuture(context).flatMap(e -> List.of(e)).flatMap(i -> List.of(null)).get(2));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).flatMap((n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).flatMap((n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void flatMapAfter() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context)
        .flatMapAfter(0, (Function<? super Integer, ? extends Iterable<Integer>>) null));
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context)
        .flatMapAfter(0, (IndexedFunction<? super Integer, ? extends Iterable<Integer>>) null));
    var l = List.of(1, 2);
    test(l, () -> l, ll -> ll.flatMapAfter(-1, i -> List.of(i, i)));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.toFuture(context).flatMapAfter(2, i -> List.of(i, i)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.toFuture(context).flatMap(e -> List.of(e)).flatMapAfter(2, i -> List.of(i, i))
            .get(2));
    test(List.of(1, 1, 2), () -> l, ll -> ll.flatMapAfter(0, i -> List.of(i, i)));
    test(List.of(1, 2, 2), () -> l, ll -> ll.flatMapAfter(1, i -> List.of(i, i)));
    test(List.of(1, 2), () -> l, ll -> ll.flatMapAfter(2, i -> List.of(i, i)));
    test(List.of(2), () -> l, ll -> ll.flatMapAfter(0, i -> List.of()));
    test(List.of(1), () -> l, ll -> ll.flatMapAfter(1, i -> List.of()));
    test(List.of(1, 2), () -> l, ll -> ll.flatMapAfter(2, i -> List.of()));
    test(List.of(), List::of, ll -> ll.flatMapAfter(0, i -> List.of(i, i)));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).flatMapAfter(1, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(1), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).flatMapAfter(1, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(1), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).flatMapAfter(0, i -> {
        Thread.sleep(60000);
        return List.of(i);
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void flatMapFirstWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapFirstWhere(i -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapFirstWhere(null, i -> List.of(i)));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapFirstWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapFirstWhere(null, (n, i) -> List.of(i)));
    var l = List.of(1, 2, null, 4);
    test(l, () -> l, ll -> ll.flatMapFirstWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 1, 2, null, 4), () -> l,
        ll -> ll.flatMapFirstWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 2, 3, 4), () -> l,
        ll -> ll.flatMapFirstWhere(Objects::isNull, i -> List.of(3)));
    test(l, () -> l, ll -> ll.flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(2, null, 4), () -> l, ll -> ll.flatMapFirstWhere(i -> true, i -> List.of()));
    test(List.of(1, 2, 4), () -> l, ll -> ll.flatMapFirstWhere(Objects::isNull, i -> List.of()));
    test(List.of(1, 1, 2, null, 4), () -> l,
        ll -> ll.flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)));

    assertFalse(l.toFuture(context).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, l.toFuture(context).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(0));
    assertEquals(2, l.toFuture(context).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(2));
    assertFalse(l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class, () -> l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(0));
    assertEquals(2, l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(1));
    assertThrows(NullPointerException.class, () -> l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(2));

    test(List.of(), List::of, ll -> ll.flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(), List::of, ll -> ll.flatMapFirstWhere(i -> true, i -> List.of()));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).flatMapFirstWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).flatMapFirstWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e))
          .flatMapFirstWhere(i -> true, i -> {
            Thread.sleep(60000);
            return List.of(i);
          });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void flatMapLastWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapLastWhere(i -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapLastWhere(null, i -> List.of(i)));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapLastWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapLastWhere(null, (n, i) -> List.of(i)));
    var l = List.of(1, 2, null, 4);
    test(l, () -> l, ll -> ll.flatMapLastWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 2, null, 4, 4), () -> l,
        ll -> ll.flatMapLastWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 2, 3, 4), () -> l, ll -> ll.flatMapLastWhere(Objects::isNull, i -> List.of(3)));
    test(l, () -> l, ll -> ll.flatMapLastWhere(i -> false, i -> List.of()));
    test(List.of(1, 2, null), () -> l, ll -> ll.flatMapLastWhere(i -> true, i -> List.of()));
    test(List.of(1, 2, 4), () -> l, ll -> ll.flatMapLastWhere(Objects::isNull, i -> List.of()));
    test(List.of(1, 2, null, 4, 4), () -> l,
        ll -> ll.flatMapLastWhere(i -> i == 4, i -> List.of(i, i)));

    assertFalse(l.toFuture(context).flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).get(3));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).get(2));
    assertFalse(l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class, () -> l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class, () -> l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).get(3));
    assertThrows(NullPointerException.class, () -> l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).get(2));

    test(List.of(), List::of, ll -> ll.flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(), List::of, ll -> ll.flatMapFirstWhere(i -> true, i -> List.of()));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).flatMapLastWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).flatMapLastWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e))
          .flatMapLastWhere(i -> true, i -> {
            Thread.sleep(60000);
            return List.of(i);
          });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void flatMapWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapWhere(i -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapWhere(null, i -> List.of(i)));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).flatMapWhere(null, (n, i) -> List.of(i)));
    var l = List.of(1, null, null, 4);
    test(l, () -> l, ll -> ll.flatMapWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 1, null, null, null, null, 4, 4), () -> l,
        ll -> ll.flatMapWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 3, 3, 4), () -> l, ll -> ll.flatMapWhere(Objects::isNull, i -> List.of(3)));
    test(l, () -> l, ll -> ll.flatMapWhere(i -> false, i -> List.of()));
    test(List.of(), () -> l, ll -> ll.flatMapWhere(i -> true, i -> List.of()));
    test(List.of(1, 4), () -> l, ll -> ll.flatMapWhere(Objects::isNull, i -> List.of()));

    assertFalse(l.toFuture(context).flatMapWhere(i -> i == 1, i -> List.of(i, i)).isEmpty());
    assertEquals(1, l.toFuture(context).flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(0));
    assertEquals(1, l.toFuture(context).flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapWhere(i -> i == 1, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(2));
    assertFalse(l.toFuture(context).flatMapWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, l.toFuture(context).flatMapWhere(i -> i > 2, i -> List.of(i, i)).get(0));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapWhere(i -> i > 2, i -> List.of(i, i)).get(1));
    assertFalse(
        l.toFuture(context).flatMap(e -> List.of(e)).flatMapWhere(i -> i == 1, i -> List.of(i, i))
            .isEmpty());
    assertEquals(1,
        l.toFuture(context).flatMap(e -> List.of(e)).flatMapWhere(i -> i == 1, i -> List.of(i, i))
            .get(0));
    assertEquals(1,
        l.toFuture(context).flatMap(e -> List.of(e)).flatMapWhere(i -> i == 1, i -> List.of(i, i))
            .get(1));
    assertThrows(NullPointerException.class, () -> l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapWhere(i -> i == 1, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class, () -> l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(2));
    assertFalse(
        l.toFuture(context).flatMap(e -> List.of(e)).flatMapWhere(i -> i > 2, i -> List.of(i, i))
            .isEmpty());
    assertThrows(NullPointerException.class, () -> l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1,
        l.toFuture(context).flatMap(e -> List.of(e)).flatMapWhere(i -> i > 2, i -> List.of(i, i))
            .get(0));
    assertThrows(NullPointerException.class, () -> l.toFuture(context).flatMap(e -> List.of(e))
        .flatMapWhere(i -> i > 2, i -> List.of(i, i)).get(1));

    test(List.of(), List::of, ll -> ll.flatMapWhere(i -> false, i -> List.of()));
    test(List.of(), List::of, ll -> ll.flatMapWhere(i -> true, i -> List.of()));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).flatMapWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).flatMapWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e))
          .flatMapWhere(i -> true, i -> {
            Thread.sleep(60000);
            return List.of(i);
          });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void foldLeft() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).foldLeft(1, null));
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(16), () -> l, ll -> ll.foldLeft(1, Integer::sum));
    test(List.of(List.of(1, 2)), () -> List.of(1, 2),
        ll -> ll.foldLeft(List.<Integer>of(), List::append));
    test(List.of(1), List::<Integer>of, ll -> ll.foldLeft(1, Integer::sum));
    test(List.of(List.of()), List::of, ll -> ll.foldLeft(List.of(), List::append));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).foldLeft(0, (a, i) -> {
        Thread.sleep(60000);
        return i;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void foldRight() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).foldRight(1, null));
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(16), () -> l, ll -> ll.foldRight(1, Integer::sum));
    test(List.of(List.of(2, 1)), () -> List.of(1, 2),
        ll -> ll.foldRight(List.<Integer>of(), (i, li) -> li.append(i)));
    test(List.of(1), List::<Integer>of, ll -> ll.foldRight(1, Integer::sum));
    test(List.of(List.of()), List::of, ll -> ll.foldRight(List.of(), (i, li) -> li.append(i)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).foldRight(0, (i, a) -> {
        Thread.sleep(60000);
        return i;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void group() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> List.of(0).toFuture(context).group(-1));
    assertThrows(IllegalArgumentException.class, () -> List.of(0).toFuture(context).group(0));
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5)), () -> l,
        ll -> ll.group(1));
    test(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), () -> l, ll -> ll.group(2));
    test(List.of(List.of(1, 2, 3), List.of(4, 5)), () -> l, ll -> ll.group(3));
    test(List.of(List.of(1, 2, 3, 4, 5)), () -> l, ll -> ll.group(10));

    if (TEST_ASYNC_CANCEL) {
      // TODO
//      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
//        Thread.sleep(60000);
//        return true;
//      }).group(3);
//      executor.submit(() -> {
//        try {
//          Thread.sleep(1000);
//        } catch (final InterruptedException e) {
//          throw UncheckedInterruptedException.toUnchecked(e);
//        }
//        f.cancel(true);
//      });
//      assertThrows(CancellationException.class, f::get);
//      assertTrue(f.isDone());
//      assertTrue(f.isCancelled());
//      assertFalse(f.isFailed());
    }
  }

  @Test
  public void groupWithPadding() throws Exception {
    assertThrows(IllegalArgumentException.class,
        () -> List.of(0).toFuture(context).groupWithPadding(-1, 0));
    assertThrows(IllegalArgumentException.class,
        () -> List.of(0).toFuture(context).groupWithPadding(0, 0));
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5)), () -> l,
        ll -> ll.groupWithPadding(1, null));
    test(List.of(List.of(1, 2), List.of(3, 4), List.of(5, null)), () -> l,
        ll -> ll.groupWithPadding(2, null));
    test(List.of(List.of(1, 2, 3), List.of(4, 5, -1)), () -> l, ll -> ll.groupWithPadding(3, -1));
    test(List.of(List.of(1, 2, 3, 4, 5, -1, -1, -1, -1, -1)), () -> l,
        ll -> ll.groupWithPadding(10, -1));

    if (TEST_ASYNC_CANCEL) {
      // TODO
//      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
//        Thread.sleep(60000);
//        return true;
//      }).group(3, false);
//      executor.submit(() -> {
//        try {
//          Thread.sleep(1000);
//        } catch (final InterruptedException e) {
//          throw UncheckedInterruptedException.toUnchecked(e);
//        }
//        f.cancel(true);
//      });
//      assertThrows(CancellationException.class, f::get);
//      assertTrue(f.isDone());
//      assertTrue(f.isCancelled());
//      assertFalse(f.isFailed());
    }
  }

  @Test
  public void includes() throws Exception {
    var l = List.of(1, 2, 3, null, 5);
    test(List.of(true), () -> l, ll -> ll.includes(null));
    test(List.of(false), () -> l, ll -> ll.includes(0));
    test(List.of(true), () -> l, ll -> ll.includes(5));
    test(List.of(false), List::of, ll -> ll.includes(0));
    test(List.of(false), List::of, ll -> ll.includes(null));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).includes(null);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void includesAll() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).includesAll(null));
    var l = List.of(1, 2, 3, null, 5);
    test(List.of(true), () -> l, ll -> ll.includesAll(List.of(null, 1)));
    test(List.of(false), () -> l, ll -> ll.includesAll(List.of(0, 1).toFuture(context)));
    test(List.of(true), () -> l, ll -> ll.includesAll(List.of().toFuture(context)));
    test(List.of(false), List::of, ll -> ll.includesAll(List.of(null, 1).toFuture(context)));
    test(List.of(true), List::of, ll -> ll.includesAll(List.of()));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).includesAll(List.of(1));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void includesSlice() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).includesSlice(null));
    var l = List.of(1, 2, 3, null, 5);
    test(List.of(true), () -> l, ll -> ll.includesSlice(List.of(3, null)));
    test(List.of(false), () -> l, ll -> ll.includesSlice(List.of(null, 3)));
    test(List.of(true), () -> l, ll -> ll.includesSlice(List.of()));
    test(List.of(false), List::of, ll -> ll.includesSlice(List.of(null, 1)));
    test(List.of(true), List::of, ll -> ll.includesSlice(List.of()));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).includesSlice(List.of(1));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void insertAfter() throws Exception {
    var l = List.of(1, 2, 3);
    test(l, () -> l, ll -> ll.insertAfter(5, null));
    test(List.of(1, 2, 3, null), () -> l, ll -> ll.insertAfter(3, null));
    test(List.of(1, 2, null, 3), () -> l, ll -> ll.insertAfter(2, null));
    test(List.of(1, null, 2, 3), () -> l, ll -> ll.insertAfter(1, null));
    test(List.of(null, 1, 2, 3), () -> l, ll -> ll.insertAfter(0, null));
    test(l, () -> l, ll -> ll.insertAfter(-7, null));
    test(List.of(), List::of, ll -> ll.insertAfter(5, null));
    test(List.of(null), List::of, ll -> ll.insertAfter(0, null));
    Iterable<Object> iterable = () -> List.of().iterator();
    test(List.of(), () -> List.wrap(iterable), ll -> ll.insertAfter(5, null));
    test(List.of(null), () -> List.wrap(iterable), ll -> ll.insertAfter(0, null));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).insertAfter(1, 2);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void insertAllAfter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).insertAllAfter(0, null));
    var l = List.of(1, 2, 3);
    test(l, () -> l, ll -> ll.insertAllAfter(5, List.of(null, 5)));
    test(List.of(1, 2, 3, null, 5), () -> l, ll -> ll.insertAllAfter(3, List.of(null, 5)));
    test(List.of(1, 2, null, 5, 3), () -> l, ll -> ll.insertAllAfter(2, List.of(null, 5)));
    test(List.of(1, null, 5, 2, 3), () -> l, ll -> ll.insertAllAfter(1, List.of(null, 5)));
    test(List.of(null, 5, 1, 2, 3), () -> l, ll -> ll.insertAllAfter(0, List.of(null, 5)));
    test(l, () -> l, ll -> ll.insertAllAfter(-7, List.of(null, 5)));
    test(List.of(), List::of, ll -> ll.insertAllAfter(5, List.of(null, 5)));
    test(List.of(null, 5), List::of, ll -> ll.insertAllAfter(0, List.of(null, 5)));
    Iterable<Object> iterable = () -> List.of().iterator();
    test(List.of(), () -> List.wrap(iterable), ll -> ll.insertAllAfter(5, List.of(null, 5)));
    test(List.of(null, 5), () -> List.wrap(iterable), ll -> ll.insertAllAfter(0, List.of(null, 5)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).insertAllAfter(1, List.of(2));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void intersect() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).intersect(null));
    test(List.of(1, null), () -> List.of(1, 2, null, 4), ll -> ll.intersect(List.of(1, null)));
    test(List.of(1, 4), () -> List.of(1, 2, null, 4), ll -> ll.intersect(List.of(1, 4)));
    test(List.of(1, 4), () -> List.of(1, 2, null, 4), ll -> ll.intersect(List.of(1, 3, 4)));
    test(List.of(1), () -> List.of(1, 2, null, 4), ll -> ll.intersect(List.of(3, 1, 3)));
    test(List.of(null), () -> List.of(1, 2, null, 4), ll -> ll.intersect(List.of(null, null)));
    test(List.of(1, null), () -> List.of(1, null), ll -> ll.intersect(List.of(1, 2, null, 4)));
    test(List.of(1, 2), () -> List.of(1, 2, null, 4), ll -> ll.intersect(List.of(2, 1)));
    test(List.of(), () -> List.of(1, null), ll -> ll.intersect(List.of(2, 4)));
    test(List.of(), () -> List.of(1, 2, null, 4), ll -> ll.intersect(List.of()));
    test(List.of(), List::of, ll -> ll.intersect(List.of(1, 2, null, 4)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).intersect(List.of(2));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void map() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).map((Function<? super Integer, Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).map((IndexedFunction<? super Integer, Object>) null));
    var l = List.of(1, 2, 3);
    test(List.of(2, 3, 4), () -> l, ll -> ll.map(x -> x + 1));

    assertFalse(l.append(null).map(x -> x + 1).isEmpty());
    assertEquals(4, l.append(null).map(x -> x + 1).size());
    assertEquals(4, l.append(null).map(x -> x + 1).get(2));
    assertEquals(2, l.append(null).map(x -> x + 1).get(0));
    assertThrows(NullPointerException.class, () -> l.append(null).map(x -> x + 1).get(3));

    test(List.of(), List::<Integer>of, ll -> ll.map(x -> x + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).map((n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).map((n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).map(i -> {
        Thread.sleep(60000);
        return i;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void mapAfter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapAfter(0, (Function<? super Integer, Integer>) null));
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context)
        .mapAfter(0, (IndexedFunction<? super Integer, Integer>) null));
    var l = List.of(1, 2, 3);
    test(List.of(1, 2, 3), () -> l, ll -> ll.mapAfter(-1, x -> x + 1));
    test(List.of(2, 2, 3), () -> l, ll -> ll.mapAfter(0, x -> x + 1));
    test(List.of(1, 3, 3), () -> l, ll -> ll.mapAfter(1, x -> x + 1));
    test(List.of(1, 2, 4), () -> l, ll -> ll.mapAfter(2, x -> x + 1));
    test(List.of(1, 2, 3), () -> l, ll -> ll.mapAfter(3, x -> x + 1));
    test(List.of(1, 2, 3, null), () -> l, ll -> ll.append(null).mapAfter(-1, x -> x + 1));
    test(List.of(1, 3, 3, null), () -> l, ll -> ll.append(null).mapAfter(1, x -> x + 1));

    assertFalse(l.toFuture(context).append(null).mapAfter(3, x -> x + 1).isEmpty());
    assertEquals(4, l.toFuture(context).append(null).mapAfter(3, x -> x + 1).size());
    assertEquals(2, l.toFuture(context).append(null).mapAfter(3, x -> x + 1).get(1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).append(null).mapAfter(3, x -> x + 1).get(3));

    test(List.of(), List::<Integer>of, ll -> ll.mapAfter(0, x -> x + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).mapAfter(2, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).mapAfter(2, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).mapAfter(0, i -> {
        Thread.sleep(60000);
        return i;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void mapFirstWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapFirstWhere(i -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapFirstWhere(null, i -> i));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapFirstWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapFirstWhere(null, (n, i) -> i));
    var l = List.of(1, 2, null, 4);
    test(l, () -> l, ll -> ll.mapFirstWhere(i -> false, i -> i + 1));
    test(List.of(2, 2, null, 4), () -> l, ll -> ll.mapFirstWhere(i -> true, i -> i + 1));
    test(List.of(1, 2, 3, 4), () -> l, ll -> ll.mapFirstWhere(Objects::isNull, i -> 3));
    test(List.of(2, 2, null, 4), () -> l, ll -> ll.mapFirstWhere(i -> i == 1, i -> i + 1));

    assertFalse(l.toFuture(context).mapFirstWhere(i -> i > 2, i -> 1).isEmpty());
    assertEquals(4, l.toFuture(context).mapFirstWhere(i -> i > 2, i -> 1).size());
    assertEquals(1, l.toFuture(context).mapFirstWhere(i -> i > 2, i -> 1).get(0));
    assertEquals(2, l.toFuture(context).mapFirstWhere(i -> i > 2, i -> 1).get(1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).mapFirstWhere(i -> i > 2, i -> 1).get(2));

    test(List.of(), List::<Integer>of, ll -> ll.mapFirstWhere(i -> false, i -> i + 1));
    test(List.of(), List::<Integer>of, ll -> ll.mapFirstWhere(i -> true, i -> i + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).mapFirstWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).mapFirstWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e))
          .mapFirstWhere(i -> true, i -> {
            Thread.sleep(60000);
            return i;
          });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void mapLastWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapLastWhere(i -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapLastWhere(null, i -> i));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapLastWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapLastWhere(null, (n, i) -> i));
    var l = List.of(1, 2, null, 4);
    test(l, () -> l, ll -> ll.mapLastWhere(i -> false, i -> i + 1));
    test(List.of(1, 2, null, 5), () -> l, ll -> ll.mapLastWhere(i -> true, i -> i + 1));
    test(List.of(1, 2, 3, 4), () -> l, ll -> ll.mapLastWhere(Objects::isNull, i -> 3));
    test(List.of(1, 2, null, 5), () -> l, ll -> ll.mapLastWhere(i -> i == 4, i -> i + 1));

    assertFalse(l.toFuture(context).mapLastWhere(i -> i < 2, i -> 1).isEmpty());
    assertEquals(4, l.toFuture(context).mapLastWhere(i -> i < 2, i -> 1).size());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).mapLastWhere(i -> i < 2, i -> 1).get(0));

    test(List.of(), List::<Integer>of, ll -> ll.mapLastWhere(i -> false, i -> i + 1));
    test(List.of(), List::<Integer>of, ll -> ll.mapLastWhere(i -> true, i -> i + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).mapLastWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).mapLastWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e))
          .mapLastWhere(i -> true, i -> {
            Thread.sleep(60000);
            return i;
          });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void mapWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapWhere(i -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapWhere(null, i -> i));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).mapWhere(null, (n, i) -> i));
    var l = List.of(1, 2, 3, 4);
    test(l, () -> l, ll -> ll.mapWhere(i -> false, i -> i + 1));
    test(List.of(2, 3, 4, 5), () -> l, ll -> ll.mapWhere(i -> true, i -> i + 1));
    test(List.of(1, 3, 3, 4), () -> l, ll -> ll.mapWhere(i -> i == 2, i -> 3));

    assertFalse(l.toFuture(context).append(null).mapWhere(i -> i == 4, i -> i + 1).isEmpty());
    assertEquals(5, l.toFuture(context).append(null).mapWhere(i -> i == 4, i -> i + 1).size());
    assertEquals(2, l.toFuture(context).append(null).mapWhere(i -> i == 4, i -> i + 1).get(1));
    assertEquals(3, l.toFuture(context).append(null).mapWhere(i -> i == 4, i -> i + 1).get(2));
    assertEquals(5, l.toFuture(context).append(null).mapWhere(i -> i == 4, i -> i + 1).get(3));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).append(null).mapWhere(i -> i == 4, i -> i + 1).get(4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.toFuture(context).append(null).mapWhere(i -> i == 4, i -> i + 1).get(5));

    test(List.of(), List::<Integer>of, ll -> ll.mapWhere(i -> false, i -> i + 1));
    test(List.of(), List::<Integer>of, ll -> ll.mapWhere(i -> true, i -> i + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).mapWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).mapWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).mapWhere(i -> true, i -> {
        Thread.sleep(60000);
        return i;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void max() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).max(null));
    var l = List.of(1, 4, 2, 3);
    test(List.of(4), () -> l, ll -> ll.max(Integer::compareTo));

    assertFalse(List.of(1, null).toFuture(context).max(Integer::compareTo).isEmpty());
    assertTrue(List.of(1, null).toFuture(context).max(Integer::compareTo).notEmpty());
    assertEquals(1, List.of(1, null).toFuture(context).max(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> List.of(1, null).toFuture(context).max(Integer::compareTo).first());

    test(List.of(), List::<Integer>of, ll -> ll.max(Integer::compareTo));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).max(Integer::compare);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void min() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).min(null));
    var l = List.of(1, 4, 2, 3);
    test(List.of(1), () -> l, ll -> ll.min(Integer::compareTo));

    assertFalse(List.of(1, null).toFuture(context).min(Integer::compareTo).isEmpty());
    assertTrue(List.of(1, null).toFuture(context).min(Integer::compareTo).notEmpty());
    assertEquals(1, List.of(1, null).toFuture(context).min(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> List.of(1, null).toFuture(context).min(Integer::compareTo).first());

    test(List.of(), List::<Integer>of, ll -> ll.min(Integer::compareTo));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).min(Integer::compare);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void none() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).none((Predicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).none((IndexedPredicate<? super Integer>) null));
    test(List.of(true), List::of, ll -> ll.none(Objects::nonNull));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.none(i -> i < 3));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.none(i -> i < 0));
    var l = List.of(1, null, 3).toFuture(context).none(i -> i < 0);
    assertThrows(NullPointerException.class, l::first);
    l = List.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e)).none(i -> i < 0);
    assertThrows(NullPointerException.class, l::first);
//    var itr = l.iterator();
//    assertTrue(itr.hasNext());
//    assertThrows(NullPointerException.class, itr::next);
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).none((n, i) -> {
      indexes.add(n);
      return false;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).none((n, i) -> {
      indexes.add(n);
      return false;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).none(i -> {
        Thread.sleep(60000);
        return true;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void notAll() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).notAll((Predicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).notAll((IndexedPredicate<? super Integer>) null));
    test(List.of(true), List::of, ll -> ll.notAll(Objects::isNull));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.notAll(i -> i > 3));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.notAll(i -> i > 0));
    var l = List.of(1, null, 3).toFuture(context).notAll(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    l = List.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e)).notAll(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
//    var itr = l.iterator();
//    assertTrue(itr.hasNext());
//    assertThrows(NullPointerException.class, itr::next);
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).notAll((n, i) -> {
      indexes.add(n);
      return true;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).flatMap(e -> List.of(e)).notAll((n, i) -> {
      indexes.add(n);
      return true;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).notAll(i -> {
        Thread.sleep(60000);
        return true;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void orElse() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of().toFuture(context).orElse(null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findIndexOf(0).orElse(null));
    test(List.of(1), () -> List.of(1), ll -> ll.orElse(List.of(2)));
    test(List.of(1), () -> List.of(1), ll -> ll.orElse(List.of()));
    test(List.of(2), List::of, ll -> ll.orElse(List.of(2)));
    test(List.of(), List::of, ll -> ll.orElse(List.of()));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).orElse(List.of(1));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void orElseGet() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of().toFuture(context).orElseGet(null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).findIndexOf(0).orElseGet(null));
    Supplier<List<Integer>> supplier = () -> List.of(2);
    test(List.of(1), () -> List.of(1), ll -> ll.orElseGet(supplier));
    test(List.of(1), () -> List.of(1), ll -> ll.orElseGet(List::of));
    test(List.of(2), List::of, ll -> ll.orElseGet(supplier));
    test(List.of(), List::of, ll -> ll.orElseGet(List::of));

    Supplier<List<Integer>> throwing = () -> {
      throw new IllegalStateException();
    };
    test(List.of(1), () -> List.of(1), ll -> ll.orElseGet(throwing));
    assertThrows(IllegalStateException.class,
        () -> List.of().toFuture(context).flatMap(e -> List.of(e)).orElseGet(throwing).isEmpty());
    assertThrows(IllegalStateException.class,
        () -> List.of().toFuture(context).flatMap(e -> List.of(e)).orElseGet(throwing).notEmpty());
    assertThrows(IllegalStateException.class,
        () -> List.of().toFuture(context).flatMap(e -> List.of(e)).orElseGet(throwing).size());
    assertThrows(IllegalStateException.class,
        () -> List.of().toFuture(context).flatMap(e -> List.of(e)).orElseGet(throwing).first());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).orElseGet(() -> List.of(1));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void reduceLeft() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0, 0).toFuture(context).reduceLeft(null));
    assertThrows(NullPointerException.class,
        () -> List.of(0, 0).toFuture(context).findIndexOf(0).reduceLeft(null));
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(15), () -> l, ll -> ll.reduceLeft(Integer::sum));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMap(e -> List.of(e)).append(null).reduceLeft(Integer::sum)
            .first());
    test(List.of(), List::<Integer>of, ll -> ll.reduceLeft(Integer::sum));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).reduceLeft((n, i) -> {
        Thread.sleep(60000);
        return i;
      });
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void prepend() throws Exception {
    test(List.of(3, 2, 1), List::<Integer>of, ll -> ll.prepend(1).prepend(2).prepend(3));
    test(List.of(3, null, 1), List::<Integer>of, ll -> ll.prepend(1).prepend(null).prepend(3));
    test(List.of(3, 2, 1), () -> List.of(1), ll -> ll.prepend(2).prepend(3));
    test(List.of(3, null, 1), () -> List.of(1), ll -> ll.prepend(null).prepend(3));
    test(List.of(3, 1, 2), () -> List.of(1, 2), ll -> ll.prepend(3));
    test(List.of(3, 1, null), () -> List.of(1, null), ll -> ll.prepend(3));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).prepend(0);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void prependAll() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).prependAll(null));
    test(List.of(1, 2, 3), List::<Integer>of, ll -> ll.prependAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), List::<Integer>of, ll -> ll.prependAll(List.of(1, null, 3)));
    test(List.of(2, 3, 1), () -> List.of(1),
        ll -> ll.prependAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(null, 3, 1), () -> List.of(1), ll -> ll.prependAll(List.of(null, 3)));
    test(List.of(3, 1, 2), () -> List.of(1, 2), ll -> ll.prependAll(Set.of(3)));
    test(List.of(3, 1, null), () -> List.of(1, null), ll -> ll.prependAll(Set.of(3)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).prependAll(List.of(0));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void slice() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(), () -> l, ll -> ll.slice(1, 1));
    test(List.of(), () -> l, ll -> ll.slice(1, 0));
    test(List.of(), () -> l, ll -> ll.slice(1, -3));
    test(List.of(), () -> l, ll -> ll.slice(1, -4));
    test(List.of(), () -> l, ll -> ll.slice(1, -5));
    test(List.of(), () -> l, ll -> ll.slice(-1, 1));
    test(List.of(), () -> l, ll -> ll.slice(-1, 3));
    test(List.of(), () -> l, ll -> ll.slice(-1, -1));
    test(List.of(), () -> l, ll -> ll.slice(-1, -4));
    test(List.of(2, null), () -> l, ll -> ll.slice(1, -1));
    test(List.of(2), () -> l, ll -> ll.slice(1, -2));
    test(List.of(2, null), () -> l, ll -> ll.slice(1, 3));
    test(List.of(2), () -> l, ll -> ll.slice(1, 2));
    test(List.of(4), () -> l, ll -> ll.slice(-1, 4));
    test(List.of(null), () -> l, ll -> ll.slice(-2, -1));
    test(List.of(1, 2, null, 4), () -> l, ll -> ll.slice(0, Integer.MAX_VALUE));
    test(List.of(), List::of, ll -> ll.slice(1, -1));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).slice(1);
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void symmetricDiff() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).symmetricDiff(null));
    test(List.of(2, 4), () -> List.of(1, 2, null, 4), ll -> ll.symmetricDiff(List.of(1, null)));
    test(List.of(2, null), () -> List.of(1, 2, null, 4), ll -> ll.symmetricDiff(List.of(1, 4)));
    test(List.of(2, null, 3), () -> List.of(1, 2, null, 4),
        ll -> ll.symmetricDiff(List.of(1, 3, 4)));
    test(List.of(2, null, 4, 3, 3), () -> List.of(1, 2, null, 4),
        ll -> ll.symmetricDiff(List.of(3, 1, 3)));
    test(List.of(1, 2, 4, null), () -> List.of(1, 2, null, 4),
        ll -> ll.symmetricDiff(List.of(null, null)));
    test(List.of(1, 2, 4, null), () -> List.of(1, 1, 2, null, 4),
        ll -> ll.symmetricDiff(List.of(null, null, 1)));
    test(List.of(), () -> List.of(1, null), ll -> ll.symmetricDiff(List.of(1, null)));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4), ll -> ll.symmetricDiff(List.of()));
    test(List.of(1, 1, 2, null, 4), () -> List.of(1, 1, 2, null, 4),
        ll -> ll.symmetricDiff(List.of()));
    test(List.of(1, 2, null, 4), List::of, ll -> ll.symmetricDiff(List.of(1, 2, null, 4)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).symmetricDiff(List.of(1));
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
      assertFalse(f.isFailed());
    }
  }

  @Test
  public void union() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).toFuture(context).union(null));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4), ll -> ll.union(List.of(1, null)));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4), ll -> ll.union(List.of(1, 4)));
    test(List.of(1, 2, null, 4, 3), () -> List.of(1, 2, null, 4), ll -> ll.union(List.of(1, 3, 4)));
    test(List.of(1, 2, null, 4, 3, 3), () -> List.of(1, 2, null, 4),
        ll -> ll.union(List.of(3, 1, 3)));
    test(List.of(1, 2, null, 4, null), () -> List.of(1, 2, null, 4),
        ll -> ll.union(List.of(null, null)));
    test(List.of(1, null, 2, 4), () -> List.of(1, null), ll -> ll.union(List.of(1, 2, null, 4)));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4), ll -> ll.union(List.of(2, 1)));
    test(List.of(1, null, 2, 4), () -> List.of(1, null), ll -> ll.union(List.of(2, 4)));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4), ll -> ll.union(List.of()));
    test(List.of(1, 2, null, 4), List::of, ll -> ll.union(List.of(1, 2, null, 4)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).flatMap(i -> {
        Thread.sleep(60000);
        return List.of(i);
      }).union(List.of(1));
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
      assertFalse(f.isFailed());
    }
  }

  private <E, F> void test(@NotNull final java.util.List<F> expected,
      @NotNull final Supplier<? extends List<E>> baseSupplier,
      @NotNull final Function<future.List<E>, future.List<? extends F>> actualTransformer)
      throws Exception {
//    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(context)));
    test(expected, () -> actualTransformer.apply(
        baseSupplier.get().toFuture(context).flatMapWhere(e -> false, e -> List.of())));
    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(throughputContext)));
    test(expected, () -> actualTransformer.apply(
        baseSupplier.get().toFuture(throughputContext).flatMapWhere(e -> false, e -> List.of())));
  }

  // TODO: add args validation + isCancelled, isFailed
  private <E> void test(@NotNull final java.util.List<E> expected,
      @NotNull final Supplier<? extends future.List<? extends E>> actualSupplier) throws Exception {
    assertEquals(expected.isEmpty(), actualSupplier.get().isEmpty());
    assertEquals(!expected.isEmpty(), actualSupplier.get().notEmpty());
    assertEquals(expected.size(), actualSupplier.get().size());
    assertEquals(expected, actualSupplier.get());
    assertThrows(IndexOutOfBoundsException.class, () -> actualSupplier.get().get(-1));
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), actualSupplier.get().get(i));
    }
    assertThrows(IndexOutOfBoundsException.class, () -> actualSupplier.get().get(expected.size()));
    assertThrows(IndexOutOfBoundsException.class,
        () -> actualSupplier.get().get(Integer.MIN_VALUE));
    assertThrows(IndexOutOfBoundsException.class,
        () -> actualSupplier.get().get(Integer.MAX_VALUE));
    var list = actualSupplier.get();
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), list.get(i));
    }
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(expected.size()));
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(Integer.MIN_VALUE));
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(Integer.MAX_VALUE));
    for (final E element : expected) {
      assertTrue(actualSupplier.get().contains(element));
    }
    var lst = actualSupplier.get();
    assertFalse(lst.isCancelled());
    assertFalse(lst.isFailed());
    assertEquals(expected, lst.get());
    assertTrue(lst.isDone());
    assertFalse(lst.isCancelled());
    assertFalse(lst.isFailed());
    lst = actualSupplier.get();
    for (final E element : expected) {
      assertTrue(lst.contains(element));
    }
    var itr = actualSupplier.get().iterator();
    for (final E element : expected) {
      assertTrue(itr.hasNext());
      assertEquals(element, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
    }
    assertFalse(itr.hasNext());
    assertThrows(NoSuchElementException.class, itr::next);
  }
}
