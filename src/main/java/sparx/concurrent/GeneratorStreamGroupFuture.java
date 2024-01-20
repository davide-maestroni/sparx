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
import sparx.function.Supplier;
import sparx.util.UncheckedException;

abstract class GeneratorStreamGroupFuture<V> extends ReadOnlyFuture<V> {

  GeneratorStreamGroupFuture(@NotNull final StreamingFuture<V> future) {
    super(future);
  }

  public @NotNull <V1, F1 extends SignalFuture<V1>, V2, F2 extends SignalFuture<V2>> StreamingFuture<V2> thenPull(
      @NotNull final Function<? super StreamingFuture<V>, F1> firstFunction,
      @NotNull final Function<? super StreamingFuture<V1>, F2> secondFunction) {
    final StreamGroup<V2> group = new StreamGroup<V2>(FutureGroup.currentGroup());
    FutureGroup.pushGroup(group);
    try {
      final VarFuture<V> firstInput = VarFuture.create();
      final StreamingFuture<V1> first = GeneratorFuture.of(new Supplier<SignalFuture<V1>>() {
        private final SignalFuture<V1> future = firstFunction.apply(firstInput);

        @Override
        public SignalFuture<V1> get() throws Exception {
          return null;
        }
      });
//      final StreamingFuture<V1> first = GeneratorFuture.of(new Consumer<Receiver<V1>>() {
//        private final SignalFuture<V1> future = firstFunction.apply(firstInput.readOnly());
//
//        @Override
//        public void accept(final Receiver<V1> receiver) {
//          future.subscribe(receiver);
//          subscribe(new Receiver<V>() {
//            @Override
//            public void close() {
//              firstInput.close();
//            }
//
//            @Override
//            public boolean fail(@NotNull final Exception error) {
//              return firstInput.fail(error);
//            }
//
//            @Override
//            public void set(final V value) {
//              firstInput.set(value);
//              unsubscribe(this);
//            }
//
//            @Override
//            public void setBulk(@NotNull final Collection<V> values) {
//              firstInput.setBulk(values);
//              unsubscribe(this);
//            }
//          });
//        }
//      });
      final StreamingFuture<V2> second = GeneratorFuture.of(new Supplier<SignalFuture<V2>>() {
        private final SignalFuture<V2> future = secondFunction.apply(first);

        @Override
        public SignalFuture<V2> get() throws Exception {
          return null;
        }
      });
//      final StreamingFuture<V2> second = GeneratorFuture.of(new Consumer<Receiver<V2>>() {
//        private SignalFuture<V2> f = null;
//
//        @Override
//        public void accept(final Receiver<V2> receiver) throws Exception {
//          if (f == null) {
//            f = secondFunction.apply(first);
//          }
//          f.subscribe(new Receiver<V2>() {
//            @Override
//            public void close() {
//              receiver.close();
//            }
//
//            @Override
//            public boolean fail(@NotNull final Exception error) {
//              return receiver.fail(error);
//            }
//
//            @Override
//            public void set(final V2 value) {
//              receiver.set(value);
//              f.unsubscribe(this);
//            }
//
//            @Override
//            public void setBulk(@NotNull final Collection<V2> values) {
//              receiver.setBulk(values);
//              f.unsubscribe(this);
//            }
//          });
//        }
//      });
      second.subscribe(group);
      return second;
    } catch (final Exception e) {
      group.onUncaughtError(e);
      throw UncheckedException.throwUnchecked(e);
    } finally {
      FutureGroup.popGroup();
    }
  }
}
