package net.taskwolf.core.access;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class TaskwolfRequestBody {
  public static TaskwolfRequestBody of(
    String payload, HttpServletResponse response
  ) {
    return create(new JSONObject(payload), response);
  }

  private final JSONObject body;
  private final HttpServletResponse response;

  public String getString(String key) {
    if (!body.has(key)) {
      failure();
      return "";
    }
    return body.getString(key);
  }

  public int getInt(String key) {
    if (!body.has(key)) {
      failure();
      return -1;
    }
    return body.getInt(key);
  }

  public boolean getBoolean(String key) {
    if (!body.has(key)) {
      failure();
      return false;
    }
    return body.getBoolean(key);
  }

  public UUID getUUID(String key) {
    var value = getString(key);
    try {
      return UUID.fromString(value);
    } catch (Exception exception) {
      failure();
      return null;
    }
  }

  public TaskwolfRequestBody getObject(String key) {
    if (!body.has(key)) {
      failure();
      return TaskwolfRequestBody.create(new JSONObject(), response);
    }
    return TaskwolfRequestBody.create(body.getJSONObject(key), response);
  }

  public List<TaskwolfRequestBody> getObjectList(String key) {
    if (!body.has(key)) {
      failure();
      return Lists.newArrayList();
    }
    var array = body.getJSONArray(key);
    var result = Lists.<TaskwolfRequestBody>newArrayList();
    for (var i = 0; i < array.length(); i++) {
      result.add(TaskwolfRequestBody.create(array.getJSONObject(i), response));
    }
    return result;
  }

  private void failure() {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setContentLength(0);
  }
}
