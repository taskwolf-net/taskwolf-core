package net.taskwolf.core.distribution;

import com.google.inject.Guice;
import net.taskwolf.core.CoreInjectionModule;
import net.taskwolf.core.distribution.client.DistributionClient;
import net.taskwolf.core.distribution.client.DistributionClientRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class DistributionClientRegistryTest {
  @Test
  void testDistributionClientRegistry() {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    var registry = injector.getInstance(DistributionClientRegistry.class);
    var client = DistributionClient.create(null, null, null, null, null, null);
    registry.registerClient(client);
    var allClients = registry.findAllClients();
    Assertions.assertTrue(allClients.contains(client));
    Assertions.assertEquals(allClients.size(), 1);
    registry.unregisterClient(client);
    Assertions.assertTrue(registry.findAllClients().isEmpty());
  }
}
