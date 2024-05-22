package net.taskwolf.core.tutorial.level.process;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.tutorial.level.TutorialLevel;
import net.taskwolf.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class ProcessesTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/processes/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.processes.step.1.title",
      "tutorial.processes.step.1.description", true));
    steps.add(TutorialStep.create("tutorial.processes.step.2.title",
      "tutorial.processes.step.2.description", true));
    steps.add(TutorialStep.create("tutorial.processes.step.3.title",
      "tutorial.processes.step.3.description", "#process-create", true));
    return steps;
  }
}
