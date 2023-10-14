package de.flexpedite.core;

import de.flexpedite.core.action.ActionDatabaseTable;
import de.flexpedite.core.database.DatabaseConnection;
import de.flexpedite.core.database.DatabaseKeyspace;
import de.flexpedite.core.module.ModuleLoader;
import de.flexpedite.core.trigger.TriggerDatabaseTable;
import de.flexpedite.core.workflow.WorkflowDatabaseTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;

import java.net.URLClassLoader;

@SpringBootApplication(scanBasePackages = {"de.flexpedite"})
public class CoreApplication {
  public static void main(String[] args) throws Exception {
    DatabaseConnection connection = DatabaseConnection.create();
    connection.connect();
    DatabaseKeyspace keyspace = DatabaseKeyspace.create(connection, "flexpedite",
      "SimpleStrategy", 1);
    keyspace.use();
    TriggerDatabaseTable triggerDatabaseTable =
      TriggerDatabaseTable.create(connection, keyspace);
    triggerDatabaseTable.createIfNotExists();
    ActionDatabaseTable actionDatabaseTable =
      ActionDatabaseTable.create(connection, keyspace);
    triggerDatabaseTable.createIfNotExists();
    WorkflowDatabaseTable workflowDatabaseTable =
      WorkflowDatabaseTable.create(connection, keyspace);
    triggerDatabaseTable.createIfNotExists();
    ModuleLoader moduleLoader = ModuleLoader.create(
      System.getProperty("user.dir") + "/modules/");
    CoreModule coreModule = CoreModule.create(moduleLoader, connection, keyspace,
      triggerDatabaseTable, actionDatabaseTable, workflowDatabaseTable);
    coreModule.initialize();
    SpringApplication application = new SpringApplication(CoreApplication.class);
    URLClassLoader classLoader = new URLClassLoader(moduleLoader.moduleFileUrls(),
      application.getClassLoader());
    application.setResourceLoader(new DefaultResourceLoader(classLoader));
    application.run(args);
  }
}
