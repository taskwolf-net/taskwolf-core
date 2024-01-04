package net.taskwolf.core.condition;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class ConditionResult {
  public static ConditionResult success(boolean comparisonResult) {
    return new ConditionResult(Status.SUCCESS, comparisonResult);
  }

  public static ConditionResult failure(String failureMessage) {
    return new ConditionResult(Status.FAILURE, failureMessage);
  }

  enum Status {
    SUCCESS,
    FAILURE;
  }

  private final Status status;
  @Getter
  private boolean comparisonResult;
  @Getter
  private String failureMessage;

  private ConditionResult(Status status, boolean comparisonResult) {
    this.status = status;
    this.comparisonResult = comparisonResult;
    this.failureMessage = "";
  }

  private ConditionResult(Status status, String failureMessage) {
    this.status = status;
    this.comparisonResult = false;
    this.failureMessage = failureMessage;
  }

  public boolean isSuccess() {
    return status == Status.SUCCESS;
  }

  public boolean isFailure() {
    return status == Status.FAILURE;
  }
}
