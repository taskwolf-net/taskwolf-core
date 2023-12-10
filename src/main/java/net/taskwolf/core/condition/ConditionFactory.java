package net.taskwolf.core.condition;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.condition.text.ConditionTextEndsWith;
import net.taskwolf.core.condition.text.ConditionTextEquals;
import org.json.JSONObject;

@RequiredArgsConstructor(staticName = "create")
public class ConditionFactory {
  public Condition create(String content) {
    var json = new JSONObject(content);
    var type = json.getString("type");
    var inputValue = json.getString("inputValue");
    var comparativeValue = json.getString("comparativeValue");
    if (type.equals("condition-text-equals")) {
      return ConditionTextEquals.create(inputValue, comparativeValue);
    }
    if (type.equals("condition-text-ends-with")) {
      return ConditionTextEndsWith.create(inputValue, comparativeValue);
    }
    return null;
  }
}