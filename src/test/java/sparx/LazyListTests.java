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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import sparx.lazy.List;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public class LazyListTests {

  @Test
  public void append() throws Exception {
    test(List.of(1, 2, 3), () -> List.<Integer>of().append(1).append(2).append(3));
    test(List.of(1, null, 3), () -> List.<Integer>of().append(1).append(null).append(3));
    test(List.of(1, 2, 3), () -> List.of(1).append(2).append(3));
    test(List.of(1, null, 3), () -> List.of(1).append(null).append(3));
    test(List.of(1, 2, 3), () -> List.of(1, 2).append(3));
    test(List.of(1, null, 3), () -> List.of(1, null).append(3));
  }

  @Test
  public void appendAll() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of().appendAll(null));
    test(List.of(1, 2, 3), () -> List.<Integer>of().appendAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), () -> List.<Integer>of().appendAll(List.of(1, null, 3)));
    test(List.of(1, 2, 3), () -> List.of(1).appendAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(1, null, 3), () -> List.of(1).appendAll(List.of(null, 3)));
    test(List.of(1, 2, 3), () -> List.of(1, 2).appendAll(Set.of(3)));
    test(List.of(1, null, 3), () -> List.of(1, null).appendAll(Set.of(3)));
    test(List.of(1, null), () -> List.of(1, null).appendAll(List.of()));
    test(List.of(1, null), () -> List.of(1, null).appendAll(Set.of()));
  }

  @Test
  public void count() throws Exception {
    test(List.of(0), () -> List.of().count());
    test(List.of(3), () -> List.of(1, 2, 3).count());
    test(List.of(3), () -> List.of(1, null, 3).count());
  }

  @Test
  public void countWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).countWhere((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).countWhere((IndexedPredicate<? super Object>) null));
    test(List.of(0), () -> List.of().countWhere(Objects::nonNull));
    test(List.of(2), () -> List.of(1, 2, 3).countWhere(i -> i < 3));
    test(List.of(3), () -> List.of(1, 2, 3).countWhere(i -> i > 0));
    var l = List.of(1, null, 3).countWhere(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 2, 1).countWhere((n, i) -> {
      indexes.add(n);
      return i < 2;
    }).first();
    assertEquals(List.of(0, 1, 2, 3), indexes);
  }

  @Test
  public void diff() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).diff(null));
    test(List.of(2, 4), () -> List.of(1, 2, null, 4).diff(List.of(1, null)));
    test(List.of(2, null), () -> List.of(1, 2, null, 4).diff(List.of(1, 4)));
    test(List.of(2, null), () -> List.of(1, 2, null, 4).diff(List.of(1, 3, 4)));
    test(List.of(2, null, 4), () -> List.of(1, 2, null, 4).diff(List.of(3, 1, 3)));
    test(List.of(1, 2, 4), () -> List.of(1, 2, null, 4).diff(List.of(null, null)));
    test(List.of(1, 2, 4), () -> List.of(1, 1, 2, null, 4).diff(List.of(null, null, 1)));
    test(List.of(), () -> List.of(1, null).diff(List.of(1, 2, null, 4)));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4).diff(List.of()));
    test(List.of(1, 1, 2, null, 4), () -> List.of(1, 1, 2, null, 4).diff(List.of()));
    test(List.of(), () -> List.of().diff(List.of(1, 2, null, 4)));
  }

  @Test
  public void doFor() {
    assertThrows(NullPointerException.class,
        () -> List.of(0).doFor((Consumer<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).doFor((IndexedConsumer<? super Object>) null));
    var list = new ArrayList<>();
    List.of(1, 2, 3).doFor(e -> list.add(e));
    assertEquals(List.of(1, 2, 3), list);
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 2, 1).doFor((n, i) -> indexes.add(n));
    assertEquals(List.of(0, 1, 2, 3), indexes);
  }

  @Test
  public void doWhile() {
    assertThrows(NullPointerException.class,
        () -> List.of(0).doWhile((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).doWhile((IndexedPredicate<? super Object>) null));
    assertThrows(NullPointerException.class, () -> List.of(0).doWhile(null, i -> {
    }));
    assertThrows(NullPointerException.class, () -> List.of(0).doWhile(i -> true, null));
    assertThrows(NullPointerException.class, () -> List.of(0).doWhile(null, (n, i) -> {
    }));
    assertThrows(NullPointerException.class, () -> List.of(0).doWhile((n, i) -> true, null));
    var list = new ArrayList<>();
    List.of(1, 2, 3).doWhile(e -> e < 3, list::add);
    assertEquals(List.of(1, 2), list);
    list.clear();
    List.of(1, 2, 3).doWhile(e -> {
      list.add(e);
      return e < 2;
    });
    assertEquals(List.of(1, 2), list);
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).doWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    });
    assertEquals(List.of(0, 1, 2), indexes);
    indexes.clear();
    List.of(1, 2, 3, 4).doWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }, (n, i) -> indexes.add(n));
    assertEquals(List.of(0, 0, 1, 1, 2), indexes);
  }

  @Test
  public void drop() throws Exception {
    test(List.of(), () -> List.<Integer>of().drop(1));
    test(List.of(), () -> List.<Integer>of().drop(0));
    test(List.of(), () -> List.<Integer>of().drop(-1));
    test(List.of(null, 3), () -> List.of(1, null, 3).drop(1));
    test(List.of(3), () -> List.of(1, null, 3).drop(2));
    test(List.of(), () -> List.of(1, null, 3).drop(3));
    test(List.of(), () -> List.of(1, null, 3).drop(4));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).drop(0));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).drop(-1));
  }

  @Test
  public void dropRight() throws Exception {
    test(List.of(), () -> List.<Integer>of().dropRight(1));
    test(List.of(), () -> List.<Integer>of().dropRight(0));
    test(List.of(), () -> List.<Integer>of().dropRight(-1));
    test(List.of(1, null), () -> List.of(1, null, 3).dropRight(1));
    test(List.of(1), () -> List.of(1, null, 3).dropRight(2));
    test(List.of(), () -> List.of(1, null, 3).dropRight(3));
    test(List.of(), () -> List.of(1, null, 3).dropRight(4));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).dropRight(0));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).dropRight(-1));
  }

  @Test
  public void dropRightWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).dropRightWhile((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).dropRightWhile((IndexedPredicate<? super Object>) null));
    test(List.of(), () -> List.<Integer>of().dropRightWhile(e -> e > 0));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).dropRightWhile(Objects::isNull));
    test(List.of(1, null), () -> List.of(1, null, 3).dropRightWhile(Objects::nonNull));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).dropRightWhile(e -> e < 1));
    test(List.of(), () -> List.of(1, 2, 3).dropRightWhile(e -> e > 0));
    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).dropRightWhile(e -> e > 0).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).dropRightWhile((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 1), indexes);
  }

  @Test
  public void dropWhile() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).dropWhile((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).dropWhile((IndexedPredicate<? super Object>) null));
    test(List.of(), () -> List.<Integer>of().dropWhile(e -> e > 0));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).dropWhile(Objects::isNull));
    test(List.of(null, 3), () -> List.of(1, null, 3).dropWhile(Objects::nonNull));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).dropWhile(e -> e < 1));
    test(List.of(), () -> List.of(1, 2, 3).dropWhile(e -> e > 0));
    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).dropWhile(e -> e > 0).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).dropWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);
  }

  @Test
  public void each() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).each((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).each((IndexedPredicate<? super Object>) null));
    test(List.of(false), () -> List.of().each(Objects::nonNull));
    test(List.of(false), () -> List.of(1, 2, 3).each(i -> i > 3));
    test(List.of(false), () -> List.of(1, 2, 3).each(i -> i < 3));
    test(List.of(true), () -> List.of(1, 2, 3).each(i -> i > 0));
    test(List.of(true), () -> List.of(1, 2, 3).each(i -> i > 0));
    var l = List.of(1, null, 3).each(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).each((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);
  }

  @Test
  public void endsWith() throws Exception {
    test(List.of(true), () -> List.<Integer>of().endsWith(List.of()));
    test(List.of(false), () -> List.<Integer>of().endsWith(List.of(1)));
    test(List.of(true), () -> List.of(1, null, 3).endsWith(List.of()));
    test(List.of(true), () -> List.of(1, null, 3).endsWith(List.of(3)));
    test(List.of(false), () -> List.of(1, null, 3).endsWith(List.of(null)));
    test(List.of(true), () -> List.of(1, null, 3).endsWith(List.of(null, 3)));
    test(List.of(false), () -> List.of(1, null, 3).endsWith(List.of(1, null)));
    test(List.of(true), () -> List.of(1, null, 3).endsWith(List.of(1, null, 3)));
    test(List.of(false), () -> List.of(1, null, 3).endsWith(List.of(null, null, 3)));
  }

  @Test
  public void exists() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).exists((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).exists((IndexedPredicate<? super Object>) null));
    test(List.of(false), () -> List.of().exists(Objects::nonNull));
    test(List.of(false), () -> List.of(1, 2, 3).exists(i -> i > 3));
    test(List.of(false), () -> List.of(1, 2, 3).exists(i -> i > 3));
    test(List.of(true), () -> List.of(1, 2, 3).exists(i -> i > 0));
    test(List.of(true), () -> List.of(1, 2, 3).exists(i -> i > 0));
    var l = List.of(1, null, 3).exists(i -> i > 1);
    assertThrows(NullPointerException.class, l::first);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).exists((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);
  }

  @Test
  public void filter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).filter((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).filter((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4);
    test(List.of(null), () -> l.filter(Objects::isNull));
    test(List.of(1, 2, 4), () -> l.filter(Objects::nonNull));
    test(List.of(1, 2), () -> l.filter(Objects::nonNull).filter(i -> i < 3));
    test(List.of(4), () -> l.filter(Objects::nonNull).filter(i -> i > 3));
    test(List.of(), () -> l.filter(Objects::nonNull).filter(i -> i > 4));
    test(List.of(), () -> List.of().filter(Objects::isNull));
    assertThrows(NullPointerException.class, () -> l.filter(i -> i > 4).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).filter((n, i) -> {
      indexes.add(n);
      return i > 2;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
  }

  @Test
  public void findAny() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).findAny((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).findAny((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4);
    test(List.of(null), () -> l.findAny(Objects::isNull));
    test(List.of(1), () -> l.findAny(i -> i < 4));
    test(List.of(), () -> List.of().findAny(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).findAny((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);
  }

  @Test
  public void findIndexOf() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l.findIndexOf(null));
    test(List.of(3), () -> l.findIndexOf(4));
    test(List.of(), () -> l.findIndexOf(3));
    test(List.of(), () -> List.of().findIndexOf(null));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).findAny((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);
  }

  @Test
  public void findIndexOfSlice() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).findIndexOfSlice(null));
    var l = List.of(1, 2, null, 4);
    test(List.of(1), () -> l.findIndexOfSlice(List.of(2, null)));
    test(List.of(2), () -> l.findIndexOfSlice(List.of(null)));
    test(List.of(), () -> l.findIndexOfSlice(List.of(null, 2)));
    test(List.of(0), () -> l.findIndexOfSlice(List.of()));
    test(List.of(2), () -> List.of(1, 1, 1, 1, 2, 1).findIndexOfSlice(List.of(1, 1, 2)));
    test(List.of(), () -> List.of().findIndexOfSlice(List.of(null)));
    test(List.of(0), () -> List.of().findIndexOfSlice(List.of()));
  }

  @Test
  public void findIndexWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).findIndexWhere((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).findIndexWhere((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l.findIndexWhere(Objects::isNull));
    test(List.of(1), () -> l.findIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class, () -> l.findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class, () -> l.findIndexWhere(i -> i > 3).first());
    test(List.of(), () -> List.of().findIndexWhere(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).findIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(0, 1, 2), indexes);
  }

  @Test
  public void findLast() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).findLast((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).findLast((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4, 5);
    test(List.of(null), () -> l.findLast(Objects::isNull));
    assertThrows(NullPointerException.class, () -> l.findLast(i -> i < 4).first());
    test(List.of(4), () -> l.findLast(i -> i < 5));
    test(List.of(), () -> l.findLast(i -> i != null && i > 5));
    test(List.of(), () -> List.of().findLast(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).findLast((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(3, 2), indexes);
  }

  @Test
  public void findLastIndexOf() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l.findLastIndexOf(null));
    test(List.of(3), () -> l.findLastIndexOf(4));
    test(List.of(), () -> l.findLastIndexOf(3));
    test(List.of(), () -> List.of().findLastIndexOf(null));
  }

  @Test
  public void findLastIndexOfSlice() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).findLastIndexOfSlice(null));
    var l = List.of(1, 2, null, 4);
    test(List.of(1), () -> l.findLastIndexOfSlice(List.of(2, null)));
    test(List.of(2), () -> l.findLastIndexOfSlice(List.of(null)));
    test(List.of(), () -> l.findLastIndexOfSlice(List.of(null, 2)));
    test(List.of(4), () -> l.findLastIndexOfSlice(List.of()));
    test(List.of(2), () -> List.of(1, 1, 1, 1, 2, 1).findLastIndexOfSlice(List.of(1, 1, 2)));
    test(List.of(), () -> List.of().findLastIndexOfSlice(List.of(null)));
    test(List.of(0), () -> List.of().findLastIndexOfSlice(List.of()));
  }

  @Test
  public void findLastIndexWhere() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).findLastIndexWhere((Predicate<? super Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).findLastIndexWhere((IndexedPredicate<? super Object>) null));
    var l = List.of(1, 2, null, 4);
    test(List.of(2), () -> l.findLastIndexWhere(Objects::isNull));
    test(List.of(3), () -> l.findLastIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class, () -> l.findLastIndexWhere(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class, () -> l.findLastIndexWhere(i -> i < 3).first());
    test(List.of(), () -> List.of().findLastIndexWhere(Objects::isNull));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).findLastIndexWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).first();
    assertEquals(List.of(3, 2), indexes);
  }

  @Test
  public void flatMap() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).flatMap((Function<? super Integer, ? extends Iterable<Object>>) null));
    assertThrows(NullPointerException.class, () -> List.of(0)
        .flatMap((IndexedFunction<? super Integer, ? extends Iterable<Object>>) null));
    var l = List.of(1, 2);
    test(List.of(1, 1, 2, 2), () -> l.flatMap(i -> List.of(i, i)));
    test(List.of(), () -> l.flatMap(i -> List.of()));
    test(List.of(null, null), () -> l.flatMap(i -> List.of(null)));
    assertNull(l.flatMap(i -> List.of(null)).get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.flatMap(i -> List.of(null)).get(2));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).flatMap((n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
  }

  @Test
  public void flatMapAfter() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0)
        .flatMapAfter(0, (Function<? super Integer, ? extends Iterable<Integer>>) null));
    assertThrows(NullPointerException.class, () -> List.of(0)
        .flatMapAfter(0, (IndexedFunction<? super Integer, ? extends Iterable<Integer>>) null));
    var l = List.of(1, 2);
    test(l, () -> l.flatMapAfter(-1, i -> List.of(i, i)));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapAfter(2, i -> List.of(i, i)).get(2));
    test(List.of(1, 1, 2), () -> l.flatMapAfter(0, i -> List.of(i, i)));
    test(List.of(1, 2, 2), () -> l.flatMapAfter(1, i -> List.of(i, i)));
    test(List.of(1, 2), () -> l.flatMapAfter(2, i -> List.of(i, i)));
    test(List.of(2), () -> l.flatMapAfter(0, i -> List.of()));
    test(List.of(1), () -> l.flatMapAfter(1, i -> List.of()));
    test(List.of(1, 2), () -> l.flatMapAfter(2, i -> List.of()));
    test(List.of(), () -> List.of().flatMapAfter(0, i -> List.of(i, i)));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).flatMapAfter(1, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(1), indexes);
  }

  @Test
  public void flatMapFirstWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).flatMapFirstWhere(i -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).flatMapFirstWhere(null, i -> List.of(i)));
    assertThrows(NullPointerException.class,
        () -> List.of(0).flatMapFirstWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).flatMapFirstWhere(null, (n, i) -> List.of(i)));
    var l = List.of(1, 2, null, 4);
    test(l, () -> l.flatMapFirstWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 1, 2, null, 4), () -> l.flatMapFirstWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 2, 3, 4), () -> l.flatMapFirstWhere(Objects::isNull, i -> List.of(3)));
    test(l, () -> l.flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(2, null, 4), () -> l.flatMapFirstWhere(i -> true, i -> List.of()));
    test(List.of(1, 2, 4), () -> l.flatMapFirstWhere(Objects::isNull, i -> List.of()));
    test(List.of(1, 1, 2, null, 4), () -> l.flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)));

    assertFalse(l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(0));
    assertEquals(2, l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(1));
    assertThrows(NullPointerException.class,
        () -> l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(2));

    test(List.of(), () -> List.of().flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(), () -> List.of().flatMapFirstWhere(i -> true, i -> List.of()));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).flatMapFirstWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2), indexes);
  }

  @Test
  public void flatMapLastWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).flatMapLastWhere(i -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).flatMapLastWhere(null, i -> List.of(i)));
    assertThrows(NullPointerException.class,
        () -> List.of(0).flatMapLastWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).flatMapLastWhere(null, (n, i) -> List.of(i)));
    var l = List.of(1, 2, null, 4);
    test(l, () -> l.flatMapLastWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 2, null, 4, 4), () -> l.flatMapLastWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 2, 3, 4), () -> l.flatMapLastWhere(Objects::isNull, i -> List.of(3)));
    test(l, () -> l.flatMapLastWhere(i -> false, i -> List.of()));
    test(List.of(1, 2, null), () -> l.flatMapLastWhere(i -> true, i -> List.of()));
    test(List.of(1, 2, 4), () -> l.flatMapLastWhere(Objects::isNull, i -> List.of()));
    test(List.of(1, 2, null, 4, 4), () -> l.flatMapLastWhere(i -> i == 4, i -> List.of(i, i)));

    assertFalse(l.flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> l.flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).get(3));
    assertThrows(NullPointerException.class,
        () -> l.flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).get(2));

    test(List.of(), () -> List.of().flatMapFirstWhere(i -> false, i -> List.of()));
    test(List.of(), () -> List.of().flatMapFirstWhere(i -> true, i -> List.of()));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).flatMapLastWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 2), indexes);
  }

  @Test
  public void flatMapWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).flatMapWhere(i -> false, null));
    assertThrows(NullPointerException.class, () -> List.of(0).flatMapWhere(null, i -> List.of(i)));
    assertThrows(NullPointerException.class, () -> List.of(0).flatMapWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).flatMapWhere(null, (n, i) -> List.of(i)));
    var l = List.of(1, null, null, 4);
    test(l, () -> l.flatMapWhere(i -> false, i -> List.of(i, i)));
    test(List.of(1, 1, null, null, null, null, 4, 4),
        () -> l.flatMapWhere(i -> true, i -> List.of(i, i)));
    test(List.of(1, 3, 3, 4), () -> l.flatMapWhere(Objects::isNull, i -> List.of(3)));
    test(l, () -> l.flatMapWhere(i -> false, i -> List.of()));
    test(List.of(), () -> l.flatMapWhere(i -> true, i -> List.of()));
    test(List.of(1, 4), () -> l.flatMapWhere(Objects::isNull, i -> List.of()));

    assertFalse(l.flatMapWhere(i -> i == 1, i -> List.of(i, i)).isEmpty());
    assertEquals(1, l.flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(0));
    assertEquals(1, l.flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(1));
    assertThrows(NullPointerException.class,
        () -> l.flatMapWhere(i -> i == 1, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> l.flatMapWhere(i -> i == 1, i -> List.of(i, i)).get(2));
    assertFalse(l.flatMapWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.flatMapWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, l.flatMapWhere(i -> i > 2, i -> List.of(i, i)).get(0));
    assertThrows(NullPointerException.class,
        () -> l.flatMapWhere(i -> i > 2, i -> List.of(i, i)).get(1));

    test(List.of(), () -> List.of().flatMapWhere(i -> false, i -> List.of()));
    test(List.of(), () -> List.of().flatMapWhere(i -> true, i -> List.of()));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).flatMapWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return List.of(i);
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2, 3), indexes);
  }

  @Test
  public void foldLeft() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).foldLeft(1, null));
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(16), () -> l.foldLeft(1, Integer::sum));
    test(List.of(List.of(1, 2)), () -> List.of(1, 2).foldLeft(List.<Integer>of(), List::append));
    test(List.of(1), () -> List.<Integer>of().foldLeft(1, Integer::sum));
    test(List.of(List.of()), () -> List.of().foldLeft(List.of(), List::append));
  }

  @Test
  public void foldRight() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).foldRight(1, null));
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(16), () -> l.foldRight(1, Integer::sum));
    test(List.of(List.of(2, 1)),
        () -> List.of(1, 2).foldRight(List.<Integer>of(), (i, li) -> li.append(i)));
    test(List.of(1), () -> List.<Integer>of().foldRight(1, Integer::sum));
    test(List.of(List.of()), () -> List.of().foldRight(List.of(), (i, li) -> li.append(i)));
  }

  @Test
  public void group() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> List.of(0).group(-1));
    assertThrows(IllegalArgumentException.class, () -> List.of(0).group(0));
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5)), () -> l.group(1));
    test(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), () -> l.group(2));
    test(List.of(List.of(1, 2, 3), List.of(4, 5)), () -> l.group(3));
    test(List.of(List.of(1, 2, 3, 4, 5)), () -> l.group(10));
  }

  @Test
  public void groupWithPadding() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> List.of(0).groupWithPadding(-1, 0));
    assertThrows(IllegalArgumentException.class, () -> List.of(0).groupWithPadding(0, 0));
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5)),
        () -> l.groupWithPadding(1, null));
    test(List.of(List.of(1, 2), List.of(3, 4), List.of(5, null)),
        () -> l.groupWithPadding(2, null));
    test(List.of(List.of(1, 2, 3), List.of(4, 5, -1)), () -> l.groupWithPadding(3, -1));
    test(List.of(List.of(1, 2, 3, 4, 5, -1, -1, -1, -1, -1)), () -> l.groupWithPadding(10, -1));
  }

  @Test
  public void includes() throws Exception {
    var l = List.of(1, 2, 3, null, 5);
    test(List.of(true), () -> l.includes(null));
    test(List.of(false), () -> l.includes(0));
    test(List.of(false), () -> List.of().includes(0));
    test(List.of(false), () -> List.of().includes(null));
  }

  @Test
  public void includesAll() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).includesAll(null));
    var l = List.of(1, 2, 3, null, 5);
    test(List.of(true), () -> l.includesAll(List.of(null, 1)));
    test(List.of(false), () -> l.includesAll(List.of(0, 1)));
    test(List.of(true), () -> l.includesAll(List.of()));
    test(List.of(false), () -> List.of().includesAll(List.of(null, 1)));
    test(List.of(true), () -> List.of().includesAll(List.of()));
  }

  @Test
  public void includesSlice() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).includesSlice(null));
    var l = List.of(1, 2, 3, null, 5);
    test(List.of(true), () -> l.includesSlice(List.of(3, null)));
    test(List.of(false), () -> l.includesSlice(List.of(null, 3)));
    test(List.of(true), () -> l.includesSlice(List.of()));
    test(List.of(false), () -> List.of().includesSlice(List.of(null, 1)));
    test(List.of(true), () -> List.of().includesSlice(List.of()));
  }

  @Test
  public void insertAfter() throws Exception {
    var l = List.of(1, 2, 3);
    test(l, () -> l.insertAfter(5, null));
    test(List.of(1, 2, 3, null), () -> l.insertAfter(3, null));
    test(List.of(1, 2, null, 3), () -> l.insertAfter(2, null));
    test(List.of(1, null, 2, 3), () -> l.insertAfter(1, null));
    test(List.of(null, 1, 2, 3), () -> l.insertAfter(0, null));
    test(l, () -> l.insertAfter(-7, null));
    test(List.of(), () -> List.of().insertAfter(5, null));
    test(List.of(null), () -> List.of().insertAfter(0, null));
    Iterable<Object> iterable = () -> List.of().iterator();
    test(List.of(), () -> List.wrap(iterable).insertAfter(5, null));
    test(List.of(null), () -> List.wrap(iterable).insertAfter(0, null));
  }

  @Test
  public void insertAllAfter() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).insertAllAfter(0, null));
    var l = List.of(1, 2, 3);
    test(l, () -> l.insertAllAfter(5, List.of(null, 5)));
    test(List.of(1, 2, 3, null, 5), () -> l.insertAllAfter(3, List.of(null, 5)));
    test(List.of(1, 2, null, 5, 3), () -> l.insertAllAfter(2, List.of(null, 5)));
    test(List.of(1, null, 5, 2, 3), () -> l.insertAllAfter(1, List.of(null, 5)));
    test(List.of(null, 5, 1, 2, 3), () -> l.insertAllAfter(0, List.of(null, 5)));
    test(l, () -> l.insertAllAfter(-7, List.of(null, 5)));
    test(List.of(), () -> List.of().insertAllAfter(5, List.of(null, 5)));
    test(List.of(null, 5), () -> List.of().insertAllAfter(0, List.of(null, 5)));
    Iterable<Object> iterable = () -> List.of().iterator();
    test(List.of(), () -> List.wrap(iterable).insertAllAfter(5, List.of(null, 5)));
    test(List.of(null, 5), () -> List.wrap(iterable).insertAllAfter(0, List.of(null, 5)));
  }

  @Test
  public void intersect() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).intersect(null));
    test(List.of(1, null), () -> List.of(1, 2, null, 4).intersect(List.of(1, null)));
    test(List.of(1, 4), () -> List.of(1, 2, null, 4).intersect(List.of(1, 4)));
    test(List.of(1, 4), () -> List.of(1, 2, null, 4).intersect(List.of(1, 3, 4)));
    test(List.of(1), () -> List.of(1, 2, null, 4).intersect(List.of(3, 1, 3)));
    test(List.of(null), () -> List.of(1, 2, null, 4).intersect(List.of(null, null)));
    test(List.of(1, null), () -> List.of(1, null).intersect(List.of(1, 2, null, 4)));
    test(List.of(1, 2), () -> List.of(1, 2, null, 4).intersect(List.of(2, 1)));
    test(List.of(), () -> List.of(1, null).intersect(List.of(2, 4)));
    test(List.of(), () -> List.of(1, 2, null, 4).intersect(List.of()));
    test(List.of(), () -> List.of().intersect(List.of(1, 2, null, 4)));
  }

  @Test
  public void map() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).map((Function<? super Integer, Object>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).map((IndexedFunction<? super Integer, Object>) null));
    var l = List.of(1, 2, 3);
    test(List.of(2, 3, 4), () -> l.map(x -> x + 1));

    assertFalse(l.append(null).map(x -> x + 1).isEmpty());
    assertEquals(4, l.append(null).map(x -> x + 1).size());
    assertEquals(4, l.append(null).map(x -> x + 1).get(2));
    assertEquals(2, l.append(null).map(x -> x + 1).get(0));
    assertThrows(NullPointerException.class, () -> l.append(null).map(x -> x + 1).get(3));

    test(List.of(), () -> List.<Integer>of().map(x -> x + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).map((n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
  }

  @Test
  public void mapAfter() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).mapAfter(0, (Function<? super Integer, Integer>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).mapAfter(0, (IndexedFunction<? super Integer, Integer>) null));
    var l = List.of(1, 2, 3);
    test(List.of(1, 2, 3), () -> l.mapAfter(-1, x -> x + 1));
    test(List.of(2, 2, 3), () -> l.mapAfter(0, x -> x + 1));
    test(List.of(1, 3, 3), () -> l.mapAfter(1, x -> x + 1));
    test(List.of(1, 2, 4), () -> l.mapAfter(2, x -> x + 1));
    test(List.of(1, 2, 3), () -> l.mapAfter(3, x -> x + 1));
    test(List.of(1, 2, 3, null), () -> l.append(null).mapAfter(-1, x -> x + 1));
    test(List.of(1, 3, 3, null), () -> l.append(null).mapAfter(1, x -> x + 1));

    assertFalse(l.append(null).mapAfter(3, x -> x + 1).isEmpty());
    assertEquals(4, l.append(null).mapAfter(3, x -> x + 1).size());
    assertEquals(2, l.append(null).mapAfter(3, x -> x + 1).get(1));
    assertThrows(NullPointerException.class, () -> l.append(null).mapAfter(3, x -> x + 1).get(3));

    test(List.of(), () -> List.<Integer>of().mapAfter(0, x -> x + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).mapAfter(2, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(2), indexes);
  }

  @Test
  public void mapFirstWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).mapFirstWhere(i -> false, null));
    assertThrows(NullPointerException.class, () -> List.of(0).mapFirstWhere(null, i -> i));
    assertThrows(NullPointerException.class, () -> List.of(0).mapFirstWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class, () -> List.of(0).mapFirstWhere(null, (n, i) -> i));
    var l = List.of(1, 2, null, 4);
    test(l, () -> l.mapFirstWhere(i -> false, i -> i + 1));
    test(List.of(2, 2, null, 4), () -> l.mapFirstWhere(i -> true, i -> i + 1));
    test(List.of(1, 2, 3, 4), () -> l.mapFirstWhere(Objects::isNull, i -> 3));
    test(List.of(2, 2, null, 4), () -> l.mapFirstWhere(i -> i == 1, i -> i + 1));

    assertFalse(l.mapFirstWhere(i -> i > 2, i -> 1).isEmpty());
    assertEquals(4, l.mapFirstWhere(i -> i > 2, i -> 1).size());
    assertEquals(1, l.mapFirstWhere(i -> i > 2, i -> 1).get(0));
    assertEquals(2, l.mapFirstWhere(i -> i > 2, i -> 1).get(1));
    assertThrows(NullPointerException.class, () -> l.mapFirstWhere(i -> i > 2, i -> 1).get(2));

    test(List.of(), () -> List.<Integer>of().mapFirstWhere(i -> false, i -> i + 1));
    test(List.of(), () -> List.<Integer>of().mapFirstWhere(i -> true, i -> i + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).mapFirstWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2), indexes);
  }

  @Test
  public void mapLastWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).mapLastWhere(i -> false, null));
    assertThrows(NullPointerException.class, () -> List.of(0).mapLastWhere(null, i -> i));
    assertThrows(NullPointerException.class, () -> List.of(0).mapLastWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class, () -> List.of(0).mapLastWhere(null, (n, i) -> i));
    var l = List.of(1, 2, null, 4);
    test(l, () -> l.mapLastWhere(i -> false, i -> i + 1));
    test(List.of(1, 2, null, 5), () -> l.mapLastWhere(i -> true, i -> i + 1));
    test(List.of(1, 2, 3, 4), () -> l.mapLastWhere(Objects::isNull, i -> 3));
    test(List.of(1, 2, null, 5), () -> l.mapLastWhere(i -> i == 4, i -> i + 1));

    assertFalse(l.mapLastWhere(i -> i < 2, i -> 1).isEmpty());
    assertEquals(4, l.mapLastWhere(i -> i < 2, i -> 1).size());
    assertThrows(NullPointerException.class, () -> l.mapLastWhere(i -> i < 2, i -> 1).get(0));

    test(List.of(), () -> List.<Integer>of().mapLastWhere(i -> false, i -> i + 1));
    test(List.of(), () -> List.<Integer>of().mapLastWhere(i -> true, i -> i + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).mapLastWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2, 2), indexes);
  }

  @Test
  public void mapWhere() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).mapWhere(i -> false, null));
    assertThrows(NullPointerException.class, () -> List.of(0).mapWhere(null, i -> i));
    assertThrows(NullPointerException.class, () -> List.of(0).mapWhere((i, n) -> false, null));
    assertThrows(NullPointerException.class, () -> List.of(0).mapWhere(null, (n, i) -> i));
    var l = List.of(1, 2, 3, 4);
    test(l, () -> l.mapWhere(i -> false, i -> i + 1));
    test(List.of(2, 3, 4, 5), () -> l.mapWhere(i -> true, i -> i + 1));
    test(List.of(1, 3, 3, 4), () -> l.mapWhere(i -> i == 2, i -> 3));

    assertFalse(l.append(null).mapWhere(i -> i == 4, i -> i + 1).isEmpty());
    assertEquals(5, l.append(null).mapWhere(i -> i == 4, i -> i + 1).size());
    assertEquals(2, l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(1));
    assertEquals(3, l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(2));
    assertEquals(5, l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(3));
    assertThrows(NullPointerException.class,
        () -> l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(5));

    test(List.of(), () -> List.<Integer>of().mapWhere(i -> false, i -> i + 1));
    test(List.of(), () -> List.<Integer>of().mapWhere(i -> true, i -> i + 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).mapWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, (n, i) -> {
      indexes.add(n);
      return i;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 2, 3), indexes);
  }

  @Test
  public void max() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).max(null));
    var l = List.of(1, 4, 2, 3);
    test(List.of(4), () -> l.max(Integer::compareTo));

    assertFalse(List.of(1, null).max(Integer::compareTo).isEmpty());
    assertTrue(List.of(1, null).max(Integer::compareTo).notEmpty());
    assertEquals(1, List.of(1, null).max(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> List.of(1, null).max(Integer::compareTo).first());

    test(List.of(), () -> List.<Integer>of().max(Integer::compareTo));
  }

  @Test
  public void min() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).min(null));
    var l = List.of(1, 4, 2, 3);
    test(List.of(1), () -> l.min(Integer::compareTo));

    assertFalse(List.of(1, null).min(Integer::compareTo).isEmpty());
    assertTrue(List.of(1, null).min(Integer::compareTo).notEmpty());
    assertEquals(1, List.of(1, null).min(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> List.of(1, null).min(Integer::compareTo).first());

    test(List.of(), () -> List.<Integer>of().min(Integer::compareTo));
  }

  @Test
  public void none() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).none((Predicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).none((IndexedPredicate<? super Integer>) null));
    test(List.of(true), () -> List.of().none(Objects::nonNull));
    test(List.of(false), () -> List.of(1, 2, 3).none(i -> i < 3));
    test(List.of(true), () -> List.of(1, 2, 3).none(i -> i < 0));
    var l = List.of(1, null, 3).none(i -> i < 0);
    assertThrows(NullPointerException.class, l::first);
    var itr = l.iterator();
    assertTrue(itr.hasNext());
    assertThrows(NullPointerException.class, itr::next);
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).none((n, i) -> {
      indexes.add(n);
      return false;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
  }

  @Test
  public void notAll() throws Exception {
    assertThrows(NullPointerException.class,
        () -> List.of(0).notAll((Predicate<? super Integer>) null));
    assertThrows(NullPointerException.class,
        () -> List.of(0).notAll((IndexedPredicate<? super Integer>) null));
    test(List.of(true), () -> List.of().notAll(Objects::isNull));
    test(List.of(true), () -> List.of(1, 2, 3).notAll(i -> i > 3));
    test(List.of(false), () -> List.of(1, 2, 3).notAll(i -> i > 0));
    var l = List.of(1, null, 3).notAll(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    var itr = l.iterator();
    assertTrue(itr.hasNext());
    assertThrows(NullPointerException.class, itr::next);
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).notAll((n, i) -> {
      indexes.add(n);
      return true;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
  }

  @Test
  public void orElse() throws Exception {
    test(List.of(1), () -> List.of(1).orElse(List.of(2)));
    test(List.of(1), () -> List.of(1).orElse(List.of()));
    test(List.of(2), () -> List.of().orElse(List.of(2)));
    test(List.of(), () -> List.of().orElse(List.of()));
  }

  @Test
  public void orElseGet() throws Exception {
    Supplier<List<Integer>> supplier = () -> List.of(2);
    test(List.of(1), () -> List.of(1).orElseGet(supplier));
    test(List.of(1), () -> List.of(1).orElseGet(List::of));
    test(List.of(2), () -> List.of().orElseGet(supplier));
    test(List.of(), () -> List.of().orElseGet(List::of));

    Supplier<List<Integer>> throwing = () -> {
      throw new IllegalStateException();
    };
    test(List.of(1), () -> List.of(1).orElseGet(throwing));
    assertThrows(IllegalStateException.class, () -> List.of().orElseGet(throwing).isEmpty());
    assertThrows(IllegalStateException.class, () -> List.of().orElseGet(throwing).notEmpty());
    assertThrows(IllegalStateException.class, () -> List.of().orElseGet(throwing).size());
    assertThrows(IllegalStateException.class, () -> List.of().orElseGet(throwing).first());
  }

  @Test
  public void plus() throws Exception {
    test(List.of(1, 2, 3), () -> List.<Integer>of().plus(1).plus(2).plus(3));
    test(List.of(1, null, 3), () -> List.<Integer>of().plus(1).plus(null).plus(3));
    test(List.of(1, 2, 3), () -> List.of(1).plus(2).plus(3));
    test(List.of(1, null, 3), () -> List.of(1).plus(null).plus(3));
    test(List.of(1, 2, 3), () -> List.of(1, 2).plus(3));
    test(List.of(1, null, 3), () -> List.of(1, null).plus(3));
  }

  @Test
  public void plusAll() throws Exception {
    test(List.of(1, 2, 3), () -> List.<Integer>of().plusAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), () -> List.<Integer>of().plusAll(List.of(1, null, 3)));
    test(List.of(1, 2, 3), () -> List.of(1).plusAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(1, null, 3), () -> List.of(1).plusAll(List.of(null, 3)));
    test(List.of(1, 2, 3), () -> List.of(1, 2).plusAll(Set.of(3)));
    test(List.of(1, null, 3), () -> List.of(1, null).plusAll(Set.of(3)));
  }

  @Test
  public void prepend() throws Exception {
    test(List.of(3, 2, 1), () -> List.<Integer>of().prepend(1).prepend(2).prepend(3));
    test(List.of(3, null, 1), () -> List.<Integer>of().prepend(1).prepend(null).prepend(3));
    test(List.of(3, 2, 1), () -> List.of(1).prepend(2).prepend(3));
    test(List.of(3, null, 1), () -> List.of(1).prepend(null).prepend(3));
    test(List.of(3, 1, 2), () -> List.of(1, 2).prepend(3));
    test(List.of(3, 1, null), () -> List.of(1, null).prepend(3));
  }

  @Test
  public void prependAll() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).prependAll(null));
    test(List.of(1, 2, 3), () -> List.<Integer>of().prependAll(Arrays.asList(1, 2, 3)));
    test(List.of(1, null, 3), () -> List.<Integer>of().prependAll(List.of(1, null, 3)));
    test(List.of(2, 3, 1), () -> List.of(1).prependAll(new LinkedHashSet<>(List.of(2, 3))));
    test(List.of(null, 3, 1), () -> List.of(1).prependAll(List.of(null, 3)));
    test(List.of(3, 1, 2), () -> List.of(1, 2).prependAll(Set.of(3)));
    test(List.of(3, 1, null), () -> List.of(1, null).prependAll(Set.of(3)));
  }

  @Test
  public void reduceLeft() throws Exception {
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(15), () -> l.reduceLeft(Integer::sum));
    assertThrows(NullPointerException.class, () -> l.append(null).reduceLeft(Integer::sum).first());
    test(List.of(), () -> List.<Integer>of().reduceLeft(Integer::sum));
  }

  @Test
  public void reduceRight() throws Exception {
    var l = List.of(1, 2, 3, 4, 5);
    test(List.of(15), () -> l.reduceRight(Integer::sum));
    assertThrows(NullPointerException.class,
        () -> l.prepend(null).reduceRight(Integer::sum).first());
    test(List.of(), () -> List.<Integer>of().reduceRight(Integer::sum));
  }

  @Test
  public void removeAfter() throws Exception {
    var l = List.of(1, 2, 3);
    test(List.of(1, 2, 3), () -> l.removeAfter(5));
    test(List.of(1, 2, 3), () -> l.removeAfter(3));
    test(List.of(1, 2), () -> l.removeAfter(2));
    test(List.of(1, 3), () -> l.removeAfter(1));
    test(List.of(2, 3), () -> l.removeAfter(0));
    test(List.of(1, 2, 3), () -> l.removeAfter(-7));
    test(List.of(), () -> List.of().removeAfter(5));
    Iterable<Object> iterable = () -> List.of().iterator();
    test(List.of(), () -> List.wrap(iterable).removeAfter(5));
  }

  @Test
  public void removeEach() throws Exception {
    var l = List.of(1, 2, null, 4, 2);
    test(List.of(2, null, 4, 2), () -> l.removeEach(1));
    test(List.of(1, 2, 4, 2), () -> l.removeEach(null));
    test(List.of(1, null, 4), () -> l.removeEach(2));
    test(l, () -> l.removeEach(0));
    test(List.of(), () -> List.of().removeEach(1));
  }

  @Test
  public void removeFirst() throws Exception {
    var l = List.of(1, 2, null, 4, 2);
    test(List.of(2, null, 4, 2), () -> l.removeFirst(1));
    test(List.of(1, 2, 4, 2), () -> l.removeFirst(null));
    test(List.of(1, null, 4, 2), () -> l.removeFirst(2));
    test(l, () -> l.removeFirst(0));
    test(List.of(), () -> List.of().removeFirst(1));
  }

  @Test
  public void removeFirstWhere() throws Exception {
    var l = List.of(1, 2, null, 4, 2);
    test(List.of(2, null, 4, 2), () -> l.removeFirstWhere(i -> i == 1));
    test(List.of(1, 2, 4, 2), () -> l.removeFirstWhere(Objects::isNull));
    test(List.of(1, null, 4, 2), () -> l.removeFirstWhere(i -> i == 2));
    test(List.of(1, null, 4, 2), () -> l.removeFirstWhere(i -> i > 1));
    test(l, () -> l.removeFirstWhere(i -> false));

    assertThrows(NullPointerException.class, () -> l.removeFirstWhere(i -> i > 2).isEmpty());
    assertThrows(NullPointerException.class, () -> l.removeFirstWhere(i -> i > 2).size());
    assertEquals(2, l.removeFirstWhere(i -> i > 2).get(1));

    test(List.of(), () -> List.<Integer>of().removeFirstWhere(i -> i == 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).removeFirstWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);
  }

  @Test
  public void removeLast() throws Exception {
    var l = List.of(1, 2, null, 4, 2);
    test(List.of(2, null, 4, 2), () -> l.removeLast(1));
    test(List.of(1, 2, 4, 2), () -> l.removeLast(null));
    test(List.of(1, 2, null, 4), () -> l.removeLast(2));
    test(l, () -> l.removeLast(0));
    test(List.of(), () -> List.of().removeLast(1));
  }

  @Test
  public void removeLastWhere() throws Exception {
    var l = List.of(1, 2, null, 4, 2);
    test(List.of(1, 2, 4, 2), () -> l.removeLastWhere(Objects::isNull));
    test(List.of(1, 2, null, 4), () -> l.removeLastWhere(i -> i == 2));
    test(List.of(1, 2, null, 2), () -> l.removeLastWhere(i -> i > 2));
    test(l, () -> l.removeLastWhere(i -> false));

    assertThrows(NullPointerException.class, () -> l.removeLastWhere(i -> i > 4).isEmpty());
    assertThrows(NullPointerException.class, () -> l.removeLastWhere(i -> i > 4).size());
    assertThrows(NullPointerException.class, () -> l.removeLastWhere(i -> i > 4).get(1));

    test(List.of(), () -> List.<Integer>of().removeLastWhere(i -> i == 1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).removeLastWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2), indexes);
  }

  @Test
  public void removeSlice() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(1, 2, null, 4), () -> l.removeSlice(1, 1));
    test(List.of(1, 2, null, 4), () -> l.removeSlice(1, 0));
    test(List.of(1, 2, null, 4), () -> l.removeSlice(1, -3));
    test(List.of(1, 2, null, 4), () -> l.removeSlice(1, -4));
    test(List.of(1, 2, null, 4), () -> l.removeSlice(1, -5));
    test(List.of(1, 2, null, 4), () -> l.removeSlice(-1, 1));
    test(List.of(1, 2, null, 4), () -> l.removeSlice(-1, 3));
    test(List.of(1, 2, null, 4), () -> l.removeSlice(-1, -1));
    test(List.of(1, 2, null, 4), () -> l.removeSlice(-1, -4));
    test(List.of(1, 4), () -> l.removeSlice(1, -1));
    test(List.of(1, null, 4), () -> l.removeSlice(1, -2));
    test(List.of(1, 4), () -> l.removeSlice(1, 3));
    test(List.of(1, null, 4), () -> l.removeSlice(1, 2));
    test(List.of(1, 2, null), () -> l.removeSlice(-1, 4));
    test(List.of(1, 2, 4), () -> l.removeSlice(-2, -1));
    test(List.of(), () -> l.removeSlice(0, Integer.MAX_VALUE));
    test(List.of(), () -> List.of().removeSlice(1, -1));
  }

  @Test
  public void removeWhere() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(1, 2, null, 4), () -> l.removeWhere(i -> false));
    test(List.of(), () -> l.removeWhere(i -> true));
    test(List.of(1, 2, 4), () -> l.removeWhere(Objects::isNull));
    test(List.of(null), () -> l.removeWhere(Objects::nonNull));
    test(List.of(), () -> List.of().removeWhere(i -> false));

    assertFalse(l.removeWhere(i -> i < 2).isEmpty());
    assertThrows(NullPointerException.class, () -> l.removeWhere(i -> i < 2).size());
    assertEquals(2, l.removeWhere(i -> i < 2).get(0));
    assertThrows(NullPointerException.class, () -> l.removeWhere(i -> i < 2).get(1));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).removeWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
  }

  @Test
  public void replaceAfter() throws Exception {
    var l = List.of(1, 2, null);
    test(List.of(1, 2, null), () -> l.replaceAfter(-1, 4));
    test(List.of(4, 2, null), () -> l.replaceAfter(0, 4));
    test(List.of(1, 4, null), () -> l.replaceAfter(1, 4));
    test(List.of(1, 2, 4), () -> l.replaceAfter(2, 4));
    test(List.of(1, 2, null), () -> l.replaceAfter(3, 4));
    test(List.of(), () -> List.<Integer>of().replaceAfter(0, 4));
  }

  @Test
  public void replaceEach() throws Exception {
    var l = List.of(1, 2, null);
    test(List.of(1, 2, null), () -> l.replaceEach(-1, 4));
    test(List.of(1, 2, null), () -> l.replaceEach(0, 4));
    test(List.of(4, 2, null), () -> l.replaceEach(1, 4));
    test(List.of(1, 4, null), () -> l.replaceEach(2, 4));
    test(List.of(1, 2, 4), () -> l.replaceEach(null, 4));
    test(List.of(4, 2, null, 4), () -> l.append(1).replaceEach(1, 4));
    test(List.of(), () -> List.<Integer>of().replaceEach(0, 4));
  }

  @Test
  public void replaceFirst() throws Exception {
    var l = List.of(1, 2, null);
    test(List.of(1, 2, null), () -> l.replaceFirst(-1, 4));
    test(List.of(1, 2, null), () -> l.replaceFirst(0, 4));
    test(List.of(4, 2, null), () -> l.replaceFirst(1, 4));
    test(List.of(1, 4, null), () -> l.replaceFirst(2, 4));
    test(List.of(1, 2, 4), () -> l.replaceFirst(null, 4));
    test(List.of(4, 2, null, 1), () -> l.append(1).replaceFirst(1, 4));
    test(List.of(), () -> List.<Integer>of().replaceFirst(0, 4));
  }

  @Test
  public void replaceFirstWhere() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(l, () -> l.replaceFirstWhere(i -> false, 4));
    test(List.of(4, 2, null, 4), () -> l.replaceFirstWhere(i -> true, 4));
    test(List.of(1, 2, 3, 4), () -> l.replaceFirstWhere(Objects::isNull, 3));
    test(List.of(2, 2, null, 4), () -> l.replaceFirstWhere(i -> i == 1, 2));
    assertEquals(2, l.replaceFirstWhere(i -> i == 1, 2).get(1));
    assertNull(l.replaceFirstWhere(i -> i == 1, 2).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceFirstWhere(i -> i == 1, 2).get(5));

    assertFalse(l.replaceFirstWhere(i -> i > 2, 1).isEmpty());
    assertEquals(4, l.replaceFirstWhere(i -> i > 2, 1).size());
    assertEquals(1, l.replaceFirstWhere(i -> i > 2, 1).get(0));
    assertEquals(2, l.replaceFirstWhere(i -> i > 2, 1).get(1));
    assertThrows(NullPointerException.class, () -> l.replaceFirstWhere(i -> i > 2, 1).get(2));

    test(List.of(), () -> List.<Integer>of().replaceFirstWhere(i -> false, 4));
    test(List.of(), () -> List.<Integer>of().replaceFirstWhere(i -> true, 4));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).replaceFirstWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, 0).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);
  }

  @Test
  public void replaceLast() throws Exception {
    var l = List.of(1, 2, null);
    test(List.of(1, 2, null), () -> l.replaceLast(-1, 4));
    test(List.of(1, 2, null), () -> l.replaceLast(0, 4));
    test(List.of(4, 2, null), () -> l.replaceLast(1, 4));
    test(List.of(1, 4, null), () -> l.replaceLast(2, 4));
    test(List.of(1, 2, 4), () -> l.replaceLast(null, 4));
    test(List.of(1, 2, null, 4), () -> l.append(1).replaceLast(1, 4));
    test(List.of(), () -> List.<Integer>of().replaceLast(0, 4));
  }

  @Test
  public void replaceLastWhere() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(l, () -> l.replaceLastWhere(i -> false, 5));
    test(List.of(1, 2, null, 5), () -> l.replaceLastWhere(i -> true, 5));
    test(List.of(1, 2, 3, 4), () -> l.replaceLastWhere(Objects::isNull, 3));
    test(List.of(1, 2, null, 5), () -> l.replaceLastWhere(i -> i == 4, 5));

    assertFalse(l.replaceLastWhere(i -> i < 2, 1).isEmpty());
    assertEquals(4, l.replaceLastWhere(i -> i < 2, 1).size());
    assertThrows(NullPointerException.class, () -> l.replaceLastWhere(i -> i < 2, 1).get(0));

    test(List.of(), () -> List.<Integer>of().replaceLastWhere(i -> false, 5));
    test(List.of(), () -> List.<Integer>of().replaceLastWhere(i -> true, 5));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).replaceLastWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, 0).doFor(i -> {
    });
    assertEquals(List.of(3, 2), indexes);
  }

  @Test
  public void replaceSlice() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(1, 5, 2, null, 4), () -> l.replaceSlice(1, 1, List.of(5)));
    test(List.of(1, 5, 2, null, 4), () -> l.replaceSlice(1, 0, List.of(5)));
    test(List.of(1, 5, 2, null, 4), () -> l.replaceSlice(1, -3, List.of(5)));
    test(List.of(1, 5, 2, null, 4), () -> l.replaceSlice(1, -4, List.of(5)));
    test(List.of(1, 5, 2, null, 4), () -> l.replaceSlice(1, -5, List.of(5)));
    test(List.of(1, 2, null, 5, 4), () -> l.replaceSlice(-1, 1, List.of(5)));
    test(List.of(1, 2, null, 5, 4), () -> l.replaceSlice(-1, 3, List.of(5)));
    test(List.of(1, 2, null, 5, 4), () -> l.replaceSlice(-1, -1, List.of(5)));
    test(List.of(1, 2, null, 5, 4), () -> l.replaceSlice(-1, -4, List.of(5)));
    test(List.of(1, 5, 4), () -> l.replaceSlice(1, -1, List.of(5)));
    test(List.of(1, 5, null, 4), () -> l.replaceSlice(1, -2, List.of(5)));
    test(List.of(1, 5, 4), () -> l.replaceSlice(1, 3, List.of(5)));
    test(List.of(1, 5, null, 4), () -> l.replaceSlice(1, 2, List.of(5)));
    test(List.of(1, 2, null, 5), () -> l.replaceSlice(-1, 4, List.of(5)));
    test(List.of(1, 2, 5, 4), () -> l.replaceSlice(-2, -1, List.of(5)));
    test(List.of(5), () -> l.replaceSlice(0, Integer.MAX_VALUE, List.of(5)));
    test(List.of(), () -> l.replaceSlice(0, Integer.MAX_VALUE, List.of()));
    test(List.of(5), () -> List.of().replaceSlice(0, 0, List.of(5)));
    test(List.of(), () -> List.of().replaceSlice(1, -1, List.of(5)));
  }

  @Test
  public void replaceWhere() throws Exception {
    var l = List.of(1, 2, 3, 4);
    test(l, () -> l.replaceWhere(i -> false, 5));
    test(List.of(5, 5, 5, 5), () -> l.replaceWhere(i -> true, 5));
    test(List.of(1, 3, 3, 4), () -> l.replaceWhere(i -> i == 2, 3));

    assertFalse(l.append(null).replaceWhere(i -> i == 4, 5).isEmpty());
    assertEquals(5, l.append(null).replaceWhere(i -> i == 4, 5).size());
    assertEquals(2, l.append(null).replaceWhere(i -> i == 4, 5).get(1));
    assertEquals(3, l.append(null).replaceWhere(i -> i == 4, 5).get(2));
    assertEquals(5, l.append(null).replaceWhere(i -> i == 4, 5).get(3));
    assertThrows(NullPointerException.class,
        () -> l.append(null).replaceWhere(i -> i == 4, 5).get(4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.append(null).replaceWhere(i -> i == 4, 5).get(5));

    test(List.of(), () -> List.<Integer>of().replaceWhere(i -> false, 5));
    test(List.of(), () -> List.<Integer>of().replaceWhere(i -> true, 5));
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).replaceWhere((n, i) -> {
      indexes.add(n);
      return i == 3;
    }, 0).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2, 3), indexes);
  }

  @Test
  public void reverse() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(4, null, 2, 1), l::reverse);
    test(l, () -> l.reverse().reverse());
    test(List.of(), () -> List.<Integer>of().reverse());
  }

  @Test
  public void resizeTo() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> List.of(1, 2, null, 4).resizeTo(-1, 5));
    test(List.of(), () -> List.of(1, 2, null, 4).resizeTo(0, 5));
    test(List.of(1), () -> List.of(1, 2, null, 4).resizeTo(1, 5));
    test(List.of(1, 2), () -> List.of(1, 2, null, 4).resizeTo(2, 5));
    test(List.of(1, 2, null), () -> List.of(1, 2, null, 4).resizeTo(3, 5));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4).resizeTo(4, 5));
    test(List.of(1, 2, null, 4, 5), () -> List.of(1, 2, null, 4).resizeTo(5, 5));
    test(List.of(1, 2, null, 4, 5, 5), () -> List.of(1, 2, null, 4).resizeTo(6, 5));
  }

  @Test
  public void slice() throws Exception {
    var l = List.of(1, 2, null, 4);
    test(List.of(), () -> l.slice(1, 1));
    test(List.of(), () -> l.slice(1, 0));
    test(List.of(), () -> l.slice(1, -3));
    test(List.of(), () -> l.slice(1, -4));
    test(List.of(), () -> l.slice(1, -5));
    test(List.of(), () -> l.slice(-1, 1));
    test(List.of(), () -> l.slice(-1, 3));
    test(List.of(), () -> l.slice(-1, -1));
    test(List.of(), () -> l.slice(-1, -4));
    test(List.of(2, null), () -> l.slice(1, -1));
    test(List.of(2), () -> l.slice(1, -2));
    test(List.of(2, null), () -> l.slice(1, 3));
    test(List.of(2), () -> l.slice(1, 2));
    test(List.of(4), () -> l.slice(-1, 4));
    test(List.of(null), () -> l.slice(-2, -1));
    test(List.of(1, 2, null, 4), () -> l.slice(0, Integer.MAX_VALUE));
    test(List.of(), () -> List.of().slice(1, -1));
  }

  @Test
  public void startsWith() throws Exception {
    test(List.of(true), () -> List.<Integer>of().startsWith(List.of()));
    test(List.of(false), () -> List.<Integer>of().startsWith(List.of(1)));
    test(List.of(true), () -> List.of(1, null, 3).startsWith(List.of()));
    test(List.of(true), () -> List.of(1, null, 3).startsWith(List.of(1)));
    test(List.of(false), () -> List.of(1, null, 3).startsWith(List.of(null)));
    test(List.of(true), () -> List.of(1, null, 3).startsWith(List.of(1, null)));
    test(List.of(false), () -> List.of(1, null, 3).startsWith(List.of(null, 3)));
    test(List.of(true), () -> List.of(1, null, 3).startsWith(List.of(1, null, 3)));
    test(List.of(false), () -> List.of(1, null, 3).startsWith(List.of(null, null, 3)));
  }

  @Test
  public void sorted() throws Exception {
    var l = List.of(1, 2, 3, 2, 1);
    test(List.of(1, 1, 2, 2, 3), () -> l.sorted(Integer::compare));
    test(List.of(), () -> List.<Integer>of().sorted(Integer::compare));
  }

  @Test
  public void take() throws Exception {
    test(List.of(), () -> List.<Integer>of().take(1));
    test(List.of(), () -> List.<Integer>of().take(0));
    test(List.of(), () -> List.<Integer>of().take(-1));
    test(List.of(1), () -> List.of(1, null, 3).take(1));
    test(List.of(1, null), () -> List.of(1, null, 3).take(2));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).take(3));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).take(4));
    test(List.of(), () -> List.of(1, null, 3).take(0));
    test(List.of(), () -> List.of(1, null, 3).take(-1));
  }

  @Test
  public void takeRight() throws Exception {
    test(List.of(), () -> List.<Integer>of().takeRight(1));
    test(List.of(), () -> List.<Integer>of().takeRight(0));
    test(List.of(), () -> List.<Integer>of().takeRight(-1));
    test(List.of(3), () -> List.of(1, null, 3).takeRight(1));
    test(List.of(null, 3), () -> List.of(1, null, 3).takeRight(2));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).takeRight(3));
    test(List.of(1, null, 3), () -> List.of(1, null, 3).takeRight(4));
    test(List.of(), () -> List.of(1, null, 3).takeRight(0));
    test(List.of(), () -> List.of(1, null, 3).takeRight(-1));
  }

  @Test
  public void takeRightWhile() throws Exception {
    test(List.of(), () -> List.<Integer>of().takeRightWhile(e -> e > 0));
    test(List.of(), () -> List.of(1, null, 3).takeRightWhile(Objects::isNull));
    test(List.of(3), () -> List.of(1, null, 3).takeRightWhile(Objects::nonNull));
    test(List.of(), () -> List.of(1, null, 3).takeRightWhile(e -> e < 1));
    test(List.of(1, 2, 3), () -> List.of(1, 2, 3).takeRightWhile(e -> e > 0));

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).takeRightWhile(e -> e > 0).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).takeRightWhile((n, i) -> {
      indexes.add(n);
      return i > 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(3, 2), indexes);
  }

  @Test
  public void takeWhile() throws Exception {
    test(List.of(), () -> List.<Integer>of().takeWhile(e -> e > 0));
    test(List.of(), () -> List.of(1, null, 3).takeWhile(Objects::isNull));
    test(List.of(1), () -> List.of(1, null, 3).takeWhile(Objects::nonNull));
    test(List.of(), () -> List.of(1, null, 3).takeWhile(e -> e < 1));
    test(List.of(1, 2, 3), () -> List.of(1, 2, 3).takeWhile(e -> e > 0));

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).takeWhile(e -> e > 0).size());
    var indexes = new ArrayList<Integer>();
    List.of(1, 2, 3, 4).takeWhile((n, i) -> {
      indexes.add(n);
      return i < 3;
    }).doFor(i -> {
    });
    assertEquals(List.of(0, 1, 2), indexes);
  }

  @Test
  public void union() throws Exception {
    assertThrows(NullPointerException.class, () -> List.of(0).union(null));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4).union(List.of(1, null)));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4).union(List.of(1, 4)));
    test(List.of(1, 2, null, 4, 3), () -> List.of(1, 2, null, 4).union(List.of(1, 3, 4)));
    test(List.of(1, 2, null, 4, 3, 3), () -> List.of(1, 2, null, 4).union(List.of(3, 1, 3)));
    test(List.of(1, 2, null, 4, null), () -> List.of(1, 2, null, 4).union(List.of(null, null)));
    test(List.of(1, null, 2, 4), () -> List.of(1, null).union(List.of(1, 2, null, 4)));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4).union(List.of(2, 1)));
    test(List.of(1, null, 2, 4), () -> List.of(1, null).union(List.of(2, 4)));
    test(List.of(1, 2, null, 4), () -> List.of(1, 2, null, 4).union(List.of()));
    test(List.of(1, 2, null, 4), () -> List.of().union(List.of(1, 2, null, 4)));
  }

  @Test
  public void testLazy() throws Exception {
    var l = List.of(1, 2, null, 4, 2, null, 1);
    assertEquals(List.of(1, 4, 1), removeSlices(List.of(2, null)).apply(l));
  }

  private static Function<List<Integer>, List<Integer>> removeSlices(final List<Integer> slice) {
    return new Function<>() {
      @Override
      public List<Integer> apply(final List<Integer> input) {
        return input.findIndexOfSlice(slice)
            .flatMap(i -> input.take(i).appendAll(apply(input.drop(i + slice.size()))))
            .orElse(input);
      }
    };
  }

  private <E> void test(@NotNull final java.util.List<E> expected,
      @NotNull final Supplier<? extends List<? extends E>> actualSupplier) throws Exception {
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
