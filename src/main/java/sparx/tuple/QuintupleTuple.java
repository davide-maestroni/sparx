////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.tuple;

import java.util.Arrays;

class QuintupleTuple<T, T1 extends T, T2 extends T, T3 extends T, T4 extends T, T5 extends T> extends GenericTuple<T>
    implements Quintuple<T, T1, T2, T3, T4, T5> {

  private final T1 first;
  private final T2 second;
  private final T3 third;
  private final T4 fourth;
  private final T5 fifth;

  QuintupleTuple(final T1 first, final T2 second, final T3 third, final T4 fourth, final T5 fifth) {
    super(Arrays.asList(first, second, third, fourth, fifth));
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
    this.fifth = fifth;
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
}