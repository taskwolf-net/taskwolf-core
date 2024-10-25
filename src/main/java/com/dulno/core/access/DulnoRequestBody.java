package com.dulno.core.access;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class DulnoRequestBody {
  public static DulnoRequestBody of(
    String payload, HttpServletResponse response
  ) {
    return create(new JSONObject(payload), response);
  }

  private final JSONObject body;
  private final HttpServletResponse response;

  /**
   * Is used to get a string from request body
   * @param key The key to find the content
   * @return The value behind the key
   */
  public String getString(String key) {
    if (!body.has(key)) {
      failure();
      return "";
    }
    return body.getString(key);
  }

  /**
   * Is used to get a string from request body
   * @param key The key to find the content
   * @param maxLength The maximum length of the content (if the content is longer,
   *                 it is split, so it matches the maximum length)
   * @return The value behind the key
   */
  public String getString(String key, int maxLength) {
    var value = getString(key);
    return value.substring(0, Math.min(maxLength, value.length()));
  }

  /**
   * Is used to get an integer from request body
   * @param key The key to find the content
   * @return The value behind the key
   */
  public int getInt(String key) {
    if (!body.has(key)) {
      failure();
      return -1;
    }
    return body.getInt(key);
  }

  /**
   * Is used to get a double from request body
   * @param key The key to find the content
   * @return The value behind the key
   */
  public double getDouble(String key) {
    if (!body.has(key)) {
      failure();
      return -1;
    }
    return body.getDouble(key);
  }

  /**
   * Is used to get a long from request body
   * @param key The key to find the content
   * @return The value behind the key
   */
  public long getLong(String key) {
    if (!body.has(key)) {
      failure();
      return -1;
    }
    return body.getLong(key);
  }

  /**
   * Is used to get a boolean from request body
   * @param key The key to find the content
   * @return The value behind the key
   */
  public boolean getBoolean(String key) {
    if (!body.has(key)) {
      failure();
      return false;
    }
    return body.getBoolean(key);
  }

  /**
   * Is used to get an uuid from request body
   * @param key The key to find the content
   * @return The value behind the key
   */
  public UUID getUUID(String key) {
    var value = getString(key);
    try {
      return UUID.fromString(value);
    } catch (Exception exception) {
      failure();
      return null;
    }
  }

  /**
   * Is used to get a sub object from request body
   * @param key The key to find the content
   * @return The value behind the key
   */
  public DulnoRequestBody getObject(String key) {
    if (!body.has(key)) {
      failure();
      return DulnoRequestBody.create(new JSONObject(), response);
    }
    return DulnoRequestBody.create(body.getJSONObject(key), response);
  }

  /**
   * Is used to get an object list / array from request body
   * @param key The key to find the content
   * @return The value behind the key
   */
  public List<DulnoRequestBody> getObjectList(String key) {
    if (!body.has(key)) {
      failure();
      return Lists.newArrayList();
    }
    var array = body.getJSONArray(key);
    var result = Lists.<DulnoRequestBody>newArrayList();
    for (var i = 0; i < array.length(); i++) {
      result.add(DulnoRequestBody.create(array.getJSONObject(i), response));
    }
    return result;
  }

  /**
   * Checks whether the body contains a certain key
   * @param key The key that should be checked
   * @return Is true if body contains key, otherwise false
   */
  public boolean has(String key) {
    return body.has(key);
  }

  /**
   * The raw content of the body
   * @return The raw json object
   */
  public JSONObject raw() {
    return body;
  }

  private void failure() {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setContentLength(0);
  }
}
