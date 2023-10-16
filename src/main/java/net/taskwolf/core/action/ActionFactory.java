package net.taskwolf.core.action;

public interface ActionFactory {
  Action create(String json);
}
