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
import sparx.util.function.Function;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/concurrent/GeneratingFuture.mustache
///////////////////////////////////////////////

public interface GeneratingFuture<V> extends StreamingFuture<V> {

  @NotNull <V1, F1 extends SignalFuture<V1>> GeneratingFuture<V1> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> GeneratingFuture<V2> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> GeneratingFuture<V3> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> GeneratingFuture<V4> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> GeneratingFuture<V5> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>> GeneratingFuture<V6> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>> GeneratingFuture<V7> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>> GeneratingFuture<V8> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>> GeneratingFuture<V9> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>> GeneratingFuture<V10> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>> GeneratingFuture<V11> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>> GeneratingFuture<V12> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction,
      @NotNull Function<? super F11, F12> twelfthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>> GeneratingFuture<V13> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction,
      @NotNull Function<? super F11, F12> twelfthFunction,
      @NotNull Function<? super F12, F13> thirteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>> GeneratingFuture<V14> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction,
      @NotNull Function<? super F11, F12> twelfthFunction,
      @NotNull Function<? super F12, F13> thirteenthFunction,
      @NotNull Function<? super F13, F14> fourteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>> GeneratingFuture<V15> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction,
      @NotNull Function<? super F11, F12> twelfthFunction,
      @NotNull Function<? super F12, F13> thirteenthFunction,
      @NotNull Function<? super F13, F14> fourteenthFunction,
      @NotNull Function<? super F14, F15> fifteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>> GeneratingFuture<V16> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction,
      @NotNull Function<? super F11, F12> twelfthFunction,
      @NotNull Function<? super F12, F13> thirteenthFunction,
      @NotNull Function<? super F13, F14> fourteenthFunction,
      @NotNull Function<? super F14, F15> fifteenthFunction,
      @NotNull Function<? super F15, F16> sixteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>> GeneratingFuture<V17> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction,
      @NotNull Function<? super F11, F12> twelfthFunction,
      @NotNull Function<? super F12, F13> thirteenthFunction,
      @NotNull Function<? super F13, F14> fourteenthFunction,
      @NotNull Function<? super F14, F15> fifteenthFunction,
      @NotNull Function<? super F15, F16> sixteenthFunction,
      @NotNull Function<? super F16, F17> seventeenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>> GeneratingFuture<V18> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction,
      @NotNull Function<? super F11, F12> twelfthFunction,
      @NotNull Function<? super F12, F13> thirteenthFunction,
      @NotNull Function<? super F13, F14> fourteenthFunction,
      @NotNull Function<? super F14, F15> fifteenthFunction,
      @NotNull Function<? super F15, F16> sixteenthFunction,
      @NotNull Function<? super F16, F17> seventeenthFunction,
      @NotNull Function<? super F17, F18> eighteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>> GeneratingFuture<V19> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction,
      @NotNull Function<? super F11, F12> twelfthFunction,
      @NotNull Function<? super F12, F13> thirteenthFunction,
      @NotNull Function<? super F13, F14> fourteenthFunction,
      @NotNull Function<? super F14, F15> fifteenthFunction,
      @NotNull Function<? super F15, F16> sixteenthFunction,
      @NotNull Function<? super F16, F17> seventeenthFunction,
      @NotNull Function<? super F17, F18> eighteenthFunction,
      @NotNull Function<? super F18, F19> nineteenthFunction);

  @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>, V20, F20 extends SignalFuture<V20>> GeneratingFuture<V20> thenPulling(
      @NotNull Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull Function<? super F1, F2> secondFunction,
      @NotNull Function<? super F2, F3> thirdFunction,
      @NotNull Function<? super F3, F4> fourthFunction,
      @NotNull Function<? super F4, F5> fifthFunction,
      @NotNull Function<? super F5, F6> sixthFunction,
      @NotNull Function<? super F6, F7> seventhFunction,
      @NotNull Function<? super F7, F8> eighthFunction,
      @NotNull Function<? super F8, F9> ninthFunction,
      @NotNull Function<? super F9, F10> tenthFunction,
      @NotNull Function<? super F10, F11> eleventhFunction,
      @NotNull Function<? super F11, F12> twelfthFunction,
      @NotNull Function<? super F12, F13> thirteenthFunction,
      @NotNull Function<? super F13, F14> fourteenthFunction,
      @NotNull Function<? super F14, F15> fifteenthFunction,
      @NotNull Function<? super F15, F16> sixteenthFunction,
      @NotNull Function<? super F16, F17> seventeenthFunction,
      @NotNull Function<? super F17, F18> eighteenthFunction,
      @NotNull Function<? super F18, F19> nineteenthFunction,
      @NotNull Function<? super F19, F20> twentiethFunction);

  @NotNull Subscription subscribe();
}
