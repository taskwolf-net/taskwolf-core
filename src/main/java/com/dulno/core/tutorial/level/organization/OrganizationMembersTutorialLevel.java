package com.dulno.core.tutorial.level.organization;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import com.dulno.core.tutorial.level.TutorialLevel;
import com.dulno.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class OrganizationMembersTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/organization/members/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.organization.members.step.1.title",
      "tutorial.organization.members.step.1.description", true));
    steps.add(TutorialStep.create("tutorial.organization.members.step.2.title",
      "tutorial.organization.members.step.2.description", true));
    steps.add(TutorialStep.create("tutorial.organization.members.step.3.title",
      "tutorial.organization.members.step.3.description", true));
    return steps;
  }
}

