package com.dulno.core.event;

import com.dulno.core.error.ErrorRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.annotation.Nullable;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class EventExecutor {
  private final HookRegistry registry;
  @Nullable
  private final ErrorRepository errorRepository;

  /**
   * Executes an event
   * @param event The event that is to be executed
   */
  @SneakyThrows
  public void execute(Event event) {
    try {
      event.call(registry);
    } catch (Exception exception) {
      errorRepository.processError(exception);
    }
  }

  /**
   * This function executes an event without catching any exception
   * @param event The event that is to be executed
   * @throws Exception
   */
  public void executeUncaught(Event event) throws Exception {
    event.call(registry);
  }
}
