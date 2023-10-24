package net.taskwolf.core.command.implementation;


import net.taskwolf.core.command.Command;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.log.Log;

import java.util.List;

public final class DistributionCommand extends Command {
  public static DistributionCommand create(
    Log log, DistributionConfiguration distributionConfiguration,
    Distribution distribution
  ) {
    return new DistributionCommand(log, distributionConfiguration, distribution);
  }

  private final DistributionConfiguration distributionConfiguration;
  private final Distribution distribution;

  private DistributionCommand(
    Log log, DistributionConfiguration distributionConfiguration,
    Distribution distribution
  ) {
    super(log, "distribution", new String[] {"cluster"}, new String[0]);
    this.distributionConfiguration = distributionConfiguration;
    this.distribution = distribution;
  }

  @Override
  public boolean execute(String[] arguments) {
    distribution.findConnectedNodes().thenAccept(this::printDistribution);
    return true;
  }

  private static final String COLOR_RESET = "\u001b[32m";
  private static final String COLOR_RED = "\u001b[31m";
  private static final String COLOR_GREEN = "\u001b[32m";

  private void printDistribution(List<String> connectedNodes) {
    var self = distributionConfiguration.self();
    log().info("Self (" + self.hostname()  + ":" + self.redisPort() + "): " +
      COLOR_GREEN + "CONNECTED" + COLOR_RESET);
    var nodes = distributionConfiguration.nodes();
    log().info("Nodes (" + nodes.size() + "):");
    for (var node : nodes) {
      var address = node.hostname() + ":" + node.redisPort();
      log().info(" - " + address + " " + (connectedNodes.contains(address) ?
        COLOR_GREEN + "CONNECTED" : COLOR_RED + "DISCONNECTED") + COLOR_RESET);
    }
  }
}
