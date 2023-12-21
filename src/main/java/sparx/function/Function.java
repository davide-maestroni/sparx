package sparx.function;

public interface Function<I, O> {

  O apply(I input) throws Exception;
}
