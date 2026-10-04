package net.taskwolf.core.ticket;

public enum TicketMessageAuthorType {
  USER,
  TEAM;

  public boolean isUser() {
    return this == USER;
  }

  public boolean isTeam() {
    return this == TEAM;
  }
}