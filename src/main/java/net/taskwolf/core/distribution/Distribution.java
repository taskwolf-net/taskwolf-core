package net.taskwolf.core.distribution;

import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Distribution {
  private UUID localIdentifier;
  private Jedis jedis;

  public void initialize() {
    localIdentifier = UUID.randomUUID();
    jedis = new Jedis();
    jedis.connect();
  }

  private void initializeDatabase() {
    if (!jedis.exists("taskwolf-nodes")) {
      jedis.lpush("taskwolf-nodes", localIdentifier.toString());
    }

  }

  public void registerModule(String name) {

  }

  public List<UUID> findAssignedUsers(String moduleName) {
    return Lists.newArrayList();
  }

  public void destroy() {

  }

  private List<UUID> findNodes() {
    return jedis.lrange("taskwolf-nodes", 0, jedis.llen("taskwolf-nodes")).stream()
      .map(UUID::fromString).filter(node -> !node.equals(localIdentifier))
      .collect(Collectors.toList());
  }
}
