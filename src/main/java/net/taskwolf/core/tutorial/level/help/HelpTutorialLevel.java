package net.taskwolf.core.tutorial.level.help;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.tutorial.level.TutorialLevel;
import net.taskwolf.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class HelpTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/help/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.help.step.1.title",
      "tutorial.help.step.1.description", true));
    steps.add(TutorialStep.create("tutorial.help.step.2.title",
      "tutorial.help.step.2.description", true));
    steps.add(TutorialStep.create("tutorial.help.step.3.title",
      "tutorial.help.step.3.description", true));
    return steps;
  }
}
