package net.taskwolf.core.event;

import com.google.inject.Guice;
import net.taskwolf.core.CoreInjectionModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class EventExecutionTest {
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
  void testHookRegistry() throws Exception {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    var registry = injector.getInstance(HookRegistry.class);
    var hook = new ExampleHook();
    registry.register(hook);
    var executor = injector.getInstance(EventExecutor.class);
    executor.executeUncaught(new ExampleEvent("Test"));
  }
}
