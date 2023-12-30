package net.taskwolf.core;

import net.taskwolf.core.action.ActionDatabaseTable;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.command.CommandTask;
import net.taskwolf.core.command.implementation.*;
import net.taskwolf.core.condition.ConditionDatabaseTable;
import net.taskwolf.core.condition.ConditionFactory;
import net.taskwolf.core.condition.ConditionInformationRepository;
import net.taskwolf.core.condition.text.ConditionTextEndsWith;
import net.taskwolf.core.condition.text.ConditionTextEquals;
import net.taskwolf.core.condition.text.ConditionTextStartsWith;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.intro.Intro;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.template.TemplateDatabaseTable;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.user.UserPasswordResetDatabaseTable;
import net.taskwolf.core.user.UserVerificationDatabaseTable;
import net.taskwolf.core.workflow.WorkflowDatabaseTable;
import net.taskwolf.core.workflow.WorkflowExecutionDatabaseTable;
import net.taskwolf.core.workflow.timeline.TimelineDatabaseTable;
import net.taskwolf.core.workflow.timeline.TimelineFactory;
import net.taskwolf.core.workflow.timeline.entry.TimelineEntryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;

import java.net.URLClassLoader;

@SpringBootApplication(scanBasePackages = {"net.taskwolf"}, exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class CoreApplication {
  public static void main(String[] args) throws Exception {
    Intro.create("1.0.0").print();
    var log = Log.create("Core", "/logs/");
    log.info("Initializing Taskwolf - Core");
    var databaseConnection = createDatabaseConnection();
    var databaseKeyspace = createDatabaseKeyspace(databaseConnection);
    var userDatabaseTable = createUserDatabaseTable(databaseConnection, databaseKeyspace);
    var userVerificationDatabaseTable = createUserVerificationDatabaseTable(databaseConnection, databaseKeyspace);
    var userPasswordDatabaseTable = createUserPasswordResetDatabaseTable(databaseConnection, databaseKeyspace);
    var organizationDatabaseTable = createOrganizationDatabaseTable(databaseConnection, databaseKeyspace);
    var triggerDatabaseTable = createTriggerDatabaseTable(databaseConnection, databaseKeyspace);
    var actionDatabaseTable = createActionDatabaseTable(databaseConnection, databaseKeyspace);
    var conditionDatabaseTable = createConditionDatabaseTable(databaseConnection, databaseKeyspace);
    var workflowDatabaseTable = createWorkflowDatabaseTable(databaseConnection, databaseKeyspace);
    var workflowExecutionDatabaseTable = createWorkflowExecutionDatabaseTable(databaseConnection, databaseKeyspace);
    var templateDatabaseTable = createTemplateDatabaseTable(databaseConnection, databaseKeyspace);
    var timelineDatabaseTable = createTimelineDatabaseTable(databaseConnection, databaseKeyspace);
    var distributionConfiguration = DistributionConfiguration.createAndLoad();
    var distribution = Distribution.create(distributionConfiguration,
      userDatabaseTable, organizationDatabaseTable);
    distribution.initialize();
    var moduleLoader = ModuleLoader.create(log, System.getProperty("user.dir") +
      "/modules/", distribution);
    var application = createSpringApplication(moduleLoader);
    var commandRegistry = CommandRegistry.create();
    var conditionFactory = ConditionFactory.create();
    var conditionRepository = ConditionInformationRepository.create();
    registerConditions(conditionRepository);
    var timelineEntryFactory = TimelineEntryFactory.create(userDatabaseTable);
    var timelineFactory = TimelineFactory.create(timelineDatabaseTable, timelineEntryFactory);
    var coreModule = CoreModule.create(log, moduleLoader, databaseConnection,
      databaseKeyspace, userDatabaseTable, userVerificationDatabaseTable,
      userPasswordDatabaseTable, organizationDatabaseTable, triggerDatabaseTable,
      actionDatabaseTable, conditionDatabaseTable, workflowDatabaseTable,
      workflowExecutionDatabaseTable, templateDatabaseTable, timelineDatabaseTable,
      distribution, commandRegistry, conditionFactory, conditionRepository,
      timelineFactory, application);
    coreModule.initialize();
    registerCommands(log, commandRegistry, moduleLoader, coreModule,
      distributionConfiguration, distribution, templateDatabaseTable);
    application.run(args);
    new Thread(() -> CommandTask.create(log, commandRegistry).start()).start();
    log.info("Successfully booted Taskwolf - Core");
  }

  private static DatabaseConnection createDatabaseConnection() {
    var databaseConnection = DatabaseConnection.create();
    databaseConnection.connect();
    return databaseConnection;
  }

  private static DatabaseKeyspace createDatabaseKeyspace(DatabaseConnection connection) {
    var databaseKeyspace = DatabaseKeyspace.create(connection, "taskwolf",
      "SimpleStrategy", 1);
    databaseKeyspace.createIfNotExists();
    databaseKeyspace.use();
    return databaseKeyspace;
  }

  private static UserDatabaseTable createUserDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var userDatabaseTable = UserDatabaseTable.create(connection, keyspace);
    userDatabaseTable.createIfNotExists();
    return userDatabaseTable;
  }

  private static UserVerificationDatabaseTable createUserVerificationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var userVerificationDatabaseTable = UserVerificationDatabaseTable.create(connection, keyspace);
    userVerificationDatabaseTable.createIfNotExists();
    return userVerificationDatabaseTable;
  }

  private static UserPasswordResetDatabaseTable createUserPasswordResetDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var userPasswordResetDatabaseTable = UserPasswordResetDatabaseTable.create(connection, keyspace);
    userPasswordResetDatabaseTable.createIfNotExists();
    return userPasswordResetDatabaseTable;
  }

  private static OrganizationDatabaseTable createOrganizationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var organizationDatabaseTable = OrganizationDatabaseTable.create(connection, keyspace);
    organizationDatabaseTable.createIfNotExists();
    return organizationDatabaseTable;
  }

  private static TriggerDatabaseTable createTriggerDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var triggerDatabaseTable = TriggerDatabaseTable.create(connection, keyspace);
    triggerDatabaseTable.createIfNotExists();
    return triggerDatabaseTable;
  }

  private static ActionDatabaseTable createActionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var actionDatabaseTable = ActionDatabaseTable.create(connection, keyspace);
    actionDatabaseTable.createIfNotExists();
    return actionDatabaseTable;
  }

  private static ConditionDatabaseTable createConditionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var conditionDatabaseTable = ConditionDatabaseTable.create(connection, keyspace);
    conditionDatabaseTable.createIfNotExists();
    return conditionDatabaseTable;
  }

  private static WorkflowDatabaseTable createWorkflowDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var workflowDatabaseTable = WorkflowDatabaseTable.create(connection, keyspace);
    workflowDatabaseTable.createIfNotExists();
    return workflowDatabaseTable;
  }

  private static WorkflowExecutionDatabaseTable createWorkflowExecutionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var workflowExecutionDatabaseTable = WorkflowExecutionDatabaseTable.create(connection, keyspace);
    workflowExecutionDatabaseTable.createIfNotExists();
    return workflowExecutionDatabaseTable;
  }

  private static TemplateDatabaseTable createTemplateDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var templateDatabaseTable = TemplateDatabaseTable.create(connection, keyspace);
    templateDatabaseTable.createIfNotExists();
    templateDatabaseTable.createIndexIfNotExists("modules");
    return templateDatabaseTable;
  }

  private static TimelineDatabaseTable createTimelineDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var timelineDatabaseTable = TimelineDatabaseTable.create(connection, keyspace);
    timelineDatabaseTable.createIfNotExists();
    return timelineDatabaseTable;
  }

  private static SpringApplication createSpringApplication(ModuleLoader loader) {
    var application = new SpringApplication(CoreApplication.class);
    var classLoader = new URLClassLoader(loader.moduleFileUrls(),
      application.getClassLoader());
    application.setResourceLoader(new DefaultResourceLoader(classLoader));
    return application;
  }

  private static void registerConditions(ConditionInformationRepository repository) {
    repository.register(ConditionTextEquals.information());
    repository.register(ConditionTextStartsWith.information());
    repository.register(ConditionTextEndsWith.information());
  }

  private static void registerCommands(
    Log log, CommandRegistry registry, ModuleLoader moduleLoader,
    CoreModule coreModule, DistributionConfiguration distributionConfiguration,
    Distribution distribution, TemplateDatabaseTable templateDatabaseTable
  ) {
    registry.register(HelpCommand.create(log));
    registry.register(ModuleCommand.create(log, moduleLoader, coreModule));
    registry.register(DistributionCommand.create(log, distributionConfiguration,
      distribution));
    registry.register(TemplateCommand.create(log, templateDatabaseTable));
    registry.register(ExitCommand.create(log));
  }
}