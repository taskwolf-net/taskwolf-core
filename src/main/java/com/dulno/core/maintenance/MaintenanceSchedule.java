package com.dulno.core.maintenance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class MaintenanceSchedule {
  private final MaintenanceDatabaseTable maintenanceDatabaseTable;
  private final ScheduledExecutorService executorService =
    Executors.newScheduledThreadPool(1);
  private ScheduledFuture<?> scheduler;
  private Maintenance maintenance;

  private static final int RESET_INTERVAL = 1000 * 60;
  private static final TimeUnit RESET_TIME_UNIT = TimeUnit.MILLISECONDS;

  public void start() {
    scheduler = executorService.scheduleAtFixedRate(this::execute,
      calculateInitialDelay(), RESET_INTERVAL, RESET_TIME_UNIT);
    execute();
  }

  private long calculateInitialDelay() {
    var current = LocalDateTime.now();
    return Duration.between(current, current.plusMinutes(1)
      .truncatedTo(ChronoUnit.MINUTES)).toMillis();
  }

  private void execute() {
    maintenanceDatabaseTable.findMaintenanceByStatus(MaintenanceStatus.RUNNING)
      .thenAccept(this::applyMaintenance);
  }

  private void applyMaintenance(List<Maintenance> maintenanceList) {
    if (maintenanceList.isEmpty()) {
      maintenance = null;
      return;
    }
    maintenance = maintenanceList.get(0);
  }

  public boolean isMaintenanceRunning() {
    return maintenance != null;
  }

  public Optional<Maintenance> currentMaintenance() {
    return Optional.ofNullable(maintenance);
  }

  public void stop() {
    scheduler.cancel(false);
  }
}
