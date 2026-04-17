/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.iterator;

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.itf.Iterator;
import sparx1.util.DequeArrayList;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Nullable;
import sparx1.util.annotation.Positive;
import sparx1.util.function.Function;
import sparx1.util.function.IndexedFunction;

public class PartitionIteratorMaterializer<E, I extends Iterator<E, ?>> extends
    StatefulIteratorMaterializer<I> {

  public PartitionIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedFunction<? super E, Integer> indexExtractor, final int numPartitions,
      final @NotNull Function<IteratorMaterializer<E>, I> factory) {
    setState(new InitialState(wrapped, indexExtractor, numPartitions, factory));
  }

  private class InitialState implements IteratorMaterializer<I> {

    private final Function<IteratorMaterializer<E>, I> factory;
    private final IndexedFunction<? super E, Integer> indexExtractor;
    private final DequeArrayList<?>[] partitions;
    private final boolean[] skippedPartitions;
    private final IteratorMaterializer<E> wrapped;

    private int index;
    private int pos;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedFunction<? super E, Integer> indexExtractor, final int numPartitions,
        final @NotNull Function<IteratorMaterializer<E>, I> factory) {
      this.wrapped = wrapped;
      this.indexExtractor = indexExtractor;
      this.factory = factory;
      partitions = new DequeArrayList[numPartitions];
      skippedPartitions = new boolean[numPartitions];
    }

    @Override
    public int currentKnownSize() {
      return partitions.length;
    }

    @Override
    public boolean isSizeKnown() {
      return true;
    }

    @Override
    public boolean materializeHasNext() {
      return index < partitions.length;
    }

    @Override
    public I materializeNext() {
      final StatefulIteratorMaterializer<E> materializer = new StatefulIteratorMaterializer<E>() {
      };
      materializer.setState(new PartitionState(getPartition(index++)));
      try {
        return factory.apply(materializer);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      int i = index;
      for (; i < count && index < partitions.length; ++i, ++index) {
        partitions[index] = null;
        skippedPartitions[index] = true;
      }
      return i;
    }

    private boolean advance() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        final int pos = this.pos;
        final E next = wrapped.materializeNext();
        ++this.pos;
        try {
          final int index = indexExtractor.apply(pos, next);
          if (index < 0 || index >= partitions.length) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
          }
          final DequeArrayList<E> partition = getPartition(index);
          if (partition != null) {
            partition.add(next);
          }
          return true;
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
      return false;
    }

    private @Nullable DequeArrayList<E> getPartition(final int index) {
      if (skippedPartitions[index]) {
        return null;
      }
      final DequeArrayList<?>[] partitions = this.partitions;
      @SuppressWarnings("unchecked") DequeArrayList<E> partition = (DequeArrayList<E>) partitions[index];
      if (partition == null) {
        partitions[index] = partition = new DequeArrayList<E>();
      }
      return partition;
    }

    private class PartitionState implements IteratorMaterializer<E> {

      private final DequeArrayList<E> partition;

      public PartitionState(final @NotNull DequeArrayList<E> partition) {
        this.partition = partition;
      }

      @Override
      public int currentKnownSize() {
        return -1;
      }

      @Override
      public boolean isSizeKnown() {
        return false;
      }

      @Override
      public boolean materializeHasNext() {
        final DequeArrayList<E> partition = this.partition;
        do {
          if (!partition.isEmpty()) {
            return true;
          }
        } while (advance());
        setEmptyState();
        return false;
      }

      @Override
      public E materializeNext() {
        if (!materializeHasNext()) {
          throw new NoSuchElementException();
        }
        return partition.removeFirst();
      }

      @Override
      public int materializeSkip(final @Positive int count) {
        final DequeArrayList<E> partition = this.partition;
        int skipped = Math.min(count, partition.size());
        if (skipped > 0) {
          partition.removeRange(0, skipped);
        }
        for (; skipped < count && materializeHasNext(); ++skipped) {
          materializeNext();
        }
        return skipped;
      }
    }
  }
}
