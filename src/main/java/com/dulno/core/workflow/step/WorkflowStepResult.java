package com.dulno.core.workflow.step;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(fluent = true)
public final class WorkflowStepResult {
  public static WorkflowStepResult success() {
    return success(true);
  }

  public static WorkflowStepResult success(boolean mayContinue) {
    return new WorkflowStepResult(Status.SUCCESS, mayContinue, Maps.newHashMap());
  }

  public static WorkflowStepResult success(Map<String, Object> passOnInformation) {
    return new WorkflowStepResult(Status.SUCCESS, true, passOnInformation);
  }

  public static WorkflowStepResult failure(String failureMessage) {
    return new WorkflowStepResult(Status.FAILURE, failureMessage);
  }

  enum Status {
    SUCCESS,
    FAILURE;
  }

  private final Status status;
  @Getter
  private boolean mayContinue;
  private Map<String, Object> passOnInformation;
  @Getter
  private String failureMessage;

  private WorkflowStepResult(
    Status status, boolean mayContinue, Map<String, Object> passOnInformation
  ) {
    this.status = status;
    this.mayContinue = mayContinue;
    this.passOnInformation = passOnInformation;
    this.failureMessage = "";
  }

  private WorkflowStepResult(Status status, String failureMessage) {
    this.status = status;
    this.mayContinue = false;
    this.failureMessage = failureMessage;
  }

  public boolean isSuccess() {
    return status == Status.SUCCESS;
  }

  public boolean isFailure() {
    return status == Status.FAILURE;
  }

  public Map<String, Object> passOnInformation() {
    return Map.copyOf(passOnInformation);
  }
}
