package net.taskwolf.core.tutorial.level.dashboard;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.tutorial.level.TutorialLevel;
import net.taskwolf.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class DashboardTutorialLevel implements TutorialLevel {
  @Override
  public int id() {
    return 0;
  }

  @Override
  public String page() {
    return "/dashboard/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.dashboard.step.1.title",
      "tutorial.dashboard.step.1.description", ""));
    steps.add(TutorialStep.create("tutorial.dashboard.step.2.title",
      "tutorial.dashboard.step.2.description", ""));
    return steps;
  }
}
