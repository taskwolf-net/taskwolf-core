package net.taskwolf.core.iterator;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor(staticName = "create")
public final class AsyncIterator<T, U> {
  public static <T, U> AsyncIterator<T, U> execute(
    List<T> list, Function<T, CompletableFuture<U>> transformation, int number,
    Consumer<List<U>> completion
  ) {
    var iterator = AsyncIterator.<T, U>create(list, transformation, number, completion);
    iterator.execute();
    return iterator;
  }

  private final List<T> list;
  private final Function<T, CompletableFuture<U>> transformation;
  private final int number;
  private final Consumer<List<U>> completion;
  private final List<U> result = Lists.newArrayList();
  private int counter = 0;

  public void execute() {
    if (list.isEmpty()) {
      completion.accept(result);
      return;
    }
    for (var entry : list) {
      transformation.apply(entry)
        .thenAccept(result::add)
        .thenAccept(value -> counter++)
        .thenAccept(value -> checkCompletion());
    }
  }

  private void checkCompletion() {
    if (result.size() == number) {
      completion.accept(result);
    }
  }
}
