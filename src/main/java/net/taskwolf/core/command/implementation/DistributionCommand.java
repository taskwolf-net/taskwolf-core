package net.taskwolf.core.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.log.Log;

@Singleton
public final class DistributionCommand extends Command {
  private final DistributionConfiguration distributionConfiguration;
  private final Distribution distribution;

  @Inject
  private DistributionCommand(
    Log log, DistributionConfiguration distributionConfiguration,
    Distribution distribution
  ) {
    super(log, "distribution", new String[] {"cluster"}, new String[0]);
    this.distributionConfiguration = distributionConfiguration;
    this.distribution = distribution;
  }

  private static final String COLOR_RESET = "\u001b[32m";
  private static final String COLOR_RED = "\u001b[31m";
  private static final String COLOR_GREEN = "\u001b[32m";

  @Override
  public boolean execute(String[] arguments) {
    var connectedNodes = distribution.findConnectedNodes();
    var self = distributionConfiguration.self();
    log().info("Self " + self.information() + ": " + COLOR_GREEN + "CONNECTED" +
      COLOR_RESET);
    var nodes = distributionConfiguration.nodes();
    log().info("Nodes (" + nodes.size() + "):");
    for (var node : nodes) {
      var isConnected = connectedNodes.stream().anyMatch(connectedNode ->
        connectedNode.contains(node.hostname()));
      log().info(" - " + node.information() + " " + (isConnected ?
        COLOR_GREEN + "CONNECTED" : COLOR_RED + "DISCONNECTED") + COLOR_RESET);
    }
    return true;
  }
}
