package net.taskwolf.core.iterator;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class AsyncListIterator<T, U> extends TaskwolfIterator<T, U> {
  public static <T, U> AsyncListIterator<T, U> execute(
    List<T> list, Function<T, CompletableFuture<List<U>>> transformation,
    int number, Consumer<List<U>> completion
  ) {
    var iterator = AsyncListIterator.<T, U>create(list, transformation, number,
      completion);
    iterator.execute();
    return iterator;
  }

  public static <T, U> AsyncListIterator<T, U> create(
    List<T> list, Function<T, CompletableFuture<List<U>>> transformation,
    int number, Consumer<List<U>> completion
  ) {
    return new AsyncListIterator<T, U>(list, transformation, number, completion);
  }

  private final Function<T, CompletableFuture<List<U>>> transformation;
  private final Consumer<List<U>> completion;
  private final List<U> result = Lists.newArrayList();

  private AsyncListIterator(
    List<T> list, Function<T, CompletableFuture<List<U>>> transformation,
    int number, Consumer<List<U>> completion
  ) {
    super(list, number);
    this.transformation = transformation;
    this.completion = completion;
  }

  @Override
  protected CompletableFuture<?> entryFuture(T entry) {
    return transformation.apply(entry).thenAccept(result::addAll);
  }

  @Override
  protected void complete() {
    completion.accept(result);
  }
}
