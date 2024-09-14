package com.dulno.core.event;

import com.google.inject.Guice;
import com.dulno.core.CoreInjectionModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class HookRegistryTest {
  private class ExampleHook implements Hook {

  }

  @Test
  void testHookRegistry() {
    var injector = Guice.createInjector(CoreInjectionModule.create());
    var registry = injector.getInstance(HookRegistry.class);
    var hook = new ExampleHook();
    registry.register(hook);
    Assertions.assertEquals(registry.findAll().size(), 1);
    var hookSearch = registry.findByClass(ExampleHook.class);
    Assertions.assertTrue(hookSearch.isPresent());
    Assertions.assertEquals(hookSearch.get(), hook);
    registry.unregister(hook);
    Assertions.assertEquals(registry.findAll().size(), 0);
  }
}
