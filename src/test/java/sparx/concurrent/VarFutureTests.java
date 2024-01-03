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
package sparx.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sparx.concurrent.VarFuture.HistoryStrategy;
import sparx.config.AlertModule;
import sparx.config.LogModule;
import sparx.config.SparxConfig;

public class VarFutureTests {

  @BeforeAll
  public static void init() {
    LogModule.addModule();
    AlertModule.addModule();
    SparxConfig.initFromConfigFile();
  }

  @AfterAll
  public static void cleanup() {
    SparxConfig.reset();
  }

  @Test
  @SuppressWarnings({"DataFlowIssue", "resource"})
  public void futureCreate() {
    assertThrows(NullPointerException.class, () -> VarFuture.create(null));
    var future = VarFuture.<String>create();
    assertFalse(future.isReadOnly());
    assertFalse(future.isCancelled());
    assertFalse(future.isDone());
    future.close();
    assertFalse(future.isReadOnly());
    assertFalse(future.isCancelled());
    assertTrue(future.isDone());
    future = VarFuture.create(new HistoryStrategy<>() {
      @Override
      public void onClear() {
      }

      @Override
      public void onClose() {
      }

      @Override
      public void onSet(final String value) {
      }

      @Override
      public void onSetBulk(@NotNull final List<String> values) {
      }

      @Override
      public @NotNull List<String> onSubscribe() {
        return List.of();
      }
    });
    assertFalse(future.isReadOnly());
    assertFalse(future.isCancelled());
    assertFalse(future.isDone());
    future.close();
    assertFalse(future.isReadOnly());
    assertFalse(future.isCancelled());
    assertTrue(future.isDone());
  }

  @Test
  public void futureClear() {
    try (var future = VarFuture.<String>create()) {
      future.clear();
      future.set("hello");

      future.clear();
      assertThrows(NoSuchElementException.class, future::getCurrent);
      assertThrows(TimeoutException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertThrows(UncheckedTimeoutException.class,
          () -> future.iterator(100, TimeUnit.MILLISECONDS).hasNext());
      assertThrows(UncheckedTimeoutException.class,
          () -> future.iterator().hasNext(100, TimeUnit.MILLISECONDS));
      assertEquals("none", future.getCurrentOr("none"));

      future.set("test");
      assertThrows(TimeoutException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertEquals("test", future.getCurrent());
      assertEquals("test", future.getCurrentOr("none"));

      var timeoutIterator = future.iterator(100, TimeUnit.MILLISECONDS);
      assertTrue(timeoutIterator.hasNext());
      assertEquals("test", timeoutIterator.next());
      assertThrows(UncheckedTimeoutException.class, timeoutIterator::hasNext);

      var indefiniteIterator = future.iterator();
      assertTrue(indefiniteIterator.hasNext());
      assertEquals("test", indefiniteIterator.next());
      assertThrows(UncheckedTimeoutException.class,
          () -> indefiniteIterator.hasNext(100, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  public void futureCompute() {
    try (var future = VarFuture.<String>create()) {
      future.set("hello");
      assertThrows(NullPointerException.class, () -> future.compute(null));
      future.compute(s -> s + " test");
      assertEquals("hello test", future.getCurrent());
      assertFalse(future.isCancelled());
      assertFalse(future.isDone());
      future.compute(s -> String.valueOf(Integer.parseInt(s)));
      assertFalse(future.isCancelled());
      assertTrue(future.isDone());
      assertThrows(NoSuchElementException.class, future::getCurrent);
      var ex = assertThrows(ExecutionException.class, future::get);
      assertEquals(NumberFormatException.class, ex.getCause().getClass());
    }
  }

  @Test
  public void futureReadOnly() {
    try (var future = VarFuture.<String>create()) {
      assertFalse(future.isReadOnly());
      var readOnly = future.readOnly();
      assertNotEquals(readOnly, future);
      assertTrue(readOnly.isReadOnly());
      assertEquals(readOnly, future.readOnly());
    }
  }

  @Test
  public void futureSubscribe() {
    try (var future = VarFuture.<String>create()) {
      future.set("1");
      var value1 = new AtomicReference<String>();
      var sub1 = future.subscribe(value1::set, null, null, null);
      assertEquals("1", value1.get());
      future.set("2");
      var value2 = new AtomicReference<String>();
      var sub2 = future.subscribe(value2::set, null, null, null);
      assertEquals("2", value1.get());
      assertEquals("2", value2.get());
      sub1.cancel();
      future.setBulk("3", "4", "5");
      assertEquals("2", value1.get());
      assertEquals("2", value2.get());
      var value3 = new AtomicReference<String>();
      var sub3 = future.subscribe(value3::set, null, null, null);
      assertEquals("2", value1.get());
      assertEquals("2", value2.get());
      assertEquals("5", value3.get());
      future.set("6");
      assertEquals("2", value1.get());
      assertEquals("6", value2.get());
      assertEquals("6", value3.get());
      sub3.cancel();
      future.set("7");
      assertEquals("2", value1.get());
      assertEquals("7", value2.get());
      assertEquals("6", value3.get());
      sub2.cancel();
      future.set("8");
      assertEquals("2", value1.get());
      assertEquals("7", value2.get());
      assertEquals("6", value3.get());
    }
  }

  @Test
  public void futureSet() {
    try (var future = VarFuture.<String>create()) {
      assertThrows(NoSuchElementException.class, future::getCurrent);
      assertThrows(TimeoutException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertThrows(UncheckedTimeoutException.class,
          () -> future.iterator(100, TimeUnit.MILLISECONDS).hasNext());
      assertThrows(UncheckedTimeoutException.class,
          () -> future.iterator().hasNext(100, TimeUnit.MILLISECONDS));
      assertEquals("none", future.getCurrentOr("none"));

      future.set("test");
      assertThrows(TimeoutException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertEquals("test", future.getCurrent());
      assertEquals("test", future.getCurrentOr("none"));

      var timeoutIterator = future.iterator(100, TimeUnit.MILLISECONDS);
      assertTrue(timeoutIterator.hasNext());
      assertEquals("test", timeoutIterator.next());
      assertThrows(UncheckedTimeoutException.class, timeoutIterator::hasNext);

      var indefiniteIterator = future.iterator();
      assertTrue(indefiniteIterator.hasNext());
      assertEquals("test", indefiniteIterator.next());
      assertThrows(UncheckedTimeoutException.class,
          () -> indefiniteIterator.hasNext(100, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  public void futureSetBulk() {
    try (var future = VarFuture.<String>create()) {
      assertThrows(NoSuchElementException.class, future::getCurrent);
      assertThrows(TimeoutException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertThrows(UncheckedTimeoutException.class,
          () -> future.iterator(100, TimeUnit.MILLISECONDS).hasNext());
      assertThrows(UncheckedTimeoutException.class,
          () -> future.iterator().hasNext(100, TimeUnit.MILLISECONDS));
      assertEquals("none", future.getCurrentOr("none"));

      future.setBulk("hello", "test");
      assertThrows(TimeoutException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertEquals("test", future.getCurrent());
      assertEquals("test", future.getCurrentOr("none"));

      var timeoutIterator = future.iterator(100, TimeUnit.MILLISECONDS);
      assertTrue(timeoutIterator.hasNext());
      assertEquals("test", timeoutIterator.next());
      assertThrows(UncheckedTimeoutException.class, timeoutIterator::hasNext);

      var indefiniteIterator = future.iterator();
      assertTrue(indefiniteIterator.hasNext());
      assertEquals("test", indefiniteIterator.next());
      assertThrows(UncheckedTimeoutException.class,
          () -> indefiniteIterator.hasNext(100, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  public void futureFail() {
    try (var future = VarFuture.<String>create()) {
      future.set("hello");
      future.fail(new IllegalAccessException());
      assertThrows(NoSuchElementException.class, future::getCurrent);
      assertThrows(ExecutionException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertThrows(UncheckedException.class,
          () -> future.iterator(100, TimeUnit.MILLISECONDS).hasNext());
      assertThrows(UncheckedException.class,
          () -> future.iterator().hasNext(100, TimeUnit.MILLISECONDS));
      assertEquals("none", future.getCurrentOr("none"));

      future.clear();
      assertThrows(NoSuchElementException.class, future::getCurrent);
      assertThrows(ExecutionException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertThrows(UncheckedException.class,
          () -> future.iterator(100, TimeUnit.MILLISECONDS).hasNext());
      assertThrows(UncheckedException.class,
          () -> future.iterator().hasNext(100, TimeUnit.MILLISECONDS));
      assertEquals("none", future.getCurrentOr("none"));

      future.set("test");
      assertThrows(NoSuchElementException.class, future::getCurrent);
      assertThrows(ExecutionException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertThrows(UncheckedException.class,
          () -> future.iterator(100, TimeUnit.MILLISECONDS).hasNext());
      assertThrows(UncheckedException.class,
          () -> future.iterator().hasNext(100, TimeUnit.MILLISECONDS));
      assertEquals("none", future.getCurrentOr("none"));
    }
  }

  @Test
  public void futureIteratorSetBulk() {
    var future = VarFuture.<String>create();
    var timeoutIterator = future.iterator(100, TimeUnit.MILLISECONDS);
    var indefiniteIterator = future.iterator();
    future.setBulk("hello", "test");

    assertTrue(timeoutIterator.hasNext());
    assertEquals("hello", timeoutIterator.next());
    assertTrue(timeoutIterator.hasNext(100, TimeUnit.MILLISECONDS));
    assertEquals("test", timeoutIterator.next(100, TimeUnit.MILLISECONDS));
    assertThrows(UncheckedTimeoutException.class,
        () -> timeoutIterator.hasNext(10, TimeUnit.MILLISECONDS));

    assertTrue(indefiniteIterator.hasNext());
    assertEquals("hello", indefiniteIterator.next());
    assertTrue(indefiniteIterator.hasNext(100, TimeUnit.MILLISECONDS));
    assertEquals("test", indefiniteIterator.next(100, TimeUnit.MILLISECONDS));
    assertThrows(UncheckedTimeoutException.class,
        () -> indefiniteIterator.hasNext(10, TimeUnit.MILLISECONDS));

    future.close();
    assertFalse(timeoutIterator.hasNext());
    assertFalse(timeoutIterator.hasNext(100, TimeUnit.MILLISECONDS));
    assertThrows(NoSuchElementException.class, timeoutIterator::next);
    assertThrows(NoSuchElementException.class,
        () -> timeoutIterator.next(100, TimeUnit.MILLISECONDS));

    assertFalse(indefiniteIterator.hasNext());
    assertFalse(indefiniteIterator.hasNext(100, TimeUnit.MILLISECONDS));
    assertThrows(NoSuchElementException.class, indefiniteIterator::next);
    assertThrows(NoSuchElementException.class,
        () -> indefiniteIterator.next(100, TimeUnit.MILLISECONDS));
  }

  @Test
  public void liveIteratorIndefinite() {
    var future = VarFuture.<String>create();
    var iterator = future.iterator();
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.next(10, TimeUnit.MILLISECONDS));

    future.set("1");
    assertTrue(iterator.hasNext());
    assertTrue(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertEquals("1", iterator.next());

    future.set("2");
    assertTrue(iterator.hasNext());
    assertTrue(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertEquals("2", iterator.next(10, TimeUnit.MILLISECONDS));

    future.setBulk("3", "4", "5");
    assertTrue(iterator.hasNext());
    assertEquals("3", iterator.next(10, TimeUnit.MILLISECONDS));
    assertTrue(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertEquals("4", iterator.next());
    assertEquals("5", iterator.next(10, TimeUnit.MILLISECONDS));

    future.close();
    assertFalse(iterator.hasNext());
    assertFalse(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertThrows(NoSuchElementException.class, iterator::next);
    assertThrows(NoSuchElementException.class,
        () -> iterator.next(10, TimeUnit.MILLISECONDS));
  }

  @Test
  public void liveIterator() {
    var future = VarFuture.<String>create();
    var iterator = future.iterator(1, TimeUnit.MINUTES);
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.next(10, TimeUnit.MILLISECONDS));

    future.set("1");
    assertTrue(iterator.hasNext());
    assertTrue(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertEquals("1", iterator.next());

    future.set("2");
    assertTrue(iterator.hasNext());
    assertTrue(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertEquals("2", iterator.next(10, TimeUnit.MILLISECONDS));

    future.setBulk("3", "4", "5");
    assertTrue(iterator.hasNext());
    assertEquals("3", iterator.next(10, TimeUnit.MILLISECONDS));
    assertTrue(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertEquals("4", iterator.next());
    assertEquals("5", iterator.next(10, TimeUnit.MILLISECONDS));

    future.close();
    assertFalse(iterator.hasNext());
    assertFalse(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertThrows(NoSuchElementException.class, iterator::next);
    assertThrows(NoSuchElementException.class,
        () -> iterator.next(10, TimeUnit.MILLISECONDS));
  }

  @Test
  public void liveIteratorTimeout() {
    var future = VarFuture.<String>create();
    var iterator = future.iterator(40, TimeUnit.MILLISECONDS);
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.next(10, TimeUnit.MILLISECONDS));

    future.set("1");
    assertTrue(iterator.hasNext());
    assertTrue(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertEquals("1", iterator.next());

    future.setBulk("2", "3", "4", "5");
    assertEquals("2", iterator.next(10, TimeUnit.MILLISECONDS));
    assertTrue(iterator.hasNext());
    assertEquals("3", iterator.next(10, TimeUnit.MILLISECONDS));
    assertTrue(iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertEquals("4", iterator.next());
    assertEquals("5", iterator.next(10, TimeUnit.MILLISECONDS));

    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.next(10, TimeUnit.MILLISECONDS));

    future.set("6");
    assertThrows(UncheckedTimeoutException.class, iterator::hasNext);
    assertThrows(UncheckedTimeoutException.class, iterator::next);
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.next(10, TimeUnit.MILLISECONDS));

    future.close();
    assertThrows(UncheckedTimeoutException.class, iterator::hasNext);
    assertThrows(UncheckedTimeoutException.class, iterator::next);
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.hasNext(10, TimeUnit.MILLISECONDS));
    assertThrows(UncheckedTimeoutException.class,
        () -> iterator.next(10, TimeUnit.MILLISECONDS));
  }
}
