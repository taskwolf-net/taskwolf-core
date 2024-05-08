package net.taskwolf.core.iterator;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class AsyncListIterator<T, U> extends TaskwolfIterator<T, List<U>> {
  public static <T, U> CompletableFuture<List<U>> execute(
    List<T> list, Function<T, CompletableFuture<List<U>>> transformation
  ) {
    var iterator = AsyncListIterator.<T, U>create(list, transformation);
    return iterator.execute();
  }

  public static <T, U> AsyncListIterator<T, U> create(
    List<T> list, Function<T, CompletableFuture<List<U>>> transformation
  ) {
    return new AsyncListIterator<T, U>(list, transformation);
  }

  private final Function<T, CompletableFuture<List<U>>> transformation;
  private final List<U> result = Lists.newArrayList();

  private AsyncListIterator(
    List<T> list, Function<T, CompletableFuture<List<U>>> transformation
  ) {
    super(list);
    this.transformation = transformation;
  }

  @Override
  protected CompletableFuture<?> entryFuture(T entry) {
    return transformation.apply(entry).thenAccept(result::addAll);
  }

  @Override
  protected List<U> result() {
    return result;
  }
}
