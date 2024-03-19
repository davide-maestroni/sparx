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
import sparx.Sparx.lazy.List;

public class ListTests {

  @Test
  public void all() {
    assertFalse(List.of().all(Objects::nonNull).isEmpty());
    assertTrue(List.of().all(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().all(Objects::nonNull).size());
    assertFalse(List.of().all(Objects::nonNull).head());
    assertFalse(List.of(1, 2, 3).all(i -> i < 3).head());
    {
      var itr = List.of(1, 2, 3).all(i -> i < 3).iterator();
      assertTrue(itr.hasNext());
      assertFalse(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertTrue(List.of(1, 2, 3).all(i -> i > 0).head());
    {
      var itr = List.of(1, 2, 3).all(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertTrue(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).all(i -> i > 0);
    assertThrows(NullPointerException.class, l::head);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
  }

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
    assertEquals(0, List.of().count().head());
    assertEquals(3, List.of(1, 2, 3).count().head());
    {
      var itr = List.of(1, 2, 3).count().iterator();
      assertTrue(itr.hasNext());
      assertEquals(3, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertEquals(3, List.of(1, null, 3).count().head());
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
    assertEquals(0, List.of().count(Objects::nonNull).head());
    assertEquals(2, List.of(1, 2, 3).count(i -> i < 3).head());
    {
      var itr = List.of(1, 2, 3).count(i -> i < 3).iterator();
      assertTrue(itr.hasNext());
      assertEquals(2, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertEquals(3, List.of(1, 2, 3).count(i -> i > 0).head());
    {
      var itr = List.of(1, 2, 3).count(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertEquals(3, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).count(i -> i > 0);
    assertThrows(NullPointerException.class, l::head);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
  }

  @Test
  public void countNot() {
    assertFalse(List.of().countNot(Objects::nonNull).isEmpty());
    assertTrue(List.of().countNot(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().countNot(Objects::nonNull).size());
    assertEquals(0, List.of().countNot(Objects::nonNull).head());
    assertEquals(1, List.of(1, 2, 3).countNot(i -> i < 3).head());
    {
      var itr = List.of(1, 2, 3).countNot(i -> i < 3).iterator();
      assertTrue(itr.hasNext());
      assertEquals(1, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertEquals(0, List.of(1, 2, 3).countNot(i -> i > 0).head());
    {
      var itr = List.of(1, 2, 3).countNot(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertEquals(0, itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).countNot(i -> i > 0);
    assertThrows(NullPointerException.class, l::head);
    {
      var itr = l.iterator();
      assertTrue(itr.hasNext());
      assertThrows(NullPointerException.class, itr::next);
    }
  }

  @Test
  public void doFor() {
    var list = new ArrayList<>();
    List.of(1, 2, 3).doFor(list::add);
    assertEquals(List.of(1, 2, 3), list);
  }

  @Test
  public void doUntil() {
    var list = new ArrayList<>();
    List.of(1, 2, 3).doUntil(e -> e > 2, list::add);
    assertEquals(List.of(1, 2), list);
    list.clear();
    List.of(1, 2, 3).doUntil(e -> {
      list.add(e);
      return e > 1;
    });
    assertEquals(List.of(1, 2), list);
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
  public void dropUntil() {
    var l = List.<Integer>of().dropUntil(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());

    l = List.of(1, null, 3).dropUntil(Objects::isNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(null, 3), l);
    l = List.of(1, null, 3).dropUntil(Objects::nonNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).dropUntil(e -> e < 2);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2, 3).dropUntil(e -> e > 3);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).dropUntil(e -> e > 1).size());
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
  public void dropRightUntil() {
    var l = List.<Integer>of().dropRightUntil(e -> e > 0);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());

    l = List.of(1, null, 3).dropRightUntil(Objects::isNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(2, l.size());
    assertEquals(List.of(1, null), l);
    l = List.of(1, null, 3).dropRightUntil(Objects::nonNull);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);
    l = List.of(1, null, 3).dropRightUntil(e -> e > 2);
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(3, l.size());
    assertEquals(List.of(1, null, 3), l);

    l = List.of(1, 2, 3).dropRightUntil(e -> e > 3);
    assertTrue(l.isEmpty());
    assertFalse(l.notEmpty());
    assertEquals(0, l.size());
    assertEquals(List.of(), l);

    assertThrows(NullPointerException.class,
        () -> List.of(1, null, 3).dropRightUntil(e -> e < 1).size());
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
  public void endsWith() {
    var l = List.<Integer>of().endsWith(List.of());
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.head());

    l = List.<Integer>of().endsWith(List.of(1));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.head());

    l = List.of(1, null, 3).endsWith(List.of());
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.head());

    l = List.of(1, null, 3).endsWith(List.of(3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.head());

    l = List.of(1, null, 3).endsWith(List.of(null));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.head());

    l = List.of(1, null, 3).endsWith(List.of(null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.head());

    l = List.of(1, null, 3).endsWith(List.of(1, null));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.head());

    l = List.of(1, null, 3).endsWith(List.of(1, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertTrue(l.head());

    l = List.of(1, null, 3).endsWith(List.of(null, null, 3));
    assertFalse(l.isEmpty());
    assertTrue(l.notEmpty());
    assertEquals(1, l.size());
    assertFalse(l.head());
  }

  @Test
  public void exists() {
    assertFalse(List.of().exists(Objects::nonNull).isEmpty());
    assertTrue(List.of().exists(Objects::nonNull).notEmpty());
    assertEquals(1, List.of().exists(Objects::nonNull).size());
    assertFalse(List.of().exists(Objects::nonNull).head());
    assertFalse(List.of(1, 2, 3).exists(i -> i > 3).head());
    {
      var itr = List.of(1, 2, 3).exists(i -> i > 3).iterator();
      assertTrue(itr.hasNext());
      assertFalse(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    assertTrue(List.of(1, 2, 3).exists(i -> i > 0).head());
    {
      var itr = List.of(1, 2, 3).exists(i -> i > 0).iterator();
      assertTrue(itr.hasNext());
      assertTrue(itr.next());
      assertThrows(UnsupportedOperationException.class, itr::remove);
      assertFalse(itr.hasNext());
      assertThrows(NoSuchElementException.class, itr::next);
    }
    var l = List.of(1, null, 3).exists(i -> i > 1);
    assertThrows(NullPointerException.class, l::head);
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
  public void filterNot() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.filterNot(Objects::isNull).isEmpty());
    assertEquals(List.of(1, 2, 4), l.filterNot(Objects::isNull));
    assertEquals(List.of(1, 2), l.filterNot(Objects::isNull).filterNot(i -> i > 3));
    assertEquals(List.of(4), l.filterNot(Objects::isNull).filterNot(i -> i < 3));
    assertEquals(List.of(), l.filterNot(Objects::isNull).filterNot(i -> i <= 4));
    assertThrows(NullPointerException.class, () -> l.filterNot(i -> i <= 4).size());

    assertTrue(List.of().filterNot(Objects::isNull).isEmpty());
    assertEquals(0, List.of().filterNot(Objects::isNull).size());
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
  public void findAnyNot() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findAnyNot(Objects::nonNull).isEmpty());
    assertEquals(1, l.findAnyNot(Objects::nonNull).size());
    assertEquals(List.of(null), l.findAnyNot(Objects::nonNull));
    assertFalse(l.findAnyNot(i -> i > 4).isEmpty());
    assertEquals(1, l.findAnyNot(i -> i > 4).size());

    assertTrue(List.of().findAnyNot(Objects::isNull).isEmpty());
    assertEquals(0, List.of().findAnyNot(Objects::isNull).size());
  }

  @Test
  public void findIndexOf() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findIndexOf(null).isEmpty());
    assertEquals(1, l.findIndexOf(null).size());
    assertEquals(2, l.findIndexOf(null).head());
    assertEquals(List.of(2), l.findIndexOf(null));
    assertFalse(l.findIndexOf(4).isEmpty());
    assertEquals(1, l.findIndexOf(4).size());
    assertEquals(3, l.findIndexOf(4).head());
    assertEquals(List.of(3), l.findIndexOf(4));
    assertTrue(l.findIndexOf(3).isEmpty());
    assertEquals(0, l.findIndexOf(3).size());
    assertThrows(IndexOutOfBoundsException.class, () -> l.findIndexOf(3).head());
    assertEquals(List.of(), l.findIndexOf(3));

    assertTrue(List.of().findIndexOf(null).isEmpty());
    assertEquals(0, List.of().findIndexOf(null).size());
  }

  @Test
  public void findIndexOfSlice() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findIndexOfSlice(List.of(2, null)).isEmpty());
    assertEquals(1, l.findIndexOfSlice(List.of(2, null)).size());
    assertEquals(1, l.findIndexOfSlice(List.of(2, null)).head());
    assertEquals(List.of(1), l.findIndexOfSlice(List.of(2, null)));
    assertFalse(l.findIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(1, l.findIndexOfSlice(List.of(null)).size());
    assertEquals(2, l.findIndexOfSlice(List.of(null)).head());
    assertEquals(List.of(2), l.findIndexOfSlice(List.of(null)));
    assertTrue(l.findIndexOfSlice(List.of(null, 2)).isEmpty());
    assertEquals(0, l.findIndexOfSlice(List.of(null, 2)).size());
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.findIndexOfSlice(List.of(null, 2)).head());
    assertEquals(List.of(), l.findIndexOfSlice(List.of(null, 2)));
    assertFalse(l.findIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, l.findIndexOfSlice(List.of()).size());
    assertEquals(0, l.findIndexOfSlice(List.of()).head());
    assertEquals(List.of(0), l.findIndexOfSlice(List.of()));

    assertEquals(2, List.of(1, 1, 1, 1, 2, 1).findIndexOfSlice(List.of(1, 1, 2)).head());

    assertTrue(List.of().findIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(0, List.of().findIndexOfSlice(List.of(null)).size());
    assertFalse(List.of().findIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, List.of().findIndexOfSlice(List.of()).size());
    assertEquals(0, List.of().findIndexOfSlice(List.of()).head());
    assertEquals(List.of(0), List.of().findIndexOfSlice(List.of()));
  }

  @Test
  public void findIndexWhere() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findIndexWhere(Objects::isNull).isEmpty());
    assertEquals(1, l.findIndexWhere(Objects::isNull).size());
    assertEquals(2, l.findIndexWhere(Objects::isNull).head());
    assertEquals(List.of(2), l.findIndexWhere(Objects::isNull));
    assertFalse(l.findIndexWhere(i -> i > 1).isEmpty());
    assertEquals(1, l.findIndexWhere(i -> i > 1).size());
    assertEquals(1, l.findIndexWhere(i -> i > 1).head());
    assertEquals(List.of(1), l.findIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class, () -> l.findIndexWhere(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class, () -> l.findIndexWhere(i -> i > 3).head());

    assertTrue(List.of().findIndexWhere(Objects::isNull).isEmpty());
    assertEquals(0, List.of().findIndexWhere(Objects::isNull).size());
  }

  @Test
  public void findIndexWhereNot() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findIndexWhereNot(Objects::nonNull).isEmpty());
    assertEquals(1, l.findIndexWhereNot(Objects::nonNull).size());
    assertEquals(2, l.findIndexWhereNot(Objects::nonNull).head());
    assertEquals(List.of(2), l.findIndexWhereNot(Objects::nonNull));
    assertFalse(l.findIndexWhereNot(i -> i < 2).isEmpty());
    assertEquals(1, l.findIndexWhereNot(i -> i < 2).size());
    assertEquals(1, l.findIndexWhereNot(i -> i < 2).head());
    assertEquals(List.of(1), l.findIndexWhereNot(i -> i < 2));
    assertThrows(NullPointerException.class, () -> l.findIndexWhereNot(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class, () -> l.findIndexWhereNot(i -> i < 3).head());

    assertTrue(List.of().findIndexWhereNot(Objects::nonNull).isEmpty());
    assertEquals(0, List.of().findIndexWhereNot(Objects::nonNull).size());
  }

  @Test
  public void findLast() {
    var l = List.of(1, 2, null, 4, 5);
    assertFalse(l.findLast(Objects::isNull).isEmpty());
    assertEquals(1, l.findLast(Objects::isNull).size());
    assertNull(l.findLast(Objects::isNull).head());
    assertEquals(List.of(null), l.findLast(Objects::isNull));
    assertThrows(NullPointerException.class, () -> l.findLast(i -> i < 4).head());
    assertFalse(l.findLast(i -> i < 5).isEmpty());
    assertEquals(1, l.findLast(i -> i < 5).size());
    assertEquals(4, l.findLast(i -> i < 5).head());
    assertEquals(List.of(4), l.findLast(i -> i < 5));
    assertTrue(l.findLast(i -> i != null && i > 5).isEmpty());
    assertEquals(0, l.findLast(i -> i != null && i > 5).size());
    assertThrows(IndexOutOfBoundsException.class, () -> l.findLast(i -> i != null && i > 5).head());
    assertEquals(List.of(), l.findLast(i -> i != null && i > 5));

    assertTrue(List.of().findLast(Objects::isNull).isEmpty());
    assertEquals(0, List.of().findLast(Objects::isNull).size());
  }

  @Test
  public void findLastIndexOf() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findLastIndexOf(null).isEmpty());
    assertEquals(1, l.findLastIndexOf(null).size());
    assertEquals(2, l.findLastIndexOf(null).head());
    assertEquals(List.of(2), l.findLastIndexOf(null));
    assertFalse(l.findLastIndexOf(4).isEmpty());
    assertEquals(1, l.findLastIndexOf(4).size());
    assertEquals(3, l.findLastIndexOf(4).head());
    assertEquals(List.of(3), l.findLastIndexOf(4));
    assertTrue(l.findLastIndexOf(3).isEmpty());
    assertEquals(0, l.findLastIndexOf(3).size());
    assertThrows(IndexOutOfBoundsException.class, () -> l.findLastIndexOf(3).head());
    assertEquals(List.of(), l.findLastIndexOf(3));

    assertTrue(List.of().findLastIndexOf(null).isEmpty());
    assertEquals(0, List.of().findLastIndexOf(null).size());
    assertFalse(List.of().findLastIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, List.of().findLastIndexOfSlice(List.of()).size());
    assertEquals(0, List.of().findLastIndexOfSlice(List.of()).head());
    assertEquals(List.of(0), List.of().findLastIndexOfSlice(List.of()));
  }

  @Test
  public void findLastIndexOfSlice() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findLastIndexOfSlice(List.of(2, null)).isEmpty());
    assertEquals(1, l.findLastIndexOfSlice(List.of(2, null)).size());
    assertEquals(1, l.findLastIndexOfSlice(List.of(2, null)).head());
    assertEquals(List.of(1), l.findLastIndexOfSlice(List.of(2, null)));
    assertFalse(l.findLastIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(1, l.findLastIndexOfSlice(List.of(null)).size());
    assertEquals(2, l.findLastIndexOfSlice(List.of(null)).head());
    assertEquals(List.of(2), l.findLastIndexOfSlice(List.of(null)));
    assertTrue(l.findLastIndexOfSlice(List.of(null, 2)).isEmpty());
    assertEquals(0, l.findLastIndexOfSlice(List.of(null, 2)).size());
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.findLastIndexOfSlice(List.of(null, 2)).head());
    assertEquals(List.of(), l.findLastIndexOfSlice(List.of(null, 2)));
    assertFalse(l.findLastIndexOfSlice(List.of()).isEmpty());
    assertEquals(1, l.findLastIndexOfSlice(List.of()).size());
    assertEquals(4, l.findLastIndexOfSlice(List.of()).head());
    assertEquals(List.of(4), l.findLastIndexOfSlice(List.of()));

    assertEquals(2, List.of(1, 1, 1, 1, 2, 1).findLastIndexOfSlice(List.of(1, 1, 2)).head());

    assertTrue(List.of().findLastIndexOfSlice(List.of(null)).isEmpty());
    assertEquals(0, List.of().findLastIndexOfSlice(List.of(null)).size());
  }

  @Test
  public void findLastIndexWhere() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findLastIndexWhere(Objects::isNull).isEmpty());
    assertEquals(1, l.findLastIndexWhere(Objects::isNull).size());
    assertEquals(2, l.findLastIndexWhere(Objects::isNull).head());
    assertEquals(List.of(2), l.findLastIndexWhere(Objects::isNull));
    assertFalse(l.findLastIndexWhere(i -> i > 1).isEmpty());
    assertEquals(1, l.findLastIndexWhere(i -> i > 1).size());
    assertEquals(3, l.findLastIndexWhere(i -> i > 1).head());
    assertEquals(List.of(3), l.findLastIndexWhere(i -> i > 1));
    assertThrows(NullPointerException.class, () -> l.findLastIndexWhere(i -> i < 3).isEmpty());
    assertThrows(NullPointerException.class, () -> l.findLastIndexWhere(i -> i < 3).head());

    assertTrue(List.of().findLastIndexWhere(Objects::isNull).isEmpty());
    assertEquals(0, List.of().findLastIndexWhere(Objects::isNull).size());
  }

  @Test
  public void findLastIndexWhereNot() {
    var l = List.of(1, 2, null, 4);
    assertFalse(l.findLastIndexWhereNot(Objects::nonNull).isEmpty());
    assertEquals(1, l.findLastIndexWhereNot(Objects::nonNull).size());
    assertEquals(2, l.findLastIndexWhereNot(Objects::nonNull).head());
    assertEquals(List.of(2), l.findLastIndexWhereNot(Objects::nonNull));
    assertFalse(l.findLastIndexWhereNot(i -> i < 2).isEmpty());
    assertEquals(1, l.findLastIndexWhereNot(i -> i < 2).size());
    assertEquals(3, l.findLastIndexWhereNot(i -> i < 2).head());
    assertEquals(List.of(3), l.findLastIndexWhereNot(i -> i < 2));
    assertThrows(NullPointerException.class, () -> l.findLastIndexWhereNot(i -> i > 3).isEmpty());
    assertThrows(NullPointerException.class, () -> l.findLastIndexWhereNot(i -> i > 3).head());

    assertTrue(List.of().findLastIndexWhereNot(Objects::nonNull).isEmpty());
    assertEquals(0, List.of().findLastIndexWhereNot(Objects::nonNull).size());
  }

  @Test
  public void findLastNot() {
    var l = List.of(1, 2, null, 4, 5);
    assertFalse(l.findLastNot(Objects::nonNull).isEmpty());
    assertEquals(1, l.findLastNot(Objects::nonNull).size());
    assertNull(l.findLastNot(Objects::nonNull).head());
    assertEquals(List.of(null), l.findLastNot(Objects::nonNull));
    assertFalse(l.findLastNot(Objects::isNull).isEmpty());
    assertEquals(1, l.findLastNot(Objects::isNull).size());
    assertEquals(5, l.findLastNot(Objects::isNull).head());
    assertEquals(List.of(5), l.findLastNot(Objects::isNull));
    assertFalse(l.findLastNot(i -> i < 4).isEmpty());
    assertEquals(1, l.findLastNot(i -> i < 4).size());
    assertEquals(5, l.findLastNot(i -> i < 4).head());
    assertEquals(List.of(5), l.findLastNot(i -> i < 4));
    assertThrows(NullPointerException.class, () -> l.findLastNot(i -> i > 3).head());
    assertTrue(l.findLastNot(i -> i == null || i < 6).isEmpty());
    assertEquals(0, l.findLastNot(i -> i == null || i < 6).size());
    assertThrows(IndexOutOfBoundsException.class,
        () -> l.findLastNot(i -> i == null || i < 6).head());
    assertEquals(List.of(), l.findLastNot(i -> i == null || i < 6));

    assertTrue(List.of().findLastNot(Objects::isNull).isEmpty());
    assertEquals(0, List.of().findLastNot(Objects::isNull).size());
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
    assertThrows(IndexOutOfBoundsException.class, () -> l.flatMap(i -> List.of()).head());
    assertFalse(l.flatMap(i -> List.of(null)).isEmpty());
    assertEquals(2, l.flatMap(i -> List.of(null)).size());
    assertEquals(List.of(null, null), l.flatMap(i -> List.of(null)));
    assertNull(l.flatMap(i -> List.of(null)).get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.flatMap(i -> List.of(null)).get(2));
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

    assertEquals(List.of(2, 1), List.of(1, 2).foldRight(List.of(), List::append).get(0));

    assertFalse(List.<Integer>of().foldRight(1, Integer::sum).isEmpty());
    assertEquals(1, List.<Integer>of().foldRight(1, Integer::sum).size());
    assertEquals(List.of(1), List.<Integer>of().foldRight(1, Integer::sum));
    assertEquals(1, List.<Integer>of().foldRight(1, Integer::sum).get(0));
    assertEquals(List.of(), List.of().foldRight(List.of(), List::append).get(0));
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
  public void groupFiller() {
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
    assertTrue(l.includes(null).head());
    assertEquals(List.of(true), l.includes(null));
    assertFalse(l.includes(0).isEmpty());
    assertEquals(1, l.includes(0).size());
    assertFalse(l.includes(0).head());
    assertEquals(List.of(false), l.includes(0));
    assertFalse(List.of().includes(0).isEmpty());
    assertEquals(1, List.of().includes(0).size());
    assertFalse(List.of().includes(0).head());
    assertEquals(List.of(false), List.of().includes(null));
  }

  @Test
  public void includesAll() {
    var l = List.of(1, 2, 3, null, 5);
    assertFalse(l.includesAll(List.of(null, 1)).isEmpty());
    assertEquals(1, l.includesAll(List.of(null, 1)).size());
    assertTrue(l.includesAll(List.of(null, 1)).head());
    assertEquals(List.of(true), l.includesAll(List.of(null, 1)));
    assertFalse(l.includesAll(List.of(0, 1)).isEmpty());
    assertEquals(1, l.includesAll(List.of(0, 1)).size());
    assertFalse(l.includesAll(List.of(0, 1)).head());
    assertEquals(List.of(false), l.includesAll(List.of(0, 1)));
    assertFalse(l.includesAll(List.of()).isEmpty());
    assertEquals(1, l.includesAll(List.of()).size());
    assertTrue(l.includesAll(List.of()).head());
    assertEquals(List.of(true), l.includesAll(List.of()));
    assertFalse(List.of().includesAll(List.of(null, 1)).isEmpty());
    assertEquals(1, List.of().includesAll(List.of(null, 1)).size());
    assertFalse(List.of().includesAll(List.of(null, 1)).head());
    assertEquals(List.of(false), List.of().includesAll(List.of(null, 1)));
    assertFalse(List.of().includesAll(List.of()).isEmpty());
    assertEquals(1, List.of().includesAll(List.of()).size());
    assertTrue(List.of().includesAll(List.of()).head());
    assertEquals(List.of(true), List.of().includesAll(List.of()));
  }

  @Test
  public void includesSlice() {
    var l = List.of(1, 2, 3, null, 5);
    assertFalse(l.includesSlice(List.of(3, null)).isEmpty());
    assertEquals(1, l.includesSlice(List.of(3, null)).size());
    assertTrue(l.includesSlice(List.of(3, null)).head());
    assertEquals(List.of(true), l.includesSlice(List.of(3, null)));
    assertFalse(l.includesSlice(List.of(null, 3)).isEmpty());
    assertEquals(1, l.includesSlice(List.of(null, 3)).size());
    assertFalse(l.includesSlice(List.of(null, 3)).head());
    assertEquals(List.of(false), l.includesSlice(List.of(null, 3)));
    assertFalse(l.includesSlice(List.of()).isEmpty());
    assertEquals(1, l.includesSlice(List.of()).size());
    assertTrue(l.includesSlice(List.of()).head());
    assertEquals(List.of(true), l.includesSlice(List.of()));
    assertFalse(List.of().includesSlice(List.of(null, 1)).isEmpty());
    assertEquals(1, List.of().includesSlice(List.of(null, 1)).size());
    assertFalse(List.of().includesSlice(List.of(null, 1)).head());
    assertEquals(List.of(false), List.of().includesSlice(List.of(null, 1)));
    assertFalse(List.of().includesSlice(List.of()).isEmpty());
    assertEquals(1, List.of().includesSlice(List.of()).size());
    assertTrue(List.of().includesSlice(List.of()).head());
    assertEquals(List.of(true), List.of().includesSlice(List.of()));
  }

  @Test
  public void insertAllAt() {
    var l = List.of(1, 2, 3);
    assertFalse(l.insertAllAt(5, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAt(5, List.of(null, 5)).size());
    assertEquals(List.of(1, 2, 3, null, 5), l.insertAllAt(5, List.of(null, 5)));
    assertFalse(l.insertAllAt(3, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAt(3, List.of(null, 5)).size());
    assertEquals(List.of(1, 2, 3, null, 5), l.insertAllAt(3, List.of(null, 5)));
    assertFalse(l.insertAllAt(2, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAt(2, List.of(null, 5)).size());
    assertEquals(List.of(1, 2, null, 5, 3), l.insertAllAt(2, List.of(null, 5)));
    assertFalse(l.insertAllAt(1, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAt(1, List.of(null, 5)).size());
    assertEquals(List.of(1, null, 5, 2, 3), l.insertAllAt(1, List.of(null, 5)));
    assertFalse(l.insertAllAt(0, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAt(0, List.of(null, 5)).size());
    assertEquals(List.of(null, 5, 1, 2, 3), l.insertAllAt(0, List.of(null, 5)));
    assertFalse(l.insertAllAt(-7, List.of(null, 5)).isEmpty());
    assertEquals(5, l.insertAllAt(-7, List.of(null, 5)).size());
    assertEquals(List.of(null, 5, 1, 2, 3), l.insertAllAt(-7, List.of(null, 5)));

    assertFalse(List.of().insertAllAt(5, List.of(null, 5)).isEmpty());
    assertEquals(2, List.of().insertAllAt(5, List.of(null, 5)).size());
    assertEquals(List.of(null, 5), List.of().insertAllAt(5, List.of(null, 5)));

    Iterable<Object> iterable = () -> List.of().iterator();
    assertFalse(List.wrap(iterable).insertAllAt(5, List.of(null, 5)).isEmpty());
    assertEquals(2, List.wrap(iterable).insertAllAt(5, List.of(null, 5)).size());
    assertEquals(List.of(null, 5), List.wrap(iterable).insertAllAt(5, List.of(null, 5)));
  }

  @Test
  public void insertAt() {
    var l = List.of(1, 2, 3);
    assertFalse(l.insertAt(5, null).isEmpty());
    assertEquals(4, l.insertAt(5, null).size());
    assertEquals(List.of(1, 2, 3, null), l.insertAt(5, null));
    assertFalse(l.insertAt(3, null).isEmpty());
    assertEquals(4, l.insertAt(3, null).size());
    assertEquals(List.of(1, 2, 3, null), l.insertAt(3, null));
    assertFalse(l.insertAt(2, null).isEmpty());
    assertEquals(4, l.insertAt(2, null).size());
    assertEquals(List.of(1, 2, null, 3), l.insertAt(2, null));
    assertFalse(l.insertAt(1, null).isEmpty());
    assertEquals(4, l.insertAt(1, null).size());
    assertEquals(List.of(1, null, 2, 3), l.insertAt(1, null));
    assertFalse(l.insertAt(0, null).isEmpty());
    assertEquals(4, l.insertAt(0, null).size());
    assertEquals(List.of(null, 1, 2, 3), l.insertAt(0, null));
    assertFalse(l.insertAt(-7, null).isEmpty());
    assertEquals(4, l.insertAt(-7, null).size());
    assertEquals(List.of(null, 1, 2, 3), l.insertAt(-7, null));

    assertFalse(List.of().insertAt(5, null).isEmpty());
    assertEquals(1, List.of().insertAt(5, null).size());
    assertEquals(List.of(null), List.of().insertAt(5, null));

    Iterable<Object> iterable = () -> List.of().iterator();
    assertFalse(List.wrap(iterable).insertAt(5, null).isEmpty());
    assertEquals(1, List.wrap(iterable).insertAt(5, null).size());
    assertEquals(List.of(null), List.wrap(iterable).insertAt(5, null));
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
}
