package net.taskwolf.core.sale;

public enum SaleMessageSenderType {
  USER,
  TEAM;

  public boolean isUser() {
    return this == USER;
  }

  public boolean isTeam() {
    return this == TEAM;
  }
}