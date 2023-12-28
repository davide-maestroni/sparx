package sparx.tuple;

class SingleTuple<T> extends GenericTuple<T> implements Single<T> {

  private final T first;

  @SuppressWarnings("unchecked")
  SingleTuple(final T first) {
    super(first);
    this.first = first;
  }

  @Override
  public T getFirst() {
    return first;
  }
}
