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
package sparx.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class GeneratorFutureTests {

  @Test
  public void test() {
    var future = GeneratorFuture.of(0, (count, output) -> {
      if (count < 10) {
        output.set(count);
        ++count;
      } else {
        output.close();
      }
      return count;
    });
    var iterator = future.iterator();
    assertEquals(0, iterator.next());
    assertEquals(1, iterator.next());
    assertEquals(2, iterator.next());
    assertEquals(2, future.getCurrent());
    assertFalse(future.isDone());
  }
}
