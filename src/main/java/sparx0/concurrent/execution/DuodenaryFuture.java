/*
 * Copyright 2024 Davide Maestroni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sparx0.concurrent.execution;

import org.jetbrains.annotations.NotNull;
import sparx0.concurrent.StreamingFuture;
import sparx0.util.tuple.Duodecuple;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/concurrent/ExecutionFuture.mustache
///////////////////////////////////////////////

public interface DuodenaryFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, R> extends ExecutionFuture<P, R> {

  @Override
  @NotNull Duodecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>> parameters();
}
