package sparx.tuple;

import java.util.Collections;

class SingleTuple<T> extends GenericTuple<T> implements Single<T> {

  private final T first;

  SingleTuple(final T first) {
    super(Collections.singletonList(first));
    this.first = first;
  }

  @Override
  public T getFirst() {
    return first;
  }
}
