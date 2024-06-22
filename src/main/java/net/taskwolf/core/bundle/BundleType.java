package net.taskwolf.core.bundle;

public enum BundleType {
  TRIAL,
  PROFESSIONAL,
  TEAM,
  ENTERPRISE;

  public boolean isTrial() {
    return this == TRIAL;
  }

  public boolean isProfessional() {
    return this == PROFESSIONAL;
  }

  public boolean isTeam() {
    return this == TEAM;
  }

  public boolean isEnterprise() {
    return this == ENTERPRISE;
  }
}
