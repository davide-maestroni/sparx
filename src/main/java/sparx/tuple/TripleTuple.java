////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.tuple;

import java.util.Arrays;

class TripleTuple<T, T1 extends T, T2 extends T, T3 extends T> extends GenericTuple<T>
    implements Triple<T, T1, T2, T3> {

  private final T1 first;
  private final T2 second;
  private final T3 third;

  TripleTuple(final T1 first, final T2 second, final T3 third) {
    super(Arrays.asList(first, second, third));
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