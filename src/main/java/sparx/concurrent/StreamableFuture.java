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
import sparx.function.Function;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

public interface StreamableFuture<V, F extends SignalFuture<V>> extends SignalFuture<V> {

  @NotNull <V1, F1 extends SignalFuture<V1>> F1 then(
    @NotNull Function<? super F, F1> firstFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> F2 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> F3 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> F4 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> F5 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>> F6 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>> F7 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>> F8 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>> F9 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>> F10 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>> F11 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>> F12 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>> F13 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>> F14 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>> F15 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>> F16 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>> F17 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction,
      @NotNull final Function<? super F16, F17> seventeenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>> F18 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction,
      @NotNull final Function<? super F16, F17> seventeenthFunction,
      @NotNull final Function<? super F17, F18> eighteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>> F19 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction,
      @NotNull final Function<? super F16, F17> seventeenthFunction,
      @NotNull final Function<? super F17, F18> eighteenthFunction,
      @NotNull final Function<? super F18, F19> nineteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>, V20, F20 extends SignalFuture<V20>> F20 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction,
      @NotNull final Function<? super F16, F17> seventeenthFunction,
      @NotNull final Function<? super F17, F18> eighteenthFunction,
      @NotNull final Function<? super F18, F19> nineteenthFunction,
      @NotNull final Function<? super F19, F20> twentiethFunction);

    @NotNull <V1, F1 extends SignalFuture<V1>> F1 thenSequentially(
      @NotNull Function<? super F, F1> firstFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> F2 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> F3 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> F4 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> F5 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>> F6 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>> F7 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>> F8 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>> F9 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>> F10 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>> F11 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>> F12 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>> F13 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>> F14 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>> F15 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>> F16 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>> F17 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction,
      @NotNull final Function<? super F16, F17> seventeenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>> F18 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction,
      @NotNull final Function<? super F16, F17> seventeenthFunction,
      @NotNull final Function<? super F17, F18> eighteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>> F19 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction,
      @NotNull final Function<? super F16, F17> seventeenthFunction,
      @NotNull final Function<? super F17, F18> eighteenthFunction,
      @NotNull final Function<? super F18, F19> nineteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>, V20, F20 extends SignalFuture<V20>> F20 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction,
      @NotNull final Function<? super F11, F12> twelfthFunction,
      @NotNull final Function<? super F12, F13> thirteenthFunction,
      @NotNull final Function<? super F13, F14> fourteenthFunction,
      @NotNull final Function<? super F14, F15> fifteenthFunction,
      @NotNull final Function<? super F15, F16> sixteenthFunction,
      @NotNull final Function<? super F16, F17> seventeenthFunction,
      @NotNull final Function<? super F17, F18> eighteenthFunction,
      @NotNull final Function<? super F18, F19> nineteenthFunction,
      @NotNull final Function<? super F19, F20> twentiethFunction);
}
