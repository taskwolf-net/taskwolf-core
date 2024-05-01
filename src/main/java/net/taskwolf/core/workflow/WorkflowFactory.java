package net.taskwolf.core.workflow;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.taskwolf.core.action.ActionExecutor;
import net.taskwolf.core.condition.Condition;
import net.taskwolf.core.locale.Locale;
import net.taskwolf.core.mail.TaskwolfMail;
import net.taskwolf.core.notification.NotificationDatabaseTable;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.timeline.TimelineDatabaseTable;

import java.util.Map;

@Singleton
public final class WorkflowFactory {
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final WorkflowExecutionDatabaseTable workflowExecutionDatabaseTable;
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final Locale englishLocale;
  private final TaskwolfMail notificationMail;

  @Inject
  private WorkflowFactory(
    WorkflowDatabaseTable workflowDatabaseTable,
    WorkflowExecutionDatabaseTable workflowExecutionDatabaseTable,
    TimelineDatabaseTable timelineDatabaseTable, UserDatabaseTable userDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable,
    NotificationDatabaseTable notificationDatabaseTable,
    @Named("englishLocale") Locale englishLocale,
    @Named("notificationMail") TaskwolfMail notificationMail
  ) {
    this.workflowDatabaseTable = workflowDatabaseTable;
    this.workflowExecutionDatabaseTable = workflowExecutionDatabaseTable;
    this.timelineDatabaseTable = timelineDatabaseTable;
    this.userDatabaseTable = userDatabaseTable;
    this.organizationDatabaseTable = organizationDatabaseTable;
    this.notificationDatabaseTable = notificationDatabaseTable;
    this.englishLocale = englishLocale;
    this.notificationMail = notificationMail;
  }

  public Workflow create(
    WorkflowEntry workflowEntry, Map<Integer, ActionExecutor> actions,
    Multimap<Integer, Condition> conditions
  ) {
    return Workflow.create(workflowDatabaseTable, workflowExecutionDatabaseTable,
      timelineDatabaseTable, userDatabaseTable, organizationDatabaseTable,
      notificationDatabaseTable, englishLocale, notificationMail, workflowEntry,
      actions, conditions);
  }
}
