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
import org.junit.jupiter.api.Test;
import sparx.lazy.List;
import sparx.util.function.Function;
import sparx.util.function.Supplier;

public class LazyListTests {

  @Test
  public void append() {
    var l = List.<Integer>of().append(1).append(2).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.<Integer>of().append(1).append(null).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1).append(2).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1).append(null).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1, null).append(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
  }

  @Test
  public void appendAll() {
    var l = List.<Integer>of().appendAll(Arrays.asList(1, 2, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.<Integer>of().appendAll(List.of(1, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1).appendAll(new LinkedHashSet<>(List.of(2, 3)));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1).appendAll(List.of(null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2).appendAll(Set.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1, null).appendAll(Set.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
  }

  @Test
  public void count() {
    assertFalse(List.of().count().isEmpty());
    assertTrue(List.of().count().notEmpty());
    assertEquals(1, List.of().count().size());
    assertEquals(0, List.of().count().first());
    assertEquals(3, List.of(1, 2, 3).count().first());
    {
      var itr = List.of(1, 2, 3).count().iterator();
      assertTrue(itr.hasNext());
      assertEquals(3, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertEquals(3, List.of(1, null, 3).count().first());
    {
      var itr = List.of(1, null, 3).count().iterator();
      assertTrue(itr.hasNext());
      assertEquals(3, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
  }

  @Test
  public void countWhere() {
    assertFalse(List.of().count(Objects::nonNull).isEmpty());
    assertTrue(List.of().count(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().count(Objects::nonNull).size());
    assertEquals(0, List.of().count(Objects::nonNull).first());
    assertEquals(2, List.of(1, 2, 3).count(i -> i < 3).first());
    {
      var itr = List.of(1, 2, 3).count(i -> i < 3).iterator();
      assertTrue(itr.hasNext());
      assertEquals(2, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertEquals(3, List.of(1, 2, 3).count(i -> i > 0).first());
    {
      var itr = List.of(1, 2, 3).count(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertEquals(3, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).count(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
  }

  @Test
  public void diff() {
    assertEquals(List.of(2, 4), List.of(1, 2, null, 4).diff(List.of(1, null)));
    assertEquals(List.of(2, null), List.of(1, 2, null, 4).diff(List.of(1, 4)));
    assertEquals(List.of(2, null), List.of(1, 2, null, 4).diff(List.of(1, 3, 4)));
    assertEquals(List.of(2, null, 4), List.of(1, 2, null, 4).diff(List.of(3, 1, 3)));
    assertEquals(List.of(1, 2, 4), List.of(1, 2, null, 4).diff(List.of(null, null)));
    assertEquals(List.of(), List.of(1, null).diff(List.of(1, 2, null, 4)));

    assertEquals(List.of(1, 2, null, 4), List.of(1, 2, null, 4).diff(List.of()));
    assertEquals(List.of(), List.of().diff(List.of(1, 2, null, 4)));
  }

  @Test
  public void doFor() {
    var list = new ArrayList<>();
    List.of(1, 2, 3).doFor(e -> list.add(e));
    assertEquals(List.of(1, 2, 3), list);
  }

  @Test
  public void doWhile() {
    var list = new ArrayList<>();
    List.of(1, 2, 3).doWhile(e -> e < 3, list::add);
    assertEquals(List.of(1, 2), list);
    list.clear();
    List.of(1, 2, 3).doWhile(e -> {
      list.add(e);
      return e < 2;
    });
    assertEquals(List.of(1, 2), list);
  }

  @Test
  public void drop() {
    var l = List.<Integer>of().drop(1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().drop(0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().drop(-1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    l = List.of(1, null, 3).drop(1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(null, 3), l);
    l = List.of(1, null, 3).drop(2);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertEquals(List.of(3), l);
    l = List.of(1, null, 3).drop(3);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).drop(4);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).drop(0);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).drop(-1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
  }

  @Test
  public void dropRight() {
    var l = List.<Integer>of().dropRight(1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().dropRight(0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().dropRight(-1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    l = List.of(1, null, 3).dropRight(1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(1, null), l);
    l = List.of(1, null, 3).dropRight(2);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertEquals(List.of(1), l);
    l = List.of(1, null, 3).dropRight(3);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).dropRight(4);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).dropRight(0);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).dropRight(-1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
  }

  @Test
  public void dropRightWhile() {
    var l = List.<Integer>of().dropRightWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());

    l = List.of(1, null, 3).dropRightWhile(Objects::isNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).dropRightWhile(Objects::nonNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(1, null), l);
    l = List.of(1, null, 3).dropRightWhile(e -> e < 1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2, 3).dropRightWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).dropRightWhile(e -> e > 0).size());
  }

  @Test
  public void dropWhile() {
    var l = List.<Integer>of().dropWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());

    l = List.of(1, null, 3).dropWhile(Objects::isNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).dropWhile(Objects::nonNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(null, 3), l);
    l = List.of(1, null, 3).dropWhile(e -> e < 1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2, 3).dropWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).dropWhile(e -> e > 0).size());
  }

  @Test
  public void each() {
    assertFalse(List.of().each(Objects::nonNull).isEmpty());
    assertTrue(List.of().each(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().each(Objects::nonNull).size());
    assertFalse(List.of().each(Objects::nonNull).first());
    assertFalse(List.of(1, 2, 3).each(i -> i > 3).first());
    {
      var itr = List.of(1, 2, 3).each(i -> i < 3).iterator();
      assertTrue(itr.hasNext());
      assertFalse(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertTrue(List.of(1, 2, 3).each(i -> i > 0).first());
    {
      var itr = List.of(1, 2, 3).each(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertTrue(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).each(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
  }

  @Test
  public void endsWith() {
    var l = List.<Integer>of().endsWith(List.of());
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.<Integer>of().endsWith(List.of(1));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    l = List.of(1, null, 3).endsWith(List.of());
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).endsWith(List.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).endsWith(List.of(null));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    l = List.of(1, null, 3).endsWith(List.of(null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).endsWith(List.of(1, null));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    l = List.of(1, null, 3).endsWith(List.of(1, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).endsWith(List.of(null, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());
  }

  @Test
  public void exists() {
    assertFalse(List.of().exists(Objects::nonNull).isEmpty());
    assertTrue(List.of().exists(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().exists(Objects::nonNull).size());
    assertFalse(List.of().exists(Objects::nonNull).first());
    assertFalse(List.of(1, 2, 3).exists(i -> i > 3).first());
    {
      var itr = List.of(1, 2, 3).exists(i -> i > 3).iterator();
      assertTrue(itr.hasNext());
      assertFalse(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertTrue(List.of(1, 2, 3).exists(i -> i > 0).first());
    {
      var itr = List.of(1, 2, 3).exists(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertTrue(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).exists(i -> i > 1);
    assertThrows(NullPointerException.class, l::first);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
  }

  @Test
  public void filter() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.filter(Objects::nonNull).isEmpty());
    assertEquals(List.of(1, 2, 4), l.filter(Objects::nonNull));
    assertEquals(List.of(1, 2), l.filter(Objects::nonNull).filter(i -> i < 3));
    assertEquals(List.of(4), l.filter(Objects::nonNull).filter(i -> i > 3));
    assertEquals(List.of(), l.filter(Objects::nonNull).filter(i -> i > 4));
    assertThrows(NullPointerException.class, () -> l.filter(i -> i > 4).size());

    assertTrue(List.of().filter(Objects::isNull).isEmpty());
    assertEquals(0, List.of().filter(Objects::isNull).size());
  }

  @Test
  public void findAny() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findAny(Objects::isNull).isEmpty());
    assertEquals(1, l.findAny(Objects::isNull).size());
    assertEquals(List.of(null), l.findAny(Objects::isNull));
    assertFalse(l.findAny(i -> i < 4).isEmpty());
    assertEquals(1, l.findAny(i -> i < 4).size());

    assertTrue(List.of().findAny(Objects::isNull).isEmpty());
    assertEquals(0, List.of().findAny(Objects::isNull).size());
  }

  @Test
  public void findIndexOf() {
    var l = List.of(1, 2, null, 4);
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

    assertTrue(List.of().findIndexOf(null).isEmpty());
    assertEquals(0, List.of().findIndexOf(null).size());
  }

  @Test
  public void findIndexOfSlice() {
    var l = List.of(1, 2, null, 4);
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

    assertEquals(2, List.of(1, 1, 1, 1, 2, 1).findIndexOfSlice(List.of(1, 1, 2)).first());

    assertTrue(List.of().findIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(0, List.of().findIndexOfSlice(List.of(null)).size());
    assertFalse(List.of().findIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, List.of().findIndexOfSlice(List.of()).size());
    assertEquals(0, List.of().findIndexOfSlice(List.of()).first());
    assertEquals(List.of(0), List.of().findIndexOfSlice(List.of()));
  }

  @Test
  public void findIndexWhere() {
    var l = List.of(1, 2, null, 4);
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

    assertTrue(List.of().findIndexWhere(Objects::isNull).isEmpty());
    assertEquals(0, List.of().findIndexWhere(Objects::isNull).size());
  }

  @Test
  public void findLast() {
    var l = List.of(1, 2, null, 4, 5);
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

    assertTrue(List.of().findLast(Objects::isNull).isEmpty());
    assertEquals(0, List.of().findLast(Objects::isNull).size());
  }

  @Test
  public void findLastIndexOf() {
    var l = List.of(1, 2, null, 4);
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

    assertTrue(List.of().findLastIndexOf(null).isEmpty());
    assertEquals(0, List.of().findLastIndexOf(null).size());
  }

  @Test
  public void findLastIndexOfSlice() {
    var l = List.of(1, 2, null, 4);
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

    assertEquals(2, List.of(1, 1, 1, 1, 2, 1).findLastIndexOfSlice(List.of(1, 1, 2)).first());

    assertTrue(List.of().findLastIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(0, List.of().findLastIndexOfSlice(List.of(null)).size());
    assertFalse(List.of().findLastIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, List.of().findLastIndexOfSlice(List.of()).size());
    assertEquals(0, List.of().findLastIndexOfSlice(List.of()).first());
    assertEquals(List.of(0), List.of().findLastIndexOfSlice(List.of()));
  }

  @Test
  public void findLastIndexWhere() {
    var l = List.of(1, 2, null, 4);
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

    assertTrue(List.of().findLastIndexWhere(Objects::isNull).isEmpty());
    assertEquals(0, List.of().findLastIndexWhere(Objects::isNull).size());
  }

  @Test
  public void flatMap() {
    var l = List.of(1, 2);
    assertFalse(l.flatMap(i -> List.of(i, i)).isEmpty());
    assertEquals(4, l.flatMap(i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, 2, 2), l.flatMap(i -> List.of(i, i)));
    assertEquals(2, l.flatMap(i -> List.of(i, i)).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.flatMap(i -> List.of(i, i)).get(4));
    assertTrue(l.flatMap(i -> List.of()).isEmpty());
    assertEquals(0, l.flatMap(i -> List.of()).size());
    assertEquals(List.of(), l.flatMap(i -> List.of()));
    assertThrows(IndexOutOfBoundsException.class, () -> l.flatMap(i -> List.of()).first());
    assertFalse(l.flatMap(i -> List.of(null)).isEmpty());
    assertEquals(2, l.flatMap(i -> List.of(null)).size());
    assertEquals(List.of(null, null), l.flatMap(i -> List.of(null)));
    assertNull(l.flatMap(i -> List.of(null)).get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.flatMap(i -> List.of(null)).get(2));
  }

  @Test
  public void flatMapAfter() {
    var l = List.of(1, 2);
    assertFalse(l.flatMapAfter(-1, i -> List.of(i, i)).isEmpty());
    assertEquals(2, l.flatMapAfter(-1, i -> List.of(i, i)).size());
    assertEquals(l, l.flatMapAfter(-1, i -> List.of(i, i)));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapAfter(2, i -> List.of(i, i)).get(2));
    assertFalse(l.flatMapAfter(0, i -> List.of(i, i)).isEmpty());
    assertEquals(3, l.flatMapAfter(0, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, 2), l.flatMapAfter(0, i -> List.of(i, i)));
    assertEquals(2, l.flatMapAfter(0, i -> List.of(i, i)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapAfter(0, i -> List.of(i, i)).get(3));
    assertFalse(l.flatMapAfter(1, i -> List.of(i, i)).isEmpty());
    assertEquals(3, l.flatMapAfter(1, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2, 2), l.flatMapAfter(1, i -> List.of(i, i)));
    assertEquals(2, l.flatMapAfter(1, i -> List.of(i, i)).get(2));
    assertFalse(l.flatMapAfter(2, i -> List.of(i, i)).isEmpty());
    assertEquals(2, l.flatMapAfter(2, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2), l.flatMapAfter(2, i -> List.of(i, i)));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapAfter(2, i -> List.of(i, i)).get(2));

    assertFalse(l.flatMapAfter(0, i -> List.of()).isEmpty());
    assertEquals(1, l.flatMapAfter(0, i -> List.of()).size());
    assertEquals(List.of(2), l.flatMapAfter(0, i -> List.of()));
    assertEquals(2, l.flatMapAfter(0, i -> List.of()).get(0));
    assertThrows(IndexOutOfBoundsException.class, () -> l.flatMapAfter(0, i -> List.of()).get(1));
    assertFalse(l.flatMapAfter(1, i -> List.of()).isEmpty());
    assertEquals(1, l.flatMapAfter(1, i -> List.of()).size());
    assertEquals(List.of(1), l.flatMapAfter(1, i -> List.of()));
    assertEquals(1, l.flatMapAfter(1, i -> List.of()).get(0));
    assertThrows(IndexOutOfBoundsException.class, () -> l.flatMapAfter(1, i -> List.of()).get(1));
    assertFalse(l.flatMapAfter(2, i -> List.of()).isEmpty());
    assertEquals(2, l.flatMapAfter(2, i -> List.of()).size());
    assertEquals(List.of(1, 2), l.flatMapAfter(2, i -> List.of()));
    assertThrows(IndexOutOfBoundsException.class, () -> l.flatMapAfter(2, i -> List.of()).get(2));

    assertTrue(List.of().flatMapAfter(0, i -> List.of(i, i)).isEmpty());
    assertEquals(0, List.of().flatMapAfter(0, i -> List.of(i, i)).size());
    assertEquals(List.of(), List.of().flatMapAfter(0, i -> List.of(i, i)));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.of().flatMapAfter(0, i -> List.of(i, i)).first());
  }

  @Test
  public void flatMapFirstWhere() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.flatMapFirstWhere(i -> false, i -> List.of(i, i)).isEmpty());
    assertEquals(4, l.flatMapFirstWhere(i -> false, i -> List.of(i, i)).size());
    assertEquals(l, l.flatMapFirstWhere(i -> false, i -> List.of(i, i)));
    assertNull(l.flatMapFirstWhere(i -> false, i -> List.of(i, i)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapFirstWhere(i -> false, i -> List.of(i, i)).get(4));
    assertFalse(l.flatMapFirstWhere(i -> true, i -> List.of(i, i)).isEmpty());
    assertEquals(5, l.flatMapFirstWhere(i -> true, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, 2, null, 4), l.flatMapFirstWhere(i -> true, i -> List.of(i, i)));
    assertEquals(1, l.flatMapFirstWhere(i -> true, i -> List.of(i, i)).get(1));
    assertNull(l.flatMapFirstWhere(i -> true, i -> List.of(i, i)).get(3));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapFirstWhere(i -> true, i -> List.of(i, i)).get(5));
    assertFalse(l.flatMapFirstWhere(Objects::isNull, i -> List.of(3)).isEmpty());
    assertEquals(4, l.flatMapFirstWhere(Objects::isNull, i -> List.of(3)).size());
    assertEquals(List.of(1, 2, 3, 4), l.flatMapFirstWhere(Objects::isNull, i -> List.of(3)));
    assertEquals(2, l.flatMapFirstWhere(Objects::isNull, i -> List.of(3)).get(1));
    assertEquals(3, l.flatMapFirstWhere(Objects::isNull, i -> List.of(3)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapFirstWhere(Objects::isNull, i -> List.of(3)).get(4));

    assertFalse(l.flatMapFirstWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(4, l.flatMapFirstWhere(i -> false, i -> List.of()).size());
    assertEquals(l, l.flatMapFirstWhere(i -> false, i -> List.of()));
    assertNull(l.flatMapFirstWhere(i -> false, i -> List.of()).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapFirstWhere(i -> false, i -> List.of()).get(4));
    assertFalse(l.flatMapFirstWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(3, l.flatMapFirstWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(2, null, 4), l.flatMapFirstWhere(i -> true, i -> List.of()));
    assertEquals(4, l.flatMapFirstWhere(i -> true, i -> List.of()).get(2));
    assertNull(l.flatMapFirstWhere(i -> true, i -> List.of()).get(1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapFirstWhere(i -> true, i -> List.of()).get(3));
    assertFalse(l.flatMapFirstWhere(Objects::isNull, i -> List.of()).isEmpty());
    assertEquals(3, l.flatMapFirstWhere(Objects::isNull, i -> List.of()).size());
    assertEquals(List.of(1, 2, 4), l.flatMapFirstWhere(Objects::isNull, i -> List.of()));
    assertEquals(2, l.flatMapFirstWhere(Objects::isNull, i -> List.of()).get(1));
    assertEquals(4, l.flatMapFirstWhere(Objects::isNull, i -> List.of()).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapFirstWhere(Objects::isNull, i -> List.of()).get(3));

    assertFalse(l.flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).isEmpty());
    assertEquals(5, l.flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, 2, null, 4), l.flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)));
    assertEquals(1, l.flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).get(1));
    assertNull(l.flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).get(3));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapFirstWhere(i -> i == 1, i -> List.of(i, i)).get(5));
    assertFalse(l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).size());
    assertEquals(1, l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(0));
    assertEquals(2, l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(1));
    assertThrows(NullPointerException.class,
        () -> l.flatMapFirstWhere(i -> i > 2, i -> List.of(i, i)).get(2));

    assertTrue(List.of().flatMapFirstWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(0, List.of().flatMapFirstWhere(i -> false, i -> List.of()).size());
    assertEquals(List.of(), List.of().flatMapFirstWhere(i -> false, i -> List.of()));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.of().flatMapFirstWhere(i -> false, i -> List.of()).get(2));
    assertTrue(List.of().flatMapFirstWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(0, List.of().flatMapFirstWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(), List.of().flatMapFirstWhere(i -> true, i -> List.of()));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.of().flatMapFirstWhere(i -> true, i -> List.of()).get(2));
  }

  @Test
  public void flatMapLastWhere() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.flatMapLastWhere(i -> false, i -> List.of(i, i)).isEmpty());
    assertEquals(4, l.flatMapLastWhere(i -> false, i -> List.of(i, i)).size());
    assertEquals(l, l.flatMapLastWhere(i -> false, i -> List.of(i, i)));
    assertNull(l.flatMapLastWhere(i -> false, i -> List.of(i, i)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapLastWhere(i -> false, i -> List.of(i, i)).get(4));
    assertFalse(l.flatMapLastWhere(i -> true, i -> List.of(i, i)).isEmpty());
    assertEquals(5, l.flatMapLastWhere(i -> true, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2, null, 4, 4), l.flatMapLastWhere(i -> true, i -> List.of(i, i)));
    assertEquals(2, l.flatMapLastWhere(i -> true, i -> List.of(i, i)).get(1));
    assertNull(l.flatMapLastWhere(i -> true, i -> List.of(i, i)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapLastWhere(i -> true, i -> List.of(i, i)).get(5));
    assertFalse(l.flatMapLastWhere(Objects::isNull, i -> List.of(3)).isEmpty());
    assertEquals(4, l.flatMapLastWhere(Objects::isNull, i -> List.of(3)).size());
    assertEquals(List.of(1, 2, 3, 4), l.flatMapLastWhere(Objects::isNull, i -> List.of(3)));
    assertEquals(2, l.flatMapLastWhere(Objects::isNull, i -> List.of(3)).get(1));
    assertEquals(3, l.flatMapLastWhere(Objects::isNull, i -> List.of(3)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapLastWhere(Objects::isNull, i -> List.of(3)).get(4));

    assertFalse(l.flatMapLastWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(4, l.flatMapLastWhere(i -> false, i -> List.of()).size());
    assertEquals(l, l.flatMapLastWhere(i -> false, i -> List.of()));
    assertNull(l.flatMapLastWhere(i -> false, i -> List.of()).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapLastWhere(i -> false, i -> List.of()).get(4));
    assertFalse(l.flatMapLastWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(3, l.flatMapLastWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(1, 2, null), l.flatMapLastWhere(i -> true, i -> List.of()));
    assertEquals(2, l.flatMapLastWhere(i -> true, i -> List.of()).get(1));
    assertNull(l.flatMapLastWhere(i -> true, i -> List.of()).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapLastWhere(i -> true, i -> List.of()).get(3));
    assertFalse(l.flatMapLastWhere(Objects::isNull, i -> List.of()).isEmpty());
    assertEquals(3, l.flatMapLastWhere(Objects::isNull, i -> List.of()).size());
    assertEquals(List.of(1, 2, 4), l.flatMapLastWhere(Objects::isNull, i -> List.of()));
    assertEquals(2, l.flatMapLastWhere(Objects::isNull, i -> List.of()).get(1));
    assertEquals(4, l.flatMapLastWhere(Objects::isNull, i -> List.of()).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapLastWhere(Objects::isNull, i -> List.of()).get(3));

    assertFalse(l.flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).isEmpty());
    assertEquals(5, l.flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 2, null, 4, 4), l.flatMapLastWhere(i -> i == 4, i -> List.of(i, i)));
    assertEquals(2, l.flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).get(1));
    assertNull(l.flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapLastWhere(i -> i == 4, i -> List.of(i, i)).get(5));
    assertFalse(l.flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).isEmpty());
    assertThrows(NullPointerException.class,
        () -> l.flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).size());
    assertThrows(NullPointerException.class,
        () -> l.flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).get(3));
    assertThrows(NullPointerException.class,
        () -> l.flatMapLastWhere(i -> i < 2, i -> List.of(i, i)).get(2));

    assertTrue(List.of().flatMapLastWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(0, List.of().flatMapLastWhere(i -> false, i -> List.of()).size());
    assertEquals(List.of(), List.of().flatMapLastWhere(i -> false, i -> List.of()));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.of().flatMapLastWhere(i -> false, i -> List.of()).get(2));
    assertTrue(List.of().flatMapLastWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(0, List.of().flatMapLastWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(), List.of().flatMapLastWhere(i -> true, i -> List.of()));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.of().flatMapLastWhere(i -> true, i -> List.of()).get(2));
  }

  @Test
  public void flatMapWhere() {
    var l = List.of(1, null, null, 4);
    assertFalse(l.flatMapWhere(i -> false, i -> List.of(i, i)).isEmpty());
    assertEquals(4, l.flatMapWhere(i -> false, i -> List.of(i, i)).size());
    assertEquals(l, l.flatMapWhere(i -> false, i -> List.of(i, i)));
    assertNull(l.flatMapWhere(i -> false, i -> List.of(i, i)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapWhere(i -> false, i -> List.of(i, i)).get(4));
    assertFalse(l.flatMapWhere(i -> true, i -> List.of(i, i)).isEmpty());
    assertEquals(8, l.flatMapWhere(i -> true, i -> List.of(i, i)).size());
    assertEquals(List.of(1, 1, null, null, null, null, 4, 4),
        l.flatMapWhere(i -> true, i -> List.of(i, i)));
    assertEquals(1, l.flatMapWhere(i -> true, i -> List.of(i, i)).get(1));
    assertNull(l.flatMapWhere(i -> true, i -> List.of(i, i)).get(4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapWhere(i -> true, i -> List.of(i, i)).get(8));
    assertFalse(l.flatMapWhere(Objects::isNull, i -> List.of(3)).isEmpty());
    assertEquals(4, l.flatMapWhere(Objects::isNull, i -> List.of(3)).size());
    assertEquals(List.of(1, 3, 3, 4), l.flatMapWhere(Objects::isNull, i -> List.of(3)));
    assertEquals(3, l.flatMapWhere(Objects::isNull, i -> List.of(3)).get(1));
    assertEquals(3, l.flatMapWhere(Objects::isNull, i -> List.of(3)).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapWhere(Objects::isNull, i -> List.of(3)).get(4));

    assertFalse(l.flatMapWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(4, l.flatMapWhere(i -> false, i -> List.of()).size());
    assertEquals(l, l.flatMapWhere(i -> false, i -> List.of()));
    assertNull(l.flatMapWhere(i -> false, i -> List.of()).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapWhere(i -> false, i -> List.of()).get(4));
    assertTrue(l.flatMapWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(0, l.flatMapWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(), l.flatMapWhere(i -> true, i -> List.of()));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapWhere(i -> true, i -> List.of()).get(0));
    assertFalse(l.flatMapWhere(Objects::isNull, i -> List.of()).isEmpty());
    assertEquals(2, l.flatMapWhere(Objects::isNull, i -> List.of()).size());
    assertEquals(List.of(1, 4), l.flatMapWhere(Objects::isNull, i -> List.of()));
    assertEquals(4, l.flatMapWhere(Objects::isNull, i -> List.of()).get(1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.flatMapWhere(Objects::isNull, i -> List.of()).get(2));

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

    assertTrue(List.of().flatMapWhere(i -> false, i -> List.of()).isEmpty());
    assertEquals(0, List.of().flatMapWhere(i -> false, i -> List.of()).size());
    assertEquals(List.of(), List.of().flatMapWhere(i -> false, i -> List.of()));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.of().flatMapWhere(i -> false, i -> List.of()).get(2));
    assertTrue(List.of().flatMapWhere(i -> true, i -> List.of()).isEmpty());
    assertEquals(0, List.of().flatMapWhere(i -> true, i -> List.of()).size());
    assertEquals(List.of(), List.of().flatMapWhere(i -> true, i -> List.of()));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.of().flatMapWhere(i -> true, i -> List.of()).get(2));
  }

  @Test
  public void foldLeft() {
    var l = List.of(1, 2, 3, 4, 5);
    assertFalse(l.foldLeft(1, Integer::sum).isEmpty());
    assertEquals(1, l.foldLeft(1, Integer::sum).size());
    assertEquals(List.of(16), l.foldLeft(1, Integer::sum));
    assertEquals(16, l.foldLeft(1, Integer::sum).get(0));

    assertEquals(List.of(1, 2), List.of(1, 2).foldLeft(List.of(), List::append).get(0));

    assertFalse(List.<Integer>of().foldLeft(1, Integer::sum).isEmpty());
    assertEquals(1, List.<Integer>of().foldLeft(1, Integer::sum).size());
    assertEquals(List.of(1), List.<Integer>of().foldLeft(1, Integer::sum));
    assertEquals(1, List.<Integer>of().foldLeft(1, Integer::sum).get(0));
    assertEquals(List.of(), List.of().foldLeft(List.of(), List::append).get(0));
  }

  @Test
  public void foldRight() {
    var l = List.of(1, 2, 3, 4, 5);
    assertFalse(l.foldRight(1, Integer::sum).isEmpty());
    assertEquals(1, l.foldRight(1, Integer::sum).size());
    assertEquals(List.of(16), l.foldRight(1, Integer::sum));
    assertEquals(16, l.foldRight(1, Integer::sum).get(0));

    assertEquals(List.of(2, 1), List.of(1, 2).foldRight(List.of(), (i, li) -> li.append(i)).get(0));

    assertFalse(List.<Integer>of().foldRight(1, Integer::sum).isEmpty());
    assertEquals(1, List.<Integer>of().foldRight(1, Integer::sum).size());
    assertEquals(List.of(1), List.<Integer>of().foldRight(1, Integer::sum));
    assertEquals(1, List.<Integer>of().foldRight(1, Integer::sum).get(0));
    assertEquals(List.of(), List.of().foldRight(List.of(), (i, li) -> li.append(i)).get(0));
  }

  @Test
  public void group() {
    var l = List.of(1, 2, 3, 4, 5);
    assertThrows(IllegalArgumentException.class, () -> l.group(0));
    assertFalse(l.group(1).isEmpty());
    assertEquals(5, l.group(1).size());
    assertEquals(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5)), l.group(1));
    assertEquals(List.of(3), l.group(1).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.group(1).get(5));
    assertFalse(l.group(2).isEmpty());
    assertEquals(3, l.group(2).size());
    assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), l.group(2));
    assertEquals(List.of(3, 4), l.group(2).get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.group(2).get(3));
    assertFalse(l.group(3).isEmpty());
    assertEquals(2, l.group(3).size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5)), l.group(3));
    assertEquals(List.of(4, 5), l.group(3).get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.group(3).get(2));
    assertFalse(l.group(10).isEmpty());
    assertEquals(1, l.group(10).size());
    assertEquals(List.of(List.of(1, 2, 3, 4, 5)), l.group(10));
    assertEquals(List.of(1, 2, 3, 4, 5), l.group(10).get(0));
    assertThrows(IndexOutOfBoundsException.class, () -> l.group(10).get(1));
  }

  @Test
  public void groupWithPadding() {
    var l = List.of(1, 2, 3, 4, 5);
    assertThrows(IllegalArgumentException.class, () -> l.group(0, null));
    assertFalse(l.group(1, null).isEmpty());
    assertEquals(5, l.group(1, null).size());
    assertEquals(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5)),
        l.group(1, null));
    assertEquals(List.of(3), l.group(1, null).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.group(1, null).get(5));
    assertFalse(l.group(2, null).isEmpty());
    assertEquals(3, l.group(2, null).size());
    assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5, null)), l.group(2, null));
    assertEquals(List.of(3, 4), l.group(2, null).get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.group(2, null).get(3));
    assertFalse(l.group(3, -1).isEmpty());
    assertEquals(2, l.group(3, -1).size());
    assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, -1)), l.group(3, -1));
    assertEquals(List.of(4, 5, -1), l.group(3, -1).get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.group(3, -1).get(2));
    assertFalse(l.group(10, -1).isEmpty());
    assertEquals(1, l.group(10, -1).size());
    assertEquals(List.of(List.of(1, 2, 3, 4, 5, -1, -1, -1, -1, -1)), l.group(10, -1));
    assertEquals(List.of(1, 2, 3, 4, 5, -1, -1, -1, -1, -1), l.group(10, -1).get(0));
    assertThrows(IndexOutOfBoundsException.class, () -> l.group(10, -1).get(1));
  }

  @Test
  public void includes() {
    var l = List.of(1, 2, 3, null, 5);
    assertFalse(l.includes(null).isEmpty());
    assertEquals(1, l.includes(null).size());
    assertTrue(l.includes(null).first());
    assertEquals(List.of(true), l.includes(null));
    assertFalse(l.includes(0).isEmpty());
    assertEquals(1, l.includes(0).size());
    assertFalse(l.includes(0).first());
    assertEquals(List.of(false), l.includes(0));
    assertFalse(List.of().includes(0).isEmpty());
    assertEquals(1, List.of().includes(0).size());
    assertFalse(List.of().includes(0).first());
    assertEquals(List.of(false), List.of().includes(null));
  }

  @Test
  public void includesAll() {
    var l = List.of(1, 2, 3, null, 5);
    assertFalse(l.includesAll(List.of(null, 1)).isEmpty());
    assertEquals(1, l.includesAll(List.of(null, 1)).size());
    assertTrue(l.includesAll(List.of(null, 1)).first());
    assertEquals(List.of(true), l.includesAll(List.of(null, 1)));
    assertFalse(l.includesAll(List.of(0, 1)).isEmpty());
    assertEquals(1, l.includesAll(List.of(0, 1)).size());
    assertFalse(l.includesAll(List.of(0, 1)).first());
    assertEquals(List.of(false), l.includesAll(List.of(0, 1)));
    assertFalse(l.includesAll(List.of()).isEmpty());
    assertEquals(1, l.includesAll(List.of()).size());
    assertTrue(l.includesAll(List.of()).first());
    assertEquals(List.of(true), l.includesAll(List.of()));
    assertFalse(List.of().includesAll(List.of(null, 1)).isEmpty());
    assertEquals(1, List.of().includesAll(List.of(null, 1)).size());
    assertFalse(List.of().includesAll(List.of(null, 1)).first());
    assertEquals(List.of(false), List.of().includesAll(List.of(null, 1)));
    assertFalse(List.of().includesAll(List.of()).isEmpty());
    assertEquals(1, List.of().includesAll(List.of()).size());
    assertTrue(List.of().includesAll(List.of()).first());
    assertEquals(List.of(true), List.of().includesAll(List.of()));
  }

  @Test
  public void includesSlice() {
    var l = List.of(1, 2, 3, null, 5);
    assertFalse(l.includesSlice(List.of(3, null)).isEmpty());
    assertEquals(1, l.includesSlice(List.of(3, null)).size());
    assertTrue(l.includesSlice(List.of(3, null)).first());
    assertEquals(List.of(true), l.includesSlice(List.of(3, null)));
    assertFalse(l.includesSlice(List.of(null, 3)).isEmpty());
    assertEquals(1, l.includesSlice(List.of(null, 3)).size());
    assertFalse(l.includesSlice(List.of(null, 3)).first());
    assertEquals(List.of(false), l.includesSlice(List.of(null, 3)));
    assertFalse(l.includesSlice(List.of()).isEmpty());
    assertEquals(1, l.includesSlice(List.of()).size());
    assertTrue(l.includesSlice(List.of()).first());
    assertEquals(List.of(true), l.includesSlice(List.of()));
    assertFalse(List.of().includesSlice(List.of(null, 1)).isEmpty());
    assertEquals(1, List.of().includesSlice(List.of(null, 1)).size());
    assertFalse(List.of().includesSlice(List.of(null, 1)).first());
    assertEquals(List.of(false), List.of().includesSlice(List.of(null, 1)));
    assertFalse(List.of().includesSlice(List.of()).isEmpty());
    assertEquals(1, List.of().includesSlice(List.of()).size());
    assertTrue(List.of().includesSlice(List.of()).first());
    assertEquals(List.of(true), List.of().includesSlice(List.of()));
  }

  @Test
  public void insertAfter() {
    var l = List.of(1, 2, 3);
    assertFalse(l.insertAfter(5, null).isEmpty());
    assertEquals(3, l.insertAfter(5, null).size());
    assertEquals(l, l.insertAfter(5, null));
    assertFalse(l.insertAfter(3, null).isEmpty());
    assertEquals(4, l.insertAfter(3, null).size());
    assertEquals(List.of(1, 2, 3, null), l.insertAfter(3, null));
    assertFalse(l.insertAfter(2, null).isEmpty());
    assertEquals(4, l.insertAfter(2, null).size());
    assertEquals(List.of(1, 2, null, 3), l.insertAfter(2, null));
    assertFalse(l.insertAfter(1, null).isEmpty());
    assertEquals(4, l.insertAfter(1, null).size());
    assertEquals(List.of(1, null, 2, 3), l.insertAfter(1, null));
    assertFalse(l.insertAfter(0, null).isEmpty());
    assertEquals(4, l.insertAfter(0, null).size());
    assertEquals(List.of(null, 1, 2, 3), l.insertAfter(0, null));
    assertFalse(l.insertAfter(-7, null).isEmpty());
    assertEquals(3, l.insertAfter(-7, null).size());
    assertEquals(l, l.insertAfter(-7, null));

    assertTrue(List.of().insertAfter(5, null).isEmpty());
    assertEquals(0, List.of().insertAfter(5, null).size());
    assertEquals(List.of(), List.of().insertAfter(5, null));
    assertFalse(List.of().insertAfter(0, null).isEmpty());
    assertEquals(1, List.of().insertAfter(0, null).size());
    assertEquals(List.of(null), List.of().insertAfter(0, null));

    Iterable<Object> iterable = () -> List.of().iterator();
    assertTrue(List.wrap(iterable).insertAfter(5, null).isEmpty());
    assertEquals(0, List.wrap(iterable).insertAfter(5, null).size());
    assertEquals(List.of(), List.wrap(iterable).insertAfter(5, null));
    assertFalse(List.wrap(iterable).insertAfter(0, null).isEmpty());
    assertEquals(1, List.wrap(iterable).insertAfter(0, null).size());
    assertEquals(List.of(null), List.wrap(iterable).insertAfter(0, null));
  }

  @Test
  public void insertAllAfter() {
    var l = List.of(1, 2, 3);
    assertFalse(l.insertAllAfter(5, List.of(null, 5)).isEmpty());
    assertEquals(3, l.insertAllAfter(5, List.of(null, 5)).size());
    assertEquals(l, l.insertAllAfter(5, List.of(null, 5)));
    assertFalse(l.insertAllAfter(3, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAfter(3, List.of(null, 5)).size());
    assertEquals(List.of(1, 2, 3, null, 5), l.insertAllAfter(3, List.of(null, 5)));
    assertFalse(l.insertAllAfter(2, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAfter(2, List.of(null, 5)).size());
    assertEquals(List.of(1, 2, null, 5, 3), l.insertAllAfter(2, List.of(null, 5)));
    assertFalse(l.insertAllAfter(1, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAfter(1, List.of(null, 5)).size());
    assertEquals(List.of(1, null, 5, 2, 3), l.insertAllAfter(1, List.of(null, 5)));
    assertFalse(l.insertAllAfter(0, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAfter(0, List.of(null, 5)).size());
    assertEquals(List.of(null, 5, 1, 2, 3), l.insertAllAfter(0, List.of(null, 5)));
    assertFalse(l.insertAllAfter(-7, List.of(null, 5)).isEmpty());
    assertEquals(3, l.insertAllAfter(-7, List.of(null, 5)).size());
    assertEquals(l, l.insertAllAfter(-7, List.of(null, 5)));

    assertTrue(List.of().insertAllAfter(5, List.of(null, 5)).isEmpty());
    assertEquals(0, List.of().insertAllAfter(5, List.of(null, 5)).size());
    assertEquals(List.of(), List.of().insertAllAfter(5, List.of(null, 5)));
    assertFalse(List.of().insertAllAfter(0, List.of(null, 5)).isEmpty());
    assertEquals(2, List.of().insertAllAfter(0, List.of(null, 5)).size());
    assertEquals(List.of(null, 5), List.of().insertAllAfter(0, List.of(null, 5)));

    Iterable<Object> iterable = () -> List.of().iterator();
    assertTrue(List.wrap(iterable).insertAllAfter(5, List.of(null, 5)).isEmpty());
    assertEquals(0, List.wrap(iterable).insertAllAfter(5, List.of(null, 5)).size());
    assertEquals(List.of(), List.wrap(iterable).insertAllAfter(5, List.of(null, 5)));
    assertFalse(List.wrap(iterable).insertAllAfter(0, List.of(null, 5)).isEmpty());
    assertEquals(2, List.wrap(iterable).insertAllAfter(0, List.of(null, 5)).size());
    assertEquals(List.of(null, 5), List.wrap(iterable).insertAllAfter(0, List.of(null, 5)));
  }

  @Test
  public void intersect() {
    assertEquals(List.of(1, null), List.of(1, 2, null, 4).intersect(List.of(1, null)));
    assertEquals(List.of(1, 4), List.of(1, 2, null, 4).intersect(List.of(1, 4)));
    assertEquals(List.of(1, 4), List.of(1, 2, null, 4).intersect(List.of(1, 3, 4)));
    assertEquals(List.of(1), List.of(1, 2, null, 4).intersect(List.of(3, 1, 3)));
    assertEquals(List.of(null), List.of(1, 2, null, 4).intersect(List.of(null, null)));
    assertEquals(List.of(1, null), List.of(1, null).intersect(List.of(1, 2, null, 4)));
    assertEquals(List.of(1, 2), List.of(1, 2, null, 4).intersect(List.of(2, 1)));
    assertEquals(List.of(), List.of(1, null).intersect(List.of(2, 4)));

    assertEquals(List.of(), List.of(1, 2, null, 4).intersect(List.of()));
    assertEquals(List.of(), List.of().intersect(List.of(1, 2, null, 4)));
  }

  @Test
  public void map() {
    var l = List.of(1, 2, 3);
    assertFalse(l.map(x -> x + 1).isEmpty());
    assertEquals(3, l.map(x -> x + 1).size());
    assertEquals(List.of(2, 3, 4), l.map(x -> x + 1));
    assertFalse(l.append(null).map(x -> x + 1).isEmpty());
    assertEquals(4, l.append(null).map(x -> x + 1).size());
    assertEquals(4, l.append(null).map(x -> x + 1).get(2));
    assertEquals(2, l.append(null).map(x -> x + 1).get(0));
    assertThrows(NullPointerException.class, () -> l.append(null).map(x -> x + 1).get(3));

    assertTrue(List.<Integer>of().map(x -> x + 1).isEmpty());
    assertEquals(0, List.<Integer>of().map(x -> x + 1).size());
    assertEquals(List.of(), List.<Integer>of().map(x -> x + 1));
  }

  @Test
  public void mapAfter() {
    var l = List.of(1, 2, 3);
    assertFalse(l.mapAfter(-1, x -> x + 1).isEmpty());
    assertEquals(3, l.mapAfter(-1, x -> x + 1).size());
    assertEquals(List.of(1, 2, 3), l.mapAfter(-1, x -> x + 1));
    assertEquals(2, l.mapAfter(-1, x -> x + 1).get(1));
    assertFalse(l.mapAfter(0, x -> x + 1).isEmpty());
    assertEquals(3, l.mapAfter(0, x -> x + 1).size());
    assertEquals(List.of(2, 2, 3), l.mapAfter(0, x -> x + 1));
    assertEquals(2, l.mapAfter(0, x -> x + 1).get(1));
    assertFalse(l.mapAfter(1, x -> x + 1).isEmpty());
    assertEquals(3, l.mapAfter(1, x -> x + 1).size());
    assertEquals(List.of(1, 3, 3), l.mapAfter(1, x -> x + 1));
    assertEquals(3, l.mapAfter(1, x -> x + 1).get(1));
    assertFalse(l.mapAfter(2, x -> x + 1).isEmpty());
    assertEquals(3, l.mapAfter(2, x -> x + 1).size());
    assertEquals(List.of(1, 2, 4), l.mapAfter(2, x -> x + 1));
    assertEquals(2, l.mapAfter(2, x -> x + 1).get(1));
    assertFalse(l.mapAfter(3, x -> x + 1).isEmpty());
    assertEquals(3, l.mapAfter(3, x -> x + 1).size());
    assertEquals(List.of(1, 2, 3), l.mapAfter(3, x -> x + 1));
    assertEquals(2, l.mapAfter(3, x -> x + 1).get(1));

    assertFalse(l.append(null).mapAfter(-1, x -> x + 1).isEmpty());
    assertEquals(4, l.append(null).mapAfter(-1, x -> x + 1).size());
    assertEquals(List.of(1, 2, 3, null), l.append(null).mapAfter(-1, x -> x + 1));
    assertEquals(2, l.append(null).mapAfter(-1, x -> x + 1).get(1));
    assertFalse(l.append(null).mapAfter(1, x -> x + 1).isEmpty());
    assertEquals(4, l.append(null).mapAfter(1, x -> x + 1).size());
    assertEquals(List.of(1, 3, 3, null), l.append(null).mapAfter(1, x -> x + 1));
    assertEquals(3, l.append(null).mapAfter(1, x -> x + 1).get(1));
    assertFalse(l.append(null).mapAfter(3, x -> x + 1).isEmpty());
    assertEquals(4, l.append(null).mapAfter(3, x -> x + 1).size());
    assertEquals(2, l.append(null).mapAfter(3, x -> x + 1).get(1));
    assertThrows(NullPointerException.class, () -> l.append(null).mapAfter(3, x -> x + 1).get(3));

    assertTrue(List.<Integer>of().mapAfter(0, x -> x + 1).isEmpty());
    assertEquals(0, List.<Integer>of().mapAfter(0, x -> x + 1).size());
    assertEquals(List.of(), List.<Integer>of().mapAfter(0, x -> x + 1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().mapAfter(0, x -> x + 1).get(0));
  }

  @Test
  public void mapFirstWhere() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.mapFirstWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(4, l.mapFirstWhere(i -> false, i -> i + 1).size());
    assertEquals(l, l.mapFirstWhere(i -> false, i -> i + 1));
    assertNull(l.mapFirstWhere(i -> false, i -> i + 1).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.mapFirstWhere(i -> false, i -> i + 1).get(4));
    assertFalse(l.mapFirstWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(4, l.mapFirstWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(2, 2, null, 4), l.mapFirstWhere(i -> true, i -> i + 1));
    assertEquals(2, l.mapFirstWhere(i -> true, i -> i + 1).get(1));
    assertNull(l.mapFirstWhere(i -> true, i -> i + 1).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.mapFirstWhere(i -> true, i -> i + 1).get(5));
    assertFalse(l.mapFirstWhere(Objects::isNull, i -> 3).isEmpty());
    assertEquals(4, l.mapFirstWhere(Objects::isNull, i -> 3).size());
    assertEquals(List.of(1, 2, 3, 4), l.mapFirstWhere(Objects::isNull, i -> 3));
    assertEquals(2, l.mapFirstWhere(Objects::isNull, i -> 3).get(1));
    assertEquals(3, l.mapFirstWhere(Objects::isNull, i -> 3).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.mapFirstWhere(Objects::isNull, i -> 3).get(4));

    assertFalse(l.mapFirstWhere(i -> i == 1, i -> i + 1).isEmpty());
    assertEquals(4, l.mapFirstWhere(i -> i == 1, i -> i + 1).size());
    assertEquals(List.of(2, 2, null, 4), l.mapFirstWhere(i -> i == 1, i -> i + 1));
    assertEquals(2, l.mapFirstWhere(i -> i == 1, i -> i + 1).get(1));
    assertNull(l.mapFirstWhere(i -> i == 1, i -> i + 1).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.mapFirstWhere(i -> i == 1, i -> i + 1).get(5));
    assertFalse(l.mapFirstWhere(i -> i > 2, i -> 1).isEmpty());
    assertEquals(4, l.mapFirstWhere(i -> i > 2, i -> 1).size());
    assertEquals(1, l.mapFirstWhere(i -> i > 2, i -> 1).get(0));
    assertEquals(2, l.mapFirstWhere(i -> i > 2, i -> 1).get(1));
    assertThrows(NullPointerException.class, () -> l.mapFirstWhere(i -> i > 2, i -> 1).get(2));

    assertTrue(List.<Integer>of().mapFirstWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(0, List.<Integer>of().mapFirstWhere(i -> false, i -> i + 1).size());
    assertEquals(List.of(), List.<Integer>of().mapFirstWhere(i -> false, i -> i + 1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().mapFirstWhere(i -> false, i -> i + 1).get(2));
    assertTrue(List.<Integer>of().mapFirstWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(0, List.<Integer>of().mapFirstWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(), List.<Integer>of().mapFirstWhere(i -> true, i -> i + 1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().mapFirstWhere(i -> true, i -> i + 1).get(2));
  }

  @Test
  public void mapLastWhere() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.mapLastWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(4, l.mapLastWhere(i -> false, i -> i + 1).size());
    assertEquals(l, l.mapLastWhere(i -> false, i -> i + 1));
    assertNull(l.mapLastWhere(i -> false, i -> i + 1).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.mapLastWhere(i -> false, i -> i + 1).get(4));
    assertFalse(l.mapLastWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(4, l.mapLastWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(1, 2, null, 5), l.mapLastWhere(i -> true, i -> i + 1));
    assertEquals(2, l.mapLastWhere(i -> true, i -> i + 1).get(1));
    assertNull(l.mapLastWhere(i -> true, i -> i + 1).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.mapLastWhere(i -> true, i -> i + 1).get(5));
    assertFalse(l.mapLastWhere(Objects::isNull, i -> 3).isEmpty());
    assertEquals(4, l.mapLastWhere(Objects::isNull, i -> 3).size());
    assertEquals(List.of(1, 2, 3, 4), l.mapLastWhere(Objects::isNull, i -> 3));
    assertEquals(2, l.mapLastWhere(Objects::isNull, i -> 3).get(1));
    assertEquals(3, l.mapLastWhere(Objects::isNull, i -> 3).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.mapLastWhere(Objects::isNull, i -> 3).get(4));

    assertFalse(l.mapLastWhere(i -> i == 4, i -> i + 1).isEmpty());
    assertEquals(4, l.mapLastWhere(i -> i == 4, i -> i + 1).size());
    assertEquals(List.of(1, 2, null, 5), l.mapLastWhere(i -> i == 4, i -> i + 1));
    assertEquals(2, l.mapLastWhere(i -> i == 4, i -> i + 1).get(1));
    assertNull(l.mapLastWhere(i -> i == 4, i -> i + 1).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.mapLastWhere(i -> i == 4, i -> i + 1).get(5));
    assertFalse(l.mapLastWhere(i -> i < 2, i -> 1).isEmpty());
    assertEquals(4, l.mapLastWhere(i -> i < 2, i -> 1).size());
    assertThrows(NullPointerException.class, () -> l.mapLastWhere(i -> i < 2, i -> 1).get(0));

    assertTrue(List.<Integer>of().mapLastWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(0, List.<Integer>of().mapLastWhere(i -> false, i -> i + 1).size());
    assertEquals(List.of(), List.<Integer>of().mapLastWhere(i -> false, i -> i + 1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().mapLastWhere(i -> false, i -> i + 1).get(2));
    assertTrue(List.<Integer>of().mapLastWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(0, List.<Integer>of().mapLastWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(), List.<Integer>of().mapLastWhere(i -> true, i -> i + 1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().mapLastWhere(i -> true, i -> i + 1).get(2));
  }

  @Test
  public void mapWhere() {
    var l = List.of(1, 2, 3, 4);
    assertFalse(l.mapWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(4, l.mapWhere(i -> false, i -> i + 1).size());
    assertEquals(l, l.mapWhere(i -> false, i -> i + 1));
    assertEquals(3, l.mapWhere(i -> false, i -> i + 1).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.mapWhere(i -> false, i -> i + 1).get(4));
    assertFalse(l.mapWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(4, l.mapWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(2, 3, 4, 5), l.mapWhere(i -> true, i -> i + 1));
    assertEquals(3, l.mapWhere(i -> true, i -> i + 1).get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.mapWhere(i -> true, i -> i + 1).get(5));
    assertFalse(l.mapWhere(i -> i == 2, i -> 3).isEmpty());
    assertEquals(4, l.mapWhere(i -> i == 2, i -> 3).size());
    assertEquals(List.of(1, 3, 3, 4), l.mapWhere(i -> i == 2, i -> 3));
    assertEquals(3, l.mapWhere(i -> i == 2, i -> 3).get(1));
    assertEquals(3, l.mapWhere(i -> i == 2, i -> 3).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.mapWhere(i -> i == 2, i -> 3).get(4));

    assertFalse(l.append(null).mapWhere(i -> i == 4, i -> i + 1).isEmpty());
    assertEquals(5, l.append(null).mapWhere(i -> i == 4, i -> i + 1).size());
    assertEquals(2, l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(1));
    assertEquals(3, l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(2));
    assertEquals(5, l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(3));
    assertThrows(NullPointerException.class,
        () -> l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.append(null).mapWhere(i -> i == 4, i -> i + 1).get(5));

    assertTrue(List.<Integer>of().mapWhere(i -> false, i -> i + 1).isEmpty());
    assertEquals(0, List.<Integer>of().mapWhere(i -> false, i -> i + 1).size());
    assertEquals(List.of(), List.<Integer>of().mapWhere(i -> false, i -> i + 1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().mapWhere(i -> false, i -> i + 1).get(2));
    assertTrue(List.<Integer>of().mapWhere(i -> true, i -> i + 1).isEmpty());
    assertEquals(0, List.<Integer>of().mapWhere(i -> true, i -> i + 1).size());
    assertEquals(List.of(), List.<Integer>of().mapWhere(i -> true, i -> i + 1));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().mapWhere(i -> true, i -> i + 1).get(2));
  }

  @Test
  public void max() {
    var l = List.of(1, 4, 2, 3);
    assertFalse(l.max(Integer::compareTo).isEmpty());
    assertTrue(l.max(Integer::compareTo).notEmpty());
    assertEquals(1, l.max(Integer::compareTo).size());
    assertEquals(4, l.max(Integer::compareTo).first());
    assertEquals(List.of(4), l.max(Integer::compareTo));

    assertFalse(List.of(1, null).max(Integer::compareTo).isEmpty());
    assertTrue(List.of(1, null).max(Integer::compareTo).notEmpty());
    assertEquals(1, List.of(1, null).max(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> List.of(1, null).max(Integer::compareTo).first());

    assertTrue(List.<Integer>of().max(Integer::compareTo).isEmpty());
    assertFalse(List.<Integer>of().max(Integer::compareTo).notEmpty());
    assertEquals(0, List.<Integer>of().max(Integer::compareTo).size());
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().max(Integer::compareTo).first());
    assertEquals(List.of(), List.<Integer>of().max(Integer::compareTo));
  }

  @Test
  public void min() {
    var l = List.of(1, 4, 2, 3);
    assertFalse(l.min(Integer::compareTo).isEmpty());
    assertTrue(l.min(Integer::compareTo).notEmpty());
    assertEquals(1, l.min(Integer::compareTo).size());
    assertEquals(1, l.min(Integer::compareTo).first());
    assertEquals(List.of(1), l.min(Integer::compareTo));

    assertFalse(List.of(1, null).min(Integer::compareTo).isEmpty());
    assertTrue(List.of(1, null).min(Integer::compareTo).notEmpty());
    assertEquals(1, List.of(1, null).min(Integer::compareTo).size());
    assertThrows(NullPointerException.class,
        () -> List.of(1, null).min(Integer::compareTo).first());

    assertTrue(List.<Integer>of().min(Integer::compareTo).isEmpty());
    assertFalse(List.<Integer>of().min(Integer::compareTo).notEmpty());
    assertEquals(0, List.<Integer>of().min(Integer::compareTo).size());
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().min(Integer::compareTo).first());
    assertEquals(List.of(), List.<Integer>of().min(Integer::compareTo));
  }

  @Test
  public void none() {
    assertFalse(List.of().none(Objects::nonNull).isEmpty());
    assertTrue(List.of().none(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().none(Objects::nonNull).size());
    assertTrue(List.of().none(Objects::nonNull).first());
    assertFalse(List.of(1, 2, 3).none(i -> i < 3).first());
    {
      var itr = List.of(1, 2, 3).none(i -> i < 3).iterator();
      assertTrue(itr.hasNext());
      assertFalse(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertTrue(List.of(1, 2, 3).none(i -> i < 0).first());
    {
      var itr = List.of(1, 2, 3).none(i -> i < 0).iterator();
      assertTrue(itr.hasNext());
      assertTrue(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).none(i -> i < 0);
    assertThrows(NullPointerException.class, l::first);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
  }

  @Test
  public void notAll() {
    assertFalse(List.of().notAll(Objects::isNull).isEmpty());
    assertTrue(List.of().notAll(Objects::isNull).notEmpty());
    assertEquals(1, List.of().notAll(Objects::isNull).size());
    assertTrue(List.of().notAll(Objects::isNull).first());
    assertTrue(List.of(1, 2, 3).notAll(i -> i > 3).first());
    {
      var itr = List.of(1, 2, 3).notAll(i -> i > 3).iterator();
      assertTrue(itr.hasNext());
      assertTrue(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertFalse(List.of(1, 2, 3).notAll(i -> i > 0).first());
    {
      var itr = List.of(1, 2, 3).notAll(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertFalse(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).notAll(i -> i > 0);
    assertThrows(NullPointerException.class, l::first);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
  }

  @Test
  public void orElse() {
    assertFalse(List.of(1).orElse(List.of(2)).isEmpty());
    assertTrue(List.of(1).orElse(List.of(2)).notEmpty());
    assertEquals(1, List.of(1).orElse(List.of(2)).size());
    assertEquals(1, List.of(1).orElse(List.of(2)).first());
    assertEquals(List.of(1), List.of(1).orElse(List.of(2)));

    assertFalse(List.of(1).orElse(List.of()).isEmpty());
    assertTrue(List.of(1).orElse(List.of()).notEmpty());
    assertEquals(1, List.of(1).orElse(List.of()).size());
    assertEquals(1, List.of(1).orElse(List.of()).first());
    assertEquals(List.of(1), List.of(1).orElse(List.of()));

    assertFalse(List.of().orElse(List.of(2)).isEmpty());
    assertTrue(List.of().orElse(List.of(2)).notEmpty());
    assertEquals(1, List.of().orElse(List.of(2)).size());
    assertEquals(2, List.of().orElse(List.of(2)).first());
    assertEquals(List.of(2), List.of().orElse(List.of(2)));

    assertTrue(List.of().orElse(List.of()).isEmpty());
    assertFalse(List.of().orElse(List.of()).notEmpty());
    assertEquals(0, List.of().orElse(List.of()).size());
    assertThrows(IndexOutOfBoundsException.class, () -> List.of().orElse(List.of()).first());
    assertEquals(List.of(), List.of().orElse(List.of()));
  }

  @Test
  public void orElseGet() {
    Supplier<List<Integer>> supplier = () -> List.of(2);
    assertFalse(List.of(1).orElseGet(supplier).isEmpty());
    assertTrue(List.of(1).orElseGet(supplier).notEmpty());
    assertEquals(1, List.of(1).orElseGet(supplier).size());
    assertEquals(1, List.of(1).orElseGet(supplier).first());
    assertEquals(List.of(1), List.of(1).orElseGet(supplier));

    assertFalse(List.of(1).orElseGet(List::of).isEmpty());
    assertTrue(List.of(1).orElseGet(List::of).notEmpty());
    assertEquals(1, List.of(1).orElseGet(List::of).size());
    assertEquals(1, List.of(1).orElseGet(List::of).first());
    assertEquals(List.of(1), List.of(1).orElseGet(List::of));

    assertFalse(List.of().orElseGet(supplier).isEmpty());
    assertTrue(List.of().orElseGet(supplier).notEmpty());
    assertEquals(1, List.of().orElseGet(supplier).size());
    assertEquals(2, List.of().orElseGet(supplier).first());
    assertEquals(List.of(2), List.of().orElseGet(supplier));

    assertTrue(List.of().orElseGet(List::of).isEmpty());
    assertFalse(List.of().orElseGet(List::of).notEmpty());
    assertEquals(0, List.of().orElseGet(List::of).size());
    assertThrows(IndexOutOfBoundsException.class, () -> List.of().orElseGet(List::of).first());
    assertEquals(List.of(), List.of().orElseGet(List::of));

    Supplier<List<Integer>> throwing = () -> {
      throw new IllegalStateException();
    };
    assertFalse(List.of(1).orElseGet(throwing).isEmpty());
    assertTrue(List.of(1).orElseGet(throwing).notEmpty());
    assertEquals(1, List.of(1).orElseGet(throwing).size());
    assertEquals(1, List.of(1).orElseGet(throwing).first());
    assertEquals(List.of(1), List.of(1).orElseGet(throwing));

    assertThrows(IllegalStateException.class, () -> List.of().orElseGet(throwing).isEmpty());
    assertThrows(IllegalStateException.class, () -> List.of().orElseGet(throwing).notEmpty());
    assertThrows(IllegalStateException.class, () -> List.of().orElseGet(throwing).size());
    assertThrows(IllegalStateException.class, () -> List.of().orElseGet(throwing).first());
  }

  @Test
  public void plus() {
    var l = List.<Integer>of().plus(1).plus(2).plus(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.<Integer>of().plus(1).plus(null).plus(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1).plus(2).plus(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1).plus(null).plus(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2).plus(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1, null).plus(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
  }

  @Test
  public void plusAll() {
    var l = List.<Integer>of().plusAll(Arrays.asList(1, 2, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.<Integer>of().plusAll(List.of(1, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1).plusAll(new LinkedHashSet<>(List.of(2, 3)));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1).plusAll(List.of(null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2).plusAll(Set.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.of(1, null).plusAll(Set.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
  }

  @Test
  public void prepend() {
    var l = List.<Integer>of().prepend(1).prepend(2).prepend(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(3, 2, 1), l);

    l = List.<Integer>of().prepend(1).prepend(null).prepend(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(3, null, 1), l);

    l = List.of(1).prepend(2).prepend(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(3, 2, 1), l);

    l = List.of(1).prepend(null).prepend(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(3, null, 1), l);

    l = List.of(1, 2).prepend(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(3, 1, 2), l);

    l = List.of(1, null).prepend(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(3, 1, null), l);
  }

  @Test
  public void prependAll() {
    var l = List.<Integer>of().prependAll(Arrays.asList(1, 2, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    l = List.<Integer>of().prependAll(List.of(1, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1).prependAll(new LinkedHashSet<>(List.of(2, 3)));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(2, 3, 1), l);

    l = List.of(1).prependAll(List.of(null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(null, 3, 1), l);

    l = List.of(1, 2).prependAll(Set.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(3, 1, 2), l);

    l = List.of(1, null).prependAll(Set.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(3, 1, null), l);
  }

  @Test
  public void reduceLeft() {
    var l = List.of(1, 2, 3, 4, 5);
    assertFalse(l.reduceLeft(Integer::sum).isEmpty());
    assertEquals(1, l.reduceLeft(Integer::sum).size());
    assertEquals(List.of(15), l.reduceLeft(Integer::sum));
    assertEquals(15, l.reduceLeft(Integer::sum).get(0));

    assertThrows(NullPointerException.class, () -> l.append(null).reduceLeft(Integer::sum).first());

    assertTrue(List.<Integer>of().reduceLeft(Integer::sum).isEmpty());
    assertEquals(0, List.<Integer>of().reduceLeft(Integer::sum).size());
    assertEquals(List.of(), List.<Integer>of().reduceLeft(Integer::sum));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().reduceLeft(Integer::sum).get(0));
  }

  @Test
  public void reduceRight() {
    var l = List.of(1, 2, 3, 4, 5);
    assertFalse(l.reduceRight(Integer::sum).isEmpty());
    assertEquals(1, l.reduceRight(Integer::sum).size());
    assertEquals(List.of(15), l.reduceRight(Integer::sum));
    assertEquals(15, l.reduceRight(Integer::sum).get(0));

    assertThrows(NullPointerException.class,
        () -> l.prepend(null).reduceRight(Integer::sum).first());

    assertTrue(List.<Integer>of().reduceRight(Integer::sum).isEmpty());
    assertEquals(0, List.<Integer>of().reduceRight(Integer::sum).size());
    assertEquals(List.of(), List.<Integer>of().reduceRight(Integer::sum));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().reduceRight(Integer::sum).get(0));
  }

  @Test
  public void removeAfter() {
    var l = List.of(1, 2, 3);
    assertFalse(l.removeAfter(5).isEmpty());
    assertEquals(3, l.removeAfter(5).size());
    assertEquals(List.of(1, 2, 3), l.removeAfter(5));
    assertFalse(l.removeAfter(3).isEmpty());
    assertEquals(3, l.removeAfter(3).size());
    assertEquals(List.of(1, 2, 3), l.removeAfter(3));
    assertFalse(l.removeAfter(2).isEmpty());
    assertEquals(2, l.removeAfter(2).size());
    assertEquals(List.of(1, 2), l.removeAfter(2));
    assertFalse(l.removeAfter(1).isEmpty());
    assertEquals(2, l.removeAfter(1).size());
    assertEquals(List.of(1, 3), l.removeAfter(1));
    assertFalse(l.removeAfter(0).isEmpty());
    assertEquals(2, l.removeAfter(0).size());
    assertEquals(List.of(2, 3), l.removeAfter(0));
    assertFalse(l.removeAfter(-7).isEmpty());
    assertEquals(3, l.removeAfter(-7).size());
    assertEquals(List.of(1, 2, 3), l.removeAfter(-7));

    assertTrue(List.of().removeAfter(5).isEmpty());
    assertEquals(0, List.of().removeAfter(5).size());
    assertEquals(List.of(), List.of().removeAfter(5));

    Iterable<Object> iterable = () -> List.of().iterator();
    assertTrue(List.wrap(iterable).removeAfter(5).isEmpty());
    assertEquals(0, List.wrap(iterable).removeAfter(5).size());
    assertEquals(List.of(), List.wrap(iterable).removeAfter(5));
  }

  @Test
  public void removeEach() {
    var l = List.of(1, 2, null, 4, 2);
    assertFalse(l.removeEach(1).isEmpty());
    assertEquals(4, l.removeEach(1).size());
    assertEquals(List.of(2, null, 4, 2), l.removeEach(1));
    assertNull(l.removeEach(1).get(1));
    assertFalse(l.removeEach(null).isEmpty());
    assertEquals(4, l.removeEach(null).size());
    assertEquals(List.of(1, 2, 4, 2), l.removeEach(null));
    assertEquals(4, l.removeEach(null).get(2));
    assertFalse(l.removeEach(2).isEmpty());
    assertEquals(3, l.removeEach(2).size());
    assertEquals(List.of(1, null, 4), l.removeEach(2));
    assertNull(l.removeEach(2).get(1));

    assertFalse(l.removeEach(0).isEmpty());
    assertEquals(5, l.removeEach(0).size());
    assertEquals(l, l.removeEach(0));
    assertNull(l.removeEach(0).get(2));

    assertTrue(List.of().removeEach(1).isEmpty());
    assertEquals(0, List.of().removeEach(1).size());
    assertEquals(List.of(), List.of().removeEach(1));
  }

  @Test
  public void removeFirst() {
    var l = List.of(1, 2, null, 4, 2);
    assertFalse(l.removeFirst(1).isEmpty());
    assertEquals(4, l.removeFirst(1).size());
    assertEquals(List.of(2, null, 4, 2), l.removeFirst(1));
    assertNull(l.removeFirst(1).get(1));
    assertFalse(l.removeFirst(null).isEmpty());
    assertEquals(4, l.removeFirst(null).size());
    assertEquals(List.of(1, 2, 4, 2), l.removeFirst(null));
    assertEquals(4, l.removeFirst(null).get(2));
    assertFalse(l.removeFirst(2).isEmpty());
    assertEquals(4, l.removeFirst(2).size());
    assertEquals(List.of(1, null, 4, 2), l.removeFirst(2));
    assertNull(l.removeFirst(2).get(1));

    assertFalse(l.removeFirst(0).isEmpty());
    assertEquals(5, l.removeFirst(0).size());
    assertEquals(l, l.removeFirst(0));
    assertNull(l.removeFirst(0).get(2));

    assertTrue(List.of().removeFirst(1).isEmpty());
    assertEquals(0, List.of().removeFirst(1).size());
    assertEquals(List.of(), List.of().removeFirst(1));
  }

  @Test
  public void removeFirstWhere() {
    var l = List.of(1, 2, null, 4, 2);
    assertFalse(l.removeFirstWhere(i -> i == 1).isEmpty());
    assertEquals(4, l.removeFirstWhere(i -> i == 1).size());
    assertEquals(List.of(2, null, 4, 2), l.removeFirstWhere(i -> i == 1));
    assertNull(l.removeFirstWhere(i -> i == 1).get(1));
    assertFalse(l.removeFirstWhere(Objects::isNull).isEmpty());
    assertEquals(4, l.removeFirstWhere(Objects::isNull).size());
    assertEquals(List.of(1, 2, 4, 2), l.removeFirstWhere(Objects::isNull));
    assertEquals(4, l.removeFirstWhere(Objects::isNull).get(2));
    assertFalse(l.removeFirstWhere(i -> i == 2).isEmpty());
    assertEquals(4, l.removeFirstWhere(i -> i == 2).size());
    assertEquals(List.of(1, null, 4, 2), l.removeFirstWhere(i -> i == 2));
    assertNull(l.removeFirstWhere(i -> i == 2).get(1));

    assertFalse(l.removeFirstWhere(i -> i > 1).isEmpty());
    assertEquals(4, l.removeFirstWhere(i -> i > 1).size());
    assertEquals(List.of(1, null, 4, 2), l.removeFirstWhere(i -> i > 1));
    assertNull(l.removeFirstWhere(i -> i > 1).get(1));

    assertFalse(l.removeFirstWhere(i -> false).isEmpty());
    assertEquals(5, l.removeFirstWhere(i -> false).size());
    assertEquals(l, l.removeFirstWhere(i -> false));
    assertNull(l.removeFirstWhere(i -> false).get(2));

    assertThrows(NullPointerException.class, () -> l.removeFirstWhere(i -> i > 2).isEmpty());
    assertThrows(NullPointerException.class, () -> l.removeFirstWhere(i -> i > 2).size());
    assertEquals(2, l.removeFirstWhere(i -> i > 2).get(1));

    assertTrue(List.<Integer>of().removeFirstWhere(i -> i == 1).isEmpty());
    assertEquals(0, List.<Integer>of().removeFirstWhere(i -> i == 1).size());
    assertEquals(List.of(), List.<Integer>of().removeFirstWhere(i -> i == 1));
  }

  @Test
  public void removeLast() {
    var l = List.of(1, 2, null, 4, 2);
    assertFalse(l.removeLast(1).isEmpty());
    assertEquals(4, l.removeLast(1).size());
    assertEquals(List.of(2, null, 4, 2), l.removeLast(1));
    assertNull(l.removeLast(1).get(1));
    assertFalse(l.removeLast(null).isEmpty());
    assertEquals(4, l.removeLast(null).size());
    assertEquals(List.of(1, 2, 4, 2), l.removeLast(null));
    assertEquals(4, l.removeLast(null).get(2));
    assertFalse(l.removeLast(2).isEmpty());
    assertEquals(4, l.removeLast(2).size());
    assertEquals(List.of(1, 2, null, 4), l.removeLast(2));
    assertNull(l.removeLast(2).get(2));

    assertFalse(l.removeLast(0).isEmpty());
    assertEquals(5, l.removeLast(0).size());
    assertEquals(l, l.removeLast(0));
    assertNull(l.removeLast(0).get(2));

    assertTrue(List.of().removeLast(1).isEmpty());
    assertEquals(0, List.of().removeLast(1).size());
    assertEquals(List.of(), List.of().removeLast(1));
  }

  @Test
  public void removeLastWhere() {
    var l = List.of(1, 2, null, 4, 2);
    assertFalse(l.removeLastWhere(Objects::isNull).isEmpty());
    assertEquals(4, l.removeLastWhere(Objects::isNull).size());
    assertEquals(List.of(1, 2, 4, 2), l.removeLastWhere(Objects::isNull));
    assertEquals(4, l.removeLastWhere(Objects::isNull).get(2));
    assertFalse(l.removeLastWhere(i -> i == 2).isEmpty());
    assertEquals(4, l.removeLastWhere(i -> i == 2).size());
    assertEquals(List.of(1, 2, null, 4), l.removeLastWhere(i -> i == 2));
    assertEquals(2, l.removeLastWhere(i -> i == 2).get(1));

    assertFalse(l.removeLastWhere(i -> i > 2).isEmpty());
    assertEquals(4, l.removeLastWhere(i -> i > 2).size());
    assertEquals(List.of(1, 2, null, 2), l.removeLastWhere(i -> i > 2));
    assertNull(l.removeLastWhere(i -> i > 2).get(2));

    assertFalse(l.removeLastWhere(i -> false).isEmpty());
    assertEquals(5, l.removeLastWhere(i -> false).size());
    assertEquals(l, l.removeLastWhere(i -> false));
    assertNull(l.removeLastWhere(i -> false).get(2));

    assertThrows(NullPointerException.class, () -> l.removeLastWhere(i -> i > 4).isEmpty());
    assertThrows(NullPointerException.class, () -> l.removeLastWhere(i -> i > 4).size());
    assertThrows(NullPointerException.class, () -> l.removeLastWhere(i -> i > 4).get(1));

    assertTrue(List.<Integer>of().removeLastWhere(i -> i == 1).isEmpty());
    assertEquals(0, List.<Integer>of().removeLastWhere(i -> i == 1).size());
    assertEquals(List.of(), List.<Integer>of().removeLastWhere(i -> i == 1));
  }

  @Test
  public void removeSlice() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.removeSlice(1, 1).isEmpty());
    assertEquals(4, l.removeSlice(1, 1).size());
    assertEquals(List.of(1, 2, null, 4), l.removeSlice(1, 1));
    assertEquals(2, l.removeSlice(1, 1).get(1));
    assertFalse(l.removeSlice(1, 0).isEmpty());
    assertEquals(4, l.removeSlice(1, 0).size());
    assertEquals(List.of(1, 2, null, 4), l.removeSlice(1, 0));
    assertEquals(2, l.removeSlice(1, 0).get(1));
    assertFalse(l.removeSlice(1, -3).isEmpty());
    assertEquals(4, l.removeSlice(1, -3).size());
    assertEquals(List.of(1, 2, null, 4), l.removeSlice(1, -3));
    assertEquals(2, l.removeSlice(1, -3).get(1));
    assertFalse(l.removeSlice(1, -4).isEmpty());
    assertEquals(4, l.removeSlice(1, -4).size());
    assertEquals(List.of(1, 2, null, 4), l.removeSlice(1, -4));
    assertEquals(2, l.removeSlice(1, -4).get(1));
    assertFalse(l.removeSlice(1, -5).isEmpty());
    assertEquals(4, l.removeSlice(1, -5).size());
    assertEquals(List.of(1, 2, null, 4), l.removeSlice(1, -5));
    assertEquals(2, l.removeSlice(1, -5).get(1));
    assertFalse(l.removeSlice(-1, 1).isEmpty());
    assertEquals(4, l.removeSlice(-1, 1).size());
    assertEquals(List.of(1, 2, null, 4), l.removeSlice(-1, 1));
    assertEquals(2, l.removeSlice(-1, 1).get(1));
    assertFalse(l.removeSlice(-1, 3).isEmpty());
    assertEquals(4, l.removeSlice(-1, 3).size());
    assertEquals(List.of(1, 2, null, 4), l.removeSlice(-1, 3));
    assertEquals(2, l.removeSlice(-1, 3).get(1));
    assertFalse(l.removeSlice(-1, -1).isEmpty());
    assertEquals(4, l.removeSlice(-1, -1).size());
    assertEquals(List.of(1, 2, null, 4), l.removeSlice(-1, -1));
    assertEquals(2, l.removeSlice(-1, -1).get(1));
    assertFalse(l.removeSlice(-1, -4).isEmpty());
    assertEquals(4, l.removeSlice(-1, -4).size());
    assertEquals(List.of(1, 2, null, 4), l.removeSlice(-1, -4));
    assertEquals(2, l.removeSlice(-1, -4).get(1));

    assertFalse(l.removeSlice(1, -1).isEmpty());
    assertEquals(2, l.removeSlice(1, -1).size());
    assertEquals(List.of(1, 4), l.removeSlice(1, -1));
    assertEquals(4, l.removeSlice(1, -1).get(1));
    assertFalse(l.removeSlice(1, -2).isEmpty());
    assertEquals(3, l.removeSlice(1, -2).size());
    assertEquals(List.of(1, null, 4), l.removeSlice(1, -2));
    assertNull(l.removeSlice(1, -2).get(1));
    assertFalse(l.removeSlice(1, 3).isEmpty());
    assertEquals(2, l.removeSlice(1, 3).size());
    assertEquals(List.of(1, 4), l.removeSlice(1, 3));
    assertEquals(4, l.removeSlice(1, 3).get(1));
    assertFalse(l.removeSlice(1, 2).isEmpty());
    assertEquals(3, l.removeSlice(1, 2).size());
    assertEquals(List.of(1, null, 4), l.removeSlice(1, 2));
    assertNull(l.removeSlice(1, 2).get(1));
    assertFalse(l.removeSlice(-1, 4).isEmpty());
    assertEquals(3, l.removeSlice(-1, 4).size());
    assertEquals(List.of(1, 2, null), l.removeSlice(-1, 4));
    assertEquals(2, l.removeSlice(-1, 4).get(1));
    assertFalse(l.removeSlice(-2, -1).isEmpty());
    assertEquals(3, l.removeSlice(-2, -1).size());
    assertEquals(List.of(1, 2, 4), l.removeSlice(-2, -1));
    assertEquals(2, l.removeSlice(-2, -1).get(1));

    assertTrue(l.removeSlice(0, Integer.MAX_VALUE).isEmpty());
    assertEquals(0, l.removeSlice(0, Integer.MAX_VALUE).size());
    assertEquals(List.of(), l.removeSlice(0, Integer.MAX_VALUE));
    assertThrows(IndexOutOfBoundsException.class, () -> l.removeSlice(0, Integer.MAX_VALUE).get(1));

    assertTrue(List.of().removeSlice(1, -1).isEmpty());
    assertEquals(0, List.of().removeSlice(1, -1).size());
    assertEquals(List.of(), List.of().removeSlice(1, -1));
    assertThrows(IndexOutOfBoundsException.class, () -> List.of().removeSlice(1, -1).get(0));
  }

  @Test
  public void removeWhere() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.removeWhere(i -> false).isEmpty());
    assertEquals(4, l.removeWhere(i -> false).size());
    assertEquals(List.of(1, 2, null, 4), l.removeWhere(i -> false));
    assertEquals(2, l.removeWhere(i -> false).get(1));
    assertTrue(l.removeWhere(i -> true).isEmpty());
    assertEquals(0, l.removeWhere(i -> true).size());
    assertEquals(List.of(), l.removeWhere(i -> true));
    assertThrows(IndexOutOfBoundsException.class, () -> l.removeWhere(i -> true).get(0));

    assertFalse(l.removeWhere(Objects::isNull).isEmpty());
    assertEquals(3, l.removeWhere(Objects::isNull).size());
    assertEquals(List.of(1, 2, 4), l.removeWhere(Objects::isNull));
    assertEquals(2, l.removeWhere(Objects::isNull).get(1));
    assertFalse(l.removeWhere(Objects::nonNull).isEmpty());
    assertEquals(1, l.removeWhere(Objects::nonNull).size());
    assertEquals(List.of(null), l.removeWhere(Objects::nonNull));
    assertNull(l.removeWhere(Objects::nonNull).get(0));
    assertThrows(IndexOutOfBoundsException.class, () -> l.removeWhere(Objects::nonNull).get(1));

    assertTrue(List.of().removeWhere(i -> false).isEmpty());
    assertEquals(0, List.of().removeWhere(i -> false).size());
    assertEquals(List.of(), List.of().removeWhere(i -> false));
    assertThrows(IndexOutOfBoundsException.class, () -> List.of().removeWhere(i -> false).get(0));

    assertFalse(l.removeWhere(i -> i < 2).isEmpty());
    assertThrows(NullPointerException.class, () -> l.removeWhere(i -> i < 2).size());
    assertEquals(2, l.removeWhere(i -> i < 2).get(0));
    assertThrows(NullPointerException.class, () -> l.removeWhere(i -> i < 2).get(1));
  }

  @Test
  public void replaceAfter() {
    var l = List.of(1, 2, null);
    assertFalse(l.replaceAfter(-1, 4).isEmpty());
    assertEquals(3, l.replaceAfter(-1, 4).size());
    assertEquals(List.of(1, 2, null), l.replaceAfter(-1, 4));
    assertEquals(2, l.replaceAfter(-1, 4).get(1));
    assertFalse(l.replaceAfter(0, 4).isEmpty());
    assertEquals(3, l.replaceAfter(0, 4).size());
    assertEquals(List.of(4, 2, null), l.replaceAfter(0, 4));
    assertEquals(2, l.replaceAfter(0, 4).get(1));
    assertFalse(l.replaceAfter(1, 4).isEmpty());
    assertEquals(3, l.replaceAfter(1, 4).size());
    assertEquals(List.of(1, 4, null), l.replaceAfter(1, 4));
    assertEquals(4, l.replaceAfter(1, 4).get(1));
    assertFalse(l.replaceAfter(2, 4).isEmpty());
    assertEquals(3, l.replaceAfter(2, 4).size());
    assertEquals(List.of(1, 2, 4), l.replaceAfter(2, 4));
    assertEquals(2, l.replaceAfter(2, 4).get(1));
    assertFalse(l.replaceAfter(3, 4).isEmpty());
    assertEquals(3, l.replaceAfter(3, 4).size());
    assertEquals(List.of(1, 2, null), l.replaceAfter(3, 4));
    assertEquals(2, l.replaceAfter(3, 4).get(1));

    assertTrue(List.<Integer>of().replaceAfter(0, 4).isEmpty());
    assertEquals(0, List.<Integer>of().replaceAfter(0, 4).size());
    assertEquals(List.of(), List.<Integer>of().replaceAfter(0, 4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceAfter(0, 4).get(0));
  }

  @Test
  public void replaceEach() {
    var l = List.of(1, 2, null);
    assertFalse(l.replaceEach(-1, 4).isEmpty());
    assertEquals(3, l.replaceEach(-1, 4).size());
    assertEquals(List.of(1, 2, null), l.replaceEach(-1, 4));
    assertEquals(2, l.replaceEach(-1, 4).get(1));
    assertFalse(l.replaceEach(0, 4).isEmpty());
    assertEquals(3, l.replaceEach(0, 4).size());
    assertEquals(List.of(1, 2, null), l.replaceEach(0, 4));
    assertEquals(2, l.replaceEach(0, 4).get(1));
    assertFalse(l.replaceEach(1, 4).isEmpty());
    assertEquals(3, l.replaceEach(1, 4).size());
    assertEquals(List.of(4, 2, null), l.replaceEach(1, 4));
    assertEquals(2, l.replaceEach(1, 4).get(1));
    assertFalse(l.replaceEach(2, 4).isEmpty());
    assertEquals(3, l.replaceEach(2, 4).size());
    assertEquals(List.of(1, 4, null), l.replaceEach(2, 4));
    assertEquals(4, l.replaceEach(2, 4).get(1));
    assertFalse(l.replaceEach(null, 4).isEmpty());
    assertEquals(3, l.replaceEach(null, 4).size());
    assertEquals(List.of(1, 2, 4), l.replaceEach(null, 4));
    assertEquals(2, l.replaceEach(null, 4).get(1));

    assertFalse(l.append(1).replaceEach(1, 4).isEmpty());
    assertEquals(4, l.append(1).replaceEach(1, 4).size());
    assertEquals(List.of(4, 2, null, 4), l.append(1).replaceEach(1, 4));
    assertEquals(2, l.append(1).replaceEach(1, 4).get(1));

    assertTrue(List.<Integer>of().replaceEach(0, 4).isEmpty());
    assertEquals(0, List.<Integer>of().replaceEach(0, 4).size());
    assertEquals(List.of(), List.<Integer>of().replaceEach(0, 4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceEach(0, 4).get(0));
  }

  @Test
  public void replaceFirst() {
    var l = List.of(1, 2, null);
    assertFalse(l.replaceFirst(-1, 4).isEmpty());
    assertEquals(3, l.replaceFirst(-1, 4).size());
    assertEquals(List.of(1, 2, null), l.replaceFirst(-1, 4));
    assertEquals(2, l.replaceFirst(-1, 4).get(1));
    assertFalse(l.replaceFirst(0, 4).isEmpty());
    assertEquals(3, l.replaceFirst(0, 4).size());
    assertEquals(List.of(1, 2, null), l.replaceFirst(0, 4));
    assertEquals(2, l.replaceFirst(0, 4).get(1));
    assertFalse(l.replaceFirst(1, 4).isEmpty());
    assertEquals(3, l.replaceFirst(1, 4).size());
    assertEquals(List.of(4, 2, null), l.replaceFirst(1, 4));
    assertEquals(2, l.replaceFirst(1, 4).get(1));
    assertFalse(l.replaceFirst(2, 4).isEmpty());
    assertEquals(3, l.replaceFirst(2, 4).size());
    assertEquals(List.of(1, 4, null), l.replaceFirst(2, 4));
    assertEquals(4, l.replaceFirst(2, 4).get(1));
    assertFalse(l.replaceFirst(null, 4).isEmpty());
    assertEquals(3, l.replaceFirst(null, 4).size());
    assertEquals(List.of(1, 2, 4), l.replaceFirst(null, 4));
    assertEquals(2, l.replaceFirst(null, 4).get(1));

    assertFalse(l.append(1).replaceFirst(1, 4).isEmpty());
    assertEquals(4, l.append(1).replaceFirst(1, 4).size());
    assertEquals(List.of(4, 2, null, 1), l.append(1).replaceFirst(1, 4));
    assertEquals(2, l.append(1).replaceFirst(1, 4).get(1));

    assertTrue(List.<Integer>of().replaceFirst(0, 4).isEmpty());
    assertEquals(0, List.<Integer>of().replaceFirst(0, 4).size());
    assertEquals(List.of(), List.<Integer>of().replaceFirst(0, 4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceFirst(0, 4).get(0));
  }

  @Test
  public void replaceFirstWhere() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.replaceFirstWhere(i -> false, 4).isEmpty());
    assertEquals(4, l.replaceFirstWhere(i -> false, 4).size());
    assertEquals(l, l.replaceFirstWhere(i -> false, 4));
    assertNull(l.replaceFirstWhere(i -> false, 4).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceFirstWhere(i -> false, 4).get(4));
    assertFalse(l.replaceFirstWhere(i -> true, 4).isEmpty());
    assertEquals(4, l.replaceFirstWhere(i -> true, 4).size());
    assertEquals(List.of(4, 2, null, 4), l.replaceFirstWhere(i -> true, 4));
    assertEquals(2, l.replaceFirstWhere(i -> true, 4).get(1));
    assertNull(l.replaceFirstWhere(i -> true, 4).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceFirstWhere(i -> true, 4).get(5));
    assertFalse(l.replaceFirstWhere(Objects::isNull, 3).isEmpty());
    assertEquals(4, l.replaceFirstWhere(Objects::isNull, 3).size());
    assertEquals(List.of(1, 2, 3, 4), l.replaceFirstWhere(Objects::isNull, 3));
    assertEquals(2, l.replaceFirstWhere(Objects::isNull, 3).get(1));
    assertEquals(3, l.replaceFirstWhere(Objects::isNull, 3).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.replaceFirstWhere(Objects::isNull, 3).get(4));

    assertFalse(l.replaceFirstWhere(i -> i == 1, 2).isEmpty());
    assertEquals(4, l.replaceFirstWhere(i -> i == 1, 2).size());
    assertEquals(List.of(2, 2, null, 4), l.replaceFirstWhere(i -> i == 1, 2));
    assertEquals(2, l.replaceFirstWhere(i -> i == 1, 2).get(1));
    assertNull(l.replaceFirstWhere(i -> i == 1, 2).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceFirstWhere(i -> i == 1, 2).get(5));
    assertFalse(l.replaceFirstWhere(i -> i > 2, 1).isEmpty());
    assertEquals(4, l.replaceFirstWhere(i -> i > 2, 1).size());
    assertEquals(1, l.replaceFirstWhere(i -> i > 2, 1).get(0));
    assertEquals(2, l.replaceFirstWhere(i -> i > 2, 1).get(1));
    assertThrows(NullPointerException.class, () -> l.replaceFirstWhere(i -> i > 2, 1).get(2));

    assertTrue(List.<Integer>of().replaceFirstWhere(i -> false, 4).isEmpty());
    assertEquals(0, List.<Integer>of().replaceFirstWhere(i -> false, 4).size());
    assertEquals(List.of(), List.<Integer>of().replaceFirstWhere(i -> false, 4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceFirstWhere(i -> false, 4).get(2));
    assertTrue(List.<Integer>of().replaceFirstWhere(i -> true, 4).isEmpty());
    assertEquals(0, List.<Integer>of().replaceFirstWhere(i -> true, 4).size());
    assertEquals(List.of(), List.<Integer>of().replaceFirstWhere(i -> true, 4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceFirstWhere(i -> true, 4).get(2));
  }

  @Test
  public void replaceLast() {
    var l = List.of(1, 2, null);
    assertFalse(l.replaceLast(-1, 4).isEmpty());
    assertEquals(3, l.replaceLast(-1, 4).size());
    assertEquals(List.of(1, 2, null), l.replaceLast(-1, 4));
    assertEquals(2, l.replaceLast(-1, 4).get(1));
    assertFalse(l.replaceLast(0, 4).isEmpty());
    assertEquals(3, l.replaceLast(0, 4).size());
    assertEquals(List.of(1, 2, null), l.replaceLast(0, 4));
    assertEquals(2, l.replaceLast(0, 4).get(1));
    assertFalse(l.replaceLast(1, 4).isEmpty());
    assertEquals(3, l.replaceLast(1, 4).size());
    assertEquals(List.of(4, 2, null), l.replaceLast(1, 4));
    assertEquals(2, l.replaceLast(1, 4).get(1));
    assertFalse(l.replaceLast(2, 4).isEmpty());
    assertEquals(3, l.replaceLast(2, 4).size());
    assertEquals(List.of(1, 4, null), l.replaceLast(2, 4));
    assertEquals(4, l.replaceLast(2, 4).get(1));
    assertFalse(l.replaceLast(null, 4).isEmpty());
    assertEquals(3, l.replaceLast(null, 4).size());
    assertEquals(List.of(1, 2, 4), l.replaceLast(null, 4));
    assertEquals(2, l.replaceLast(null, 4).get(1));

    assertFalse(l.append(1).replaceLast(1, 4).isEmpty());
    assertEquals(4, l.append(1).replaceLast(1, 4).size());
    assertEquals(List.of(1, 2, null, 4), l.append(1).replaceLast(1, 4));
    assertEquals(2, l.append(1).replaceLast(1, 4).get(1));

    assertTrue(List.<Integer>of().replaceLast(0, 4).isEmpty());
    assertEquals(0, List.<Integer>of().replaceLast(0, 4).size());
    assertEquals(List.of(), List.<Integer>of().replaceLast(0, 4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceLast(0, 4).get(0));
  }

  @Test
  public void replaceLastWhere() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.replaceLastWhere(i -> false, 5).isEmpty());
    assertEquals(4, l.replaceLastWhere(i -> false, 5).size());
    assertEquals(l, l.replaceLastWhere(i -> false, 5));
    assertNull(l.replaceLastWhere(i -> false, 5).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceLastWhere(i -> false, 5).get(4));
    assertFalse(l.replaceLastWhere(i -> true, 5).isEmpty());
    assertEquals(4, l.replaceLastWhere(i -> true, 5).size());
    assertEquals(List.of(1, 2, null, 5), l.replaceLastWhere(i -> true, 5));
    assertEquals(2, l.replaceLastWhere(i -> true, 5).get(1));
    assertNull(l.replaceLastWhere(i -> true, 5).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceLastWhere(i -> true, 5).get(5));
    assertFalse(l.replaceLastWhere(Objects::isNull, 3).isEmpty());
    assertEquals(4, l.replaceLastWhere(Objects::isNull, 3).size());
    assertEquals(List.of(1, 2, 3, 4), l.replaceLastWhere(Objects::isNull, 3));
    assertEquals(2, l.replaceLastWhere(Objects::isNull, 3).get(1));
    assertEquals(3, l.replaceLastWhere(Objects::isNull, 3).get(2));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.replaceLastWhere(Objects::isNull, 3).get(4));

    assertFalse(l.replaceLastWhere(i -> i == 4, 5).isEmpty());
    assertEquals(4, l.replaceLastWhere(i -> i == 4, 5).size());
    assertEquals(List.of(1, 2, null, 5), l.replaceLastWhere(i -> i == 4, 5));
    assertEquals(2, l.replaceLastWhere(i -> i == 4, 5).get(1));
    assertNull(l.replaceLastWhere(i -> i == 4, 5).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceLastWhere(i -> i == 4, 5).get(5));
    assertFalse(l.replaceLastWhere(i -> i < 2, 1).isEmpty());
    assertEquals(4, l.replaceLastWhere(i -> i < 2, 1).size());
    assertThrows(NullPointerException.class, () -> l.replaceLastWhere(i -> i < 2, 1).get(0));

    assertTrue(List.<Integer>of().replaceLastWhere(i -> false, 5).isEmpty());
    assertEquals(0, List.<Integer>of().replaceLastWhere(i -> false, 5).size());
    assertEquals(List.of(), List.<Integer>of().replaceLastWhere(i -> false, 5));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceLastWhere(i -> false, 5).get(2));
    assertTrue(List.<Integer>of().replaceLastWhere(i -> true, 5).isEmpty());
    assertEquals(0, List.<Integer>of().replaceLastWhere(i -> true, 5).size());
    assertEquals(List.of(), List.<Integer>of().replaceLastWhere(i -> true, 5));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceLastWhere(i -> true, 5).get(2));
  }

  @Test
  public void replaceSlice() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.replaceSlice(1, 1, List.of(5)).isEmpty());
    assertEquals(5, l.replaceSlice(1, 1, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), l.replaceSlice(1, 1, List.of(5)));
    assertEquals(5, l.replaceSlice(1, 1, List.of(5)).get(1));
    assertFalse(l.replaceSlice(1, 0, List.of(5)).isEmpty());
    assertEquals(5, l.replaceSlice(1, 0, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), l.replaceSlice(1, 0, List.of(5)));
    assertEquals(5, l.replaceSlice(1, 0, List.of(5)).get(1));
    assertFalse(l.replaceSlice(1, -3, List.of(5)).isEmpty());
    assertEquals(5, l.replaceSlice(1, -3, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), l.replaceSlice(1, -3, List.of(5)));
    assertEquals(5, l.replaceSlice(1, -3, List.of(5)).get(1));
    assertFalse(l.replaceSlice(1, -4, List.of(5)).isEmpty());
    assertEquals(5, l.replaceSlice(1, -4, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), l.replaceSlice(1, -4, List.of(5)));
    assertEquals(5, l.replaceSlice(1, -4, List.of(5)).get(1));
    assertFalse(l.replaceSlice(1, -5, List.of(5)).isEmpty());
    assertEquals(5, l.replaceSlice(1, -5, List.of(5)).size());
    assertEquals(List.of(1, 5, 2, null, 4), l.replaceSlice(1, -5, List.of(5)));
    assertEquals(5, l.replaceSlice(1, -5, List.of(5)).get(1));
    assertFalse(l.replaceSlice(-1, 1, List.of(5)).isEmpty());
    assertEquals(5, l.replaceSlice(-1, 1, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5, 4), l.replaceSlice(-1, 1, List.of(5)));
    assertEquals(2, l.replaceSlice(-1, 1, List.of(5)).get(1));
    assertFalse(l.replaceSlice(-1, 3, List.of(5)).isEmpty());
    assertEquals(5, l.replaceSlice(-1, 3, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5, 4), l.replaceSlice(-1, 3, List.of(5)));
    assertEquals(2, l.replaceSlice(-1, 3, List.of(5)).get(1));
    assertFalse(l.replaceSlice(-1, -1, List.of(5)).isEmpty());
    assertEquals(5, l.replaceSlice(-1, -1, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5, 4), l.replaceSlice(-1, -1, List.of(5)));
    assertEquals(2, l.replaceSlice(-1, -1, List.of(5)).get(1));
    assertFalse(l.replaceSlice(-1, -4, List.of(5)).isEmpty());
    assertEquals(5, l.replaceSlice(-1, -4, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5, 4), l.replaceSlice(-1, -4, List.of(5)));
    assertEquals(2, l.replaceSlice(-1, -4, List.of(5)).get(1));

    assertFalse(l.replaceSlice(1, -1, List.of(5)).isEmpty());
    assertEquals(3, l.replaceSlice(1, -1, List.of(5)).size());
    assertEquals(List.of(1, 5, 4), l.replaceSlice(1, -1, List.of(5)));
    assertEquals(5, l.replaceSlice(1, -1, List.of(5)).get(1));
    assertFalse(l.replaceSlice(1, -2, List.of(5)).isEmpty());
    assertEquals(4, l.replaceSlice(1, -2, List.of(5)).size());
    assertEquals(List.of(1, 5, null, 4), l.replaceSlice(1, -2, List.of(5)));
    assertNull(l.replaceSlice(1, -2, List.of(5)).get(2));
    assertFalse(l.replaceSlice(1, 3, List.of(5)).isEmpty());
    assertEquals(3, l.replaceSlice(1, 3, List.of(5)).size());
    assertEquals(List.of(1, 5, 4), l.replaceSlice(1, 3, List.of(5)));
    assertEquals(4, l.replaceSlice(1, 3, List.of(5)).get(2));
    assertFalse(l.replaceSlice(1, 2, List.of(5)).isEmpty());
    assertEquals(4, l.replaceSlice(1, 2, List.of(5)).size());
    assertEquals(List.of(1, 5, null, 4), l.replaceSlice(1, 2, List.of(5)));
    assertNull(l.replaceSlice(1, 2, List.of(5)).get(2));
    assertFalse(l.replaceSlice(-1, 4, List.of(5)).isEmpty());
    assertEquals(4, l.replaceSlice(-1, 4, List.of(5)).size());
    assertEquals(List.of(1, 2, null, 5), l.replaceSlice(-1, 4, List.of(5)));
    assertEquals(2, l.replaceSlice(-1, 4, List.of(5)).get(1));
    assertFalse(l.replaceSlice(-2, -1, List.of(5)).isEmpty());
    assertEquals(4, l.replaceSlice(-2, -1, List.of(5)).size());
    assertEquals(List.of(1, 2, 5, 4), l.replaceSlice(-2, -1, List.of(5)));
    assertEquals(2, l.replaceSlice(-2, -1, List.of(5)).get(1));

    assertFalse(l.replaceSlice(0, Integer.MAX_VALUE, List.of(5)).isEmpty());
    assertEquals(1, l.replaceSlice(0, Integer.MAX_VALUE, List.of(5)).size());
    assertEquals(List.of(5), l.replaceSlice(0, Integer.MAX_VALUE, List.of(5)));
    assertEquals(5, l.replaceSlice(0, Integer.MAX_VALUE, List.of(5)).get(0));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.replaceSlice(0, Integer.MAX_VALUE, List.of(5)).get(2));

    assertTrue(l.replaceSlice(0, Integer.MAX_VALUE, List.of()).isEmpty());
    assertEquals(0, l.replaceSlice(0, Integer.MAX_VALUE, List.of()).size());
    assertEquals(List.of(), l.replaceSlice(0, Integer.MAX_VALUE, List.of()));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.replaceSlice(0, Integer.MAX_VALUE, List.of()).get(1));

    assertFalse(List.of().replaceSlice(0, 0, List.of(5)).isEmpty());
    assertEquals(1, List.of().replaceSlice(0, 0, List.of(5)).size());
    assertEquals(List.of(5), List.of().replaceSlice(0, 0, List.of(5)));
    assertEquals(5, List.of().replaceSlice(0, 0, List.of(5)).get(0));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.of().replaceSlice(1, -1, List.of(5)).get(0));

    assertTrue(List.of().replaceSlice(1, -1, List.of(5)).isEmpty());
    assertEquals(0, List.of().replaceSlice(1, -1, List.of(5)).size());
    assertEquals(List.of(), List.of().replaceSlice(1, -1, List.of(5)));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.of().replaceSlice(1, -1, List.of(5)).get(0));
  }

  @Test
  public void replaceWhere() {
    var l = List.of(1, 2, 3, 4);
    assertFalse(l.replaceWhere(i -> false, 5).isEmpty());
    assertEquals(4, l.replaceWhere(i -> false, 5).size());
    assertEquals(l, l.replaceWhere(i -> false, 5));
    assertEquals(3, l.replaceWhere(i -> false, 5).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceWhere(i -> false, 5).get(4));
    assertFalse(l.replaceWhere(i -> true, 5).isEmpty());
    assertEquals(4, l.replaceWhere(i -> true, 5).size());
    assertEquals(List.of(5, 5, 5, 5), l.replaceWhere(i -> true, 5));
    assertEquals(5, l.replaceWhere(i -> true, 5).get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceWhere(i -> true, 5).get(5));
    assertFalse(l.replaceWhere(i -> i == 2, 3).isEmpty());
    assertEquals(4, l.replaceWhere(i -> i == 2, 3).size());
    assertEquals(List.of(1, 3, 3, 4), l.replaceWhere(i -> i == 2, 3));
    assertEquals(3, l.replaceWhere(i -> i == 2, 3).get(1));
    assertEquals(3, l.replaceWhere(i -> i == 2, 3).get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> l.replaceWhere(i -> i == 2, 3).get(4));

    assertFalse(l.append(null).replaceWhere(i -> i == 4, 5).isEmpty());
    assertEquals(5, l.append(null).replaceWhere(i -> i == 4, 5).size());
    assertEquals(2, l.append(null).replaceWhere(i -> i == 4, 5).get(1));
    assertEquals(3, l.append(null).replaceWhere(i -> i == 4, 5).get(2));
    assertEquals(5, l.append(null).replaceWhere(i -> i == 4, 5).get(3));
    assertThrows(NullPointerException.class,
        () -> l.append(null).replaceWhere(i -> i == 4, 5).get(4));
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.append(null).replaceWhere(i -> i == 4, 5).get(5));

    assertTrue(List.<Integer>of().replaceWhere(i -> false, 5).isEmpty());
    assertEquals(0, List.<Integer>of().replaceWhere(i -> false, 5).size());
    assertEquals(List.of(), List.<Integer>of().replaceWhere(i -> false, 5));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceWhere(i -> false, 5).get(2));
    assertTrue(List.<Integer>of().replaceWhere(i -> true, 5).isEmpty());
    assertEquals(0, List.<Integer>of().replaceWhere(i -> true, 5).size());
    assertEquals(List.of(), List.<Integer>of().replaceWhere(i -> true, 5));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().replaceWhere(i -> true, 5).get(2));
  }

  @Test
  public void reverse() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.reverse().isEmpty());
    assertEquals(4, l.reverse().size());
    assertEquals(List.of(4, null, 2, 1), l.reverse());
    assertNull(l.reverse().get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.reverse().get(4));
    assertEquals(4, l.reverse().reverse().size());
    assertEquals(l, l.reverse().reverse());
    assertEquals(2, l.reverse().reverse().get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.reverse().reverse().get(4));

    assertTrue(List.<Integer>of().reverse().isEmpty());
    assertEquals(0, List.<Integer>of().reverse().size());
    assertEquals(List.of(), List.<Integer>of().reverse());
    assertThrows(IndexOutOfBoundsException.class, () -> List.<Integer>of().reverse().get(0));
  }

  @Test
  public void resizeTo() {
    assertThrows(IllegalArgumentException.class, () -> List.of(1, 2, null, 4).resizeTo(-1, 5));
    assertEquals(List.of(), List.of(1, 2, null, 4).resizeTo(0, 5));
    assertEquals(List.of(1), List.of(1, 2, null, 4).resizeTo(1, 5));
    assertEquals(List.of(1, 2), List.of(1, 2, null, 4).resizeTo(2, 5));
    assertEquals(List.of(1, 2, null), List.of(1, 2, null, 4).resizeTo(3, 5));
    assertEquals(List.of(1, 2, null, 4), List.of(1, 2, null, 4).resizeTo(4, 5));
    assertEquals(List.of(1, 2, null, 4, 5), List.of(1, 2, null, 4).resizeTo(5, 5));
    assertEquals(List.of(1, 2, null, 4, 5, 5), List.of(1, 2, null, 4).resizeTo(6, 5));
  }

  @Test
  public void slice() {
    var l = List.of(1, 2, null, 4);
    assertTrue(l.slice(1, 1).isEmpty());
    assertEquals(0, l.slice(1, 1).size());
    assertEquals(List.of(), l.slice(1, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.slice(1, 1).get(0));
    assertTrue(l.slice(1, 0).isEmpty());
    assertEquals(0, l.slice(1, 0).size());
    assertEquals(List.of(), l.slice(1, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> l.slice(1, 0).get(0));
    assertTrue(l.slice(1, -3).isEmpty());
    assertEquals(0, l.slice(1, -3).size());
    assertEquals(List.of(), l.slice(1, -3));
    assertThrows(IndexOutOfBoundsException.class, () -> l.slice(1, -3).get(0));
    assertTrue(l.slice(1, -4).isEmpty());
    assertEquals(0, l.slice(1, -4).size());
    assertEquals(List.of(), l.slice(1, -4));
    assertThrows(IndexOutOfBoundsException.class, () -> l.slice(1, -4).get(0));
    assertTrue(l.slice(1, -5).isEmpty());
    assertEquals(0, l.slice(1, -5).size());
    assertEquals(List.of(), l.slice(1, -5));
    assertThrows(IndexOutOfBoundsException.class, () -> l.slice(1, -5).get(0));
    assertTrue(l.slice(-1, 1).isEmpty());
    assertEquals(0, l.slice(-1, 1).size());
    assertEquals(List.of(), l.slice(-1, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.slice(-1, 1).get(0));
    assertTrue(l.slice(-1, 3).isEmpty());
    assertEquals(0, l.slice(-1, 3).size());
    assertEquals(List.of(), l.slice(-1, 3));
    assertThrows(IndexOutOfBoundsException.class, () -> l.slice(-1, 3).get(0));
    assertTrue(l.slice(-1, -1).isEmpty());
    assertEquals(0, l.slice(-1, -1).size());
    assertEquals(List.of(), l.slice(-1, -1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.slice(-1, -1).get(0));
    assertTrue(l.slice(-1, -4).isEmpty());
    assertEquals(0, l.slice(-1, -4).size());
    assertEquals(List.of(), l.slice(-1, -4));
    assertThrows(IndexOutOfBoundsException.class, () -> l.slice(-1, -4).get(0));

    assertFalse(l.slice(1, -1).isEmpty());
    assertEquals(2, l.slice(1, -1).size());
    assertEquals(List.of(2, null), l.slice(1, -1));
    assertNull(l.slice(1, -1).get(1));
    assertFalse(l.slice(1, -2).isEmpty());
    assertEquals(1, l.slice(1, -2).size());
    assertEquals(List.of(2), l.slice(1, -2));
    assertEquals(2, l.slice(1, -2).get(0));
    assertFalse(l.slice(1, 3).isEmpty());
    assertEquals(2, l.slice(1, 3).size());
    assertEquals(List.of(2, null), l.slice(1, 3));
    assertNull(l.slice(1, 3).get(1));
    assertFalse(l.slice(1, 2).isEmpty());
    assertEquals(1, l.slice(1, 2).size());
    assertEquals(List.of(2), l.slice(1, 2));
    assertEquals(2, l.slice(1, 2).get(0));
    assertFalse(l.slice(-1, 4).isEmpty());
    assertEquals(1, l.slice(-1, 4).size());
    assertEquals(List.of(4), l.slice(-1, 4));
    assertEquals(4, l.slice(-1, 4).get(0));
    assertFalse(l.slice(-2, -1).isEmpty());
    assertEquals(1, l.slice(-2, -1).size());
    assertEquals(List.of(null), l.slice(-2, -1));
    assertNull(l.slice(-2, -1).get(0));

    assertFalse(l.slice(0, Integer.MAX_VALUE).isEmpty());
    assertEquals(4, l.slice(0, Integer.MAX_VALUE).size());
    assertEquals(List.of(1, 2, null, 4), l.slice(0, Integer.MAX_VALUE));
    assertEquals(2, l.slice(0, Integer.MAX_VALUE).get(1));

    assertTrue(List.of().slice(1, -1).isEmpty());
    assertEquals(0, List.of().slice(1, -1).size());
    assertEquals(List.of(), List.of().slice(1, -1));
    assertThrows(IndexOutOfBoundsException.class, () -> List.of().slice(1, -1).get(0));
  }

  @Test
  public void startsWith() {
    var l = List.<Integer>of().startsWith(List.of());
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.<Integer>of().startsWith(List.of(1));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    l = List.of(1, null, 3).startsWith(List.of());
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).startsWith(List.of(1));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).startsWith(List.of(null));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    l = List.of(1, null, 3).startsWith(List.of(1, null));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).startsWith(List.of(null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());

    l = List.of(1, null, 3).startsWith(List.of(1, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.first());

    l = List.of(1, null, 3).startsWith(List.of(null, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.first());
  }

  @Test
  public void sorted() {
    var l = List.of(1, 2, 3, 2, 1);
    assertFalse(l.sorted(Integer::compare).isEmpty());
    assertTrue(l.sorted(Integer::compare).notEmpty());
    assertEquals(5, l.sorted(Integer::compare).size());
    assertEquals(List.of(1, 1, 2, 2, 3), l.sorted(Integer::compare));
    assertEquals(1, l.sorted(Integer::compare).get(1));

    assertTrue(List.<Integer>of().sorted(Integer::compare).isEmpty());
    assertFalse(List.<Integer>of().sorted(Integer::compare).notEmpty());
    assertEquals(0, List.<Integer>of().sorted(Integer::compare).size());
    assertEquals(List.of(), List.<Integer>of().sorted(Integer::compare));
    assertThrows(IndexOutOfBoundsException.class,
        () -> List.<Integer>of().sorted(Integer::compare).get(0));
  }

  @Test
  public void take() {
    var l = List.<Integer>of().take(1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().take(0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().take(-1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    l = List.of(1, null, 3).take(1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertEquals(List.of(1), l);
    l = List.of(1, null, 3).take(2);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(1, null), l);
    l = List.of(1, null, 3).take(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).take(4);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).take(0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).take(-1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
  }

  @Test
  public void takeRight() {
    var l = List.<Integer>of().takeRight(1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().takeRight(0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    l = List.<Integer>of().takeRight(-1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    l = List.of(1, null, 3).takeRight(1);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertEquals(List.of(3), l);
    l = List.of(1, null, 3).takeRight(2);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(null, 3), l);
    l = List.of(1, null, 3).takeRight(3);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).takeRight(4);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).takeRight(0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).takeRight(-1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
  }

  @Test
  public void takeRightWhile() {
    var l = List.<Integer>of().takeRightWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());

    l = List.of(1, null, 3).takeRightWhile(Objects::isNull);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).takeRightWhile(Objects::nonNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertEquals(List.of(3), l);
    l = List.of(1, null, 3).takeRightWhile(e -> e < 1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    l = List.of(1, 2, 3).takeRightWhile(e -> e > 0);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).takeRightWhile(e -> e > 0).size());
  }

  @Test
  public void takeWhile() {
    var l = List.<Integer>of().takeWhile(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());

    l = List.of(1, null, 3).takeWhile(Objects::isNull);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);
    l = List.of(1, null, 3).takeWhile(Objects::nonNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertEquals(List.of(1), l);
    l = List.of(1, null, 3).takeWhile(e -> e < 1);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    l = List.of(1, 2, 3).takeWhile(e -> e > 0);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, 2, 3), l);

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).takeWhile(e -> e > 0).size());
  }

  @Test
  public void union() {
    assertEquals(List.of(1, 2, null, 4), List.of(1, 2, null, 4).union(List.of(1, null)));
    assertEquals(List.of(1, 2, null, 4), List.of(1, 2, null, 4).union(List.of(1, 4)));
    assertEquals(List.of(1, 2, null, 4, 3), List.of(1, 2, null, 4).union(List.of(1, 3, 4)));
    assertEquals(List.of(1, 2, null, 4, 3, 3), List.of(1, 2, null, 4).union(List.of(3, 1, 3)));
    assertEquals(List.of(1, 2, null, 4, null), List.of(1, 2, null, 4).union(List.of(null, null)));
    assertEquals(List.of(1, null, 2, 4), List.of(1, null).union(List.of(1, 2, null, 4)));
    assertEquals(List.of(1, 2, null, 4), List.of(1, 2, null, 4).union(List.of(2, 1)));
    assertEquals(List.of(1, null, 2, 4), List.of(1, null).union(List.of(2, 4)));

    assertEquals(List.of(1, 2, null, 4), List.of(1, 2, null, 4).union(List.of()));
    assertEquals(List.of(1, 2, null, 4), List.of().union(List.of(1, 2, null, 4)));
  }

  @Test
  public void test() throws Exception {
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
}
