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

@SuppressWarnings("DataFlowIssue")
public class LazyIteratorTests {

  @Test
  public void append() {
    var itr = Iterator.<Integer>of().append(1).append(2).append(3);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, 2, 3), itr.toList());

    itr = Iterator.<Integer>of().append(1).append(null).append(3);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());

    itr = Iterator.of(1).append(2).append(3);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, 2, 3), itr.toList());

    itr = Iterator.of(1).append(null).append(3);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());

    itr = Iterator.of(1, 2).append(3);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, 2, 3), itr.toList());

    itr = Iterator.of(1, null).append(3);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());
  }

  @Test
  public void appendAll() {
    var itr = Iterator.<Integer>of().appendAll(Arrays.asList(1, 2, 3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, 2, 3), itr.toList());

    itr = Iterator.<Integer>of().appendAll(List.of(1, null, 3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());

    itr = Iterator.<Integer>of().appendAll(Iterator.of(1, null, 3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());

    itr = Iterator.of(1).appendAll(new LinkedHashSet<>(List.of(2, 3)));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, 2, 3), itr.toList());

    itr = Iterator.of(1).appendAll(List.of(null, 3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());

    itr = Iterator.of(1).appendAll(Iterator.of(null, 3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());

    itr = Iterator.of(1, 2).appendAll(Set.of(3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, 2, 3), itr.toList());

    itr = Iterator.of(1, null).appendAll(Set.of(3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());

    itr = Iterator.of(1, null).appendAll(Iterator.of(3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());
  }

  @Test
  public void count() {
    assertFalse(Iterator.of().count().isEmpty());
    assertTrue(Iterator.of().count().notEmpty());
    assertEquals(1, Iterator.of().count().size());
    assertEquals(0, Iterator.of().count().first());
    assertEquals(3, Iterator.of(1, 2, 3).count().first());
    assertEquals(3, Iterator.of(1, null, 3).count().first());
  }

  @Test
  public void countWhere() {
    assertFalse(Iterator.of().countWhere(Objects::nonNull).isEmpty());
    assertTrue(Iterator.of().countWhere(Objects::nonNull).notEmpty());
    assertEquals(1, Iterator.of().countWhere(Objects::nonNull).size());
    assertEquals(0, Iterator.of().countWhere(Objects::nonNull).first());
    assertEquals(2, Iterator.of(1, 2, 3).countWhere(i -> i < 3).first());
    assertEquals(3, Iterator.of(1, 2, 3).countWhere(i -> i > 0).first());
    var itr = Iterator.of(1, null, 3).countWhere(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void diff() {
    assertEquals(List.of(2, 4), Iterator.of(1, 2, null, 4).diff(List.of(1, null)).toList());
    assertEquals(List.of(2, null), Iterator.of(1, 2, null, 4).diff(Iterator.of(1, 4)).toList());
    assertEquals(List.of(2, null), Iterator.of(1, 2, null, 4).diff(List.of(1, 3, 4)).toList());
    assertEquals(List.of(2, null, 4),
        Iterator.of(1, 2, null, 4).diff(Iterator.of(3, 1, 3)).toList());
    assertEquals(List.of(1, 2, 4), Iterator.of(1, 2, null, 4).diff(List.of(null, null)).toList());
    assertEquals(List.of(), Iterator.of(1, null).diff(Iterator.of(1, 2, null, 4)).toList());

    assertEquals(List.of(1, 2, null, 4), Iterator.of(1, 2, null, 4).diff(Iterator.of()).toList());
    assertEquals(List.of(), Iterator.of().diff(Iterator.of(1, 2, null, 4)).toList());
  }

  @Test
  public void doFor() {
    var list = new ArrayList<>();
    Iterator.of(1, 2, 3).doFor(e -> list.add(e));
    assertEquals(List.of(1, 2, 3), list);
  }

  @Test
  public void doWhile() {
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
  public void drop() {
    var itr = Iterator.<Integer>of().drop(1);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(0, itr.size());
    itr = Iterator.<Integer>of().drop(0);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(0, itr.size());
    itr = Iterator.<Integer>of().drop(-1);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(List.of(), itr.toList());

    itr = Iterator.of(1, null, 3).drop(1);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(null, 3), itr.toList());
    itr = Iterator.of(1, null, 3).drop(2);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(3), itr.toList());
    itr = Iterator.of(1, null, 3).drop(3);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(List.of(), itr.toList());
    itr = Iterator.of(1, null, 3).drop(4);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(List.of(), itr.toList());
    itr = Iterator.of(1, null, 3).drop(0);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());
    itr = Iterator.of(1, null, 3).drop(-1);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());
  }

  @Test
  public void dropRight() {
    var itr = Iterator.<Integer>of().dropRight(1);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(0, itr.size());
    itr = Iterator.<Integer>of().dropRight(0);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(0, itr.size());
    itr = Iterator.<Integer>of().dropRight(-1);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(List.of(), itr.toList());

    itr = Iterator.of(1, null, 3).dropRight(1);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null), itr.toList());
    itr = Iterator.of(1, null, 3).dropRight(2);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1), itr.toList());
    itr = Iterator.of(1, null, 3).dropRight(3);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(List.of(), itr.toList());
    itr = Iterator.of(1, null, 3).dropRight(4);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(List.of(), itr.toList());
    itr = Iterator.of(1, null, 3).dropRight(0);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());
    itr = Iterator.of(1, null, 3).dropRight(-1);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());
  }

  @Test
  public void dropRightWhile() {
    var itr = Iterator.<Integer>of().dropRightWhile(e -> e > 0);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(0, itr.size());

    itr = Iterator.of(1, null, 3).dropRightWhile(Objects::isNull);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());
    itr = Iterator.of(1, null, 3).dropRightWhile(Objects::nonNull);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null), itr.toList());
    itr = Iterator.of(1, null, 3).dropRightWhile(e -> e < 1);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());

    itr = Iterator.of(1, 2, 3).dropRightWhile(e -> e > 0);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(List.of(), itr.toList());

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).dropRightWhile(e -> e > 0).size());
  }

  @Test
  public void dropWhile() {
    var itr = Iterator.<Integer>of().dropWhile(e -> e > 0);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(0, itr.size());

    itr = Iterator.of(1, null, 3).dropWhile(Objects::isNull);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());
    itr = Iterator.of(1, null, 3).dropWhile(Objects::nonNull);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(null, 3), itr.toList());
    itr = Iterator.of(1, null, 3).dropWhile(e -> e < 1);
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertEquals(List.of(1, null, 3), itr.toList());

    itr = Iterator.of(1, 2, 3).dropWhile(e -> e > 0);
    assertTrue(itr.isEmpty());
    assertFalse(itr.notEmpty());
    assertEquals(List.of(), itr.toList());

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).dropWhile(e -> e > 0).size());
  }

  @Test
  public void each() {
    assertFalse(Iterator.of().each(Objects::nonNull).isEmpty());
    assertTrue(Iterator.of().each(Objects::nonNull).notEmpty());
    assertEquals(1, Iterator.of().each(Objects::nonNull).size());
    assertFalse(Iterator.of().each(Objects::nonNull).first());
    assertFalse(Iterator.of(1, 2, 3).each(i -> i < 3).first());
    assertTrue(Iterator.of(1, 2, 3).each(i -> i > 0).first());
    var itr = Iterator.of(1, null, 3).each(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void endsWith() {
    var itr = Iterator.<Integer>of().endsWith(List.of());
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertTrue(itr.first());
    assertEquals(0, itr.size());

    itr = Iterator.<Integer>of().endsWith(List.of(1));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertFalse(itr.first());
    assertEquals(0, itr.size());

    itr = Iterator.of(1, null, 3).endsWith(List.of());
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertTrue(itr.first());
    assertEquals(0, itr.size());

    itr = Iterator.of(1, null, 3).endsWith(List.of(3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertTrue(itr.first());
    assertEquals(0, itr.size());

    itr = Iterator.of(1, null, 3).endsWith(List.of(null));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertFalse(itr.first());
    assertEquals(0, itr.size());

    itr = Iterator.of(1, null, 3).endsWith(List.of(null, 3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertTrue(itr.first());
    assertEquals(0, itr.size());

    itr = Iterator.of(1, null, 3).endsWith(List.of(1, null));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertFalse(itr.first());
    assertEquals(0, itr.size());

    itr = Iterator.of(1, null, 3).endsWith(List.of(1, null, 3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertTrue(itr.first());
    assertEquals(0, itr.size());

    itr = Iterator.of(1, null, 3).endsWith(List.of(null, null, 3));
    assertFalse(itr.isEmpty());
    assertTrue(itr.notEmpty());
    assertFalse(itr.first());
    assertEquals(0, itr.size());
  }

  @Test
  public void exists() {
    assertFalse(Iterator.of().exists(Objects::nonNull).isEmpty());
    assertTrue(Iterator.of().exists(Objects::nonNull).notEmpty());
    assertEquals(1, Iterator.of().exists(Objects::nonNull).size());
    assertFalse(Iterator.of().exists(Objects::nonNull).first());
    assertFalse(Iterator.of(1, 2, 3).exists(i -> i > 3).first());
    assertTrue(Iterator.of(1, 2, 3).exists(i -> i > 0).first());
    var itr = Iterator.of(1, null, 3).exists(i -> i > 1);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void filter() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().filter(Objects::nonNull).isEmpty());
    assertEquals(List.of(1, 2, 4), itr.get().filter(Objects::nonNull).toList());
    assertEquals(List.of(1, 2), itr.get().filter(Objects::nonNull).filter(i -> i < 3).toList());
    assertEquals(List.of(4), itr.get().filter(Objects::nonNull).filter(i -> i > 3).toList());
    assertEquals(List.of(), itr.get().filter(Objects::nonNull).filter(i -> i > 4).toList());
    assertThrows(NullPointerException.class, () -> itr.get().filter(i -> i > 4).size());

    assertTrue(Iterator.of().filter(Objects::isNull).isEmpty());
    assertEquals(0, Iterator.of().filter(Objects::isNull).size());
  }

  @Test
  public void findAny() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().findAny(Objects::isNull).isEmpty());
    assertEquals(1, itr.get().findAny(Objects::isNull).size());
    assertEquals(List.of(null), itr.get().findAny(Objects::isNull).toList());
    assertFalse(itr.get().findAny(i -> i < 4).isEmpty());
    assertEquals(1, itr.get().findAny(i -> i < 4).size());

    assertTrue(Iterator.of().findAny(Objects::isNull).isEmpty());
    assertEquals(0, Iterator.of().findAny(Objects::isNull).size());
  }

  @Test
  public void findIndexOf() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().findIndexOf(null).isEmpty());
    assertEquals(1, itr.get().findIndexOf(null).size());
    assertEquals(2, itr.get().findIndexOf(null).first());
    assertEquals(List.of(2), itr.get().findIndexOf(null).toList());
    assertFalse(itr.get().findIndexOf(4).isEmpty());
    assertEquals(1, itr.get().findIndexOf(4).size());
    assertEquals(3, itr.get().findIndexOf(4).first());
    assertEquals(List.of(3), itr.get().findIndexOf(4).toList());
    assertTrue(itr.get().findIndexOf(3).isEmpty());
    assertEquals(0, itr.get().findIndexOf(3).size());
    assertThrows(NoSuchElementException.class, () -> itr.get().findIndexOf(3).first());
    assertEquals(List.of(), itr.get().findIndexOf(3).toList());

    assertTrue(Iterator.of().findIndexOf(null).isEmpty());
    assertEquals(0, Iterator.of().findIndexOf(null).size());
  }

  @Test
  public void findIndexOfSlice() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().findIndexOfSlice(List.of(2, null)).isEmpty());
    assertEquals(1, itr.get().findIndexOfSlice(List.of(2, null)).size());
    assertEquals(1, itr.get().findIndexOfSlice(List.of(2, null)).first());
    assertEquals(List.of(1), itr.get().findIndexOfSlice(List.of(2, null)).toList());
    assertFalse(itr.get().findIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(1, itr.get().findIndexOfSlice(List.of(null)).size());
    assertEquals(2, itr.get().findIndexOfSlice(List.of(null)).first());
    assertEquals(List.of(2), itr.get().findIndexOfSlice(List.of(null)).toList());
    assertTrue(itr.get().findIndexOfSlice(List.of(null, 2)).isEmpty());
    assertEquals(0, itr.get().findIndexOfSlice(List.of(null, 2)).size());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().findIndexOfSlice(List.of(null, 2)).first());
    assertEquals(List.of(), itr.get().findIndexOfSlice(List.of(null, 2)).toList());
    assertFalse(itr.get().findIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, itr.get().findIndexOfSlice(List.of()).size());
    assertEquals(0, itr.get().findIndexOfSlice(List.of()).first());
    assertEquals(List.of(0), itr.get().findIndexOfSlice(List.of()).toList());

    assertEquals(2, Iterator.of(1, 1, 1, 1, 2, 1).findIndexOfSlice(List.of(1, 1, 2)).first());

    assertTrue(Iterator.of().findIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(0, Iterator.of().findIndexOfSlice(List.of(null)).size());
    assertFalse(Iterator.of().findIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, Iterator.of().findIndexOfSlice(List.of()).size());
    assertEquals(0, Iterator.of().findIndexOfSlice(List.of()).first());
    assertEquals(List.of(0), Iterator.of().findIndexOfSlice(List.of()).toList());
  }

  @Test
  public void findIndexWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().findIndexWhere(Objects::isNull).isEmpty());
    assertEquals(1, itr.get().findIndexWhere(Objects::isNull).size());
    assertEquals(2, itr.get().findIndexWhere(Objects::isNull).first());
    assertEquals(List.of(2), itr.get().findIndexWhere(Objects::isNull).toList());
    assertFalse(itr.get().findIndexWhere(i -> i > 1).isEmpty());
    assertEquals(1, itr.get().findIndexWhere(i -> i > 1).size());
    assertEquals(1, itr.get().findIndexWhere(i -> i > 1).first());
    assertEquals(List.of(1), itr.get().findIndexWhere(i -> i > 1).toList());
    assertThrows(NullPointerException.class, () -> itr.get().findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().findIndexWhere(i -> i > 3).first());

    assertTrue(Iterator.of().findIndexWhere(Objects::isNull).isEmpty());
    assertEquals(0, Iterator.of().findIndexWhere(Objects::isNull).size());
  }

  @Test
  public void findLast() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 5);
    assertFalse(itr.get().findLast(Objects::isNull).isEmpty());
    assertEquals(1, itr.get().findLast(Objects::isNull).size());
    assertNull(itr.get().findLast(Objects::isNull).first());
    assertEquals(List.of(null), itr.get().findLast(Objects::isNull).toList());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 4).first());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 5).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 5).size());
    assertThrows(NullPointerException.class, () -> itr.get().findLast(i -> i < 5).first());
    assertTrue(itr.get().findLast(i -> i != null && i > 5).isEmpty());
    assertEquals(0, itr.get().findLast(i -> i != null && i > 5).size());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().findLast(i -> i != null && i > 5).first());
    assertEquals(List.of(), itr.get().findLast(i -> i != null && i > 5).toList());

    assertTrue(Iterator.of().findLast(Objects::isNull).isEmpty());
    assertEquals(0, Iterator.of().findLast(Objects::isNull).size());
  }

  @Test
  public void findLastIndexOf() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().findLastIndexOf(null).isEmpty());
    assertEquals(1, itr.get().findLastIndexOf(null).size());
    assertEquals(2, itr.get().findLastIndexOf(null).first());
    assertEquals(List.of(2), itr.get().findLastIndexOf(null).toList());
    assertFalse(itr.get().findLastIndexOf(4).isEmpty());
    assertEquals(1, itr.get().findLastIndexOf(4).size());
    assertEquals(3, itr.get().findLastIndexOf(4).first());
    assertEquals(List.of(3), itr.get().findLastIndexOf(4).toList());
    assertTrue(itr.get().findLastIndexOf(3).isEmpty());
    assertEquals(0, itr.get().findLastIndexOf(3).size());
    assertThrows(NoSuchElementException.class, () -> itr.get().findLastIndexOf(3).first());
    assertEquals(List.of(), itr.get().findLastIndexOf(3).toList());

    assertTrue(Iterator.of().findLastIndexOf(null).isEmpty());
    assertEquals(0, Iterator.of().findLastIndexOf(null).size());
    assertFalse(Iterator.of().findLastIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, Iterator.of().findLastIndexOfSlice(List.of()).size());
    assertEquals(0, Iterator.of().findLastIndexOfSlice(List.of()).first());
    assertEquals(List.of(0), Iterator.of().findLastIndexOfSlice(List.of()).toList());
  }

  @Test
  public void findLastIndexOfSlice() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().findLastIndexOfSlice(List.of(2, null)).isEmpty());
    assertEquals(1, itr.get().findLastIndexOfSlice(List.of(2, null)).size());
    assertEquals(1, itr.get().findLastIndexOfSlice(Iterator.of(2, null)).first());
    assertEquals(List.of(1), itr.get().findLastIndexOfSlice(List.of(2, null)).toList());
    assertFalse(itr.get().findLastIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(1, itr.get().findLastIndexOfSlice(List.of(null)).size());
    assertEquals(2, itr.get().findLastIndexOfSlice(List.of(null)).first());
    assertEquals(List.of(2), itr.get().findLastIndexOfSlice(List.of(null)).toList());
    assertTrue(itr.get().findLastIndexOfSlice(List.of(null, 2)).isEmpty());
    assertEquals(0, itr.get().findLastIndexOfSlice(List.of(null, 2)).size());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().findLastIndexOfSlice(List.of(null, 2)).first());
    assertEquals(List.of(), itr.get().findLastIndexOfSlice(List.of(null, 2)).toList());
    assertFalse(itr.get().findLastIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, itr.get().findLastIndexOfSlice(List.of()).size());
    assertEquals(4, itr.get().findLastIndexOfSlice(List.of()).first());
    assertEquals(List.of(4), itr.get().findLastIndexOfSlice(List.of()).toList());

    assertEquals(2, Iterator.of(1, 1, 1, 1, 2, 1).findLastIndexOfSlice(List.of(1, 1, 2)).first());

    assertTrue(Iterator.of().findLastIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(0, Iterator.of().findLastIndexOfSlice(List.of(null)).size());
  }

  @Test
  public void findLastIndexWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().findLastIndexWhere(Objects::isNull).isEmpty());
    assertEquals(1, itr.get().findLastIndexWhere(Objects::isNull).size());
    assertEquals(2, itr.get().findLastIndexWhere(Objects::isNull).first());
    assertEquals(List.of(2), itr.get().findLastIndexWhere(Objects::isNull).toList());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i > 1).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().findLastIndexWhere(i -> i > 1).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i > 1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().findLastIndexWhere(i -> i < 3).first());

    assertTrue(Iterator.of().findLastIndexWhere(Objects::isNull).isEmpty());
    assertEquals(0, Iterator.of().findLastIndexWhere(Objects::isNull).size());
  }

  @Test
  public void flatMap() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2);
    assertFalse(itr.get().flatMap(i -> List.of(i, i)).isEmpty());
    assertEquals(4, itr.get().flatMap(i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, 2, 2), itr.get().flatMap(i -> List.of(i, i)).toList());
    assertEquals(2, itr.get().flatMap(i -> List.of(i, i)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMap(i -> List.of(i, i)).drop(4).first());
    assertTrue(itr.get().flatMap(i -> List.of()).isEmpty());
    assertEquals(0, itr.get().flatMap(i -> List.of()).size());
    assertEquals(List.of(), itr.get().flatMap(i -> List.of()).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().flatMap(i -> List.of()).first());
    assertFalse(itr.get().flatMap(i -> List.of(null)).isEmpty());
    assertEquals(2, itr.get().flatMap(i -> List.of(null)).size());
    assertEquals(List.of(null, null), itr.get().flatMap(i -> List.of(null)).toList());
    assertNull(itr.get().flatMap(i -> List.of(null)).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMap(i -> List.of(null)).drop(2).first());
  }

  @Test
  public void flatMapAfter() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2);
    assertFalse(itr.get().flatMapAfter(-1, i -> List.of(i, i)).isEmpty());
    assertEquals(2, itr.get().flatMapAfter(-1, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2), itr.get().flatMapAfter(-1, i -> List.of(i, i)).toList());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapAfter(2, i -> List.of(i, i)).drop(2).first());
    assertFalse(itr.get().flatMapAfter(0, i -> List.of(i, i)).isEmpty());
    assertEquals(3, itr.get().flatMapAfter(0, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, 2), itr.get().flatMapAfter(0, i -> List.of(i, i)).toList());
    assertEquals(2, itr.get().flatMapAfter(0, i -> List.of(i, i)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapAfter(0, i -> List.of(i, i)).drop(3).first());
    assertFalse(itr.get().flatMapAfter(1, i -> List.of(i, i)).isEmpty());
    assertEquals(3, itr.get().flatMapAfter(1, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2, 2), itr.get().flatMapAfter(1, i -> List.of(i, i)).toList());
    assertEquals(2, itr.get().flatMapAfter(1, i -> List.of(i, i)).drop(2).first());
    assertFalse(itr.get().flatMapAfter(2, i -> List.of(i, i)).isEmpty());
    assertEquals(2, itr.get().flatMapAfter(2, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2), itr.get().flatMapAfter(2, i -> List.of(i, i)).toList());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapAfter(2, i -> List.of(i, i)).drop(2).first());

    assertFalse(itr.get().flatMapAfter(0, i -> List.of()).isEmpty());
    assertEquals(1, itr.get().flatMapAfter(0, i -> List.of()).size());
    assertEquals(List.of(2), itr.get().flatMapAfter(0, i -> List.of()).toList());
    assertEquals(2, itr.get().flatMapAfter(0, i -> List.of()).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapAfter(0, i -> List.of()).drop(1).first());
    assertFalse(itr.get().flatMapAfter(1, i -> List.of()).isEmpty());
    assertEquals(1, itr.get().flatMapAfter(1, i -> List.of()).size());
    assertEquals(List.of(1), itr.get().flatMapAfter(1, i -> List.of()).toList());
    assertEquals(1, itr.get().flatMapAfter(1, i -> List.of()).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapAfter(1, i -> List.of()).drop(1).first());
    assertFalse(itr.get().flatMapAfter(2, i -> List.of()).isEmpty());
    assertEquals(2, itr.get().flatMapAfter(2, i -> List.of()).size());
    assertEquals(List.of(1, 2), itr.get().flatMapAfter(2, i -> List.of()).toList());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapAfter(2, i -> List.of()).drop(2).first());

    assertTrue(Iterator.of().flatMapAfter(0, i -> List.of(i, i)).isEmpty());
    assertEquals(0, Iterator.of().flatMapAfter(0, i -> List.of(i, i)).size());
    assertEquals(List.of(), Iterator.of().flatMapAfter(0, i -> List.of(i, i)).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.of().flatMapAfter(0, i -> List.of(i, i)).first());
  }

  @Test
  public void flatMapFirstWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().flatMapFirstWhere(i -> false, i -> List.of(i, i)).isEmpty());
    assertEquals(4, itr.get().flatMapFirstWhere(i -> false, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2, null, 4),
        itr.get().flatMapFirstWhere(i -> false, i -> List.of(i, i)).toList());
    assertNull(itr.get().flatMapFirstWhere(i -> false, i -> List.of(i, i)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapFirstWhere(i -> false, i -> List.of(i, i)).drop(4).first());
    assertFalse(itr.get().flatMapFirstWhere(i -> true, i -> List.of(i, i)).isEmpty());
    assertEquals(5, itr.get().flatMapFirstWhere(i -> true, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, 2, null, 4),
        itr.get().flatMapFirstWhere(i -> true, i -> List.of(i, i)).toList());
    assertEquals(1, itr.get().flatMapFirstWhere(i -> true, i -> List.of(i, i)).drop(1).first());
    assertNull(itr.get().flatMapFirstWhere(i -> true, i -> List.of(i, i)).drop(3).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapFirstWhere(i -> true, i -> List.of(i, i)).drop(5).first());
    assertFalse(itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of(3)).isEmpty());
    assertEquals(4, itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of(3)).size());
    assertEquals(List.of(1, 2, 3, 4),
        itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of(3)).toList());
    assertEquals(2, itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of(3)).drop(1).first());
    assertEquals(3, itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of(3)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of(3)).drop(4).first());

    assertFalse(itr.get().flatMapFirstWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(4, itr.get().flatMapFirstWhere(i -> false, i -> List.of()).size());
    assertEquals(List.of(1, 2, null, 4),
        itr.get().flatMapFirstWhere(i -> false, i -> List.of()).toList());
    assertNull(itr.get().flatMapFirstWhere(i -> false, i -> List.of()).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapFirstWhere(i -> false, i -> List.of()).drop(4).first());
    assertFalse(itr.get().flatMapFirstWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(3, itr.get().flatMapFirstWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(2, null, 4),
        itr.get().flatMapFirstWhere(i -> true, i -> List.of()).toList());
    assertEquals(4, itr.get().flatMapFirstWhere(i -> true, i -> List.of()).drop(2).first());
    assertNull(itr.get().flatMapFirstWhere(i -> true, i -> List.of()).drop(1).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapFirstWhere(i -> true, i -> List.of()).drop(3).first());
    assertFalse(itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of()).isEmpty());
    assertEquals(3, itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of()).size());
    assertEquals(List.of(1, 2, 4),
        itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of()).toList());
    assertEquals(2, itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of()).drop(1).first());
    assertEquals(4, itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of()).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapFirstWhere(Objects::isNull, i -> List.of()).drop(3).first());

    assertFalse(itr.get().flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).isEmpty());
    assertEquals(5, itr.get().flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, 2, null, 4),
        itr.get().flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).toList());
    assertEquals(1, itr.get().flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).drop(1).first());
    assertNull(itr.get().flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).drop(3).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).drop(5).first());
    assertFalse(itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).first());
    assertEquals(2, itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).drop(2).first());

    assertTrue(Iterator.of().flatMapFirstWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(0, Iterator.of().flatMapFirstWhere(i -> false, i -> List.of()).size());
    assertEquals(List.of(), Iterator.of().flatMapFirstWhere(i -> false, i -> List.of()).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.of().flatMapFirstWhere(i -> false, i -> List.of()).drop(2).first());
    assertTrue(Iterator.of().flatMapFirstWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(0, Iterator.of().flatMapFirstWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(), Iterator.of().flatMapFirstWhere(i -> true, i -> List.of()).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.of().flatMapFirstWhere(i -> true, i -> List.of()).drop(2).first());
  }

  @Test
  public void flatMapLastWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().flatMapLastWhere(i -> false, i -> List.of(i, i)).isEmpty());
    assertEquals(4, itr.get().flatMapLastWhere(i -> false, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2, null, 4),
        itr.get().flatMapLastWhere(i -> false, i -> List.of(i, i)).toList());
    assertNull(itr.get().flatMapLastWhere(i -> false, i -> List.of(i, i)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapLastWhere(i -> false, i -> List.of(i, i)).drop(4).first());
    assertFalse(itr.get().flatMapLastWhere(i -> true, i -> List.of(i, i)).isEmpty());
    assertEquals(5, itr.get().flatMapLastWhere(i -> true, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2, null, 4, 4),
        itr.get().flatMapLastWhere(i -> true, i -> List.of(i, i)).toList());
    assertEquals(2, itr.get().flatMapLastWhere(i -> true, i -> List.of(i, i)).drop(1).first());
    assertNull(itr.get().flatMapLastWhere(i -> true, i -> List.of(i, i)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapLastWhere(i -> true, i -> List.of(i, i)).drop(5).first());
    assertFalse(itr.get().flatMapLastWhere(Objects::isNull, i -> List.of(3)).isEmpty());
    assertEquals(4, itr.get().flatMapLastWhere(Objects::isNull, i -> List.of(3)).size());
    assertEquals(List.of(1, 2, 3, 4),
        itr.get().flatMapLastWhere(Objects::isNull, i -> List.of(3)).toList());
    assertEquals(2, itr.get().flatMapLastWhere(Objects::isNull, i -> List.of(3)).drop(1).first());
    assertEquals(3, itr.get().flatMapLastWhere(Objects::isNull, i -> List.of(3)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapLastWhere(Objects::isNull, i -> List.of(3)).drop(4).first());

    assertFalse(itr.get().flatMapLastWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(4, itr.get().flatMapLastWhere(i -> false, i -> List.of()).size());
    assertEquals(List.of(1, 2, null, 4),
        itr.get().flatMapLastWhere(i -> false, i -> List.of()).toList());
    assertNull(itr.get().flatMapLastWhere(i -> false, i -> List.of()).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapLastWhere(i -> false, i -> List.of()).drop(4).first());
    assertFalse(itr.get().flatMapLastWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(3, itr.get().flatMapLastWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(1, 2, null),
        itr.get().flatMapLastWhere(i -> true, i -> List.of()).toList());
    assertEquals(2, itr.get().flatMapLastWhere(i -> true, i -> List.of()).drop(1).first());
    assertNull(itr.get().flatMapLastWhere(i -> true, i -> List.of()).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapLastWhere(i -> true, i -> List.of()).drop(3).first());
    assertFalse(itr.get().flatMapLastWhere(Objects::isNull, i -> List.of()).isEmpty());
    assertEquals(3, itr.get().flatMapLastWhere(Objects::isNull, i -> List.of()).size());
    assertEquals(List.of(1, 2, 4),
        itr.get().flatMapLastWhere(Objects::isNull, i -> List.of()).toList());
    assertEquals(2, itr.get().flatMapLastWhere(Objects::isNull, i -> List.of()).drop(1).first());
    assertEquals(4, itr.get().flatMapLastWhere(Objects::isNull, i -> List.of()).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapLastWhere(Objects::isNull, i -> List.of()).drop(3).first());

    assertFalse(itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).isEmpty());
    assertEquals(5, itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2, null, 4, 4),
        itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).toList());
    assertEquals(2, itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).drop(1).first());
    assertNull(itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).drop(5).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).drop(3).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).drop(2).first());

    assertTrue(Iterator.of().flatMapLastWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(0, Iterator.of().flatMapLastWhere(i -> false, i -> List.of()).size());
    assertEquals(List.of(), Iterator.of().flatMapLastWhere(i -> false, i -> List.of()).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.of().flatMapLastWhere(i -> false, i -> List.of()).drop(2).first());
    assertTrue(Iterator.of().flatMapLastWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(0, Iterator.of().flatMapLastWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(), Iterator.of().flatMapLastWhere(i -> true, i -> List.of()).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.of().flatMapLastWhere(i -> true, i -> List.of()).drop(2).first());
  }

  @Test
  public void flatMapWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, null, null, 4);
    assertFalse(itr.get().flatMapWhere(i -> false, i -> List.of(i, i)).isEmpty());
    assertEquals(4, itr.get().flatMapWhere(i -> false, i -> List.of(i, i)).size());
    assertEquals(List.of(1, null, null, 4),
        itr.get().flatMapWhere(i -> false, i -> List.of(i, i)).toList());
    assertNull(itr.get().flatMapWhere(i -> false, i -> List.of(i, i)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapWhere(i -> false, i -> List.of(i, i)).drop(4).first());
    assertFalse(itr.get().flatMapWhere(i -> true, i -> List.of(i, i)).isEmpty());
    assertEquals(8, itr.get().flatMapWhere(i -> true, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, null, null, null, null, 4, 4),
        itr.get().flatMapWhere(i -> true, i -> List.of(i, i)).toList());
    assertEquals(1, itr.get().flatMapWhere(i -> true, i -> List.of(i, i)).drop(1).first());
    assertNull(itr.get().flatMapWhere(i -> true, i -> List.of(i, i)).drop(4).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapWhere(i -> true, i -> List.of(i, i)).drop(8).first());
    assertFalse(itr.get().flatMapWhere(Objects::isNull, i -> List.of(3)).isEmpty());
    assertEquals(4, itr.get().flatMapWhere(Objects::isNull, i -> List.of(3)).size());
    assertEquals(List.of(1, 3, 3, 4),
        itr.get().flatMapWhere(Objects::isNull, i -> List.of(3)).toList());
    assertEquals(3, itr.get().flatMapWhere(Objects::isNull, i -> List.of(3)).drop(1).first());
    assertEquals(3, itr.get().flatMapWhere(Objects::isNull, i -> List.of(3)).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapWhere(Objects::isNull, i -> List.of(3)).drop(4).first());

    assertFalse(itr.get().flatMapWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(4, itr.get().flatMapWhere(i -> false, i -> List.of()).size());
    assertEquals(List.of(1, null, null, 4),
        itr.get().flatMapWhere(i -> false, i -> List.of()).toList());
    assertNull(itr.get().flatMapWhere(i -> false, i -> List.of()).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapWhere(i -> false, i -> List.of()).drop(4).first());
    assertTrue(itr.get().flatMapWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(0, itr.get().flatMapWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(), itr.get().flatMapWhere(i -> true, i -> List.of()).toList());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapWhere(i -> true, i -> List.of()).first());
    assertFalse(itr.get().flatMapWhere(Objects::isNull, i -> List.of()).isEmpty());
    assertEquals(2, itr.get().flatMapWhere(Objects::isNull, i -> List.of()).size());
    assertEquals(List.of(1, 4), itr.get().flatMapWhere(Objects::isNull, i -> List.of()).toList());
    assertEquals(4, itr.get().flatMapWhere(Objects::isNull, i -> List.of()).drop(1).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapWhere(Objects::isNull, i -> List.of()).drop(2).first());

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

    assertTrue(Iterator.of().flatMapWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(0, Iterator.of().flatMapWhere(i -> false, i -> List.of()).size());
    assertEquals(List.of(), Iterator.of().flatMapWhere(i -> false, i -> List.of()).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.of().flatMapWhere(i -> false, i -> List.of()).drop(2).first());
    assertTrue(Iterator.of().flatMapWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(0, Iterator.of().flatMapWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(), Iterator.of().flatMapWhere(i -> true, i -> List.of()).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.of().flatMapWhere(i -> true, i -> List.of()).drop(2).first());
  }

  @Test
  public void foldLeft() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, 4, 5);
    assertFalse(itr.get().foldLeft(1, Integer::sum).isEmpty());
    assertEquals(1, itr.get().foldLeft(1, Integer::sum).size());
    assertEquals(List.of(16), itr.get().foldLeft(1, Integer::sum).toList());
    assertEquals(16, itr.get().foldLeft(1, Integer::sum).first());

    assertEquals(java.util.List.of(1, 2),
        Iterator.of(1, 2).foldLeft(List.of(), List::append).first());

    assertFalse(Iterator.<Integer>of().foldLeft(1, Integer::sum).isEmpty());
    assertEquals(1, Iterator.<Integer>of().foldLeft(1, Integer::sum).size());
    assertEquals(List.of(1), Iterator.<Integer>of().foldLeft(1, Integer::sum).toList());
    assertEquals(1, Iterator.<Integer>of().foldLeft(1, Integer::sum).first());
    assertEquals(List.of(), Iterator.of().foldLeft(List.of(), List::append).first());
  }

  @Test
  public void foldRight() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, 4, 5);
    assertFalse(itr.get().foldRight(1, Integer::sum).isEmpty());
    assertEquals(1, itr.get().foldRight(1, Integer::sum).size());
    assertEquals(List.of(16), itr.get().foldRight(1, Integer::sum).toList());
    assertEquals(16, itr.get().foldRight(1, Integer::sum).first());

    assertEquals(List.of(2, 1),
        Iterator.of(1, 2).foldRight(List.of(), (i, li) -> li.append(i)).first());

    assertFalse(Iterator.<Integer>of().foldRight(1, Integer::sum).isEmpty());
    assertEquals(1, Iterator.<Integer>of().foldRight(1, Integer::sum).size());
    assertEquals(List.of(1), Iterator.<Integer>of().foldRight(1, Integer::sum).toList());
    assertEquals(1, Iterator.<Integer>of().foldRight(1, Integer::sum).first());
    assertEquals(List.of(), Iterator.of().foldRight(List.of(), (i, li) -> li.append(i)).first());
  }

  @Test
  public void includes() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, null, 5);
    assertFalse(itr.get().includes(null).isEmpty());
    assertEquals(1, itr.get().includes(null).size());
    assertTrue(itr.get().includes(null).first());
    assertEquals(List.of(true), itr.get().includes(null).toList());
    assertFalse(itr.get().includes(0).isEmpty());
    assertEquals(1, itr.get().includes(0).size());
    assertFalse(itr.get().includes(0).first());
    assertEquals(List.of(false), itr.get().includes(0).toList());
    assertFalse(Iterator.of().includes(0).isEmpty());
    assertEquals(1, Iterator.of().includes(0).size());
    assertFalse(Iterator.of().includes(0).first());
    assertEquals(List.of(false), Iterator.of().includes(null).toList());
  }

  @Test
  public void includesAll() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, null, 5);
    assertFalse(itr.get().includesAll(List.of(null, 1)).isEmpty());
    assertEquals(1, itr.get().includesAll(List.of(null, 1)).size());
    assertTrue(itr.get().includesAll(List.of(null, 1)).first());
    assertEquals(List.of(true), itr.get().includesAll(List.of(null, 1)).toList());
    assertFalse(itr.get().includesAll(List.of(0, 1)).isEmpty());
    assertEquals(1, itr.get().includesAll(List.of(0, 1)).size());
    assertFalse(itr.get().includesAll(List.of(0, 1)).first());
    assertEquals(List.of(false), itr.get().includesAll(List.of(0, 1)).toList());
    assertFalse(itr.get().includesAll(List.of()).isEmpty());
    assertEquals(1, itr.get().includesAll(List.of()).size());
    assertTrue(itr.get().includesAll(List.of()).first());
    assertEquals(List.of(true), itr.get().includesAll(List.of()).toList());
    assertFalse(Iterator.of().includesAll(List.of(null, 1)).isEmpty());
    assertEquals(1, Iterator.of().includesAll(List.of(null, 1)).size());
    assertFalse(Iterator.of().includesAll(List.of(null, 1)).first());
    assertEquals(List.of(false), Iterator.of().includesAll(List.of(null, 1)).toList());
    assertFalse(Iterator.of().includesAll(List.of()).isEmpty());
    assertEquals(1, Iterator.of().includesAll(List.of()).size());
    assertTrue(Iterator.of().includesAll(List.of()).first());
    assertEquals(List.of(true), Iterator.of().includesAll(List.of()).toList());
  }

  @Test
  public void includesSlice() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, null, 5);
    assertFalse(itr.get().includesSlice(List.of(3, null)).isEmpty());
    assertEquals(1, itr.get().includesSlice(List.of(3, null)).size());
    assertTrue(itr.get().includesSlice(List.of(3, null)).first());
    assertEquals(List.of(true), itr.get().includesSlice(List.of(3, null)).toList());
    assertFalse(itr.get().includesSlice(List.of(null, 3)).isEmpty());
    assertEquals(1, itr.get().includesSlice(List.of(null, 3)).size());
    assertFalse(itr.get().includesSlice(List.of(null, 3)).first());
    assertEquals(List.of(false), itr.get().includesSlice(List.of(null, 3)).toList());
    assertFalse(itr.get().includesSlice(List.of()).isEmpty());
    assertEquals(1, itr.get().includesSlice(List.of()).size());
    assertTrue(itr.get().includesSlice(List.of()).first());
    assertEquals(List.of(true), itr.get().includesSlice(List.of()).toList());
    assertFalse(Iterator.of().includesSlice(List.of(null, 1)).isEmpty());
    assertEquals(1, Iterator.of().includesSlice(List.of(null, 1)).size());
    assertFalse(Iterator.of().includesSlice(List.of(null, 1)).first());
    assertEquals(List.of(false), Iterator.of().includesSlice(List.of(null, 1)).toList());
    assertFalse(Iterator.of().includesSlice(List.of()).isEmpty());
    assertEquals(1, Iterator.of().includesSlice(List.of()).size());
    assertTrue(Iterator.of().includesSlice(List.of()).first());
    assertEquals(List.of(true), Iterator.of().includesSlice(List.of()).toList());
  }

  @Test
  public void insert() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.<Integer>of().insert(1).insert(2).insert(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(3, 2, 1), itr.get().toList());

    itr = () -> Iterator.<Integer>of().insert(1).insert(null).insert(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(3, null, 1), itr.get().toList());

    itr = () -> Iterator.of(1).insert(2).insert(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(3, 2, 1), itr.get().toList());

    itr = () -> Iterator.of(1).insert(null).insert(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(3, null, 1), itr.get().toList());

    itr = () -> Iterator.of(1, 2).insert(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(3, 1, 2), itr.get().toList());

    itr = () -> Iterator.of(1, null).insert(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(3, 1, null), itr.get().toList());

    itr = () -> Iterator.of(1, null).drop(1).insert(2);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(2, itr.get().size());
    assertEquals(List.of(2, null), itr.get().toList());
  }

  @Test
  public void insertAfter() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3);
    assertFalse(itr.get().insertAfter(5, null).isEmpty());
    assertEquals(3, itr.get().insertAfter(5, null).size());
    assertEquals(List.of(1, 2, 3), itr.get().insertAfter(5, null).toList());
    assertFalse(itr.get().insertAfter(3, null).isEmpty());
    assertEquals(4, itr.get().insertAfter(3, null).size());
    assertEquals(List.of(1, 2, 3, null), itr.get().insertAfter(3, null).toList());
    assertFalse(itr.get().insertAfter(2, null).isEmpty());
    assertEquals(4, itr.get().insertAfter(2, null).size());
    assertEquals(List.of(1, 2, null, 3), itr.get().insertAfter(2, null).toList());
    assertFalse(itr.get().insertAfter(1, null).isEmpty());
    assertEquals(4, itr.get().insertAfter(1, null).size());
    assertEquals(List.of(1, null, 2, 3), itr.get().insertAfter(1, null).toList());
    assertFalse(itr.get().insertAfter(0, null).isEmpty());
    assertEquals(4, itr.get().insertAfter(0, null).size());
    assertEquals(List.of(null, 1, 2, 3), itr.get().insertAfter(0, null).toList());
    assertFalse(itr.get().insertAfter(-7, null).isEmpty());
    assertEquals(3, itr.get().insertAfter(-7, null).size());
    assertEquals(List.of(1, 2, 3), itr.get().insertAfter(-7, null).toList());

    assertTrue(Iterator.of().insertAfter(5, null).isEmpty());
    assertEquals(0, Iterator.of().insertAfter(5, null).size());
    assertEquals(List.of(), Iterator.of().insertAfter(5, null).toList());
    assertFalse(Iterator.of().insertAfter(0, null).isEmpty());
    assertEquals(1, Iterator.of().insertAfter(0, null).size());
    assertEquals(List.of(null), Iterator.of().insertAfter(0, null).toList());

    Iterable<Object> iterable = () -> List.of().iterator();
    assertTrue(Iterator.wrap(iterable).insertAfter(5, null).isEmpty());
    assertEquals(0, Iterator.wrap(iterable).insertAfter(5, null).size());
    assertEquals(List.of(), Iterator.wrap(iterable).insertAfter(5, null).toList());
    assertFalse(Iterator.wrap(iterable).insertAfter(0, null).isEmpty());
    assertEquals(1, Iterator.wrap(iterable).insertAfter(0, null).size());
    assertEquals(List.of(null), Iterator.wrap(iterable).insertAfter(0, null).toList());
  }

  @Test
  public void insertAll() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.<Integer>of()
        .insertAll(Arrays.asList(1, 2, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    itr = () -> Iterator.<Integer>of().insertAll(List.of(1, null, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());

    itr = () -> Iterator.of(1).insertAll(new LinkedHashSet<>(List.of(2, 3)));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(2, 3, 1), itr.get().toList());

    itr = () -> Iterator.of(1).insertAll(List.of(null, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(null, 3, 1), itr.get().toList());

    itr = () -> Iterator.of(1, 2).insertAll(Set.of(3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(3, 1, 2), itr.get().toList());

    itr = () -> Iterator.of(1, null).insertAll(Set.of(3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(3, 1, null), itr.get().toList());

    itr = () -> Iterator.of(1, null).drop(1).insertAll(Iterator.of(2, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(2, 3, null), itr.get().toList());
  }

  @Test
  public void insertAllAfter() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3);
    assertFalse(itr.get().insertAllAfter(5, List.of(null, 5)).isEmpty());
    assertEquals(3, itr.get().insertAllAfter(5, List.of(null, 5)).size());
    assertEquals(List.of(1, 2, 3), itr.get().insertAllAfter(5, List.of(null, 5)).toList());
    assertFalse(itr.get().insertAllAfter(3, List.of(null, 5)).isEmpty());
    assertEquals(5, itr.get().insertAllAfter(3, List.of(null, 5)).size());
    assertEquals(List.of(1, 2, 3, null, 5), itr.get().insertAllAfter(3, List.of(null, 5)).toList());
    assertFalse(itr.get().insertAllAfter(2, List.of(null, 5)).isEmpty());
    assertEquals(5, itr.get().insertAllAfter(2, List.of(null, 5)).size());
    assertEquals(List.of(1, 2, null, 5, 3), itr.get().insertAllAfter(2, List.of(null, 5)).toList());
    assertFalse(itr.get().insertAllAfter(1, List.of(null, 5)).isEmpty());
    assertEquals(5, itr.get().insertAllAfter(1, List.of(null, 5)).size());
    assertEquals(List.of(1, null, 5, 2, 3), itr.get().insertAllAfter(1, List.of(null, 5)).toList());
    assertFalse(itr.get().insertAllAfter(0, List.of(null, 5)).isEmpty());
    assertEquals(5, itr.get().insertAllAfter(0, List.of(null, 5)).size());
    assertEquals(List.of(null, 5, 1, 2, 3), itr.get().insertAllAfter(0, List.of(null, 5)).toList());
    assertFalse(itr.get().insertAllAfter(-7, List.of(null, 5)).isEmpty());
    assertEquals(3, itr.get().insertAllAfter(-7, List.of(null, 5)).size());
    assertEquals(List.of(1, 2, 3), itr.get().insertAllAfter(-7, List.of(null, 5)).toList());

    assertTrue(Iterator.of().insertAllAfter(5, List.of(null, 5)).isEmpty());
    assertEquals(0, Iterator.of().insertAllAfter(5, List.of(null, 5)).size());
    assertEquals(List.of(), Iterator.of().insertAllAfter(5, List.of(null, 5)).toList());
    assertFalse(Iterator.of().insertAllAfter(0, List.of(null, 5)).isEmpty());
    assertEquals(2, Iterator.of().insertAllAfter(0, List.of(null, 5)).size());
    assertEquals(List.of(null, 5), Iterator.of().insertAllAfter(0, List.of(null, 5)).toList());

    Iterable<Object> iterable = () -> List.of().iterator();
    assertTrue(Iterator.wrap(iterable).insertAllAfter(5, List.of(null, 5)).isEmpty());
    assertEquals(0, Iterator.wrap(iterable).insertAllAfter(5, List.of(null, 5)).size());
    assertEquals(List.of(), Iterator.wrap(iterable).insertAllAfter(5, List.of(null, 5)).toList());
    assertFalse(Iterator.wrap(iterable).insertAllAfter(0, List.of(null, 5)).isEmpty());
    assertEquals(2, Iterator.wrap(iterable).insertAllAfter(0, List.of(null, 5)).size());
    assertEquals(List.of(null, 5),
        Iterator.wrap(iterable).insertAllAfter(0, List.of(null, 5)).toList());
  }

  @Test
  public void intersect() {
    assertEquals(List.of(1, null), Iterator.of(1, 2, null, 4).intersect(List.of(1, null)).toList());
    assertEquals(List.of(1, 4), Iterator.of(1, 2, null, 4).intersect(Iterator.of(1, 4)).toList());
    assertEquals(List.of(1, 4), Iterator.of(1, 2, null, 4).intersect(List.of(1, 3, 4)).toList());
    assertEquals(List.of(1), Iterator.of(1, 2, null, 4).intersect(Iterator.of(3, 1, 3)).toList());
    assertEquals(List.of(null), Iterator.of(1, 2, null, 4).intersect(List.of(null, null)).toList());
    assertEquals(List.of(1, null),
        Iterator.of(1, null).intersect(Iterator.of(1, 2, null, 4)).toList());
    assertEquals(List.of(1, 2), Iterator.of(1, 2, null, 4).intersect(List.of(2, 1)).toList());
    assertEquals(List.of(), Iterator.of(1, null).intersect(Iterator.of(2, 4)).toList());

    assertEquals(List.of(), Iterator.of(1, 2, null, 4).intersect(Iterator.of()).toList());
    assertEquals(List.of(), Iterator.of().intersect(Iterator.of(1, 2, null, 4)).toList());
  }

  @Test
  public void map() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3);
    assertFalse(itr.get().map(x -> x + 1).isEmpty());
    assertEquals(3, itr.get().map(x -> x + 1).size());
    assertEquals(List.of(2, 3, 4), itr.get().map(x -> x + 1).toList());
    assertFalse(itr.get().append(null).map(x -> x + 1).isEmpty());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).size());
    assertEquals(4, itr.get().append(null).map(x -> x + 1).drop(2).first());
    assertEquals(2, itr.get().append(null).map(x -> x + 1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).map(x -> x + 1).drop(3).first());

    assertTrue(Iterator.<Integer>of().map(x -> x + 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().map(x -> x + 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().map(x -> x + 1).toList());
  }

  @Test
  public void mapAfter() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3);
    assertFalse(itr.get().mapAfter(-1, x -> x + 1).isEmpty());
    assertEquals(3, itr.get().mapAfter(-1, x -> x + 1).size());
    assertEquals(List.of(1, 2, 3), itr.get().mapAfter(-1, x -> x + 1).toList());
    assertEquals(2, itr.get().mapAfter(-1, x -> x + 1).drop(1).first());
    assertFalse(itr.get().mapAfter(0, x -> x + 1).isEmpty());
    assertEquals(3, itr.get().mapAfter(0, x -> x + 1).size());
    assertEquals(List.of(2, 2, 3), itr.get().mapAfter(0, x -> x + 1).toList());
    assertEquals(2, itr.get().mapAfter(0, x -> x + 1).drop(1).first());
    assertFalse(itr.get().mapAfter(1, x -> x + 1).isEmpty());
    assertEquals(3, itr.get().mapAfter(1, x -> x + 1).size());
    assertEquals(List.of(1, 3, 3), itr.get().mapAfter(1, x -> x + 1).toList());
    assertEquals(3, itr.get().mapAfter(1, x -> x + 1).drop(1).first());
    assertFalse(itr.get().mapAfter(2, x -> x + 1).isEmpty());
    assertEquals(3, itr.get().mapAfter(2, x -> x + 1).size());
    assertEquals(List.of(1, 2, 4), itr.get().mapAfter(2, x -> x + 1).toList());
    assertEquals(2, itr.get().mapAfter(2, x -> x + 1).drop(1).first());
    assertFalse(itr.get().mapAfter(3, x -> x + 1).isEmpty());
    assertEquals(3, itr.get().mapAfter(3, x -> x + 1).size());
    assertEquals(List.of(1, 2, 3), itr.get().mapAfter(3, x -> x + 1).toList());
    assertEquals(2, itr.get().mapAfter(3, x -> x + 1).drop(1).first());

    assertFalse(itr.get().append(null).mapAfter(-1, x -> x + 1).isEmpty());
    assertEquals(4, itr.get().append(null).mapAfter(-1, x -> x + 1).size());
    assertEquals(List.of(1, 2, 3, null), itr.get().append(null).mapAfter(-1, x -> x + 1).toList());
    assertEquals(2, itr.get().append(null).mapAfter(-1, x -> x + 1).drop(1).first());
    assertFalse(itr.get().append(null).mapAfter(1, x -> x + 1).isEmpty());
    assertEquals(4, itr.get().append(null).mapAfter(1, x -> x + 1).size());
    assertEquals(List.of(1, 3, 3, null), itr.get().append(null).mapAfter(1, x -> x + 1).toList());
    assertEquals(3, itr.get().append(null).mapAfter(1, x -> x + 1).drop(1).first());
    assertFalse(itr.get().append(null).mapAfter(3, x -> x + 1).isEmpty());
    assertEquals(4, itr.get().append(null).mapAfter(3, x -> x + 1).size());
    assertEquals(2, itr.get().append(null).mapAfter(3, x -> x + 1).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).mapAfter(3, x -> x + 1).drop(3).first());

    assertTrue(Iterator.<Integer>of().mapAfter(0, x -> x + 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().mapAfter(0, x -> x + 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().mapAfter(0, x -> x + 1).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().mapAfter(0, x -> x + 1).first());
  }

  @Test
  public void mapFirstWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().mapFirstWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(4, itr.get().mapFirstWhere(i -> false, i -> i + 1).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().mapFirstWhere(i -> false, i -> i + 1).toList());
    assertNull(itr.get().mapFirstWhere(i -> false, i -> i + 1).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapFirstWhere(i -> false, i -> i + 1).drop(4).first());
    assertFalse(itr.get().mapFirstWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(4, itr.get().mapFirstWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(2, 2, null, 4), itr.get().mapFirstWhere(i -> true, i -> i + 1).toList());
    assertEquals(2, itr.get().mapFirstWhere(i -> true, i -> i + 1).drop(1).first());
    assertNull(itr.get().mapFirstWhere(i -> true, i -> i + 1).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapFirstWhere(i -> true, i -> i + 1).drop(5).first());
    assertFalse(itr.get().mapFirstWhere(Objects::isNull, i -> 3).isEmpty());
    assertEquals(4, itr.get().mapFirstWhere(Objects::isNull, i -> 3).size());
    assertEquals(List.of(1, 2, 3, 4), itr.get().mapFirstWhere(Objects::isNull, i -> 3).toList());
    assertEquals(2, itr.get().mapFirstWhere(Objects::isNull, i -> 3).drop(1).first());
    assertEquals(3, itr.get().mapFirstWhere(Objects::isNull, i -> 3).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapFirstWhere(Objects::isNull, i -> 3).drop(4).first());

    assertFalse(itr.get().mapFirstWhere(i -> i == 1, i -> i + 1).isEmpty());
    assertEquals(4, itr.get().mapFirstWhere(i -> i == 1, i -> i + 1).size());
    assertEquals(List.of(2, 2, null, 4), itr.get().mapFirstWhere(i -> i == 1, i -> i + 1).toList());
    assertEquals(2, itr.get().mapFirstWhere(i -> i == 1, i -> i + 1).drop(1).first());
    assertNull(itr.get().mapFirstWhere(i -> i == 1, i -> i + 1).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapFirstWhere(i -> i == 1, i -> i + 1).drop(5).first());
    assertFalse(itr.get().mapFirstWhere(i -> i > 2, i -> 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapFirstWhere(i -> i > 2, i -> 1).size());
    assertEquals(1, itr.get().mapFirstWhere(i -> i > 2, i -> 1).first());
    assertEquals(2, itr.get().mapFirstWhere(i -> i > 2, i -> 1).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapFirstWhere(i -> i > 2, i -> 1).drop(2).first());

    assertTrue(Iterator.<Integer>of().mapFirstWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().mapFirstWhere(i -> false, i -> i + 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().mapFirstWhere(i -> false, i -> i + 1).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().mapFirstWhere(i -> false, i -> i + 1).drop(2).first());
    assertTrue(Iterator.<Integer>of().mapFirstWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().mapFirstWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().mapFirstWhere(i -> true, i -> i + 1).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().mapFirstWhere(i -> true, i -> i + 1).drop(2).first());
  }

  @Test
  public void mapLastWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().mapLastWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(4, itr.get().mapLastWhere(i -> false, i -> i + 1).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().mapLastWhere(i -> false, i -> i + 1).toList());
    assertNull(itr.get().mapLastWhere(i -> false, i -> i + 1).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapLastWhere(i -> false, i -> i + 1).drop(4).first());
    assertFalse(itr.get().mapLastWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(4, itr.get().mapLastWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(1, 2, null, 5), itr.get().mapLastWhere(i -> true, i -> i + 1).toList());
    assertEquals(2, itr.get().mapLastWhere(i -> true, i -> i + 1).drop(1).first());
    assertNull(itr.get().mapLastWhere(i -> true, i -> i + 1).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapLastWhere(i -> true, i -> i + 1).drop(5).first());
    assertFalse(itr.get().mapLastWhere(Objects::isNull, i -> 3).isEmpty());
    assertEquals(4, itr.get().mapLastWhere(Objects::isNull, i -> 3).size());
    assertEquals(List.of(1, 2, 3, 4), itr.get().mapLastWhere(Objects::isNull, i -> 3).toList());
    assertEquals(2, itr.get().mapLastWhere(Objects::isNull, i -> 3).drop(1).first());
    assertEquals(3, itr.get().mapLastWhere(Objects::isNull, i -> 3).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapLastWhere(Objects::isNull, i -> 3).drop(4).first());

    assertFalse(itr.get().mapLastWhere(i -> i == 4, i -> i + 1).isEmpty());
    assertEquals(4, itr.get().mapLastWhere(i -> i == 4, i -> i + 1).size());
    assertEquals(List.of(1, 2, null, 5), itr.get().mapLastWhere(i -> i == 4, i -> i + 1).toList());
    assertEquals(2, itr.get().mapLastWhere(i -> i == 4, i -> i + 1).drop(1).first());
    assertNull(itr.get().mapLastWhere(i -> i == 4, i -> i + 1).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapLastWhere(i -> i == 4, i -> i + 1).drop(5).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapLastWhere(i -> i < 2, i -> 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapLastWhere(i -> i < 2, i -> 1).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().mapLastWhere(i -> i < 2, i -> 1).first());

    assertTrue(Iterator.<Integer>of().mapLastWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().mapLastWhere(i -> false, i -> i + 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().mapLastWhere(i -> false, i -> i + 1).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().mapLastWhere(i -> false, i -> i + 1).drop(2).first());
    assertTrue(Iterator.<Integer>of().mapLastWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().mapLastWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().mapLastWhere(i -> true, i -> i + 1).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().mapLastWhere(i -> true, i -> i + 1).drop(2).first());
  }

  @Test
  public void mapWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, 4);
    assertFalse(itr.get().mapWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(4, itr.get().mapWhere(i -> false, i -> i + 1).size());
    assertEquals(List.of(1, 2, 3, 4), itr.get().mapWhere(i -> false, i -> i + 1).toList());
    assertEquals(3, itr.get().mapWhere(i -> false, i -> i + 1).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapWhere(i -> false, i -> i + 1).drop(4).first());
    assertFalse(itr.get().mapWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(4, itr.get().mapWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(2, 3, 4, 5), itr.get().mapWhere(i -> true, i -> i + 1).toList());
    assertEquals(3, itr.get().mapWhere(i -> true, i -> i + 1).drop(1).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapWhere(i -> true, i -> i + 1).drop(5).first());
    assertFalse(itr.get().mapWhere(i -> i == 2, i -> 3).isEmpty());
    assertEquals(4, itr.get().mapWhere(i -> i == 2, i -> 3).size());
    assertEquals(List.of(1, 3, 3, 4), itr.get().mapWhere(i -> i == 2, i -> 3).toList());
    assertEquals(3, itr.get().mapWhere(i -> i == 2, i -> 3).drop(1).first());
    assertEquals(3, itr.get().mapWhere(i -> i == 2, i -> 3).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().mapWhere(i -> i == 2, i -> 3).drop(4).first());

    assertFalse(itr.get().append(null).mapWhere(i -> i == 4, i -> i + 1).isEmpty());
    assertEquals(5, itr.get().append(null).mapWhere(i -> i == 4, i -> i + 1).size());
    assertEquals(2, itr.get().append(null).mapWhere(i -> i == 4, i -> i + 1).drop(1).first());
    assertEquals(3, itr.get().append(null).mapWhere(i -> i == 4, i -> i + 1).drop(2).first());
    assertEquals(5, itr.get().append(null).mapWhere(i -> i == 4, i -> i + 1).drop(3).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).mapWhere(i -> i == 4, i -> i + 1).drop(4).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().append(null).mapWhere(i -> i == 4, i -> i + 1).drop(5).first());

    assertTrue(Iterator.<Integer>of().mapWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().mapWhere(i -> false, i -> i + 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().mapWhere(i -> false, i -> i + 1).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().mapWhere(i -> false, i -> i + 1).drop(2).first());
    assertTrue(Iterator.<Integer>of().mapWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().mapWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().mapWhere(i -> true, i -> i + 1).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().mapWhere(i -> true, i -> i + 1).drop(2).first());
  }

  @Test
  public void max() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 4, 2, 3);
    assertFalse(itr.get().max(Integer::compareTo).isEmpty());
    assertTrue(itr.get().max(Integer::compareTo).notEmpty());
    assertEquals(1, itr.get().max(Integer::compareTo).size());
    assertEquals(4, itr.get().max(Integer::compareTo).first());
    assertEquals(List.of(4), itr.get().max(Integer::compareTo).toList());

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).max(Integer::compareTo).isEmpty());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).max(Integer::compareTo).notEmpty());
    assertEquals(1, Iterator.of(1, null).max(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).max(Integer::compareTo).first());

    assertTrue(Iterator.<Integer>of().max(Integer::compareTo).isEmpty());
    assertFalse(Iterator.<Integer>of().max(Integer::compareTo).notEmpty());
    assertEquals(0, Iterator.<Integer>of().max(Integer::compareTo).size());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().max(Integer::compareTo).first());
    assertEquals(List.of(), Iterator.<Integer>of().max(Integer::compareTo).toList());
  }

  @Test
  public void min() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 4, 2, 3);
    assertFalse(itr.get().min(Integer::compareTo).isEmpty());
    assertTrue(itr.get().min(Integer::compareTo).notEmpty());
    assertEquals(1, itr.get().min(Integer::compareTo).size());
    assertEquals(1, itr.get().min(Integer::compareTo).first());
    assertEquals(List.of(1), itr.get().min(Integer::compareTo).toList());

    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).min(Integer::compareTo).isEmpty());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).min(Integer::compareTo).notEmpty());
    assertEquals(1, Iterator.of(1, null).min(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null).min(Integer::compareTo).first());

    assertTrue(Iterator.<Integer>of().min(Integer::compareTo).isEmpty());
    assertFalse(Iterator.<Integer>of().min(Integer::compareTo).notEmpty());
    assertEquals(0, Iterator.<Integer>of().min(Integer::compareTo).size());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().min(Integer::compareTo).first());
    assertEquals(List.of(), Iterator.<Integer>of().min(Integer::compareTo).toList());
  }

  @Test
  public void none() {
    assertFalse(Iterator.of().none(Objects::nonNull).isEmpty());
    assertTrue(Iterator.of().none(Objects::nonNull).notEmpty());
    assertEquals(1, Iterator.of().none(Objects::nonNull).size());
    assertTrue(Iterator.of().none(Objects::nonNull).first());
    assertFalse(Iterator.of(1, 2, 3).none(i -> i < 3).first());
    assertTrue(Iterator.of(1, 2, 3).none(i -> i < 0).first());
    var itr = Iterator.of(1, null, 3).none(i -> i < 0);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void notAll() {
    assertFalse(Iterator.of().notAll(Objects::isNull).isEmpty());
    assertTrue(Iterator.of().notAll(Objects::isNull).notEmpty());
    assertEquals(1, Iterator.of().notAll(Objects::isNull).size());
    assertTrue(Iterator.of().notAll(Objects::isNull).first());
    assertFalse(Iterator.of(1, 2, 3).notAll(i -> i < 4).first());
    assertTrue(Iterator.of(1, 2, 3).notAll(i -> i > 1).first());
    var itr = Iterator.of(1, null, 3).notAll(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);
  }

  @Test
  public void orElse() {
    assertFalse(Iterator.of(1).orElse(List.of(2)).isEmpty());
    assertTrue(Iterator.of(1).orElse(Iterator.of(2)).notEmpty());
    assertEquals(1, Iterator.of(1).orElse(Iterator.of(2)).size());
    assertEquals(1, Iterator.of(1).orElse(List.of(2)).first());
    assertEquals(List.of(1), Iterator.of(1).orElse(Iterator.of(2)).toList());

    assertFalse(Iterator.of(1).orElse(Iterator.of()).isEmpty());
    assertTrue(Iterator.of(1).orElse(List.of()).notEmpty());
    assertEquals(1, Iterator.of(1).orElse(List.of()).size());
    assertEquals(1, Iterator.of(1).orElse(Iterator.of()).first());
    assertEquals(List.of(1), Iterator.of(1).orElse(Iterator.of()).toList());

    assertFalse(Iterator.of().orElse(List.of(2)).isEmpty());
    assertTrue(Iterator.of().orElse(Iterator.of(2)).notEmpty());
    assertEquals(1, Iterator.of().orElse(Iterator.of(2)).size());
    assertEquals(2, Iterator.of().orElse(List.of(2)).first());
    assertEquals(List.of(2), Iterator.of().orElse(List.of(2)).toList());

    assertTrue(Iterator.of().orElse(Iterator.of()).isEmpty());
    assertFalse(Iterator.of().orElse(List.of()).notEmpty());
    assertEquals(0, Iterator.of().orElse(List.of()).size());
    assertThrows(NoSuchElementException.class, () -> Iterator.of().orElse(Iterator.of()).first());
    assertEquals(List.of(), Iterator.of().orElse(Iterator.of()).toList());
  }

  @Test
  public void orElseGet() {
    sparx.util.function.Supplier<Iterator<Integer>> supplier = () -> Iterator.of(2);
    assertFalse(Iterator.of(1).orElseGet(supplier).isEmpty());
    assertTrue(Iterator.of(1).orElseGet(supplier).notEmpty());
    assertEquals(1, Iterator.of(1).orElseGet(supplier).size());
    assertEquals(1, Iterator.of(1).orElseGet(supplier).first());
    assertEquals(List.of(1), Iterator.of(1).orElseGet(supplier).toList());

    assertFalse(Iterator.of(1).orElseGet(List::of).isEmpty());
    assertTrue(Iterator.of(1).orElseGet(Iterator::of).notEmpty());
    assertEquals(1, Iterator.of(1).orElseGet(Iterator::of).size());
    assertEquals(1, Iterator.of(1).orElseGet(List::of).first());
    assertEquals(List.of(1), Iterator.of(1).orElseGet(Iterator::of).toList());

    assertFalse(Iterator.of().orElseGet(supplier).isEmpty());
    assertTrue(Iterator.of().orElseGet(supplier).notEmpty());
    assertEquals(1, Iterator.of().orElseGet(supplier).size());
    assertEquals(2, Iterator.of().orElseGet(supplier).first());
    assertEquals(List.of(2), Iterator.of().orElseGet(supplier).toList());

    assertTrue(Iterator.of().orElseGet(Iterator::of).isEmpty());
    assertFalse(Iterator.of().orElseGet(List::of).notEmpty());
    assertEquals(0, Iterator.of().orElseGet(Iterator::of).size());
    assertThrows(NoSuchElementException.class, () -> Iterator.of().orElseGet(List::of).first());
    assertEquals(List.of(), Iterator.of().orElseGet(Iterator::of).toList());

    sparx.util.function.Supplier<Iterator<Integer>> throwing = () -> {
      throw new IllegalStateException();
    };
    assertFalse(Iterator.of(1).orElseGet(throwing).isEmpty());
    assertTrue(Iterator.of(1).orElseGet(throwing).notEmpty());
    assertEquals(1, Iterator.of(1).orElseGet(throwing).size());
    assertEquals(1, Iterator.of(1).orElseGet(throwing).first());
    assertEquals(List.of(1), Iterator.of(1).orElseGet(throwing).toList());

    assertThrows(IllegalStateException.class, () -> Iterator.of().orElseGet(throwing).isEmpty());
    assertThrows(IllegalStateException.class, () -> Iterator.of().orElseGet(throwing).notEmpty());
    assertThrows(IllegalStateException.class, () -> Iterator.of().orElseGet(throwing).size());
    assertThrows(IllegalStateException.class, () -> Iterator.of().orElseGet(throwing).first());
  }

  @Test
  public void peek() {
    var list = new ArrayList<Integer>();
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3).peek(i -> list.add(i));
    assertFalse(itr.get().isEmpty());
    assertTrue(list.isEmpty());
    assertTrue(itr.get().notEmpty());
    assertTrue(list.isEmpty());
    assertEquals(3, itr.get().size());
    assertTrue(list.isEmpty());
    assertEquals(List.of(1, 2, 3), itr.get().toList());
    assertEquals(List.of(1, 2, 3), list);
    list.clear();
    itr.get().next();
    assertEquals(List.of(1), list);
    list.clear();
    assertEquals(List.of(3), itr.get().drop(2).toList());
    assertEquals(List.of(3), list);
  }

  @Test
  public void peekExceptionally() {
    var ex = new AtomicReference<Throwable>();
    assertThrows(NullPointerException.class,
        () -> Iterator.of(1, null, 3).filter(i -> i > 0).drop(1).peekExceptionally(ex::set).next());
    assertInstanceOf(NullPointerException.class, ex.get());
  }

  @Test
  public void plus() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.<Integer>of().plus(1).plus(2).plus(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    itr = () -> Iterator.<Integer>of().plus(1).plus(null).plus(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());

    itr = () -> Iterator.of(1).plus(2).plus(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    itr = () -> Iterator.of(1).plus(null).plus(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());

    itr = () -> Iterator.of(1, 2).plus(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    itr = () -> Iterator.of(1, null).plus(3);
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());
  }

  @Test
  public void plusAll() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.<Integer>of().plusAll(Arrays.asList(1, 2, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    itr = () -> Iterator.<Integer>of().plusAll(List.of(1, null, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());

    itr = () -> Iterator.of(1).plusAll(new LinkedHashSet<>(List.of(2, 3)));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    itr = () -> Iterator.of(1).plusAll(List.of(null, 3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());

    itr = () -> Iterator.of(1, 2).plusAll(Set.of(3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, 2, 3), itr.get().toList());

    itr = () -> Iterator.of(1, null).plusAll(Set.of(3));
    assertFalse(itr.get().isEmpty());
    assertTrue(itr.get().notEmpty());
    assertEquals(3, itr.get().size());
    assertEquals(List.of(1, null, 3), itr.get().toList());
  }

  @Test
  public void reduceLeft() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, 4, 5);
    assertFalse(itr.get().reduceLeft(Integer::sum).isEmpty());
    assertEquals(1, itr.get().reduceLeft(Integer::sum).size());
    assertEquals(List.of(15), itr.get().reduceLeft(Integer::sum).toList());
    assertEquals(15, itr.get().reduceLeft(Integer::sum).first());

    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).reduceLeft(Integer::sum).first());

    assertTrue(Iterator.<Integer>of().reduceLeft(Integer::sum).isEmpty());
    assertEquals(0, Iterator.<Integer>of().reduceLeft(Integer::sum).size());
    assertEquals(List.of(), Iterator.<Integer>of().reduceLeft(Integer::sum).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().reduceLeft(Integer::sum).first());
  }

  @Test
  public void reduceRight() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, 4, 5);
    assertFalse(itr.get().reduceRight(Integer::sum).isEmpty());
    assertEquals(1, itr.get().reduceRight(Integer::sum).size());
    assertEquals(List.of(15), itr.get().reduceRight(Integer::sum).toList());
    assertEquals(15, itr.get().reduceRight(Integer::sum).first());

    assertThrows(NullPointerException.class,
        () -> itr.get().insert(null).reduceRight(Integer::sum).first());

    assertTrue(Iterator.<Integer>of().reduceRight(Integer::sum).isEmpty());
    assertEquals(0, Iterator.<Integer>of().reduceRight(Integer::sum).size());
    assertEquals(List.of(), Iterator.<Integer>of().reduceRight(Integer::sum).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().reduceRight(Integer::sum).first());
  }

  @Test
  public void removeAfter() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3);
    assertFalse(itr.get().removeAfter(5).isEmpty());
    assertEquals(3, itr.get().removeAfter(5).size());
    assertEquals(List.of(1, 2, 3), itr.get().removeAfter(5).toList());
    assertFalse(itr.get().removeAfter(3).isEmpty());
    assertEquals(3, itr.get().removeAfter(3).size());
    assertEquals(List.of(1, 2, 3), itr.get().removeAfter(3).toList());
    assertFalse(itr.get().removeAfter(2).isEmpty());
    assertEquals(2, itr.get().removeAfter(2).size());
    assertEquals(List.of(1, 2), itr.get().removeAfter(2).toList());
    assertFalse(itr.get().removeAfter(1).isEmpty());
    assertEquals(2, itr.get().removeAfter(1).size());
    assertEquals(List.of(1, 3), itr.get().removeAfter(1).toList());
    assertFalse(itr.get().removeAfter(0).isEmpty());
    assertEquals(2, itr.get().removeAfter(0).size());
    assertEquals(List.of(2, 3), itr.get().removeAfter(0).toList());
    assertFalse(itr.get().removeAfter(-7).isEmpty());
    assertEquals(3, itr.get().removeAfter(-7).size());
    assertEquals(List.of(1, 2, 3), itr.get().removeAfter(-7).toList());

    assertTrue(Iterator.of().removeAfter(5).isEmpty());
    assertEquals(0, Iterator.of().removeAfter(5).size());
    assertEquals(List.of(), Iterator.of().removeAfter(5).toList());

    Iterable<Object> iterable = () -> List.of().iterator();
    assertTrue(Iterator.wrap(iterable).removeAfter(5).isEmpty());
    assertEquals(0, Iterator.wrap(iterable).removeAfter(5).size());
    assertEquals(List.of(), Iterator.wrap(iterable).removeAfter(5).toList());
  }

  @Test
  public void removeEach() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 2);
    assertFalse(itr.get().removeEach(1).isEmpty());
    assertEquals(4, itr.get().removeEach(1).size());
    assertEquals(List.of(2, null, 4, 2), itr.get().removeEach(1).toList());
    assertNull(itr.get().removeEach(1).drop(1).first());
    assertFalse(itr.get().removeEach(null).isEmpty());
    assertEquals(4, itr.get().removeEach(null).size());
    assertEquals(List.of(1, 2, 4, 2), itr.get().removeEach(null).toList());
    assertEquals(4, itr.get().removeEach(null).drop(2).first());
    assertFalse(itr.get().removeEach(2).isEmpty());
    assertEquals(3, itr.get().removeEach(2).size());
    assertEquals(List.of(1, null, 4), itr.get().removeEach(2).toList());
    assertNull(itr.get().removeEach(2).drop(1).first());

    assertFalse(itr.get().removeEach(0).isEmpty());
    assertEquals(5, itr.get().removeEach(0).size());
    assertEquals(List.of(1, 2, null, 4, 2), itr.get().removeEach(0).toList());
    assertNull(itr.get().removeEach(0).drop(2).first());

    assertTrue(Iterator.of().removeEach(1).isEmpty());
    assertEquals(0, Iterator.of().removeEach(1).size());
    assertEquals(List.of(), Iterator.of().removeEach(1).toList());
  }

  @Test
  public void removeFirst() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 2);
    assertFalse(itr.get().removeFirst(1).isEmpty());
    assertEquals(4, itr.get().removeFirst(1).size());
    assertEquals(List.of(2, null, 4, 2), itr.get().removeFirst(1).toList());
    assertNull(itr.get().removeFirst(1).drop(1).first());
    assertFalse(itr.get().removeFirst(null).isEmpty());
    assertEquals(4, itr.get().removeFirst(null).size());
    assertEquals(List.of(1, 2, 4, 2), itr.get().removeFirst(null).toList());
    assertEquals(4, itr.get().removeFirst(null).drop(2).first());
    assertFalse(itr.get().removeFirst(2).isEmpty());
    assertEquals(4, itr.get().removeFirst(2).size());
    assertEquals(List.of(1, null, 4, 2), itr.get().removeFirst(2).toList());
    assertNull(itr.get().removeFirst(2).drop(1).first());

    assertFalse(itr.get().removeFirst(0).isEmpty());
    assertEquals(5, itr.get().removeFirst(0).size());
    assertEquals(List.of(1, 2, null, 4, 2), itr.get().removeFirst(0).toList());
    assertNull(itr.get().removeFirst(0).drop(2).first());

    assertTrue(Iterator.of().removeFirst(1).isEmpty());
    assertEquals(0, Iterator.of().removeFirst(1).size());
    assertEquals(List.of(), Iterator.of().removeFirst(1).toList());
  }

  @Test
  public void removeFirstWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 2);
    assertFalse(itr.get().removeFirstWhere(i -> i == 1).isEmpty());
    assertEquals(4, itr.get().removeFirstWhere(i -> i == 1).size());
    assertEquals(List.of(2, null, 4, 2), itr.get().removeFirstWhere(i -> i == 1).toList());
    assertNull(itr.get().removeFirstWhere(i -> i == 1).drop(1).first());
    assertFalse(itr.get().removeFirstWhere(Objects::isNull).isEmpty());
    assertEquals(4, itr.get().removeFirstWhere(Objects::isNull).size());
    assertEquals(List.of(1, 2, 4, 2), itr.get().removeFirstWhere(Objects::isNull).toList());
    assertEquals(4, itr.get().removeFirstWhere(Objects::isNull).drop(2).first());
    assertFalse(itr.get().removeFirstWhere(i -> i == 2).isEmpty());
    assertEquals(4, itr.get().removeFirstWhere(i -> i == 2).size());
    assertEquals(List.of(1, null, 4, 2), itr.get().removeFirstWhere(i -> i == 2).toList());
    assertNull(itr.get().removeFirstWhere(i -> i == 2).drop(1).first());

    assertFalse(itr.get().removeFirstWhere(i -> i > 1).isEmpty());
    assertEquals(4, itr.get().removeFirstWhere(i -> i > 1).size());
    assertEquals(List.of(1, null, 4, 2), itr.get().removeFirstWhere(i -> i > 1).toList());
    assertNull(itr.get().removeFirstWhere(i -> i > 1).drop(1).first());

    assertFalse(itr.get().removeFirstWhere(i -> false).isEmpty());
    assertEquals(5, itr.get().removeFirstWhere(i -> false).size());
    assertEquals(List.of(1, 2, null, 4, 2), itr.get().removeFirstWhere(i -> false).toList());
    assertNull(itr.get().removeFirstWhere(i -> false).drop(2).first());

    assertFalse(itr.get().removeFirstWhere(i -> i > 2).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().removeFirstWhere(i -> i > 2).size());
    assertEquals(2, itr.get().removeFirstWhere(i -> i > 2).drop(1).first());

    assertTrue(Iterator.<Integer>of().removeFirstWhere(i -> i == 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().removeFirstWhere(i -> i == 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().removeFirstWhere(i -> i == 1).toList());
  }

  @Test
  public void removeLast() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 2);
    assertFalse(itr.get().removeLast(1).isEmpty());
    assertEquals(4, itr.get().removeLast(1).size());
    assertEquals(List.of(2, null, 4, 2), itr.get().removeLast(1).toList());
    assertNull(itr.get().removeLast(1).drop(1).first());
    assertFalse(itr.get().removeLast(null).isEmpty());
    assertEquals(4, itr.get().removeLast(null).size());
    assertEquals(List.of(1, 2, 4, 2), itr.get().removeLast(null).toList());
    assertEquals(4, itr.get().removeLast(null).drop(2).first());
    assertFalse(itr.get().removeLast(2).isEmpty());
    assertEquals(4, itr.get().removeLast(2).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeLast(2).toList());
    assertNull(itr.get().removeLast(2).drop(2).first());

    assertFalse(itr.get().removeLast(0).isEmpty());
    assertEquals(5, itr.get().removeLast(0).size());
    assertEquals(List.of(1, 2, null, 4, 2), itr.get().removeLast(0).toList());
    assertNull(itr.get().removeLast(0).drop(2).first());

    assertTrue(Iterator.of().removeLast(1).isEmpty());
    assertEquals(0, Iterator.of().removeLast(1).size());
    assertEquals(List.of(), Iterator.of().removeLast(1).toList());
  }

  @Test
  public void removeLastWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4, 2);
    assertFalse(itr.get().removeLastWhere(Objects::isNull).isEmpty());
    assertEquals(4, itr.get().removeLastWhere(Objects::isNull).size());
    assertEquals(List.of(1, 2, 4, 2), itr.get().removeLastWhere(Objects::isNull).toList());
    assertEquals(4, itr.get().removeLastWhere(Objects::isNull).drop(2).first());
    assertFalse(itr.get().removeLastWhere(i -> i == 2).isEmpty());
    assertEquals(4, itr.get().removeLastWhere(i -> i == 2).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeLastWhere(i -> i == 2).toList());
    assertEquals(2, itr.get().removeLastWhere(i -> i == 2).drop(1).first());

    assertFalse(itr.get().removeLastWhere(i -> i > 2).isEmpty());
    assertEquals(4, itr.get().removeLastWhere(i -> i > 2).size());
    assertEquals(List.of(1, 2, null, 2), itr.get().removeLastWhere(i -> i > 2).toList());
    assertNull(itr.get().removeLastWhere(i -> i > 2).drop(2).first());

    assertFalse(itr.get().removeLastWhere(i -> false).isEmpty());
    assertEquals(5, itr.get().removeLastWhere(i -> false).size());
    assertEquals(List.of(1, 2, null, 4, 2), itr.get().removeLastWhere(i -> false).toList());
    assertNull(itr.get().removeLastWhere(i -> false).drop(2).first());

    assertThrows(NullPointerException.class, () -> itr.get().removeLastWhere(i -> i > 4).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().removeLastWhere(i -> i > 4).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().removeLastWhere(i -> i > 4).drop(1).first());

    assertTrue(Iterator.<Integer>of().removeLastWhere(i -> i == 1).isEmpty());
    assertEquals(0, Iterator.<Integer>of().removeLastWhere(i -> i == 1).size());
    assertEquals(List.of(), Iterator.<Integer>of().removeLastWhere(i -> i == 1).toList());
  }

  @Test
  public void removeSlice() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().removeSlice(1, 1).isEmpty());
    assertEquals(4, itr.get().removeSlice(1, 1).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeSlice(1, 1).toList());
    assertEquals(2, itr.get().removeSlice(1, 1).drop(1).first());
    assertFalse(itr.get().removeSlice(1, 0).isEmpty());
    assertEquals(4, itr.get().removeSlice(1, 0).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeSlice(1, 0).toList());
    assertEquals(2, itr.get().removeSlice(1, 0).drop(1).first());
    assertFalse(itr.get().removeSlice(1, -3).isEmpty());
    assertEquals(4, itr.get().removeSlice(1, -3).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeSlice(1, -3).toList());
    assertEquals(2, itr.get().removeSlice(1, -3).drop(1).first());
    assertFalse(itr.get().removeSlice(1, -4).isEmpty());
    assertEquals(4, itr.get().removeSlice(1, -4).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeSlice(1, -4).toList());
    assertEquals(2, itr.get().removeSlice(1, -4).drop(1).first());
    assertFalse(itr.get().removeSlice(1, -5).isEmpty());
    assertEquals(4, itr.get().removeSlice(1, -5).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeSlice(1, -5).toList());
    assertEquals(2, itr.get().removeSlice(1, -5).drop(1).first());
    assertFalse(itr.get().removeSlice(-1, 1).isEmpty());
    assertEquals(4, itr.get().removeSlice(-1, 1).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeSlice(-1, 1).toList());
    assertEquals(2, itr.get().removeSlice(-1, 1).drop(1).first());
    assertFalse(itr.get().removeSlice(-1, 3).isEmpty());
    assertEquals(4, itr.get().removeSlice(-1, 3).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeSlice(-1, 3).toList());
    assertEquals(2, itr.get().removeSlice(-1, 3).drop(1).first());
    assertFalse(itr.get().removeSlice(-1, -1).isEmpty());
    assertEquals(4, itr.get().removeSlice(-1, -1).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeSlice(-1, -1).toList());
    assertEquals(2, itr.get().removeSlice(-1, -1).drop(1).first());
    assertFalse(itr.get().removeSlice(-1, -4).isEmpty());
    assertEquals(4, itr.get().removeSlice(-1, -4).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeSlice(-1, -4).toList());
    assertEquals(2, itr.get().removeSlice(-1, -4).drop(1).first());

    assertFalse(itr.get().removeSlice(1, -1).isEmpty());
    assertEquals(2, itr.get().removeSlice(1, -1).size());
    assertEquals(List.of(1, 4), itr.get().removeSlice(1, -1).toList());
    assertEquals(4, itr.get().removeSlice(1, -1).drop(1).first());
    assertFalse(itr.get().removeSlice(1, -2).isEmpty());
    assertEquals(3, itr.get().removeSlice(1, -2).size());
    assertEquals(List.of(1, null, 4), itr.get().removeSlice(1, -2).toList());
    assertNull(itr.get().removeSlice(1, -2).drop(1).first());
    assertFalse(itr.get().removeSlice(1, 3).isEmpty());
    assertEquals(2, itr.get().removeSlice(1, 3).size());
    assertEquals(List.of(1, 4), itr.get().removeSlice(1, 3).toList());
    assertEquals(4, itr.get().removeSlice(1, 3).drop(1).first());
    assertFalse(itr.get().removeSlice(1, 2).isEmpty());
    assertEquals(3, itr.get().removeSlice(1, 2).size());
    assertEquals(List.of(1, null, 4), itr.get().removeSlice(1, 2).toList());
    assertNull(itr.get().removeSlice(1, 2).drop(1).first());
    assertFalse(itr.get().removeSlice(-1, 4).isEmpty());
    assertEquals(3, itr.get().removeSlice(-1, 4).size());
    assertEquals(List.of(1, 2, null), itr.get().removeSlice(-1, 4).toList());
    assertEquals(2, itr.get().removeSlice(-1, 4).drop(1).first());
    assertFalse(itr.get().removeSlice(-2, -1).isEmpty());
    assertEquals(3, itr.get().removeSlice(-2, -1).size());
    assertEquals(List.of(1, 2, 4), itr.get().removeSlice(-2, -1).toList());
    assertEquals(2, itr.get().removeSlice(-2, -1).drop(1).first());

    assertTrue(itr.get().removeSlice(0, Integer.MAX_VALUE).isEmpty());
    assertEquals(0, itr.get().removeSlice(0, Integer.MAX_VALUE).size());
    assertEquals(List.of(), itr.get().removeSlice(0, Integer.MAX_VALUE).toList());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().removeSlice(0, Integer.MAX_VALUE).drop(1).first());

    assertTrue(Iterator.of().removeSlice(1, -1).isEmpty());
    assertEquals(0, Iterator.of().removeSlice(1, -1).size());
    assertEquals(List.of(), Iterator.of().removeSlice(1, -1).toList());
    assertThrows(NoSuchElementException.class, () -> Iterator.of().removeSlice(1, -1).first());
  }

  @Test
  public void removeWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().removeWhere(i -> false).isEmpty());
    assertEquals(4, itr.get().removeWhere(i -> false).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().removeWhere(i -> false).toList());
    assertEquals(2, itr.get().removeWhere(i -> false).drop(1).first());
    assertTrue(itr.get().removeWhere(i -> true).isEmpty());
    assertEquals(0, itr.get().removeWhere(i -> true).size());
    assertEquals(List.of(), itr.get().removeWhere(i -> true).toList());
    assertThrows(NoSuchElementException.class, () -> itr.get().removeWhere(i -> true).first());

    assertFalse(itr.get().removeWhere(Objects::isNull).isEmpty());
    assertEquals(3, itr.get().removeWhere(Objects::isNull).size());
    assertEquals(List.of(1, 2, 4), itr.get().removeWhere(Objects::isNull).toList());
    assertEquals(2, itr.get().removeWhere(Objects::isNull).drop(1).first());
    assertFalse(itr.get().removeWhere(Objects::nonNull).isEmpty());
    assertEquals(1, itr.get().removeWhere(Objects::nonNull).size());
    assertEquals(List.of(null), itr.get().removeWhere(Objects::nonNull).toList());
    assertNull(itr.get().removeWhere(Objects::nonNull).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().removeWhere(Objects::nonNull).drop(1).first());

    assertTrue(Iterator.of().removeWhere(i -> false).isEmpty());
    assertEquals(0, Iterator.of().removeWhere(i -> false).size());
    assertEquals(List.of(), Iterator.of().removeWhere(i -> false).toList());
    assertThrows(NoSuchElementException.class, () -> Iterator.of().removeWhere(i -> false).first());

    assertFalse(itr.get().removeWhere(i -> i < 2).isEmpty());
    assertThrows(NullPointerException.class, () -> itr.get().removeWhere(i -> i < 2).size());
    assertEquals(2, itr.get().removeWhere(i -> i < 2).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().removeWhere(i -> i < 2).drop(1).first());
  }

  @Test
  public void replaceAfter() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null);
    assertFalse(itr.get().replaceAfter(-1, 4).isEmpty());
    assertEquals(3, itr.get().replaceAfter(-1, 4).size());
    assertEquals(List.of(1, 2, null), itr.get().replaceAfter(-1, 4).toList());
    assertEquals(2, itr.get().replaceAfter(-1, 4).drop(1).first());
    assertFalse(itr.get().replaceAfter(0, 4).isEmpty());
    assertEquals(3, itr.get().replaceAfter(0, 4).size());
    assertEquals(List.of(4, 2, null), itr.get().replaceAfter(0, 4).toList());
    assertEquals(2, itr.get().replaceAfter(0, 4).drop(1).first());
    assertFalse(itr.get().replaceAfter(1, 4).isEmpty());
    assertEquals(3, itr.get().replaceAfter(1, 4).size());
    assertEquals(List.of(1, 4, null), itr.get().replaceAfter(1, 4).toList());
    assertEquals(4, itr.get().replaceAfter(1, 4).drop(1).first());
    assertFalse(itr.get().replaceAfter(2, 4).isEmpty());
    assertEquals(3, itr.get().replaceAfter(2, 4).size());
    assertEquals(List.of(1, 2, 4), itr.get().replaceAfter(2, 4).toList());
    assertEquals(2, itr.get().replaceAfter(2, 4).drop(1).first());
    assertFalse(itr.get().replaceAfter(3, 4).isEmpty());
    assertEquals(3, itr.get().replaceAfter(3, 4).size());
    assertEquals(List.of(1, 2, null), itr.get().replaceAfter(3, 4).toList());
    assertEquals(2, itr.get().replaceAfter(3, 4).drop(1).first());

    assertTrue(Iterator.<Integer>of().replaceAfter(0, 4).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceAfter(0, 4).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceAfter(0, 4).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceAfter(0, 4).first());
  }

  @Test
  public void replaceEach() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null);
    assertFalse(itr.get().replaceEach(-1, 4).isEmpty());
    assertEquals(3, itr.get().replaceEach(-1, 4).size());
    assertEquals(List.of(1, 2, null), itr.get().replaceEach(-1, 4).toList());
    assertEquals(2, itr.get().replaceEach(-1, 4).drop(1).first());
    assertFalse(itr.get().replaceEach(0, 4).isEmpty());
    assertEquals(3, itr.get().replaceEach(0, 4).size());
    assertEquals(List.of(1, 2, null), itr.get().replaceEach(0, 4).toList());
    assertEquals(2, itr.get().replaceEach(0, 4).drop(1).first());
    assertFalse(itr.get().replaceEach(1, 4).isEmpty());
    assertEquals(3, itr.get().replaceEach(1, 4).size());
    assertEquals(List.of(4, 2, null), itr.get().replaceEach(1, 4).toList());
    assertEquals(2, itr.get().replaceEach(1, 4).drop(1).first());
    assertFalse(itr.get().replaceEach(2, 4).isEmpty());
    assertEquals(3, itr.get().replaceEach(2, 4).size());
    assertEquals(List.of(1, 4, null), itr.get().replaceEach(2, 4).toList());
    assertEquals(4, itr.get().replaceEach(2, 4).drop(1).first());
    assertFalse(itr.get().replaceEach(null, 4).isEmpty());
    assertEquals(3, itr.get().replaceEach(null, 4).size());
    assertEquals(List.of(1, 2, 4), itr.get().replaceEach(null, 4).toList());
    assertEquals(2, itr.get().replaceEach(null, 4).drop(1).first());

    assertFalse(itr.get().append(1).replaceEach(1, 4).isEmpty());
    assertEquals(4, itr.get().append(1).replaceEach(1, 4).size());
    assertEquals(List.of(4, 2, null, 4), itr.get().append(1).replaceEach(1, 4).toList());
    assertEquals(2, itr.get().append(1).replaceEach(1, 4).drop(1).first());

    assertTrue(Iterator.<Integer>of().replaceEach(0, 4).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceEach(0, 4).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceEach(0, 4).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceEach(0, 4).first());
  }

  @Test
  public void replaceFirst() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null);
    assertFalse(itr.get().replaceFirst(-1, 4).isEmpty());
    assertEquals(3, itr.get().replaceFirst(-1, 4).size());
    assertEquals(List.of(1, 2, null), itr.get().replaceFirst(-1, 4).toList());
    assertEquals(2, itr.get().replaceFirst(-1, 4).drop(1).first());
    assertFalse(itr.get().replaceFirst(0, 4).isEmpty());
    assertEquals(3, itr.get().replaceFirst(0, 4).size());
    assertEquals(List.of(1, 2, null), itr.get().replaceFirst(0, 4).toList());
    assertEquals(2, itr.get().replaceFirst(0, 4).drop(1).first());
    assertFalse(itr.get().replaceFirst(1, 4).isEmpty());
    assertEquals(3, itr.get().replaceFirst(1, 4).size());
    assertEquals(List.of(4, 2, null), itr.get().replaceFirst(1, 4).toList());
    assertEquals(2, itr.get().replaceFirst(1, 4).drop(1).first());
    assertFalse(itr.get().replaceFirst(2, 4).isEmpty());
    assertEquals(3, itr.get().replaceFirst(2, 4).size());
    assertEquals(List.of(1, 4, null), itr.get().replaceFirst(2, 4).toList());
    assertEquals(4, itr.get().replaceFirst(2, 4).drop(1).first());
    assertFalse(itr.get().replaceFirst(null, 4).isEmpty());
    assertEquals(3, itr.get().replaceFirst(null, 4).size());
    assertEquals(List.of(1, 2, 4), itr.get().replaceFirst(null, 4).toList());
    assertEquals(2, itr.get().replaceFirst(null, 4).drop(1).first());

    assertFalse(itr.get().append(1).replaceFirst(1, 4).isEmpty());
    assertEquals(4, itr.get().append(1).replaceFirst(1, 4).size());
    assertEquals(List.of(4, 2, null, 1), itr.get().append(1).replaceFirst(1, 4).toList());
    assertEquals(2, itr.get().append(1).replaceFirst(1, 4).drop(1).first());

    assertTrue(Iterator.<Integer>of().replaceFirst(0, 4).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceFirst(0, 4).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceFirst(0, 4).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceFirst(0, 4).first());
  }

  @Test
  public void replaceFirstWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().replaceFirstWhere(i -> false, 4).isEmpty());
    assertEquals(4, itr.get().replaceFirstWhere(i -> false, 4).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().replaceFirstWhere(i -> false, 4).toList());
    assertNull(itr.get().replaceFirstWhere(i -> false, 4).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceFirstWhere(i -> false, 4).drop(4).first());
    assertFalse(itr.get().replaceFirstWhere(i -> true, 4).isEmpty());
    assertEquals(4, itr.get().replaceFirstWhere(i -> true, 4).size());
    assertEquals(List.of(4, 2, null, 4), itr.get().replaceFirstWhere(i -> true, 4).toList());
    assertEquals(2, itr.get().replaceFirstWhere(i -> true, 4).drop(1).first());
    assertNull(itr.get().replaceFirstWhere(i -> true, 4).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceFirstWhere(i -> true, 4).drop(5).first());
    assertFalse(itr.get().replaceFirstWhere(Objects::isNull, 3).isEmpty());
    assertEquals(4, itr.get().replaceFirstWhere(Objects::isNull, 3).size());
    assertEquals(List.of(1, 2, 3, 4), itr.get().replaceFirstWhere(Objects::isNull, 3).toList());
    assertEquals(2, itr.get().replaceFirstWhere(Objects::isNull, 3).drop(1).first());
    assertEquals(3, itr.get().replaceFirstWhere(Objects::isNull, 3).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceFirstWhere(Objects::isNull, 3).drop(4).first());

    assertFalse(itr.get().replaceFirstWhere(i -> i == 1, 2).isEmpty());
    assertEquals(4, itr.get().replaceFirstWhere(i -> i == 1, 2).size());
    assertEquals(List.of(2, 2, null, 4), itr.get().replaceFirstWhere(i -> i == 1, 2).toList());
    assertEquals(2, itr.get().replaceFirstWhere(i -> i == 1, 2).drop(1).first());
    assertNull(itr.get().replaceFirstWhere(i -> i == 1, 2).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceFirstWhere(i -> i == 1, 2).drop(5).first());
    assertFalse(itr.get().replaceFirstWhere(i -> i > 2, 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceFirstWhere(i -> i > 2, 1).size());
    assertEquals(1, itr.get().replaceFirstWhere(i -> i > 2, 1).first());
    assertEquals(2, itr.get().replaceFirstWhere(i -> i > 2, 1).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceFirstWhere(i -> i > 2, 1).drop(2).first());

    assertTrue(Iterator.<Integer>of().replaceFirstWhere(i -> false, 4).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceFirstWhere(i -> false, 4).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceFirstWhere(i -> false, 4).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceFirstWhere(i -> false, 4).drop(2).first());
    assertTrue(Iterator.<Integer>of().replaceFirstWhere(i -> true, 4).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceFirstWhere(i -> true, 4).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceFirstWhere(i -> true, 4).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceFirstWhere(i -> true, 4).drop(2).first());
  }

  @Test
  public void replaceLast() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null);
    assertFalse(itr.get().replaceLast(-1, 4).isEmpty());
    assertEquals(3, itr.get().replaceLast(-1, 4).size());
    assertEquals(List.of(1, 2, null), itr.get().replaceLast(-1, 4).toList());
    assertEquals(2, itr.get().replaceLast(-1, 4).drop(1).first());
    assertFalse(itr.get().replaceLast(0, 4).isEmpty());
    assertEquals(3, itr.get().replaceLast(0, 4).size());
    assertEquals(List.of(1, 2, null), itr.get().replaceLast(0, 4).toList());
    assertEquals(2, itr.get().replaceLast(0, 4).drop(1).first());
    assertFalse(itr.get().replaceLast(1, 4).isEmpty());
    assertEquals(3, itr.get().replaceLast(1, 4).size());
    assertEquals(List.of(4, 2, null), itr.get().replaceLast(1, 4).toList());
    assertEquals(2, itr.get().replaceLast(1, 4).drop(1).first());
    assertFalse(itr.get().replaceLast(2, 4).isEmpty());
    assertEquals(3, itr.get().replaceLast(2, 4).size());
    assertEquals(List.of(1, 4, null), itr.get().replaceLast(2, 4).toList());
    assertEquals(4, itr.get().replaceLast(2, 4).drop(1).first());
    assertFalse(itr.get().replaceLast(null, 4).isEmpty());
    assertEquals(3, itr.get().replaceLast(null, 4).size());
    assertEquals(List.of(1, 2, 4), itr.get().replaceLast(null, 4).toList());
    assertEquals(2, itr.get().replaceLast(null, 4).drop(1).first());

    assertFalse(itr.get().append(1).replaceLast(1, 4).isEmpty());
    assertEquals(4, itr.get().append(1).replaceLast(1, 4).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().append(1).replaceLast(1, 4).toList());
    assertEquals(2, itr.get().append(1).replaceLast(1, 4).drop(1).first());

    assertTrue(Iterator.<Integer>of().replaceLast(0, 4).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceLast(0, 4).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceLast(0, 4).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceLast(0, 4).first());
  }

  @Test
  public void replaceLastWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().replaceLastWhere(i -> false, 5).isEmpty());
    assertEquals(4, itr.get().replaceLastWhere(i -> false, 5).size());
    assertEquals(List.of(1, 2, null, 4), itr.get().replaceLastWhere(i -> false, 5).toList());
    assertNull(itr.get().replaceLastWhere(i -> false, 5).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceLastWhere(i -> false, 5).drop(4).first());
    assertFalse(itr.get().replaceLastWhere(i -> true, 5).isEmpty());
    assertEquals(4, itr.get().replaceLastWhere(i -> true, 5).size());
    assertEquals(List.of(1, 2, null, 5), itr.get().replaceLastWhere(i -> true, 5).toList());
    assertEquals(2, itr.get().replaceLastWhere(i -> true, 5).drop(1).first());
    assertNull(itr.get().replaceLastWhere(i -> true, 5).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceLastWhere(i -> true, 5).drop(5).first());
    assertFalse(itr.get().replaceLastWhere(Objects::isNull, 3).isEmpty());
    assertEquals(4, itr.get().replaceLastWhere(Objects::isNull, 3).size());
    assertEquals(List.of(1, 2, 3, 4), itr.get().replaceLastWhere(Objects::isNull, 3).toList());
    assertEquals(2, itr.get().replaceLastWhere(Objects::isNull, 3).drop(1).first());
    assertEquals(3, itr.get().replaceLastWhere(Objects::isNull, 3).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceLastWhere(Objects::isNull, 3).drop(4).first());

    assertFalse(itr.get().replaceLastWhere(i -> i == 4, 5).isEmpty());
    assertEquals(4, itr.get().replaceLastWhere(i -> i == 4, 5).size());
    assertEquals(List.of(1, 2, null, 5), itr.get().replaceLastWhere(i -> i == 4, 5).toList());
    assertEquals(2, itr.get().replaceLastWhere(i -> i == 4, 5).drop(1).first());
    assertNull(itr.get().replaceLastWhere(i -> i == 4, 5).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceLastWhere(i -> i == 4, 5).drop(5).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceLastWhere(i -> i < 2, 1).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceLastWhere(i -> i < 2, 1).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().replaceLastWhere(i -> i < 2, 1).first());

    assertTrue(Iterator.<Integer>of().replaceLastWhere(i -> false, 5).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceLastWhere(i -> false, 5).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceLastWhere(i -> false, 5).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceLastWhere(i -> false, 5).drop(2).first());
    assertTrue(Iterator.<Integer>of().replaceLastWhere(i -> true, 5).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceLastWhere(i -> true, 5).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceLastWhere(i -> true, 5).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceLastWhere(i -> true, 5).drop(2).first());
  }

  @Test
  public void replaceSlice() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, null, 4);
    assertFalse(itr.get().replaceSlice(1, 1, List.of(5)).isEmpty());
    assertEquals(5, itr.get().replaceSlice(1, 1, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), itr.get().replaceSlice(1, 1, List.of(5)).toList());
    assertEquals(5, itr.get().replaceSlice(1, 1, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(1, 0, List.of(5)).isEmpty());
    assertEquals(5, itr.get().replaceSlice(1, 0, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), itr.get().replaceSlice(1, 0, List.of(5)).toList());
    assertEquals(5, itr.get().replaceSlice(1, 0, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(1, -3, List.of(5)).isEmpty());
    assertEquals(5, itr.get().replaceSlice(1, -3, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), itr.get().replaceSlice(1, -3, List.of(5)).toList());
    assertEquals(5, itr.get().replaceSlice(1, -3, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(1, -4, List.of(5)).isEmpty());
    assertEquals(5, itr.get().replaceSlice(1, -4, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), itr.get().replaceSlice(1, -4, List.of(5)).toList());
    assertEquals(5, itr.get().replaceSlice(1, -4, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(1, -5, List.of(5)).isEmpty());
    assertEquals(5, itr.get().replaceSlice(1, -5, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), itr.get().replaceSlice(1, -5, List.of(5)).toList());
    assertEquals(5, itr.get().replaceSlice(1, -5, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(-1, 1, List.of(5)).isEmpty());
    assertEquals(5, itr.get().replaceSlice(-1, 1, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5, 4), itr.get().replaceSlice(-1, 1, List.of(5)).toList());
    assertEquals(2, itr.get().replaceSlice(-1, 1, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(-1, 3, List.of(5)).isEmpty());
    assertEquals(5, itr.get().replaceSlice(-1, 3, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5, 4), itr.get().replaceSlice(-1, 3, List.of(5)).toList());
    assertEquals(2, itr.get().replaceSlice(-1, 3, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(-1, -1, List.of(5)).isEmpty());
    assertEquals(5, itr.get().replaceSlice(-1, -1, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5, 4), itr.get().replaceSlice(-1, -1, List.of(5)).toList());
    assertEquals(2, itr.get().replaceSlice(-1, -1, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(-1, -4, List.of(5)).isEmpty());
    assertEquals(5, itr.get().replaceSlice(-1, -4, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5, 4), itr.get().replaceSlice(-1, -4, List.of(5)).toList());
    assertEquals(2, itr.get().replaceSlice(-1, -4, List.of(5)).drop(1).first());

    assertFalse(itr.get().replaceSlice(1, -1, List.of(5)).isEmpty());
    assertEquals(3, itr.get().replaceSlice(1, -1, List.of(5)).size());
    assertEquals(List.of(1, 5, 4), itr.get().replaceSlice(1, -1, List.of(5)).toList());
    assertEquals(5, itr.get().replaceSlice(1, -1, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(1, -2, List.of(5)).isEmpty());
    assertEquals(4, itr.get().replaceSlice(1, -2, List.of(5)).size());
    assertEquals(List.of(1, 5, null, 4), itr.get().replaceSlice(1, -2, List.of(5)).toList());
    assertNull(itr.get().replaceSlice(1, -2, List.of(5)).drop(2).first());
    assertFalse(itr.get().replaceSlice(1, 3, List.of(5)).isEmpty());
    assertEquals(3, itr.get().replaceSlice(1, 3, List.of(5)).size());
    assertEquals(List.of(1, 5, 4), itr.get().replaceSlice(1, 3, List.of(5)).toList());
    assertEquals(4, itr.get().replaceSlice(1, 3, List.of(5)).drop(2).first());
    assertFalse(itr.get().replaceSlice(1, 2, List.of(5)).isEmpty());
    assertEquals(4, itr.get().replaceSlice(1, 2, List.of(5)).size());
    assertEquals(List.of(1, 5, null, 4), itr.get().replaceSlice(1, 2, List.of(5)).toList());
    assertNull(itr.get().replaceSlice(1, 2, List.of(5)).drop(2).first());
    assertFalse(itr.get().replaceSlice(-1, 4, List.of(5)).isEmpty());
    assertEquals(4, itr.get().replaceSlice(-1, 4, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5), itr.get().replaceSlice(-1, 4, List.of(5)).toList());
    assertEquals(2, itr.get().replaceSlice(-1, 4, List.of(5)).drop(1).first());
    assertFalse(itr.get().replaceSlice(-2, -1, List.of(5)).isEmpty());
    assertEquals(4, itr.get().replaceSlice(-2, -1, List.of(5)).size());
    assertEquals(List.of(1, 2, 5, 4), itr.get().replaceSlice(-2, -1, List.of(5)).toList());
    assertEquals(2, itr.get().replaceSlice(-2, -1, List.of(5)).drop(1).first());

    assertFalse(itr.get().replaceSlice(0, Integer.MAX_VALUE, List.of(5)).isEmpty());
    assertEquals(1, itr.get().replaceSlice(0, Integer.MAX_VALUE, List.of(5)).size());
    assertEquals(List.of(5), itr.get().replaceSlice(0, Integer.MAX_VALUE, List.of(5)).toList());
    assertEquals(5, itr.get().replaceSlice(0, Integer.MAX_VALUE, List.of(5)).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceSlice(0, Integer.MAX_VALUE, List.of(5)).drop(2).first());

    assertTrue(itr.get().replaceSlice(0, Integer.MAX_VALUE, List.of()).isEmpty());
    assertEquals(0, itr.get().replaceSlice(0, Integer.MAX_VALUE, List.of()).size());
    assertEquals(List.of(), itr.get().replaceSlice(0, Integer.MAX_VALUE, List.of()).toList());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceSlice(0, Integer.MAX_VALUE, List.of()).drop(1).first());

    assertFalse(Iterator.of().replaceSlice(0, 0, List.of(5)).isEmpty());
    assertEquals(1, Iterator.of().replaceSlice(0, 0, List.of(5)).size());
    assertEquals(List.of(5), Iterator.of().replaceSlice(0, 0, List.of(5)).toList());
    assertEquals(5, Iterator.of().replaceSlice(0, 0, List.of(5)).first());

    assertFalse(Iterator.of().replaceSlice(1, -1, List.of(5)).isEmpty());
    assertEquals(1, Iterator.of().replaceSlice(1, -1, List.of(5)).size());
    assertEquals(List.of(5), Iterator.of().replaceSlice(1, -1, List.of(5)).toList());
    assertEquals(5, Iterator.of().replaceSlice(1, -1, List.of(5)).first());
  }

  @Test
  public void replaceWhere() {
    Supplier<Iterator<Integer>> itr = () -> Iterator.of(1, 2, 3, 4);
    assertFalse(itr.get().replaceWhere(i -> false, 5).isEmpty());
    assertEquals(4, itr.get().replaceWhere(i -> false, 5).size());
    assertEquals(List.of(1, 2, 3, 4), itr.get().replaceWhere(i -> false, 5).toList());
    assertEquals(3, itr.get().replaceWhere(i -> false, 5).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceWhere(i -> false, 5).drop(4).first());
    assertFalse(itr.get().replaceWhere(i -> true, 5).isEmpty());
    assertEquals(4, itr.get().replaceWhere(i -> true, 5).size());
    assertEquals(List.of(5, 5, 5, 5), itr.get().replaceWhere(i -> true, 5).toList());
    assertEquals(5, itr.get().replaceWhere(i -> true, 5).drop(1).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceWhere(i -> true, 5).drop(5).first());
    assertFalse(itr.get().replaceWhere(i -> i == 2, 3).isEmpty());
    assertEquals(4, itr.get().replaceWhere(i -> i == 2, 3).size());
    assertEquals(List.of(1, 3, 3, 4), itr.get().replaceWhere(i -> i == 2, 3).toList());
    assertEquals(3, itr.get().replaceWhere(i -> i == 2, 3).drop(1).first());
    assertEquals(3, itr.get().replaceWhere(i -> i == 2, 3).drop(2).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().replaceWhere(i -> i == 2, 3).drop(4).first());

    assertFalse(itr.get().append(null).replaceWhere(i -> i == 4, 5).isEmpty());
    assertEquals(5, itr.get().append(null).replaceWhere(i -> i == 4, 5).size());
    assertEquals(2, itr.get().append(null).replaceWhere(i -> i == 4, 5).drop(1).first());
    assertEquals(3, itr.get().append(null).replaceWhere(i -> i == 4, 5).drop(2).first());
    assertEquals(5, itr.get().append(null).replaceWhere(i -> i == 4, 5).drop(3).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().append(null).replaceWhere(i -> i == 4, 5).drop(4).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().append(null).replaceWhere(i -> i == 4, 5).drop(5).first());

    assertTrue(Iterator.<Integer>of().replaceWhere(i -> false, 5).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceWhere(i -> false, 5).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceWhere(i -> false, 5).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceWhere(i -> false, 5).drop(2).first());
    assertTrue(Iterator.<Integer>of().replaceWhere(i -> true, 5).isEmpty());
    assertEquals(0, Iterator.<Integer>of().replaceWhere(i -> true, 5).size());
    assertEquals(List.of(), Iterator.<Integer>of().replaceWhere(i -> true, 5).toList());
    assertThrows(NoSuchElementException.class,
        () -> Iterator.<Integer>of().replaceWhere(i -> true, 5).drop(2).first());
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
  }
}
