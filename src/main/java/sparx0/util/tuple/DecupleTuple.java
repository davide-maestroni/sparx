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
package sparx0.util.tuple;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/tuple/TupleClass.mustache
///////////////////////////////////////////////

class DecupleTuple<T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T> extends GenericTuple<T>
    implements Decuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> {

  private final T1 first;
  private final T2 second;
  private final T3 third;
  private final T4 fourth;
  private final T5 fifth;
  private final T6 sixth;
  private final T7 seventh;
  private final T8 eighth;
  private final T9 ninth;
  private final T10 tenth;

  @SuppressWarnings("unchecked")
  DecupleTuple(final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth) {
    super(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
    this.fifth = fifth;
    this.sixth = sixth;
    this.seventh = seventh;
    this.eighth = eighth;
    this.ninth = ninth;
    this.tenth = tenth;
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

  @Override
  public T4 getFourth() {
    return fourth;
  }

  @Override
  public T5 getFifth() {
    return fifth;
  }

  @Override
  public T6 getSixth() {
    return sixth;
  }

  @Override
  public T7 getSeventh() {
    return seventh;
  }

  @Override
  public T8 getEighth() {
    return eighth;
  }

  @Override
  public T9 getNinth() {
    return ninth;
  }

  @Override
  public T10 getTenth() {
    return tenth;
  }
}