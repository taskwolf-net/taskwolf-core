package com.dulno.core.tutorial.level.process;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import com.dulno.core.tutorial.level.TutorialLevel;
import com.dulno.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class ProcessTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/process/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.process.step.1.title",
      "tutorial.process.step.1.description", "", "bottom-end", false));
    steps.add(TutorialStep.create("tutorial.process.step.2.title",
      "tutorial.process.step.2.description", "", "bottom-end", false));
    steps.add(TutorialStep.create("tutorial.process.step.3.title",
      "tutorial.process.step.2.description", "", "bottom-end", false));
    steps.add(TutorialStep.create("tutorial.process.step.4.title",
      "tutorial.process.step.2.description", "#process-run-wrapper",
      "bottom-end", false));
    steps.add(TutorialStep.create("tutorial.process.step.5.title",
      "tutorial.process.step.2.description", "#process-publish",
      "bottom-end", false));
    return steps;
  }
}
