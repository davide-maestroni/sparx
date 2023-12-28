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
////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.tuple;

class TripleTuple<T, T1 extends T, T2 extends T, T3 extends T> extends GenericTuple<T>
    implements Triple<T, T1, T2, T3> {

  private final T1 first;
  private final T2 second;
  private final T3 third;

  @SuppressWarnings("unchecked")
  TripleTuple(final T1 first, final T2 second, final T3 third) {
    super(first, second, third);
    this.first = first;
    this.second = second;
    this.third = third;
  }

  @Override
  public T1 getFirst() {
    return first;
  }

  @Override
  public T2 getSecond() {
    return second;
  }

  @Override
  public T3 getThird() {
   return third;
  }
}