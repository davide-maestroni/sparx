package sparx.function;

public interface BiFunction<I1, I2, O> {

  O apply(I1 firstInput, I2 secondInput) throws Exception;
}
