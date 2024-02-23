package net.taskwolf.core.action;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
public class ActionResult {
  public static CompletableFuture<ActionResult> futureSuccess(
    Map<String, Object> information
  ) {
    return CompletableFuture.completedFuture(success(information));
  }

  public static CompletableFuture<ActionResult> futureFailure(
    String failureMessage
  ) {
    return CompletableFuture.completedFuture(failure(failureMessage));
  }

  public static ActionResult success(Map<String, Object> information) {
    return new ActionResult(Status.SUCCESS, information);
  }

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
