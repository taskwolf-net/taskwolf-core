package net.taskwolf.core.distribution;

import com.google.inject.Guice;
import net.taskwolf.core.CoreInjectionModule;
import net.taskwolf.core.distribution.packet.PacketEventRepository;
import net.taskwolf.core.event.Event;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class PacketEventRepositoryTest {
  private class ExampleEvent extends Event {
    private final String value;

    public ExampleEvent(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }
  }

  private class ExampleHook implements Hook {
    @EventHook
    private void example(ExampleEvent event) {
      Assertions.assertEquals(event.value(), "Test");
    }
  }

  @Test
  void testPacketEventRepository() {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    var repository = injector.getInstance(PacketEventRepository.class);
    var exampleEvent = new ExampleEvent("Test");
    repository.registerEvent(ExamplePacketIncoming.class, (client, packet) ->
      exampleEvent);
    var eventSearch = repository.findEvent(ExamplePacketIncoming.class);
    Assertions.assertTrue(eventSearch.isPresent());
    repository.unregisterEvent(ExamplePacketIncoming.class);
  }
}
