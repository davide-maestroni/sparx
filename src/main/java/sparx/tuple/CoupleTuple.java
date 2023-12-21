////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.tuple;

import java.util.Arrays;

class CoupleTuple<T, T1 extends T, T2 extends T> extends GenericTuple<T>
    implements Couple<T, T1, T2> {

  private final T1 first;
  private final T2 second;

  CoupleTuple(final T1 first, final T2 second) {
    super(Arrays.asList(first, second));
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