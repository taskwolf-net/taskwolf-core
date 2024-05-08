package net.taskwolf.core.iterator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TaskwolfIterator<T, U> {
private final List<T> list;
  private int counter = 0;

  public CompletableFuture<U> execute() {
    if (list.isEmpty()) {
      return CompletableFuture.completedFuture(result());
    }
    var futureResponse = new CompletableFuture<U>();
    for (var entry : list) {
      entryFuture(entry).thenAccept(value -> counter++)
        .thenAccept(value -> checkCompletion(futureResponse));
    }
    return futureResponse;
  }

  private void checkCompletion(CompletableFuture<U> futureResponse) {
    if (counter == list.size()) {
      futureResponse.complete(result());
    }
  }

  protected abstract CompletableFuture<?> entryFuture(T entry);

  protected abstract U result();
}
