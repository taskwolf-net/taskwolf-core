package net.taskwolf.core.action;

import java.util.Map;

public interface Action {
  Map<String, Object> execute(Map<String, Object> information);
}
