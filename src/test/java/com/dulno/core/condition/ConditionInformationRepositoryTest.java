package com.dulno.core.condition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class ConditionInformationRepositoryTest {
  @Test
  void testConditionInformationRepository() {
    var repository = ConditionInformationRepository.create();
    var testConditionInformation = ConditionInformation.builder()
      .withName("Test")
      .withDataType(ConditionDataType.TEXT)
      .withIdentifier("test-identifier")
      .build();
    repository.register(testConditionInformation);
    var conditionSearch = repository.findByIdentifier("test-identifier");
    Assertions.assertTrue(conditionSearch.isPresent());
    Assertions.assertEquals(conditionSearch.get(), testConditionInformation);
    Assertions.assertEquals(repository.findAll().size(), 1);
    repository.unregister(testConditionInformation);
    Assertions.assertTrue(repository.findAll().isEmpty());
  }
}
