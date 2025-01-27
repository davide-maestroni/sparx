/*
 * Copyright 2025 Davide Maestroni
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
package sparx.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import sparx.util.function.Predicate;

public class DequeArrayListTests {

  @Test
  public void constructors() {
    assertTrue(new DequeArrayList<>().isEmpty());
    assertTrue(new DequeArrayList<>(0).isEmpty());
    assertTrue(new DequeArrayList<>(10).isEmpty());
    assertTrue(new DequeArrayList<>(List.of()).isEmpty());
    assertThrows(IllegalArgumentException.class, () -> new DequeArrayList<>(-1));
    var list = new DequeArrayList<String>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    assertEquals(list, new DequeArrayList<>(list));
    assertEquals(list, new DequeArrayList<>(List.of("1", "2", "3", "4")));
    assertEquals(List.of("1", "2", "3", "4"), new DequeArrayList<>(List.of("1", "2", "3", "4")));
    assertEquals(list, list.clone());
  }

  @Test
  @SuppressWarnings("ConstantValue")
  public void add() {
    var list = new DequeArrayList<String>();
    assertTrue(list.isEmpty());
    list.add("1");
    assertFalse(list.isEmpty());
    assertEquals(1, list.size());
    assertEquals("1", list.get(0));
    assertEquals("1", list.element());
    assertEquals("1", list.getFirst());
    assertEquals("1", list.getLast());
    list.add("2");
    assertFalse(list.isEmpty());
    assertEquals(2, list.size());
    assertEquals("1", list.get(0));
    assertEquals("2", list.get(1));
    assertEquals("1", list.element());
    assertEquals("1", list.getFirst());
    assertEquals("2", list.getLast());
    list.add("3");
    list.add("4");
    list.add("5");
    list.add("6");
    list.add("7");
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
    }
    list.add("8");
    list.add("9");
    list.add("10");
    list.add("11");
    list.add("12");
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
    }
    assertFalse(list.isEmpty());
    assertEquals(2, list.size());
    assertEquals("11", list.get(0));
    assertEquals("12", list.get(1));
    assertEquals("11", list.element());
    assertEquals("11", list.getFirst());
    assertEquals("12", list.getLast());

    // corner cases
    list = new DequeArrayList<>(0);
    list.add("1");
    assertEquals(List.of("1"), list);
    list = new DequeArrayList<>(1);
    list.add("1");
    list.add("2");
    assertEquals(List.of("1", "2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("2");
    assertEquals(List.of("2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("1");
    list.add("2");
    list.add("3");
    assertEquals(List.of("1", "2", "3"), list);
  }

  @Test
  @SuppressWarnings({"DataFlowIssue", "RedundantCollectionOperation"})
  public void addAll() {
    // first < last (start) - no resize
    var list = new DequeArrayList<String>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.addAll(0, List.of("5", "6", "7", "8"));
    assertEquals(List.of("5", "6", "7", "8", "1", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.addAll(2, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "5", "6", "7", "8", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.addAll(4, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8"), list);

    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.addAll(List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8"), list);

    // first < last (start) - resize
    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.addAll(0, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("5", "6", "7", "8", "9", "1", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.addAll(2, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "5", "6", "7", "8", "9", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.addAll(4, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"), list);

    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.addAll(List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"), list);

    // first < last (middle) - no resize
    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.addAll(0, List.of("5", "6", "7", "8"));
    assertEquals(List.of("5", "6", "7", "8", "1", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.addAll(2, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "5", "6", "7", "8", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.addAll(4, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8"), list);

    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.addAll(List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8"), list);

    // first < last (middle) - resize
    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.addAll(0, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("5", "6", "7", "8", "9", "1", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.addAll(2, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "5", "6", "7", "8", "9", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.addAll(4, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"), list);

    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.addAll(List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"), list);

    // first < last (end) - no resize
    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.addAll(0, List.of("5", "6", "7", "8"));
    assertEquals(List.of("5", "6", "7", "8", "1", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.addAll(2, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "5", "6", "7", "8", "3", "4"), list);

    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.addAll(4, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8"), list);

    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.addAll(List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8"), list);

    // first < last (end) - resize
    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.addAll(0, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("5", "6", "7", "8", "9", "1", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.addAll(2, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "5", "6", "7", "8", "9", "3", "4"), list);

    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.addAll(4, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"), list);

    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.addAll(List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"), list);

    // last < first - no resize
    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(0, List.of("5", "6", "7", "8"));
    assertEquals(List.of("5", "6", "7", "8", "1", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(1, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "5", "6", "7", "8", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(2, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "5", "6", "7", "8", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(3, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "3", "5", "6", "7", "8", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(4, List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(List.of("5", "6", "7", "8"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8"), list);

    // last < first - resize
    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(0, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("5", "6", "7", "8", "9", "1", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(1, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "5", "6", "7", "8", "9", "2", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(2, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "5", "6", "7", "8", "9", "3", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(3, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "3", "5", "6", "7", "8", "9", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(4, List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.addAll(List.of("5", "6", "7", "8", "9"));
    assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"), list);

    // corner cases
    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    assertFalse(list.addAll(List.of()));
    assertFalse(list.addAll(0, List.of()));
    assertFalse(list.addAll(4, List.of()));
    assertEquals(List.of("1", "2", "3", "4"), list);
    assertThrows(NullPointerException.class, () -> new DequeArrayList<>().addAll(null));
    assertThrows(NullPointerException.class, () -> new DequeArrayList<>().addAll(0, null));
    assertThrows(IndexOutOfBoundsException.class, () -> new DequeArrayList<>().addAll(-1, List.of()));
    assertThrows(IndexOutOfBoundsException.class, () -> new DequeArrayList<>().addAll(1, List.of()));

    // corner cases
    list = new DequeArrayList<>(0);
    list.addAll(List.of("1"));
    assertEquals(List.of("1"), list);
    list = new DequeArrayList<>(1);
    list.addAll(List.of("1", "2"));
    assertEquals(List.of("1", "2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.addAll(List.of("2"));
    assertEquals(List.of("2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.addAll(List.of("1", "2", "3"));
    assertEquals(List.of("1", "2", "3"), list);
  }

  @Test
  public void addFirst() {
    var list = new DequeArrayList<String>();
    assertTrue(list.isEmpty());
    list.addFirst("1");
    assertFalse(list.isEmpty());
    assertEquals(1, list.size());
    assertEquals("1", list.get(0));
    assertEquals("1", list.element());
    assertEquals("1", list.getFirst());
    assertEquals("1", list.getLast());
    list.addFirst("2");
    assertFalse(list.isEmpty());
    assertEquals(2, list.size());
    assertEquals("2", list.get(0));
    assertEquals("1", list.get(1));
    assertEquals("2", list.element());
    assertEquals("2", list.getFirst());
    assertEquals("1", list.getLast());
    list.addFirst("3");
    list.addFirst("4");
    list.addFirst("5");
    list.addFirst("6");
    list.addFirst("7");
    for (int i = 0; i < 5; i++) {
      list.removeLast();
    }
    list.addFirst("8");
    list.addFirst("9");
    list.addFirst("10");
    list.addFirst("11");
    list.addFirst("12");
    for (int i = 0; i < 5; i++) {
      list.removeLast();
    }
    assertFalse(list.isEmpty());
    assertEquals(2, list.size());
    assertEquals("12", list.get(0));
    assertEquals("11", list.get(1));
    assertEquals("12", list.element());
    assertEquals("12", list.getFirst());
    assertEquals("11", list.getLast());

    // corner cases
    list = new DequeArrayList<>(0);
    list.addFirst("1");
    assertEquals(List.of("1"), list);
    list = new DequeArrayList<>(1);
    list.addFirst("1");
    list.addFirst("2");
    assertEquals(List.of("2", "1"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.addFirst("2");
    assertEquals(List.of("2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.addFirst("1");
    list.addFirst("2");
    list.addFirst("3");
    assertEquals(List.of("3", "2", "1"), list);
  }

  @Test
  @SuppressWarnings("ConstantValue")
  public void addIndex() {
    var list = new DequeArrayList<String>();
    var l = list;
    assertThrows(IndexOutOfBoundsException.class, () -> l.remove(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.remove(0));
    assertThrows(IndexOutOfBoundsException.class, () -> l.remove(1));
    assertTrue(list.isEmpty());
    list.add(0, "1");
    assertFalse(list.isEmpty());
    assertEquals(1, list.size());
    assertEquals("1", list.get(0));
    assertEquals("1", list.element());
    assertEquals("1", list.getFirst());
    assertEquals("1", list.getLast());
    list.add(1, "2");
    assertFalse(list.isEmpty());
    assertEquals(2, list.size());
    assertEquals("1", list.get(0));
    assertEquals("2", list.get(1));
    assertEquals("1", list.element());
    assertEquals("1", list.getFirst());
    assertEquals("2", list.getLast());
    list.add(1, "3");
    assertEquals(3, list.size());
    assertEquals("1", list.get(0));
    assertEquals("3", list.get(1));
    assertEquals("2", list.get(2));
    assertEquals("1", list.element());
    assertEquals("1", list.getFirst());
    assertEquals("2", list.getLast());
    list.add("4");
    list.add("5");
    list.add("6");
    list.add("7");
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
    }
    list.add("8");
    list.add("9");
    list.add("10");
    list.add("11");
    list.add("12");
    // 6 7 8 9 10 11 12
    assertEquals(7, list.size());
    list.add(1, "13");
    // 6 13 7 8 9 10 11 12
    assertEquals(8, list.size());
    list.add(6, "14");
    // 6 13 7 8 9 10 14 11 12
    assertEquals(9, list.size());
    assertEquals(List.of("6", "13", "7", "8", "9", "10", "14", "11", "12"), list);
    list.clear();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add(1, "4");
    assertEquals(List.of("1", "4", "2", "3"), list);
    list.clear();
    list.add("1");
    list.addFirst("2");
    list.addFirst("3");
    list.addFirst("4");
    list.addFirst("5");
    list.add(3, "6");
    assertEquals(List.of("5", "4", "3", "6", "2", "1"), list);

    // corner cases
    list = new DequeArrayList<>(0);
    list.add(0, "1");
    assertEquals(List.of("1"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.add("3");
    list.add(1, "2");
    assertEquals(List.of("1", "2", "3"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add(0, "2");
    assertEquals(List.of("2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("1");
    list.add("3");
    list.add(1, "2");
    assertEquals(List.of("1", "2", "3"), list);
  }

  @Test
  public void autoShrink() {
    var list = new DequeArrayList<String>(64, true);
    assertEquals(64, list.capacity());
    list.add("");
    list.removeFirst();
    assertEquals(64, list.capacity());
    for (int i = 0; i < 64; i++) {
      list.add(Integer.toString(i));
    }
    assertEquals(64, list.capacity());
    for (int i = 0; i < 60; i++) {
      list.removeFirst();
    }
    assertEquals(List.of("60", "61", "62", "63"), list);
    assertTrue(list.capacity() < 64);

    list = new DequeArrayList<>(64, true);
    for (int i = 0; i < 64; i++) {
      list.add(Integer.toString(i));
    }
    for (int i = 0; i < 60; i++) {
      list.remove(list.size() >> 1);
    }
    assertEquals(List.of("0", "1", "62", "63"), list);
    assertTrue(list.capacity() < 64);

    list = new DequeArrayList<>(64, true);
    for (int i = 0; i < 58; i++) {
      list.add(Integer.toString(6 + i));
    }
    for (int i = 0; i < 6; i++) {
      list.addFirst(Integer.toString(5 - i));
    }
    for (int i = 0; i < 60; i++) {
      list.remove(list.size() - 3);
    }
    assertEquals(List.of("0", "1", "62", "63"), list);
    assertTrue(list.capacity() < 64);

    list = new DequeArrayList<>(64, true);
    list.addFirst("0");
    for (int i = 0; i < 63; i++) {
      list.add(Integer.toString(1 + i));
    }
    for (int i = 0; i < 60; i++) {
      list.remove(list.size() - 2);
    }
    assertEquals(List.of("0", "1", "2", "63"), list);
    assertTrue(list.capacity() < 64);

    list = new DequeArrayList<>(64, true);
    assertEquals(64, list.capacity());
    list.add("");
    list.removeFirst();
    assertEquals(64, list.capacity());
    for (int i = 0; i < 64; i++) {
      list.add(Integer.toString(i));
    }
    assertEquals(64, list.capacity());
    list.removeRange(0, 60);
    assertEquals(List.of("60", "61", "62", "63"), list);
    assertTrue(list.capacity() < 64);

    list = new DequeArrayList<>(64, true);
    for (int i = 0; i < 64; i++) {
      list.add(Integer.toString(i));
    }
    list.add("");
    list.removeLast();
    list.removeRange(2, 62);
    assertEquals(List.of("0", "1", "62", "63"), list);
    assertTrue(list.capacity() < 64);

    list = new DequeArrayList<>(64, true);
    for (int i = 0; i < 32; i++) {
      list.add(Integer.toString(32 + i));
    }
    for (int i = 0; i < 32; i++) {
      list.addFirst(Integer.toString(31 - i));
    }
    list.removeRange(2, 62);
    assertEquals(List.of("0", "1", "62", "63"), list);
    assertTrue(list.capacity() < 64);

    list = new DequeArrayList<>(64, true);
    list.addFirst("0");
    for (int i = 0; i < 63; i++) {
      list.add(Integer.toString(1 + i));
    }
    list.removeRange(3, 63);
    assertEquals(List.of("0", "1", "2", "63"), list);
    assertTrue(list.capacity() < 64);
  }

  @Test
  @SuppressWarnings("ConstantValue")
  public void clear() {
    var list = new DequeArrayList<String>();
    assertTrue(list.isEmpty());
    list.add("1");
    list.clear();
    assertTrue(list.isEmpty());
    list.add("1");
    list.add("2");
    list.clear();
    assertTrue(list.isEmpty());
    assertEquals(0, list.size());
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.add("5");
    list.add("6");
    list.add("7");
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
    }
    list.add("8");
    list.add("9");
    list.add("10");
    list.add("11");
    list.add("12");
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
    }
    list.clear();
    assertTrue(list.isEmpty());
    assertEquals(0, list.size());
    list.clear();
    assertTrue(list.isEmpty());
    assertEquals(0, list.size());
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    assertThrows(NoSuchElementException.class, list::element);
    assertThrows(NoSuchElementException.class, list::getFirst);
    assertThrows(NoSuchElementException.class, list::getLast);
  }

  @Test
  public void get() {
    var list = new DequeArrayList<String>();
    assertTrue(list.isEmpty());
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.add("5");
    list.add("6");
    list.add("7");
    assertEquals("1", list.get(0));
    assertEquals("2", list.get(1));
    assertEquals("4", list.get(3));
    assertEquals("6", list.get(5));
    assertEquals("7", list.get(6));
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
      assertEquals(Integer.toString(i + 2), list.get(0));
    }
    list.add("8");
    list.add("9");
    list.add("10");
    list.add("11");
    list.add("12");
    assertEquals("6", list.get(0));
    assertEquals("7", list.get(1));
    assertEquals("9", list.get(3));
    assertEquals("11", list.get(5));
    assertEquals("12", list.get(6));
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
      assertEquals(Integer.toString(i + 7), list.get(0));
    }
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
  }

  @Test
  public void indexOf() {
    var list = new DequeArrayList<String>();
    assertTrue(list.isEmpty());
    assertEquals(-1, list.indexOf("1"));
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.add("5");
    list.add("6");
    list.add("7");
    assertEquals(0, list.indexOf("1"));
    assertEquals(1, list.indexOf("2"));
    assertEquals(3, list.indexOf("4"));
    assertEquals(5, list.indexOf("6"));
    assertEquals(6, list.indexOf("7"));
    assertEquals(-1, list.indexOf("8"));
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
      assertEquals(0, list.indexOf(Integer.toString(i + 2)));
    }
    list.add("8");
    list.add("9");
    list.add("10");
    list.add("11");
    list.add("12");
    assertEquals(0, list.indexOf("6"));
    assertEquals(1, list.indexOf("7"));
    assertEquals(3, list.indexOf("9"));
    assertEquals(5, list.indexOf("11"));
    assertEquals(6, list.indexOf("12"));
    assertEquals(-1, list.indexOf("5"));
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
      assertEquals(1, list.indexOf(Integer.toString(i + 8)));
    }
    list.add("12");
    list.add("11");
    assertEquals(0, list.indexOf("11"));
    assertEquals(1, list.indexOf("12"));
    list.set(1, null);
    assertEquals(1, list.indexOf(null));
    assertEquals(2, list.indexOf("12"));

    list = new DequeArrayList<>(1);
    list.add("1");
    assertEquals(0, list.indexOf("1"));
    assertEquals(-1, list.indexOf("2"));
    assertEquals(-1, list.indexOf(null));
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("1");
    list.add("2");
    assertEquals(0, list.indexOf("1"));
    assertEquals(1, list.indexOf("2"));
    assertEquals(-1, list.indexOf("3"));
    assertEquals(-1, list.indexOf(null));
  }

  @Test
  public void lastIndexOf() {
    var list = new DequeArrayList<String>();
    assertTrue(list.isEmpty());
    assertEquals(-1, list.lastIndexOf("1"));
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.add("5");
    list.add("6");
    list.add("7");
    assertEquals(0, list.lastIndexOf("1"));
    assertEquals(1, list.lastIndexOf("2"));
    assertEquals(3, list.lastIndexOf("4"));
    assertEquals(5, list.lastIndexOf("6"));
    assertEquals(6, list.lastIndexOf("7"));
    assertEquals(-1, list.lastIndexOf("8"));
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
      assertEquals(0, list.lastIndexOf(Integer.toString(i + 2)));
    }
    list.add("8");
    list.add("9");
    list.add("10");
    list.add("11");
    list.add("12");
    assertEquals(0, list.lastIndexOf("6"));
    assertEquals(1, list.lastIndexOf("7"));
    assertEquals(3, list.lastIndexOf("9"));
    assertEquals(5, list.lastIndexOf("11"));
    assertEquals(6, list.lastIndexOf("12"));
    assertEquals(-1, list.lastIndexOf("5"));
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
      assertEquals(1, list.lastIndexOf(Integer.toString(i + 8)));
    }
    list.add("12");
    list.add("11");
    assertEquals(3, list.lastIndexOf("11"));
    assertEquals(2, list.lastIndexOf("12"));
    list.set(1, null);
    assertEquals(1, list.lastIndexOf(null));
    assertEquals(2, list.lastIndexOf("12"));

    list = new DequeArrayList<>(1);
    list.add("1");
    assertEquals(0, list.lastIndexOf("1"));
    assertEquals(-1, list.lastIndexOf("2"));
    assertEquals(-1, list.lastIndexOf(null));
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("1");
    list.add("2");
    assertEquals(0, list.lastIndexOf("1"));
    assertEquals(1, list.lastIndexOf("2"));
    assertEquals(-1, list.lastIndexOf("3"));
    assertEquals(-1, list.lastIndexOf(null));
  }

  @Test
  public void ascendingIterator() {
    var list = new DequeArrayList<String>();
    var iterator = list.iterator();
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    list.offer("1");
    list.offer("2");
    list.offer("3");
    list.offer("4");
    list.offerLast("5");
    iterator = list.iterator();
    for (int i = 0; i < 5; i++) {
      assertTrue(iterator.hasNext());
      assertEquals(Integer.toString(i + 1), iterator.next());
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    iterator = list.iterator();
    assertThrows(IllegalStateException.class, iterator::remove);
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("2", "3", "4", "5"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("2", "4", "5"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("2", "4"), list);
    list.clear();
    list.offer("1");
    list.offerFirst("2");
    list.offerFirst("3");
    list.offerFirst("4");
    list.offerFirst("5");
    iterator = list.iterator();
    for (int i = 0; i < 5; i++) {
      assertTrue(iterator.hasNext());
      assertEquals(Integer.toString(5 - i), iterator.next());
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    iterator = list.iterator();
    assertThrows(IllegalStateException.class, iterator::remove);
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("4", "3", "2", "1"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("4", "2", "1"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("4", "2"), list);
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    list.clear();
    list.offer("1");
    list.offer("2");
    list.offer("3");
    list.offer("4");
    iterator = list.iterator();
    iterator.next();
    list.remove(2);
    assertThrows(ConcurrentModificationException.class, iterator::next);
    assertThrows(ConcurrentModificationException.class, iterator::remove);
    list.clear();
    list.offer("1");
    list.offerFirst("2");
    list.offerFirst("3");
    list.offerFirst("4");
    iterator = list.iterator();
    iterator.next();
    list.remove(2);
    assertThrows(ConcurrentModificationException.class, iterator::next);
    assertThrows(ConcurrentModificationException.class, iterator::remove);
  }

  @Test
  public void descendingIterator() {
    var list = new DequeArrayList<String>();
    var iterator = list.descendingIterator();
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    list.offer("1");
    list.offer("2");
    list.offer("3");
    list.offer("4");
    list.offerLast("5");
    iterator = list.descendingIterator();
    for (int i = 0; i < 5; i++) {
      assertTrue(iterator.hasNext());
      assertEquals(Integer.toString(5 - i), iterator.next());
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    iterator = list.descendingIterator();
    assertThrows(IllegalStateException.class, iterator::remove);
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("1", "2", "3", "4"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("1", "2", "4"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("2", "4"), list);
    list.clear();
    list.offer("1");
    list.offerFirst("2");
    list.offerFirst("3");
    list.offerFirst("4");
    list.offerFirst("5");
    iterator = list.iterator();
    for (int i = 0; i < 5; i++) {
      assertTrue(iterator.hasNext());
      assertEquals(Integer.toString(5 - i), iterator.next());
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    iterator = list.iterator();
    assertThrows(IllegalStateException.class, iterator::remove);
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("4", "3", "2", "1"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("4", "2", "1"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("4", "2"), list);
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    list.clear();
    list.offer("1");
    list.offer("2");
    list.offer("3");
    list.offer("4");
    iterator = list.descendingIterator();
    iterator.next();
    list.remove(2);
    assertThrows(ConcurrentModificationException.class, iterator::next);
    assertThrows(ConcurrentModificationException.class, iterator::remove);
    list.clear();
    list.offer("1");
    list.offerFirst("2");
    list.offerFirst("3");
    list.offerFirst("4");
    iterator = list.descendingIterator();
    iterator.next();
    list.remove(2);
    assertThrows(ConcurrentModificationException.class, iterator::next);
    assertThrows(ConcurrentModificationException.class, iterator::remove);
  }

  @Test
  public void ensureCapacity() {
    var list = new DequeArrayList<String>();
    assertFalse(list.ensureCapacity(1));
    assertTrue(list.capacity() >= 1);
    assertTrue(list.capacity() < 1000);
    assertTrue(list.ensureCapacity(1000));
    assertTrue(list.capacity() >= 1000);
    assertThrows(IllegalStateException.class, () -> list.ensureCapacity(Integer.MAX_VALUE));
  }


  @Test
  public void freeCapacity() {
    var list = new DequeArrayList<String>();
    list.add("1");
    list.add("2");
    assertFalse(list.freeCapacity(1000));
    assertTrue(list.capacity() < 1000);
    assertTrue(list.capacity() >= 2);
    assertTrue(list.freeCapacity(1));
    assertTrue(list.capacity() >= 2);
    assertFalse(list.freeCapacity(1));
    assertTrue(list.capacity() >= 2);
  }

  @Test
  public void listIterator() {
    var list = new DequeArrayList<String>();
    assertThrows(IndexOutOfBoundsException.class, () -> list.listIterator(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> list.listIterator(1));
    var iterator = list.listIterator();
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    assertThrows(NoSuchElementException.class, iterator::previous);
    list.offer("1");
    list.offer("2");
    list.offer("3");
    list.offer("4");
    list.offerLast("5");
    iterator = list.listIterator();
    for (int i = 0; i < 5; i++) {
      assertTrue(iterator.hasNext());
      assertEquals(i, iterator.nextIndex());
      assertEquals(i - 1, iterator.previousIndex());
      assertEquals(Integer.toString(i + 1), iterator.next());
      assertEquals(i + 1, iterator.nextIndex());
      assertEquals(i, iterator.previousIndex());
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    for (int i = 0; i < 5; i++) {
      assertTrue(iterator.hasPrevious());
      assertEquals(5 - i, iterator.nextIndex());
      assertEquals(4 - i, iterator.previousIndex());
      assertEquals(Integer.toString(5 - i), iterator.previous());
      assertEquals(4 - i, iterator.nextIndex());
      assertEquals(3 - i, iterator.previousIndex());
    }
    assertFalse(iterator.hasPrevious());
    assertThrows(NoSuchElementException.class, iterator::previous);
    iterator = list.listIterator();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertFalse(iterator.hasPrevious());
    assertThrows(NoSuchElementException.class, iterator::previous);
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("2", "3", "4", "5"), list);
    iterator.next();
    iterator.next();
    iterator.previous();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("2", "4", "5"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("2", "4"), list);
    list.clear();
    list.offer("1");
    list.offerFirst("2");
    list.offerFirst("3");
    list.offerFirst("4");
    list.offerFirst("5");
    iterator = list.listIterator();
    for (int i = 0; i < 5; i++) {
      assertTrue(iterator.hasNext());
      assertEquals(i, iterator.nextIndex());
      assertEquals(i - 1, iterator.previousIndex());
      assertEquals(Integer.toString(5 - i), iterator.next());
      assertEquals(i + 1, iterator.nextIndex());
      assertEquals(i, iterator.previousIndex());
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    for (int i = 0; i < 5; i++) {
      assertTrue(iterator.hasPrevious());
      assertEquals(5 - i, iterator.nextIndex());
      assertEquals(4 - i, iterator.previousIndex());
      assertEquals(Integer.toString(i + 1), iterator.previous());
      assertEquals(4 - i, iterator.nextIndex());
      assertEquals(3 - i, iterator.previousIndex());
    }
    assertFalse(iterator.hasPrevious());
    assertThrows(NoSuchElementException.class, iterator::previous);
    iterator = list.listIterator();
    assertThrows(IllegalStateException.class, iterator::remove);
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertFalse(iterator.hasPrevious());
    assertThrows(NoSuchElementException.class, iterator::previous);
    assertEquals(List.of("4", "3", "2", "1"), list);
    iterator.next();
    iterator.next();
    iterator.previous();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("4", "2", "1"), list);
    iterator.next();
    iterator.next();
    iterator.remove();
    assertThrows(IllegalStateException.class, iterator::remove);
    assertEquals(List.of("4", "2"), list);
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    list.clear();
    list.offer("1");
    list.offer("2");
    list.offer("3");
    list.offer("4");
    iterator = list.listIterator();
    iterator.next();
    list.remove(2);
    assertThrows(ConcurrentModificationException.class, iterator::next);
    assertThrows(ConcurrentModificationException.class, iterator::previous);
    assertThrows(ConcurrentModificationException.class, iterator::remove);
    list.clear();
    list.offer("1");
    list.offerFirst("2");
    list.offerFirst("3");
    list.offerFirst("4");
    iterator = list.listIterator();
    iterator.next();
    iterator.previous();
    list.remove(2);
    assertThrows(ConcurrentModificationException.class, iterator::next);
    assertThrows(ConcurrentModificationException.class, iterator::previous);
    assertThrows(ConcurrentModificationException.class, iterator::remove);
  }

  @Test
  public void listIteratorAdd() {
    var list = new DequeArrayList<String>();
    var iterator = list.listIterator();
    iterator.add("1");
    assertEquals(List.of("1"), list);
    list.clear();
    list.offer("1");
    list.offer("2");
    list.offer("3");
    list.offer("4");
    iterator = list.listIterator();
    iterator.next();
    iterator.next();
    iterator.add("5");
    assertEquals(List.of("1", "2", "5", "3", "4"), list);
    assertEquals("5", iterator.previous());
    iterator.add("6");
    assertEquals(List.of("1", "2", "6", "5", "3", "4"), list);
    iterator.next();
    iterator.next();
    iterator.next();
    iterator.add("7");
    assertEquals(List.of("1", "2", "6", "5", "3", "4", "7"), list);
    for (int i = 0; i < 7; i++) {
      iterator.previous();
    }
    iterator.add("8");
    assertEquals(List.of("8", "1", "2", "6", "5", "3", "4", "7"), list);
    iterator.add("9");
    assertEquals(List.of("8", "9", "1", "2", "6", "5", "3", "4", "7"), list);
    iterator.next();
    iterator.next();
    iterator.previous();
    iterator.add("10");
    assertEquals(List.of("8", "9", "1", "10", "2", "6", "5", "3", "4", "7"), list);

    // corner cases
    list = new DequeArrayList<>(0);
    iterator = list.listIterator();
    iterator.add("1");
    assertFalse(iterator.hasNext());
    assertEquals("1", iterator.previous());
    assertEquals(List.of("1"), list);
    list = new DequeArrayList<>(1);
    list.add("1");
    iterator = list.listIterator(1);
    iterator.add("2");
    assertFalse(iterator.hasNext());
    assertEquals("2", iterator.previous());
    assertEquals(List.of("1", "2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    iterator = list.listIterator();
    iterator.add("2");
    assertFalse(iterator.hasNext());
    assertEquals("2", iterator.previous());
    assertEquals(List.of("2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("1");
    list.add("3");
    iterator = list.listIterator(1);
    iterator.add("2");
    assertTrue(iterator.hasNext());
    assertEquals("2", iterator.previous());
    assertEquals(List.of("1", "2", "3"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("1");
    list.add("3");
    iterator = list.listIterator(1);
    iterator.add("2");
    assertEquals("3", iterator.next());
    assertEquals(List.of("1", "2", "3"), list);
  }

  @Test
  public void listIteratorSet() {
    var list = new DequeArrayList<String>();
    var emptyIterator = list.listIterator();
    assertThrows(IndexOutOfBoundsException.class, () -> emptyIterator.set("1"));
    list.offer("1");
    list.offer("2");
    list.offer("3");
    list.offer("4");
    var iterator = list.listIterator();
    iterator.next();
    iterator.next();
    iterator.set("5");
    assertEquals(List.of("1", "5", "3", "4"), list);
    assertEquals("5", iterator.previous());
    iterator.set("6");
    assertEquals(List.of("1", "6", "3", "4"), list);
    iterator.next();
    iterator.next();
    iterator.next();
    iterator.set("7");
    assertEquals(List.of("1", "6", "3", "7"), list);
    iterator.previous();
    iterator.set("8");
    assertEquals(List.of("1", "6", "3", "8"), list);
    list.clear();
    list.offerFirst("1");
    list.offerFirst("2");
    list.offerFirst("3");
    list.offerFirst("4");
    iterator = list.listIterator();
    iterator.next();
    iterator.next();
    iterator.set("5");
    assertEquals(List.of("4", "5", "2", "1"), list);
    assertEquals("5", iterator.previous());
    iterator.set("6");
    assertEquals(List.of("4", "6", "2", "1"), list);
    iterator.next();
    iterator.next();
    iterator.next();
    iterator.set("7");
    assertEquals(List.of("4", "6", "2", "7"), list);
    iterator.set("8");
    assertEquals(List.of("4", "6", "2", "8"), list);
  }

  @Test
  @SuppressWarnings("ConstantValue")
  public void peek() {
    var list = new DequeArrayList<String>();
    assertTrue(list.isEmpty());
    assertNull(list.peek());
    assertNull(list.peekFirst());
    assertNull(list.peekLast());
    list.add(null);
    assertFalse(list.isEmpty());
    assertNull(list.peek());
    assertNull(list.peekFirst());
    assertNull(list.peekLast());
    assertEquals(Collections.singletonList(null), list);
    list.add("1");
    assertNull(list.peek());
    assertNull(list.peekFirst());
    assertEquals("1", list.peekLast());
    assertEquals(Arrays.asList(null, "1"), list);
    list.addFirst("0");
    assertEquals("0", list.peek());
    assertEquals("0", list.peekFirst());
    assertEquals("1", list.peekLast());
    assertEquals(Arrays.asList("0", null, "1"), list);
  }

  @Test
  @SuppressWarnings("ConstantValue")
  public void poll() {
    var list = new DequeArrayList<String>();
    assertTrue(list.isEmpty());
    assertNull(list.poll());
    assertNull(list.pollFirst());
    assertNull(list.pollLast());
    list.add(null);
    assertFalse(list.isEmpty());
    assertNull(list.poll());
    assertTrue(list.isEmpty());
    list.add(null);
    assertFalse(list.isEmpty());
    assertNull(list.pollFirst());
    assertTrue(list.isEmpty());
    list.add(null);
    assertFalse(list.isEmpty());
    assertNull(list.pollLast());
    assertTrue(list.isEmpty());
    list.add("0");
    assertFalse(list.isEmpty());
    assertEquals("0", list.poll());
    assertTrue(list.isEmpty());
    list.add("0");
    assertFalse(list.isEmpty());
    assertEquals("0", list.pollFirst());
    assertTrue(list.isEmpty());
    list.add("0");
    assertFalse(list.isEmpty());
    assertEquals("0", list.pollLast());
    assertTrue(list.isEmpty());
    list.add("0");
    list.add("1");
    assertFalse(list.isEmpty());
    assertEquals("0", list.poll());
    assertEquals(Collections.singletonList("1"), list);
    list.addFirst("0");
    assertFalse(list.isEmpty());
    assertEquals("0", list.pollFirst());
    assertEquals(Collections.singletonList("1"), list);
    list.addFirst("0");
    assertFalse(list.isEmpty());
    assertEquals("1", list.pollLast());
    assertEquals(Collections.singletonList("0"), list);
  }

  @Test
  public void remove() {
    var list = new DequeArrayList<String>();
    assertThrows(NoSuchElementException.class, list::remove);
    assertThrows(NoSuchElementException.class, list::removeFirst);
    assertThrows(NoSuchElementException.class, list::removeLast);
    assertThrows(NoSuchElementException.class, list::pop);
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    assertEquals("1", list.remove());
    assertEquals(List.of("2", "3", "4"), list);
    assertEquals("2", list.removeFirst());
    assertEquals(List.of("3", "4"), list);
    assertEquals("4", list.removeLast());
    assertEquals(List.of("3"), list);
    list.clear();
    list.add("1");
    list.push("2");
    list.push("3");
    list.push("4");
    assertEquals("4", list.remove());
    assertEquals(List.of("3", "2", "1"), list);
    assertEquals("3", list.removeFirst());
    assertEquals(List.of("2", "1"), list);
    assertEquals("1", list.removeLast());
    assertEquals(List.of("2"), list);
  }

  @Test
  @SuppressWarnings({"SuspiciousMethodCalls", "DataFlowIssue"})
  public void removeAll() {
    var l = new DequeArrayList<String>();
    assertThrows(NullPointerException.class, () -> l.removeAll((Collection<?>) null));
    assertThrows(NullPointerException.class, () -> l.removeAll((Predicate<? super String>) null));

    var list = new DequeArrayList<String>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    assertFalse(list.removeAll(Objects::isNull));
    assertEquals(List.of("1", "2", "3", "4"), list);
    assertTrue(list.removeAll(e -> e.equals("1") || e.equals("3")));
    assertEquals(List.of("2", "4"), list);

    list = new DequeArrayList<>();
    list.addFirst("2");
    list.addFirst("1");
    list.add("3");
    list.add("4");
    assertFalse(list.removeAll(Objects::isNull));
    assertEquals(List.of("1", "2", "3", "4"), list);
    assertTrue(list.removeAll(e -> e.equals("1") || e.equals("3")));
    assertEquals(List.of("2", "4"), list);

    assertFalse(new DequeArrayList<>().removeAll(Objects::isNull));
    assertFalse(new DequeArrayList<>(0).removeAll(Objects::isNull));

    list = new DequeArrayList<>();
    list.add("1");
    list.add("1");
    list.add("4");
    list.add("3");
    list.add("4");
    list.add("1");
    assertFalse(list.removeAll(List.of("0", "2")));
    assertTrue(list.removeAll(List.of("1", "3")));
    assertEquals(List.of("4", "4"), list);

    var l1 = new DequeArrayList<Integer>();
    l1.add(1);
    l1.add(2);
    l1.add(null);
    l1.add(3);
    assertThrows(NullPointerException.class, () -> l1.removeAll(e -> e < 4));
    var r1 = new DequeArrayList<Integer>();
    r1.add(null);
    r1.add(3);
    assertEquals(r1, l1);

    var l2 = new DequeArrayList<Integer>();
    l2.add(1);
    l2.add(2);
    l2.add(null);
    l2.add(3);
    assertThrows(NullPointerException.class, () -> l2.removeAll(e -> e != 2));
    var r2 = new DequeArrayList<Integer>();
    r2.add(2);
    r2.add(null);
    r2.add(3);
    assertEquals(r2, l2);

    var l3 = new DequeArrayList<Integer>();
    l3.add(1);
    l3.add(2);
    l3.add(null);
    l3.add(3);
    assertThrows(NullPointerException.class, () -> l3.removeAll(e -> e > 1));
    var r3 = new DequeArrayList<Integer>();
    r3.add(1);
    r3.add(null);
    r3.add(3);
    assertEquals(r3, l3);
  }

  @Test
  public void removeIndex() {
    var list = new DequeArrayList<String>();
    var l = list;
    assertThrows(IndexOutOfBoundsException.class, () -> l.remove(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.remove(0));
    assertThrows(IndexOutOfBoundsException.class, () -> l.remove(1));
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    assertEquals("2", list.remove(1));
    assertEquals(List.of("1", "3", "4"), list);
    assertEquals("4", list.remove(2));
    assertEquals(List.of("1", "3"), list);
    assertEquals("1", list.remove(0));
    assertEquals(List.of("3"), list);
    list.clear();
    list.add("1");
    list.push("2");
    list.push("3");
    list.push("4");
    assertEquals("4", list.remove(0));
    assertEquals(List.of("3", "2", "1"), list);
    assertEquals("3", list.remove(0));
    assertEquals(List.of("2", "1"), list);
    assertEquals("1", list.remove(1));
    assertEquals(List.of("2"), list);
    list.addFirst("3");
    list.addFirst("4");
    list.addFirst("5");
    list.addFirst("6");
    assertEquals("3", list.remove(3));
    assertEquals(List.of("6", "5", "4", "2"), list);
    list = new DequeArrayList<>();
    list.addFirst("2");
    list.addFirst("1");
    list.add("3");
    list.add("4");
    list.add("5");
    assertEquals("3", list.remove(2));
    assertEquals(List.of("1", "2", "4", "5"), list);

    // corner cases
    list = new DequeArrayList<>(1);
    list.add("1");
    list.remove(0);
    assertEquals(List.of(), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.add("2");
    list.remove(0);
    assertEquals(List.of("2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("2");
    list.remove(0);
    assertEquals(List.of(), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("1");
    list.add("2");
    list.add("3");
    list.remove(1);
    assertEquals(List.of("1", "3"), list);
  }

  @Test
  public void removeOccurrence() {
    var list = new DequeArrayList<String>();
    assertFalse(list.removeFirstOccurrence(null));
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    assertFalse(list.removeFirstOccurrence(null));
    assertFalse(list.removeFirstOccurrence("0"));
    assertFalse(list.removeLastOccurrence(null));
    assertFalse(list.removeLastOccurrence("0"));
    assertTrue(list.removeFirstOccurrence("2"));
    assertEquals(List.of("1", "3", "4"), list);
    assertTrue(list.removeLastOccurrence("4"));
    assertEquals(List.of("1", "3"), list);
    list.add(null);
    list.add(1, null);
    list.add(1, null);
    list.add(1, null);
    list.addFirst(null);
    assertTrue(list.removeFirstOccurrence(null));
    assertEquals(Arrays.asList("1", null, null, null, "3", null), list);
    assertTrue(list.removeLastOccurrence(null));
    assertEquals(Arrays.asList("1", null, null, null, "3"), list);
    assertTrue(list.removeFirstOccurrence(null));
    assertEquals(Arrays.asList("1", null, null, "3"), list);
    assertTrue(list.removeLastOccurrence(null));
    assertEquals(Arrays.asList("1", null, "3"), list);
  }

  @Test
  public void removeRange() {
    var l = new DequeArrayList<String>();
    assertThrows(IndexOutOfBoundsException.class, () -> l.removeRange(-1, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> l.removeRange(0, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> l.removeRange(1, 2));
    l.add("1");
    assertThrows(IndexOutOfBoundsException.class, () -> l.removeRange(0, 2));
    assertThrows(IllegalArgumentException.class, () -> l.removeRange(0, -1));
    l.removeRange(0, 0);
    assertEquals(List.of("1"), l);

    // first < last (start)
    var list = new DequeArrayList<String>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeRange(0, 2);
    assertEquals(List.of("3", "4"), list);

    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeRange(1, 3);
    assertEquals(List.of("1", "4"), list);

    list = new DequeArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeRange(2, 4);
    assertEquals(List.of("1", "2"), list);

    // first < last (middle)
    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.removeRange(0, 2);
    assertEquals(List.of("3", "4"), list);

    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.removeRange(1, 3);
    assertEquals(List.of("1", "4"), list);

    list = new DequeArrayList<>();
    list.add("");
    list.add("");
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.removeFirst();
    list.removeFirst();
    list.removeRange(2, 4);
    assertEquals(List.of("1", "2"), list);

    // first < last (end)
    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.removeRange(0, 2);
    assertEquals(List.of("3", "4"), list);

    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.removeRange(1, 3);
    assertEquals(List.of("1", "4"), list);

    list = new DequeArrayList<>();
    list.addFirst("");
    list.addFirst("4");
    list.addFirst("3");
    list.addFirst("2");
    list.addFirst("1");
    list.removeLast();
    list.removeRange(2, 4);
    assertEquals(List.of("1", "2"), list);

    // last < first
    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.removeRange(0, 2);
    assertEquals(List.of("3", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.removeRange(1, 3);
    assertEquals(List.of("1", "4"), list);

    list = new DequeArrayList<>();
    list.add("3");
    list.add("4");
    list.addFirst("2");
    list.addFirst("1");
    list.removeRange(2, 4);
    assertEquals(List.of("1", "2"), list);

    // corner cases
    list = new DequeArrayList<>(1);
    list.add("1");
    list.removeRange(0, 1);
    assertEquals(List.of(), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.add("2");
    list.removeRange(0, 1);
    assertEquals(List.of("2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("2");
    list.removeRange(0, 1);
    assertEquals(List.of(), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("1");
    list.add("2");
    list.add("3");
    list.removeRange(1, 2);
    assertEquals(List.of("1", "3"), list);
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  public void retainAll() {
    var l = new DequeArrayList<String>();
    assertThrows(NullPointerException.class, () -> l.retainAll(null));
    assertTrue(l.isEmpty());

    var list = new DequeArrayList<String>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    assertFalse(list.retainAll(List.of("1", "2", "3", "4")));
    assertEquals(List.of("1", "2", "3", "4"), list);
    assertTrue(list.retainAll(List.of("2", "4")));
    assertEquals(List.of("2", "4"), list);

    list = new DequeArrayList<>();
    list.addFirst("2");
    list.addFirst("1");
    list.add("3");
    list.add("4");
    assertFalse(list.retainAll(List.of("1", "2", "3", "4")));
    assertEquals(List.of("1", "2", "3", "4"), list);
    assertTrue(list.retainAll(List.of("2", "4")));
    assertEquals(List.of("2", "4"), list);

    assertFalse(new DequeArrayList<>().retainAll(List.of("2", "4")));
    assertFalse(new DequeArrayList<>(0).retainAll(List.of("2", "4")));

    list = new DequeArrayList<>();
    list.add("1");
    list.add("1");
    list.add("4");
    list.add("3");
    list.add("4");
    list.add("1");
    assertFalse(list.retainAll(List.of("1", "3", "4")));
    assertTrue(list.retainAll(List.of("4")));
    assertEquals(List.of("4", "4"), list);
  }

  @Test
  public void set() {
    var list = new DequeArrayList<String>();
    assertTrue(list.isEmpty());
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.add("5");
    list.add("6");
    list.add("7");
    assertEquals("3", list.set(2, "30"));
    assertEquals("5", list.set(4, "50"));
    assertEquals("30", list.get(2));
    assertEquals("50", list.get(4));
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
    }
    list.add("8");
    list.add("9");
    list.add("10");
    list.add("11");
    list.add("12");
    assertEquals("8", list.set(2, "80"));
    assertEquals("10", list.set(4, "100"));
    assertEquals("80", list.get(2));
    assertEquals("100", list.get(4));
    for (int i = 0; i < 5; i++) {
      list.removeFirst();
    }
    list.add("13");
    list.add("14");
    assertEquals("12", list.set(1, null));
    assertNull(list.set(1, "12"));
    list.set(1, "120");
    assertEquals("120", list.set(1, null));
    var l = list;
    assertThrows(IndexOutOfBoundsException.class, () -> l.set(4, "4"));

    // corner cases
    list = new DequeArrayList<>(1);
    list.add("1");
    list.set(0, "0");
    assertEquals(List.of("0"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.add("2");
    list.set(0, "0");
    assertEquals(List.of("0", "2"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("2");
    list.set(0, "0");
    assertEquals(List.of("0"), list);
    list = new DequeArrayList<>(2);
    list.add("1");
    list.removeFirst();
    list.add("1");
    list.add("2");
    list.add("3");
    list.set(1, "0");
    assertEquals(List.of("1", "0", "3"), list);
  }

  @Test
  public void toArray() {
    var list = new DequeArrayList<String>();
    assertArrayEquals(new Object[0], list.toArray());
    assertArrayEquals(new String[0], list.toArray(new String[0]));
    assertArrayEquals(new String[]{null}, list.toArray(new String[1]));
    list.add(null);
    list.add("1");
    list.add("2");
    list.add(null);
    list.add("3");
    list.add("4");
    list.add(null);
    assertArrayEquals(new Object[]{null, "1", "2", null, "3", "4", null}, list.toArray());
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null},
        list.toArray(new String[0]));
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null},
        list.toArray(new String[1]));
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null},
        list.toArray(new String[7]));
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null, null, null, null},
        list.toArray(new String[10]));
    var array = new String[10];
    Arrays.fill(array, "9");
    list.toArray(array);
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null, null, "9", "9"}, array);
    list.clear();
    assertArrayEquals(new Object[0], list.toArray());
    assertArrayEquals(new String[0], list.toArray(new String[0]));
    assertArrayEquals(new String[]{null}, list.toArray(new String[1]));
    list.add("4");
    list.add(null);
    list.addFirst("3");
    list.addFirst(null);
    list.addFirst("2");
    list.addFirst("1");
    list.addFirst(null);
    assertArrayEquals(new Object[]{null, "1", "2", null, "3", "4", null}, list.toArray());
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null},
        list.toArray(new String[0]));
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null},
        list.toArray(new String[1]));
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null},
        list.toArray(new String[7]));
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null, null, null, null},
        list.toArray(new String[10]));
    array = new String[10];
    Arrays.fill(array, "9");
    list.toArray(array);
    assertArrayEquals(new String[]{null, "1", "2", null, "3", "4", null, null, "9", "9"}, array);
  }
}
