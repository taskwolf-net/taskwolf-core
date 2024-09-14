package com.dulno.core.organization;

import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.target.TargetIdentificationPublish;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.organization.team.TeamDatabaseTable;

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
