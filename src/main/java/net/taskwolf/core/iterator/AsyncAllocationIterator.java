package net.taskwolf.core.iterator;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class AsyncAllocationIterator<T, U> extends TaskwolfIterator<T, Map<T, U>> {
  public static <T, U> CompletableFuture<Map<T, U>> execute(
    List<T> list, Function<T, CompletableFuture<U>> transformation
  ) {
    var iterator = AsyncAllocationIterator.<T, U>create(list, transformation);
    return iterator.execute();
  }

  public static <T, U> AsyncAllocationIterator<T, U> create(
    List<T> list, Function<T, CompletableFuture<U>> transformation
  ) {
    return new AsyncAllocationIterator<T, U>(list, transformation);
  }

  private final Function<T, CompletableFuture<U>> transformation;
  private final Map<T, U> result = Maps.newHashMap();

  private AsyncAllocationIterator(
    List<T> list, Function<T, CompletableFuture<U>> transformation
  ) {
    super(list);
    this.transformation = transformation;
  }

  @Override
  protected CompletableFuture<?> entryFuture(T entry) {
    return transformation.apply(entry).thenAccept(value -> result.put(entry, value));
  }

  @Override
  protected Map<T, U> result() {
    return result;
  }
}