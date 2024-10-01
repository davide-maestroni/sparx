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
package sparx.internal.lazy.list;

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.util.annotation.NotNegative;

public class CharSequenceToListMaterializer implements ListMaterializer<Character> {

  private final CharSequence elements;

  public CharSequenceToListMaterializer(@NotNull final CharSequence elements) {
    this.elements = elements;
  }

  @Override
  public boolean canMaterializeElement(@NotNegative final int index) {
    return index < elements.length();
  }

  @Override
  public int knownSize() {
    return elements.length();
  }

  @Override
  @SuppressWarnings("EqualsBetweenInconvertibleTypes")
  public boolean materializeContains(final Object element) {
    final CharSequence elements = this.elements;
    if (element != null) {
      final int length = elements.length();
      for (int i = 0; i < length; ++i) {
        if (element.equals(elements.charAt(i))) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Character materializeElement(@NotNegative final int index) {
    return elements.charAt(index);
  }

  @Override
  public int materializeElements() {
    return elements.length();
  }

  @Override
  public boolean materializeEmpty() {
    return elements.length() == 0;
  }

  @Override
  public @NotNull Iterator<Character> materializeIterator() {
    return new ListMaterializerIterator<Character>(this);
  }

  @Override
  public int materializeSize() {
    return elements.length();
  }
}
