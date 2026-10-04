package net.taskwolf.core.bundle;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(staticName = "create")
public final class BundlePresetRepository {
  private final List<BundlePreset> presets = Lists.newArrayList();

  public void registerPreset(BundlePreset preset) {
    presets.add(preset);
  }

  public void unregisterPreset(BundlePreset preset) {
    presets.remove(preset);
  }

  public Optional<BundlePreset> findPreset(BundleType bundleType) {
    return presets.stream().filter(preset -> preset.bundleType().equals(bundleType))
      .findFirst();
  }

  public Optional<BundlePreset> findPreset(
    BundleType bundleType, BundleClass bundleClass
  ) {
    return presets.stream()
      .filter(preset -> preset.bundleType().equals(bundleType))
      .filter(preset -> preset.bundleClass().equals(bundleClass))
      .findFirst();
  }
}
