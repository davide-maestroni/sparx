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
import sparx.util.UncheckedException;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/StreamScopeFuture.mustache
///////////////////////////////////////////////

public abstract class StreamScopeFuture<V, F extends SignalFuture<V>> implements
    StreamableFuture<V, F> {

  protected static @NotNull <V> StreamingFuture<V> pauseFuture(
      @NotNull final StreamingFuture<V> future) {
    return new PausedFuture<V>(future);
  }

  protected static void resumeFuture(@NotNull final StreamingFuture<?> pausedFuture) {
    ((PausedFuture<?>) pausedFuture).resume();
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>> F1 then(
      @NotNull final Function<? super F, F1> firstFunction) {
    return thenSequentially(firstFunction);
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> F2 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction) {
    final StreamScope<V2> scope = new StreamScope<V2>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      second.subscribe(scope);
      resumePaused(future);
      return second;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> F3 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction) {
    final StreamScope<V3> scope = new StreamScope<V3>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      third.subscribe(scope);
      resumePaused(future);
      return third;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> F4 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction) {
    final StreamScope<V4> scope = new StreamScope<V4>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      fourth.subscribe(scope);
      resumePaused(future);
      return fourth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> F5 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction) {
    final StreamScope<V5> scope = new StreamScope<V5>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      fifth.subscribe(scope);
      resumePaused(future);
      return fifth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>> F6 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction) {
    final StreamScope<V6> scope = new StreamScope<V6>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      sixth.subscribe(scope);
      resumePaused(future);
      return sixth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>> F7 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction) {
    final StreamScope<V7> scope = new StreamScope<V7>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      seventh.subscribe(scope);
      resumePaused(future);
      return seventh;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>> F8 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction) {
    final StreamScope<V8> scope = new StreamScope<V8>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      eighth.subscribe(scope);
      resumePaused(future);
      return eighth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>> F9 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction) {
    final StreamScope<V9> scope = new StreamScope<V9>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      ninth.subscribe(scope);
      resumePaused(future);
      return ninth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>> F10 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction) {
    final StreamScope<V10> scope = new StreamScope<V10>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      tenth.subscribe(scope);
      resumePaused(future);
      return tenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>> F11 then(
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
      @NotNull final Function<? super F10, F11> eleventhFunction) {
    final StreamScope<V11> scope = new StreamScope<V11>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      eleventh.subscribe(scope);
      resumePaused(future);
      return eleventh;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>> F12 then(
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
      @NotNull final Function<? super F11, F12> twelfthFunction) {
    final StreamScope<V12> scope = new StreamScope<V12>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      twelfth.subscribe(scope);
      resumePaused(future);
      return twelfth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>> F13 then(
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
      @NotNull final Function<? super F12, F13> thirteenthFunction) {
    final StreamScope<V13> scope = new StreamScope<V13>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      thirteenth.subscribe(scope);
      resumePaused(future);
      return thirteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>> F14 then(
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
      @NotNull final Function<? super F13, F14> fourteenthFunction) {
    final StreamScope<V14> scope = new StreamScope<V14>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      fourteenth.subscribe(scope);
      resumePaused(future);
      return fourteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>> F15 then(
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
      @NotNull final Function<? super F14, F15> fifteenthFunction) {
    final StreamScope<V15> scope = new StreamScope<V15>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      fifteenth.subscribe(scope);
      resumePaused(future);
      return fifteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>> F16 then(
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
      @NotNull final Function<? super F15, F16> sixteenthFunction) {
    final StreamScope<V16> scope = new StreamScope<V16>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      sixteenth.subscribe(scope);
      resumePaused(future);
      return sixteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>> F17 then(
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
      @NotNull final Function<? super F16, F17> seventeenthFunction) {
    final StreamScope<V17> scope = new StreamScope<V17>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      final F17 seventeenth = seventeenthFunction.apply(sixteenth);
      seventeenth.subscribe(scope);
      resumePaused(future);
      return seventeenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>> F18 then(
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
      @NotNull final Function<? super F17, F18> eighteenthFunction) {
    final StreamScope<V18> scope = new StreamScope<V18>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      final F17 seventeenth = seventeenthFunction.apply(sixteenth);
      final F18 eighteenth = eighteenthFunction.apply(seventeenth);
      eighteenth.subscribe(scope);
      resumePaused(future);
      return eighteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>> F19 then(
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
      @NotNull final Function<? super F18, F19> nineteenthFunction) {
    final StreamScope<V19> scope = new StreamScope<V19>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      final F17 seventeenth = seventeenthFunction.apply(sixteenth);
      final F18 eighteenth = eighteenthFunction.apply(seventeenth);
      final F19 nineteenth = nineteenthFunction.apply(eighteenth);
      nineteenth.subscribe(scope);
      resumePaused(future);
      return nineteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>, V20, F20 extends SignalFuture<V20>> F20 then(
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
      @NotNull final Function<? super F19, F20> twentiethFunction) {
    final StreamScope<V20> scope = new StreamScope<V20>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F future = createPaused();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      final F17 seventeenth = seventeenthFunction.apply(sixteenth);
      final F18 eighteenth = eighteenthFunction.apply(seventeenth);
      final F19 nineteenth = nineteenthFunction.apply(eighteenth);
      final F20 twentieth = twentiethFunction.apply(nineteenth);
      twentieth.subscribe(scope);
      resumePaused(future);
      return twentieth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>> F1 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction) {
    final StreamScope<V1> scope = new StreamScope<V1>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      first.subscribe(scope);
      return first;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> F2 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction) {
    final StreamScope<V2> scope = new StreamScope<V2>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      second.subscribe(scope);
      return second;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> F3 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction) {
    final StreamScope<V3> scope = new StreamScope<V3>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      third.subscribe(scope);
      return third;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> F4 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction) {
    final StreamScope<V4> scope = new StreamScope<V4>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      fourth.subscribe(scope);
      return fourth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> F5 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction) {
    final StreamScope<V5> scope = new StreamScope<V5>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      fifth.subscribe(scope);
      return fifth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>> F6 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction) {
    final StreamScope<V6> scope = new StreamScope<V6>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      sixth.subscribe(scope);
      return sixth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>> F7 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction) {
    final StreamScope<V7> scope = new StreamScope<V7>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      seventh.subscribe(scope);
      return seventh;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>> F8 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction) {
    final StreamScope<V8> scope = new StreamScope<V8>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      eighth.subscribe(scope);
      return eighth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>> F9 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction) {
    final StreamScope<V9> scope = new StreamScope<V9>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      ninth.subscribe(scope);
      return ninth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>> F10 thenSequentially(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction,
      @NotNull final Function<? super F9, F10> tenthFunction) {
    final StreamScope<V10> scope = new StreamScope<V10>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      tenth.subscribe(scope);
      return tenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>> F11 thenSequentially(
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
      @NotNull final Function<? super F10, F11> eleventhFunction) {
    final StreamScope<V11> scope = new StreamScope<V11>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      eleventh.subscribe(scope);
      return eleventh;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>> F12 thenSequentially(
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
      @NotNull final Function<? super F11, F12> twelfthFunction) {
    final StreamScope<V12> scope = new StreamScope<V12>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      twelfth.subscribe(scope);
      return twelfth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>> F13 thenSequentially(
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
      @NotNull final Function<? super F12, F13> thirteenthFunction) {
    final StreamScope<V13> scope = new StreamScope<V13>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      thirteenth.subscribe(scope);
      return thirteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>> F14 thenSequentially(
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
      @NotNull final Function<? super F13, F14> fourteenthFunction) {
    final StreamScope<V14> scope = new StreamScope<V14>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      fourteenth.subscribe(scope);
      return fourteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>> F15 thenSequentially(
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
      @NotNull final Function<? super F14, F15> fifteenthFunction) {
    final StreamScope<V15> scope = new StreamScope<V15>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      fifteenth.subscribe(scope);
      return fifteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>> F16 thenSequentially(
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
      @NotNull final Function<? super F15, F16> sixteenthFunction) {
    final StreamScope<V16> scope = new StreamScope<V16>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      sixteenth.subscribe(scope);
      return sixteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>> F17 thenSequentially(
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
      @NotNull final Function<? super F16, F17> seventeenthFunction) {
    final StreamScope<V17> scope = new StreamScope<V17>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      final F17 seventeenth = seventeenthFunction.apply(sixteenth);
      seventeenth.subscribe(scope);
      return seventeenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>> F18 thenSequentially(
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
      @NotNull final Function<? super F17, F18> eighteenthFunction) {
    final StreamScope<V18> scope = new StreamScope<V18>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      final F17 seventeenth = seventeenthFunction.apply(sixteenth);
      final F18 eighteenth = eighteenthFunction.apply(seventeenth);
      eighteenth.subscribe(scope);
      return eighteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>> F19 thenSequentially(
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
      @NotNull final Function<? super F18, F19> nineteenthFunction) {
    final StreamScope<V19> scope = new StreamScope<V19>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      final F17 seventeenth = seventeenthFunction.apply(sixteenth);
      final F18 eighteenth = eighteenthFunction.apply(seventeenth);
      final F19 nineteenth = nineteenthFunction.apply(eighteenth);
      nineteenth.subscribe(scope);
      return nineteenth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>, V20, F20 extends SignalFuture<V20>> F20 thenSequentially(
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
      @NotNull final Function<? super F19, F20> twentiethFunction) {
    final StreamScope<V20> scope = new StreamScope<V20>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      final F10 tenth = tenthFunction.apply(ninth);
      final F11 eleventh = eleventhFunction.apply(tenth);
      final F12 twelfth = twelfthFunction.apply(eleventh);
      final F13 thirteenth = thirteenthFunction.apply(twelfth);
      final F14 fourteenth = fourteenthFunction.apply(thirteenth);
      final F15 fifteenth = fifteenthFunction.apply(fourteenth);
      final F16 sixteenth = sixteenthFunction.apply(fifteenth);
      final F17 seventeenth = seventeenthFunction.apply(sixteenth);
      final F18 eighteenth = eighteenthFunction.apply(seventeenth);
      final F19 nineteenth = nineteenthFunction.apply(eighteenth);
      final F20 twentieth = twentiethFunction.apply(nineteenth);
      twentieth.subscribe(scope);
      return twentieth;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  protected abstract @NotNull F createPaused();

  protected abstract void resumePaused(@NotNull F pausedFuture);
}
