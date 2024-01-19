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

import java.util.Collection;
import java.util.LinkedList;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.Scheduler.Task;
import sparx.function.BinaryFunction;
import sparx.function.Consumer;
import sparx.logging.Log;
import sparx.util.Requires;

public class GeneratorFuture<V> implements Receiver<V> {

  private final Consumer<? super Receiver<V>> consumer;
  private final PullVarFuture future;
  private final LinkedList<V> pendingValues = new LinkedList<V>();
  private final Scheduler scheduler;

  private boolean pullFromIterator = false;
  private boolean pullFromJoin = false;
  private boolean pullFromReceiver = false;

  public static @NotNull <V> StreamingFuture<V> of(
      @NotNull final Consumer<? super Receiver<V>> consumer) {
    return new GeneratorFuture<V>(consumer).getFuture().readOnly();
  }

  public static @NotNull <V> StreamingFuture<V> of(final V initialValue,
      @NotNull final BinaryFunction<? super V, ? super Receiver<V>, V> function) {
    return new GeneratorFuture<V>(new Consumer<Receiver<V>>() {
      private V value = initialValue;

      @Override
      public void accept(final Receiver<V> receiver) throws Exception {
        value = function.apply(value, receiver);
      }
    }).getFuture().readOnly();
  }

  private GeneratorFuture(@NotNull final Consumer<? super Receiver<V>> consumer) {
    this.consumer = Requires.notNull(consumer, "consumer");
    this.future = new PullVarFuture();
    this.scheduler = this.future.scheduler();
  }

  @Override
  public void close() {
    future.close();
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    return future.fail(error);
  }

  @Override
  public void set(final V value) {
    pendingValues.add(value);
    consumeValues();
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    pendingValues.addAll(values);
    consumeValues();
  }

  private void consumeValues() {
    final LinkedList<V> pendingValues = this.pendingValues;
    if (!pendingValues.isEmpty()) {
      if (pullFromJoin || pullFromReceiver) {
        future.setBulk(pendingValues);
        pendingValues.clear();
        pullFromReceiver = false;
        pullFromIterator = false;
      } else if (pullFromIterator) {
        future.set(pendingValues.removeFirst());
        pullFromIterator = false;
      }
    }
  }

  private @NotNull StreamingFuture<V> getFuture() {
    return future;
  }

  private boolean isPull() {
    return pullFromJoin || pullFromReceiver || pullFromIterator;
  }

  private void pull() {
    consumeValues();
    if (isPull()) {
      try {
        consumer.accept(this);
      } catch (final Exception e) {
        Log.err(GeneratorFuture.class, "Failed to generate new values: %s", Log.printable(e));
        future.fail(e);
      }
    }
  }

  private class PullVarFuture extends VarFuture<V> {

    @Override
    protected void pullFromIterator() {
      scheduler.scheduleBefore(new PullTask() {
        @Override
        public void run() {
          final boolean wasPull = isPull();
          pullFromIterator = true;
          if (!wasPull) {
            pull();
          }
        }
      });
    }

    @Override
    protected void pullFromJoin(final boolean isPull) {
      final PullTask task;
      if (isPull) {
        task = new PullTask() {
          @Override
          public void run() {
            final boolean wasPull = isPull();
            pullFromJoin = true;
            if (!wasPull) {
              pull();
            }
          }
        };
      } else {
        task = new PullTask() {
          @Override
          public void run() {
            pullFromJoin = false;
          }
        };
      }
      scheduler.scheduleBefore(task);
    }

    @Override
    protected void pullFromReceiver() {
      final boolean wasPull = isPull();
      pullFromReceiver = true;
      if (!wasPull) {
        pull();
      }
    }

    private abstract class PullTask implements Task {

      @Override
      public @NotNull String taskID() {
        return PullVarFuture.super.taskID();
      }

      @Override
      public int weight() {
        return 1;
      }
    }
  }
}
