package com.dulno.core.distribution;

import com.google.inject.Guice;
import com.dulno.core.CoreInjectionModule;
import com.dulno.core.packet.PacketEventRepository;
import com.dulno.core.event.Event;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
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
