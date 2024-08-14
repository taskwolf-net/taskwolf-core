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
  private final BundlePresetRepository presetRepository;

  @Inject
  private BundleCommand(
    Log log, BundleDatabaseTable bundleDatabaseTable,
    OperationDatabaseTable operationDatabaseTable,
    BundlePresetRepository presetRepository
  ) {
    super(log, "bundle", new String[0], new String[] {"info <owner>",
      "apply <owner, type, class, monthly / yearly, (price), (operation-limit), " +
        "(process-limit), (data-limit), (organization-members), (organization-teams)>",
      "change <owner, type, class, monthly / yearly, (price), (operation-limit), " +
        "(process-limit), (data-limit), (organization-members), (organization-teams)>",
      "delete <owner>"});
    this.bundleDatabaseTable = bundleDatabaseTable;
    this.operationDatabaseTable = operationDatabaseTable;
    this.presetRepository = presetRepository;
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
    log().info("Workflow number limit: " + bundle.workflowNumberLimit());
    log().info("Workflow operation limit: " + bundle.workflowOperationLimit());
    log().info("Process number limit: " + bundle.processNumberLimit());
    log().info("Database number limit: " + bundle.databaseNumberLimit());
    log().info("Database data limit: " + bundle.databaseDataLimit());
  }

  private boolean applyBundle(String[] arguments) throws Exception {
    if (arguments.length != 3 && arguments.length != 5 && arguments.length != 11) {
      return false;
    }
    var owner = UUID.fromString(arguments[1]);
    var bundleType = BundleType.valueOf(arguments[2].toUpperCase());
    if (bundleType.isTrial()) {
      bundleDatabaseTable.insertBundle(Bundle.of(owner,
        presetRepository.findPreset(bundleType).get(), BundleRuntime.WEEKLY));
      log().info("You have successfully applied the bundle");
      return true;
    }
    var bundleClass = BundleClass.valueOf(arguments[3].toUpperCase());
    var bundleRuntime = BundleRuntime.valueOf(arguments[4].toUpperCase());
    if (bundleType.isEnterprise() && arguments.length != 11) {
      log().info("If you want to use the Enterprise bundle, you must " +
        "specify a workflow execution limit, a process number limit, " +
        "a database data limit, an organization member limit and " +
        "an organization team limit.");
      return true;
    }
    if (arguments.length == 11) {
      var price = Double.parseDouble(arguments[5]);
      var operationLimit = Long.parseLong(arguments[6]);
      var processLimit = Long.parseLong(arguments[7]);
      var dataLimit = Long.parseLong(arguments[8]);
      var memberLimit = Long.parseLong(arguments[9]);
      var teamLimit = Long.parseLong(arguments[10]);
      bundleDatabaseTable.insertBundle(Bundle.of(owner,
        presetRepository.findPreset(bundleType, bundleClass).get(), bundleRuntime,
        price, operationLimit, processLimit, dataLimit, memberLimit, teamLimit));
    } else {
      bundleDatabaseTable.insertBundle(Bundle.of(owner,
        presetRepository.findPreset(bundleType, bundleClass).get(), bundleRuntime));
    }
    operationDatabaseTable.insertOperations(owner);
    log().info("You have successfully applied the bundle");
    return true;
  }

  private boolean changeBundle(String[] arguments) throws Exception {
    if (arguments.length != 3 && arguments.length != 5 && arguments.length != 11) {
      return false;
    }
    var owner = UUID.fromString(arguments[1]);
    var bundleType = BundleType.valueOf(arguments[2].toUpperCase());
    if (bundleType.isTrial()) {
      bundleDatabaseTable.updateBundle(Bundle.of(owner,
        presetRepository.findPreset(bundleType).get(), BundleRuntime.WEEKLY));
      log().info("You have successfully applied the bundle");
      return true;
    }
    var bundleClass = BundleClass.valueOf(arguments[3].toUpperCase());
    var bundleRuntime = BundleRuntime.valueOf(arguments[4].toUpperCase());
    if (bundleType.isEnterprise() && arguments.length != 11) {
      log().info("If you want to use the Enterprise bundle, you must " +
        "specify a workflow execution limit, a process number limit, " +
        "a database data limit, an organization member limit and " +
        "an organization team limit.");
      return true;
    }
    if (arguments.length == 11) {
      var price = Double.parseDouble(arguments[5]);
      var operationLimit = Long.parseLong(arguments[6]);
      var processLimit = Long.parseLong(arguments[7]);
      var dataLimit = Long.parseLong(arguments[8]);
      var memberLimit = Long.parseLong(arguments[9]);
      var teamLimit = Long.parseLong(arguments[10]);
      bundleDatabaseTable.updateBundle(Bundle.of(owner,
        BundlePreset.createAndLoad(bundleType, bundleClass), bundleRuntime,
        price, operationLimit, processLimit, dataLimit, memberLimit, teamLimit));
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
