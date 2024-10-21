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
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutorContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.internal.future.iterator.AppendAllIteratorFutureMaterializer;
import sparx.internal.future.iterator.AppendIteratorFutureMaterializer;
import sparx.internal.future.iterator.CountIteratorFutureMaterializer;
import sparx.internal.future.iterator.CountWhereIteratorFutureMaterializer;
import sparx.internal.future.iterator.DiffIteratorFutureMaterializer;
import sparx.internal.future.iterator.DistinctByIteratorFutureMaterializer;
import sparx.internal.future.iterator.DropIteratorFutureMaterializer;
import sparx.internal.future.iterator.DropRightIteratorFutureMaterializer;
import sparx.internal.future.iterator.DropRightWhileIteratorFutureMaterializer;
import sparx.internal.future.iterator.DropWhileIteratorFutureMaterializer;
import sparx.internal.future.iterator.EachIteratorFutureMaterializer;
import sparx.internal.future.iterator.ElementToIteratorFutureMaterializer;
import sparx.internal.future.iterator.EndsWithIteratorFutureMaterializer;
import sparx.internal.future.iterator.ExistsIteratorFutureMaterializer;
import sparx.internal.future.iterator.FilterIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindFirstIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindIndexIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindIndexOfSliceIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindLastIndexIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindLastIndexOfSliceIteratorFutureMaterializer;
import sparx.internal.future.iterator.FindLastIteratorFutureMaterializer;
import sparx.internal.future.iterator.FlatMapAfterIteratorFutureMaterializer;
import sparx.internal.future.iterator.FlatMapFirstWhereIteratorFutureMaterializer;
import sparx.internal.future.iterator.FlatMapIteratorFutureMaterializer;
import sparx.internal.future.iterator.FlatMapLastWhereIteratorFutureMaterializer;
import sparx.internal.future.iterator.FoldLeftIteratorFutureMaterializer;
import sparx.internal.future.iterator.FoldLeftWhileIteratorFutureMaterializer;
import sparx.internal.future.iterator.FoldRightIteratorFutureMaterializer;
import sparx.internal.future.iterator.FoldRightWhileIteratorFutureMaterializer;
import sparx.internal.future.iterator.IncludesAllIteratorFutureMaterializer;
import sparx.internal.future.iterator.IncludesSliceIteratorFutureMaterializer;
import sparx.internal.future.iterator.InsertAfterIteratorFutureMaterializer;
import sparx.internal.future.iterator.InsertAllAfterIteratorFutureMaterializer;
import sparx.internal.future.iterator.InsertAllIteratorFutureMaterializer;
import sparx.internal.future.iterator.InsertIteratorFutureMaterializer;
import sparx.internal.future.iterator.IntersectIteratorFutureMaterializer;
import sparx.internal.future.iterator.IteratorFutureMaterializer;
import sparx.internal.future.iterator.ListToIteratorFutureMaterializer;
import sparx.internal.future.iterator.MapAfterIteratorFutureMaterializer;
import sparx.internal.future.iterator.MapFirstWhereIteratorFutureMaterializer;
import sparx.internal.future.iterator.MapIteratorFutureMaterializer;
import sparx.internal.future.list.ListToListFutureMaterializer;
import sparx.lazy.Iterator;
import sparx.lazy.List;
import sparx.util.DequeueList;
import sparx.util.UncheckedException;
import sparx.util.UncheckedException.UncheckedInterruptedException;
import sparx.util.function.Action;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
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

    testMaterializer(List.of(1, 2, 3),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2), c),
        (c, m) -> new AppendIteratorFutureMaterializer<>(m, 3, c, new AtomicReference<>(),
            (l, e) -> List.wrap(l).append(e)));

    testCancel(it -> it.append(null));
  }

  @Test
  public void appendAll() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of().toFuture(context).appendAll(null));
    test(List.of(1, 2, 3), Iterator::<Integer>of, it -> it.appendAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), Iterator::<Integer>of, it -> it.appendAll(List.of(1, null, 3)));
    test(List.of(1, null, 3), Iterator::<Integer>of, it -> it.appendAll(Iterator.of(1, null, 3)));
    test(List.of(1, 2, 3), () -> Iterator.of(1),
        it -> it.appendAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(1, null, 3), () -> Iterator.of(1), it -> it.appendAll(List.of(null, 3)));
    test(List.of(1, null, 3), () -> Iterator.of(1), it -> it.appendAll(Iterator.of(null, 3)));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2), it -> it.appendAll(Set.of(3)));
    test(List.of(1, null, 3), () -> Iterator.of(1, null), it -> it.appendAll(Set.of(3)));
    test(List.of(1, null, 3), () -> Iterator.of(1, null), it -> it.appendAll(Iterator.of(3)));

    testMaterializer(List.of(1, 2, 3, 4),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2), c),
        (c, m) -> new AppendAllIteratorFutureMaterializer<>(m,
            new ListToIteratorFutureMaterializer<>(List.of(3, 4), c), c, new AtomicReference<>(),
            (l, e) -> List.wrap(l).appendAll(e)));

    testCancel(it -> it.appendAll(List.of(null)));
  }

  @Test
  public void count() throws Exception {
    test(List.of(0), Iterator::of, future.Iterator::count);
    test(List.of(3), () -> Iterator.of(1, 2, 3), future.Iterator::count);
    test(List.of(3), () -> Iterator.of(1, null, 3), future.Iterator::count);

    testMaterializer(List.of(3), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new CountIteratorFutureMaterializer<>(m, c, new AtomicReference<>()));

    testCancel(future.Iterator::count);
  }

  @Test
  public void countWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .countWhere((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).countWhere((Predicate<? super Integer>) null));
    test(List.of(0), Iterator::of, it -> it.countWhere(Objects::nonNull));
    test(List.of(2), () -> Iterator.of(1, 2, 3), it -> it.countWhere(i -> i < 3));
    test(List.of(3), () -> Iterator.of(1, 2, 3), it -> it.countWhere(i -> i > 0));

    var itr = Iterator.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e))
        .countWhere(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);

    testMaterializer(List.of(2), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new CountWhereIteratorFutureMaterializer<>(m, (n, i) -> i < 3, c,
            new AtomicReference<>()));

    testCancel(it -> it.countWhere(e -> true));
  }

  @Test
  public void diff() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context).diff(null));
    test(List.of(2, 4), () -> Iterator.of(1, 2, null, 4), it -> it.diff(List.of(1, null)));
    test(List.of(2, null), () -> Iterator.of(1, 2, null, 4), it -> it.diff(Iterator.of(1, 4)));
    test(List.of(2, null), () -> Iterator.of(1, 2, null, 4), it -> it.diff(List.of(1, 3, 4)));
    test(List.of(2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.diff(Iterator.of(3, 1, 3)));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null, 4), it -> it.diff(List.of(null, null)));
    test(List.of(), () -> Iterator.of(1, null), it -> it.diff(Iterator.of(1, 2, null, 4)));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4), it -> it.diff(Iterator.of()));
    test(List.of(), Iterator::of, it -> it.diff(Iterator.of(1, 2, null, 4)));

    testMaterializer(List.of(1), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 1, 3), c),
        (c, m) -> new DiffIteratorFutureMaterializer<>(m,
            new ListToIteratorFutureMaterializer<>(List.of(1, 3), c), c, new AtomicReference<>()));

    testCancel(it -> it.diff(List.of(null)));
  }

  @Test
  public void distinctBy() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0, 0).toFuture(context)
        .distinctBy((Function<? super Integer, Object>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0, 0).toFuture(context)
        .distinctBy((IndexedFunction<? super Integer, Object>) null));
    test(List.of(1, null, 2), () -> Iterator.of(1, 1, null, 2, null, 1), future.Iterator::distinct);
    test(List.of(1, 2), () -> Iterator.of(1, 1, null, 2, null, 1),
        it -> it.distinctBy(e -> e == null ? 1 : e));
    test(List.of(1, null), () -> Iterator.of(1, 1, null, 2, null, 1),
        it -> it.distinctBy(e -> e == null ? 2 : e));

    testMaterializer(List.of(1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new DistinctByIteratorFutureMaterializer<>(m, (i, e) -> e / 2, c,
            new AtomicReference<>()));

    testCancel(future.Iterator::distinct);
  }

  @Test
  public void doFor() {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).toFuture(context).doFor((Consumer<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).toFuture(context).doFor((IndexedConsumer<? super Integer>) null));
    var list = new ArrayList<>();
    Iterator.of(1, 2, 3).toFuture(context).doFor(e -> list.add(e));
    assertEquals(List.of(1, 2, 3), list);
    list.clear();
    Iterator.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).doFor(e -> list.add(e));
    assertEquals(List.of(1, 2, 3), list);
  }

  @Test
  public void doWhile() {
    assertThrows(NullPointerException.class, () -> Iterator.of(0, 0).toFuture(context)
        .doWhile((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).toFuture(context).doWhile((Predicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).toFuture(context).doWhile(null, (i, e) -> {
        }));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).toFuture(context).doWhile(null, e -> {
        }));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).toFuture(context).doWhile((i, e) -> true, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).toFuture(context).doWhile(e -> true, null));
    var list = new ArrayList<>();
    Iterator.of(1, 2, 3).toFuture(context).doWhile(e -> e < 3, list::add);
    assertEquals(List.of(1, 2), list);
    list.clear();
    Iterator.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).doWhile(e -> e < 3, list::add);
    assertEquals(List.of(1, 2), list);
    list.clear();
    Iterator.of(1, 2, 3).toFuture(context).doWhile(e -> {
      list.add(e);
      return e < 2;
    });
    assertEquals(List.of(1, 2), list);
    list.clear();
    Iterator.of(1, 2, 3).toFuture(context).flatMap(e -> List.of(e)).doWhile(e -> {
      list.add(e);
      return e < 2;
    });
    assertEquals(List.of(1, 2), list);
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

    testMaterializer(List.of(2, 3),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new DropIteratorFutureMaterializer<>(m, 1, c, new AtomicReference<>()));

    testCancel(it -> it.drop(1));
  }

  @Test
  public void dropRight() throws Exception {
    test(List.of(), Iterator::<Integer>of, it -> it.dropRight(1));
    test(List.of(), Iterator::<Integer>of, it -> it.dropRight(0));
    test(List.of(), Iterator::<Integer>of, it -> it.dropRight(-1));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3), it -> it.dropRight(-1));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3), it -> it.dropRight(0));
    test(List.of(1, null), () -> Iterator.of(1, null, 3), it -> it.dropRight(1));
    test(List.of(1), () -> Iterator.of(1, null, 3), it -> it.dropRight(2));
    test(List.of(), () -> Iterator.of(1, null, 3), it -> it.dropRight(3));
    test(List.of(), () -> Iterator.of(1, null, 3), it -> it.dropRight(4));

    testMaterializer(List.of(1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new DropRightIteratorFutureMaterializer<>(m, 1, c, new AtomicReference<>()));

    testCancel(it -> it.dropRight(1));
  }

  @Test
  public void dropRightWhile() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .dropRightWhile((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).dropRightWhile((Predicate<? super Integer>) null));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3),
        it -> it.dropRightWhile(Objects::isNull));
    test(List.of(1, null), () -> Iterator.of(1, null, 3),
        it -> it.dropRightWhile(Objects::nonNull));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3), it -> it.dropRightWhile(e -> e < 1));
    test(List.of(), () -> Iterator.of(1, 2, 3), it -> it.dropRightWhile(e -> e > 0));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).toFuture(context).dropRightWhile(e -> e > 0).size());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e))
            .dropRightWhile(e -> e > 0).size());

    testMaterializer(List.of(1), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new DropRightWhileIteratorFutureMaterializer<>(m, (i, e) -> e > 1, c,
            new AtomicReference<>()));

    testCancel(it -> it.dropRightWhile(e -> false));
  }

  @Test
  public void dropWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).dropWhile((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).dropWhile((Predicate<? super Integer>) null));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3), it -> it.dropWhile(Objects::isNull));
    test(List.of(null, 3), () -> Iterator.of(1, null, 3), it -> it.dropWhile(Objects::nonNull));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3), it -> it.dropWhile(e -> e < 1));
    test(List.of(), () -> Iterator.of(1, 2, 3), it -> it.dropWhile(e -> e > 0));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).toFuture(context).dropWhile(e -> e > 0).size());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e))
            .dropWhile(e -> e > 0).size());

    testMaterializer(List.of(3), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new DropWhileIteratorFutureMaterializer<>(m, (i, e) -> e < 3, c,
            new AtomicReference<>(), (l, e) -> lazy.List.wrap(l).prepend(e)));

    testCancel(it -> it.dropWhile(e -> false));
  }

  @Test
  public void each() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).each((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).each((Predicate<? super Integer>) null));
    test(List.of(false), Iterator::of, it -> it.each(Objects::nonNull));
    test(List.of(false), () -> Iterator.of(1, 2, 3), it -> it.each(i -> i < 3));
    test(List.of(true), () -> Iterator.of(1, 2, 3), it -> it.each(i -> i > 0));

    var itr = Iterator.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e)).each(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);

    testMaterializer(List.of(false),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new EachIteratorFutureMaterializer<>(m, (i, e) -> e < 3, false, c,
            new AtomicReference<>()));

    testCancel(it -> it.each(e -> true));
  }

  @Test
  public void endsWith() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context).endsWith(null));
    test(List.of(true), Iterator::<Integer>of, it -> it.endsWith(List.of()));
    test(List.of(false), Iterator::<Integer>of, it -> it.endsWith(List.of(1)));
    test(List.of(true), () -> Iterator.of(1, null, 3), it -> it.endsWith(List.of()));
    test(List.of(true), () -> Iterator.of(1, null, 3), it -> it.endsWith(List.of(3)));
    test(List.of(false), () -> Iterator.of(1, null, 3), it -> it.endsWith(List.of(null)));
    test(List.of(true), () -> Iterator.of(1, null, 3), it -> it.endsWith(List.of(null, 3)));
    test(List.of(false), () -> Iterator.of(1, null, 3), it -> it.endsWith(List.of(1, null)));
    test(List.of(true), () -> Iterator.of(1, null, 3), it -> it.endsWith(List.of(1, null, 3)));
    test(List.of(false), () -> Iterator.of(1, null, 3), it -> it.endsWith(List.of(null, null, 3)));

    testMaterializer(List.of(false),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new EndsWithIteratorFutureMaterializer<>(m,
            new ListToListFutureMaterializer<>(List.of(2, 2), c), c, new AtomicReference<>()));

    testCancel(it -> it.endsWith(List.of(null)));
  }

  @Test
  public void exists() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).exists((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).exists((Predicate<? super Integer>) null));
    test(List.of(false), Iterator::of, it -> it.exists(Objects::nonNull));
    test(List.of(false), () -> Iterator.of(1, 2, 3), it -> it.exists(i -> i > 3));
    test(List.of(true), () -> Iterator.of(1, 2, 3), it -> it.exists(i -> i > 0));

    var itr = Iterator.of(1, null, 3).toFuture(context).flatMap(e -> List.of(e)).exists(i -> i > 1);
    assertThrows(NullPointerException.class, itr::first);

    testMaterializer(List.of(true),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new ExistsIteratorFutureMaterializer<>(m, (i, e) -> e > 2, false, c,
            new AtomicReference<>()));

    testCancel(it -> it.exists(e -> false));
  }

  @Test
  public void filter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).filter((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).filter((Predicate<? super Integer>) null));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null, 4), it -> it.filter(Objects::nonNull));
    test(List.of(1, 2), () -> Iterator.of(1, 2, null, 4),
        it -> it.filter(Objects::nonNull).filter(i -> i < 3));
    test(List.of(4), () -> Iterator.of(1, 2, null, 4),
        it -> it.filter(Objects::nonNull).filter(i -> i > 3));
    test(List.of(), () -> Iterator.of(1, 2, null, 4),
        it -> it.filter(Objects::nonNull).filter(i -> i > 4));
    test(List.of(), Iterator::of, it -> it.filter(Objects::isNull));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, 2, null, 4).toFuture(context).flatMap(e -> List.of(e))
            .filter(i -> i > 4).size());

    testMaterializer(List.of(1, 3),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FilterIteratorFutureMaterializer<>(m, (i, e) -> e % 2 == 1, c,
            new AtomicReference<>()));

    testCancel(it -> it.filter(e -> true));
  }

  @Test
  public void findAny() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).findAny((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).findAny((Predicate<? super Integer>) null));
    test(List.of(null), () -> Iterator.of(1, 2, null, 4), it -> it.findAny(Objects::isNull));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4), it -> it.findAny(i -> i < 4));
    test(List.of(), Iterator::of, it -> it.findAny(Objects::isNull));

    testMaterializer(List.of(3), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FindFirstIteratorFutureMaterializer<>(m, (i, e) -> e > 2, c,
            new AtomicReference<>()));

    testCancel(it -> it.findAny(e -> false));
  }

  @Test
  public void findIndexOf() throws Exception {
    test(List.of(2), () -> Iterator.of(1, 2, null, 4), it -> it.findIndexOf(null));
    test(List.of(3), () -> Iterator.of(1, 2, null, 4), it -> it.findIndexOf(4));
    test(List.of(), () -> Iterator.of(1, 2, null, 4), it -> it.findIndexOf(3));
    test(List.of(), Iterator::of, it -> it.findIndexOf(null));

    testMaterializer(List.of(1), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FindIndexIteratorFutureMaterializer<>(m, (i, e) -> e > 1, c,
            new AtomicReference<>()));

    testCancel(it -> it.findIndexOf(null));
  }

  @Test
  public void findIndexOfSlice() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).findIndexOfSlice(null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e)).findIndexOfSlice(null));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4), it -> it.findIndexOfSlice(List.of(2, null)));
    test(List.of(2), () -> Iterator.of(1, 2, null, 4), it -> it.findIndexOfSlice(List.of(null)));
    test(List.of(), () -> Iterator.of(1, 2, null, 4), it -> it.findIndexOfSlice(List.of(null, 2)));
    test(List.of(0), () -> Iterator.of(1, 2, null, 4), it -> it.findIndexOfSlice(List.of()));
    test(List.of(2), () -> Iterator.of(1, 1, 1, 1, 2, 1),
        it -> it.findIndexOfSlice(List.of(1, 1, 2)));
    test(List.of(), Iterator::of, it -> it.findIndexOfSlice(List.of(null)));
    test(List.of(0), Iterator::of, it -> it.findIndexOfSlice(List.of()));

    testMaterializer(List.of(1), c -> new ListToIteratorFutureMaterializer<>(List.of(2, 2, 3), c),
        (c, m) -> new FindIndexOfSliceIteratorFutureMaterializer<>(m,
            new ListToListFutureMaterializer<>(List.of(2, 3), c), c, new AtomicReference<>()));

    testCancel(it -> it.findIndexOfSlice(List.of(null)));
  }

  @Test
  public void findIndexWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .findIndexWhere((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).findIndexWhere((Predicate<? super Integer>) null));
    test(List.of(2), () -> Iterator.of(1, 2, null, 4), it -> it.findIndexWhere(Objects::isNull));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4), it -> it.findIndexWhere(i -> i > 1));
    test(List.of(), Iterator::of, it -> it.findIndexWhere(Objects::isNull));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, 2, null, 4).toFuture(context).flatMap(e -> List.of(e))
            .findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, 2, null, 4).toFuture(context).flatMap(e -> List.of(e))
            .findIndexWhere(i -> i > 3).first());

    testMaterializer(List.of(), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FindIndexIteratorFutureMaterializer<>(m, (i, e) -> e > 3, c,
            new AtomicReference<>()));

    testCancel(it -> it.findIndexWhere(e -> false));
  }

  @Test
  public void findLast() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).findLast((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).findLast((Predicate<? super Integer>) null));
    test(List.of(null), () -> Iterator.of(1, 2, null, 4, 5), it -> it.findLast(Objects::isNull));
    test(List.of(5), () -> Iterator.of(1, 2, null, 4, 5), it -> it.findLast(Objects::nonNull));
    test(List.of(), () -> Iterator.of(1, 2, null, 4, 5),
        it -> it.findLast(i -> i != null && i > 5));
    test(List.of(), Iterator::of, it -> it.findLast(Objects::isNull));

    java.util.function.Supplier<future.Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 5)
        .toFuture(context).flatMap(e -> List.of(e));
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 4).first());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 5).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 5).size());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 5).first());

    testMaterializer(List.of(2), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FindLastIteratorFutureMaterializer<>(m, (i, e) -> e < 3, c,
            new AtomicReference<>()));

    testCancel(it -> it.findLast(e -> true));
  }

  @Test
  public void findLastIndexOf() throws Exception {
    test(List.of(2), () -> Iterator.of(1, 2, null, 4), it -> it.findLastIndexOf(null));
    test(List.of(3), () -> Iterator.of(1, 2, null, 4), it -> it.findLastIndexOf(4));
    test(List.of(), () -> Iterator.of(1, 2, null, 4), it -> it.findLastIndexOf(3));
    test(List.of(), Iterator::of, it -> it.findLastIndexOf(null));
    test(List.of(), Iterator::of, it -> it.findLastIndexOf(4));

    testMaterializer(List.of(2), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FindLastIndexIteratorFutureMaterializer<>(m, (i, e) -> e > 1, c,
            new AtomicReference<>()));

    testCancel(it -> it.findLastIndexOf(null));
  }

  @Test
  public void findLastIndexOfSlice() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).findLastIndexOfSlice(null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e)).findLastIndexOfSlice(null));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4),
        it -> it.findLastIndexOfSlice(Iterator.of(2, null)));
    test(List.of(2), () -> Iterator.of(1, 2, null, 4),
        it -> it.findLastIndexOfSlice(Iterator.of(null)));
    test(List.of(), () -> Iterator.of(1, 2, null, 4),
        it -> it.findLastIndexOfSlice(Iterator.of(null, 2)));
    test(List.of(4), () -> Iterator.of(1, 2, null, 4),
        it -> it.findLastIndexOfSlice(Iterator.of()));
    test(List.of(2), () -> Iterator.of(1, 1, 1, 1, 2, 1),
        it -> it.findLastIndexOfSlice(List.of(1, 1, 2)));
    test(List.of(), Iterator::of, it -> it.findLastIndexOfSlice(List.of(null)));
    test(List.of(0), Iterator::of, it -> it.findLastIndexOfSlice(List.of()));

    testMaterializer(List.of(1), c -> new ListToIteratorFutureMaterializer<>(List.of(2, 2, 3), c),
        (c, m) -> new FindLastIndexOfSliceIteratorFutureMaterializer<>(m,
            new ListToListFutureMaterializer<>(List.of(2, 3), c), c, new AtomicReference<>()));

    testCancel(it -> it.findLastIndexOfSlice(List.of(null)));
  }

  @Test
  public void findLastIndexWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .findLastIndexWhere((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .findLastIndexWhere((Predicate<? super Integer>) null));
    test(List.of(2), () -> Iterator.of(1, 2, null, 4),
        it -> it.findLastIndexWhere(Objects::isNull));
    test(List.of(0), () -> Iterator.of(null), it -> it.findLastIndexWhere(Objects::isNull));
    test(List.of(), Iterator::of, it -> it.findLastIndexWhere(Objects::isNull));

    java.util.function.Supplier<future.Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4)
        .toFuture(context).flatMap(e -> List.of(e));
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i > 1).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().findLastIndexWhere(i -> i > 1).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i > 1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i < 3).first());

    testMaterializer(List.of(1), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FindLastIndexIteratorFutureMaterializer<>(m, (i, e) -> e < 3, c,
            new AtomicReference<>()));

    testCancel(it -> it.findLastIndexWhere(e -> true));
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

    testMaterializer(List.of(0, 1, 1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2), c),
        (c, m) -> new FlatMapIteratorFutureMaterializer<>(m,
            (i, e) -> new ListToIteratorFutureMaterializer<>(List.of(i, e), c), c,
            new AtomicReference<>()));

    testCancel(it -> it.flatMap(e -> List.of(e)));
  }

  @Test
  public void flatMapAfter() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .flatMapAfter(0, (Function<? super Integer, List<Integer>>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .flatMapAfter(0, (IndexedFunction<? super Integer, List<Integer>>) null));
    test(List.of(1, 2), () -> Iterator.of(1, 2), it -> it.flatMapAfter(-1, i -> List.of(i, i)));
    test(List.of(1, 1, 2), () -> Iterator.of(1, 2), it -> it.flatMapAfter(0, i -> List.of(i, i)));
    test(List.of(1, 2, 2), () -> Iterator.of(1, 2), it -> it.flatMapAfter(1, i -> List.of(i, i)));
    test(List.of(1, 2), () -> Iterator.of(1, 2), it -> it.flatMapAfter(2, i -> List.of(i, i)));
    test(List.of(1, 2), () -> Iterator.of(1, 2), it -> it.flatMapAfter(-1, i -> List.of()));
    test(List.of(2), () -> Iterator.of(1, 2), it -> it.flatMapAfter(0, i -> List.of()));
    test(List.of(1), () -> Iterator.of(1, 2), it -> it.flatMapAfter(1, i -> List.of()));
    test(List.of(1, 2), () -> Iterator.of(1, 2), it -> it.flatMapAfter(2, i -> List.of()));
    test(List.of(), Iterator::of, it -> it.flatMapAfter(0, i -> List.of(i, i)));

    testMaterializer(List.of(1, 1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2), c),
        (c, m) -> new FlatMapAfterIteratorFutureMaterializer<>(m, 1,
            (i, e) -> new ListToIteratorFutureMaterializer<>(List.of(i, e), c), c,
            new AtomicReference<>()));

    testCancel(it -> it.flatMapAfter(0, e -> List.of(e)));
  }

  @Test
  public void flatMapFirstWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapFirstWhere(null, (i, e) -> Iterator.of(e)));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapFirstWhere(null, e -> Iterator.of(e)));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapFirstWhere((i, e) -> false, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapFirstWhere(e -> false, null));
//    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4),
//        it -> it.flatMapFirstWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 1, 2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapFirstWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 2, 3, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapFirstWhere(Objects::isNull, i -> List.of(3)));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapFirstWhere(i -> true, i -> List.of()));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapFirstWhere(Objects::isNull, i -> List.of()));
    test(List.of(1, 1, 2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)));
    test(List.of(), Iterator::of, it -> it.flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(), Iterator::of, it -> it.flatMapFirstWhere(i -> true, i -> List.of()));

    java.util.function.Supplier<future.Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4)
        .toFuture(context).flatMapFirstWhere(i -> i > 2, i -> List.of(i, i));
    assertFalse(itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).first());
    assertEquals(2, itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).drop(2).first());

    testMaterializer(List.of(1, 1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2), c),
        (c, m) -> new FlatMapFirstWhereIteratorFutureMaterializer<>(m, (i, e) -> e > 1,
            (i, e) -> new ListToIteratorFutureMaterializer<>(List.of(i, e), c), c,
            new AtomicReference<>(), (l, e) -> lazy.List.wrap(l).prependAll(e)));

    testCancel(it -> it.flatMapFirstWhere(e -> true, List::of));
  }

  @Test
  public void flatMapLastWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapFirstWhere(null, (i, e) -> Iterator.of(e)));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapFirstWhere(null, e -> Iterator.of(e)));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapFirstWhere((i, e) -> false, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMapFirstWhere(e -> false, null));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapLastWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 2, null, 4, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapLastWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 2, 3, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapLastWhere(Objects::isNull, i -> List.of(3)));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapLastWhere(i -> false, i -> List.of()));
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapLastWhere(i -> true, i -> List.of()));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.flatMapLastWhere(Objects::isNull, i -> List.of()));
    test(List.of(), Iterator::of, it -> it.flatMapLastWhere(i -> false, i -> List.of()));
    test(List.of(), Iterator::of, it -> it.flatMapLastWhere(i -> true, i -> List.of()));

    java.util.function.Supplier<future.Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4)
        .toFuture(context);
    assertFalse(itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).isEmpty());
//    assertEquals(5, itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).size());
//    assertEquals(List.of(1, 2, null, 4, 4),
//        itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).toList());
//    assertEquals(2, itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).drop(1).first());
//    assertNull(itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).drop(2).first());
//    assertThrows(NoSuchElementException.class,
//        () -> itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).drop(5).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).size());
    assertEquals(2, itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).drop(5).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).drop(5).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).drop(3).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).drop(2).first());

    testMaterializer(List.of(1, 1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2), c),
        (c, m) -> new FlatMapLastWhereIteratorFutureMaterializer<>(m, (i, e) -> e > 0,
            (i, e) -> new ListToIteratorFutureMaterializer<>(List.of(i, e), c), c,
            new AtomicReference<>(), (l, e) -> lazy.List.wrap(l).prependAll(e)));

    testCancel(it -> it.flatMapLastWhere(e -> true, List::of));
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
  public void foldLeft() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).foldLeft(null, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e)).foldLeft(null, null));
    test(List.of(16), () -> Iterator.of(1, 2, 3, 4, 5), it -> it.foldLeft(1, Integer::sum));
    test(List.of(List.of(1, 2)), () -> Iterator.of(1, 2),
        it -> it.foldLeft(List.of(), List::append));
    test(List.of(1), Iterator::<Integer>of, it -> it.foldLeft(1, Integer::sum));
    test(List.of(List.of()), Iterator::of, it -> it.foldLeft(List.of(), List::append));

    testMaterializer(List.of(6), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FoldLeftIteratorFutureMaterializer<>(m, 0, Integer::sum, c,
            new AtomicReference<>()));

    testCancel(it -> it.foldLeft(null, (a, e) -> e));
  }

  @Test
  public void foldLeftWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).foldLeftWhile(null, e -> true, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).foldLeftWhile(null, null, (a, e) -> a));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e))
            .foldLeftWhile(null, e -> true, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e))
            .foldLeftWhile(null, null, (a, e) -> a));
    test(List.of(7), () -> Iterator.of(1, 2, 3, 4, 5),
        it -> it.foldLeftWhile(1, s -> s < 5, Integer::sum));
    test(List.of(List.of(1)), () -> Iterator.of(1, 2),
        it -> it.foldLeftWhile(List.of(), List::isEmpty, List::append));
    test(List.of(1), Iterator::<Integer>of, it -> it.foldLeftWhile(1, s -> s < 4, Integer::sum));
    test(List.of(List.of()), Iterator::of,
        it -> it.foldLeftWhile(List.of(), List::isEmpty, List::append));

    testMaterializer(List.of(3), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FoldLeftWhileIteratorFutureMaterializer<>(m, 0, s -> s < 2, Integer::sum, c,
            new AtomicReference<>()));

    testCancel(it -> it.foldLeftWhile(null, a -> true, (a, e) -> e));
  }

  @Test
  public void foldRight() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).foldRight(null, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e)).foldRight(null, null));
    test(List.of(16), () -> Iterator.of(1, 2, 3, 4, 5), it -> it.foldRight(1, Integer::sum));
    test(List.of(List.of(2, 1)), () -> Iterator.of(1, 2),
        it -> it.foldRight(List.of(), (i, l) -> l.append(i)));
    test(List.of(1), Iterator::<Integer>of, it -> it.foldRight(1, Integer::sum));
    test(List.of(List.of()), Iterator::of, it -> it.foldRight(List.of(), (i, l) -> l.append(i)));

    testMaterializer(List.of(6), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FoldRightIteratorFutureMaterializer<>(m, 0, Integer::sum, c,
            new AtomicReference<>()));

    testCancel(it -> it.foldRight(null, (e, a) -> e));
  }

  @Test
  public void foldRightWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).foldRightWhile(null, e -> true, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).foldRightWhile(null, null, (a, e) -> a));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e))
            .foldRightWhile(null, e -> true, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e))
            .foldRightWhile(null, null, (a, e) -> a));
    test(List.of(6), () -> Iterator.of(1, 2, 3, 4, 5),
        it -> it.foldRightWhile(1, s -> s < 5, Integer::sum));
    test(List.of(List.of(2)), () -> Iterator.of(1, 2),
        it -> it.foldRightWhile(List.of(), List::isEmpty, (i, l) -> l.append(i)));
    test(List.of(1), Iterator::<Integer>of, it -> it.foldRightWhile(1, s -> s < 4, Integer::sum));
    test(List.of(List.of()), Iterator::of,
        it -> it.foldRightWhile(List.of(), List::isEmpty, (i, l) -> l.append(i)));

    testMaterializer(List.of(5), c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new FoldRightWhileIteratorFutureMaterializer<>(m, 0, s -> s < 4, Integer::sum, c,
            new AtomicReference<>()));

    testCancel(it -> it.foldRightWhile(null, a -> true, (e, a) -> e));
  }

  @Test
  public void includes() throws Exception {
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5), it -> it.includes(null));
    test(List.of(false), () -> Iterator.of(1, 2, 3, null, 5), it -> it.includes(0));
    test(List.of(false), Iterator::of, it -> it.includes(null));
    test(List.of(false), Iterator::of, it -> it.includes(0));

    testCancel(it -> it.includes(null));
  }

  @Test
  public void includesAll() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).includesAll(null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e)).includesAll(null));
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5),
        it -> it.includesAll(List.of(null, 1)));
    test(List.of(false), () -> Iterator.of(1, 2, 3, null, 5), it -> it.includesAll(List.of(0, 1)));
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5), it -> it.includesAll(List.of()));
    test(List.of(false), Iterator::of, it -> it.includesAll(List.of(null, 1)));
    test(List.of(true), Iterator::of, it -> it.includesAll(List.of()));

    testMaterializer(List.of(false),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new IncludesAllIteratorFutureMaterializer<>(m,
            new ListToIteratorFutureMaterializer<>(List.of(2, 3, 4), c), c,
            new AtomicReference<>()));

    testCancel(it -> it.includesAll(List.of(null)));
  }

  @Test
  public void includesSlice() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).includesSlice(null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e)).includesSlice(null));
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5),
        it -> it.includesSlice(List.of(3, null)));
    test(List.of(false), () -> Iterator.of(1, 2, 3, null, 5),
        it -> it.includesSlice(List.of(null, 3)));
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5), it -> it.includesSlice(List.of()));
    test(List.of(false), Iterator::of, it -> it.includesSlice(List.of(null, 1)));
    test(List.of(true), Iterator::of, it -> it.includesSlice(List.of()));

    testMaterializer(List.of(true),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 1, 1, 2, 3), c),
        (c, m) -> new IncludesSliceIteratorFutureMaterializer<>(m,
            new ListToListFutureMaterializer<>(List.of(1, 1, 2), c), c, new AtomicReference<>()));

    testCancel(it -> it.includesSlice(List.of(null)));
  }

  @Test
  public void insert() throws Exception {
    test(List.of(3, 2, 1), Iterator::<Integer>of, it -> it.insert(1).insert(2).insert(3));
    test(List.of(3, null, 1), Iterator::<Integer>of, it -> it.insert(1).insert(null).insert(3));
    test(List.of(3, 2, 1), () -> Iterator.of(1), it -> it.insert(2).insert(3));
    test(List.of(3, null, 1), () -> Iterator.of(1), it -> it.insert(null).insert(3));
    test(List.of(3, 1, 2), () -> Iterator.of(1, 2), it -> it.insert(3));
    test(List.of(3, 1, null), () -> Iterator.of(1, null), it -> it.insert(3));
    test(List.of(2, null), () -> Iterator.of(1, null), it -> it.drop(1).insert(2));

    testMaterializer(List.of(1, 1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2), c),
        (c, m) -> new InsertIteratorFutureMaterializer<>(m, 1, c, new AtomicReference<>(),
            (l, e) -> lazy.List.wrap(l).prepend(e)));

    testCancel(it -> it.insert(null));
  }

  @Test
  public void insertAfter() throws Exception {
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3), it -> it.insertAfter(5, null));
    test(List.of(1, 2, 3, null), () -> Iterator.of(1, 2, 3), it -> it.insertAfter(3, null));
    test(List.of(1, 2, null, 3), () -> Iterator.of(1, 2, 3), it -> it.insertAfter(2, null));
    test(List.of(1, null, 2, 3), () -> Iterator.of(1, 2, 3), it -> it.insertAfter(1, null));
    test(List.of(null, 1, 2, 3), () -> Iterator.of(1, 2, 3), it -> it.insertAfter(0, null));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3), it -> it.insertAfter(-7, null));
    test(List.of(), Iterator::of, it -> it.insertAfter(5, null));
    test(List.of(null), Iterator::of, it -> it.insertAfter(0, null));
    test(List.of(), () -> Iterator.wrap(() -> List.of().iterator()), it -> it.insertAfter(5, null));
    test(List.of(null), () -> Iterator.wrap(() -> List.of().iterator()),
        it -> it.insertAfter(0, null));

    testMaterializer(List.of(1, 2, 3),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 3), c),
        (c, m) -> new InsertAfterIteratorFutureMaterializer<>(m, 1, 2, c, new AtomicReference<>(),
            (l, n, e) -> lazy.List.wrap(l).insertAfter(n, e)));

    testCancel(it -> it.insertAfter(1, null));
  }

  @Test
  public void insertAll() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).insertAll(null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e)).insertAll(null));
    test(List.of(1, 2, 3), Iterator::<Integer>of, it -> it.insertAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), Iterator::<Integer>of, it -> it.insertAll(Arrays.asList(1, null, 3)));
    test(List.of(2, 3, 1), () -> Iterator.of(1),
        it -> it.insertAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(null, 3, 1), () -> Iterator.of(1),
        it -> it.insertAll(new LinkedHashSet<>(List.of(null, 3))));
    test(List.of(3, 1, 2), () -> Iterator.of(1, 2), it -> it.insertAll(Set.of(3)));
    test(List.of(3, 1, null), () -> Iterator.of(1, null), it -> it.insertAll(Set.of(3)));
    test(List.of(2, 3, null), () -> Iterator.of(1, null),
        it -> it.drop(1).insertAll(Iterator.of(2, 3)));

    testMaterializer(List.of(1, 1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2), c),
        (c, m) -> new InsertAllIteratorFutureMaterializer<>(m,
            new ListToIteratorFutureMaterializer<>(List.of(1), c), c, new AtomicReference<>(),
            (l, e) -> lazy.List.wrap(l).prependAll(e)));

    testCancel(it -> it.insertAll(List.of(null)));
  }

  @Test
  public void insertAllAfter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).insertAllAfter(0, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e)).insertAllAfter(0, null));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3),
        it -> it.insertAllAfter(5, List.of(null, 5)));
    test(List.of(1, 2, 3, null, 5), () -> Iterator.of(1, 2, 3),
        it -> it.insertAllAfter(3, List.of(null, 5)));
    test(List.of(1, 2, null, 5, 3), () -> Iterator.of(1, 2, 3),
        it -> it.insertAllAfter(2, List.of(null, 5)));
    test(List.of(1, null, 5, 2, 3), () -> Iterator.of(1, 2, 3),
        it -> it.insertAllAfter(1, List.of(null, 5)));
    test(List.of(null, 5, 1, 2, 3), () -> Iterator.of(1, 2, 3),
        it -> it.insertAllAfter(0, List.of(null, 5)));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3),
        it -> it.insertAllAfter(-7, List.of(null, 5)));
    test(List.of(), Iterator::of, it -> it.insertAllAfter(5, List.of(null, 5)));
    test(List.of(null, 5), Iterator::of, it -> it.insertAllAfter(0, List.of(null, 5)));
    test(List.of(), () -> Iterator.wrap(() -> List.of().iterator()),
        it -> it.insertAllAfter(5, List.of(null, 5)));
    test(List.of(null, 5), () -> Iterator.wrap(() -> List.of().iterator()),
        it -> it.insertAllAfter(0, List.of(null, 5)));

    testMaterializer(List.of(1, 1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2), c),
        (c, m) -> new InsertAllAfterIteratorFutureMaterializer<>(m, 1,
            new ListToIteratorFutureMaterializer<>(List.of(1), c), c, new AtomicReference<>()));

    testCancel(it -> it.insertAllAfter(0, List.of(null)));
  }

  @Test
  public void intersect() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).intersect(null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e)).intersect(null));
    test(List.of(1, null), () -> Iterator.of(1, 2, null, 4), it -> it.intersect(List.of(1, null)));
    test(List.of(1, 4), () -> Iterator.of(1, 2, null, 4), it -> it.intersect(Iterator.of(1, 4)));
    test(List.of(1, 4), () -> Iterator.of(1, 2, null, 4), it -> it.intersect(List.of(1, 3, 4)));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4), it -> it.intersect(Iterator.of(3, 1, 3)));
    test(List.of(null), () -> Iterator.of(1, 2, null, 4), it -> it.intersect(List.of(null, null)));
    test(List.of(1, null), () -> Iterator.of(1, null),
        it -> it.intersect(Iterator.of(1, 2, null, 4)));
    test(List.of(1, 2), () -> Iterator.of(1, 2, null, 4), it -> it.intersect(List.of(2, 1)));
    test(List.of(), () -> Iterator.of(1, null), it -> it.intersect(Iterator.of(2, 4)));
    test(List.of(), () -> Iterator.of(1, 2, null, 4), it -> it.intersect(Iterator.of()));
    test(List.of(), Iterator::of, it -> it.intersect(Iterator.of(1, 2, null, 4)));

    testMaterializer(List.of(1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 1, 2), c),
        (c, m) -> new IntersectIteratorFutureMaterializer<>(m,
            new ListToIteratorFutureMaterializer<>(List.of(1, 2), c), c, new AtomicReference<>()));

    testCancel(it -> it.intersect(List.of(null)));
  }

  @Test
  public void map() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).map((Function<? super Integer, Object>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .map((IndexedFunction<? super Integer, Object>) null));
    test(List.of(2, 3, 4), () -> Iterator.of(1, 2, 3), it -> it.map(x -> x + 1));
    test(List.of(), Iterator::<Integer>of, it -> it.map(x -> x + 1));

    Supplier<future.Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3).toFuture(context)
        .flatMap(e -> List.of(e));
    assertFalse(itr.get().append(null).map(x -> x + 1).isEmpty());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).size());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).drop(2).first());
    assertEquals(2, itr.get().append(null).map(x -> x + 1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).map(x -> x + 1).drop(3).first());

    testMaterializer(List.of(0, 1, 2),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new MapIteratorFutureMaterializer<>(m, (i, e) -> i, c, new AtomicReference<>()));

    testCancel(it -> it.map(e -> e));
  }

  @Test
  public void mapAfter() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .mapAfter(0, (Function<? super Integer, ? extends Integer>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).toFuture(context)
        .mapAfter(0, (IndexedFunction<? super Integer, ? extends Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e))
            .mapAfter(0, (Function<? super Integer, ? extends Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).flatMap(e -> List.of(e))
            .mapAfter(0, (IndexedFunction<? super Integer, ? extends Integer>) null));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3), it -> it.mapAfter(-1, x -> x + 1));
    test(List.of(2, 2, 3), () -> Iterator.of(1, 2, 3), it -> it.mapAfter(0, x -> x + 1));
    test(List.of(1, 3, 3), () -> Iterator.of(1, 2, 3), it -> it.mapAfter(1, x -> x + 1));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, 3), it -> it.mapAfter(2, x -> x + 1));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3), it -> it.mapAfter(3, x -> x + 1));
    test(List.of(1, 2, 3, null), () -> Iterator.of(1, 2, 3).append(null),
        it -> it.mapAfter(-1, x -> x + 1));
    test(List.of(1, 3, 3, null), () -> Iterator.of(1, 2, 3).append(null),
        it -> it.mapAfter(1, x -> x + 1));
    test(List.of(), Iterator::<Integer>of, it -> it.mapAfter(0, x -> x + 1));

    java.util.function.Supplier<future.Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3)
        .toFuture(context).flatMap(e -> List.of(e));
    assertEquals(4, itr.get().append(null).mapAfter(3, x -> x + 1).size());
    assertEquals(2, itr.get().append(null).mapAfter(3, x -> x + 1).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).mapAfter(3, x -> x + 1).drop(3).first());

    testMaterializer(List.of(1, 1, 3),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new MapAfterIteratorFutureMaterializer<>(m, 1, (i, e) -> i, c,
            new AtomicReference<>(), (l, n, e) -> lazy.List.wrap(l).replaceAfter(n, e)));

    testCancel(it -> it.mapAfter(0, e -> e));
  }

  @Test
  public void mapFirstWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).mapFirstWhere(null, (i, e) -> e));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).mapFirstWhere(null, e -> e));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).mapFirstWhere((i, e) -> false, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).toFuture(context).mapFirstWhere(e -> false, null));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.mapFirstWhere(i -> false, i -> i + 1));
    test(List.of(2, 2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.mapFirstWhere(i -> true, i -> i + 1));
    test(List.of(1, 2, 3, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.mapFirstWhere(Objects::isNull, i -> 3));
    test(List.of(2, 2, null, 4), () -> Iterator.of(1, 2, null, 4),
        it -> it.mapFirstWhere(i -> i == 1, i -> i + 1));
    test(List.of(), Iterator::<Integer>of, it -> it.mapFirstWhere(i -> false, i -> i + 1));
    test(List.of(), Iterator::<Integer>of, it -> it.mapFirstWhere(i -> true, i -> i + 1));

    java.util.function.Supplier<future.Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4)
        .toFuture(context).flatMap(e -> List.of(e));
    assertFalse(itr.get().mapFirstWhere(i -> i > 2, i -> 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapFirstWhere(i -> i > 2, i -> 1).size());
    assertEquals(1, itr.get().mapFirstWhere(i -> i > 2, i -> 1).first());
    assertEquals(2, itr.get().mapFirstWhere(i -> i > 2, i -> 1).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapFirstWhere(i -> i > 2, i -> 1).drop(2).first());

    testMaterializer(List.of(1, 1, 3),
        c -> new ListToIteratorFutureMaterializer<>(List.of(1, 2, 3), c),
        (c, m) -> new MapFirstWhereIteratorFutureMaterializer<>(m, (i, e) -> e == 2, (i, e) -> i, c,
            new AtomicReference<>(), (l, n, e) -> lazy.List.wrap(l).replaceAfter(n, e)));

    testCancel(it -> it.mapFirstWhere(e -> false, e -> e));
  }

  private void runInContext(@NotNull final ExecutionContext context, @NotNull final Action action) {
    context.scheduleAfter(new ContextTask(context) {
      @Override
      protected void runWithContext() {
        try {
          action.run();
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
    });
  }

  private <E> void test(@NotNull final java.util.List<E> expected,
      @NotNull final Supplier<? extends future.Iterator<? extends E>> actualSupplier)
      throws Exception {
//    assertEquals(expected.isEmpty(), actualSupplier.get().isEmpty());
//    assertEquals(!expected.isEmpty(), actualSupplier.get().notEmpty());
//    assertEquals(expected.size(), actualSupplier.get().size());
//    assertEquals(expected, actualSupplier.get().toList());
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
    assertEquals(expected, itr.get().toList());
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

  private <E, F> void testMaterializer(@NotNull final java.util.List<F> expected,
      @NotNull final Function<ExecutorContext, ? extends IteratorFutureMaterializer<E>> sourceFactory,
      @NotNull final BinaryFunction<ExecutorContext, IteratorFutureMaterializer<E>, ? extends IteratorFutureMaterializer<F>> materializerFactory)
      throws Exception {
    testMaterializer(expected, c -> materializerFactory.apply(c, sourceFactory.apply(c)));
    testMaterializer(expected, c -> materializerFactory.apply(c,
        new FlatMapIteratorFutureMaterializer<>(sourceFactory.apply(c),
            (i, e) -> new ElementToIteratorFutureMaterializer<>(e), c, new AtomicReference<>())));
  }

  private <E> void testMaterializer(@NotNull final java.util.List<E> expected,
      @NotNull final Function<ExecutorContext, ? extends IteratorFutureMaterializer<E>> factory)
      throws Exception {
    var trampoline = ExecutorContext.trampoline();
    var atError = new AtomicReference<Exception>();
    var atSkipped = new AtomicInteger(-1);
    var atHasNext = new AtomicBoolean();
    var atMaterializer = new AtomicReference<IteratorFutureMaterializer<E>>();
    /* materializeHasNext */
    for (int i = 0; i < expected.size(); i++) {
      atMaterializer.set(factory.apply(trampoline));
      if (i > 0) {
        final int index = i;
        runInContext(trampoline,
            () -> atMaterializer.get().materializeSkip(index, new FutureConsumer<>() {
              @Override
              public void accept(final Integer skipped) {
                atSkipped.set(skipped);
              }

              @Override
              public void error(@NotNull final Exception error) {
                atError.set(error);
              }
            }));
        assertNull(atError.get());
        assertEquals(i, atSkipped.get());
        atSkipped.set(-1);
      }
      runInContext(trampoline,
          () -> atMaterializer.get().materializeHasNext(new FutureConsumer<>() {
            @Override
            public void accept(final Boolean hasNext) {
              atHasNext.set(hasNext);
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }
          }));
      assertNull(atError.get());
      assertTrue(atHasNext.get());
      atHasNext.set(false);
    }
    atMaterializer.set(factory.apply(trampoline));
    if (!expected.isEmpty()) {
      runInContext(trampoline,
          () -> atMaterializer.get().materializeSkip(expected.size(), new FutureConsumer<>() {
            @Override
            public void accept(final Integer skipped) {
              atSkipped.set(skipped);
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }
          }));
      assertNull(atError.get());
      assertEquals(expected.size(), atSkipped.get());
      atSkipped.set(-1);
    }
    atHasNext.set(true);
    runInContext(trampoline, () -> atMaterializer.get().materializeHasNext(new FutureConsumer<>() {
      @Override
      public void accept(final Boolean hasNext) {
        atHasNext.set(hasNext);
      }

      @Override
      public void error(@NotNull final Exception error) {
        atError.set(error);
      }
    }));
    assertNull(atError.get());
    assertFalse(atHasNext.get());

    var atCalled = new AtomicBoolean();
    var atSize = new AtomicInteger(-1);
    var atIndex = new AtomicInteger(-1);
    /* materializeNext */
    var atElement = new AtomicReference<E>();
    atMaterializer.set(factory.apply(trampoline));
    for (int i = 0; i < expected.size(); i++) {
      runInContext(trampoline,
          () -> atMaterializer.get().materializeNext(new IndexedFutureConsumer<>() {
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
          }));
      assertNull(atError.get());
      assertFalse(atCalled.get());
      assertEquals(i, atIndex.get());
      if (atSize.get() >= 0) {
        assertEquals(expected.size(), atSize.get() + atIndex.get());
      }
      assertEquals(expected.get(i), atElement.get());
      atSize.set(-1);
      atIndex.set(-1);
    }
    runInContext(trampoline,
        () -> atMaterializer.get().materializeNext(new IndexedFutureConsumer<>() {
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
        }));
    assertNull(atError.get());
    assertTrue(atCalled.get());
    assertEquals(0, atSize.get());
    assertEquals(-1, atIndex.get());
    atSize.set(-1);
    atCalled.set(false);

    /* materializeNextWhile (stop) */
    for (int i = 0; i < expected.size(); i++) {
      atMaterializer.set(factory.apply(trampoline));
      if (i > 0) {
        final int index = i;
        runInContext(trampoline,
            () -> atMaterializer.get().materializeSkip(index, new FutureConsumer<>() {
              @Override
              public void accept(final Integer skipped) {
                atSkipped.set(skipped);
              }

              @Override
              public void error(@NotNull final Exception error) {
                atError.set(error);
              }
            }));
        assertNull(atError.get());
        assertEquals(i, atSkipped.get());
        atSkipped.set(-1);
      }
      runInContext(trampoline,
          () -> atMaterializer.get().materializeNextWhile(new IndexedFuturePredicate<>() {
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
          }));
      assertNull(atError.get());
      assertFalse(atCalled.get());
      assertEquals(i, atIndex.get());
      if (atSize.get() >= 0) {
        assertEquals(expected.size(), atSize.get() + atIndex.get());
      }
      assertEquals(expected.get(i), atElement.get());
      atSize.set(-1);
      atIndex.set(-1);
    }
    atMaterializer.set(factory.apply(trampoline));
    if (!expected.isEmpty()) {
      runInContext(trampoline,
          () -> atMaterializer.get().materializeSkip(expected.size(), new FutureConsumer<>() {
            @Override
            public void accept(final Integer skipped) {
              atSkipped.set(skipped);
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }
          }));
      assertNull(atError.get());
      assertEquals(expected.size(), atSkipped.get());
      atSkipped.set(-1);
    }
    runInContext(trampoline,
        () -> atMaterializer.get().materializeNextWhile(new IndexedFuturePredicate<>() {
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
        }));
    assertNull(atError.get());
    assertTrue(atCalled.get());
    assertEquals(-1, atIndex.get());
    assertEquals(0, atSize.get());
    atSize.set(-1);
    atCalled.set(false);

    var elementList = new ArrayList<E>();
    var indexList = new ArrayList<Integer>();
    var sizeList = new ArrayList<Integer>();
    /* materializeNextWhile (continue) */
    for (int i = 0; i < expected.size(); i++) {
      atMaterializer.set(factory.apply(trampoline));
      if (i > 0) {
        final int index = i;
        runInContext(trampoline,
            () -> atMaterializer.get().materializeSkip(index, new FutureConsumer<>() {
              @Override
              public void accept(final Integer skipped) {
                atSkipped.set(skipped);
              }

              @Override
              public void error(@NotNull final Exception error) {
                atError.set(error);
              }
            }));
        assertNull(atError.get());
        assertEquals(i, atSkipped.get());
        atSkipped.set(-1);
      }
      runInContext(trampoline,
          () -> atMaterializer.get().materializeNextWhile(new IndexedFuturePredicate<>() {
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
              sizeList.add(size);
              indexList.add(index);
              elementList.add(element);
              return true;
            }
          }));
      for (int j = i; j < expected.size(); j++) {
        assertEquals(j, indexList.get(j - i));
        if (sizeList.get(j - i) >= 0) {
          assertEquals(expected.size(), sizeList.get(j - i) + indexList.get(j - i));
        }
        assertEquals(expected.get(j), elementList.get(j - i));
      }
      assertNull(atError.get());
      assertTrue(atCalled.get());
      atCalled.set(false);
      sizeList.clear();
      indexList.clear();
      elementList.clear();
    }
    atMaterializer.set(factory.apply(trampoline));
    if (!expected.isEmpty()) {
      runInContext(trampoline,
          () -> atMaterializer.get().materializeSkip(expected.size(), new FutureConsumer<>() {
            @Override
            public void accept(final Integer skipped) {
              atSkipped.set(skipped);
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }
          }));
      assertNull(atError.get());
      assertEquals(expected.size(), atSkipped.get());
      atSkipped.set(-1);
    }
    runInContext(trampoline,
        () -> atMaterializer.get().materializeNextWhile(new IndexedFuturePredicate<>() {
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
            sizeList.add(size);
            indexList.add(index);
            elementList.add(element);
            return true;
          }
        }));
    assertTrue(sizeList.isEmpty());
    assertTrue(indexList.isEmpty());
    assertTrue(elementList.isEmpty());
    assertNull(atError.get());
    assertTrue(atCalled.get());
    assertEquals(0, atSize.get());
    atSize.set(-1);
    atCalled.set(false);

    /* materializeSkip */
    runInContext(trampoline,
        () -> factory.apply(trampoline).materializeSkip(Integer.MAX_VALUE, new FutureConsumer<>() {
          @Override
          public void accept(final Integer skipped) {
            atSkipped.set(skipped);
          }

          @Override
          public void error(@NotNull final Exception error) {
            atError.set(error);
          }
        }));
    assertNull(atError.get());
    assertEquals(expected.size(), atSkipped.get());

    /* materializeElements */
    for (int i = 0; i < expected.size(); i++) {
      atMaterializer.set(factory.apply(trampoline));
      if (i > 0) {
        final int index = i;
        runInContext(trampoline,
            () -> atMaterializer.get().materializeSkip(index, new FutureConsumer<>() {
              @Override
              public void accept(final Integer skipped) {
                atSkipped.set(skipped);
              }

              @Override
              public void error(@NotNull final Exception error) {
                atError.set(error);
              }
            }));
        assertNull(atError.get());
        assertEquals(i, atSkipped.get());
        atSkipped.set(-1);
      }
      runInContext(trampoline,
          () -> atMaterializer.get().materializeElements(new FutureConsumer<>() {
            @Override
            public void accept(final java.util.List<E> elements) {
              elementList.addAll(elements);
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }
          }));
      for (int j = i; j < expected.size(); j++) {
        assertEquals(expected.get(j), elementList.get(j - i));
      }
      assertNull(atError.get());
      elementList.clear();
    }
    atMaterializer.set(factory.apply(trampoline));
    if (!expected.isEmpty()) {
      runInContext(trampoline,
          () -> atMaterializer.get().materializeSkip(expected.size(), new FutureConsumer<>() {
            @Override
            public void accept(final Integer skipped) {
              atSkipped.set(skipped);
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }
          }));
      assertNull(atError.get());
      assertEquals(expected.size(), atSkipped.get());
      atSkipped.set(-1);
    }
    runInContext(trampoline, () -> atMaterializer.get().materializeElements(new FutureConsumer<>() {
      @Override
      public void accept(final java.util.List<E> elements) {
        elementList.addAll(elements);
      }

      @Override
      public void error(@NotNull final Exception error) {
        atError.set(error);
      }
    }));
    assertTrue(elementList.isEmpty());
    assertNull(atError.get());

    /* materializeIterator */
    for (int i = 0; i < expected.size(); i++) {
      atMaterializer.set(factory.apply(trampoline));
      if (i > 0) {
        final int index = i;
        runInContext(trampoline,
            () -> atMaterializer.get().materializeSkip(index, new FutureConsumer<>() {
              @Override
              public void accept(final Integer skipped) {
                atSkipped.set(skipped);
              }

              @Override
              public void error(@NotNull final Exception error) {
                atError.set(error);
              }
            }));
        assertNull(atError.get());
        assertEquals(i, atSkipped.get());
        atSkipped.set(-1);
      }
      runInContext(trampoline,
          () -> atMaterializer.get().materializeIterator(new FutureConsumer<>() {
            @Override
            public void accept(final java.util.Iterator<E> elements) {
              elementList.addAll(lazy.Iterator.ofIterator(elements).toList());
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }
          }));
      for (int j = i; j < expected.size(); j++) {
        assertEquals(expected.get(j), elementList.get(j - i));
      }
      assertNull(atError.get());
      elementList.clear();
      atHasNext.set(true);
      runInContext(trampoline,
          () -> atMaterializer.get().materializeHasNext(new FutureConsumer<>() {
            @Override
            public void accept(final Boolean hasNext) {
              atHasNext.set(hasNext);
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }
          }));
      assertNull(atError.get());
      assertFalse(atHasNext.get());
    }
    atMaterializer.set(factory.apply(trampoline));
    if (!expected.isEmpty()) {
      runInContext(trampoline,
          () -> atMaterializer.get().materializeSkip(expected.size(), new FutureConsumer<>() {
            @Override
            public void accept(final Integer skipped) {
              atSkipped.set(skipped);
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }
          }));
      assertNull(atError.get());
      assertEquals(expected.size(), atSkipped.get());
      atSkipped.set(-1);
    }
    runInContext(trampoline, () -> atMaterializer.get().materializeIterator(new FutureConsumer<>() {
      @Override
      public void accept(final java.util.Iterator<E> elements) {
        elementList.addAll(lazy.Iterator.ofIterator(elements).toList());
      }

      @Override
      public void error(@NotNull final Exception error) {
        atError.set(error);
      }
    }));
    assertTrue(elementList.isEmpty());
    assertNull(atError.get());
    atHasNext.set(true);
    runInContext(trampoline, () -> atMaterializer.get().materializeHasNext(new FutureConsumer<>() {
      @Override
      public void accept(final Boolean hasNext) {
        atHasNext.set(hasNext);
      }

      @Override
      public void error(@NotNull final Exception error) {
        atError.set(error);
      }
    }));
    assertNull(atError.get());
    assertFalse(atHasNext.get());

    /* materializeNextWhile (concurrent) */
    var testExecutor = new TestExecutor();
    var testContext = ExecutorContext.of(testExecutor, 1);
    var elementMap = new TreeMap<Integer, E>();
    var atCompleted = new AtomicInteger();
    atMaterializer.set(factory.apply(testContext));
    for (int i = 0; i < 3; i++) {
      runInContext(testContext,
          () -> atMaterializer.get().materializeNextWhile(new IndexedFuturePredicate<>() {
            @Override
            public void complete(final int size) {
              atCompleted.incrementAndGet();
            }

            @Override
            public void error(@NotNull final Exception error) {
              atError.set(error);
            }

            @Override
            public boolean test(final int size, final int index, final E element) {
              elementMap.put(index, element);
              return true;
            }
          }));
    }
    testExecutor.run(Integer.MAX_VALUE);
    assertEquals(IntStream.range(0, expected.size()).boxed().collect(Collectors.toList()),
        List.wrap(elementMap.keySet()));
    assertEquals(expected, List.wrap(elementMap.values()));
    assertEquals(3, atCompleted.get());
    assertNull(atError.get());
  }

  private static class TestExecutor implements Executor {

    private final DequeueList<Runnable> commands = new DequeueList<>();

    @SuppressWarnings("UnusedReturnValue")
    public int run(final int maxCommands) {
      int count = 0;
      while (count < maxCommands && !commands.isEmpty()) {
        commands.removeFirst().run();
        ++count;
      }
      return count;
    }

    @Override
    public void execute(@NotNull final Runnable command) {
      commands.add(command);
    }
  }
}
