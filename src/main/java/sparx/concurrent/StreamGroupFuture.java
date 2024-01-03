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
///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////
package sparx.concurrent;

import org.jetbrains.annotations.NotNull;
import sparx.function.Function;

abstract class StreamGroupFuture<V, F extends SignalFuture<V>> implements StreamableFuture<V, F> {

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>> F1 then(
      @NotNull final Function<? super F, F1> firstFunction) {
    final StreamGroup<V1> group = new StreamGroup<V1>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final F1 first = firstFunction.apply((F) this);
      first.subscribe(group);
      return first;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> F2 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction) {
    final StreamGroup<V2> group = new StreamGroup<V2>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      second.subscribe(group);
      return second;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> F3 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction) {
    final StreamGroup<V3> group = new StreamGroup<V3>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      third.subscribe(group);
      return third;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> F4 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction) {
    final StreamGroup<V4> group = new StreamGroup<V4>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      fourth.subscribe(group);
      return fourth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> F5 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction) {
    final StreamGroup<V5> group = new StreamGroup<V5>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      fifth.subscribe(group);
      return fifth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>> F6 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction) {
    final StreamGroup<V6> group = new StreamGroup<V6>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      sixth.subscribe(group);
      return sixth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>> F7 then(
      @NotNull final Function<? super F, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction) {
    final StreamGroup<V7> group = new StreamGroup<V7>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      seventh.subscribe(group);
      return seventh;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>> F8 then(
      @NotNull final Function<? super F, F1> firstFunction,
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
      final F1 first = firstFunction.apply((F) this);
      final F2 second = secondFunction.apply(first);
      final F3 third = thirdFunction.apply(second);
      final F4 fourth = fourthFunction.apply(third);
      final F5 fifth = fifthFunction.apply(fourth);
      final F6 sixth = sixthFunction.apply(fifth);
      final F7 seventh = seventhFunction.apply(sixth);
      final F8 eighth = eighthFunction.apply(seventh);
      eighth.subscribe(group);
      return eighth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V9> group = new StreamGroup<V9>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      ninth.subscribe(group);
      return ninth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V10> group = new StreamGroup<V10>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      tenth.subscribe(group);
      return tenth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V11> group = new StreamGroup<V11>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      eleventh.subscribe(group);
      return eleventh;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V12> group = new StreamGroup<V12>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      twelfth.subscribe(group);
      return twelfth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V13> group = new StreamGroup<V13>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      thirteenth.subscribe(group);
      return thirteenth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V14> group = new StreamGroup<V14>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      fourteenth.subscribe(group);
      return fourteenth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V15> group = new StreamGroup<V15>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      fifteenth.subscribe(group);
      return fifteenth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V16> group = new StreamGroup<V16>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      sixteenth.subscribe(group);
      return sixteenth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V17> group = new StreamGroup<V17>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      seventeenth.subscribe(group);
      return seventeenth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V18> group = new StreamGroup<V18>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      eighteenth.subscribe(group);
      return eighteenth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V19> group = new StreamGroup<V19>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      nineteenth.subscribe(group);
      return nineteenth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final StreamGroup<V20> group = new StreamGroup<V20>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
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
      twentieth.subscribe(group);
      return twentieth;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }
}
