package net.taskwolf.core;

import net.taskwolf.core.action.Action;
import net.taskwolf.core.action.ActionDatabaseTable;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.trigger.Trigger;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.Workflow;
import net.taskwolf.core.workflow.WorkflowDatabaseTable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
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
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final ActionDatabaseTable actionDatabaseTable;
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final CommandRegistry commandRegistry;

  void initialize() throws Exception {
    moduleLoader.loadModules(this);
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
        createAction(entry.module(), entry.content())).collect(Collectors.toList()));
  }

  public Trigger createTrigger(String module, String content) {
    return moduleLoader.findModule(module).triggerFactory().create(content);
  }

  public Action createAction(String module, String content) {
    return moduleLoader.findModule(module).actionFactory().create(content);
  }
}
