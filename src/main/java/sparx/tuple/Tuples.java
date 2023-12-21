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

import org.jetbrains.annotations.NotNull;
import sparx.util.Nothing;

public class Tuples {

  private Tuples() {
  }

  public static @NotNull Empty<Nothing> tupleOf() {
    return EmptyTuple.instance();
  }

  public static @NotNull <T> Single<T> tupleOf(final T first) {
    return new SingleTuple<T>(first);
  }

  public static @NotNull <T, T1 extends T, T2 extends T> Couple<T, T1, T2> tupleOf(
      final T1 first, final T2 second) {
    return new CoupleTuple<T, T1, T2>(first, second);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T> Triple<T, T1, T2, T3> tupleOf(
      final T1 first, final T2 second, final T3 third) {
    return new TripleTuple<T, T1, T2, T3>(first, second, third);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T> Quadruple<T, T1, T2, T3, T4> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth) {
    return new QuadrupleTuple<T, T1, T2, T3, T4>(first, second, third, fourth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T> Quintuple<T, T1, T2, T3, T4, T5> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth) {
    return new QuintupleTuple<T, T1, T2, T3, T4, T5>(first, second, third, fourth, fifth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T> Sextuple<T, T1, T2, T3, T4, T5, T6> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth) {
    return new SextupleTuple<T, T1, T2, T3, T4, T5, T6>(first, second, third, fourth, fifth, sixth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T> Septuple<T, T1, T2, T3, T4, T5, T6, T7> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh) {
    return new SeptupleTuple<T, T1, T2, T3, T4, T5, T6, T7>(first, second, third, fourth, fifth, sixth, seventh);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T> Octuple<T, T1, T2, T3, T4, T5, T6, T7, T8> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth) {
    return new OctupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8>(first, second, third, fourth, fifth, sixth, seventh, eighth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T> Nonuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth) {
    return new NonupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T> Decuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth) {
    return new DecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T> Undecuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh) {
    return new UndecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T> Duodecuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh, final T12 twelfth) {
    return new DuodecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T, T13 extends T> Tredecuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh, final T12 twelfth, final T13 thirteenth) {
    return new TredecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T, T13 extends T, T14 extends T> Quattuordecuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh, final T12 twelfth, final T13 thirteenth, final T14 fourteenth) {
    return new QuattuordecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T, T13 extends T, T14 extends T, T15 extends T> Quindecuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh, final T12 twelfth, final T13 thirteenth, final T14 fourteenth, final T15 fifteenth) {
    return new QuindecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T, T13 extends T, T14 extends T, T15 extends T, T16 extends T> Sexdecuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh, final T12 twelfth, final T13 thirteenth, final T14 fourteenth, final T15 fifteenth, final T16 sixteenth) {
    return new SexdecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T, T13 extends T, T14 extends T, T15 extends T, T16 extends T, T17 extends T> Septendecuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh, final T12 twelfth, final T13 thirteenth, final T14 fourteenth, final T15 fifteenth, final T16 sixteenth, final T17 seventeenth) {
    return new SeptendecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T, T13 extends T, T14 extends T, T15 extends T, T16 extends T, T17 extends T, T18 extends T> Octodecuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh, final T12 twelfth, final T13 thirteenth, final T14 fourteenth, final T15 fifteenth, final T16 sixteenth, final T17 seventeenth, final T18 eighteenth) {
    return new OctodecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T, T13 extends T, T14 extends T, T15 extends T, T16 extends T, T17 extends T, T18 extends T, T19 extends T> Novemdecuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh, final T12 twelfth, final T13 thirteenth, final T14 fourteenth, final T15 fifteenth, final T16 sixteenth, final T17 seventeenth, final T18 eighteenth, final T19 nineteenth) {
    return new NovemdecupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth);
  }

  public static @NotNull <T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T, T10 extends T, T11 extends T, T12 extends T, T13 extends T, T14 extends T, T15 extends T, T16 extends T, T17 extends T, T18 extends T, T19 extends T, T20 extends T> Viguple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> tupleOf(
      final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth, final T10 tenth, final T11 eleventh, final T12 twelfth, final T13 thirteenth, final T14 fourteenth, final T15 fifteenth, final T16 sixteenth, final T17 seventeenth, final T18 eighteenth, final T19 nineteenth, final T20 twentieth) {
    return new VigupleTuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth, twentieth);
  }
}
