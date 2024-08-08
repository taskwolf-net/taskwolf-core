package net.taskwolf.core.offer;

public enum OfferStatus {
  READY,
  ACCEPTED,
  WITHDRAWN;

  public boolean isReady() {
    return this == READY;
  }

  public boolean isAccepted() {
    return this == ACCEPTED;
  }

  public boolean isWithdrawn() {
    return this == WITHDRAWN;
  }
}
