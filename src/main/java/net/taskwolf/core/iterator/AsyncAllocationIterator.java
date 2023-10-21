package net.taskwolf.core.iterator;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class AsyncAllocationIterator<T, U> extends TaskwolfIterator<T, U> {
  public static <T, U> AsyncAllocationIterator<T, U> execute(
    List<T> list, Function<T, CompletableFuture<U>> transformation,
    int number, Consumer<Map<T, U>> completion
  ) {
    var iterator = AsyncAllocationIterator.<T, U>create(list, transformation, number,
      completion);
    iterator.execute();
    return iterator;
  }

  public static <T, U> AsyncAllocationIterator<T, U> create(
    List<T> list, Function<T, CompletableFuture<U>> transformation,
    int number, Consumer<Map<T, U>> completion
  ) {
    return new AsyncAllocationIterator<T, U>(list, transformation, number, completion);
  }

  private final Function<T, CompletableFuture<U>> transformation;;
  private final Consumer<Map<T, U>> completion;
  private final Map<T, U> result = Maps.newHashMap();

  private AsyncAllocationIterator(
    List<T> list, Function<T, CompletableFuture<U>> transformation,
    int number, Consumer<Map<T, U>> completion
  ) {
    super(list, number);
    this.transformation = transformation;
    this.completion = completion;
  }

  @Override
  protected CompletableFuture<?> entryFuture(T entry) {
    return transformation.apply(entry).thenAccept(value -> result.put(entry, value));
  }

  @Override
  protected void checkCompletion() {
    if (result.size() == number()) {
      complete();
    }
  }

  @Override
  protected void complete() {
    completion.accept(result);
  }
}