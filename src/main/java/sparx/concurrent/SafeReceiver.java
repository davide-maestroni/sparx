package sparx.concurrent;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.Scheduler.Task;
import sparx.logging.Log;

public class SafeReceiver<V> implements Receiver<V> {

  private final Scheduler scheduler = Scheduler.trampoline();

  private Receiver<V> status;

  @Override
  public final boolean fail(@NotNull final Exception error) {
    scheduler.scheduleAfter(new Task() {
      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        status.fail(error);
      }
    });
    return true;
  }

  @Override
  public final void set(final V value) {
    scheduler.scheduleAfter(new Task() {
      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        status.set(value);
      }
    });
  }

  @Override
  public final void setBulk(@NotNull final Collection<V> values) {
    scheduler.scheduleAfter(new Task() {
      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        status.setBulk(values);
      }
    });
  }

  @Override
  public final void close() {
    scheduler.scheduleAfter(new Task() {
      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        status.close();
      }
    });
  }

  protected void failSafe(@NotNull Exception error) {
    // TODO: log not implemented => TemplateReceiver
  }

  protected void setSafe(V value) {
  }

  protected void setBulkSafe(@NotNull Collection<V> values) {
  }

  protected void closeSafe() {
  }

  private class DoneStatus implements Receiver<V> {

    @Override
    public boolean fail(@NotNull final Exception error) {
      return false;
    }

    @Override
    public void set(final V value) {
    }

    @Override
    public void setBulk(@NotNull final Collection<V> values) {
    }

    @Override
    public void close() {
    }
  }

  private class RunningStatus implements Receiver<V> {

    @Override
    public boolean fail(@NotNull final Exception error) {
      status = new DoneStatus();
      failSafe(error);
      return true;
    }

    @Override
    public void set(final V value) {
      try {
        setSafe(value);
      } catch (final RuntimeException e) {
        Log.wrn(SafeReceiver.class, "Uncaught exception: %s", Log.printable(e));
        fail(e);
      }
    }

    @Override
    public void setBulk(@NotNull final Collection<V> values) {
      try {
        setBulkSafe(values);
      } catch (final RuntimeException e) {
        Log.wrn(SafeReceiver.class, "Uncaught exception: %s", Log.printable(e));
        fail(e);
      }
    }

    @Override
    public void close() {
      try {
        closeSafe();
        status = new DoneStatus();
      } catch (final RuntimeException e) {
        Log.wrn(SafeReceiver.class, "Uncaught exception: %s", Log.printable(e));
        fail(e);
      }
    }
  }
}
