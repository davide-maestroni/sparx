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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import sparx.Sparx.lazy.List;
import sparx.concurrent.ExecutorContext;

public class FutureListTests {

  @Test
  public void all() {
    var executor = Executors.newSingleThreadExecutor();
    var context = ExecutorContext.of(executor);
    try {
      assertFalse(List.of().toFuture(context).all(Objects::nonNull).isEmpty());
      assertTrue(List.of().toFuture(context).all(Objects::nonNull).notEmpty());
      assertEquals(1, List.of().toFuture(context).all(Objects::nonNull).size());
      assertTrue(List.of().toFuture(context).all(Objects::nonNull).first());
      assertFalse(List.of(1, 2, 3).toFuture(context).all(i -> i < 3).first());
      {
        var itr = List.of(1, 2, 3).all(i -> i < 3).iterator();
        assertTrue(itr.hasNext());
        assertFalse(itr.next());
        assertThrows(UnsupportedOperationException.class, itr::remove);
        assertFalse(itr.hasNext());
        assertThrows(NoSuchElementException.class, itr::next);
      }
      assertTrue(List.of(1, 2, 3).toFuture(context).all(i -> i > 0).first());
      {
        var itr = List.of(1, 2, 3).all(i -> i > 0).iterator();
        assertTrue(itr.hasNext());
        assertTrue(itr.next());
        assertThrows(UnsupportedOperationException.class, itr::remove);
        assertFalse(itr.hasNext());
        assertThrows(NoSuchElementException.class, itr::next);
      }
      var l = List.of(1, null, 3).toFuture(context).all(i -> i > 0);
      assertThrows(NullPointerException.class, l::first);
      {
//        var itr = l.iterator();
//        assertTrue(itr.hasNext());
//        assertThrows(NullPointerException.class, itr::next);
      }
    } finally {
      executor.shutdownNow();
    }
  }
}
