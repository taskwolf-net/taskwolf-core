package de.flexpedite.core;

import de.flexpedite.core.action.ActionDatabaseTable;
import de.flexpedite.core.command.CommandRegistry;
import de.flexpedite.core.command.CommandTask;
import de.flexpedite.core.database.DatabaseConnection;
import de.flexpedite.core.database.DatabaseKeyspace;
import de.flexpedite.core.log.Log;
import de.flexpedite.core.module.ModuleLoader;
import de.flexpedite.core.trigger.TriggerDatabaseTable;
import de.flexpedite.core.user.UserDatabaseTable;
import de.flexpedite.core.workflow.WorkflowDatabaseTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;

import java.net.URLClassLoader;

@SpringBootApplication(scanBasePackages = {"de.flexpedite"})
public class CoreApplication {
  public static void main(String[] args) throws Exception {
    var log = Log.create("Core", "/logs/");
    var connection = DatabaseConnection.create();
    connection.connect();
    var keyspace = DatabaseKeyspace.create(connection, "flexpedite",
      "SimpleStrategy", 1);
    keyspace.use();
    var userDatabaseTable = UserDatabaseTable.create(connection, keyspace);
    userDatabaseTable.createIfNotExists();
    var triggerDatabaseTable = TriggerDatabaseTable.create(connection, keyspace);
    triggerDatabaseTable.createIfNotExists();
    var actionDatabaseTable = ActionDatabaseTable.create(connection, keyspace);
    triggerDatabaseTable.createIfNotExists();
    var workflowDatabaseTable = WorkflowDatabaseTable.create(connection, keyspace);
    triggerDatabaseTable.createIfNotExists();
    var moduleLoader = ModuleLoader.create(System.getProperty("user.dir") + "/modules/");
    var application = new SpringApplication(CoreApplication.class);
    var classLoader = new URLClassLoader(moduleLoader.moduleFileUrls(),
      application.getClassLoader());
    application.setResourceLoader(new DefaultResourceLoader(classLoader));
    var commandRegistry = CommandRegistry.create();
    var coreModule = CoreModule.create(log, application, moduleLoader,
      connection, keyspace, userDatabaseTable, triggerDatabaseTable,
      actionDatabaseTable, workflowDatabaseTable, commandRegistry);
    coreModule.initialize();
    application.run(args);
    new Thread(() -> CommandTask.create(log, commandRegistry).start()).start();
  }
}
