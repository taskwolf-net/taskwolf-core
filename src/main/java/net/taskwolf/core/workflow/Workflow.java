package net.taskwolf.core.workflow;

import net.taskwolf.core.action.Action;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class Workflow {
  private final List<Action> actions;

  public void trigger(Map<String, Object> information) {
    for (var action : actions) {
      information.putAll(action.execute(information));
    }
  }
}
