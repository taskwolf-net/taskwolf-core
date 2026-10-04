package net.taskwolf.core.tutorial.level.template;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.tutorial.level.TutorialLevel;
import net.taskwolf.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class TemplateTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/templates/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.templates.step.1.title",
      "tutorial.templates.step.1.description", true));
    steps.add(TutorialStep.create("tutorial.templates.step.2.title",
      "tutorial.templates.step.2.description", true));
    return steps;
  }
}
