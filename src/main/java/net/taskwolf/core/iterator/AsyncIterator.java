package net.taskwolf.core.iterator;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class AsyncIterator<T, U> extends TaskwolfIterator<T, List<U>> {
  public static <T, U> CompletableFuture<List<U>> execute(
    List<T> list, Function<T, CompletableFuture<U>> transformation
  ) {
    var iterator = AsyncIterator.<T, U>create(list, transformation);
    return iterator.execute();
  }

  public static <T, U> AsyncIterator<T, U> create(
    List<T> list, Function<T, CompletableFuture<U>> transformation
  ) {
    return new AsyncIterator<T, U>(list, transformation);
  }

  private final Function<T, CompletableFuture<U>> transformation;
  private final List<U> result = Lists.newArrayList();

  private AsyncIterator(
    List<T> list, Function<T, CompletableFuture<U>> transformation
  ) {
    super(list);
    this.transformation = transformation;
  }

  @Override
  protected CompletableFuture<?> entryFuture(T entry) {
    return transformation.apply(entry).thenAccept(result::add);
  }

  @Override
  protected List<U> result() {
    return result;
  }
}