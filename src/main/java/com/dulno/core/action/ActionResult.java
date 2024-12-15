package com.dulno.core.action;

import com.dulno.core.workflow.step.WorkflowStepResult;
import com.dulno.core.workflow.step.WorkflowStepStatus;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(fluent = true)
public final class ActionResult extends WorkflowStepResult {
  private ActionResult(
    WorkflowStepStatus status, boolean mayContinue,
    Map<String, Object> passOnInformation
  ) {
    super(status, mayContinue, passOnInformation);
  }

  private ActionResult(WorkflowStepStatus status, String failureMessage) {
    super(status, failureMessage);
  }
}
