package net.taskwolf.core;

import net.taskwolf.core.action.ActionDatabaseTable;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.command.CommandTask;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.core.trigger.TriggerDatabaseTable;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.workflow.WorkflowDatabaseTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;

import java.net.URLClassLoader;

@SpringBootApplication(scanBasePackages = {"net.taskwolf"})
public class CoreApplication {
  public static void main(String[] args) throws Exception {
    var log = Log.create("Core", "/logs/");
    var connection = DatabaseConnection.create();
    connection.connect();
    var keyspace = DatabaseKeyspace.create(connection, "taskwolf",
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
