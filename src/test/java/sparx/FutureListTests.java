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
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public class FutureListTests {

  private static final boolean TEST_ASYNC_CANCEL = true;

  private ExecutorContext context;
  private ExecutorService executor;

  @BeforeEach
  public void setUp() {
    executor = Executors.newCachedThreadPool();
    context = ExecutorContext.of(executor);
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
      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
        Thread.sleep(60000);
        return true;
      }).append(true);
      executor.submit(() -> {
        try {
          Thread.sleep(1000);
        } catch (final InterruptedException e) {
          throw UncheckedInterruptedException.toUnchecked(e);
        }
        f.cancel(true);
      });
      assertThrows(CancellationException.class, f::get);
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
      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
        Thread.sleep(60000);
        return true;
      }).appendAll(List.of(true, false));
      executor.submit(() -> {
        try {
          Thread.sleep(1000);
        } catch (final InterruptedException e) {
          throw UncheckedInterruptedException.toUnchecked(e);
        }
        f.cancel(true);
      });
      assertThrows(CancellationException.class, f::get);
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
        () -> List.of(0).toFuture(context).count((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).count((IndexedPredicate<? super Object>) null));
    test(List.of(0), List::of, ll -> ll.count(Objects::nonNull));
    test(List.of(2), () -> List.of(1, 2, 3), ll -> ll.count(i -> i < 3));
    test(List.of(3), () -> List.of(1, 2, 3), ll -> ll.count(i -> i > 0));
    var l = List.of(1, null, 3).toFuture(context).count(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      // TODO
//      var itr = l.iterator();
//      assertTrue(itr.hasNext());
//      assertThrows(NullPointerException.class, itr::next);
    }
    l = List.of(1, null, 3).toFuture(context).map(e -> e).count(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      // TODO
//      var itr = l.iterator();
//      assertTrue(itr.hasNext());
//      assertThrows(NullPointerException.class, itr::next);
    }
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 2, 1).toFuture(context).count((n, i) -> {
      indexes.add(n);
      return i < 2;
    }).first();
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 2, 1).toFuture(context).map(e -> e).count((n, i) -> {
      indexes.add(n);
      return i < 2;
    }).first();
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).count(i -> {
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
      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
        Thread.sleep(60000);
        return true;
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
    }
  }

  @Test
  public void doWhile() throws ExecutionException, InterruptedException {
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doWhile((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doWhile((IndexedPredicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doWhile((Predicate<? super Object>) null, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).toFuture(context).doWhile((IndexedPredicate<? super Object>) null, null));
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
      // TODO: uncomment when prepend is implemented
//      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
//        Thread.sleep(60000);
//        return true;
//      }).prepend(false).drop(1);
//      executor.submit(() -> {
//        try {
//          Thread.sleep(1000);
//        } catch (final InterruptedException e) {
//          throw UncheckedInterruptedException.toUnchecked(e);
//        }
//        f.cancel(true);
//      });
//      assertThrows(CancellationException.class, f::get);
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
      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
        Thread.sleep(60000);
        return true;
      }).append(false).dropRight(1);
      executor.submit(() -> {
        try {
          Thread.sleep(1000);
        } catch (final InterruptedException e) {
          throw UncheckedInterruptedException.toUnchecked(e);
        }
        f.cancel(true);
      });
      assertThrows(CancellationException.class, f::get);
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
        () -> List.of(1, null, 3).toFuture(context).map(e -> e).dropRightWhile(e -> e > 0).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).dropRightWhile((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 1), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).dropRightWhile((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 1), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).dropRightWhile(i -> {
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
        () -> List.of(1, null, 3).toFuture(context).map(e -> e).dropWhile(e -> e > 0).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).dropWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).dropWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).dropWhile(i -> {
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
    l = List.of(1, null, 3).toFuture(context).map(e -> e).each(i -> i > 0);
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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).each((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).each(i -> {
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
      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
        Thread.sleep(60000);
        return true;
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
    }
  }

  @Test
  public void exists() throws Exception {
    test(List.of(false), List::of, ll -> ll.exists(Objects::nonNull));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.exists(i -> i > 3));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.exists(i -> i > 3));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.exists(i -> i > 0));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.exists(i -> i > 0));
    var l = List.of(1, null, 3).toFuture(context).exists(i -> i > 1);
    assertThrows(NullPointerException.class, l::first);
    List.of(1, null, 3).toFuture(context).map(e -> e).exists(i -> i > 1);
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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).exists((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).exists(i -> {
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
    }
  }

  @Test
  public void filter() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(null), () -> l, ll -> ll.filter(Objects::isNull));
    test(List.of(1, 2, 4), () -> l, ll -> ll.filter(Objects::nonNull));
    test(List.of(1, 2), () -> l, ll -> ll.filter(Objects::nonNull).filter(i -> i < 3));
    test(List.of(4), () -> l, ll -> ll.filter(Objects::nonNull).filter(i -> i > 3));
    test(List.of(), () -> l, ll -> ll.filter(Objects::nonNull).filter(i -> i > 4));
    test(List.of(), List::of, ll -> ll.filter(Objects::isNull));
    assertThrows(NullPointerException.class, () -> l.toFuture(context).filter(i -> i > 4).size());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).filter(i -> i > 4).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).filter((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).filter((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).filter(i -> {
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
    }
  }

  @Test
  public void findAny() throws Exception {
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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).findAny((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).findAny(i -> {
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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).findAny((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
        Thread.sleep(60000);
        return true;
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
    }
  }

  @Test
  public void findIndexOfSlice() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(1), () -> l, ll -> ll.findIndexOfSlice(List.of(2, null)));
    test(List.of(2), () -> l, ll -> ll.findIndexOfSlice(List.of(null)));
    test(List.of(), () -> l, ll -> ll.findIndexOfSlice(List.of(null, 2)));
    test(List.of(0), () -> l, ll -> ll.findIndexOfSlice(List.of()));
    test(List.of(2), () -> List.of(1, 1, 1, 1, 2, 1), ll -> ll.findIndexOfSlice(List.of(1, 1, 2)));
    test(List.of(), List::of, ll -> ll.findIndexOfSlice(List.of(null)));
    test(List.of(0), List::of, ll -> ll.findIndexOfSlice(List.of()));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
        Thread.sleep(60000);
        return true;
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
    }
  }

  @Test
  public void findIndexWhere() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l, ll -> ll.findIndexWhere(Objects::isNull));
    test(List.of(1), () -> l, ll -> ll.findIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findIndexWhere(i -> i > 3).first());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).findIndexWhere(i -> i > 3).first());
    test(List.of(), List::of, ll -> ll.findIndexWhere(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).findIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).findIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).findIndexWhere(i -> {
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
    }
  }

  @Test
  public void findLast() throws Exception {
    var l = List.of(1, 2, null, 4, 5);
    test(List.of(null), () -> l, ll -> ll.findLast(Objects::isNull));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findLast(i -> i < 4).first());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).findLast(i -> i < 4).first());
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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).findLast((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(3, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).findLast(i -> {
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
      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
        Thread.sleep(60000);
        return true;
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
    }
  }

  @Test
  public void findLastIndexOfSlice() throws Exception {
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
      var f = List.of(1, 2, 3).toFuture(context).none(i -> {
        Thread.sleep(60000);
        return true;
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
    }
  }

  @Test
  public void findLastIndexWhere() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l, ll -> ll.findLastIndexWhere(Objects::isNull));
    test(List.of(3), () -> l, ll -> ll.findLastIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findLastIndexWhere(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).findLastIndexWhere(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).findLastIndexWhere(i -> i < 3).first());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).findLastIndexWhere(i -> i < 3).first());
    test(List.of(), List::of, ll -> ll.findLastIndexWhere(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).findLastIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(3, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).findLastIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(3, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).findLastIndexWhere(i -> {
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
    }
  }

  @Test
  public void flatMap() throws Exception {
    var l = List.of(1, 2);
    test(List.of(1, 1, 2, 2), () -> l, ll -> ll.flatMap(i -> List.of(i, i)));
    test(List.of(), () -> l, ll -> ll.flatMap(i -> List.of()));
    test(List.of(null, null), () -> l, ll -> ll.flatMap(i -> List.of(null)));
    assertNull(l.toFuture(context).flatMap(i -> List.of(null)).get(1));
    assertNull(l.toFuture(context).map(e -> e).flatMap(i -> List.of(null)).get(1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.toFuture(context).flatMap(i -> List.of(null)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.toFuture(context).map(e -> e).flatMap(i -> List.of(null)).get(2));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).toFuture(context).flatMap((n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).flatMap((n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).flatMap(i -> {
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
    }
  }

  @Test
  public void flatMapAfter() throws Exception {
    var l = List.of(1, 2);
    test(l, () -> l, ll -> ll.flatMapAfter(-1, i -> List.of(i, i)));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.toFuture(context).flatMapAfter(2, i -> List.of(i, i)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.toFuture(context).map(e -> e).flatMapAfter(2, i -> List.of(i, i)).get(2));
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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).flatMapAfter(1, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(1), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).flatMapAfter(0, i -> {
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
    }
  }

  @Test
  public void flatMapFirstWhere() throws Exception {
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
    // TODO: materializeUntil???
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(0));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(2));
    assertFalse(l.toFuture(context).map(e -> e).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i))
        .isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i))
            .size());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i))
            .get(0));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i))
            .get(1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i))
            .get(2));

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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).flatMapFirstWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).flatMapFirstWhere(i -> true, i -> {
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
    }
  }

  @Test
  public void flatMapLastWhere() throws Exception {
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
    assertFalse(
        l.toFuture(context).map(e -> e).flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapLastWhere(i -> i < 2, i -> List.of(i, i))
            .size());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapLastWhere(i -> i < 2, i -> List.of(i, i))
            .get(3));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapLastWhere(i -> i < 2, i -> List.of(i, i))
            .get(2));

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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).flatMapLastWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 2), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).flatMapLastWhere(i -> true, i -> {
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
    }
  }

  @Test
  public void flatMapWhere() throws Exception {
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
        l.toFuture(context).map(e -> e).flatMapWhere(i -> i == 1, i -> List.of(i, i)).isEmpty());
    assertEquals(1,
        l.toFuture(context).map(e -> e).flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(0));
    assertEquals(1,
        l.toFuture(context).map(e -> e).flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(1));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapWhere(i -> i == 1, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(2));
    assertFalse(
        l.toFuture(context).map(e -> e).flatMapWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1,
        l.toFuture(context).map(e -> e).flatMapWhere(i -> i > 2, i -> List.of(i, i)).get(0));
    assertThrows(NullPointerException.class,
        () -> l.toFuture(context).map(e -> e).flatMapWhere(i -> i > 2, i -> List.of(i, i)).get(1));

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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).flatMapWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).flatMapWhere(i -> true, i -> {
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
    }
  }

  @Test
  public void foldLeft() throws Exception {
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(16), () -> l, ll -> ll.foldLeft(1, Integer::sum));
    test(List.of(List.of(1, 2)), () -> List.of(1, 2),
        ll -> ll.foldLeft(List.<Integer>of(), List::append));
    test(List.of(1), List::<Integer>of, ll -> ll.foldLeft(1, Integer::sum));
    test(List.of(List.of()), List::of, ll -> ll.foldLeft(List.of(), List::append));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).foldLeft(0, (a, i) -> {
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
    }
  }

  @Test
  public void foldRight() throws Exception {
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(16), () -> l, ll -> ll.foldRight(1, Integer::sum));
    test(List.of(List.of(2, 1)), () -> List.of(1, 2),
        ll -> ll.foldRight(List.<Integer>of(), (i, li) -> li.append(i)));
    test(List.of(1), List::<Integer>of, ll -> ll.foldRight(1, Integer::sum));
    test(List.of(List.of()), List::of, ll -> ll.foldRight(List.of(), (i, li) -> li.append(i)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).foldRight(0, (i, a) -> {
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
    }
  }

  @Test
  public void group() throws Exception {
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
    }
  }

  @Test
  public void groupWithPadding() throws Exception {
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5)), () -> l,
        ll -> ll.group(1, null));
    test(List.of(List.of(1, 2), List.of(3, 4), List.of(5, null)), () -> l, ll -> ll.group(2, null));
    test(List.of(List.of(1, 2, 3), List.of(4, 5, -1)), () -> l, ll -> ll.group(3, -1));
    test(List.of(List.of(1, 2, 3, 4, 5, -1, -1, -1, -1, -1)), () -> l, ll -> ll.group(10, -1));

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
      var f = List.of(1, 2, 3).toFuture(context).map(i -> {
        Thread.sleep(60000);
        return i;
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
    }
  }

  @Test
  public void map() throws Exception {
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
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).map(i -> {
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
      var f = List.of(1, 2, 3).toFuture(context).map(i -> {
        Thread.sleep(60000);
        return i;
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
    }
  }

  @Test
  public void none() throws Exception {
    test(List.of(true), List::of, ll -> ll.none(Objects::nonNull));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.none(i -> i < 3));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.none(i -> i < 0));
    var l = List.of(1, null, 3).toFuture(context).none(i -> i < 0);
    assertThrows(NullPointerException.class, l::first);
    l = List.of(1, null, 3).toFuture(context).map(e -> e).none(i -> i < 0);
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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).none((n, i) -> {
      indexes.add(n);
      return false;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).none(i -> {
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
    }
  }

  @Test
  public void notAll() throws Exception {
    test(List.of(true), List::of, ll -> ll.notAll(Objects::isNull));
    test(List.of(true), () -> List.of(1, 2, 3), ll -> ll.notAll(i -> i > 3));
    test(List.of(false), () -> List.of(1, 2, 3), ll -> ll.notAll(i -> i > 0));
    var l = List.of(1, null, 3).toFuture(context).notAll(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    l = List.of(1, null, 3).toFuture(context).map(e -> e).notAll(i -> i > 0);
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
    List.of(1, 2, 3, 4).toFuture(context).map(e -> e).notAll((n, i) -> {
      indexes.add(n);
      return true;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).map(e -> e).notAll(i -> {
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
    }
  }

  @Test
  public void union() throws Exception {
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
      var f = List.of(1, 2, 3).toFuture(context).map(i -> {
        Thread.sleep(60000);
        return i;
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
    }
  }

  private <E, F> void test(@NotNull final java.util.List<F> expected,
      @NotNull final Supplier<? extends List<E>> baseSupplier,
      @NotNull final Function<future.List<E>, future.List<? extends F>> actualTransformer)
      throws Exception {
    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(context)));
    test(expected, () -> actualTransformer.apply(baseSupplier.get().toFuture(context).map(e -> e)));
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
