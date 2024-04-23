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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
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
}
