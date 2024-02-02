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
import sparx.util.Require;
import sparx.util.UncheckedException;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

abstract class GeneratorScopeFuture<V> extends ReadOnlyFuture<V> implements GeneratingFuture<V> {

  GeneratorScopeFuture(@NotNull final StreamingFuture<V> future) {
    super(future);
  }

  @Override
  public @NotNull Subscription subscribe() {
    return subscribe(null, null, null, null);
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>> GeneratingFuture<V1> thenPulling(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction) {
    final StreamScope<V1> scope = new StreamScope<V1>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F1 output = input.thenSequentially(firstFunction);
      final GeneratorStreamReceiver<V1> receiver = new GeneratorStreamReceiver<V1>(input, output);
      final GeneratingFuture<V1> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> GeneratingFuture<V2> thenPulling(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction) {
    final StreamScope<V2> scope = new StreamScope<V2>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F2 output = input.thenSequentially(
          firstFunction,
          secondFunction
      );
      final GeneratorStreamReceiver<V2> receiver = new GeneratorStreamReceiver<V2>(input, output);
      final GeneratingFuture<V2> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>> GeneratingFuture<V3> thenPulling(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction) {
    final StreamScope<V3> scope = new StreamScope<V3>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F3 output = input.thenSequentially(
          firstFunction,
          secondFunction,
          thirdFunction
      );
      final GeneratorStreamReceiver<V3> receiver = new GeneratorStreamReceiver<V3>(input, output);
      final GeneratingFuture<V3> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>> GeneratingFuture<V4> thenPulling(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction) {
    final StreamScope<V4> scope = new StreamScope<V4>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F4 output = input.thenSequentially(
          firstFunction,
          secondFunction,
          thirdFunction,
          fourthFunction
      );
      final GeneratorStreamReceiver<V4> receiver = new GeneratorStreamReceiver<V4>(input, output);
      final GeneratingFuture<V4> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>> GeneratingFuture<V5> thenPulling(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction) {
    final StreamScope<V5> scope = new StreamScope<V5>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F5 output = input.thenSequentially(
          firstFunction,
          secondFunction,
          thirdFunction,
          fourthFunction,
          fifthFunction
      );
      final GeneratorStreamReceiver<V5> receiver = new GeneratorStreamReceiver<V5>(input, output);
      final GeneratingFuture<V5> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>> GeneratingFuture<V6> thenPulling(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction) {
    final StreamScope<V6> scope = new StreamScope<V6>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F6 output = input.thenSequentially(
          firstFunction,
          secondFunction,
          thirdFunction,
          fourthFunction,
          fifthFunction,
          sixthFunction
      );
      final GeneratorStreamReceiver<V6> receiver = new GeneratorStreamReceiver<V6>(input, output);
      final GeneratingFuture<V6> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>> GeneratingFuture<V7> thenPulling(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super F1, F2> secondFunction,
      @NotNull final Function<? super F2, F3> thirdFunction,
      @NotNull final Function<? super F3, F4> fourthFunction,
      @NotNull final Function<? super F4, F5> fifthFunction,
      @NotNull final Function<? super F5, F6> sixthFunction,
      @NotNull final Function<? super F6, F7> seventhFunction) {
    final StreamScope<V7> scope = new StreamScope<V7>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F7 output = input.thenSequentially(
          firstFunction,
          secondFunction,
          thirdFunction,
          fourthFunction,
          fifthFunction,
          sixthFunction,
          seventhFunction
      );
      final GeneratorStreamReceiver<V7> receiver = new GeneratorStreamReceiver<V7>(input, output);
      final GeneratingFuture<V7> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>> GeneratingFuture<V8> thenPulling(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      final VarFuture<V> input = VarFuture.create();
      final F8 output = input.thenSequentially(
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
      final GeneratingFuture<V8> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>> GeneratingFuture<V9> thenPulling(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
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
      final VarFuture<V> input = VarFuture.create();
      final F9 output = input.thenSequentially(
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
      final GeneratingFuture<V9> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>> GeneratingFuture<V10> thenPulling(
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
    final StreamScope<V10> scope = new StreamScope<V10>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F10 output = input.thenSequentially(
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
      final GeneratingFuture<V10> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>> GeneratingFuture<V11> thenPulling(
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
    final StreamScope<V11> scope = new StreamScope<V11>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F11 output = input.thenSequentially(
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
      final GeneratingFuture<V11> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>> GeneratingFuture<V12> thenPulling(
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
    final StreamScope<V12> scope = new StreamScope<V12>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F12 output = input.thenSequentially(
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
      final GeneratingFuture<V12> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>> GeneratingFuture<V13> thenPulling(
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
    final StreamScope<V13> scope = new StreamScope<V13>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F13 output = input.thenSequentially(
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
      final GeneratingFuture<V13> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>> GeneratingFuture<V14> thenPulling(
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
    final StreamScope<V14> scope = new StreamScope<V14>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F14 output = input.thenSequentially(
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
      final GeneratingFuture<V14> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>> GeneratingFuture<V15> thenPulling(
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
    final StreamScope<V15> scope = new StreamScope<V15>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F15 output = input.thenSequentially(
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
      final GeneratingFuture<V15> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>> GeneratingFuture<V16> thenPulling(
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
    final StreamScope<V16> scope = new StreamScope<V16>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F16 output = input.thenSequentially(
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
      final GeneratingFuture<V16> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>> GeneratingFuture<V17> thenPulling(
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
    final StreamScope<V17> scope = new StreamScope<V17>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F17 output = input.thenSequentially(
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
      final GeneratingFuture<V17> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>> GeneratingFuture<V18> thenPulling(
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
    final StreamScope<V18> scope = new StreamScope<V18>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F18 output = input.thenSequentially(
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
      final GeneratingFuture<V18> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>> GeneratingFuture<V19> thenPulling(
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
    final StreamScope<V19> scope = new StreamScope<V19>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F19 output = input.thenSequentially(
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
      final GeneratingFuture<V19> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>, V3, F3 extends SignalFuture<V3>, V4, F4 extends SignalFuture<V4>, V5, F5 extends SignalFuture<V5>, V6, F6 extends SignalFuture<V6>, V7, F7 extends SignalFuture<V7>, V8, F8 extends SignalFuture<V8>, V9, F9 extends SignalFuture<V9>, V10, F10 extends SignalFuture<V10>, V11, F11 extends SignalFuture<V11>, V12, F12 extends SignalFuture<V12>, V13, F13 extends SignalFuture<V13>, V14, F14 extends SignalFuture<V14>, V15, F15 extends SignalFuture<V15>, V16, F16 extends SignalFuture<V16>, V17, F17 extends SignalFuture<V17>, V18, F18 extends SignalFuture<V18>, V19, F19 extends SignalFuture<V19>, V20, F20 extends SignalFuture<V20>> GeneratingFuture<V20> thenPulling(
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
    final StreamScope<V20> scope = new StreamScope<V20>(FutureScope.currentScope());
    FutureScope.pushScope(scope);
    try {
      final VarFuture<V> input = VarFuture.create();
      final F20 output = input.thenSequentially(
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
      final GeneratingFuture<V20> future = createGeneratingFuture(receiver);
      future.subscribe(scope);
      return future;
    } catch (final Exception e) {
      scope.onReceiverError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureScope.popScope();
    }
  }

  abstract @NotNull <U> GeneratingFuture<U> createGeneratingFuture(
      @NotNull Supplier<? extends SignalFuture<U>> supplier);

  private class GeneratorStreamReceiver<U> implements Receiver<U>, Supplier<SignalFuture<U>> {

    private final VarFuture<V> input;
    private final SignalFuture<U> output;
    private final ArrayList<U> pendingValues = new ArrayList<U>();
    private final Receiver<V> receiver = new Receiver<V>() {
      @Override
      public void close() {
        input.close();
      }

      @Override
      public boolean fail(@NotNull final Exception error) {
        return input.fail(error);
      }

      @Override
      public void set(final V value) {
        unsubscribe(this);
        input.set(value);
      }

      @Override
      public void setBulk(@NotNull final Collection<V> values) {
        unsubscribe(this);
        input.setBulk(values);
      }
    };

    private VarFuture<U> future;
    private volatile Supplier<SignalFuture<U>> status = new RunningStatus();

    private GeneratorStreamReceiver(@NotNull final VarFuture<V> input,
        @NotNull final SignalFuture<U> output) {
      this.input = input;
      this.output = Require.notNull(output, "output");
    }

    @Override
    public void close() {
      final ArrayList<U> pendingValues = this.pendingValues;
      synchronized (pendingValues) {
        status = new ClosedStatus();
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
        status = new FailedStatus(error);
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
    public SignalFuture<U> get() throws Exception {
      return status.get();
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

    private class ClosedStatus implements Supplier<SignalFuture<U>> {

      @Override
      public SignalFuture<U> get() {
        final ArrayList<U> pendingValues = GeneratorStreamReceiver.this.pendingValues;
        synchronized (pendingValues) {
          if (!pendingValues.isEmpty()) {
            final ValFuture<U> future = ValFuture.ofBulk(pendingValues);
            pendingValues.clear();
            return future;
          }
        }
        return null;
      }
    }

    private class FailedStatus implements Supplier<SignalFuture<U>> {

      private final Exception failureException;

      private FailedStatus(@NotNull final Exception error) {
        failureException = error;
      }

      @Override
      public SignalFuture<U> get() {
        final ArrayList<U> pendingValues = GeneratorStreamReceiver.this.pendingValues;
        synchronized (pendingValues) {
          if (!pendingValues.isEmpty()) {
            final VarFuture<U> future = VarFuture.create();
            future.setBulk(pendingValues);
            future.fail(failureException);
            pendingValues.clear();
            return future;
          }
        }
        return ValFuture.ofFailure(failureException);
      }
    }

    private class RunningStatus implements Supplier<SignalFuture<U>> {

      @Override
      public SignalFuture<U> get() {
        final ArrayList<U> pendingValues = GeneratorStreamReceiver.this.pendingValues;
        synchronized (pendingValues) {
          if (!pendingValues.isEmpty()) {
            final ValFuture<U> future = ValFuture.ofBulk(pendingValues);
            pendingValues.clear();
            return future;
          } else {
            future = VarFuture.create();
          }
        }
        output.subscribe(GeneratorStreamReceiver.this);
        subscribeNext(receiver);
        return future;
      }
    }
  }
}
