package net.taskwolf.core.organization;

import net.taskwolf.core.organization.team.TeamTargetDatabaseTable;
import net.taskwolf.core.target.TargetIdentificationPublish;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.organization.team.TeamDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class OrganizationInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  OrganizationDatabaseTable provideOrganizationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    TargetIdentificationPublish targetIdentificationPublish
  ) {
    var organizationDatabaseTable = OrganizationDatabaseTable.create(connection,
      keyspace, targetIdentificationPublish);
    organizationDatabaseTable.createIfNotExists();
    organizationDatabaseTable.createIndexIfNotExists("owner");
    return organizationDatabaseTable;
  }

  @Provides
  @Singleton
  TeamDatabaseTable provideTeamDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    TargetIdentificationPublish targetIdentificationPublish
  ) {
    var organizationTeamDatabaseTable = TeamDatabaseTable.create(
      connection, keyspace, targetIdentificationPublish);
    organizationTeamDatabaseTable.createIfNotExists();
    organizationTeamDatabaseTable.createIndexIfNotExists("organization");
    return organizationTeamDatabaseTable;
  }

  @Provides
  @Singleton
  TeamTargetDatabaseTable provideTeamTargetDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    TeamDatabaseTable teamDatabaseTable
  ) {
    var teamTargetDatabaseTable = TeamTargetDatabaseTable.create(
      connection, keyspace, teamDatabaseTable);
    teamTargetDatabaseTable.createIfNotExists();
    return teamTargetDatabaseTable;
  }
}
