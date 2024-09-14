package com.dulno.core.tutorial.level.account;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import com.dulno.core.tutorial.level.TutorialLevel;
import com.dulno.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class AccountsTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/account/apps/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.accounts.step.1.title",
      "tutorial.accounts.step.1.description", true));
    steps.add(TutorialStep.create("tutorial.accounts.step.2.title",
      "tutorial.accounts.step.2.description", true));
    return steps;
  }
}
