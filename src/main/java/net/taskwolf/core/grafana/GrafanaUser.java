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
      .thenAccept(userId -> switchUserContext(organizationId, adminToken)
        .thenAccept(value -> createDatasource(apiKey, username, password, userId,
          organizationId, adminToken)));
  }

  private void createDatasource(
    String apiKey, String username, String password, int userId,
    int organizationId, String adminToken
  ) {
    sendAuthorizedRequest("https://analytics.taskwolf.net/api/datasources",
      "POST", Map.of("type", "yesoreyeram-infinity-datasource", "access", "proxy"),
      adminToken).thenApply(response ->  new JSONObject(response.body())
        .getJSONObject("datasource").getString("uid")).thenAccept(datasourceUid ->
      fillDatasource(apiKey, datasourceUid, 2, adminToken).thenAccept(value ->
        createDashboard(username, password, datasourceUid, userId, organizationId,
          adminToken)));
  }

  private void createDashboard(
    String username, String password, String datasourceUid, int userId,
    int organizationId, String adminToken
  ) {
    sendAuthorizedRequest("https://analytics.taskwolf.net/api/dashboards/import",
      "POST", configuration.dashboard().replace("%DATASOURCE%", datasourceUid),
      adminToken)
      .thenApply(response -> new JSONObject(response.body()).getString("importedUrl"))
      .thenAccept(dashboardUrl -> grafanaDatabaseTable.insertAccount(ownerId,
        username, password, userId, organizationId, datasourceUid, 2, dashboardUrl))
      .thenAccept(value -> switchUserContext(1, adminToken));
  }

  public void updateApiKey(String apiKey) {
    loginUser(configuration.adminName(), configuration.adminPassword())
      .thenAccept(adminToken -> grafanaDatabaseTable.findAccount(ownerId)
        .thenAccept(account -> switchUserContext(account.organizationId(), adminToken)
          .thenAccept(value -> updateApiKey(apiKey, account, adminToken))));
  }

  private void updateApiKey(
    String apiKey, GrafanaAccount account, String adminToken
  ) {
    fillDatasource(apiKey, account.datasourceUid(),
      account.datasourceVersion() + 1, adminToken)
      .thenAccept(value -> switchUserContext(1, adminToken));
    grafanaDatabaseTable.updateDatasourceVersion(account,
      account.datasourceVersion() + 1);
  }

  private static final String FILL_DATASOURCE_QUERY = "{\"id\":%DATASOURCE_ID%,\"uid\":\"%DATASOURCE_UID%\",\"orgId\":1,\"name\":\"taskwolf-statistics-datasource\",\"type\":\"yesoreyeram-infinity-datasource\",\"typeLogoUrl\":\"public/plugins/yesoreyeram-infinity-datasource/img/icon.svg\",\"access\":\"proxy\",\"url\":\"__IGNORE_URL__\",\"user\":\"\",\"database\":\"\",\"basicAuth\":false,\"basicAuthUser\":\"\",\"withCredentials\":false,\"isDefault\":true,\"jsonData\":{\"allowedHosts\":[\"https://api.taskwolf.net/v1/\"],\"auth_method\":\"bearerToken\",\"global_queries\":[],\"oauthPassThru\":false,\"httpHeaderName1\":\"WHITELIST-KEY\"},\"secureJsonFields\":{\"bearerToken\":false,\"httpHeaderValue1\":true,\"httpHeaderValue2\":true},\"version\":%DATASOURCE_VERSION%,\"readOnly\":false,\"accessControl\":{\"alert.instances.external:read\":true,\"alert.instances.external:write\":true,\"alert.notifications.external:read\":true,\"alert.notifications.external:write\":true,\"alert.rules.external:read\":true,\"alert.rules.external:write\":true,\"datasources.id:read\":true,\"datasources:delete\":true,\"datasources:query\":true,\"datasources:read\":true,\"datasources:write\":true},\"secureJsonData\":{\"bearerToken\":\"%API-KEY%\",\"httpHeaderValue1\":\"%WHITELIST-KEY%\"}}";

  private CompletableFuture<Void> fillDatasource(
    String apiKey, String datasourceUid, int datasourceVersion,
    String adminToken
  ) {
    var query = FILL_DATASOURCE_QUERY.replace("%DATASOURCE_ID%", "1")
      .replace("%DATASOURCE_UID%", datasourceUid)
      .replace("%DATASOURCE_VERSION%", String.valueOf(datasourceVersion))
      .replace("%API-KEY%", apiKey)
      .replace("%WHITELIST-KEY%", whitelistConfiguration.whitelistKey());
    return sendAuthorizedRequest("https://analytics.taskwolf.net/api/datasources/uid/" +
      datasourceUid, "PUT", query, adminToken).thenAccept(value -> {});
  }

  public CompletableFuture<String> login() {
    return grafanaDatabaseTable.findAccount(ownerId).thenCompose(account ->
      loginUser(account.username(), account.password()));
  }

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

  private CompletableFuture<Void> switchUserContext(int organizationId, String token) {
    return sendAuthorizedRequest("https://analytics.taskwolf.net/api/user/using/" +
      organizationId, "POST", "", token).thenAccept(response -> {});
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
