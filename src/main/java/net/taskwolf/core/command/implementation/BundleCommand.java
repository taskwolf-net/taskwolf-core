package net.taskwolf.core.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.core.bundle.Bundle;
import net.taskwolf.core.bundle.BundleDatabaseTable;
import net.taskwolf.core.bundle.BundlePreset;
import net.taskwolf.core.bundle.BundleType;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.log.Log;

import java.util.UUID;

@Singleton
public final class BundleCommand extends Command {
  private final BundleDatabaseTable bundleDatabaseTable;

  @Inject
  private BundleCommand(Log log, BundleDatabaseTable bundleDatabaseTable) {
    super(log, "bundle", new String[0], new String[] {"info <owner>",
      "apply <owner, type, expiration, (execution-limit), (data-limit)>",
      "change <owner, type, expiration, (execution-limit), (data-limit)>",
      "delete <owner>"});
    this.bundleDatabaseTable = bundleDatabaseTable;
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
    log().info("Type: " + bundle.type());
    log().info("Expiration: " + bundle.expiration());
    log().info("Workflow execution limit: " +
      bundle.workflowExecutionLimit());
    log().info("Database data limit: " + bundle.databaseDataLimit());
  }

  private boolean applyBundle(String[] arguments) throws Exception {
    if (arguments.length != 4 && arguments.length != 6) {
      return false;
    }
    var owner = UUID.fromString(arguments[1]);
    var type = BundleType.valueOf(arguments[2].toUpperCase());
    var expiration = Long.valueOf(arguments[3]);
    if (type.isEnterprise() && arguments.length != 6) {
      log().info("If you want to use the Enterprise bundle, you must " +
        "specify a workflow execution limit and a database data limit.");
      return true;
    }
    if (arguments.length == 6) {
      var executionLimit = Long.parseLong(arguments[4]);
      var dataLimit = Long.parseLong(arguments[5]);
      bundleDatabaseTable.insertBundle(Bundle.of(owner,
        BundlePreset.createAndLoad(type), executionLimit,  executionLimit,
        dataLimit));
    } else {
      bundleDatabaseTable.insertBundle(Bundle.of(owner,
        BundlePreset.createAndLoad(type), expiration));
    }
    log().info("You have successfully applied the bundle");
    return true;
  }

  private boolean changeBundle(String[] arguments) throws Exception {
    if (arguments.length != 4 && arguments.length != 6) {
      return false;
    }
    var owner = UUID.fromString(arguments[1]);
    var type = BundleType.valueOf(arguments[2].toUpperCase());
    var expiration = Long.valueOf(arguments[3]);
    if (type.isEnterprise() && arguments.length != 6) {
      log().info("If you want to use the Enterprise bundle, you must " +
        "specify a workflow execution limit and a database data limit.");
      return true;
    }
    if (arguments.length == 6) {
      var executionLimit = Long.parseLong(arguments[4]);
      var dataLimit = Long.parseLong(arguments[5]);
      bundleDatabaseTable.updateBundle(Bundle.of(owner,
        BundlePreset.createAndLoad(type), executionLimit,  executionLimit,
        dataLimit));
    } else {
      bundleDatabaseTable.updateBundle(Bundle.of(owner,
        BundlePreset.createAndLoad(type), expiration));
    }
    log().info("You have successfully changed the bundle");
    return true;
  }

  private boolean deleteBundle(String[] arguments) {
    if (arguments.length != 2) {
      return false;
    }
    var owner = UUID.fromString(arguments[1]);
    bundleDatabaseTable.deleteBundle(owner);
    log().info("You have successfully deleted the bundle");
    return true;
  }
}
