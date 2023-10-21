package net.taskwolf.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionDatabaseTable;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.iterator.AsyncAllocationIterator;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.organization.InvitationDatabaseTable;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.trigger.TriggerEntry;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.Workflow;
import net.taskwolf.core.workflow.WorkflowDatabaseTable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public class CoreModule {
  private final Log log;
  private final ModuleLoader moduleLoader;
  private final DatabaseConnection databaseConnection;
  private final DatabaseKeyspace databaseKeyspace;
  private final UserDatabaseTable userDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final InvitationDatabaseTable invitationDatabaseTable;
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final ActionDatabaseTable actionDatabaseTable;
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final Distribution distribution;
  private final CommandRegistry commandRegistry;

  void initialize() throws Exception {
    moduleLoader.loadModules(this);
  }

  public CompletableFuture<List<TriggerEntry>> findTriggerEntries(
    String module, String type
  ) {
    var futureResponse = new CompletableFuture<List<TriggerEntry>>();
    triggerDatabaseTable.findTriggersByModuleAndType(module, type)
      .thenApply(entries -> AsyncAllocationIterator.execute(entries, entry ->
          distribution.isAssignedUser(module, entry.ownerId()), entries.size(),
        triggers -> futureResponse.complete(triggers.entrySet().stream()
          .filter(Map.Entry::getValue).map(Map.Entry::getKey).toList())));
    return futureResponse;
  }

  public CompletableFuture<Workflow> createWorkflow(UUID triggerId) {
    var futureResponse = new CompletableFuture<Workflow>();
    workflowDatabaseTable.findWorkflowByTrigger(triggerId).thenAccept(workflowEntry ->
      createActions(workflowEntry.id()).thenApply(actions ->
        futureResponse.complete(Workflow.create(actions))));
    return futureResponse;
  }

  private CompletableFuture<List<Action>> createActions(UUID workflowId) {
    return actionDatabaseTable.findActionsByWorkflow(workflowId)
      .thenApply(entries -> entries.stream().map(entry ->
        createAction(entry.module(), entry.type(), entry.content()))
        .collect(Collectors.toList()));
  }

  public Trigger createTrigger(String module, String type, String content) {
    return moduleLoader.findModule(module).triggerFactory().create(type, content);
  }

  public Action createAction(String module, String type, String content) {
    return moduleLoader.findModule(module).actionFactory().create(type, content);
  }
}
