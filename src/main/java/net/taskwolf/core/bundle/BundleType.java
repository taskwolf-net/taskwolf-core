package net.taskwolf.core.bundle;

public enum BundleType {
  TRIAL,
  INDIVIDUAL,
  TEAM,
  ENTERPRISE;

  public boolean isTrial() {
    return this == TRIAL;
  }

  public boolean isIndividual() {
    return this == INDIVIDUAL;
  }

  public boolean isTeam() {
    return this == TEAM;
  }

  public boolean isEnterprise() {
    return this == ENTERPRISE;
  }
}
