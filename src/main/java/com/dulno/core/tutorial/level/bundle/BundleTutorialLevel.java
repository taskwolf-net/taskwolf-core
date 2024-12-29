package com.dulno.core.tutorial.level.bundle;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import com.dulno.core.tutorial.level.TutorialLevel;
import com.dulno.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class BundleTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/package/general/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.bundle.step.1.title",
      "tutorial.bundle.step.1.description", true));
    steps.add(TutorialStep.create("tutorial.bundle.step.2.title",
      "tutorial.bundle.step.2.description", true));
    steps.add(TutorialStep.create("tutorial.bundle.step.3.title",
      "tutorial.bundle.step.3.description", true));
    return steps;
  }
}
