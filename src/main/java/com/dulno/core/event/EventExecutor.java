package com.dulno.core.event;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class EventExecutor {
  private final HookRegistry registry;

  /**
   * Executes an event
   * @param event The event that is to be executed
   */
  public void execute(Event event) {
    try {
      event.call(registry);
    } catch (Exception exception) {
      exception.printStackTrace();
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
