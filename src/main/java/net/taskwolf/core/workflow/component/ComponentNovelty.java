package net.taskwolf.core.workflow.component;

public enum ComponentNovelty {
  NEW,
  OLD;

  public boolean isNew() {
    return this == NEW;
  }

  public boolean isOld() {
    return this == OLD;
  }
}
