package net.taskwolf.core.tutorial.level.webhook;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.tutorial.level.TutorialLevel;
import net.taskwolf.core.tutorial.level.TutorialStep;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class WebhooksTutorialLevel implements TutorialLevel {
  @Override
  public String page() {
    return "/webhooks/";
  }

  @Override
  public List<TutorialStep> steps() {
    var steps = Lists.<TutorialStep>newArrayList();
    steps.add(TutorialStep.create("tutorial.webhooks.step.1.title",
      "tutorial.webhooks.step.1.description", true));
    steps.add(TutorialStep.create("tutorial.webhooks.step.2.title",
      "tutorial.webhooks.step.2.description", true));
    steps.add(TutorialStep.create("tutorial.webhooks.step.3.title",
      "tutorial.webhooks.step.3.description", "#webhook-create", true));
    return steps;
  }
}
