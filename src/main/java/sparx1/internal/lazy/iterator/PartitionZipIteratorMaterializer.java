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

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.itf.Iterator;
import sparx1.util.DequeArrayList;
import sparx1.util.UncheckedException;
import sparx1.util.ZipEntry;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Nullable;
import sparx1.util.annotation.Positive;
import sparx1.util.function.Function;
import sparx1.util.function.IndexedFunction;

public class PartitionZipIteratorMaterializer<E, K, I extends Iterator<E, ?>> extends
    StatefulIteratorMaterializer<ZipEntry<K, I>> {

  public PartitionZipIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedFunction<? super E, K> keyExtractor,
      final @NotNull Function<IteratorMaterializer<E>, I> factory) {
    setState(new InitialState(wrapped, keyExtractor, factory));
  }

  private class InitialState extends AbstractStateIteratorMaterializer {

    private final Function<IteratorMaterializer<E>, I> factory;
    private final IndexedFunction<? super E, K> keyExtractor;
    private final LinkedHashMap<K, DequeArrayList<E>> partitions = new LinkedHashMap<K, DequeArrayList<E>>();
    private final IteratorMaterializer<E> wrapped;

    private int index;
    private int pos;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedFunction<? super E, K> keyExtractor,
        final @NotNull Function<IteratorMaterializer<E>, I> factory) {
      this.wrapped = wrapped;
      this.keyExtractor = keyExtractor;
      this.factory = factory;
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
      final int index = this.index;
      final LinkedHashMap<K, DequeArrayList<E>> partitions = this.partitions;
      do {
        if (index < partitions.size()) {
          return true;
        }
      } while (advance());
      return false;
    }

    @Override
    public ZipEntry<K, I> materializeNext() {
      final Entry<K, DequeArrayList<E>> entry = getPartition(index++);
      if (entry == null) {
        throw new NoSuchElementException();
      }
      final StatefulIteratorMaterializer<E> materializer = new StatefulIteratorMaterializer<E>() {
      };
      materializer.setState(new PartitionState(entry.getValue()));
      try {
        return ZipEntry.of(entry.getKey(), factory.apply(materializer));
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final LinkedHashMap<K, DequeArrayList<E>> partitions = this.partitions;
      final java.util.Iterator<Entry<K, DequeArrayList<E>>> iterator = partitions.entrySet()
          .iterator();
      for (int i = 0; i < index; ++i) {
        iterator.next();
      }
      int i = 0;
      for (; i < count && index < partitions.size(); ++i, ++index) {
        iterator.next().setValue(null);
      }
      return i == count ? i : super.materializeSkip(count - i);
    }

    private boolean advance() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        final int pos = this.pos;
        final E next = wrapped.materializeNext();
        ++this.pos;
        try {
          final K key = keyExtractor.apply(pos, next);
          final LinkedHashMap<K, DequeArrayList<E>> partitions = this.partitions;
          final DequeArrayList<E> partition;
          if (!partitions.containsKey(key)) {
            partition = new DequeArrayList<E>();
            partitions.put(key, partition);
          } else {
            partition = partitions.get(key);
          }
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

    private @Nullable Entry<K, DequeArrayList<E>> getPartition(final int index) {
      final LinkedHashMap<K, DequeArrayList<E>> partitions = this.partitions;
      do {
        if (index < partitions.size()) {
          final java.util.Iterator<Entry<K, DequeArrayList<E>>> iterator = partitions.entrySet()
              .iterator();
          for (int i = 0; i < index; ++i) {
            iterator.next();
          }
          return iterator.next();
        }
      } while (advance());
      return null;
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
