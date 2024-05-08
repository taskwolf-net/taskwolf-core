package net.taskwolf.core.trigger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class TriggerRepositoryTest {
  private class ExampleTrigger implements Trigger {
    @Override
    public String type() {
      return "-";
    }

    @Override
    public TriggerInformation information() {
      return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content) {
      return null;
    }

    @Override
    public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
      return null;
    }

    @Override
    public CompletableFuture<List<UUID>> findEntries(String condition) {
      return null;
    }

    @Override
    public CompletableFuture<Void> delete(UUID triggerId) {
      return null;
    }
  }

  @Test
  void testTriggerRepository() {
    var repository = TriggerRepository.create();
    var exampleTrigger = new ExampleTrigger();
    repository.registerTrigger(exampleTrigger);
    var currentRegisteredTriggers = repository.allTriggers();
    Assertions.assertEquals(currentRegisteredTriggers.size(), 1);
    Assertions.assertEquals(currentRegisteredTriggers.get(0), exampleTrigger);
    Assertions.assertFalse(repository.isEmpty());
    repository.unregisterTrigger(exampleTrigger);
    Assertions.assertEquals(repository.allTriggers().size(), 0);
    Assertions.assertTrue(repository.isEmpty());
  }
}
