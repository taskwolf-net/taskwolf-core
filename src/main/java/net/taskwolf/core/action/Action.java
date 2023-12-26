package net.taskwolf.core.action;

import java.util.Map;

public interface Action {
  ActionResult execute(Map<String, Object> information);
}
