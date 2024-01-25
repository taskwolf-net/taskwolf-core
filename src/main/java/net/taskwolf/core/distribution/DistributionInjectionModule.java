package net.taskwolf.core.distribution;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.user.UserDatabaseTable;

@RequiredArgsConstructor(staticName = "create")
public final class DistributionInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  DistributionConfiguration provideDistributionConfiguration() throws Exception {
    return DistributionConfiguration.createAndLoad();
  }

  @Provides
  @Singleton
  Distribution provideDistribution(
    DistributionConfiguration distributionConfiguration,
    UserDatabaseTable userDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable
  ) {
    var distribution = Distribution.create(distributionConfiguration,
      userDatabaseTable, organizationDatabaseTable);
    distribution.initialize();
    return distribution;
  }
}
