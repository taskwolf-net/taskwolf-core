package net.taskwolf.core.tutorial.level.organization;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.tutorial.level.TutorialLevel;
import net.taskwolf.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class OrganizationsTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/organizations/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.organizations.step.1.title",
      "tutorial.organizations.step.1.description", true));
    steps.add(TutorialStep.create("tutorial.organizations.step.2.title",
      "tutorial.organizations.step.2.description", true));
    steps.add(TutorialStep.create("tutorial.organizations.step.3.title",
      "tutorial.organizations.step.3.description", "#organizations-create", true));
    return steps;
  }
}

