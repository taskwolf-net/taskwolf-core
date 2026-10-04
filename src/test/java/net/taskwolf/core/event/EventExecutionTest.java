package net.taskwolf.core.event;

import net.taskwolf.core.error.ErrorRepository;
import net.taskwolf.core.log.Log;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Providers;
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

  private class ExampleModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(Log.class).toProvider(Providers.of(null));
      bind(ErrorRepository.class).toProvider(Providers.of(null));
    }
  }

  @Test
  void testEventExecution() throws Exception {
    var injector = Guice.createInjector(new ExampleModule());
    var registry = injector.getInstance(HookRegistry.class);
    var hook = new ExampleHook();
    registry.register(hook);
    var executor = injector.getInstance(EventExecutor.class);
    executor.executeUncaught(new ExampleEvent("Test"));
  }
}
