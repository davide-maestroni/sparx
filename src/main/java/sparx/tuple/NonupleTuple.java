////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.tuple;

import java.util.Arrays;

class NonupleTuple<T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T, T6 extends T, T7 extends T, T8 extends T, T9 extends T> extends GenericTuple<T>
    implements Nonuple<T, T1, T2, T3, T4, T5, T6, T7, T8, T9> {

  private final T1 first;
  private final T2 second;
  private final T3 third;
  private final T4 fourth;
  private final T5 fifth;
  private final T6 sixth;
  private final T7 seventh;
  private final T8 eighth;
  private final T9 ninth;

  NonupleTuple(final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth, final T6 sixth, final T7 seventh, final T8 eighth, final T9 ninth) {
    super(Arrays.asList(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth));
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
    this.fifth = fifth;
    this.sixth = sixth;
    this.seventh = seventh;
    this.eighth = eighth;
    this.ninth = ninth;
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
}