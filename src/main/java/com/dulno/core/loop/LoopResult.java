package com.dulno.core.loop;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class LoopResult {
  public static LoopResult success() {
    return new LoopResult(Status.SUCCESS);
  }

  public static LoopResult failure(String failureMessage) {
    return new LoopResult(Status.FAILURE, failureMessage);
  }

  enum Status {
    SUCCESS,
    FAILURE;
  }

  private final Status status;
  @Getter
  private String failureMessage;

  private LoopResult(Status status) {
    this.status = status;
    this.failureMessage = "";
  }

  private LoopResult(Status status, String failureMessage) {
    this.status = status;
    this.failureMessage = failureMessage;
  }

  public boolean isSuccess() {
    return status == Status.SUCCESS;
  }

  public boolean isFailure() {
    return status == Status.FAILURE;
  }
}
