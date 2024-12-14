package com.dulno.core.action;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
public final class ActionResult {
  /**
   * Creates a new future success action result
   * @param information The information that is passed to the next component
   * @return The future result
   */
  public static CompletableFuture<ActionResult> futureSuccess(
    Map<String, Object> information
  ) {
    return CompletableFuture.completedFuture(success(information));
  }

  /**
   * Creates a new future failure action result
   * @param failureMessage The failure message that is displayed in the
   *                       workflow timeline (It is best to pass the locales key)
   * @return The future result
   */
  public static CompletableFuture<ActionResult> futureFailure(
    String failureMessage
  ) {
    return CompletableFuture.completedFuture(failure(failureMessage));
  }

  /**
   * Creates a new success action result
   * @param information The information that is passed to the next component
   * @return The result
   */
  public static ActionResult success(Map<String, Object> information) {
    return new ActionResult(Status.SUCCESS, information);
  }

  /**
   * Creates a new failre action result
   * @param failureMessage The failure message that is displayed in the
   *                      workflow timeline (It is best to pass the locales key)
   * @return The result
   */
  public static ActionResult failure(String failureMessage) {
    return new ActionResult(Status.FAILURE, failureMessage);
  }

  enum Status {
    SUCCESS,
    FAILURE;
  }

  private final Status status;
  @Getter
  private Map<String, Object> information;
  @Getter
  private String failureMessage;

  private ActionResult(Status status, Map<String, Object> information) {
    this.status = status;
    this.information = information;
    this.failureMessage = "";
  }

  private ActionResult(Status status, String failureMessage) {
    this.status = status;
    this.information = Maps.newHashMap();
    this.failureMessage = failureMessage;
  }

  public boolean isSuccess() {
    return status == Status.SUCCESS;
  }

  public boolean isFailure() {
    return status == Status.FAILURE;
  }
}
