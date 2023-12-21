package sparx.concurrent;

import sparx.tuple.Tuple;
import sparx.util.Nothing;

public interface TupleFuture<V, F extends StreamableFuture<Nothing, F>> extends
    StreamableFuture<Nothing, F>, Tuple<StreamingFuture<? extends V>> {

}
