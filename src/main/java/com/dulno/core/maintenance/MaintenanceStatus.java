package com.dulno.core.maintenance;

public enum MaintenanceStatus {
  SCHEDULED,
  RUNNING,
  COMPLETED;

  public boolean isScheduled() {
    return this == SCHEDULED;
  }

  public boolean isRunning() {
    return this == RUNNING;
  }

  public boolean isCompleted() {
    return this == COMPLETED;
  }
}
