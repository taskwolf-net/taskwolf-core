package net.taskwolf.core.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.core.bundle.*;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.workflow.operation.OperationDatabaseTable;

import java.util.UUID;

@Singleton
public final class BundleCommand extends Command {
  private final BundleDatabaseTable bundleDatabaseTable;
  private final OperationDatabaseTable operationDatabaseTable;

  @Inject
  private BundleCommand(
    Log log, BundleDatabaseTable bundleDatabaseTable,
    OperationDatabaseTable operationDatabaseTable
  ) {
    super(log, "bundle", new String[0], new String[] {"info <owner>",
      "apply <owner, type, class, monthly / yearly, (operation-limit), (data-limit)>",
      "change <owner, type, class, monthly / yearly, (operation-limit), (data-limit)>",
      "delete <owner>"});
    this.bundleDatabaseTable = bundleDatabaseTable;
    this.operationDatabaseTable = operationDatabaseTable;
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    if (arguments.length == 0) {
      return false;
    }
    if (arguments[0].equalsIgnoreCase("info")) {
      return findBundleInfo(arguments);
    } else if (arguments[0].equalsIgnoreCase("apply")) {
      return applyBundle(arguments);
    } else if (arguments[0].equalsIgnoreCase("change")) {
      return changeBundle(arguments);
    } else if (arguments[0].equalsIgnoreCase("delete")) {
      return deleteBundle(arguments);
    }
    return false;
  }

  private boolean findBundleInfo(String[] arguments) {
    if (arguments.length != 2) {
      return false;
    }
    var owner = UUID.fromString(arguments[1]);
    bundleDatabaseTable.bundleExists(owner).thenAccept(exists ->
      findBundleInfo(owner, exists));
    return true;
  }

  private void findBundleInfo(UUID owner, boolean bundleExists) {
    if (!bundleExists) {
      log().info("The owner could either not be found. Either the owner does " +
        "not exist, or he is part of an organization and therefore does not " +
        "have his own bundle.");
      return;
    }
    bundleDatabaseTable.findBundle(owner).thenAccept(this::printBundleInfo);
  }

  private void printBundleInfo(Bundle bundle) {
    log().info("Type: " + bundle.bundleType());
    log().info("Class: " + bundle.bundleClass());
    log().info("Runtime: " + bundle.bundleRuntime());
    log().info("Price: " + bundle.price());
    log().info("Expiration: " + bundle.expiration());
    log().info("Workflow operation limit: " +
      bundle.workflowOperationLimit());
    log().info("Database data limit: " + bundle.databaseDataLimit());
  }

  private boolean applyBundle(String[] arguments) throws Exception {
    if (arguments.length != 5 && arguments.length != 7) {
      return false;
    }
    var owner = UUID.fromString(arguments[1]);
    var bundleType = BundleType.valueOf(arguments[2].toUpperCase());
    var bundleClass = BundleClass.valueOf(arguments[3].toUpperCase());
    var bundleRuntime = BundleRuntime.valueOf(arguments[4].toUpperCase());
    if (bundleType.isEnterprise() && arguments.length != 7) {
      log().info("If you want to use the Enterprise bundle, you must " +
        "specify a workflow execution limit and a database data limit.");
      return true;
    }
    if (arguments.length == 7) {
      var operationLimit = Long.parseLong(arguments[5]);
      var dataLimit = Long.parseLong(arguments[6]);
      bundleDatabaseTable.insertBundle(Bundle.of(owner,
        BundlePreset.createAndLoad(bundleType, bundleClass), bundleRuntime,
        operationLimit, dataLimit));
    } else {
      bundleDatabaseTable.insertBundle(Bundle.of(owner,
        BundlePreset.createAndLoad(bundleType, bundleClass), bundleRuntime));
    }
    operationDatabaseTable.insertOperations(owner);
    log().info("You have successfully applied the bundle");
    return true;
  }

  private boolean changeBundle(String[] arguments) throws Exception {
    if (arguments.length != 5 && arguments.length != 7) {
      return false;
    }
    var owner = UUID.fromString(arguments[1]);
    var bundleType = BundleType.valueOf(arguments[2].toUpperCase());
    var bundleClass = BundleClass.valueOf(arguments[3].toUpperCase());
    var bundleRuntime = BundleRuntime.valueOf(arguments[4].toUpperCase());
    if (bundleType.isEnterprise() && arguments.length != 7) {
      log().info("If you want to use the Enterprise bundle, you must " +
        "specify a workflow execution limit and a database data limit.");
      return true;
    }
    if (arguments.length == 7) {
      var operationLimit = Long.parseLong(arguments[5]);
      var dataLimit = Long.parseLong(arguments[6]);
      bundleDatabaseTable.updateBundle(Bundle.of(owner,
        BundlePreset.createAndLoad(bundleType, bundleClass), bundleRuntime,
        operationLimit, dataLimit));
    } else {
      bundleDatabaseTable.updateBundle(Bundle.of(owner,
        BundlePreset.createAndLoad(bundleType, bundleClass), bundleRuntime));
    }
    operationDatabaseTable.resetExpiration(owner);
    log().info("You have successfully changed the bundle");
    return true;
  }

  private boolean deleteBundle(String[] arguments) {
    if (arguments.length != 2) {
      return false;
    }
    var owner = UUID.fromString(arguments[1]);
    bundleDatabaseTable.deleteBundle(owner);
    operationDatabaseTable.deleteOperations(owner);
    log().info("You have successfully deleted the bundle");
    return true;
  }
}
