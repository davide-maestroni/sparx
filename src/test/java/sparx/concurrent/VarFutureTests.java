package sparx.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sparx.config.SparxConfig;

public class VarFutureTests {

  @BeforeAll
  public static void init() {
    SparxConfig.initFromConfigFile();
  }

  @AfterAll
  public static void cleanup() {
    SparxConfig.reset();
  }

  @Test
  public void testSet() {
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
  public void testSetBulk() {
    try (var future = VarFuture.<String>create()) {
      assertThrows(NoSuchElementException.class, future::getCurrent);
      assertThrows(TimeoutException.class, () -> future.get(100, TimeUnit.MILLISECONDS));
      assertThrows(UncheckedTimeoutException.class,
          () -> future.iterator(100, TimeUnit.MILLISECONDS).hasNext());
      assertThrows(UncheckedTimeoutException.class,
          () -> future.iterator().hasNext(100, TimeUnit.MILLISECONDS));
      assertEquals("none", future.getCurrentOr("none"));

      future.setBulk(Arrays.asList("hello", "test"));
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
  public void testClear() {
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
  public void testFail() {
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
  public void testSetBulkIterator() {
    var future = VarFuture.<String>create();
    var timeoutIterator = future.iterator(100, TimeUnit.MILLISECONDS);
    var indefiniteIterator = future.iterator();
    future.setBulk(Arrays.asList("hello", "test"));

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
}
