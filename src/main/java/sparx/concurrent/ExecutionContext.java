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
package sparx.concurrent;

import org.jetbrains.annotations.NotNull;
import sparx.concurrent.execution.*;
import sparx.util.Nothing;
import sparx.util.function.*;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/concurrent/ExecutionContext.mustache
///////////////////////////////////////////////

public interface ExecutionContext {

  int minThroughput();

  int pendingCount();

  @NotNull <P> NullaryFuture<P, Nothing> submit(
      @NotNull Action action);

  @NotNull <P> UnaryFuture<P, Nothing> submit(
      @NotNull Consumer<? super StreamingFuture<P>> consumer);

  @NotNull <P, P1 extends P, P2 extends P> BinaryFuture<P, P1, P2, Nothing> submit(
      @NotNull BinaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P> TernaryFuture<P, P1, P2, P3, Nothing> submit(
      @NotNull TernaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P> QuaternaryFuture<P, P1, P2, P3, P4, Nothing> submit(
      @NotNull QuaternaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P> QuinaryFuture<P, P1, P2, P3, P4, P5, Nothing> submit(
      @NotNull QuinaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P> SenaryFuture<P, P1, P2, P3, P4, P5, P6, Nothing> submit(
      @NotNull SenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P> SeptenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, Nothing> submit(
      @NotNull SeptenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P> OctonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, Nothing> submit(
      @NotNull OctonaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P> NonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, Nothing> submit(
      @NotNull NonaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P> DenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, Nothing> submit(
      @NotNull DenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P> UndenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, Nothing> submit(
      @NotNull UndenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P> DuodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, Nothing> submit(
      @NotNull DuodenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P> TredenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, Nothing> submit(
      @NotNull TredenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P> QuattuordenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, Nothing> submit(
      @NotNull QuattuordenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P> QuindenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, Nothing> submit(
      @NotNull QuindenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P> SexdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, Nothing> submit(
      @NotNull SexdenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P> SeptendenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, Nothing> submit(
      @NotNull SeptendenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P> OctodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, Nothing> submit(
      @NotNull OctodenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P> NovemdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, Nothing> submit(
      @NotNull NovemdenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>> consumer);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, P20 extends P> VigenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, Nothing> submit(
      @NotNull VigenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>, ? super StreamingFuture<P20>> consumer);

  @NotNull <P, R> NullaryFuture<P, R> submit(
      @NotNull Supplier<? extends Signal<R>> supplier);

  @NotNull <P, R> UnaryFuture<P, R> submit(
      @NotNull Function<? super StreamingFuture<P>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, R> BinaryFuture<P, P1, P2, R> submit(
      @NotNull BinaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, R> TernaryFuture<P, P1, P2, P3, R> submit(
      @NotNull TernaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, R> QuaternaryFuture<P, P1, P2, P3, P4, R> submit(
      @NotNull QuaternaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, R> QuinaryFuture<P, P1, P2, P3, P4, P5, R> submit(
      @NotNull QuinaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, R> SenaryFuture<P, P1, P2, P3, P4, P5, P6, R> submit(
      @NotNull SenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, R> SeptenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, R> submit(
      @NotNull SeptenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, R> OctonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, R> submit(
      @NotNull OctonaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, R> NonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, R> submit(
      @NotNull NonaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, R> DenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> submit(
      @NotNull DenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, R> UndenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R> submit(
      @NotNull UndenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, R> DuodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R> submit(
      @NotNull DuodenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, R> TredenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R> submit(
      @NotNull TredenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, R> QuattuordenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R> submit(
      @NotNull QuattuordenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, R> QuindenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R> submit(
      @NotNull QuindenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, R> SexdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R> submit(
      @NotNull SexdenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, R> SeptendenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, R> submit(
      @NotNull SeptendenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, R> OctodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R> submit(
      @NotNull OctodenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, R> NovemdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R> submit(
      @NotNull NovemdenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>, ? extends Signal<R>> function);

  @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, P20 extends P, R> VigenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R> submit(
      @NotNull VigenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>, ? super StreamingFuture<P20>, ? extends Signal<R>> function);
}
