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
package sparx.concurrent.execution;

import org.jetbrains.annotations.NotNull;
import sparx.concurrent.StreamingFuture;
import sparx.tuple.Quadruple;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/concurrent/ExecutionFuture.mustache
///////////////////////////////////////////////

public interface QuaternaryFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, R> extends ExecutionFuture<P, R> {

  @Override
  @NotNull Quadruple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>> parameters();
}
