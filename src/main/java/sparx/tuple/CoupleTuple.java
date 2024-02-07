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
package sparx.tuple;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/tuple/TupleClass.mustache
///////////////////////////////////////////////

class CoupleTuple<T, T1 extends T, T2 extends T> extends GenericTuple<T>
    implements Couple<T, T1, T2> {

  private final T1 first;
  private final T2 second;

  @SuppressWarnings("unchecked")
  CoupleTuple(final T1 first, final T2 second) {
    super(first, second);
    this.first = first;
    this.second = second;
  }

  @Override
  public T1 getFirst() {
    return first;
  }

  @Override
  public T2 getSecond() {
    return second;
  }
}