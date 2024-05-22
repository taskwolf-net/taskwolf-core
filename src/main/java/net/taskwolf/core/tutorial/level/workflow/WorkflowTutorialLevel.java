package net.taskwolf.core.tutorial.level.workflow;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.tutorial.level.TutorialLevel;
import net.taskwolf.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class WorkflowTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/workflow/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.workflow.step.1.title",
      "tutorial.workflow.step.1.description"));
    steps.add(TutorialStep.create("tutorial.workflow.step.2.title",
      "tutorial.workflow.step.2.description", ".workflow-trigger"));
    steps.add(TutorialStep.create("tutorial.workflow.step.3.title",
      "tutorial.workflow.step.3.description", ".workflow-action"));
    steps.add(TutorialStep.create("tutorial.workflow.step.4.title",
      "tutorial.workflow.step.4.description"));
    return steps;
  }
}