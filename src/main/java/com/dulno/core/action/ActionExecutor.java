package com.dulno.core.action;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ActionExecutor {
  /**
   * Is used to actually execute an action
   * @param information External information that are fed into the
   *                    execution process
   * @return A future {@link ActionResult}
   */
  CompletableFuture<ActionResult> execute(Map<String, Object> information);
}
