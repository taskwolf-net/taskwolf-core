package net.taskwolf.core.condition;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(staticName = "create")
public final class ConditionInformationRepository {
  private final List<ConditionInformation> conditions = Lists.newArrayList();

  public void register(ConditionInformation information) {
    conditions.add(information);
  }

  public void unregister(ConditionInformation information) {
    conditions.remove(information);
  }

  public Optional<ConditionInformation> findByIdentifier(String identifier) {
    return conditions.stream().filter(information ->
      information.identifier().equals(identifier)).findFirst();
  }

  public List<ConditionInformation> findAll() {
    return List.copyOf(conditions);
  }
}
