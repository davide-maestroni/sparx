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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import sparx.lazy.Iterator;
import sparx.lazy.List;
import sparx.util.SizeOverflowException;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;

@SuppressWarnings("DataFlowIssue")
public class LazyIteratorTests {

  @Test
  public void append() throws Exception {
    test(List.of(1, 2, 3), () -> Iterator.<Integer>of().append(1).append(2).append(3));
    test(List.of(1, null, 3), () -> Iterator.<Integer>of().append(1).append(null).append(3));
    test(List.of(1, 2, 3), () -> Iterator.of(1).append(2).append(3));
    test(List.of(1, null, 3), () -> Iterator.of(1).append(null).append(3));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2).append(3));
    test(List.of(1, null, 3), () -> Iterator.of(1, null).append(3));
  }

  @Test
  public void appendAll() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of().appendAll(null));
    test(List.of(1, 2, 3), () -> Iterator.<Integer>of().appendAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), () -> Iterator.<Integer>of().appendAll(List.of(1, null, 3)));
    test(List.of(1, null, 3), () -> Iterator.<Integer>of().appendAll(Iterator.of(1, null, 3)));
    test(List.of(1, 2, 3), () -> Iterator.of(1).appendAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(1, null, 3), () -> Iterator.of(1).appendAll(List.of(null, 3)));
    test(List.of(1, null, 3), () -> Iterator.of(1).appendAll(Iterator.of(null, 3)));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2).appendAll(Set.of(3)));
    test(List.of(1, null, 3), () -> Iterator.of(1, null).appendAll(Set.of(3)));
    test(List.of(1, null, 3), () -> Iterator.of(1, null).appendAll(Iterator.of(3)));
  }

  @Test
  public void count() throws Exception {
    test(List.of(0), () -> Iterator.of().count());
    test(List.of(3), () -> Iterator.of(1, 2, 3).count());
    test(List.of(3), () -> Iterator.of(1, null, 3).count());
  }

  @Test
  public void countWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).countWhere((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).countWhere((Predicate<? super Integer>) null));
    test(List.of(0), () -> Iterator.of().countWhere(Objects::nonNull));
    test(List.of(2), () -> Iterator.of(1, 2, 3).countWhere(i -> i < 3));
    test(List.of(3), () -> Iterator.of(1, 2, 3).countWhere(i -> i > 0));

    var itr = Iterator.of(1, null, 3).countWhere(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void diff() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).diff(null));
    test(List.of(2, 4), () -> Iterator.of(1, 2, null, 4).diff(List.of(1, null)));
    test(List.of(2, null), () -> Iterator.of(1, 2, null, 4).diff(Iterator.of(1, 4)));
    test(List.of(2, null), () -> Iterator.of(1, 2, null, 4).diff(List.of(1, 3, 4)));
    test(List.of(2, null, 4), () -> Iterator.of(1, 2, null, 4).diff(Iterator.of(3, 1, 3)));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null, 4).diff(List.of(null, null)));
    test(List.of(), () -> Iterator.of(1, null).diff(Iterator.of(1, 2, null, 4)));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).diff(Iterator.of()));
    test(List.of(), () -> Iterator.of().diff(Iterator.of(1, 2, null, 4)));
  }

  @Test
  public void distinctBy() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).distinctBy((Function<? super Integer, Object>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).distinctBy((IndexedFunction<? super Integer, Object>) null));
    test(List.of(1, null, 2), () -> Iterator.of(1, 1, null, 2, null, 1).distinct());
    test(List.of(1, 2),
        () -> Iterator.of(1, 1, null, 2, null, 1).distinctBy(e -> e == null ? 1 : e));
    test(List.of(1, null),
        () -> Iterator.of(1, 1, null, 2, null, 1).distinctBy(e -> e == null ? 2 : e));
  }

  @Test
  public void doFor() {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).doFor((Consumer<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).doFor((IndexedConsumer<? super Integer>) null));
    var list = new ArrayList<>();
    Iterator.of(1, 2, 3).doFor(e -> list.add(e));
    assertEquals(List.of(1, 2, 3), list);
  }

  @Test
  public void doWhile() {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).doWhile((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0, 0).doWhile((Predicate<? super Integer>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0, 0).doWhile(null, (i, e) -> {
    }));
    assertThrows(NullPointerException.class, () -> Iterator.of(0, 0).doWhile(null, e -> {
    }));
    assertThrows(NullPointerException.class, () -> Iterator.of(0, 0).doWhile((i, e) -> true, null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0, 0).doWhile(e -> true, null));
    var list = new ArrayList<>();
    Iterator.of(1, 2, 3).doWhile(e -> e < 3, list::add);
    assertEquals(List.of(1, 2), list);
    list.clear();
    Iterator.of(1, 2, 3).doWhile(e -> {
      list.add(e);
      return e < 2;
    });
    assertEquals(List.of(1, 2), list);
  }

  @Test
  public void drop() throws Exception {
    test(List.of(), () -> Iterator.<Integer>of().drop(1));
    test(List.of(), () -> Iterator.<Integer>of().drop(0));
    test(List.of(), () -> Iterator.<Integer>of().drop(-1));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3).drop(-1));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3).drop(0));
    test(List.of(null, 3), () -> Iterator.of(1, null, 3).drop(1));
    test(List.of(3), () -> Iterator.of(1, null, 3).drop(2));
    test(List.of(), () -> Iterator.of(1, null, 3).drop(3));
    test(List.of(), () -> Iterator.of(1, null, 3).drop(4));
  }

  @Test
  public void dropRight() throws Exception {
    test(List.of(), () -> Iterator.<Integer>of().dropRight(1));
    test(List.of(), () -> Iterator.<Integer>of().dropRight(0));
    test(List.of(), () -> Iterator.<Integer>of().dropRight(-1));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3).dropRight(-1));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3).dropRight(0));
    test(List.of(1, null), () -> Iterator.of(1, null, 3).dropRight(1));
    test(List.of(1), () -> Iterator.of(1, null, 3).dropRight(2));
    test(List.of(), () -> Iterator.of(1, null, 3).dropRight(3));
    test(List.of(), () -> Iterator.of(1, null, 3).dropRight(4));
  }

  @Test
  public void dropRightWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).dropRightWhile((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).dropRightWhile((Predicate<? super Integer>) null));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3).dropRightWhile(Objects::isNull));
    test(List.of(1, null), () -> Iterator.of(1, null, 3).dropRightWhile(Objects::nonNull));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3).dropRightWhile(e -> e < 1));
    test(List.of(), () -> Iterator.of(1, 2, 3).dropRightWhile(e -> e > 0));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).dropRightWhile(e -> e > 0).size());
  }

  @Test
  public void dropWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).dropWhile((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).dropWhile((Predicate<? super Integer>) null));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3).dropWhile(Objects::isNull));
    test(List.of(null, 3), () -> Iterator.of(1, null, 3).dropWhile(Objects::nonNull));
    test(List.of(1, null, 3), () -> Iterator.of(1, null, 3).dropWhile(e -> e < 1));
    test(List.of(), () -> Iterator.of(1, 2, 3).dropWhile(e -> e > 0));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).dropWhile(e -> e > 0).size());
  }

  @Test
  public void each() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).each((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).each((Predicate<? super Integer>) null));
    test(List.of(false), () -> Iterator.of().each(Objects::nonNull));
    test(List.of(false), () -> Iterator.of(1, 2, 3).each(i -> i < 3));
    test(List.of(true), () -> Iterator.of(1, 2, 3).each(i -> i > 0));

    var itr = Iterator.of(1, null, 3).each(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void endsWith() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).endsWith(null));
    test(List.of(true), () -> Iterator.<Integer>of().endsWith(List.of()));
    test(List.of(false), () -> Iterator.<Integer>of().endsWith(List.of(1)));
    test(List.of(true), () -> Iterator.of(1, null, 3).endsWith(List.of()));
    test(List.of(true), () -> Iterator.of(1, null, 3).endsWith(List.of(3)));
    test(List.of(false), () -> Iterator.of(1, null, 3).endsWith(List.of(null)));
    test(List.of(true), () -> Iterator.of(1, null, 3).endsWith(List.of(null, 3)));
    test(List.of(false), () -> Iterator.of(1, null, 3).endsWith(List.of(1, null)));
    test(List.of(true), () -> Iterator.of(1, null, 3).endsWith(List.of(1, null, 3)));
    test(List.of(false), () -> Iterator.of(1, null, 3).endsWith(List.of(null, null, 3)));
  }

  @Test
  public void exists() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).exists((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).exists((Predicate<? super Integer>) null));
    test(List.of(false), () -> Iterator.of().exists(Objects::nonNull));
    test(List.of(false), () -> Iterator.of(1, 2, 3).exists(i -> i > 3));
    test(List.of(true), () -> Iterator.of(1, 2, 3).exists(i -> i > 0));

    var itr = Iterator.of(1, null, 3).exists(i -> i > 1);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void filter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).filter((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).filter((Predicate<? super Integer>) null));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null, 4).filter(Objects::nonNull));
    test(List.of(1, 2),
        () -> Iterator.of(1, 2, null, 4).filter(Objects::nonNull).filter(i -> i < 3));
    test(List.of(4), () -> Iterator.of(1, 2, null, 4).filter(Objects::nonNull).filter(i -> i > 3));
    test(List.of(), () -> Iterator.of(1, 2, null, 4).filter(Objects::nonNull).filter(i -> i > 4));
    test(List.of(), () -> Iterator.of().filter(Objects::isNull));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, 2, null, 4).filter(i -> i > 4).size());
  }

  @Test
  public void findAny() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).findAny((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).findAny((Predicate<? super Integer>) null));
    test(List.of(null), () -> Iterator.of(1, 2, null, 4).findAny(Objects::isNull));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4).findAny(i -> i < 4));
    test(List.of(), () -> Iterator.of().findAny(Objects::isNull));
  }

  @Test
  public void findIndexOf() throws Exception {
    test(List.of(2), () -> Iterator.of(1, 2, null, 4).findIndexOf(null));
    test(List.of(3), () -> Iterator.of(1, 2, null, 4).findIndexOf(4));
    test(List.of(), () -> Iterator.of(1, 2, null, 4).findIndexOf(3));
    test(List.of(), () -> Iterator.of().findIndexOf(null));
  }

  @Test
  public void findIndexOfSlice() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).findIndexOfSlice(null));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4).findIndexOfSlice(List.of(2, null)));
    test(List.of(2), () -> Iterator.of(1, 2, null, 4).findIndexOfSlice(List.of(null)));
    test(List.of(), () -> Iterator.of(1, 2, null, 4).findIndexOfSlice(List.of(null, 2)));
    test(List.of(0), () -> Iterator.of(1, 2, null, 4).findIndexOfSlice(List.of()));
    test(List.of(2), () -> Iterator.of(1, 1, 1, 1, 2, 1).findIndexOfSlice(List.of(1, 1, 2)));
    test(List.of(), () -> Iterator.of().findIndexOfSlice(List.of(null)));
    test(List.of(0), () -> Iterator.of().findIndexOfSlice(List.of()));
  }

  @Test
  public void findIndexWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).findIndexWhere((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).findIndexWhere((Predicate<? super Integer>) null));
    test(List.of(2), () -> Iterator.of(1, 2, null, 4).findIndexWhere(Objects::isNull));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4).findIndexWhere(i -> i > 1));
    test(List.of(), () -> Iterator.of().findIndexWhere(Objects::isNull));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, 2, null, 4).findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, 2, null, 4).findIndexWhere(i -> i > 3).first());
  }

  @Test
  public void findLast() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).findLast((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).findLast((Predicate<? super Integer>) null));
    test(List.of(null), () -> Iterator.of(1, 2, null, 4, 5).findLast(Objects::isNull));
    test(List.of(5), () -> Iterator.of(1, 2, null, 4, 5).findLast(Objects::nonNull));
    test(List.of(), () -> Iterator.of(1, 2, null, 4, 5).findLast(i -> i != null && i > 5));
    test(List.of(), () -> Iterator.of().findLast(Objects::isNull));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 5);
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 4).first());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 5).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 5).size());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 5).first());
  }

  @Test
  public void findLastIndexOf() throws Exception {
    test(List.of(2), () -> Iterator.of(1, 2, null, 4).findLastIndexOf(null));
    test(List.of(3), () -> Iterator.of(1, 2, null, 4).findLastIndexOf(4));
    test(List.of(), () -> Iterator.of(1, 2, null, 4).findLastIndexOf(3));
    test(List.of(), () -> Iterator.of().findLastIndexOf(null));
    test(List.of(), () -> Iterator.of().findLastIndexOf(4));
  }

  @Test
  public void findLastIndexOfSlice() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).findLastIndexOfSlice(null));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4).findLastIndexOfSlice(Iterator.of(2, null)));
    test(List.of(2), () -> Iterator.of(1, 2, null, 4).findLastIndexOfSlice(Iterator.of(null)));
    test(List.of(), () -> Iterator.of(1, 2, null, 4).findLastIndexOfSlice(Iterator.of(null, 2)));
    test(List.of(4), () -> Iterator.of(1, 2, null, 4).findLastIndexOfSlice(Iterator.of()));
    test(List.of(2), () -> Iterator.of(1, 1, 1, 1, 2, 1).findLastIndexOfSlice(List.of(1, 1, 2)));
    test(List.of(), () -> Iterator.of().findLastIndexOfSlice(List.of(null)));
    test(List.of(0), () -> Iterator.of().findLastIndexOfSlice(List.of()));
  }

  @Test
  public void findLastIndexWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).findLastIndexWhere((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).findLastIndexWhere((Predicate<? super Integer>) null));
    test(List.of(2), () -> Iterator.of(1, 2, null, 4).findLastIndexWhere(Objects::isNull));
    test(List.of(0), () -> Iterator.of(null).findLastIndexWhere(Objects::isNull));
    test(List.of(), () -> Iterator.of().findLastIndexWhere(Objects::isNull));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i > 1).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().findLastIndexWhere(i -> i > 1).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i > 1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i < 3).first());
  }

  @Test
  public void flatMap() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMap((Function<? super Integer, List<Object>>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMap((IndexedFunction<? super Integer, List<Object>>) null));
    test(List.of(1, 1, 2, 2), () -> Iterator.of(1, 2).flatMap(i -> List.of(i, i)));
    test(List.of(), () -> Iterator.of(1, 2).flatMap(i -> List.of()));
    test(List.of(null, null), () -> Iterator.of(1, 2).flatMap(i -> List.of(null)));
  }

  @Test
  public void flatMapAfter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapAfter(0, (Function<? super Integer, List<Integer>>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0)
        .flatMapAfter(0, (IndexedFunction<? super Integer, List<Integer>>) null));
    test(List.of(1, 2), () -> Iterator.of(1, 2).flatMapAfter(-1, i -> List.of(i, i)));
    test(List.of(1, 1, 2), () -> Iterator.of(1, 2).flatMapAfter(0, i -> List.of(i, i)));
    test(List.of(1, 2, 2), () -> Iterator.of(1, 2).flatMapAfter(1, i -> List.of(i, i)));
    test(List.of(1, 2), () -> Iterator.of(1, 2).flatMapAfter(2, i -> List.of(i, i)));
    test(List.of(1, 2), () -> Iterator.of(1, 2).flatMapAfter(-1, i -> List.of()));
    test(List.of(2), () -> Iterator.of(1, 2).flatMapAfter(0, i -> List.of()));
    test(List.of(1), () -> Iterator.of(1, 2).flatMapAfter(1, i -> List.of()));
    test(List.of(1, 2), () -> Iterator.of(1, 2).flatMapAfter(2, i -> List.of()));
    test(List.of(), () -> Iterator.of().flatMapAfter(0, i -> List.of(i, i)));
  }

  @Test
  public void flatMapFirstWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapFirstWhere(null, (i, e) -> Iterator.of(e)));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapFirstWhere(null, e -> Iterator.of(e)));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapFirstWhere((i, e) -> false, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapFirstWhere(e -> false, null));
    test(List.of(1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapFirstWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapFirstWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 2, 3, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapFirstWhere(Objects::isNull, i -> List.of(3)));
    test(List.of(1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(2, null, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapFirstWhere(i -> true, i -> List.of()));
    test(List.of(1, 2, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapFirstWhere(Objects::isNull, i -> List.of()));
    test(List.of(1, 1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)));
    test(List.of(), () -> Iterator.of().flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(), () -> Iterator.of().flatMapFirstWhere(i -> true, i -> List.of()));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4)
        .flatMapFirstWhere(i -> i > 2, i -> List.of(i, i));
    assertFalse(itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).first());
    assertEquals(2, itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).drop(2).first());
  }

  @Test
  public void flatMapLastWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapFirstWhere(null, (i, e) -> Iterator.of(e)));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapFirstWhere(null, e -> Iterator.of(e)));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapFirstWhere((i, e) -> false, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapFirstWhere(e -> false, null));
    test(List.of(1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapLastWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 2, null, 4, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapLastWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 2, 3, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapLastWhere(Objects::isNull, i -> List.of(3)));
    test(List.of(1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapLastWhere(i -> false, i -> List.of()));
    test(List.of(1, 2, null),
        () -> Iterator.of(1, 2, null, 4).flatMapLastWhere(i -> true, i -> List.of()));
    test(List.of(1, 2, 4),
        () -> Iterator.of(1, 2, null, 4).flatMapLastWhere(Objects::isNull, i -> List.of()));
    test(List.of(), () -> Iterator.of().flatMapLastWhere(i -> false, i -> List.of()));
    test(List.of(), () -> Iterator.of().flatMapLastWhere(i -> true, i -> List.of()));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
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
  }

  @Test
  public void flatMapWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapWhere(null, e -> List.of()));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapWhere(null, (i, e) -> List.of()));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).flatMapWhere(e -> true, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).flatMapWhere((i, e) -> true, null));
    test(List.of(1, null, null, 4),
        () -> Iterator.of(1, null, null, 4).flatMapWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 1, null, null, null, null, 4, 4),
        () -> Iterator.of(1, null, null, 4).flatMapWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 3, 3, 4),
        () -> Iterator.of(1, null, null, 4).flatMapWhere(Objects::isNull, i -> List.of(3)));
    test(List.of(1, null, null, 4),
        () -> Iterator.of(1, null, null, 4).flatMapWhere(i -> false, i -> List.of()));
    test(List.of(1, 4),
        () -> Iterator.of(1, null, null, 4).flatMapWhere(Objects::isNull, i -> List.of()));
    test(List.of(), () -> Iterator.of().flatMapWhere(i -> false, i -> List.of()));
    test(List.of(), () -> Iterator.of().flatMapWhere(i -> true, i -> List.of()));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, null, null, 4);
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
  }

  @Test
  public void foldLeft() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).foldLeft(null, null));
    test(List.of(16), () -> Iterator.of(1, 2, 3, 4, 5).foldLeft(1, Integer::sum));
    test(List.of(List.of(1, 2)), () -> Iterator.of(1, 2).foldLeft(List.of(), List::append));
    test(List.of(1), () -> Iterator.<Integer>of().foldLeft(1, Integer::sum));
    test(List.of(List.of()), () -> Iterator.of().foldLeft(List.of(), List::append));
  }

  @Test
  public void foldLeftWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).foldLeftWhile(null, e -> true, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).foldLeftWhile(null, null, (a, e) -> a));
    test(List.of(7), () -> Iterator.of(1, 2, 3, 4, 5).foldLeftWhile(1, s -> s < 5, Integer::sum));
    test(List.of(List.of(1)),
        () -> Iterator.of(1, 2).foldLeftWhile(List.of(), List::isEmpty, List::append));
    test(List.of(1), () -> Iterator.<Integer>of().foldLeftWhile(1, s -> s < 4, Integer::sum));
    test(List.of(List.of()),
        () -> Iterator.of().foldLeftWhile(List.of(), List::isEmpty, List::append));
  }

  @Test
  public void foldRight() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).foldRight(null, null));
    test(List.of(16), () -> Iterator.of(1, 2, 3, 4, 5).foldRight(1, Integer::sum));
    test(List.of(List.of(2, 1)),
        () -> Iterator.of(1, 2).foldRight(List.of(), (i, l) -> l.append(i)));
    test(List.of(1), () -> Iterator.<Integer>of().foldRight(1, Integer::sum));
    test(List.of(List.of()), () -> Iterator.of().foldRight(List.of(), (i, l) -> l.append(i)));
  }

  @Test
  public void foldRightWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).foldRightWhile(null, e -> true, null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).foldRightWhile(null, null, (a, e) -> a));
    test(List.of(6), () -> Iterator.of(1, 2, 3, 4, 5).foldRightWhile(1, s -> s < 5, Integer::sum));
    test(List.of(List.of(2)),
        () -> Iterator.of(1, 2).foldRightWhile(List.of(), List::isEmpty, (i, l) -> l.append(i)));
    test(List.of(1), () -> Iterator.<Integer>of().foldRightWhile(1, s -> s < 4, Integer::sum));
    test(List.of(List.of()),
        () -> Iterator.of().foldRightWhile(List.of(), List::isEmpty, (i, l) -> l.append(i)));
  }

  @Test
  public void includes() throws Exception {
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5).includes(null));
    test(List.of(false), () -> Iterator.of(1, 2, 3, null, 5).includes(0));
    test(List.of(false), () -> Iterator.of().includes(null));
    test(List.of(false), () -> Iterator.of().includes(0));
  }

  @Test
  public void includesAll() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).includesAll(null));
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5).includesAll(List.of(null, 1)));
    test(List.of(false), () -> Iterator.of(1, 2, 3, null, 5).includesAll(List.of(0, 1)));
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5).includesAll(List.of()));
    test(List.of(false), () -> Iterator.of().includesAll(List.of(null, 1)));
    test(List.of(true), () -> Iterator.of().includesAll(List.of()));
  }

  @Test
  public void includesSlice() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).includesSlice(null));
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5).includesSlice(List.of(3, null)));
    test(List.of(false), () -> Iterator.of(1, 2, 3, null, 5).includesSlice(List.of(null, 3)));
    test(List.of(true), () -> Iterator.of(1, 2, 3, null, 5).includesSlice(List.of()));
    test(List.of(false), () -> Iterator.of().includesSlice(List.of(null, 1)));
    test(List.of(true), () -> Iterator.of().includesSlice(List.of()));
  }

  @Test
  public void insert() throws Exception {
    test(List.of(3, 2, 1), () -> Iterator.<Integer>of().insert(1).insert(2).insert(3));
    test(List.of(3, null, 1), () -> Iterator.<Integer>of().insert(1).insert(null).insert(3));
    test(List.of(3, 2, 1), () -> Iterator.of(1).insert(2).insert(3));
    test(List.of(3, null, 1), () -> Iterator.of(1).insert(null).insert(3));
    test(List.of(3, 1, 2), () -> Iterator.of(1, 2).insert(3));
    test(List.of(3, 1, null), () -> Iterator.of(1, null).insert(3));
    test(List.of(2, null), () -> Iterator.of(1, null).drop(1).insert(2));
  }

  @Test
  public void insertAfter() throws Exception {
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).insertAfter(5, null));
    test(List.of(1, 2, 3, null), () -> Iterator.of(1, 2, 3).insertAfter(3, null));
    test(List.of(1, 2, null, 3), () -> Iterator.of(1, 2, 3).insertAfter(2, null));
    test(List.of(1, null, 2, 3), () -> Iterator.of(1, 2, 3).insertAfter(1, null));
    test(List.of(null, 1, 2, 3), () -> Iterator.of(1, 2, 3).insertAfter(0, null));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).insertAfter(-7, null));
    test(List.of(), () -> Iterator.of().insertAfter(5, null));
    test(List.of(null), () -> Iterator.of().insertAfter(0, null));
    test(List.of(), () -> Iterator.wrap(() -> List.of().iterator()).insertAfter(5, null));
    test(List.of(null), () -> Iterator.wrap(() -> List.of().iterator()).insertAfter(0, null));
  }

  @Test
  public void insertAll() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).insertAll(null));
    test(List.of(1, 2, 3), () -> Iterator.<Integer>of().insertAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), () -> Iterator.<Integer>of().insertAll(Arrays.asList(1, null, 3)));
    test(List.of(2, 3, 1), () -> Iterator.of(1).insertAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(null, 3, 1),
        () -> Iterator.of(1).insertAll(new LinkedHashSet<>(List.of(null, 3))));
    test(List.of(3, 1, 2), () -> Iterator.of(1, 2).insertAll(Set.of(3)));
    test(List.of(3, 1, null), () -> Iterator.of(1, null).insertAll(Set.of(3)));
    test(List.of(2, 3, null), () -> Iterator.of(1, null).drop(1).insertAll(Iterator.of(2, 3)));
  }

  @Test
  public void insertAllAfter() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).insertAllAfter(0, null));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).insertAllAfter(5, List.of(null, 5)));
    test(List.of(1, 2, 3, null, 5), () -> Iterator.of(1, 2, 3).insertAllAfter(3, List.of(null, 5)));
    test(List.of(1, 2, null, 5, 3), () -> Iterator.of(1, 2, 3).insertAllAfter(2, List.of(null, 5)));
    test(List.of(1, null, 5, 2, 3), () -> Iterator.of(1, 2, 3).insertAllAfter(1, List.of(null, 5)));
    test(List.of(null, 5, 1, 2, 3), () -> Iterator.of(1, 2, 3).insertAllAfter(0, List.of(null, 5)));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).insertAllAfter(-7, List.of(null, 5)));
    test(List.of(), () -> Iterator.of().insertAllAfter(5, List.of(null, 5)));
    test(List.of(null, 5), () -> Iterator.of().insertAllAfter(0, List.of(null, 5)));
    test(List.of(),
        () -> Iterator.wrap(() -> List.of().iterator()).insertAllAfter(5, List.of(null, 5)));
    test(List.of(null, 5),
        () -> Iterator.wrap(() -> List.of().iterator()).insertAllAfter(0, List.of(null, 5)));
  }

  @Test
  public void intersect() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).intersect(null));
    test(List.of(1, null), () -> Iterator.of(1, 2, null, 4).intersect(List.of(1, null)));
    test(List.of(1, 4), () -> Iterator.of(1, 2, null, 4).intersect(Iterator.of(1, 4)));
    test(List.of(1, 4), () -> Iterator.of(1, 2, null, 4).intersect(List.of(1, 3, 4)));
    test(List.of(1), () -> Iterator.of(1, 2, null, 4).intersect(Iterator.of(3, 1, 3)));
    test(List.of(null), () -> Iterator.of(1, 2, null, 4).intersect(List.of(null, null)));
    test(List.of(1, null), () -> Iterator.of(1, null).intersect(Iterator.of(1, 2, null, 4)));
    test(List.of(1, 2), () -> Iterator.of(1, 2, null, 4).intersect(List.of(2, 1)));
    test(List.of(), () -> Iterator.of(1, null).intersect(Iterator.of(2, 4)));
    test(List.of(), () -> Iterator.of(1, 2, null, 4).intersect(Iterator.of()));
    test(List.of(), () -> Iterator.of().intersect(Iterator.of(1, 2, null, 4)));
  }

  @Test
  public void map() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).map((Function<? super Integer, Object>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).map((IndexedFunction<? super Integer, Object>) null));
    test(List.of(2, 3, 4), () -> Iterator.of(1, 2, 3).map(x -> x + 1));
    test(List.of(), () -> Iterator.<Integer>of().map(x -> x + 1));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3);
    assertFalse(itr.get().append(null).map(x -> x + 1).isEmpty());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).size());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).drop(2).first());
    assertEquals(2, itr.get().append(null).map(x -> x + 1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).map(x -> x + 1).drop(3).first());
  }

  @Test
  public void mapAfter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).mapAfter(0, (Function<? super Integer, ? extends Integer>) null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0)
        .mapAfter(0, (IndexedFunction<? super Integer, ? extends Integer>) null));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).mapAfter(-1, x -> x + 1));
    test(List.of(2, 2, 3), () -> Iterator.of(1, 2, 3).mapAfter(0, x -> x + 1));
    test(List.of(1, 3, 3), () -> Iterator.of(1, 2, 3).mapAfter(1, x -> x + 1));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, 3).mapAfter(2, x -> x + 1));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).mapAfter(3, x -> x + 1));
    test(List.of(1, 2, 3, null), () -> Iterator.of(1, 2, 3).append(null).mapAfter(-1, x -> x + 1));
    test(List.of(1, 3, 3, null), () -> Iterator.of(1, 2, 3).append(null).mapAfter(1, x -> x + 1));
    test(List.of(), () -> Iterator.<Integer>of().mapAfter(0, x -> x + 1));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3);
    assertEquals(4, itr.get().append(null).mapAfter(3, x -> x + 1).size());
    assertEquals(2, itr.get().append(null).mapAfter(3, x -> x + 1).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).mapAfter(3, x -> x + 1).drop(3).first());
  }

  @Test
  public void mapFirstWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapFirstWhere(null, (i, e) -> e));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapFirstWhere(null, e -> e));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).mapFirstWhere((i, e) -> false, null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapFirstWhere(e -> false, null));
    test(List.of(1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).mapFirstWhere(i -> false, i -> i + 1));
    test(List.of(2, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).mapFirstWhere(i -> true, i -> i + 1));
    test(List.of(1, 2, 3, 4),
        () -> Iterator.of(1, 2, null, 4).mapFirstWhere(Objects::isNull, i -> 3));
    test(List.of(2, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).mapFirstWhere(i -> i == 1, i -> i + 1));
    test(List.of(), () -> Iterator.<Integer>of().mapFirstWhere(i -> false, i -> i + 1));
    test(List.of(), () -> Iterator.<Integer>of().mapFirstWhere(i -> true, i -> i + 1));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().mapFirstWhere(i -> i > 2, i -> 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapFirstWhere(i -> i > 2, i -> 1).size());
    assertEquals(1, itr.get().mapFirstWhere(i -> i > 2, i -> 1).first());
    assertEquals(2, itr.get().mapFirstWhere(i -> i > 2, i -> 1).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapFirstWhere(i -> i > 2, i -> 1).drop(2).first());
  }

  @Test
  public void mapLastWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapLastWhere(null, (i, e) -> e));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapLastWhere(null, e -> e));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).mapLastWhere((i, e) -> false, null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapLastWhere(e -> false, null));
    test(List.of(1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).mapLastWhere(i -> false, i -> i + 1));
    test(List.of(1, 2, null, 5),
        () -> Iterator.of(1, 2, null, 4).mapLastWhere(i -> true, i -> i + 1));
    test(List.of(1, 2, 3, 4),
        () -> Iterator.of(1, 2, null, 4).mapLastWhere(Objects::isNull, i -> 3));
//    test(List.of(1, 2, null, 5),
//        () -> Iterator.of(1, 2, null, 4).mapLastWhere(i -> i == 4, i -> i + 1));
    test(List.of(), () -> Iterator.<Integer>of().mapLastWhere(i -> false, i -> i + 1));
    test(List.of(), () -> Iterator.<Integer>of().mapLastWhere(i -> true, i -> i + 1));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
//    assertThrows(NullPointerException.class,
//        () -> itr.get().mapLastWhere(i -> i < 2, i -> 1).isEmpty());
//    assertThrows(NullPointerException.class,
//        () -> itr.get().mapLastWhere(i -> i < 2, i -> 1).size());
//    assertThrows(NullPointerException.class,
//        () -> itr.get().mapLastWhere(i -> i < 2, i -> 1).first());
    assertFalse(itr.get().mapLastWhere(i -> i == 4, i -> i + 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapLastWhere(i -> i == 4, i -> i + 1).size());
    assertEquals(1, itr.get().mapLastWhere(i -> i == 4, i -> i + 1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapLastWhere(i -> i < 2, i -> 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapLastWhere(i -> i < 2, i -> 1).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapLastWhere(i -> i < 2, i -> 1).first());
  }

  @Test
  public void mapWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapWhere(null, (i, e) -> e));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapWhere(null, e -> e));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapWhere((i, e) -> false, null));
    assertThrows(NullPointerException.class, () -> Iterator.of(0).mapWhere(e -> false, null));
    test(List.of(1, 2, 3, 4), () -> Iterator.of(1, 2, 3, 4).mapWhere(i -> false, i -> i + 1));
    test(List.of(2, 3, 4, 5), () -> Iterator.of(1, 2, 3, 4).mapWhere(i -> true, i -> i + 1));
    test(List.of(1, 3, 3, 4), () -> Iterator.of(1, 2, 3, 4).mapWhere(i -> i == 2, i -> 3));
    test(List.of(), () -> Iterator.<Integer>of().mapLastWhere(i -> false, i -> i + 1));
    test(List.of(), () -> Iterator.<Integer>of().mapLastWhere(i -> true, i -> i + 1));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null);
    assertFalse(itr.get().mapWhere(i -> i == 4, i -> i + 1).isEmpty());
    assertEquals(3, itr.get().mapWhere(i -> i == 4, i -> i + 1).size());
    assertEquals(1, itr.get().mapWhere(i -> i == 4, i -> i + 1).first());
    assertEquals(2, itr.get().mapWhere(i -> i == 4, i -> i + 1).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapWhere(i -> i == 4, i -> i + 1).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapWhere(i -> i == 4, i -> i + 1).drop(3).first());
  }

  @Test
  public void max() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).max(null));
    test(List.of(4), () -> Iterator.of(1, 4, 2, 3).max(Integer::compare));
    test(List.of(), () -> Iterator.<Integer>of().max(Integer::compare));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).max(Integer::compareTo).isEmpty());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).max(Integer::compareTo).notEmpty());
    assertEquals(1, Iterator.of(1, null).max(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).max(Integer::compareTo).first());
  }

  @Test
  public void min() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).min(null));
    test(List.of(1), () -> Iterator.of(1, 4, 2, 3).min(Integer::compare));
    test(List.of(), () -> Iterator.<Integer>of().min(Integer::compare));

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).min(Integer::compareTo).isEmpty());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).min(Integer::compareTo).notEmpty());
    assertEquals(1, Iterator.of(1, null).min(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).min(Integer::compareTo).first());
  }

  @Test
  public void none() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).none((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).none((Predicate<? super Integer>) null));
    test(List.of(true), () -> Iterator.of().none(Objects::nonNull));
    test(List.of(false), () -> Iterator.of(1, 2, 3).none(i -> i < 3));
    test(List.of(true), () -> Iterator.of(1, 2, 3).none(i -> i < 0));

    var itr = Iterator.of(1, null, 3).none(i -> i < 0);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void notAll() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).notAll((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).notAll((Predicate<? super Integer>) null));
    test(List.of(true), () -> Iterator.of().notAll(Objects::isNull));
    test(List.of(false), () -> Iterator.of(1, 2, 3).notAll(i -> i < 4));
    test(List.of(true), () -> Iterator.of(1, 2, 3).notAll(i -> i > 1));
    var itr = Iterator.of(1, null, 3).notAll(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void orElse() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of().orElse(null));
    test(List.of(1), () -> Iterator.of(1).orElse(Iterator.of(2)));
    test(List.of(1), () -> Iterator.of(1).orElse(Iterator.of()));
    test(List.of(2), () -> Iterator.of().orElse(Iterator.of(2)));
    test(List.of(), () -> Iterator.of().orElse(Iterator.of()));
  }

  @Test
  public void orElseGet() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of().orElseGet(null));
    test(List.of(1), () -> Iterator.of(1).orElseGet(() -> Iterator.of(2)));
    test(List.of(1), () -> Iterator.of(1).orElseGet(List::of));
    test(List.of(1), () -> Iterator.of(1).orElseGet(Iterator::of));
    test(List.of(2), () -> Iterator.of().orElseGet(() -> Iterator.of(2)));
    test(List.of(), () -> Iterator.of().orElseGet(Iterator::of));

    sparx.util.function.Supplier<Iterator<Integer>> throwing = () -> {
      throw new IllegalStateException();
    };
    test(List.of(1), () -> Iterator.of(1).orElseGet(throwing));
    assertThrows(IllegalStateException.class, () -> Iterator.of().orElseGet(throwing).isEmpty());
    assertThrows(IllegalStateException.class, () -> Iterator.of().orElseGet(throwing).notEmpty());
    assertThrows(IllegalStateException.class, () -> Iterator.of().orElseGet(throwing).size());
    assertThrows(IllegalStateException.class, () -> Iterator.of().orElseGet(throwing).first());
  }

  @Test
  public void peek() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).peek((Consumer<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).peek((IndexedConsumer<? super Integer>) null));
    var set = new HashSet<Integer>();
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).peek(set::add));
    assertEquals(Set.of(1, 2, 3), set);
    set.clear();
    test(List.of(), () -> Iterator.<Integer>of().peek(set::add));
    assertEquals(Set.of(), set);

    var list = new ArrayList<Integer>();
    assertEquals(List.of(1, 2, 3), Iterator.of(1, 2, 3).peek(i -> list.add(i)).toList());
    assertEquals(List.of(1, 2, 3), list);
    list.clear();
    assertEquals(1, Iterator.of(1, 2, 3).peek(i -> list.add(i)).next());
    assertEquals(List.of(1), list);
    list.clear();
    assertEquals(List.of(3), Iterator.of(1, 2, 3).peek(i -> list.add(i)).drop(2).toList());
    assertEquals(List.of(3), list);
  }

  @Test
  public void peekExceptionally() {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).peekExceptionally((Consumer<? super Throwable>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).peekExceptionally((IndexedConsumer<? super Throwable>) null));
    var ex = new AtomicReference<Throwable>();
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).filter(i -> i > 0).drop(1).peekExceptionally(ex::set).next());
    assertInstanceOf(NullPointerException.class, ex.get());
  }

  @Test
  public void plus() throws Exception {
    test(List.of(1, 2, 3), () -> Iterator.<Integer>of().plus(1).plus(2).plus(3));
    test(List.of(1, null, 3), () -> Iterator.<Integer>of().plus(1).plus(null).plus(3));
    test(List.of(1, 2, 3), () -> Iterator.of(1).plus(2).plus(3));
    test(List.of(1, null, 3), () -> Iterator.of(1).plus(null).plus(3));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2).plus(3));
    test(List.of(1, null, 3), () -> Iterator.of(1, null).plus(3));
  }

  @Test
  public void plusAll() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of().plusAll(null));
    test(List.of(1, 2, 3), () -> Iterator.<Integer>of().plusAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), () -> Iterator.<Integer>of().plusAll(List.of(1, null, 3)));
    test(List.of(1, null, 3), () -> Iterator.<Integer>of().plusAll(Iterator.of(1, null, 3)));
    test(List.of(1, 2, 3), () -> Iterator.of(1).plusAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(1, null, 3), () -> Iterator.of(1).plusAll(List.of(null, 3)));
    test(List.of(1, null, 3), () -> Iterator.of(1).plusAll(Iterator.of(null, 3)));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2).plusAll(Set.of(3)));
    test(List.of(1, null, 3), () -> Iterator.of(1, null).plusAll(Set.of(3)));
    test(List.of(1, null, 3), () -> Iterator.of(1, null).plusAll(Iterator.of(3)));
  }

  @Test
  public void reduceLeft() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0, 0).reduceLeft(null));
    test(List.of(15), () -> Iterator.of(1, 2, 3, 4, 5).reduceLeft(Integer::sum));
    test(List.of(), () -> Iterator.<Integer>of().reduceLeft(Integer::sum));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, 2, null).reduceLeft(Integer::sum).first());
  }

  @Test
  public void reduceRight() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0, 0).reduceRight(null));
    test(List.of(15), () -> Iterator.of(1, 2, 3, 4, 5).reduceRight(Integer::sum));
    test(List.of(), () -> Iterator.<Integer>of().reduceRight(Integer::sum));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, 2, null).reduceRight(Integer::sum).first());
  }

  @Test
  public void removeAfter() throws Exception {
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).removeAfter(5));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).removeAfter(3));
    test(List.of(1, 2), () -> Iterator.of(1, 2, 3).removeAfter(2));
    test(List.of(1, 3), () -> Iterator.of(1, 2, 3).removeAfter(1));
    test(List.of(2, 3), () -> Iterator.of(1, 2, 3).removeAfter(0));
    test(List.of(1, 2, 3), () -> Iterator.of(1, 2, 3).removeAfter(-7));
    test(List.of(), () -> Iterator.of().removeAfter(5));
    test(List.of(), () -> Iterator.of().removeAfter(0));
    test(List.of(), () -> Iterator.of().removeAfter(-7));
  }

  @Test
  public void removeEach() throws Exception {
    test(List.of(2, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeEach(1));
    test(List.of(1, 2, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeEach(null));
    test(List.of(1, null, 4), () -> Iterator.of(1, 2, null, 4, 2).removeEach(2));
    test(List.of(1, 2, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeEach(0));
    test(List.of(), () -> Iterator.of().removeEach(1));
    test(List.of(), () -> Iterator.of().removeEach(null));
  }

  @Test
  public void removeFirst() throws Exception {
    test(List.of(2, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeFirst(1));
    test(List.of(1, 2, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeFirst(null));
    test(List.of(1, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeFirst(2));
    test(List.of(1, 2, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeFirst(0));
    test(List.of(), () -> Iterator.of().removeFirst(1));
  }

  @Test
  public void removeFirstWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).removeFirstWhere((Predicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).removeFirstWhere((IndexedPredicate<? super Integer>) null));
    test(List.of(2, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeFirstWhere(i -> i == 1));
    test(List.of(1, 2, 4, 2),
        () -> Iterator.of(1, 2, null, 4, 2).removeFirstWhere(Objects::isNull));
    test(List.of(1, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeFirstWhere(i -> i == 2));
    test(List.of(1, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeFirstWhere(i -> i > 1));
    test(List.of(1, 2, null, 4, 2),
        () -> Iterator.of(1, 2, null, 4, 2).removeFirstWhere(i -> false));
    test(List.of(), () -> Iterator.<Integer>of().removeFirstWhere(i -> i == 1));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 2);
    assertFalse(itr.get().removeFirstWhere(i -> i > 2).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().removeFirstWhere(i -> i > 2).size());
    assertEquals(2, itr.get().removeFirstWhere(i -> i > 2).drop(1).first());
  }

  @Test
  public void removeLast() throws Exception {
    test(List.of(2, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeLast(1));
    test(List.of(1, 2, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeLast(null));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4, 2).removeLast(2));
    test(List.of(1, 2, null, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeLast(0));
    test(List.of(), () -> Iterator.of().removeLast(1));
  }

  @Test
  public void removeLastWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).removeLastWhere((Predicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).removeLastWhere((IndexedPredicate<? super Integer>) null));
    test(List.of(2, null, 4, 2),
        () -> Iterator.of(1, 2, null, 4, 2).removeLastWhere(Integer.valueOf(1)::equals));
    test(List.of(1, 2, 4, 2), () -> Iterator.of(1, 2, null, 4, 2).removeLastWhere(Objects::isNull));
    test(List.of(1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4, 2).removeLastWhere(Integer.valueOf(2)::equals));
    test(List.of(1, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4, 2).removeLastWhere(i -> i != null && i > 1));
    test(List.of(1, 2, null, 2),
        () -> Iterator.of(1, 2, null, 4, 2).removeLastWhere(i -> i != null && i > 2));
    test(List.of(1, 2, null, 4, 2),
        () -> Iterator.of(1, 2, null, 4, 2).removeLastWhere(i -> false));
    test(List.of(), () -> Iterator.<Integer>of().removeLastWhere(i -> i == 1));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 2);
    assertFalse(itr.get().removeLastWhere(i -> i > 2).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().removeLastWhere(i -> i > 2).size());
    assertEquals(2, itr.get().removeLastWhere(i -> i > 2).drop(1).first());
  }

  @Test
  public void removeSlice() throws Exception {
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(1, 1));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(1, 0));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(1, -3));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(1, -4));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(1, -5));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(-1, 1));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(-1, 3));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(-1, -1));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(-1, -4));
    test(List.of(1, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(1, -1));
    test(List.of(1, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(1, -2));
    test(List.of(1, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(1, 3));
    test(List.of(1, null, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(1, 2));
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null, 4).removeSlice(-1, 4));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null, 4).removeSlice(-2, -1));
    test(List.of(), () -> Iterator.of(1, 2, null, 4).removeSlice(0, Integer.MAX_VALUE));
    test(List.of(), () -> Iterator.of().removeSlice(1, -1));
    // TODO: add tests with Integer.MAX_VALUE, Integer.MIN_VALUE
  }

  @Test
  public void removeWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).removeWhere((IndexedPredicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).removeWhere((Predicate<? super Integer>) null));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).removeWhere(i -> false));
    test(List.of(), () -> Iterator.of(1, 2, null, 4).removeWhere(i -> true));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null, 4).removeWhere(Objects::isNull));
    test(List.of(null), () -> Iterator.of(1, 2, null, 4).removeWhere(Objects::nonNull));
    test(List.of(), () -> Iterator.of().removeWhere(i -> false));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().removeWhere(i -> i < 2).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().removeWhere(i -> i < 2).size());
    assertEquals(2, itr.get().removeWhere(i -> i < 2).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().removeWhere(i -> i < 2).drop(1).first());
  }

  @Test
  public void replaceAfter() throws Exception {
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null).replaceAfter(-1, 4));
    test(List.of(4, 2, null), () -> Iterator.of(1, 2, null).replaceAfter(0, 4));
    test(List.of(1, 4, null), () -> Iterator.of(1, 2, null).replaceAfter(1, 4));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null).replaceAfter(2, 4));
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null).replaceAfter(3, 4));
    test(List.of(), () -> Iterator.of().replaceAfter(0, 4));
  }

  @Test
  public void replaceEach() throws Exception {
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null).replaceEach(-1, 4));
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null).replaceEach(0, 4));
    test(List.of(4, 2, null), () -> Iterator.of(1, 2, null).replaceEach(1, 4));
    test(List.of(1, 4, null), () -> Iterator.of(1, 2, null).replaceEach(2, 4));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null).replaceEach(null, 4));
    test(List.of(4, 2, null, 4), () -> Iterator.of(1, 2, null, 1).replaceEach(1, 4));
    test(List.of(), () -> Iterator.of().replaceEach(0, 4));
  }

  @Test
  public void replaceFirst() throws Exception {
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null).replaceFirst(-1, 4));
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null).replaceFirst(0, 4));
    test(List.of(4, 2, null), () -> Iterator.of(1, 2, null).replaceFirst(1, 4));
    test(List.of(1, 4, null), () -> Iterator.of(1, 2, null).replaceFirst(2, 4));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null).replaceFirst(null, 4));
    test(List.of(4, 2, null, 1), () -> Iterator.of(1, 2, null, 1).replaceFirst(1, 4));
    test(List.of(), () -> Iterator.of().replaceFirst(0, 4));
  }

  @Test
  public void replaceFirstWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).replaceFirstWhere((IndexedPredicate<? super Integer>) null, 0));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).replaceFirstWhere((Predicate<? super Integer>) null, 0));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).replaceFirstWhere(i -> false, 4));
    test(List.of(4, 2, null, 4), () -> Iterator.of(1, 2, null, 4).replaceFirstWhere(i -> true, 4));
    test(List.of(1, 2, 4, 4),
        () -> Iterator.of(1, 2, null, 4).replaceFirstWhere(Objects::isNull, 4));
    test(List.of(2, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).replaceFirstWhere(i -> i == 1, 2));
    test(List.of(), () -> Iterator.of().replaceFirstWhere(i -> false, 4));
    test(List.of(), () -> Iterator.of().replaceFirstWhere(i -> true, 4));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().replaceFirstWhere(i -> i > 2, 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceFirstWhere(i -> i > 2, 1).size());
    assertEquals(1, itr.get().replaceFirstWhere(i -> i > 2, 1).first());
    assertEquals(2, itr.get().replaceFirstWhere(i -> i > 2, 1).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceFirstWhere(i -> i > 2, 1).drop(2).first());
  }

  @Test
  public void replaceLast() throws Exception {
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null).replaceLast(-1, 4));
    test(List.of(1, 2, null), () -> Iterator.of(1, 2, null).replaceLast(0, 4));
    test(List.of(4, 2, null), () -> Iterator.of(1, 2, null).replaceLast(1, 4));
    test(List.of(1, 4, null), () -> Iterator.of(1, 2, null).replaceLast(2, 4));
    test(List.of(1, 2, 4), () -> Iterator.of(1, 2, null).replaceLast(null, 4));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 1).replaceLast(1, 4));
    test(List.of(), () -> Iterator.of().replaceLast(0, 4));
  }

  @Test
  public void replaceLastWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).replaceLastWhere((IndexedPredicate<? super Integer>) null, 0));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).replaceLastWhere((Predicate<? super Integer>) null, 0));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).replaceLastWhere(i -> false, 5));
    test(List.of(1, 2, null, 5), () -> Iterator.of(1, 2, null, 4).replaceLastWhere(i -> true, 5));
    test(List.of(1, 2, 3, 4),
        () -> Iterator.of(1, 2, null, 4).replaceLastWhere(Objects::isNull, 3));
    test(List.of(), () -> Iterator.of().replaceLastWhere(i -> false, 4));
    test(List.of(), () -> Iterator.of().replaceLastWhere(i -> true, 4));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().replaceLastWhere(i -> i == 4, 5).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceLastWhere(i -> i == 4, 5).size());
    assertEquals(2, itr.get().replaceLastWhere(i -> i == 4, 5).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceLastWhere(i -> i == 4, 5).drop(2).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceLastWhere(i -> i == 4, 5).drop(5).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceLastWhere(i -> i < 2, 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceLastWhere(i -> i < 2, 1).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceLastWhere(i -> i < 2, 1).first());
  }

  @Test
  public void replaceSlice() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).replaceSlice(0, 1, null));
    test(List.of(1, 5, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(1, 1, List.of(5)));
    test(List.of(1, 5, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(1, 0, List.of(5)));
    test(List.of(1, 5, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(1, -3, List.of(5)));
    test(List.of(1, 5, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(1, -4, List.of(5)));
    test(List.of(1, 5, 2, null, 4),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(1, -5, List.of(5)));
    test(List.of(1, 2, null, 5, 4),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(-1, 1, List.of(5)));
    test(List.of(1, 2, null, 5, 4),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(-1, 3, List.of(5)));
    test(List.of(1, 2, null, 5, 4),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(-1, -1, List.of(5)));
    test(List.of(1, 2, null, 5, 4),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(-1, -4, List.of(5)));
    test(List.of(1, 5, 4), () -> Iterator.of(1, 2, null, 4).replaceSlice(1, -1, List.of(5)));
    test(List.of(1, 5, null, 4), () -> Iterator.of(1, 2, null, 4).replaceSlice(1, -2, List.of(5)));
    test(List.of(1, 5, 4), () -> Iterator.of(1, 2, null, 4).replaceSlice(1, 3, List.of(5)));
    test(List.of(1, 5, null, 4), () -> Iterator.of(1, 2, null, 4).replaceSlice(1, 2, List.of(5)));
    test(List.of(1, 2, null, 5), () -> Iterator.of(1, 2, null, 4).replaceSlice(-1, 4, List.of(5)));
    test(List.of(1, 2, 5, 4), () -> Iterator.of(1, 2, null, 4).replaceSlice(-2, -1, List.of(5)));
    test(List.of(5),
        () -> Iterator.of(1, 2, null, 4).replaceSlice(0, Integer.MAX_VALUE, List.of(5)));
    test(List.of(), () -> Iterator.of(1, 2, null, 4).replaceSlice(0, Integer.MAX_VALUE, List.of()));
    test(List.of(5), () -> Iterator.of().replaceSlice(0, 0, List.of(5)));
    test(List.of(5), () -> Iterator.of().replaceSlice(1, -1, List.of(5)));
  }

  @Test
  public void replaceWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).replaceWhere((IndexedPredicate<? super Integer>) null, 0));
    assertThrows(NullPointerException.class,
        () -> Iterator.of(0).replaceWhere((Predicate<? super Integer>) null, 0));
    test(List.of(1, 2, 3, 4), () -> Iterator.of(1, 2, 3, 4).replaceWhere(i -> false, 5));
    test(List.of(5, 5, 5, 5), () -> Iterator.of(1, 2, 3, 4).replaceWhere(i -> true, 5));
    test(List.of(1, 3, 3, 4), () -> Iterator.of(1, 2, 3, 4).replaceWhere(i -> i == 2, 3));
    test(List.of(), () -> Iterator.of().replaceWhere(i -> false, 4));
    test(List.of(), () -> Iterator.of().replaceWhere(i -> true, 4));

    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, 4, null);
    assertFalse(itr.get().replaceWhere(i -> i == 4, 5).isEmpty());
    assertEquals(5, itr.get().replaceWhere(i -> i == 4, 5).size());
    assertEquals(2, itr.get().replaceWhere(i -> i == 4, 5).drop(1).first());
    assertEquals(3, itr.get().replaceWhere(i -> i == 4, 5).drop(2).first());
    assertEquals(5, itr.get().replaceWhere(i -> i == 4, 5).drop(3).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceWhere(i -> i == 4, 5).drop(4).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceWhere(i -> i == 4, 5).drop(5).first());
  }

  @Test
  public void resizeTo() {
    assertThrows(IllegalArgumentException.class, () -> Iterator.of(1, 2, null, 4).resizeTo(-1, 5));
    assertEquals(List.of(), Iterator.of(1, 2, null, 4).resizeTo(0, 5).toList());
    assertEquals(List.of(1), Iterator.of(1, 2, null, 4).resizeTo(1, 5).toList());
    assertEquals(List.of(1, 2), Iterator.of(1, 2, null, 4).resizeTo(2, 5).toList());
    assertEquals(List.of(1, 2, null), Iterator.of(1, 2, null, 4).resizeTo(3, 5).toList());
    assertEquals(List.of(1, 2, null, 4), Iterator.of(1, 2, null, 4).resizeTo(4, 5).toList());
    assertEquals(List.of(1, 2, null, 4, 5), Iterator.of(1, 2, null, 4).resizeTo(5, 5).toList());
    assertEquals(List.of(1, 2, null, 4, 5, 5), Iterator.of(1, 2, null, 4).resizeTo(6, 5).toList());
  }

  @Test
  public void runFinally() {
    var called = new AtomicBoolean();
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).filter(i -> i > 0).drop(1).runFinally(() -> called.set(true))
            .next());
    assertTrue(called.get());
    called.set(false);
    assertEquals(3, Iterator.of(1, null, 3).runFinally(() -> called.set(true)).size());
    assertTrue(called.get());
    called.set(false);
    Iterator.of(1, null, 3).runFinally(() -> called.set(true)).doFor(i -> {
    });
    assertTrue(called.get());
    called.set(false);
    Iterator.of(1, null, 3).runFinally(() -> called.set(true)).doWhile((i, v) -> i < 1);
    assertFalse(called.get());
  }

  @Test
  public void slice() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertTrue(itr.get().slice(1, 1).isEmpty());
    assertEquals(0, itr.get().slice(1, 1).size());
    assertEquals(List.of(), itr.get().slice(1, 1).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().slice(1, 1).first());
    assertTrue(itr.get().slice(1, 0).isEmpty());
    assertEquals(0, itr.get().slice(1, 0).size());
    assertEquals(List.of(), itr.get().slice(1, 0).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().slice(1, 0).first());
    assertTrue(itr.get().slice(1, -3).isEmpty());
    assertEquals(0, itr.get().slice(1, -3).size());
    assertEquals(List.of(), itr.get().slice(1, -3).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().slice(1, -3).first());
    assertTrue(itr.get().slice(1, -4).isEmpty());
    assertEquals(0, itr.get().slice(1, -4).size());
    assertEquals(List.of(), itr.get().slice(1, -4).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().slice(1, -4).first());
    assertTrue(itr.get().slice(1, -5).isEmpty());
    assertEquals(0, itr.get().slice(1, -5).size());
    assertEquals(List.of(), itr.get().slice(1, -5).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().slice(1, -5).first());
    assertTrue(itr.get().slice(-1, 1).isEmpty());
    assertEquals(0, itr.get().slice(-1, 1).size());
    assertEquals(List.of(), itr.get().slice(-1, 1).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().slice(-1, 1).first());
    assertTrue(itr.get().slice(-1, 3).isEmpty());
    assertEquals(0, itr.get().slice(-1, 3).size());
    assertEquals(List.of(), itr.get().slice(-1, 3).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().slice(-1, 3).first());
    assertTrue(itr.get().slice(-1, -1).isEmpty());
    assertEquals(0, itr.get().slice(-1, -1).size());
    assertEquals(List.of(), itr.get().slice(-1, -1).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().slice(-1, -1).first());
    assertTrue(itr.get().slice(-1, -4).isEmpty());
    assertEquals(0, itr.get().slice(-1, -4).size());
    assertEquals(List.of(), itr.get().slice(-1, -4).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().slice(-1, -4).first());

    assertFalse(itr.get().slice(1, -1).isEmpty());
    assertEquals(2, itr.get().slice(1, -1).size());
    assertEquals(List.of(2, null), itr.get().slice(1, -1).toList());
    assertNull(itr.get().slice(1, -1).drop(1).first());
    assertFalse(itr.get().slice(1, -2).isEmpty());
    assertEquals(1, itr.get().slice(1, -2).size());
    assertEquals(List.of(2), itr.get().slice(1, -2).toList());
    assertEquals(2, itr.get().slice(1, -2).first());
    assertFalse(itr.get().slice(1, 3).isEmpty());
    assertEquals(2, itr.get().slice(1, 3).size());
    assertEquals(List.of(2, null), itr.get().slice(1, 3).toList());
    assertNull(itr.get().slice(1, 3).drop(1).first());
    assertFalse(itr.get().slice(1, 2).isEmpty());
    assertEquals(1, itr.get().slice(1, 2).size());
    assertEquals(List.of(2), itr.get().slice(1, 2).toList());
    assertEquals(2, itr.get().slice(1, 2).first());
    assertFalse(itr.get().slice(-1, 4).isEmpty());
    assertEquals(1, itr.get().slice(-1, 4).size());
    assertEquals(List.of(4), itr.get().slice(-1, 4).toList());
    assertEquals(4, itr.get().slice(-1, 4).first());
    assertFalse(itr.get().slice(-2, -1).isEmpty());
    assertEquals(1, itr.get().slice(-2, -1).size());
    assertEquals(List.of(null), itr.get().slice(-2, -1).toList());
    assertNull(itr.get().slice(-2, -1).first());

    assertFalse(itr.get().slice(0, Integer.MAX_VALUE).isEmpty());
    assertEquals(4, itr.get().slice(0, Integer.MAX_VALUE).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().slice(0, Integer.MAX_VALUE).toList());
    assertEquals(2, itr.get().slice(0, Integer.MAX_VALUE).drop(1).first());

    assertTrue(Iterator.of().slice(1, -1).isEmpty());
    assertEquals(0, Iterator.of().slice(1, -1).size());
    assertEquals(List.of(), Iterator.of().slice(1, -1).toList());
    assertThrows(NoSuchElementException.class, () -> Iterator.of().slice(1, -1).first());
  }

  @Test
  public void slidingWindow() {
    Supplier<Iterator<? extends Iterator<Integer>>> itr = () -> Iterator.of(1, 2, 3, 4, 5, 6)
        .slidingWindow(3, 1);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(6, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(2, 3, 4), List.of(3, 4, 5), List.of(4, 5, 6),
        List.of(5, 6), List.of(6)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(3, 2);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(3, 4, 5), List.of(5, 6)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(3, 3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(3, 4);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(5, 6)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(3, 5);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(6)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(3, 6);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(2, 1);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(6, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(2, 3), List.of(3, 4), List.of(4, 5), List.of(5, 6),
        List.of(6)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(2, 2);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5, 6)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(2, 3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(4, 5)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(2, 4);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(5, 6)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(2, 5);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(6)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(2, 6);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1, 2)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(1, 1);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(6, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5), List.of(6)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(1, 2);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(3), List.of(5)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(1, 3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(4)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(1, 4);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(5)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(1, 5);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(6)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindow(1, 6);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindow(3, 1);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(2, 3), List.of(3)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindow(3, 2);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(3)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindow(3, 3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindow(4, 1);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(2, 3), List.of(3)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindow(4, 2);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(3)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindow(4, 3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3)), itr.get().map(Iterator::toList).toList());
  }

  @Test
  public void slidingWindowWithPadding() {
    Supplier<Iterator<? extends Iterator<Integer>>> itr = () -> Iterator.of(1, 2, 3, 4, 5, 6)
        .slidingWindowWithPadding(3, 1, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(6, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(2, 3, 4), List.of(3, 4, 5), List.of(4, 5, 6),
        List.of(5, 6, 0), List.of(6, 0, 0)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(3, 2, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(3, 4, 5), List.of(5, 6, 0)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(3, 3, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(3, 4, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(5, 6, 0)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(3, 5, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(6, 0, 0)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(3, 6, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(2, 1, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(6, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(2, 3), List.of(3, 4), List.of(4, 5), List.of(5, 6),
        List.of(6, 0)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(2, 2, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5, 6)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(2, 3, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(4, 5)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(2, 4, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(5, 6)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(2, 5, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2), List.of(6, 0)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(2, 6, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1, 2)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(1, 1, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(6, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5), List.of(6)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(1, 2, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(3), List.of(5)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(1, 3, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(4)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(1, 4, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(5)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(1, 5, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1), List.of(6)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3, 4, 5, 6).slidingWindowWithPadding(1, 6, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindowWithPadding(3, 1, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(2, 3, 0), List.of(3, 0, 0)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindowWithPadding(3, 2, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(3, 0, 0)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindowWithPadding(3, 3, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3)), itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindowWithPadding(4, 1, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3, 0), List.of(2, 3, 0, 0), List.of(3, 0, 0, 0)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindowWithPadding(4, 2, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3, 0), List.of(3, 0, 0, 0)),
        itr.get().map(Iterator::toList).toList());

    itr = () -> Iterator.of(1, 2, 3).slidingWindowWithPadding(4, 3, 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(List.of(1, 2, 3, 0)), itr.get().map(Iterator::toList).toList());
  }

  @Test
  public void startsWith() {
    Supplier<Iterator<Boolean>> itr = () -> Iterator.<Integer>of().startsWith(List.of());
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertTrue(itr.get().first());

    itr = () -> Iterator.<Integer>of().startsWith(List.of(1));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertFalse(itr.get().first());

    itr = () -> Iterator.of(1, null, 3).startsWith(List.of());
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertTrue(itr.get().first());

    itr = () -> Iterator.of(1, null, 3).startsWith(List.of(1));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertTrue(itr.get().first());

    itr = () -> Iterator.of(1, null, 3).startsWith(List.of(null));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertFalse(itr.get().first());

    itr = () -> Iterator.of(1, null, 3).startsWith(List.of(1, null));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertTrue(itr.get().first());

    itr = () -> Iterator.of(1, null, 3).startsWith(List.of(null, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertFalse(itr.get().first());

    itr = () -> Iterator.of(1, null, 3).startsWith(List.of(1, null, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertTrue(itr.get().first());

    itr = () -> Iterator.of(1, null, 3).startsWith(List.of(null, null, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertFalse(itr.get().first());
  }

  @Test
  public void switchExceptionally() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, null, 3).filter(i -> i > 0)
        .switchExceptionally(t -> List.of(4));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(1, 4), itr.get().toList());

    itr = () -> Iterator.of(1, 2, 3).filter(i -> i > 0).switchExceptionally(t -> List.of(4));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    itr = () -> Iterator.of(1, null, 3).filter(i -> i > 0).drop(1)
        .switchExceptionally(t -> List.of(4));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(4), itr.get().toList());

    itr = () -> Iterator.of(1, null, 3).filter(i -> i > 0)
        .switchExceptionally(NullPointerException.class, t -> List.of(4));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(1, 4), itr.get().toList());

    itr = () -> Iterator.of(1, null, 3).filter(i -> i > 0)
        .switchExceptionally(SizeOverflowException.class, t -> List.of(4));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    Supplier<Iterator<Integer>> iter = itr;
    assertThrows(NullPointerException.class, () -> iter.get().size());
  }

  @Test
  public void symmetricDiff() throws Exception {
    assertThrows(NullPointerException.class, () -> Iterator.of(0).symmetricDiff(null));
    test(List.of(2, 4), () -> Iterator.of(1, 2, null, 4).symmetricDiff(List.of(1, null)));
    test(List.of(2, null), () -> Iterator.of(1, 2, null, 4).symmetricDiff(List.of(1, 4)));
    test(List.of(2, null, 3), () -> Iterator.of(1, 2, null, 4).symmetricDiff(List.of(1, 3, 4)));
    test(List.of(2, null, 4, 3, 3),
        () -> Iterator.of(1, 2, null, 4).symmetricDiff(List.of(3, 1, 3)));
    test(List.of(1, 2, 4, null),
        () -> Iterator.of(1, 2, null, 4).symmetricDiff(List.of(null, null)));
    test(List.of(1, 2, 4, null),
        () -> Iterator.of(1, 1, 2, null, 4).symmetricDiff(List.of(null, null, 1)));
    test(List.of(), () -> Iterator.of(1, null).symmetricDiff(List.of(1, null)));
    test(List.of(1, 2, null, 4), () -> Iterator.of(1, 2, null, 4).symmetricDiff(List.of()));
    test(List.of(1, 1, 2, null, 4), () -> Iterator.of(1, 1, 2, null, 4).symmetricDiff(List.of()));
    test(List.of(1, 2, null, 4), () -> Iterator.of().symmetricDiff(List.of(1, 2, null, 4)));

  }

  @Test
  public void take() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.<Integer>of().take(1);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    itr = () -> Iterator.<Integer>of().take(0);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    itr = () -> Iterator.<Integer>of().take(-1);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());

    itr = () -> Iterator.of(1, null, 3).take(1);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(1), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).take(2);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(1, null), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).take(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).take(4);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).take(0);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).take(-1);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());
  }

  @Test
  public void takeRight() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.<Integer>of().takeRight(1);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    itr = () -> Iterator.<Integer>of().takeRight(0);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    itr = () -> Iterator.<Integer>of().takeRight(-1);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());

    itr = () -> Iterator.of(1, null, 3).takeRight(1);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(3), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).takeRight(2);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(null, 3), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).takeRight(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).takeRight(4);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).takeRight(0);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).takeRight(-1);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());
  }

  @Test
  public void takeRightWhile() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.<Integer>of().takeRightWhile(e -> e > 0);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());

    itr = () -> Iterator.of(1, null, 3).takeRightWhile(Objects::isNull);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).takeRightWhile(Objects::nonNull);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(3), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).takeRightWhile(e -> e < 1);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());

    itr = () -> Iterator.of(1, 2, 3).takeRightWhile(e -> e > 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).takeRightWhile(e -> e > 0).size());
  }

  @Test
  public void takeWhile() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.<Integer>of().takeWhile(e -> e > 0);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());

    itr = () -> Iterator.of(1, null, 3).takeWhile(Objects::isNull);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).takeWhile(Objects::nonNull);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(1, itr.get().size());
    assertEquals(List.of(1), itr.get().toList());
    itr = () -> Iterator.of(1, null, 3).takeWhile(e -> e < 1);
    assertTrue(itr.get().isEmpty());
    assertFalse(itr.get().notEmpty());
    assertEquals(0, itr.get().size());
    assertEquals(List.of(), itr.get().toList());

    itr = () -> Iterator.of(1, 2, 3).takeWhile(e -> e > 0);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).takeWhile(e -> e > 0).size());
  }

  @Test
  public void union() {
    assertEquals(List.of(1, 2, null, 4),
        Iterator.of(1, 2, null, 4).union(Iterator.of(1, null)).toList());
    assertEquals(List.of(1, 2, null, 4), Iterator.of(1, 2, null, 4).union(List.of(1, 4)).toList());
    assertEquals(List.of(1, 2, null, 4, 3),
        Iterator.of(1, 2, null, 4).union(Iterator.of(1, 3, 4)).toList());
    assertEquals(List.of(1, 2, null, 4, 3, 3),
        Iterator.of(1, 2, null, 4).union(List.of(3, 1, 3)).toList());
    assertEquals(List.of(1, 2, null, 4, null),
        Iterator.of(1, 2, null, 4).union(Iterator.of(null, null)).toList());
    assertEquals(List.of(1, null, 2, 4),
        Iterator.of(1, null).union(List.of(1, 2, null, 4)).toList());
    assertEquals(List.of(1, 2, null, 4),
        Iterator.of(1, 2, null, 4).union(Iterator.of(2, 1)).toList());
    assertEquals(List.of(1, null, 2, 4), Iterator.of(1, null).union(List.of(2, 4)).toList());

    assertEquals(List.of(1, 2, null, 4), Iterator.of(1, 2, null, 4).union(Iterator.of()).toList());
    assertEquals(List.of(1, 2, null, 4), Iterator.of().union(Iterator.of(1, 2, null, 4)).toList());
  }

  private <E> void test(@NotNull final java.util.List<E> expected,
      @NotNull final sparx.util.function.Supplier<? extends Iterator<? extends E>> actualSupplier)
      throws Exception {
    assertEquals(expected.isEmpty(), actualSupplier.get().isEmpty());
    assertEquals(!expected.isEmpty(), actualSupplier.get().notEmpty());
    assertEquals(expected.size(), actualSupplier.get().size());
    assertEquals(expected, actualSupplier.get().toList());
    assertThrows(IndexOutOfBoundsException.class, () -> actualSupplier.get().toList().get(-1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> actualSupplier.get().toList().get(expected.size()));
    var itr = actualSupplier.get();
    for (E element : expected) {
      assertTrue(itr.hasNext());
      assertEquals(element, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
    }
    assertFalse(itr.hasNext());
    assertThrows(NoSuchElementException.class, itr::next);

    for (int i = 0; i < expected.size(); i++) {
      itr = actualSupplier.get();
      itr.skip(i + 1);
      for (int j = i + 1; j < expected.size(); j++) {
        assertEquals(expected.get(j), itr.next());
        assertThrows(UnsupportedOperationException.class, itr::remove);
      }
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }

    for (int i = 0; i < expected.size(); i++) {
      itr = actualSupplier.get();
      for (int j = 0; j < i; j++) {
        assertEquals(expected.get(j), itr.next());
      }
      itr.skip(1);
      for (int j = i + 1; j < expected.size(); j++) {
        assertEquals(expected.get(j), itr.next());
        assertThrows(UnsupportedOperationException.class, itr::remove);
      }
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
  }
}
