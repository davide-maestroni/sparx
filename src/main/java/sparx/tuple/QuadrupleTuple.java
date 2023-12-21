////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.tuple;

import java.util.Arrays;

class QuadrupleTuple<T, T1 extends T, T2 extends T, T3 extends T, T4 extends T> extends GenericTuple<T>
    implements Quadruple<T, T1, T2, T3, T4> {

  private final T1 first;
  private final T2 second;
  private final T3 third;
  private final T4 fourth;

  QuadrupleTuple(final T1 first, final T2 second, final T3 third, final T4 fourth) {
    super(Arrays.asList(first, second, third, fourth));
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
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
}