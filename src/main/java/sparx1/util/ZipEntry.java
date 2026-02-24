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
package sparx1.util;

import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZipEntry<L, R> implements Entry<L, R> {

  private L left;
  private R right;

  private ZipEntry(@Nullable L left, @Nullable R right) {
    this.left = left;
    this.right = right;
  }

  @NotNull
  public static <L, R> ZipEntry<L, R> of(@NotNull final Entry<? extends L, ? extends R> entry) {
    return new ZipEntry<L, R>(entry.getKey(), entry.getValue());
  }

  @NotNull
  public static <L, R> ZipEntry<L, R> of(@Nullable L left, @Nullable R right) {
    return new ZipEntry<L, R>(left, right);
  }

  @Override
  public L getKey() {
    return getLeft();
  }

  public L getLeft() {
    return left;
  }

  public void setLeft(@Nullable L left) {
    this.left = left;
  }

  public R getRight() {
    return right;
  }

  public void setRight(@Nullable R right) {
    this.right = right;
  }

  @Override
  public R getValue() {
    return getRight();
  }

  @Override
  public R setValue(final R value) {
    final R currentValue = getRight();
    this.right = value;
    return currentValue;
  }
}
