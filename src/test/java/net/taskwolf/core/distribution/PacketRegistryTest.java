package net.taskwolf.core.distribution;

import com.google.inject.Guice;
import net.taskwolf.core.CoreInjectionModule;
import net.taskwolf.core.packet.PacketRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class PacketRegistryTest {
  @Test
  void testPacketRegistry() throws Exception {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    var registry = injector.getInstance(PacketRegistry.class);
    registry.registerPacket(ExamplePacketIncoming.class);
    var packetSearch = registry.findPacket(0);
    Assertions.assertTrue(packetSearch.isPresent());
    Assertions.assertEquals(packetSearch.get(), ExamplePacketIncoming.class);
    registry.unregisterPacket(0);
  }
}
