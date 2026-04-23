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
package sparx1;

import static sparx1.lazy.getKnownSize;
import static sparx1.util.function.Functions.indexedIdentity;
import static sparx1.util.function.Functions.toIndexedFunction;

import java.util.Collection;
import java.util.Comparator;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.internal.lazy.list.AppendAllListMaterializer;
import sparx1.internal.lazy.list.AppendListMaterializer;
import sparx1.internal.lazy.list.ArrayToListMaterializer;
import sparx1.internal.lazy.list.CollectionToListMaterializer;
import sparx1.internal.lazy.list.CountListMaterializer;
import sparx1.internal.lazy.list.DiffListMaterializer;
import sparx1.internal.lazy.list.DistinctByListMaterializer;
import sparx1.internal.lazy.list.ElementToListMaterializer;
import sparx1.internal.lazy.list.EmptyListMaterializer;
import sparx1.internal.lazy.list.IteratorToListMaterializer;
import sparx1.internal.lazy.list.ListToListMaterializer;
import sparx1.internal.lazy.list.SuppliedListMaterializer;
import sparx1.lazy.Iterator;
import sparx1.lazy.List;
import sparx1.lazy.ListIterator;
import sparx1.util.DequeArrayList;
import sparx1.util.Require;
import sparx1.util.UncheckedException;
import sparx1.util.ZipEntry;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Nullable;
import sparx1.util.function.Action;
import sparx1.util.function.BinaryFunction;
import sparx1.util.function.Consumer;
import sparx1.util.function.Function;
import sparx1.util.function.IndexedConsumer;
import sparx1.util.function.IndexedFunction;
import sparx1.util.function.IndexedPredicate;
import sparx1.util.function.Predicate;
import sparx1.util.function.Supplier;

public class LazyList<E> extends List<E> {

  private static final LazyList<?> EMPTY_LIST = new LazyList<Object>(
      EmptyListMaterializer.instance());
  //  private static final Splitter<?, ? extends LazyList<?>> SPLITTER = new Splitter<Object, LazyList<Object>>() {
//    @Override
//    public @NotNull LazyList<Object> getChunk(final @NotNull ListMaterializer<Object> materializer,
//        final int start, final int end) {
//      return new LazyList<Object>(materializer).slice(start, end);
//    }
//  };
  private static final LazyList<Boolean> FALSE_LIST = new LazyList<Boolean>(
      new ElementToListMaterializer<Boolean>(false));
  private static final LazyList<?> NULL_LIST = new LazyList<Object>(
      new ElementToListMaterializer<Object>(null));
  private static final LazyList<Boolean> TRUE_LIST = new LazyList<Boolean>(
      new ElementToListMaterializer<Boolean>(true));
  private static final LazyList<Integer> ZERO_LIST = new LazyList<Integer>(
      new ElementToListMaterializer<Integer>(0));

  final ListMaterializer<E> materializer;

  LazyList(final @NotNull ListMaterializer<E> materializer) {
    this.materializer = materializer;
  }

  @SuppressWarnings("unchecked")
  static @NotNull <E> LazyList<E> emptyList() {
    return (LazyList<E>) EMPTY_LIST;
  }

  @SuppressWarnings("unchecked")
  static @NotNull <E> ListMaterializer<E> getElementsMaterializer(
      final @NotNull Iterable<? extends E> elements) {
    if (elements instanceof LazyList) {
      return ((LazyList<E>) elements).materializer;
    }
    if (elements instanceof java.util.List) {
      final java.util.List<E> list = (java.util.List<E>) elements;
      final int size = list.size();
      if (size == 0) {
        return EmptyListMaterializer.instance();
      }
      if (size == 1) {
        return new ElementToListMaterializer<E>(list.get(0));
      }
      return new ListToListMaterializer<E>(list);
    }
    if (elements instanceof Collection) {
      final Collection<E> collection = (Collection<E>) elements;
      if (collection.isEmpty()) {
        return EmptyListMaterializer.instance();
      }
      return new CollectionToListMaterializer<E>(collection);
    }
    return new IteratorToListMaterializer<E>((java.util.Iterator<E>) elements.iterator());
  }

  private static @NotNull <E> LazyList<E> elementList(final @Nullable E element) {
    return new LazyList<E>(new ElementToListMaterializer<E>(element));
  }

  @Override
  public List<E> append(final E element) {
    final ListMaterializer<E> materializer = this.materializer;
    if (materializer.knownSize() == 0) {
      return new LazyList<E>(new ElementToListMaterializer<E>(element));
    }
    return new LazyList<E>(new AppendListMaterializer<E>(materializer, element));
  }

  @Override
  public List<E> appendAll(final @NotNull Iterable<? extends E> elements) {
    final ListMaterializer<E> materializer = this.materializer;
    final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
        Require.notNull(elements, "elements"));
    if (materializer.knownSize() == 0) {
      return new LazyList<E>(elementsMaterializer);
    }
    return new LazyList<E>(new AppendAllListMaterializer<E>(materializer, elementsMaterializer));
  }

  @Override
  public <F> List<F> apply(final @NotNull Function<? super List<E>, Iterable<F>> function) {
    Require.notNull(function, "function");
    return new LazyList<F>(new SuppliedListMaterializer<F>() {
      @Override
      public ListMaterializer<F> get() throws Exception {
        return getElementsMaterializer(Require.notNull(function.apply(LazyList.this), "elements"));
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public <F> List<F> cast() {
    return (List<F>) this;
  }

  @Override
  public List<E> clone() {
    final ListMaterializer<E> materializer = this.materializer;
    int size = materializer.knownSize();
    if (size < 0) {
      size = materializer.materializeSize();
    }
    if (size == 0) {
      return emptyList();
    }
    if (size == 1) {
      return elementList(get(0));
    }
    return new LazyList<Object>(new ArrayToListMaterializer<Object>(toArray())).cast();
  }

  @Override
  public List<E> clone(final @NotNull Function<? super E, ? extends E> cloner) {
    final ListMaterializer<E> materializer = this.materializer;
    int size = materializer.knownSize();
    if (size < 0) {
      size = materializer.materializeSize();
    }
    if (size == 0) {
      return emptyList();
    }
    final DequeArrayList<E> elements = new DequeArrayList<E>(size);
    try {
      final java.util.Iterator<E> iterator = materializer.materializeForwardIterator(0);
      while (iterator.hasNext()) {
        elements.add(cloner.apply(iterator.next()));
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return new LazyList<E>(new ListToListMaterializer<E>(elements));
  }

  @Override
  public boolean contains(final Object o) {
    return materializer.materializeContains(o);
  }

  @Override
  public List<Integer> count() {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    if (knownSize == 0) {
      return ZERO_LIST;
    }
    if (knownSize > 0) {
      return elementList(knownSize);
    }
    return new LazyList<Integer>(new CountListMaterializer<E>(materializer));
  }

  @Override
  public List<E> diff(final @NotNull Iterable<?> elements) {
    final ListMaterializer<E> materializer = this.materializer;
    if (materializer.knownSize() == 0) {
      return emptyList();
    }
    if (getKnownSize(elements) == 0) {
      return this;
    }
    return new LazyList<E>(new DiffListMaterializer<E>(materializer,
        getElementsMaterializer(Require.notNull(elements, "elements"))));
  }

  @Override
  public List<E> distinct() {
    return distinctBy(indexedIdentity());
  }

  @Override
  public <K> List<E> distinctBy(final @NotNull Function<? super E, K> keyExtractor) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    if (knownSize == 0) {
      return emptyList();
    }
    if (knownSize == 1) {
      return this;
    }
    return new LazyList<E>(new DistinctByListMaterializer<E, K>(materializer,
        toIndexedFunction(keyExtractor, "keyExtractor")));
  }

  @Override
  public <K> List<E> distinctBy(final @NotNull IndexedFunction<? super E, K> keyExtractor) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    if (knownSize == 0) {
      return emptyList();
    }
    if (knownSize == 1) {
      return this;
    }
    return new LazyList<E>(new DistinctByListMaterializer<E, K>(materializer,
        Require.notNull(keyExtractor, "keyExtractor")));
  }

  @Override
  public void doFor(final @NotNull Consumer<? super E> elementConsumer) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    if (knownSize == 0) {
      return;
    }
    try {
      if (knownSize > 0) {
        for (int i = 0; i < knownSize; ++i) {
          elementConsumer.accept(materializer.materializeElement(i));
        }
      } else {
        int i = 0;
        while (materializer.canMaterializeElement(i)) {
          elementConsumer.accept(materializer.materializeElement(i));
          ++i;
        }
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doFor(final @NotNull Consumer<? super E> elementConsumer,
      final @NotNull Action endAction) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    try {
      if (knownSize == 0) {
        endAction.run();
        return;
      }
      if (knownSize > 0) {
        for (int i = 0; i < knownSize; ++i) {
          elementConsumer.accept(materializer.materializeElement(i));
        }
      } else {
        int i = 0;
        while (materializer.canMaterializeElement(i)) {
          elementConsumer.accept(materializer.materializeElement(i));
          ++i;
        }
      }
      endAction.run();
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doFor(final @NotNull Consumer<? super E> elementConsumer,
      final @NotNull Action endAction, final @NotNull Consumer<? super Throwable> errorConsumer) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    try {
      if (knownSize == 0) {
        endAction.run();
        return;
      }
      if (knownSize > 0) {
        for (int i = 0; i < knownSize; ++i) {
          elementConsumer.accept(materializer.materializeElement(i));
        }
      } else {
        int i = 0;
        while (materializer.canMaterializeElement(i)) {
          elementConsumer.accept(materializer.materializeElement(i));
          ++i;
        }
      }
      endAction.run();
    } catch (final Exception e) {
      try {
        errorConsumer.accept(e);
      } catch (final Exception ex) {
        throw UncheckedException.throwUnchecked(ex);
      }
    }
  }

  @Override
  public void doFor(final @NotNull IndexedConsumer<? super E> elementConsumer) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    if (knownSize == 0) {
      return;
    }
    try {
      if (knownSize > 0) {
        for (int i = 0; i < knownSize; ++i) {
          elementConsumer.accept(i, materializer.materializeElement(i));
        }
      } else {
        int i = 0;
        while (materializer.canMaterializeElement(i)) {
          elementConsumer.accept(i, materializer.materializeElement(i));
          ++i;
        }
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doFor(final @NotNull IndexedConsumer<? super E> elementConsumer,
      final @NotNull Consumer<? super Integer> endConsumer) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    try {
      if (knownSize == 0) {
        endConsumer.accept(0);
        return;
      }
      int i = 0;
      if (knownSize > 0) {
        while (i < knownSize) {
          final int index = i++;
          elementConsumer.accept(index, materializer.materializeElement(index));
        }
      } else {
        while (materializer.canMaterializeElement(i)) {
          final int index = i++;
          elementConsumer.accept(index, materializer.materializeElement(index));
        }
      }
      endConsumer.accept(i);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doFor(final @NotNull IndexedConsumer<? super E> elementConsumer,
      final @NotNull Consumer<? super Integer> endConsumer,
      final @NotNull IndexedConsumer<? super Throwable> errorConsumer) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    int i = 0;
    try {
      if (knownSize == 0) {
        endConsumer.accept(0);
        return;
      }
      if (knownSize > 0) {
        while (i < knownSize) {
          final int index = i++;
          elementConsumer.accept(index, materializer.materializeElement(index));
        }
      } else {
        while (materializer.canMaterializeElement(i)) {
          final int index = i++;
          elementConsumer.accept(index, materializer.materializeElement(index));
        }
      }
      endConsumer.accept(i);
    } catch (final Exception e) {
      try {
        errorConsumer.accept(i, e);
      } catch (final Exception ex) {
        throw UncheckedException.throwUnchecked(ex);
      }
    }
  }

  @Override
  public void doWhile(final @NotNull IndexedPredicate<? super E> elementPredicate) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    if (knownSize == 0) {
      return;
    }
    try {
      if (knownSize > 0) {
        for (int i = 0; i < knownSize; ++i) {
          if (!elementPredicate.test(i, materializer.materializeElement(i))) {
            break;
          }
        }
      } else {
        int i = 0;
        while (materializer.canMaterializeElement(i)) {
          if (!elementPredicate.test(i, materializer.materializeElement(i))) {
            break;
          }
          ++i;
        }
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doWhile(final @NotNull IndexedPredicate<? super E> elementPredicate,
      final @NotNull Consumer<? super Integer> endConsumer) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    try {
      if (knownSize == 0) {
        endConsumer.accept(0);
        return;
      }
      int i = 0;
      if (knownSize > 0) {
        while (i < knownSize) {
          final int index = i++;
          if (!elementPredicate.test(index, materializer.materializeElement(index))) {
            return;
          }
        }
      } else {
        while (materializer.canMaterializeElement(i)) {
          if (!elementPredicate.test(i, materializer.materializeElement(i))) {
            return;
          }
          ++i;
        }
      }
      endConsumer.accept(i);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doWhile(final @NotNull IndexedPredicate<? super E> elementPredicate,
      final @NotNull Consumer<? super Integer> endConsumer,
      final @NotNull IndexedConsumer<? super Throwable> errorConsumer) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    int i = 0;
    try {
      if (knownSize == 0) {
        endConsumer.accept(0);
        return;
      }
      if (knownSize > 0) {
        while (i < knownSize) {
          final int index = i++;
          if (!elementPredicate.test(index, materializer.materializeElement(index))) {
            return;
          }
        }
      } else {
        while (materializer.canMaterializeElement(i)) {
          if (!elementPredicate.test(i, materializer.materializeElement(i))) {
            return;
          }
          ++i;
        }
      }
      endConsumer.accept(i);
    } catch (final Exception e) {
      try {
        errorConsumer.accept(i, e);
      } catch (final Exception ex) {
        throw UncheckedException.throwUnchecked(ex);
      }
    }
  }

  @Override
  public void doWhile(final @NotNull Predicate<? super E> elementPredicate) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    if (knownSize == 0) {
      return;
    }
    try {
      if (knownSize > 0) {
        for (int i = 0; i < knownSize; ++i) {
          if (!elementPredicate.test(materializer.materializeElement(i))) {
            break;
          }
        }
      } else {
        int i = 0;
        while (materializer.canMaterializeElement(i)) {
          if (!elementPredicate.test(materializer.materializeElement(i))) {
            break;
          }
          ++i;
        }
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doWhile(final @NotNull Predicate<? super E> elementPredicate,
      final @NotNull Action endAction) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    try {
      if (knownSize == 0) {
        endAction.run();
        return;
      }
      int i = 0;
      if (knownSize > 0) {
        while (i < knownSize) {
          if (!elementPredicate.test(materializer.materializeElement(i++))) {
            return;
          }
        }
      } else {
        while (materializer.canMaterializeElement(i)) {
          if (!elementPredicate.test(materializer.materializeElement(i++))) {
            return;
          }
        }
      }
      endAction.run();
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void doWhile(final @NotNull Predicate<? super E> elementPredicate,
      final @NotNull Action endAction, final @NotNull Consumer<? super Throwable> errorConsumer) {
    final ListMaterializer<E> materializer = this.materializer;
    final int knownSize = materializer.knownSize();
    try {
      if (knownSize == 0) {
        endAction.run();
        return;
      }
      int i = 0;
      if (knownSize > 0) {
        while (i < knownSize) {
          if (!elementPredicate.test(materializer.materializeElement(i++))) {
            return;
          }
        }
      } else {
        while (materializer.canMaterializeElement(i)) {
          if (!elementPredicate.test(materializer.materializeElement(i++))) {
            return;
          }
        }
      }
      endAction.run();
    } catch (final Exception e) {
      try {
        errorConsumer.accept(e);
      } catch (final Exception ex) {
        throw UncheckedException.throwUnchecked(ex);
      }
    }
  }

  @Override
  public List<E> dropFirst(int maxElements) {
    return null;
  }

  @Override
  public List<E> dropFirstWhile(IndexedPredicate<? super E> condition) {
    return null;
  }

  @Override
  public List<E> dropFirstWhile(Predicate<? super E> condition) {
    return null;
  }

  @Override
  public List<E> dropLast(int maxElements) {
    return null;
  }

  @Override
  public List<E> dropLastWhile(IndexedPredicate<? super E> condition) {
    return null;
  }

  @Override
  public List<E> dropLastWhile(Predicate<? super E> condition) {
    return null;
  }

  @Override
  public List<Boolean> endsWith(Iterable<?> elements) {
    return null;
  }

  @Override
  public List<Boolean> exists(boolean whenEmpty, IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> exists(boolean whenEmpty, Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> existsBackward(boolean whenEmpty, IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> existsBackward(boolean whenEmpty, Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> existsForward(boolean whenEmpty, IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> existsForward(boolean whenEmpty, Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> filter(IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> filter(Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> filterWhile(IndexedPredicate<? super E> condition,
      IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> filterWhile(Predicate<? super E> condition, Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> find(IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> find(Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> findFirst(IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> findFirst(Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Integer> findFirstIndex(IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Integer> findFirstIndex(Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Integer> findFirstIndexOf(Object element) {
    return null;
  }

  @Override
  public List<Integer> findFirstIndexOfSequence(Iterable<?> elements) {
    return null;
  }

  @Override
  public List<Integer> findIndex(IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Integer> findIndex(Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Integer> findIndexOf(Object element) {
    return null;
  }

  @Override
  public List<E> findLast(IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> findLast(Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Integer> findLastIndex(IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Integer> findLastIndex(Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Integer> findLastIndexOf(Object element) {
    return null;
  }

  @Override
  public List<Integer> findLastIndexOfSequence(Iterable<?> elements) {
    return null;
  }

  @Override
  public E first() {
    return null;
  }

  @Override
  public <F> List<F> flatMap(Function<? super E, ? extends Iterable<F>> mapper) {
    return null;
  }

  @Override
  public <F> List<F> flatMap(IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
    return null;
  }

  @Override
  public List<E> flatMapWhile(IndexedPredicate<? super E> condition,
      IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public List<E> flatMapWhile(Predicate<? super E> condition,
      Function<? super E, ? extends Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public <F> List<F> fold(F identity, BinaryFunction<? super F, ? super E, ? extends F> operation) {
    return null;
  }

  @Override
  public <F> List<F> foldBackward(F identity,
      BinaryFunction<? super F, ? super E, ? extends F> operation) {
    return null;
  }

  @Override
  public <F> List<F> foldForward(F identity,
      BinaryFunction<? super F, ? super E, ? extends F> operation) {
    return null;
  }

  @Override
  public <F> List<F> foldWhile(F identity, Predicate<? super F> condition,
      BinaryFunction<? super F, ? super E, ? extends F> operation) {
    return null;
  }

  @Override
  public <F> List<F> foldWhileBackward(F identity, Predicate<? super F> condition,
      BinaryFunction<? super F, ? super E, ? extends F> operation) {
    return null;
  }

  @Override
  public <F> List<F> foldWhileForward(F identity, Predicate<? super F> condition,
      BinaryFunction<? super F, ? super E, ? extends F> operation) {
    return null;
  }

  @Override
  public E get(int index) {
    return null;
  }

  @Override
  public List<Boolean> includes(Object element) {
    return null;
  }

  @Override
  public List<Boolean> includesAll(Iterable<?> elements) {
    return null;
  }

  @Override
  public List<Boolean> includesSequence(Iterable<?> elements) {
    return null;
  }

  @Override
  public List<E> insertAfter(int numElements, E element) {
    return null;
  }

  @Override
  public List<E> insertAllAfter(int numElements, Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public List<E> interleave(Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public List<E> interleaveInner(Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public List<E> interleaveInnerWithPadding(Iterable<? extends E> elements, E paddingLeft,
      E paddingRight) {
    return null;
  }

  @Override
  public List<E> interleaveWithPadding(Iterable<? extends E> elements, E paddingLeft,
      E paddingRight) {
    return null;
  }

  @Override
  public List<E> intersect(Iterable<?> elements) {
    return null;
  }

  @Override
  public boolean isDefinite() {
    return false;
  }

  @Override
  public boolean isLazy() {
    return false;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public boolean isOrdered() {
    return false;
  }

  @Override
  public boolean isSorted() {
    return false;
  }

  @Override
  public boolean isTraversableAgain() {
    return false;
  }

  @Override
  public Iterator<E> iterator() {
    return null;
  }

  @Override
  public E last() {
    return null;
  }

  @Override
  public ListIterator<E> listIterator() {
    return null;
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return null;
  }

  @Override
  public <F> List<F> map(Function<? super E, F> mapper) {
    return null;
  }

  @Override
  public <F> List<F> map(IndexedFunction<? super E, F> mapper) {
    return null;
  }

  @Override
  public <F> List<F> mapBackward(Function<? super E, F> mapper) {
    return null;
  }

  @Override
  public <F> List<F> mapBackward(IndexedFunction<? super E, F> mapper) {
    return null;
  }

  @Override
  public <F> List<F> mapForward(Function<? super E, F> mapper) {
    return null;
  }

  @Override
  public <F> List<F> mapForward(IndexedFunction<? super E, F> mapper) {
    return null;
  }

  @Override
  public List<E> mapWhile(IndexedPredicate<? super E> condition,
      IndexedFunction<? super E, ? extends E> mapper) {
    return null;
  }

  @Override
  public List<E> mapWhile(Predicate<? super E> condition, Function<? super E, ? extends E> mapper) {
    return null;
  }

  @Override
  public <F> List<F> mapWhileBackward(IndexedPredicate<? super E> condition,
      IndexedFunction<? super E, F> mapper) {
    return null;
  }

  @Override
  public <F> List<F> mapWhileBackward(Predicate<? super E> condition,
      Function<? super E, F> mapper) {
    return null;
  }

  @Override
  public List<E> mapWhileForward(IndexedPredicate<? super E> condition,
      IndexedFunction<? super E, ? extends E> mapper) {
    return null;
  }

  @Override
  public List<E> mapWhileForward(Predicate<? super E> condition,
      Function<? super E, ? extends E> mapper) {
    return null;
  }

  @Override
  public List<E> materialize() {
    return null;
  }

  @Override
  public List<E> max(Comparator<? super E> comparator) {
    return null;
  }

  @Override
  public List<E> min(Comparator<? super E> comparator) {
    return null;
  }

  @Override
  public List<E> minus(E element) {
    return null;
  }

  @Override
  public List<E> minusAll(Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public List<E> minusFirst(E element) {
    return null;
  }

  @Override
  public List<E> minusLast(E element) {
    return null;
  }

  @Override
  public List<Boolean> notExists(boolean whenEmpty, IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> notExists(boolean whenEmpty, Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> notExistsBackward(boolean whenEmpty, IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> notExistsBackward(boolean whenEmpty, Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> notExistsForward(boolean whenEmpty, IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<Boolean> notExistsForward(boolean whenEmpty, Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> orElse(Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public List<E> orElseGet(Supplier<? extends Iterable<? extends E>> supplier) {
    return null;
  }

  @Override
  public Iterator<List<E>> partition(int numPartitions,
      Function<? super E, Integer> indexExtractor) {
    return null;
  }

  @Override
  public Iterator<List<E>> partition(int numPartitions,
      IndexedFunction<? super E, Integer> indexExtractor) {
    return null;
  }

  @Override
  public <K> Iterator<ZipEntry<K, List<E>>> partitionZip(Function<? super E, K> keyExtractor) {
    return null;
  }

  @Override
  public <K> Iterator<ZipEntry<K, List<E>>> partitionZip(
      IndexedFunction<? super E, K> keyExtractor) {
    return null;
  }

  @Override
  public List<E> plus(E element) {
    return null;
  }

  @Override
  public List<E> plusAll(Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public List<E> prepend(E element) {
    return null;
  }

  @Override
  public List<E> prependAll(Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public List<E> reduce(BinaryFunction<? super E, ? super E, ? extends E> operation) {
    return null;
  }

  @Override
  public List<E> reduceBackward(BinaryFunction<? super E, ? super E, ? extends E> operation) {
    return null;
  }

  @Override
  public List<E> reduceForward(BinaryFunction<? super E, ? super E, ? extends E> operation) {
    return null;
  }

  @Override
  public List<E> reduceWhile(Predicate<? super E> condition,
      BinaryFunction<? super E, ? super E, ? extends E> operation) {
    return null;
  }

  @Override
  public List<E> reduceWhileBackward(Predicate<? super E> condition,
      BinaryFunction<? super E, ? super E, ? extends E> operation) {
    return null;
  }

  @Override
  public List<E> reduceWhileForward(Predicate<? super E> condition,
      BinaryFunction<? super E, ? super E, ? extends E> operation) {
    return null;
  }

  @Override
  public List<E> removeFirst(IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> removeFirst(Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> removeFirstSequence(Iterable<?> elements) {
    return null;
  }

  @Override
  public List<E> removeLast(IndexedPredicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> removeLast(Predicate<? super E> predicate) {
    return null;
  }

  @Override
  public List<E> removeLastSequence(Iterable<?> elements) {
    return null;
  }

  @Override
  public List<E> removeSequence(Iterable<?> elements) {
    return null;
  }

  @Override
  public List<E> removeSlice(int start) {
    return null;
  }

  @Override
  public List<E> removeSlice(int start, int end) {
    return null;
  }

  @Override
  public List<E> replaceFirstSequence(Iterable<?> elements,
      Function<? super java.util.List<E>, Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public List<E> replaceLastSequence(Iterable<?> elements,
      Function<? super java.util.List<E>, Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public List<E> replaceSequence(Iterable<?> elements,
      Function<? super java.util.List<E>, Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public List<E> replaceSequence(Iterable<?> elements,
      IndexedFunction<? super java.util.List<E>, Iterable<? extends E>> mapper) {
    return null;
  }

  @Override
  public List<E> replaceSlice(int start, int end, Iterable<? extends E> patch) {
    return null;
  }

  @Override
  public List<E> replaceSlice(int start, Iterable<? extends E> patch) {
    return null;
  }

  @Override
  public List<E> resizeTo(int numElements, E padding) {
    return null;
  }

  @Override
  public List<E> reverse() {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public List<E> slice(int start) {
    return null;
  }

  @Override
  public List<E> slice(int start, int end) {
    return null;
  }

  @Override
  public List<List<E>> slidingWindow(int maxSize, int step) {
    return null;
  }

  @Override
  public List<List<E>> slidingWindowWithPadding(int size, int step, E padding) {
    return null;
  }

  @Override
  public List<E> sorted(Comparator<? super E> comparator) {
    return null;
  }

  @Override
  public List<Boolean> startsWith(Iterable<?> elements) {
    return null;
  }

  @Override
  public List<E> symmetricDiff(Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public List<E> takeFirst(int maxElements) {
    return null;
  }

  @Override
  public List<E> takeFirstWhile(IndexedPredicate<? super E> condition) {
    return null;
  }

  @Override
  public List<E> takeFirstWhile(Predicate<? super E> condition) {
    return null;
  }

  @Override
  public List<E> takeLast(int maxElements) {
    return null;
  }

  @Override
  public itf.Iterator<E, ? extends itf.Iterator<E, ?>> toIterator() {
    return null;
  }

  @Override
  public List<E> takeLastWhile(IndexedPredicate<? super E> condition) {
    return null;
  }

  @Override
  public List<E> takeLastWhile(Predicate<? super E> condition) {
    return null;
  }

  @Override
  public List<E> union(Iterable<? extends E> elements) {
    return null;
  }

  @Override
  public <F> List<ZipEntry<E, F>> zip(Iterable<F> elements) {
    return null;
  }

  @Override
  public <F> List<ZipEntry<E, F>> zipWithPadding(Iterable<F> elements, E paddingLeft,
      F paddingRight) {
    return null;
  }

  int knownSize() {
    return materializer.knownSize();
  }
}
