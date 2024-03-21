package net.taskwolf.core.access;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

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

  private void failure() {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setContentLength(0);
  }
}
