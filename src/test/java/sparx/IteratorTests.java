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
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import sparx.Sparx.lazy.Iterator;
import sparx.Sparx.lazy.List;

public class IteratorTests {

  @Test
  public void all() {
    assertFalse(Iterator.of().all(Objects::nonNull).isEmpty());
    assertTrue(Iterator.of().all(Objects::nonNull).notEmpty());
    assertEquals(1, Iterator.of().all(Objects::nonNull).size());
    assertTrue(Iterator.of().all(Objects::nonNull).first());
    assertFalse(Iterator.of(1, 2, 3).all(i -> i < 3).first());
    assertTrue(Iterator.of(1, 2, 3).all(i -> i > 0).first());
    var itr = Iterator.of(1, null, 3).all(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);
  }

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
    assertFalse(Iterator.of().count(Objects::nonNull).isEmpty());
    assertTrue(Iterator.of().count(Objects::nonNull).notEmpty());
    assertEquals(1, Iterator.of().count(Objects::nonNull).size());
    assertEquals(0, Iterator.of().count(Objects::nonNull).first());
    assertEquals(2, Iterator.of(1, 2, 3).count(i -> i < 3).first());
    assertEquals(3, Iterator.of(1, 2, 3).count(i -> i > 0).first());
    var itr = Iterator.of(1, null, 3).count(i -> i > 0);
    assertThrows(NullPointerException.class, itr::first);
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
        () -> itr.get().flatMapWhere(i -> true, i -> List.of()).drop(0).first());
    assertFalse(itr.get().flatMapWhere(Objects::isNull, i -> List.of()).isEmpty());
    assertEquals(2, itr.get().flatMapWhere(Objects::isNull, i -> List.of()).size());
    assertEquals(List.of(1, 4), itr.get().flatMapWhere(Objects::isNull, i -> List.of()).toList());
    assertEquals(4, itr.get().flatMapWhere(Objects::isNull, i -> List.of()).drop(1).first());
    assertThrows(NoSuchElementException.class,
        () -> itr.get().flatMapWhere(Objects::isNull, i -> List.of()).drop(2).first());

    assertFalse(itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).isEmpty());
    assertEquals(1, itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).drop(0).first());
    assertEquals(1, itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).drop(1).first());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapWhere(i -> i == 1, i -> List.of(i, i)).drop(2).first());
    assertFalse(itr.get().flatMapWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> itr.get().flatMapWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, itr.get().flatMapWhere(i -> i > 2, i -> List.of(i, i)).drop(0).first());
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
}
