package net.taskwolf.core.bundle;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;

@RequiredArgsConstructor(staticName = "create")
public final class BundleInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  BundleDatabaseTable provideBundleDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var bundleDatabaseTable = BundleDatabaseTable.create(connection, keyspace);
    bundleDatabaseTable.createIfNotExists();
    return bundleDatabaseTable;
  }

  @Provides
  @Singleton
  BundlePresetRepository provideBundlePresetRepository() throws Exception {
    var bundlePresetRepository = BundlePresetRepository.create();
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.TRIAL));
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.PROFESSIONAL, BundleClass.BEGINNER));
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.PROFESSIONAL, BundleClass.ADVANCED));
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.PROFESSIONAL, BundleClass.EXPERT));
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.TEAM, BundleClass.BEGINNER));
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.TEAM, BundleClass.ADVANCED));
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.TEAM, BundleClass.EXPERT));
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.ENTERPRISE, BundleClass.BEGINNER));
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.ENTERPRISE, BundleClass.ADVANCED));
    bundlePresetRepository.registerPreset(BundlePreset.createAndLoad(
      BundleType.ENTERPRISE, BundleClass.EXPERT));
    return bundlePresetRepository;
  }
}