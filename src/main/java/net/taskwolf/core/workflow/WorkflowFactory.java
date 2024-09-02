package net.taskwolf.core.workflow;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.bundle.BundleDatabaseTable;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.locale.Locale;
import net.taskwolf.core.locale.Translation;
import net.taskwolf.core.mail.Mail;
import net.taskwolf.core.notification.NotificationDatabaseTable;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.organization.team.TeamDatabaseTable;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.operation.OperationDatabaseTable;
import net.taskwolf.core.workflow.throttle.WorkflowThrottleDatabaseTable;
import net.taskwolf.core.workflow.timeline.TimelineDatabaseTable;

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
    NotificationDatabaseTable notificationDatabaseTable, Translation translation,
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
      notificationDatabaseTable, translation, notificationMail, workflowEntry,
      actions, conditions);
  }
}
