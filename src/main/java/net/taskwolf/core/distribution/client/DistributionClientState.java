package net.taskwolf.core.distribution.client;

public enum DistributionClientState {
  UNAUTHORIZED,
  AUTHORIZED;

  public boolean isUnauthorized() {
    return this == UNAUTHORIZED;
  }

  public boolean isAuthorized() {
    return this == AUTHORIZED;
  }
}
