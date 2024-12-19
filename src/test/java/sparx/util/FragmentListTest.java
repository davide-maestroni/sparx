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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sparx.lazy.List;

public class FragmentListTest {

  @Test
  public void test() {
    var list = new FragmentList<Integer>();
    list.add(1);
    list.add(2);
    list.add(3);
    assertEquals(List.of(1, 2, 3), list);
    list.add(0, 0);
    assertEquals(List.of(0, 1, 2, 3), list);
    list.add(2, null);
    assertEquals(List.of(0, 1, null, 2, 3), list);
    list.clear();
    list.add(1);
    list.add(2);
    list.add(3);
    list.add(2, null);
    list.add(4);
    assertEquals(List.of(1, 2, null, 3, 4), list);
    list.clear();
    list.add(1);
    list.add(2);
    list.add(3);
    list.add(1, null);
    list.add(0, 0);
    assertEquals(List.of(0, 1, null, 2, 3), list);
  }
}
