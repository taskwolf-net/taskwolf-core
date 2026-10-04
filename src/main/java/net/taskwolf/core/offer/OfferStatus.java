package net.taskwolf.core.offer;

public enum OfferStatus {
  PENDING,
  ACCEPTED,
  DECLINED,
  WITHDRAWN;

  public boolean isPending() {
    return this == PENDING;
  }

  public boolean isAccepted() {
    return this == ACCEPTED;
  }

  public boolean isDeclined() {
    return this == DECLINED;
  }

  public boolean isWithdrawn() {
    return this == WITHDRAWN;
  }
}
