package net.taskwolf.core.condition;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.condition.number.ConditionNumberGreaterThan;
import net.taskwolf.core.condition.number.ConditionNumberSmallerThan;
import net.taskwolf.core.condition.text.ConditionTextEndsWith;
import net.taskwolf.core.condition.text.ConditionTextEquals;
import net.taskwolf.core.condition.text.ConditionTextStartsWith;
import org.json.JSONObject;

@RequiredArgsConstructor(staticName = "create")
public class ConditionFactory {
  public Condition create(String type, String content) {
    var json = new JSONObject(content);
    var inputValue = json.getString("inputValue");
    var comparativeValue = json.getString("comparativeValue");
    if (type.equals("condition-text-equals")) {
      return ConditionTextEquals.create(inputValue, comparativeValue);
    }
    if (type.equals("condition-text-starts-with")) {
      return ConditionTextStartsWith.create(inputValue, comparativeValue);
    }
    if (type.equals("condition-text-ends-with")) {
      return ConditionTextEndsWith.create(inputValue, comparativeValue);
    }
    if (type.equals("condition-number-is-greater")) {
      return ConditionNumberGreaterThan.create(inputValue, comparativeValue);
    }
    if (type.equals("condition-number-is-smaller")) {
      return ConditionNumberSmallerThan.create(inputValue, comparativeValue);
    }
    return null;
  }
}