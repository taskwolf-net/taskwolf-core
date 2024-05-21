package net.taskwolf.core.tutorial.level.dashboard;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.tutorial.level.TutorialLevel;
import net.taskwolf.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class WorkflowsTutorialLevel implements TutorialLevel {
  @Override
  public int id() {
    return 1;
  }

  @Override
  public String page() {
    return "/workflows/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.workflows.step.1.title",
      "tutorial.workflows.step.1.description", true));
    steps.add(TutorialStep.create("tutorial.workflows.step.2.title",
      "tutorial.workflows.step.2.description", true));
    steps.add(TutorialStep.create("tutorial.workflows.step.3.title",
      "tutorial.workflows.step.3.description", "#workflow-create", true));
    return steps;
  }
}