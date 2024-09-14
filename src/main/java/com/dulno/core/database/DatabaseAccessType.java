package com.dulno.core.database;

public enum DatabaseAccessType {
  READ,
  WRITE;

  public boolean isRead() {
    return this == READ;
  }

  public boolean isWrite() {
    return this == WRITE;
  }
}
