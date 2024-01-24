package net.taskwolf.core.user;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.organization.OrganizationDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class UserInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  UserDatabaseTable provideUserDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var userDatabaseTable = UserDatabaseTable.create(connection, keyspace);
    userDatabaseTable.createIfNotExists();
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
    OrganizationDatabaseTable organizationDatabaseTable
  ) {
    var userTargetDatabaseTable = UserTargetDatabaseTable.create(
      connection, keyspace, organizationDatabaseTable);
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
  ProfilePictureDatabaseTable provideProfilePictureDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var profilePictureDatabaseTable = ProfilePictureDatabaseTable.create(
      connection, keyspace);
    profilePictureDatabaseTable.createIfNotExists();
    return profilePictureDatabaseTable;
  }
}
