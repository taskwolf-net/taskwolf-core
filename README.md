# Taskwolf - Core

Core of the backend of Taskwolf. Each module relies on the core. It bundles central functionalities and forms the framework of the entire application.

## Status

|             | Build Status                                                                                   |
|-------------|------------------------------------------------------------------------------------------------|
| Master      | ![Java CI with Gradle](https://git.taskwolf.net/root/taskwolf-core/badges/master/pipeline.svg) |

## Integration
This module can be integrated into a submodule.

To do this, the repository must first be included in *build.gradle.kts*. This looks as follows:
```
repositories {
  mavenCentral()
  maven {
    url = uri("https://git.taskwolf.net/api/v4/projects/8/packages/maven")
    credentials(HttpHeaderCredentials::class) {
      name = "Private-Token"
      value = System.getenv("TASKWOLF_GITLAB_PRIVATE_TOKEN") ?:
        findProperty("taskwolfGitlabPrivateToken") as String?
    }
    authentication {
      create("header", HttpHeaderAuthentication::class)
    }
  }
}
```

The repository can then be added and used like a regular dependency. This is done in the following way:
```
dependencies {
  compileOnly("net.taskwolf:core:1.0.0-SNAPSHOT")
}
```