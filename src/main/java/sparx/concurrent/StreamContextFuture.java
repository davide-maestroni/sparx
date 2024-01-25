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
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

public abstract class StreamContextFuture<V, F extends SignalFuture<V>> implements StreamableFuture<V, F> {

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>> F1 then(
      @NotNull final Function<? super F, F1> firstFunction) {
    return thenImmediately(firstFunction);
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> F2 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction) {
    final StreamContext<V2> context = new StreamContext<V2>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      second.subscribe(context);
      subscribeFuture(future);
      return second;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> F3 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction) {
    final StreamContext<V3> context = new StreamContext<V3>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      third.subscribe(context);
      subscribeFuture(future);
      return third;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> F4 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction) {
    final StreamContext<V4> context = new StreamContext<V4>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      fourth.subscribe(context);
      subscribeFuture(future);
      return fourth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> F5 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction) {
    final StreamContext<V5> context = new StreamContext<V5>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      fifth.subscribe(context);
      subscribeFuture(future);
      return fifth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V6> context = new StreamContext<V6>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      sixth.subscribe(context);
      subscribeFuture(future);
      return sixth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V7> context = new StreamContext<V7>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      seventh.subscribe(context);
      subscribeFuture(future);
      return seventh;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V8> context = new StreamContext<V8>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      eighth.subscribe(context);
      subscribeFuture(future);
      return eighth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V9> context = new StreamContext<V9>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
      final F1 first = firstFunction.apply(future);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      final F9 ninth = ninthFunction.apply(eighth);
      ninth.subscribe(context);
      subscribeFuture(future);
      return ninth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V10> context = new StreamContext<V10>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      tenth.subscribe(context);
      subscribeFuture(future);
      return tenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V11> context = new StreamContext<V11>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      eleventh.subscribe(context);
      subscribeFuture(future);
      return eleventh;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V12> context = new StreamContext<V12>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      twelfth.subscribe(context);
      subscribeFuture(future);
      return twelfth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V13> context = new StreamContext<V13>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      thirteenth.subscribe(context);
      subscribeFuture(future);
      return thirteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V14> context = new StreamContext<V14>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      fourteenth.subscribe(context);
      subscribeFuture(future);
      return fourteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V15> context = new StreamContext<V15>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      fifteenth.subscribe(context);
      subscribeFuture(future);
      return fifteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V16> context = new StreamContext<V16>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      sixteenth.subscribe(context);
      subscribeFuture(future);
      return sixteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V17> context = new StreamContext<V17>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      seventeenth.subscribe(context);
      subscribeFuture(future);
      return seventeenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V18> context = new StreamContext<V18>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      eighteenth.subscribe(context);
      subscribeFuture(future);
      return eighteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V19> context = new StreamContext<V19>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      nineteenth.subscribe(context);
      subscribeFuture(future);
      return nineteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
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
    final StreamContext<V20> context = new StreamContext<V20>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F future = createFuture();
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
      twentieth.subscribe(context);
      subscribeFuture(future);
      return twentieth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>> F1 thenImmediately(
      @NotNull final Function<? super F, F1> firstFunction) {
    final StreamContext<V1> context = new StreamContext<V1>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F1 first = firstFunction.apply((F) this);
      first.subscribe(context);
      return first;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> F2 thenImmediately(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction) {
    final StreamContext<V2> context = new StreamContext<V2>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      second.subscribe(context);
      return second;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> F3 thenImmediately(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction) {
    final StreamContext<V3> context = new StreamContext<V3>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      third.subscribe(context);
      return third;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> F4 thenImmediately(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction) {
    final StreamContext<V4> context = new StreamContext<V4>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      fourth.subscribe(context);
      return fourth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> F5 thenImmediately(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction) {
    final StreamContext<V5> context = new StreamContext<V5>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      fifth.subscribe(context);
      return fifth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>> F6 thenImmediately(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction) {
    final StreamContext<V6> context = new StreamContext<V6>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      sixth.subscribe(context);
      return sixth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>> F7 thenImmediately(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction) {
    final StreamContext<V7> context = new StreamContext<V7>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      seventh.subscribe(context);
      return seventh;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>> F8 thenImmediately(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction) {
    final StreamContext<V8> context = new StreamContext<V8>(FutureContext.currentContext());
    FutureContext.pushContext(context);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      eighth.subscribe(context);
      return eighth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>> F9 thenImmediately(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction,
      @NotNull final Function<? super F7, F8> eighthFunction,
      @NotNull final Function<? super F8, F9> ninthFunction) {
    final StreamContext<V9> context = new StreamContext<V9>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      ninth.subscribe(context);
      return ninth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>> F10 thenImmediately(
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
    final StreamContext<V10> context = new StreamContext<V10>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      tenth.subscribe(context);
      return tenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>> F11 thenImmediately(
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
    final StreamContext<V11> context = new StreamContext<V11>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      eleventh.subscribe(context);
      return eleventh;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>> F12 thenImmediately(
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
    final StreamContext<V12> context = new StreamContext<V12>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      twelfth.subscribe(context);
      return twelfth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>> F13 thenImmediately(
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
    final StreamContext<V13> context = new StreamContext<V13>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      thirteenth.subscribe(context);
      return thirteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>> F14 thenImmediately(
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
    final StreamContext<V14> context = new StreamContext<V14>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      fourteenth.subscribe(context);
      return fourteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>> F15 thenImmediately(
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
    final StreamContext<V15> context = new StreamContext<V15>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      fifteenth.subscribe(context);
      return fifteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>> F16 thenImmediately(
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
    final StreamContext<V16> context = new StreamContext<V16>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      sixteenth.subscribe(context);
      return sixteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>> F17 thenImmediately(
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
    final StreamContext<V17> context = new StreamContext<V17>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      seventeenth.subscribe(context);
      return seventeenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>> F18 thenImmediately(
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
    final StreamContext<V18> context = new StreamContext<V18>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      eighteenth.subscribe(context);
      return eighteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>> F19 thenImmediately(
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
    final StreamContext<V19> context = new StreamContext<V19>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      nineteenth.subscribe(context);
      return nineteenth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>, V20, F20 extends SignalFuture<V20>> F20 thenImmediately(
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
    final StreamContext<V20> context = new StreamContext<V20>(FutureContext.currentContext());
    FutureContext.pushContext(context);
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
      twentieth.subscribe(context);
      return twentieth;
    } catch (final Exception e) {
      context.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureContext.popContext();
    }
  }

  protected abstract @NotNull F createFuture();

  protected abstract void subscribeFuture(@NotNull F future);
}
