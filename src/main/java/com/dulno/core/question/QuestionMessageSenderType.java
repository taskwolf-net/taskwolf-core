package com.dulno.core.question;

public enum QuestionMessageSenderType {
  USER,
  TEAM;

  public boolean isUser() {
    return this == USER;
  }

  public boolean isTeam() {
    return this == TEAM;
  }
}