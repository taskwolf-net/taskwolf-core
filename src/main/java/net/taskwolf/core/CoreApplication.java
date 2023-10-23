package net.taskwolf.core;

import net.taskwolf.core.action.ActionDatabaseTable;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.command.CommandTask;
import net.taskwolf.core.command.implementation.ExitCommand;
import net.taskwolf.core.command.implementation.HelpCommand;
import net.taskwolf.core.command.implementation.ModuleCommand;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.intro.Intro;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.organization.InvitationDatabaseTable;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.template.TemplateDatabaseTable;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.WorkflowDatabaseTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;

import java.net.URLClassLoader;

@SpringBootApplication(scanBasePackages = {"net.taskwolf"})
public class CoreApplication {
  private static DatabaseConnection databaseConnection;
  private static DatabaseKeyspace databaseKeyspace;
  private static UserDatabaseTable userDatabaseTable;
  private static OrganizationDatabaseTable organizationDatabaseTable;
  private static InvitationDatabaseTable invitationDatabaseTable;
  private static TriggerDatabaseTable triggerDatabaseTable;
  private static ActionDatabaseTable actionDatabaseTable;
  private static WorkflowDatabaseTable workflowDatabaseTable;
  private static TemplateDatabaseTable templateDatabaseTable;
  private static Distribution distribution;

  public static void main(String[] args) throws Exception {
    Intro.create("1.0.0").print();
    var log = Log.create("Core", "/logs/");
    initializeDatabase();
    initializeDatabaseTables();
    var distributionConfiguration = DistributionConfiguration.createAndLoad();
    distribution = Distribution.create(distributionConfiguration,
      userDatabaseTable, organizationDatabaseTable);
    distribution.initialize();
    var moduleLoader = ModuleLoader.create(log, System.getProperty("user.dir") +
      "/modules/", distribution);
    var application = new SpringApplication(CoreApplication.class);
    var classLoader = new URLClassLoader(moduleLoader.moduleFileUrls(),
      application.getClassLoader());
    application.setResourceLoader(new DefaultResourceLoader(classLoader));
    var commandRegistry = CommandRegistry.create();
    var coreModule = CoreModule.create(log, moduleLoader, databaseConnection,
      databaseKeyspace, userDatabaseTable, organizationDatabaseTable,
      invitationDatabaseTable, triggerDatabaseTable, actionDatabaseTable,
      workflowDatabaseTable, distribution, commandRegistry);
    coreModule.initialize();
    registerCommands(log, commandRegistry, moduleLoader, coreModule);
    application.run(args);
    new Thread(() -> CommandTask.create(log, commandRegistry).start()).start();
  }

  private static void initializeDatabase() {
    databaseConnection = DatabaseConnection.create();
    databaseConnection.connect();
    databaseKeyspace = DatabaseKeyspace.create(databaseConnection, "taskwolf",
      "SimpleStrategy", 1);
    databaseKeyspace.createIfNotExists();
    databaseKeyspace.use();
  }

  private static void initializeDatabaseTables() {
    userDatabaseTable = UserDatabaseTable.create(databaseConnection, databaseKeyspace);
    userDatabaseTable.createIfNotExists();
    organizationDatabaseTable = OrganizationDatabaseTable.create(databaseConnection, databaseKeyspace);
    organizationDatabaseTable.createIfNotExists();
    invitationDatabaseTable = InvitationDatabaseTable.create(databaseConnection, databaseKeyspace);
    invitationDatabaseTable.createIfNotExists();
    triggerDatabaseTable = TriggerDatabaseTable.create(databaseConnection, databaseKeyspace);
    triggerDatabaseTable.createIfNotExists();
    actionDatabaseTable = ActionDatabaseTable.create(databaseConnection, databaseKeyspace);
    actionDatabaseTable.createIfNotExists();
    workflowDatabaseTable = WorkflowDatabaseTable.create(databaseConnection, databaseKeyspace);
    workflowDatabaseTable.createIfNotExists();
    templateDatabaseTable = TemplateDatabaseTable.create(databaseConnection, databaseKeyspace);
    templateDatabaseTable.createIfNotExists();
  }

  private static void registerCommands(
    Log log, CommandRegistry registry, ModuleLoader moduleLoader,
    CoreModule coreModule
  ) {
    registry.register(HelpCommand.create(log));
    registry.register(ModuleCommand.create(log, moduleLoader, coreModule));
    registry.register(ExitCommand.create(log));
  }

  @Bean
  DatabaseConnection provideDatabaseConnection() {
    return databaseConnection;
  }

  @Bean
  DatabaseKeyspace provideDatabaseKeyspace() {
    return databaseKeyspace;
  }

  @Bean
  UserDatabaseTable provideUserDatabaseTable() {
    return userDatabaseTable;
  }

  @Bean
  OrganizationDatabaseTable provideOrganizationDatabaseTable() {
    return organizationDatabaseTable;
  }

  @Bean
  InvitationDatabaseTable provideInvitationDatabaseTable() {
    return invitationDatabaseTable;
  }

  @Bean
  TriggerDatabaseTable provideTriggerDatabaseTable() {
    return triggerDatabaseTable;
  }

  @Bean
  ActionDatabaseTable provideActionDatabaseTable() {
    return actionDatabaseTable;
  }

  @Bean
  WorkflowDatabaseTable provideWorkflowDatabaseTable() {
    return workflowDatabaseTable;
  }

  @Bean
  TemplateDatabaseTable provideTemplateDatabaseTable() {
    return templateDatabaseTable;
  }

  @Bean
  Distribution provideDistribution() {
    return distribution;
  }
}