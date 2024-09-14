package com.dulno.core.action;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class ActionRepositoryTest {
  private class ExampleAction implements Action<ExampleActionExecutor> {
    @Override
    public String type() {
      return "-";
    }

    @Override
    public ActionInformation information() {
      return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
      return null;
    }

    @Override
    public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
      return null;
    }

    @Override
    public CompletableFuture<ExampleActionExecutor> build(UUID actionId) {
      return null;
    }

    @Override
    public CompletableFuture<Void> delete(UUID actionId) {
      return null;
    }
  }

  private class ExampleActionExecutor implements ActionExecutor {
    @Override
    public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
      return null;
    }
  }

  @Test
  void testActionRepository() {
    var repository = ActionRepository.create();
    var exampleAction = new ExampleAction();
    repository.registerAction(exampleAction);
    var currentRegisteredTriggers = repository.allActions();
    Assertions.assertEquals(currentRegisteredTriggers.size(), 1);
    Assertions.assertEquals(currentRegisteredTriggers.get(0), exampleAction);
    Assertions.assertFalse(repository.isEmpty());
    repository.unregisterAction(exampleAction);
    Assertions.assertEquals(repository.allActions().size(), 0);
    Assertions.assertTrue(repository.isEmpty());
  }
}
