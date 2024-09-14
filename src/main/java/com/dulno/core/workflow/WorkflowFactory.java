package com.dulno.core.workflow;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.bundle.BundleDatabaseTable;
import com.dulno.core.condition.Condition;
import com.dulno.core.locale.Translation;
import com.dulno.core.mail.Mail;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.notification.NotificationDatabaseTable;
import com.dulno.core.organization.OrganizationDatabaseTable;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.workflow.operation.OperationDatabaseTable;
import com.dulno.core.workflow.throttle.WorkflowThrottleDatabaseTable;
import com.dulno.core.workflow.timeline.TimelineDatabaseTable;

import java.util.Map;

@Singleton
public final class WorkflowFactory {
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;
  private final BundleDatabaseTable bundleDatabaseTable;
  private final OperationDatabaseTable operationDatabaseTable;
  private final WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final TeamDatabaseTable teamDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final Translation translation;
  private final Mail notificationMail;

  @Inject
  private WorkflowFactory(
    WorkflowDatabaseTable workflowDatabaseTable,
    TimelineDatabaseTable timelineDatabaseTable, UserDatabaseTable userDatabaseTable,
    BundleDatabaseTable bundleDatabaseTable,
    OperationDatabaseTable operationDatabaseTable,
    WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    NotificationDatabaseTable notificationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, Translation translation,
    @Named("notificationMail") Mail notificationMail
  ) {
    this.workflowDatabaseTable = workflowDatabaseTable;
    this.timelineDatabaseTable = timelineDatabaseTable;
    this.userDatabaseTable = userDatabaseTable;
    this.bundleDatabaseTable = bundleDatabaseTable;
    this.operationDatabaseTable = operationDatabaseTable;
    this.workflowThrottleDatabaseTable = workflowThrottleDatabaseTable;
    this.organizationDatabaseTable = organizationDatabaseTable;
    this.teamDatabaseTable = teamDatabaseTable;
    this.notificationDatabaseTable = notificationDatabaseTable;
    this.maintenanceSchedule = maintenanceSchedule;
    this.translation = translation;
    this.notificationMail = notificationMail;
  }

  public Workflow create(
    WorkflowEntry workflowEntry, Map<Integer, ActionExecutor> actions,
    Multimap<Integer, Condition> conditions
  ) {
    return Workflow.create(workflowDatabaseTable, timelineDatabaseTable,
      userDatabaseTable, bundleDatabaseTable, operationDatabaseTable,
      workflowThrottleDatabaseTable, organizationDatabaseTable, teamDatabaseTable,
      notificationDatabaseTable, maintenanceSchedule, translation,
      notificationMail, workflowEntry, actions, conditions);
  }
}
