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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sparx.Sparx.lazy.List;
import sparx.concurrent.ExecutorContext;
import sparx.util.UncheckedException.UncheckedInterruptedException;

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
  public void all() {
    assertFalse(List.of().toFuture(context).all(Objects::nonNull).isEmpty());
    assertTrue(List.of().toFuture(context).all(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().toFuture(context).all(Objects::nonNull).size());
    assertTrue(List.of().toFuture(context).all(Objects::nonNull).first());
    assertFalse(List.of(1, 2, 3).toFuture(context).all(i -> i < 3).first());
    {
      var itr = List.of(1, 2, 3).toFuture(context).all(i -> i < 3).iterator();
      assertTrue(itr.hasNext());
      assertFalse(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertTrue(List.of(1, 2, 3).toFuture(context).all(i -> i > 0).first());
    {
      var itr = List.of(1, 2, 3).toFuture(context).all(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertTrue(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).toFuture(context).all(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      // TODO
//      var itr = l.iterator();
//      assertThrows(NullPointerException.class, itr::hasNext);
//      assertThrows(NullPointerException.class, itr::next);
    }

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void append() {
    var l = List.<Integer>of().toFuture(context).append(1).append(2).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.<Integer>of().toFuture(context).append(1).append(null).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1).toFuture(context).append(2).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1).toFuture(context).append(null).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2).toFuture(context).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1, null).toFuture(context).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void appendAll() {
    var l = List.<Integer>of().toFuture(context).appendAll(Arrays.asList(1, 2, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.<Integer>of().toFuture(context).appendAll(List.of(1, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1).toFuture(context).appendAll(new LinkedHashSet<>(List.of(2, 3)));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1).toFuture(context).appendAll(List.of(null, 3).toFuture(context));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2).toFuture(context).appendAll(Set.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1, null).toFuture(context).appendAll(Set.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void count() {
    assertFalse(List.of().toFuture(context).count().isEmpty());
    assertTrue(List.of().toFuture(context).count().notEmpty());
    assertEquals(1, List.of().toFuture(context).count().size());
    assertEquals(0, List.of().toFuture(context).count().first());
    assertEquals(3, List.of(1, 2, 3).toFuture(context).count().first());
    {
      var itr = List.of(1, 2, 3).count().iterator();
      assertTrue(itr.hasNext());
      assertEquals(3, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertEquals(3, List.of(1, null, 3).toFuture(context).count().first());
    {
      var itr = List.of(1, null, 3).count().iterator();
      assertTrue(itr.hasNext());
      assertEquals(3, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }

    if (TEST_ASYNC_CANCEL) {
      // TODO
    }
  }

  @Test
  public void countWhere() {
    assertFalse(List.of().toFuture(context).count(Objects::nonNull).isEmpty());
    assertTrue(List.of().toFuture(context).count(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().toFuture(context).count(Objects::nonNull).size());
    assertEquals(0, List.of().toFuture(context).count(Objects::nonNull).first());
    assertEquals(2, List.of(1, 2, 3).toFuture(context).count(i -> i < 3).first());
    {
      var itr = List.of(1, 2, 3).count(i -> i < 3).iterator();
      assertTrue(itr.hasNext());
      assertEquals(2, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertEquals(3, List.of(1, 2, 3).toFuture(context).count(i -> i > 0).first());
    {
      var itr = List.of(1, 2, 3).count(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertEquals(3, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).toFuture(context).count(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      // TODO
//      var itr = l.iterator();
//      assertThrows(NullPointerException.class, itr::hasNext);
//      assertThrows(NullPointerException.class, itr::next);
    }

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).count(i -> {
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
  public void diff() {
    assertEquals(List.of(2, 4), List.of(1, 2, null, 4).toFuture(context).diff(List.of(1, null)));
    assertEquals(List.of(2, null), List.of(1, 2, null, 4).toFuture(context).diff(List.of(1, 4)));
    assertEquals(List.of(2, null), List.of(1, 2, null, 4).toFuture(context).diff(List.of(1, 3, 4)));
    assertEquals(List.of(2, null, 4),
        List.of(1, 2, null, 4).toFuture(context).diff(List.of(3, 1, 3)));
    assertEquals(List.of(1, 2, 4),
        List.of(1, 2, null, 4).toFuture(context).diff(List.of(null, null)));
    assertEquals(List.of(), List.of(1, null).toFuture(context).diff(List.of(1, 2, null, 4)));

    assertEquals(List.of(1, 2, null, 4), List.of(1, 2, null, 4).toFuture(context).diff(List.of()));
    assertEquals(List.of(), List.of().toFuture(context).diff(List.of(1, 2, null, 4)));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void doFor() {
    var list = new ArrayList<>();
    List.of(1, 2, 3).toFuture(context).doFor(e -> list.add(e));
    assertEquals(List.of(1, 2, 3), list);

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
  public void doWhile() {
    var list = new ArrayList<>();
    List.of(1, 2, 3).toFuture(context).doWhile(e -> e < 3, list::add);
    assertEquals(List.of(1, 2), list);
    list.clear();
    List.of(1, 2, 3).toFuture(context).doWhile(e -> {
      list.add(e);
      return e < 2;
    });
    assertEquals(List.of(1, 2), list);

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
  public void drop() {
    var l = List.<Integer>of().toFuture(context).drop(1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().toFuture(context).drop(0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().toFuture(context).drop(-1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    l = List.of(1, null, 3).toFuture(context).drop(1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(null, 3), l);
    l = List.of(1, null, 3).toFuture(context).drop(2);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertEquals(List.of(3), l);
    l = List.of(1, null, 3).toFuture(context).drop(3);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).toFuture(context).drop(4);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).toFuture(context).drop(0);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).toFuture(context).drop(-1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
        Thread.sleep(60000);
        return true;
      }).append(false).drop(1);
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
  public void dropRight() {
    var l = List.<Integer>of().toFuture(context).dropRight(1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().toFuture(context).dropRight(0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().toFuture(context).dropRight(-1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    l = List.of(1, null, 3).toFuture(context).dropRight(1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(1, null), l);
    l = List.of(1, null, 3).toFuture(context).dropRight(2);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertEquals(List.of(1), l);
    l = List.of(1, null, 3).toFuture(context).dropRight(3);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).toFuture(context).dropRight(4);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).toFuture(context).dropRight(0);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).toFuture(context).dropRight(-1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void dropRightWhile() {
    var l = List.<Integer>of().toFuture(context).dropRightWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());

    l = List.of(1, null, 3).toFuture(context).dropRightWhile(Objects::isNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).toFuture(context).dropRightWhile(Objects::nonNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(1, null), l);
    l = List.of(1, null, 3).toFuture(context).dropRightWhile(e -> e < 1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2, 3).toFuture(context).dropRightWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).dropRightWhile(e -> e > 0).size());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).dropRightWhile(i -> {
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
  public void dropWhile() {
    var l = List.<Integer>of().toFuture(context).dropWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());

    l = List.of(1, null, 3).toFuture(context).dropWhile(Objects::isNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).toFuture(context).dropWhile(Objects::nonNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(null, 3), l);
    l = List.of(1, null, 3).toFuture(context).dropWhile(e -> e < 1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2, 3).toFuture(context).dropWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).dropWhile(e -> e > 0).size());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).dropWhile(i -> {
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
  public void endsWith() {
    var l = List.<Integer>of().toFuture(context).endsWith(List.of());
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.<Integer>of().toFuture(context).endsWith(List.of(1));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    l = List.of(1, null, 3).toFuture(context).endsWith(List.of());
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).toFuture(context).endsWith(List.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).toFuture(context).endsWith(List.of(null));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    l = List.of(1, null, 3).toFuture(context).endsWith(List.of(null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).toFuture(context).endsWith(List.of(1, null));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    l = List.of(1, null, 3).toFuture(context).endsWith(List.of(1, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).toFuture(context).endsWith(List.of(null, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void exists() {
    assertFalse(List.of().toFuture(context).exists(Objects::nonNull).isEmpty());
    assertTrue(List.of().toFuture(context).exists(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().toFuture(context).exists(Objects::nonNull).size());
    assertFalse(List.of().toFuture(context).exists(Objects::nonNull).first());
    assertFalse(List.of(1, 2, 3).toFuture(context).exists(i -> i > 3).first());
    {
      var itr = List.of(1, 2, 3).toFuture(context).exists(i -> i > 3).iterator();
      assertTrue(itr.hasNext());
      assertFalse(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertTrue(List.of(1, 2, 3).toFuture(context).exists(i -> i > 0).first());
    {
      var itr = List.of(1, 2, 3).toFuture(context).exists(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertTrue(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).toFuture(context).exists(i -> i > 1);
    assertThrows(NullPointerException.class, l::first);
    {
      // TODO
//      var itr = l.iterator();
//      assertThrows(NullPointerException.class, itr::hasNext);
//      assertThrows(NullPointerException.class, itr::next);
    }

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).exists(i -> {
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
  public void filter() {
    var l = List.of(1, 2, null, 4).toFuture(context);
    assertFalse(l.filter(Objects::nonNull).isEmpty());
    assertEquals(List.of(1, 2, 4), l.filter(Objects::nonNull));
    assertEquals(List.of(1, 2), l.filter(Objects::nonNull).filter(i -> i < 3));
    assertEquals(List.of(4), l.filter(Objects::nonNull).filter(i -> i > 3));
    assertEquals(List.of(), l.filter(Objects::nonNull).filter(i -> i > 4));
    assertThrows(NullPointerException.class, () -> l.filter(i -> i > 4).size());

    assertTrue(List.of().toFuture(context).filter(Objects::isNull).isEmpty());
    assertEquals(0, List.of().toFuture(context).filter(Objects::isNull).size());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).filter(i -> {
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
  public void findAny() {
    var l = List.of(1, 2, null, 4).toFuture(context);
    assertFalse(l.findAny(Objects::isNull).isEmpty());
    assertEquals(1, l.findAny(Objects::isNull).size());
    assertEquals(List.of(null), l.findAny(Objects::isNull));
    assertFalse(l.findAny(i -> i < 4).isEmpty());
    assertEquals(1, l.findAny(i -> i < 4).size());

    assertTrue(List.of().toFuture(context).findAny(Objects::isNull).isEmpty());
    assertEquals(0, List.of().toFuture(context).findAny(Objects::isNull).size());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).findAny(i -> {
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
  public void findIndexOf() {
    var l = List.of(1, 2, null, 4).toFuture(context);
    assertFalse(l.findIndexOf(null).isEmpty());
    assertEquals(1, l.findIndexOf(null).size());
    assertEquals(2, l.findIndexOf(null).first());
    assertEquals(List.of(2), l.findIndexOf(null));
    assertFalse(l.findIndexOf(4).isEmpty());
    assertEquals(1, l.findIndexOf(4).size());
    assertEquals(3, l.findIndexOf(4).first());
    assertEquals(List.of(3), l.findIndexOf(4));
    assertTrue(l.findIndexOf(3).isEmpty());
    assertEquals(0, l.findIndexOf(3).size());
    assertThrows(IndexOutOfBoundsException.class, () -> l.findIndexOf(3).first());
    assertEquals(List.of(), l.findIndexOf(3));

    assertTrue(List.of().toFuture(context).findIndexOf(null).isEmpty());
    assertEquals(0, List.of().toFuture(context).findIndexOf(null).size());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void findIndexOfSlice() {
    var l = List.of(1, 2, null, 4).toFuture(context);
    assertFalse(l.findIndexOfSlice(List.of(2, null)).isEmpty());
    assertEquals(1, l.findIndexOfSlice(List.of(2, null)).size());
    assertEquals(1, l.findIndexOfSlice(List.of(2, null)).first());
    assertEquals(List.of(1), l.findIndexOfSlice(List.of(2, null)));
    assertFalse(l.findIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(1, l.findIndexOfSlice(List.of(null)).size());
    assertEquals(2, l.findIndexOfSlice(List.of(null)).first());
    assertEquals(List.of(2), l.findIndexOfSlice(List.of(null)));
    assertTrue(l.findIndexOfSlice(List.of(null, 2)).isEmpty());
    assertEquals(0, l.findIndexOfSlice(List.of(null, 2)).size());
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.findIndexOfSlice(List.of(null, 2)).first());
    assertEquals(List.of(), l.findIndexOfSlice(List.of(null, 2)));
    assertFalse(l.findIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, l.findIndexOfSlice(List.of()).size());
    assertEquals(0, l.findIndexOfSlice(List.of()).first());
    assertEquals(List.of(0), l.findIndexOfSlice(List.of()));

    assertEquals(2,
        List.of(1, 1, 1, 1, 2, 1).toFuture(context).findIndexOfSlice(List.of(1, 1, 2)).first());

    assertTrue(List.of().toFuture(context).findIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(0, List.of().toFuture(context).findIndexOfSlice(List.of(null)).size());
    assertFalse(List.of().toFuture(context).findIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, List.of().toFuture(context).findIndexOfSlice(List.of()).size());
    assertEquals(0, List.of().toFuture(context).findIndexOfSlice(List.of()).first());
    assertEquals(List.of(0), List.of().toFuture(context).findIndexOfSlice(List.of()));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void findIndexWhere() {
    var l = List.of(1, 2, null, 4).toFuture(context);
    assertFalse(l.findIndexWhere(Objects::isNull).isEmpty());
    assertEquals(1, l.findIndexWhere(Objects::isNull).size());
    assertEquals(2, l.findIndexWhere(Objects::isNull).first());
    assertEquals(List.of(2), l.findIndexWhere(Objects::isNull));
    assertFalse(l.findIndexWhere(i -> i > 1).isEmpty());
    assertEquals(1, l.findIndexWhere(i -> i > 1).size());
    assertEquals(1, l.findIndexWhere(i -> i > 1).first());
    assertEquals(List.of(1), l.findIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class, () -> l.findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class, () -> l.findIndexWhere(i -> i > 3).first());

    assertTrue(List.of().toFuture(context).findIndexWhere(Objects::isNull).isEmpty());
    assertEquals(0, List.of().toFuture(context).findIndexWhere(Objects::isNull).size());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).findIndexWhere(i -> {
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
  public void findLast() {
    var l = List.of(1, 2, null, 4, 5).toFuture(context);
    assertFalse(l.findLast(Objects::isNull).isEmpty());
    assertEquals(1, l.findLast(Objects::isNull).size());
    assertNull(l.findLast(Objects::isNull).first());
    assertEquals(List.of(null), l.findLast(Objects::isNull));
    assertThrows(NullPointerException.class, () -> l.findLast(i -> i < 4).first());
    assertFalse(l.findLast(i -> i < 5).isEmpty());
    assertEquals(1, l.findLast(i -> i < 5).size());
    assertEquals(4, l.findLast(i -> i < 5).first());
    assertEquals(List.of(4), l.findLast(i -> i < 5));
    assertTrue(l.findLast(i -> i != null && i > 5).isEmpty());
    assertEquals(0, l.findLast(i -> i != null && i > 5).size());
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.findLast(i -> i != null && i > 5).first());
    assertEquals(List.of(), l.findLast(i -> i != null && i > 5));

    assertTrue(List.of().toFuture(context).findLast(Objects::isNull).isEmpty());
    assertEquals(0, List.of().toFuture(context).findLast(Objects::isNull).size());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).findLast(i -> {
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
  public void findLastIndexOf() {
    var l = List.of(1, 2, null, 4).toFuture(context);
    assertFalse(l.findLastIndexOf(null).isEmpty());
    assertEquals(1, l.findLastIndexOf(null).size());
    assertEquals(2, l.findLastIndexOf(null).first());
    assertEquals(List.of(2), l.findLastIndexOf(null));
    assertFalse(l.findLastIndexOf(4).isEmpty());
    assertEquals(1, l.findLastIndexOf(4).size());
    assertEquals(3, l.findLastIndexOf(4).first());
    assertEquals(List.of(3), l.findLastIndexOf(4));
    assertTrue(l.findLastIndexOf(3).isEmpty());
    assertEquals(0, l.findLastIndexOf(3).size());
    assertThrows(IndexOutOfBoundsException.class, () -> l.findLastIndexOf(3).first());
    assertEquals(List.of(), l.findLastIndexOf(3));

    assertTrue(List.of().toFuture(context).findLastIndexOf(null).isEmpty());
    assertEquals(0, List.of().toFuture(context).findLastIndexOf(null).size());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void findLastIndexOfSlice() {
    var l = List.of(1, 2, null, 4).toFuture(context);
    assertFalse(l.findLastIndexOfSlice(List.of(2, null)).isEmpty());
    assertEquals(1, l.findLastIndexOfSlice(List.of(2, null)).size());
    assertEquals(1, l.findLastIndexOfSlice(List.of(2, null)).first());
    assertEquals(List.of(1), l.findLastIndexOfSlice(List.of(2, null)));
    assertFalse(l.findLastIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(1, l.findLastIndexOfSlice(List.of(null)).size());
    assertEquals(2, l.findLastIndexOfSlice(List.of(null)).first());
    assertEquals(List.of(2), l.findLastIndexOfSlice(List.of(null)));
    assertTrue(l.findLastIndexOfSlice(List.of(null, 2)).isEmpty());
    assertEquals(0, l.findLastIndexOfSlice(List.of(null, 2)).size());
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.findLastIndexOfSlice(List.of(null, 2)).first());
    assertEquals(List.of(), l.findLastIndexOfSlice(List.of(null, 2)));
    assertFalse(l.findLastIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, l.findLastIndexOfSlice(List.of()).size());
    assertEquals(4, l.findLastIndexOfSlice(List.of()).first());
    assertEquals(List.of(4), l.findLastIndexOfSlice(List.of()));

    assertEquals(2,
        List.of(1, 1, 1, 1, 2, 1).toFuture(context).findLastIndexOfSlice(List.of(1, 1, 2)).first());

    assertTrue(List.of().toFuture(context).findLastIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(0, List.of().toFuture(context).findLastIndexOfSlice(List.of(null)).size());
    assertFalse(List.of().toFuture(context).findLastIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, List.of().toFuture(context).findLastIndexOfSlice(List.of()).size());
    assertEquals(0, List.of().toFuture(context).findLastIndexOfSlice(List.of()).first());
    assertEquals(List.of(0), List.of().toFuture(context).findLastIndexOfSlice(List.of()));

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).all(i -> {
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
  public void findLastIndexWhere() {
    var l = List.of(1, 2, null, 4).toFuture(context);
    assertFalse(l.findLastIndexWhere(Objects::isNull).isEmpty());
    assertEquals(1, l.findLastIndexWhere(Objects::isNull).size());
    assertEquals(2, l.findLastIndexWhere(Objects::isNull).first());
    assertEquals(List.of(2), l.findLastIndexWhere(Objects::isNull));
    assertFalse(l.findLastIndexWhere(i -> i > 1).isEmpty());
    assertEquals(1, l.findLastIndexWhere(i -> i > 1).size());
    assertEquals(3, l.findLastIndexWhere(i -> i > 1).first());
    assertEquals(List.of(3), l.findLastIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class, () -> l.findLastIndexWhere(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class, () -> l.findLastIndexWhere(i -> i < 3).first());

    assertTrue(List.of().toFuture(context).findLastIndexWhere(Objects::isNull).isEmpty());
    assertEquals(0, List.of().toFuture(context).findLastIndexWhere(Objects::isNull).size());

    if (TEST_ASYNC_CANCEL) {
      var f = List.of(1, 2, 3).toFuture(context).findLastIndexWhere(i -> {
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
}
