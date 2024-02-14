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
package sparx.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class DequeueListTests {

  @Test
  public void constructors() {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") var list = new DequeueList<String>();
    assertTrue(list.isEmpty());
    list = new DequeueList<>(10);
    assertTrue(list.isEmpty());
    assertThrows(IllegalArgumentException.class, () -> new DequeueList<String>(0));
    assertThrows(IllegalArgumentException.class, () -> new DequeueList<String>(-1));
  }

  @Test
  @SuppressWarnings("ConstantValue")
  public void add() {
    var list = new DequeueList<String>();
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
  }

  @Test
  public void addFirst() {
    var list = new DequeueList<String>();
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
  }

  @Test
  @SuppressWarnings("ConstantValue")
  public void addIndex() {
    var list = new DequeueList<String>();
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
    assertEquals(Arrays.asList("6", "13", "7", "8", "9", "10", "14", "11", "12"), list);
    list.clear();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add(1, "4");
    assertEquals(Arrays.asList("1", "4", "2", "3"), list);
    list.clear();
    list.add("1");
    list.addFirst("2");
    list.addFirst("3");
    list.addFirst("4");
    list.addFirst("5");
    list.add(3, "6");
    assertEquals(Arrays.asList("5", "4", "3", "6", "2", "1"), list);
  }

  @Test
  @SuppressWarnings("ConstantValue")
  public void clear() {
    var list = new DequeueList<String>();
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
    var list = new DequeueList<String>();
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
    var list = new DequeueList<String>();
    assertTrue(list.isEmpty());
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
  }

  @Test
  public void lastIndexOf() {
    var list = new DequeueList<String>();
    assertTrue(list.isEmpty());
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
  }

  // TODO: iterators
  // TODO: remove

  @Test
  public void set() {
    var list = new DequeueList<String>();
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
    assertThrows(IndexOutOfBoundsException.class, () -> list.set(4, "4"));
  }
}
