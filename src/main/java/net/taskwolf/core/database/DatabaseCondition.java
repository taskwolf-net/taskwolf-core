package net.taskwolf.core.database;

import com.beust.jcommander.internal.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class DatabaseCondition {
  public static DatabaseCondition empty() {
    return create(Lists.newArrayList());
  }

  public static DatabaseCondition of(String column, Object value) {
    return create(Lists.newArrayList(DatabaseComparison.create(column, value)));
  }

  public static DatabaseCondition of(
    String column1, Object value1, String column2, Object value2
  ) {
    return create(Lists.newArrayList(DatabaseComparison.create(column1, value1),
      DatabaseComparison.create(column2, value2)));
  }

  public static DatabaseCondition of(
    String column1, Object value1, String column2, Object value2,
    String column3, Object value3
  ) {
    return create(Lists.newArrayList(DatabaseComparison.create(column1, value1),
      DatabaseComparison.create(column2, value2),
      DatabaseComparison.create(column3, value3)));
  }

  public static DatabaseCondition of(DatabaseComparison... comparisons) {
    return create(Lists.newArrayList(comparisons));
  }

  private final List<DatabaseComparison> comparisons;

  /**
   * Is used to build the column and placeholder combination
   * @return The condition string
   */
  public String build() {
    var condition = new StringBuilder();
    for (var i = 0; i < comparisons.size(); i++) {
      if (i > 0) {
        condition.append(" AND ");
      }
      condition.append(comparisons.get(i).build());
    }
    return condition.toString();
  }

  /**
   * Adds the comparisons of the other condition to this condition
   * @param other The other condition
   */
  public void concat(DatabaseCondition other) {
    comparisons.addAll(other.comparisons());
  }

  /**
   * Is used the get the comparisons of the condition
   * @return The list of comparisons
   */
  public List<DatabaseComparison> comparisons() {
    return Lists.newArrayList(comparisons);
  }

  /**
   * Is used to get the values of the condition
   * @return The value array
   */
  public Object[] values() {
    return comparisons.stream().map(DatabaseComparison::value).toArray();
  }
}
