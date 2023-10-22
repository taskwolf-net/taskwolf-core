package net.taskwolf.core.iterator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TaskwolfIterator<T, U> {
private final List<T> list;
  @Getter(AccessLevel.PROTECTED)
  private final int number;
  private int counter = 0;

  public void execute() {
    if (list.isEmpty()) {
      complete();
      return;
    }
    for (var entry : list) {
      entryFuture(entry).thenAccept(value -> counter++)
        .thenAccept(value -> checkCompletion());
    }
  }

  private void checkCompletion() {
    if (counter == number) {
      complete();
    }
  }

  protected abstract CompletableFuture<?> entryFuture(T entry);

  protected abstract void complete();
}
