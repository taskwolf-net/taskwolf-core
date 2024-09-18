package com.dulno.core.user;

import com.dulno.core.bundle.BundleDatabaseTable;
import com.dulno.core.user.activity.UserActivityDatabaseTable;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.organization.OrganizationDatabaseTable;
import com.dulno.core.target.TargetIdentificationPublish;
import com.dulno.core.user.mfa.MultiFactorAuthDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class UserInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  UserDatabaseTable provideUserDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    TargetIdentificationPublish targetIdentificationPublish
  ) {
    var userDatabaseTable = UserDatabaseTable.create(connection, keyspace,
      targetIdentificationPublish);
    userDatabaseTable.createIfNotExists();
    userDatabaseTable.createIndexIfNotExists("email");
    return userDatabaseTable;
  }

  @Provides
  @Singleton
  UserVerificationDatabaseTable provideUserVerificationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var userVerificationDatabaseTable = UserVerificationDatabaseTable.create(
      connection, keyspace);
    userVerificationDatabaseTable.createIfNotExists();
    return userVerificationDatabaseTable;
  }

  @Provides
  @Singleton
  UserTargetDatabaseTable provideUserTargetDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    UserDatabaseTable userDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable,
    BundleDatabaseTable bundleDatabaseTable
  ) {
    var userTargetDatabaseTable = UserTargetDatabaseTable.create(connection,
      keyspace, userDatabaseTable, organizationDatabaseTable,
      bundleDatabaseTable);
    userTargetDatabaseTable.createIfNotExists();
    return userTargetDatabaseTable;
  }

  @Provides
  @Singleton
  UserPasswordResetDatabaseTable provideUserPasswordResetDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var userPasswordResetDatabaseTable = UserPasswordResetDatabaseTable.create(
      connection, keyspace);
    userPasswordResetDatabaseTable.createIfNotExists();
    return userPasswordResetDatabaseTable;
  }

  @Provides
  @Singleton
  UserEmailChangeDatabaseTable provideUserEmailChangeDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var userEmailChangeDatabaseTable = UserEmailChangeDatabaseTable.create(
      connection, keyspace);
    userEmailChangeDatabaseTable.createIfNotExists();
    return userEmailChangeDatabaseTable;
  }

  @Provides
  @Singleton
  UserActivityDatabaseTable provideUserActivityDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var userActivityDatabaseTable = UserActivityDatabaseTable.create(
      connection, keyspace);
    userActivityDatabaseTable.createIfNotExists();
    userActivityDatabaseTable.createIndexIfNotExists("user");
    return userActivityDatabaseTable;
  }

  @Provides
  @Singleton
  MultiFactorAuthDatabaseTable provideMultiFactorAuthDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var multiFactorAuthDatabaseTable = MultiFactorAuthDatabaseTable.create(
      connection, keyspace);
    multiFactorAuthDatabaseTable.createIfNotExists();
    return multiFactorAuthDatabaseTable;
  }
}
