package com.dulno.core.sale;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class SaleInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  SaleDatabaseTable provideSaleDatabaseTable(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var saleDatabaseTable = SaleDatabaseTable.create(
      databaseConnection, databaseKeyspace);
    saleDatabaseTable.createIfNotExists();
    saleDatabaseTable.createIndexIfNotExists("sender");
    saleDatabaseTable.createIndexIfNotExists("status");
    saleDatabaseTable.createIndexIfNotExists("expirationTime");
    return saleDatabaseTable;
  }

  @Provides
  @Singleton
  SaleMessageDatabaseTable provideSaleMessageDatabaseTable(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var saleMessageDatabaseTable = SaleMessageDatabaseTable.create(
      databaseConnection, databaseKeyspace);
    saleMessageDatabaseTable.createIfNotExists();
    saleMessageDatabaseTable.createIndexIfNotExists("publicId");
    saleMessageDatabaseTable.createIndexIfNotExists("saleId");
    return saleMessageDatabaseTable;
  }
}
