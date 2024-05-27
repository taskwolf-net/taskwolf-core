package net.taskwolf.core.grafana;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.whitelist.WhitelistConfiguration;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class GrafanaUser {
  private final GrafanaConfiguration configuration;
  private final GrafanaDatabaseTable grafanaDatabaseTable;
  private final WhitelistConfiguration whitelistConfiguration;
  private final UUID ownerId;
  private final HttpClient client = HttpClient.newHttpClient();

  /**
   * Creates the grafana user account
   * @param apiKey The initial api key of the user that is used in the datasource
   */
  public void create(String apiKey) {
    loginUser(configuration.adminName(), configuration.adminPassword())
      .thenAccept(adminToken -> createUserOrganization(apiKey,
        ownerId.toString(), adminToken));
  }

  private void createUserOrganization(
    String apiKey, String name, String adminToken
  ) {
    sendAuthorizedRequest("https://analytics.taskwolf.net/api/orgs", "POST",
      Map.of("name", name), adminToken)
      .thenApply(response -> new JSONObject(response.body()).getInt("orgId"))
      .thenAccept(organizationId -> createUserAccount(apiKey, ownerId.toString(),
        UUID.randomUUID().toString(), organizationId, adminToken));
  }

  private void createUserAccount(
    String apiKey, String username, String password, int organizationId,
    String adminToken
  ) {
    sendAuthorizedRequest("https://analytics.taskwolf.net/api/admin/users",
      "POST", Map.of("name", username, "email", username, "login", username,
        "password", password, "OrgId", organizationId), adminToken)
      .thenApply(response -> new JSONObject(response.body()).getInt("id"))
      .thenAccept(userId -> updateUserRole(organizationId, userId, "Admin", adminToken)
        .thenAccept(value -> loginUser(username, password)
          .thenAccept(userToken -> createDatasource(apiKey, username, password,
            userId, organizationId, userToken))));
  }

  private void createDatasource(
    String apiKey, String username, String password, int userId,
    int organizationId, String userToken
  ) {
    sendAuthorizedRequest("https://analytics.taskwolf.net/api/datasources",
      "POST", Map.of("type", "yesoreyeram-infinity-datasource", "access", "proxy"),
      userToken).thenApply(response ->  new JSONObject(response.body())
        .getJSONObject("datasource")).thenAccept(json ->
      fillDatasource(apiKey, json.getInt("id"), json.getString("uid"), 1,
        organizationId, userToken).thenAccept(value -> createDashboard(username,
          password, json.getInt("id"), json.getString("uid"), userId,
          organizationId, userToken)));
  }

  private void createDashboard(
    String username, String password, int datasourceId, String datasourceUid,
    int userId, int organizationId, String userToken
  ) {
    var futureAccount = sendAuthorizedRequest(
      "https://analytics.taskwolf.net/api/dashboards/import", "POST",
      configuration.dashboard().replace("%DATASOURCE%", datasourceUid), userToken)
      .thenApply(response -> new JSONObject(response.body()))
      .thenApply(json -> GrafanaAccount.create(ownerId, username,
        password, userId, organizationId, datasourceId, datasourceUid, 1,
        json.getInt("dashboardId"), json.getString("uid"), 1,
        json.getString("importedUrl")));
    futureAccount.thenAccept(grafanaDatabaseTable::insertAccount);
    futureAccount.thenAccept(account -> updateUserRole(organizationId, userId,
      "Viewer", userToken));
    futureAccount.thenAccept(account ->
      updateDashboardPermission(account.dashboardUid(), userId, 1, userToken));
  }

  /**
   * Updates the user api key inside the dashboard datasource to
   * keep grafana authorized
   * @param apiKey The new api key of the user
   */
  public void updateApiKey(String apiKey) {
    loginUser(configuration.adminName(), configuration.adminPassword())
      .thenAccept(adminToken -> grafanaDatabaseTable.findAccount(ownerId)
        .thenAccept(account -> updateUserRole(account.organizationId(),
            account.userId(), "Admin", adminToken)
          .thenAccept(value -> loginUser(account.username(), account.password())
            .thenAccept(userToken -> updateApiKey(apiKey, account, userToken)))));
  }

  private void updateApiKey(
    String apiKey, GrafanaAccount account, String userToken
  ) {
    fillDatasource(apiKey, account.datasourceId(), account.datasourceUid(),
        account.datasourceVersion() + 1, account.organizationId(), userToken)
      .thenAccept(value -> updateUserRole(account.organizationId(),
        account.userId(), "Viewer", userToken));
    grafanaDatabaseTable.updateDatasourceVersion(account,
      account.datasourceVersion() + 1);
  }

  private static final String FILL_DATASOURCE_QUERY = "{\"id\":%DATASOURCE_ID%,\"uid\":\"%DATASOURCE_UID%\",\"orgId\":%ORGANIZATION_ID%,\"name\":\"taskwolf-statistics-datasource\",\"type\":\"yesoreyeram-infinity-datasource\",\"typeLogoUrl\":\"public/plugins/yesoreyeram-infinity-datasource/img/icon.svg\",\"access\":\"proxy\",\"url\":\"__IGNORE_URL__\",\"user\":\"\",\"database\":\"\",\"basicAuth\":false,\"basicAuthUser\":\"\",\"withCredentials\":false,\"isDefault\":true,\"jsonData\":{\"allowedHosts\":[\"https://api.taskwolf.net/v1/\"],\"auth_method\":\"bearerToken\",\"global_queries\":[],\"oauthPassThru\":false,\"httpHeaderName1\":\"WHITELIST-KEY\"},\"version\":%DATASOURCE_VERSION%,\"readOnly\":false,\"accessControl\":{\"alert.instances.external:read\":true,\"alert.instances.external:write\":true,\"alert.notifications.external:read\":true,\"alert.notifications.external:write\":true,\"alert.rules.external:read\":true,\"alert.rules.external:write\":true,\"datasources.id:read\":true,\"datasources:delete\":true,\"datasources:query\":true,\"datasources:read\":true,\"datasources:write\":true},\"secureJsonData\":{\"bearerToken\":\"%API-KEY%\",\"httpHeaderValue1\":\"%WHITELIST-KEY%\"}}";

  private CompletableFuture<Void> fillDatasource(
    String apiKey, int datasourceId, String datasourceUid, int datasourceVersion,
    int organizationId, String userToken
  ) {
    var query = FILL_DATASOURCE_QUERY
      .replace("%DATASOURCE_ID%", String.valueOf(datasourceId))
      .replace("%DATASOURCE_UID%", datasourceUid)
      .replace("%DATASOURCE_VERSION%", String.valueOf(datasourceVersion))
      .replace("%ORGANIZATION_ID%", String.valueOf(organizationId))
      .replace("%API-KEY%", apiKey)
      .replace("%WHITELIST-KEY%", whitelistConfiguration.whitelistKey());
    return sendAuthorizedRequest("https://analytics.taskwolf.net/api/datasources/uid/" +
      datasourceUid, "PUT", query, userToken).thenAccept(value -> {});
  }

  /**
   * Is used to log in to the account and receive the access token
   * @return A future that contains the access token
   */
  public CompletableFuture<String> login() {
    return grafanaDatabaseTable.findAccount(ownerId).thenCompose(account ->
      loginUser(account.username(), account.password()));
  }

  /**
   * Deletes the grafana user account
   */
  public void delete() {
    loginUser(configuration.adminName(), configuration.adminPassword())
      .thenAccept(adminToken -> grafanaDatabaseTable.findAccount(ownerId)
        .thenAccept(account -> deleteUserOrganization(account, adminToken)));
  }

  private void deleteUserOrganization(GrafanaAccount account, String adminToken) {
    sendAuthorizedRequest("https://analytics.taskwolf.net/api/orgs/" +
      account.organizationId(), "DELETE", "", adminToken)
      .thenAccept(response -> deleteUserAccount(account, adminToken));
  }

  private void deleteUserAccount(GrafanaAccount account, String adminToken) {
    sendAuthorizedRequest("https://analytics.taskwolf.net/api/admin/users/" +
      account.userId(), "DELETE", "", adminToken)
      .thenAccept(response -> grafanaDatabaseTable.deleteAccount(ownerId));
  }

  private CompletableFuture<Void> updateUserRole(
    int organizationId, int userId, String role, String token
  ) {
    return sendAuthorizedRequest("https://analytics.taskwolf.net/api/orgs/" +
        organizationId + "/users/" + userId,
      "PATCH", Map.of("role", role), token).thenAccept(response -> {});
  }

  private static final String DASHBOARD_PERMISSION_QUERY = "{ \"items\": [ { \"role\": \"Viewer\", \"permission\": 1 }, { \"role\": \"Editor\", \"permission\": 2 }, { \"role\": \"Admin\", \"permission\": 4 }, { \"userId\": %s, \"permission\": %s } ] }";

  private CompletableFuture<Void> updateDashboardPermission(
    String dashboardUid, int userId, int role, String token
  ) {
    return sendAuthorizedRequest("https://analytics.taskwolf.net/api/" +
        "dashboards/uid/" + dashboardUid + "/permissions",
      "POST", String.format(DASHBOARD_PERMISSION_QUERY, userId, role), token)
        .thenAccept(response -> {});
  }

  private CompletableFuture<String> loginUser(String username, String password) {
    return sendUnauthorizedRequest("https://analytics.taskwolf.net/login",
      "POST", Map.of("user", username, "password", password), Maps.newHashMap())
      .thenApply(response -> response.headers().allValues("Set-Cookie").stream()
        .filter(entry -> entry.contains("grafana_session")).findFirst().get()
        .split(";")[0].replace("grafana_session=", ""));
  }

  private CompletableFuture<HttpResponse<String>> sendAuthorizedRequest(
    String url, String method, Map<String, Object> body, String token
  ) {
    return sendAuthorizedRequest(url, method, new JSONObject(body).toString(),
      token);
  }

  private CompletableFuture<HttpResponse<String>> sendAuthorizedRequest(
    String url, String method, String body, String token
  ) {
    return sendUnauthorizedRequest(url, method, body, Map.of("Cookie",
      "grafana_session=" + token));
  }

  private CompletableFuture<HttpResponse<String>> sendUnauthorizedRequest(
    String url, String method, Map<String, Object> body, Map<String, String> headers
  ) {
    return sendUnauthorizedRequest(url, method, new JSONObject(body).toString(),
      headers);
  }

  private CompletableFuture<HttpResponse<String>> sendUnauthorizedRequest(
    String url, String method, String body, Map<String, String> headers
  ) {
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).method(
      method, !body.isEmpty() ? HttpRequest.BodyPublishers.ofString(body) :
        HttpRequest.BodyPublishers.noBody());
    for (var header : headers.entrySet()) {
      requestBuilder.setHeader(header.getKey(), header.getValue());
    }
    if (!body.isEmpty()) {
      requestBuilder.setHeader("Content-Type", "application/json");
    }
    var httpRequest = requestBuilder.build();
    return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
  }
}
