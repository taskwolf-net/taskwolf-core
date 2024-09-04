package net.taskwolf.core.target;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseCondition;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TargetIdentificationPublish {
  private final DatabaseConnection connection;
  private final DatabaseKeyspace keyspace;

  /**
   * Is used to generate an ID that does not exist as a user, organization or
   * organization team. The ID must be unique in order to guarantee
   * the functionality of the owner within different structures.
   * This class was outsourced due to circular dependencies.
   * @return The target id
   */
  public CompletableFuture<UUID> generateAvailableTargetId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    isIdentificationUsed(id).thenApply(used -> used ?
      generateAvailableTargetId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  private CompletableFuture<Boolean> isIdentificationUsed(UUID id) {
    return exists("user", DatabaseCondition.of("id", id))
      .thenCompose(userExists -> userExists ?
        CompletableFuture.completedFuture(true) :
        exists("organization", DatabaseCondition.of("id", id))
          .thenCompose(organizationExists -> organizationExists ?
            CompletableFuture.completedFuture(true) :
            exists("organization_team", DatabaseCondition.of("id", id))));
  }

  private CompletableFuture<Boolean> exists(
    String tableName, DatabaseCondition condition
  ) {
    var query = new StringBuilder("SELECT * FROM ");
    query.append(keyspace.name() + "." + tableName);
    query.append(" WHERE ");
    query.append(condition.build());
    query.append(";");
    return connection.execute(query, condition.values())
      .thenApply(result -> result.remaining() > 0);
  }
}
