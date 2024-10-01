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

import sparx.internal.lazy.CollectionMaterializer;
import sparx.util.annotation.NotNegative;

public interface ListMaterializer<E> extends CollectionMaterializer<E> {

  boolean canMaterializeElement(@NotNegative int index);

  E materializeElement(@NotNegative int index);
}
