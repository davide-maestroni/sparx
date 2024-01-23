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

import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import sparx.function.Function;
import sparx.function.Supplier;
import sparx.util.Requires;
import sparx.util.UncheckedException;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

abstract class StreamGroupGeneratorFuture<V> extends ReadOnlyFuture<V> {

  StreamGroupGeneratorFuture(@NotNull final StreamingFuture<V> future) {
    super(future);
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>> GeneratorFuture<V1> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction) {
    final StreamGroup<V1> group = new StreamGroup<V1>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F1 output = input.thenImmediately(firstFunction);
      final GeneratorStreamReceiver<V1> receiver = new GeneratorStreamReceiver<V1>(input, output);
      final GeneratorFuture<V1> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> GeneratorFuture<V2> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction) {
    final StreamGroup<V2> group = new StreamGroup<V2>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F2 output = input.thenImmediately(
        firstFunction,
        secondFunction
      );
      final GeneratorStreamReceiver<V2> receiver = new GeneratorStreamReceiver<V2>(input, output);
      final GeneratorFuture<V2> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> GeneratorFuture<V3> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction) {
    final StreamGroup<V3> group = new StreamGroup<V3>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F3 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction
      );
      final GeneratorStreamReceiver<V3> receiver = new GeneratorStreamReceiver<V3>(input, output);
      final GeneratorFuture<V3> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> GeneratorFuture<V4> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction) {
    final StreamGroup<V4> group = new StreamGroup<V4>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F4 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction
      );
      final GeneratorStreamReceiver<V4> receiver = new GeneratorStreamReceiver<V4>(input, output);
      final GeneratorFuture<V4> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> GeneratorFuture<V5> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction) {
    final StreamGroup<V5> group = new StreamGroup<V5>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F5 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction
      );
      final GeneratorStreamReceiver<V5> receiver = new GeneratorStreamReceiver<V5>(input, output);
      final GeneratorFuture<V5> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>> GeneratorFuture<V6> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction) {
    final StreamGroup<V6> group = new StreamGroup<V6>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F6 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction
      );
      final GeneratorStreamReceiver<V6> receiver = new GeneratorStreamReceiver<V6>(input, output);
      final GeneratorFuture<V6> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>> GeneratorFuture<V7> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction) {
    final StreamGroup<V7> group = new StreamGroup<V7>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F7 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction
      );
      final GeneratorStreamReceiver<V7> receiver = new GeneratorStreamReceiver<V7>(input, output);
      final GeneratorFuture<V7> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>> GeneratorFuture<V8> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction) {
    final StreamGroup<V8> group = new StreamGroup<V8>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F8 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction
      );
      final GeneratorStreamReceiver<V8> receiver = new GeneratorStreamReceiver<V8>(input, output);
      final GeneratorFuture<V8> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>> GeneratorFuture<V9> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction) {
    final StreamGroup<V9> group = new StreamGroup<V9>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F9 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction
      );
      final GeneratorStreamReceiver<V9> receiver = new GeneratorStreamReceiver<V9>(input, output);
      final GeneratorFuture<V9> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>> GeneratorFuture<V10> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction) {
    final StreamGroup<V10> group = new StreamGroup<V10>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F10 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction
      );
      final GeneratorStreamReceiver<V10> receiver = new GeneratorStreamReceiver<V10>(input, output);
      final GeneratorFuture<V10> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>> GeneratorFuture<V11> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction,
      @NotNull final Function<? super F10, F11> eleventhFunction) {
    final StreamGroup<V11> group = new StreamGroup<V11>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F11 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction
      );
      final GeneratorStreamReceiver<V11> receiver = new GeneratorStreamReceiver<V11>(input, output);
      final GeneratorFuture<V11> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>> GeneratorFuture<V12> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      @NotNull final Function<? super F11, F12> twelfthFunction) {
    final StreamGroup<V12> group = new StreamGroup<V12>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F12 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction,
        twelfthFunction
      );
      final GeneratorStreamReceiver<V12> receiver = new GeneratorStreamReceiver<V12>(input, output);
      final GeneratorFuture<V12> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>> GeneratorFuture<V13> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      @NotNull final Function<? super F12, F13> thirteenthFunction) {
    final StreamGroup<V13> group = new StreamGroup<V13>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F13 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction,
        twelfthFunction,
        thirteenthFunction
      );
      final GeneratorStreamReceiver<V13> receiver = new GeneratorStreamReceiver<V13>(input, output);
      final GeneratorFuture<V13> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>> GeneratorFuture<V14> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      @NotNull final Function<? super F13, F14> fourteenthFunction) {
    final StreamGroup<V14> group = new StreamGroup<V14>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F14 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction,
        twelfthFunction,
        thirteenthFunction,
        fourteenthFunction
      );
      final GeneratorStreamReceiver<V14> receiver = new GeneratorStreamReceiver<V14>(input, output);
      final GeneratorFuture<V14> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>> GeneratorFuture<V15> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      @NotNull final Function<? super F14, F15> fifteenthFunction) {
    final StreamGroup<V15> group = new StreamGroup<V15>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F15 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction,
        twelfthFunction,
        thirteenthFunction,
        fourteenthFunction,
        fifteenthFunction
      );
      final GeneratorStreamReceiver<V15> receiver = new GeneratorStreamReceiver<V15>(input, output);
      final GeneratorFuture<V15> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>> GeneratorFuture<V16> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      @NotNull final Function<? super F15, F16> sixteenthFunction) {
    final StreamGroup<V16> group = new StreamGroup<V16>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F16 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction,
        twelfthFunction,
        thirteenthFunction,
        fourteenthFunction,
        fifteenthFunction,
        sixteenthFunction
      );
      final GeneratorStreamReceiver<V16> receiver = new GeneratorStreamReceiver<V16>(input, output);
      final GeneratorFuture<V16> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>> GeneratorFuture<V17> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      @NotNull final Function<? super F16, F17> seventeenthFunction) {
    final StreamGroup<V17> group = new StreamGroup<V17>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F17 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction,
        twelfthFunction,
        thirteenthFunction,
        fourteenthFunction,
        fifteenthFunction,
        sixteenthFunction,
        seventeenthFunction
      );
      final GeneratorStreamReceiver<V17> receiver = new GeneratorStreamReceiver<V17>(input, output);
      final GeneratorFuture<V17> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>> GeneratorFuture<V18> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      @NotNull final Function<? super F17, F18> eighteenthFunction) {
    final StreamGroup<V18> group = new StreamGroup<V18>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F18 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction,
        twelfthFunction,
        thirteenthFunction,
        fourteenthFunction,
        fifteenthFunction,
        sixteenthFunction,
        seventeenthFunction,
        eighteenthFunction
      );
      final GeneratorStreamReceiver<V18> receiver = new GeneratorStreamReceiver<V18>(input, output);
      final GeneratorFuture<V18> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>> GeneratorFuture<V19> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      @NotNull final Function<? super F18, F19> nineteenthFunction) {
    final StreamGroup<V19> group = new StreamGroup<V19>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F19 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction,
        twelfthFunction,
        thirteenthFunction,
        fourteenthFunction,
        fifteenthFunction,
        sixteenthFunction,
        seventeenthFunction,
        eighteenthFunction,
        nineteenthFunction
      );
      final GeneratorStreamReceiver<V19> receiver = new GeneratorStreamReceiver<V19>(input, output);
      final GeneratorFuture<V19> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>, V20, F20 extends SignalFuture<V20>> GeneratorFuture<V20> thenGenerate(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      @NotNull final Function<? super F19, F20> twentiethFunction) {
    final StreamGroup<V20> group = new StreamGroup<V20>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> input = new VarFuture<V>();
      final F20 output = input.thenImmediately(
        firstFunction,
        secondFunction,
        thirdFunction,
        fourthFunction,
        fifthFunction,
        sixthFunction,
        seventhFunction,
        eighthFunction,
        ninthFunction,
        tenthFunction,
        eleventhFunction,
        twelfthFunction,
        thirteenthFunction,
        fourteenthFunction,
        fifteenthFunction,
        sixteenthFunction,
        seventeenthFunction,
        eighteenthFunction,
        nineteenthFunction,
        twentiethFunction
      );
      final GeneratorStreamReceiver<V20> receiver = new GeneratorStreamReceiver<V20>(input, output);
      final GeneratorFuture<V20> generator = GeneratorFuture.of(receiver);
      generator.subscribe(group);
      return generator;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  abstract void pull(@NotNull Receiver<V> receiver);

  private class GeneratorStreamReceiver<U> implements Supplier<SignalFuture<U>>, Receiver<U> {

    private final VarFuture<V> input;
    private final SignalFuture<U> output;
    private final ArrayList<U> pendingValues = new ArrayList<U>();

    private Exception failureException;
    private VarFuture<U> future;
    private boolean isClosed;

    private GeneratorStreamReceiver(@NotNull final VarFuture<V> input,
        @NotNull final SignalFuture<U> output) {
      this.input = input;
      this.output = Requires.notNull(output, "output");
    }

    @Override
    public SignalFuture<U> get() {
      final ArrayList<U> pendingValues = this.pendingValues;
      synchronized (pendingValues) {
        final Exception failureException = this.failureException;
        if (!pendingValues.isEmpty()) {
          if (failureException != null) {
            final VarFuture<U> future = VarFuture.create();
            future.setBulk(pendingValues);
            future.fail(failureException);
            pendingValues.clear();
            return future;
          } else {
            final ValFuture<U> future = ValFuture.ofBulk(pendingValues);
            pendingValues.clear();
            return future;
          }
        } else if (isClosed) {
          if (failureException != null) {
            return ValFuture.ofFailure(failureException);
          }
          return null;
        } else {
          future = VarFuture.create();
        }
      }
      output.subscribe(this);
      pull(input);
      return future;
    }

    @Override
    public void close() {
      final ArrayList<U> pendingValues = this.pendingValues;
      synchronized (pendingValues) {
        isClosed = true;
        final VarFuture<U> future = this.future;
        if (future != null) {
          future.setBulk(pendingValues);
          future.close();
          pendingValues.clear();
        }
      }
    }

    @Override
    public boolean fail(@NotNull final Exception error) {
      final ArrayList<U> pendingValues = this.pendingValues;
      synchronized (pendingValues) {
        isClosed = true;
        failureException = error;
        final VarFuture<U> future = this.future;
        if (future != null) {
          future.setBulk(pendingValues);
          future.fail(error);
          pendingValues.clear();
        }
      }
      return true;
    }

    @Override
    public void set(final U value) {
      unsubscribe(input);
      final VarFuture<U> future = this.future;
      final ArrayList<U> pendingValues = this.pendingValues;
      synchronized (pendingValues) {
        if (pendingValues.isEmpty()) {
          if (future != null) {
            future.set(value);
            future.close();
          } else {
            pendingValues.add(value);
          }
        } else {
          pendingValues.add(value);
          if (future != null) {
            future.setBulk(pendingValues);
            future.close();
            pendingValues.clear();
          }
        }
      }
    }

    @Override
    public void setBulk(@NotNull final Collection<U> values) {
      unsubscribe(input);
      final VarFuture<U> future = this.future;
      final ArrayList<U> pendingValues = this.pendingValues;
      synchronized (pendingValues) {
        if (pendingValues.isEmpty()) {
          if (future != null) {
            future.setBulk(values);
            future.close();
          } else {
            pendingValues.addAll(values);
          }
        } else {
          pendingValues.addAll(values);
          if (future != null) {
            future.setBulk(pendingValues);
            future.close();
            pendingValues.clear();
          }
        }
      }
    }
  }
}
