package net.taskwolf.core.workflow;

public enum WorkflowAffiliation {
  PRIVATE,
  ORGANIZATION;

  public boolean isPrivate() {
    return this == PRIVATE;
  }

  public boolean isOrganization() {
    return this == ORGANIZATION;
  }
}
