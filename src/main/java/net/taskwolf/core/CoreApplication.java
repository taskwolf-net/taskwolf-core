package net.taskwolf.core;

import net.taskwolf.core.action.ActionDatabaseTable;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.command.CommandTask;
import net.taskwolf.core.command.implementation.DistributionCommand;
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
import org.springframework.core.io.DefaultResourceLoader;

import java.net.URLClassLoader;

@SpringBootApplication(scanBasePackages = {"net.taskwolf"}, exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class CoreApplication {
  public static void main(String[] args) throws Exception {
    Intro.create("1.0.0").print();
    var log = Log.create("Core", "/logs/");
    log.info("Initializing Taskwolf - Core");
    var databaseConnection = DatabaseConnection.create();
    databaseConnection.connect();
    var databaseKeyspace = DatabaseKeyspace.create(databaseConnection, "taskwolf",
      "SimpleStrategy", 1);
    databaseKeyspace.createIfNotExists();
    databaseKeyspace.use();
    var userDatabaseTable = UserDatabaseTable.create(databaseConnection,
      databaseKeyspace);
    userDatabaseTable.createIfNotExists();
    var organizationDatabaseTable = OrganizationDatabaseTable.create(
      databaseConnection, databaseKeyspace);
    organizationDatabaseTable.createIfNotExists();
    var invitationDatabaseTable = InvitationDatabaseTable.create(
      databaseConnection, databaseKeyspace);
    invitationDatabaseTable.createIfNotExists();
    var triggerDatabaseTable = TriggerDatabaseTable.create(databaseConnection,
      databaseKeyspace);
    triggerDatabaseTable.createIfNotExists();
    var actionDatabaseTable = ActionDatabaseTable.create(databaseConnection,
      databaseKeyspace);
    actionDatabaseTable.createIfNotExists();
    var workflowDatabaseTable = WorkflowDatabaseTable.create(databaseConnection,
      databaseKeyspace);
    workflowDatabaseTable.createIfNotExists();
    var templateDatabaseTable = TemplateDatabaseTable.create(databaseConnection,
      databaseKeyspace);
    templateDatabaseTable.createIfNotExists();
    var distributionConfiguration = DistributionConfiguration.createAndLoad();
    var distribution = Distribution.create(distributionConfiguration,
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
      workflowDatabaseTable, templateDatabaseTable, distribution,
      commandRegistry, application);
    coreModule.initialize();
    registerCommands(log, commandRegistry, moduleLoader, coreModule,
      distributionConfiguration, distribution);
    application.run(args);
    new Thread(() -> CommandTask.create(log, commandRegistry).start()).start();
    log.info("Successfully booted Taskwolf - Core");
  }

  private static void registerCommands(
    Log log, CommandRegistry registry, ModuleLoader moduleLoader,
    CoreModule coreModule, DistributionConfiguration distributionConfiguration,
    Distribution distribution
  ) {
    registry.register(HelpCommand.create(log));
    registry.register(ModuleCommand.create(log, moduleLoader, coreModule));
    registry.register(DistributionCommand.create(log, distributionConfiguration,
      distribution));
    registry.register(ExitCommand.create(log));
  }
}