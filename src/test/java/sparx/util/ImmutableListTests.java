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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class ImmutableListTests {

  @Test
  @SuppressWarnings({"ConstantValue", "RedundantOperationOnEmptyContainer"})
  public void empty() {
    var list = ImmutableList.<String>of();
    assertSame(ImmutableList.of(), list);
    assertSame(ImmutableList.of(new Object[0]), list);
    assertTrue(list.isEmpty());
    assertEquals(0, list.size());
    assertEquals(0, list.toArray().length);
    var array = new Object[]{"test"};
    list.toArray(array);
    assertNull(array[0]);
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    assertEquals(-1, list.indexOf("test"));
    assertEquals(-1, list.lastIndexOf("test"));
    list.clear();
    assertFalse(list.contains("test"));
    assertFalse(list.remove("test"));
    assertFalse(list.addAll(ImmutableList.of()));
    assertFalse(list.addAll(0, ImmutableList.of()));
    assertFalse(list.removeAll(ImmutableList.<String>of()));
    assertFalse(list.removeAll(ImmutableList.of("test")));
    assertFalse(list.retainAll(ImmutableList.<String>of()));
    assertFalse(list.retainAll(ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class, () -> list.add("test"));
    assertThrows(UnsupportedOperationException.class, () -> list.add(0, "test"));
    assertThrows(UnsupportedOperationException.class, () -> list.addAll(ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class,
        () -> list.addAll(0, ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class, () -> list.set(0, "test"));
    assertThrows(UnsupportedOperationException.class, () -> list.remove(0));
    assertEquals(ImmutableList.of(), list);
    assertEquals(Collections.emptyList(), list);
    assertEquals(1, list.hashCode());
    list.sort(String.CASE_INSENSITIVE_ORDER);
  }

  @Test
  public void emptyIterator() {
    var list = ImmutableList.of();
    var iterator = list.iterator();
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    assertThrows(UnsupportedOperationException.class, iterator::remove);
    var listIterator = list.listIterator();
    assertFalse(listIterator.hasNext());
    assertEquals(0, listIterator.nextIndex());
    assertThrows(NoSuchElementException.class, listIterator::next);
    assertThrows(UnsupportedOperationException.class, listIterator::remove);
    assertThrows(UnsupportedOperationException.class, () -> listIterator.add("test"));
    assertThrows(UnsupportedOperationException.class, () -> listIterator.set("test"));
    assertFalse(listIterator.hasPrevious());
    assertEquals(0, listIterator.previousIndex());
    assertThrows(NoSuchElementException.class, listIterator::previous);
    assertSame(iterator, listIterator);
  }

  @Test
  @SuppressWarnings("SuspiciousMethodCalls")
  public void singleton() {
    var list = ImmutableList.of("test");
    assertFalse(list.isEmpty());
    assertEquals(1, list.size());
    assertEquals("test", list.get(0));
    assertEquals(0, list.indexOf("test"));
    assertEquals(0, list.lastIndexOf("test"));
    assertArrayEquals(new String[]{"test"}, list.toArray(new String[0]));
    var array = new Object[]{"a", "b"};
    list.toArray(array);
    assertEquals("test", array[0]);
    assertNull(array[1]);
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
    assertEquals(-1, list.indexOf("a"));
    assertEquals(-1, list.lastIndexOf("b"));
    assertTrue(list.contains("test"));
    assertFalse(list.contains("a"));
    assertFalse(list.remove("a"));
    assertFalse(list.addAll(ImmutableList.of()));
    assertFalse(list.addAll(0, ImmutableList.of()));
    assertFalse(list.removeAll(ImmutableList.of()));
    assertFalse(list.removeAll(ImmutableList.of("a")));
    assertFalse(list.retainAll(ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class, list::clear);
    assertThrows(UnsupportedOperationException.class, () -> list.remove("test"));
    assertThrows(UnsupportedOperationException.class, () -> list.add("test"));
    assertThrows(UnsupportedOperationException.class, () -> list.add(0, "test"));
    assertThrows(UnsupportedOperationException.class, () -> list.addAll(ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class,
        () -> list.addAll(0, ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class,
        () -> list.removeAll(ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class, () -> list.retainAll(ImmutableList.of()));
    assertThrows(UnsupportedOperationException.class, () -> list.retainAll(ImmutableList.of("a")));
    assertThrows(UnsupportedOperationException.class, () -> list.set(0, "test"));
    assertThrows(UnsupportedOperationException.class, () -> list.remove(0));
    assertEquals(ImmutableList.of("test"), list);
    assertEquals(ImmutableList.of(new String[]{"test"}), list);
    assertEquals(Collections.singletonList("test"), list);
    assertThrows(UnsupportedOperationException.class,
        () -> list.sort(String.CASE_INSENSITIVE_ORDER));
  }

  @Test
  public void singletonIterator() {
    var list = ImmutableList.of("test");
    var iterator = list.iterator();
    assertTrue(iterator.hasNext());
    assertEquals("test", iterator.next());
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    assertThrows(UnsupportedOperationException.class, iterator::remove);
    var listIterator = list.listIterator();
    assertTrue(listIterator.hasNext());
    assertEquals(0, listIterator.nextIndex());
    assertEquals("test", listIterator.next());
    assertFalse(listIterator.hasNext());
    assertEquals(1, listIterator.nextIndex());
    assertThrows(NoSuchElementException.class, listIterator::next);
    assertThrows(UnsupportedOperationException.class, listIterator::remove);
    assertThrows(UnsupportedOperationException.class, () -> listIterator.add("test"));
    assertThrows(UnsupportedOperationException.class, () -> listIterator.set("test"));
    assertTrue(listIterator.hasPrevious());
    assertEquals(0, listIterator.previousIndex());
    assertEquals("test", listIterator.previous());
    assertFalse(listIterator.hasPrevious());
    assertThrows(NoSuchElementException.class, listIterator::previous);
    assertNotSame(iterator, listIterator);
  }

  @Test
  @SuppressWarnings({"SuspiciousMethodCalls", "DataFlowIssue"})
  public void singletonNull() {
    var list = ImmutableList.of((String) null);
    assertFalse(list.isEmpty());
    assertEquals(1, list.size());
    assertNull(list.get(0));
    assertEquals(0, list.indexOf(null));
    assertEquals(0, list.lastIndexOf(null));
    assertArrayEquals(new String[]{null}, list.toArray(new String[0]));
    var array = new Object[]{"a", "b"};
    list.toArray(array);
    assertNull(array[0]);
    assertNull(array[1]);
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
    assertEquals(-1, list.indexOf("test"));
    assertEquals(-1, list.lastIndexOf("test"));
    assertTrue(list.contains(null));
    assertFalse(list.contains("test"));
    assertFalse(list.remove("test"));
    assertFalse(list.addAll(ImmutableList.of()));
    assertFalse(list.addAll(0, ImmutableList.of()));
    assertFalse(list.removeAll(ImmutableList.of()));
    assertFalse(list.removeAll(ImmutableList.of("test")));
    assertFalse(list.retainAll(ImmutableList.of((String) null)));
    assertThrows(UnsupportedOperationException.class, list::clear);
    assertThrows(UnsupportedOperationException.class, () -> list.remove(null));
    assertThrows(UnsupportedOperationException.class, () -> list.add("test"));
    assertThrows(UnsupportedOperationException.class, () -> list.add(0, "test"));
    assertThrows(UnsupportedOperationException.class, () -> list.addAll(ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class,
        () -> list.addAll(0, ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class,
        () -> list.removeAll(ImmutableList.of((String) null)));
    assertThrows(UnsupportedOperationException.class, () -> list.retainAll(ImmutableList.of()));
    assertThrows(UnsupportedOperationException.class,
        () -> list.retainAll(ImmutableList.of("test")));
    assertThrows(UnsupportedOperationException.class, () -> list.set(0, "test"));
    assertThrows(UnsupportedOperationException.class, () -> list.remove(0));
    assertEquals(ImmutableList.of((String) null), list);
    assertEquals(ImmutableList.of(new String[]{null}), list);
    assertEquals(Collections.singletonList(null), list);
    assertThrows(NullPointerException.class, () -> ImmutableList.of((String[]) null));
    assertThrows(UnsupportedOperationException.class,
        () -> list.sort(String.CASE_INSENSITIVE_ORDER));
  }

  @Test
  public void singletonNullIterator() {
    var list = ImmutableList.of((String) null);
    var iterator = list.iterator();
    assertTrue(iterator.hasNext());
    assertNull(iterator.next());
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    assertThrows(UnsupportedOperationException.class, iterator::remove);
    var listIterator = list.listIterator();
    assertTrue(listIterator.hasNext());
    assertEquals(0, listIterator.nextIndex());
    assertNull(listIterator.next());
    assertFalse(listIterator.hasNext());
    assertEquals(1, listIterator.nextIndex());
    assertThrows(NoSuchElementException.class, listIterator::next);
    assertThrows(UnsupportedOperationException.class, listIterator::remove);
    assertThrows(UnsupportedOperationException.class, () -> listIterator.add("test"));
    assertThrows(UnsupportedOperationException.class, () -> listIterator.set("test"));
    assertTrue(listIterator.hasPrevious());
    assertEquals(0, listIterator.previousIndex());
    assertNull(listIterator.previous());
    assertFalse(listIterator.hasPrevious());
    assertThrows(NoSuchElementException.class, listIterator::previous);
    assertNotSame(iterator, listIterator);
  }
}
