package net.taskwolf.core.iterator;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class AsyncIterator<T, U> extends TaskwolfIterator<T, U> {
  public static <T, U> AsyncIterator<T, U> execute(
    List<T> list, Function<T, CompletableFuture<U>> transformation,
    int number, Consumer<List<U>> completion
  ) {
    var iterator = AsyncIterator.<T, U>create(list, transformation, number,
      completion);
    iterator.execute();
    return iterator;
  }

  public static <T, U> AsyncIterator<T, U> create(
    List<T> list, Function<T, CompletableFuture<U>> transformation,
    int number, Consumer<List<U>> completion
  ) {
    return new AsyncIterator<T, U>(list, transformation, number, completion);
  }

  private final Function<T, CompletableFuture<U>> transformation;
  private final Consumer<List<U>> completion;
  private final List<U> result = Lists.newArrayList();

  private AsyncIterator(
    List<T> list, Function<T, CompletableFuture<U>> transformation,
    int number, Consumer<List<U>> completion
  ) {
    super(list, number);
    this.transformation = transformation;
    this.completion = completion;
  }

  @Override
  protected CompletableFuture<?> entryFuture(T entry) {
    return transformation.apply(entry).thenAccept(result::add);
  }

  @Override
  protected void complete() {
    completion.accept(result);
  }
}