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
package sparx.util;

import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;

public class UncheckedException extends RuntimeException {

  public static @NotNull RuntimeException throwUnchecked(final InterruptedException exception) {
    throw toUnchecked(exception);
  }

  public static @NotNull RuntimeException throwUnchecked(final TimeoutException exception) {
    throw toUnchecked(exception);
  }

  public static @NotNull RuntimeException throwUnchecked(final Throwable error) {
    if (error instanceof InterruptedException) {
      throw toUnchecked((InterruptedException) error);
    } else if (error instanceof TimeoutException) {
      throw toUnchecked((TimeoutException) error);
    } else if (error instanceof RuntimeException) {
      throw (RuntimeException) error;
    } else if (error instanceof Error) {
      throw (Error) error;
    }
    throw new UncheckedException(error);
  }

  public static @NotNull UncheckedTimeoutException timeout() {
    return new UncheckedTimeoutException();
  }

  public static @NotNull UncheckedInterruptedException toUnchecked(
      final InterruptedException exception) {
    return new UncheckedInterruptedException(exception);
  }

  public static @NotNull UncheckedTimeoutException toUnchecked(final TimeoutException exception) {
    return new UncheckedTimeoutException(exception);
  }

  public static @NotNull UncheckedException toUnchecked(final Throwable error) {
    if (error instanceof InterruptedException) {
      return toUnchecked((InterruptedException) error);
    } else if (error instanceof TimeoutException) {
      return toUnchecked((TimeoutException) error);
    } else if (error instanceof UncheckedException) {
      return (UncheckedException) error;
    }
    return new UncheckedException(error);
  }

  private UncheckedException() {
  }

  private UncheckedException(final Throwable cause) {
    super(cause);
  }

  public static class UncheckedInterruptedException extends UncheckedException {

    private UncheckedInterruptedException(final InterruptedException cause) {
      super(cause);
      Thread.currentThread().interrupt();  //set the flag back to true
    }
  }

  public static class UncheckedTimeoutException extends UncheckedException {

    private UncheckedTimeoutException() {
    }

    private UncheckedTimeoutException(final TimeoutException cause) {
      super(cause);
    }
  }
}
