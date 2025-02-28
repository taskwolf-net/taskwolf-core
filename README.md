<div align="center">
  <img src="https://dulno.com/static/img/logo-light.webp" alt="logo" width="128"  height="auto" />

  <h1><b>Dulno - Core</b><br><br></h1>

</div>

Core of the backend of Dulno. Each module relies on the core. It bundles central functionalities and forms the framework of the entire application.

## Status

|      | Pipeline status                                                       |
|------|-----------------------------------------------------------------------|
| main | ![](https://git.dulno.com/dulno/dulno-core/badges/main/pipeline.svg)  |
| dev  | ![](https://git.dulno.com/dulno/dulno-core/badges/dev/pipeline.svg)   |

## Architecture

[![](https://mermaid.ink/img/pako:eNqVmU1v4zYQhv-KoVMCJIE9Q8mJDjlkF2gLpEXh7Kl2DlqZa6u1pVSWiqRB_vvalOSI4gxF7skUn5cfM_OSWuU9SIu1DOIgKdNtVsm0qkt5_V1WySqfHP8dZPlflspJKf-t5aG6yPJKlrmsLpeL5snzKm_ITVnUL5NtcTgCFyeZLC-Xv6rmsz7WrkjWD8kuydMjme6Ken25fDw-m3QPnydZ3o7UKPszpLtaTdEKvzRNTdLHm5XMfjk1lk-qMZkpuh1IX1uLnzegKfpjkbLf8k0pD4dubW3TVf3HJstfO61quCq_JlXyPTnIi3X743LZPXId4ktRys9tn1o-StCV4K5EXYmE0kwnaOmEsXTCMJ399QG7PmDS6agm0umotKXTcYg_y-L1rZtcNdx0I2UworSUwYjSUgbAlgFqZYBjZYDDMujPguz6kCkDRzVRBo5KWxk4DjGSzhGlJZ0jSks6UUtne6_ED5Pr6_vJt7h_N3TM33WeVlmRn388yh_VbHhHkBQ4UehECScqdKKiUWqRbbYOm1QYuGHohgk3LHTDIvNi1tIYL46ZnzwOEj_EoMM0rckhxYHJCYpDkwspTphcRHEhxXWF3l5xnw46NlTnZ1970OhIO8_g4teYVjcYTR09GqeexI8nahH3XwMISh9LHQ4EpS9OHQRd0vv5HQRBGwn0qYAIAuhrBioIoK-luQ81gIwSGFECIkpgRAmIKIERJSCiBFqUdPd3s_LeaOzfcbqaIJEkgSAFSSJBhiQpCDIiyZAk9QrBfhRRDzMSFYJ6vpCqECQLAI0CQKIA0CgAJAoAjQJAogCQLIDzj9lTz5jGC46Bgx-Ofrjww_trBz8c_baKfltFv63quHbuqUio4rjvFbaWNH0CraurFzJ7ug5sOuR1WtfwlhJP3KmvUnZ2IZlQfl_GgSTc4mHc8lrOjPU1l_19j5-ZPNllizvycTd0gtdpS7fFfdT64Gd98LM--Fkf_KwPftYHP-uDn_XBz_pgtz5YrQ98qYPN-sBbH2zWB976YLM-8SpjtT7w1geb9cfiAbb1obE-1vrAWx9s1gfe-rQOeZ3WZYvHqPXRz_roZ330sz76WR_9rI9-1kc_66Of9dFufbRaH_lSR5v1kbc-2qyPvPXRZn3iJdZqfeStjzbrj8WDu_XRsD5arY-89dFmfeStj7ZbH3nro-3Wd3nhz1J5-lzgaAdnGr1o4UWHXnS3S_XfPfdtOuLohws_PGRxj1fAfpqJmm17GGuwt2kn-3aSPcRUlRCFzqiQVaFFJViVGKrue7KQlfV7miOG-fxFChbMpyLbeTk4x7RapYOuuoY6HHklOeuYbBHzgU2HvA5tOsHrhKGjckYIQzMw1Lc4Gl-QH2-Cq2Avy32SrYM4eD_pV0G1lXu5CuLjz3VS_rMKVvnHkUvqqnh6y9MgrspaXgVlUW-2Qfwj2R2OrfplnVTya5ZsymTfIS9J_ldR7M-QXGdVUf7e_HFc_Y1cMUH8HrwGMUzxBlDcTe-m4TyazeEqeAtiFOHN7UzcirvbEMNZGH1cBf-rQac3UYTRFKL5_O4W52EYfvwESivj4g?type=png)](https://mermaid.live/edit#pako:eNqVmU1v4zYQhv-KoVMCJIE9Q8mJDjlkF2gLpEXh7Kl2DlqZa6u1pVSWiqRB_vvalOSI4gxF7skUn5cfM_OSWuU9SIu1DOIgKdNtVsm0qkt5_V1WySqfHP8dZPlflspJKf-t5aG6yPJKlrmsLpeL5snzKm_ITVnUL5NtcTgCFyeZLC-Xv6rmsz7WrkjWD8kuydMjme6Ken25fDw-m3QPnydZ3o7UKPszpLtaTdEKvzRNTdLHm5XMfjk1lk-qMZkpuh1IX1uLnzegKfpjkbLf8k0pD4dubW3TVf3HJstfO61quCq_JlXyPTnIi3X743LZPXId4ktRys9tn1o-StCV4K5EXYmE0kwnaOmEsXTCMJ399QG7PmDS6agm0umotKXTcYg_y-L1rZtcNdx0I2UworSUwYjSUgbAlgFqZYBjZYDDMujPguz6kCkDRzVRBo5KWxk4DjGSzhGlJZ0jSks6UUtne6_ED5Pr6_vJt7h_N3TM33WeVlmRn388yh_VbHhHkBQ4UehECScqdKKiUWqRbbYOm1QYuGHohgk3LHTDIvNi1tIYL46ZnzwOEj_EoMM0rckhxYHJCYpDkwspTphcRHEhxXWF3l5xnw46NlTnZ1970OhIO8_g4teYVjcYTR09GqeexI8nahH3XwMISh9LHQ4EpS9OHQRd0vv5HQRBGwn0qYAIAuhrBioIoK-luQ81gIwSGFECIkpgRAmIKIERJSCiBFqUdPd3s_LeaOzfcbqaIJEkgSAFSSJBhiQpCDIiyZAk9QrBfhRRDzMSFYJ6vpCqECQLAI0CQKIA0CgAJAoAjQJAogCQLIDzj9lTz5jGC46Bgx-Ofrjww_trBz8c_baKfltFv63quHbuqUio4rjvFbaWNH0CraurFzJ7ug5sOuR1WtfwlhJP3KmvUnZ2IZlQfl_GgSTc4mHc8lrOjPU1l_19j5-ZPNllizvycTd0gtdpS7fFfdT64Gd98LM--Fkf_KwPftYHP-uDn_XBz_pgtz5YrQ98qYPN-sBbH2zWB976YLM-8SpjtT7w1geb9cfiAbb1obE-1vrAWx9s1gfe-rQOeZ3WZYvHqPXRz_roZ330sz76WR_9rI9-1kc_66Of9dFufbRaH_lSR5v1kbc-2qyPvPXRZn3iJdZqfeStjzbrj8WDu_XRsD5arY-89dFmfeStj7ZbH3nro-3Wd3nhz1J5-lzgaAdnGr1o4UWHXnS3S_XfPfdtOuLohws_PGRxj1fAfpqJmm17GGuwt2kn-3aSPcRUlRCFzqiQVaFFJViVGKrue7KQlfV7miOG-fxFChbMpyLbeTk4x7RapYOuuoY6HHklOeuYbBHzgU2HvA5tOsHrhKGjckYIQzMw1Lc4Gl-QH2-Cq2Avy32SrYM4eD_pV0G1lXu5CuLjz3VS_rMKVvnHkUvqqnh6y9MgrspaXgVlUW-2Qfwj2R2OrfplnVTya5ZsymTfIS9J_ldR7M-QXGdVUf7e_HFc_Y1cMUH8HrwGMUzxBlDcTe-m4TyazeEqeAtiFOHN7UzcirvbEMNZGH1cBf-rQac3UYTRFKL5_O4W52EYfvwESivj4g)

## Integration
This module can be integrated into a submodule.

To do this, the repository must first be included in *build.gradle.kts*. This looks as follows:
```kotlin
repositories {
  mavenCentral()
  maven {
    url = uri("https://git.dulno.com/api/v4/projects/8/packages/maven")
    credentials(HttpHeaderCredentials::class) {
      name = "Private-Token"
      value = System.getenv("DULNO_GITLAB_PRIVATE_TOKEN") ?:
        findProperty("dulnoGitlabPrivateToken") as String?
    }
    authentication {
      create("header", HttpHeaderAuthentication::class)
    }
  }
}
```

The repository can then be added and used like a regular dependency. This is done in the following way:
```kotlin
dependencies {
  compileOnly("com.dulno:core:1.0.0-SNAPSHOT")
}
```

## Module
Although the Core module offers the basic structure and skeleton of Dulno, the entire system has been developed to be extremely modular. This is how the module system was brought to life, which is based on calling up jar files at runtime when the service is started.

The following demonstrates how a module can be defined:
```java
@ModuleDescription(name = "test", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class TestModule extends Module {
  private Log log;
  private AccountLink accountLink;

  public TestModule(Injector injector) {
    //Use your own injection module here
    super(injector.createChildInjector(TestInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    //Change log name to the name of your module
    log = injector().getInstance(Log.class).subLog("Test");
    //Implement the account link externally
    accountLink = TestAccountLink.create();
  }

  @Override
  public void disable() {

  }

  @Override
  public AccountLink accountLink() {
    return accountLink;
  }

  @Override
  public ModuleInformation moduleInformation() {
    //Specify your own module information
    return ModuleInformation.create("Test", "Simple test description", "test.png",
      ModuleInformation.Type.PUBLIC);
  }

  @Override
  public TriggerRepository triggerRepository() {
    var repository = TriggerRepository.create();
    //Register your triggers here
    return repository;
  }


  @Override
  public ActionRepository actionRepository() {
    var repository = ActionRepository.create();
    //Register your actions here
    return repository
  }
}
```

## Trigger
The triggers are a core component of the Dulno system. They are used to trigger workflows. To implement a trigger, we provide the following example class:

```java
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class TestTrigger implements Trigger {
  public static TestTrigger create(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    //Specify your own content columns
    contentColumns.add(DatabaseColumn.create("value", DatabaseDataType.TEXT));
    return new TestTrigger(TriggerContentDatabaseTable.create(databaseConnection,
      databaseKeyspace, "trigger_test", contentColumns));
  }

  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    //Use an unique trigger identifier
    return "test-trigger";
  }

  @Override
  public TriggerInformation information() {
    //Build trigger information for your trigger implementation
    return TriggerInformation.builder()
      .withName("test.trigger.name")
      .withDescription("test.trigger.description")
      .withInputVariable(InputComponentVariable.createRequired("test.trigger.input.value.name",
        "value", "test.trigger.input.value.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("test.trigger.output.value", "value"))
      .build();
  }

  @Override
  public void initialize() {
    //Create content table if it doesn't already exist
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content) {
    //Insert new trigger content entry
    return contentDatabaseTable.insertContent(triggerId, DatabaseRow.of(
      content.get("value")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    //Find content of certain trigger
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("value", row.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<List<UUID>> findEntries(String condition) {
    //Find trigger entries by condition
    return contentDatabaseTable.findContentByCondition(condition).thenApply(
      rows -> rows.stream().map(row -> row.findCell(0).uuidValue()).toList());
  }

  @Override
  public CompletableFuture<Void> delete(UUID triggerId) {
    //Delete trigger content
    return contentDatabaseTable.deleteContent(triggerId);
  }
}
```

## Action
Actions are also an important part of most modules in Dulno. This is because they ensure that the workflows have any use at all. They react to the triggering of a workflow and execute tasks.

Actions are implemented in two separate classes. The action itself, which defines the basic parameters, and the ActionExecutor, which implements the actual execution. The structure of an action class is shown below:
```java
@AllArgsConstructor(staticName = "create")
public final class ChannelMessageAction implements Action<TestActionExecutor> {
  public static TestAction create(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("value", DatabaseDataType.TEXT));
    return new TestAction(ActionContentDatabaseTable.create(
      databaseConnection, databaseKeyspace, "action_test", contentColumns));
  }

  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "test-action";
  }

  @Override
  public ActionInformation information() {
    //Build action information for your action implementation
    return ActionInformation.builder()
      .withName("test.action.name")
      .withDescription("test.action.description")
      .withInputVariable(InputComponentVariable.createRequired("test.action.input.value.name",
        "value", "test.action.input.value.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("test.action.output.value", "value"))
      .build();
  }

  @Override
  public void initialize() {
    //Create content table if it doesn't already exist
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    //Insert new action content entry
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      content.get("value")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    //Find action entries by condition
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("value", row.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<TestActionExecutor> build(UUID actionId) {
    //Build a new action executor
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      TestActionExecutor.create(content.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    //Delete action content
    return contentDatabaseTable.deleteContent(actionId);
  }
}
```

Now follows an example of an ActionExecutor implementation:
```java
@AllArgsConstructor(staticName = "create")
public final class TestActionExecutor implements ActionExecutor {
  private final String value;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    //Implement execution process
    return ActionResult.futureSuccess(buildInformation(value));
  }

  private Map<String, Object> buildInformation(String value) {
    //Build your result information
    var information = Maps.<String, Object>newHashMap();
    information.put("value", value);
    return information;
  }
}
```