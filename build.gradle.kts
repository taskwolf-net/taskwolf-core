plugins {
  id("maven-publish")
}

group "de.flexpedite"
version "1.0.0-SNAPSHOT"

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/Flexpedite/flexpedite")
      credentials {
        username = System.getenv("JAVA_ACTOR")
        password = System.getenv("JAVA_TOKEN")
      }
    }
  }
}

tasks.register("build") {
  dependsOn(gradle.includedBuild("core").task(":build"))
  dependsOn(gradle.includedBuild("access").task(":build"))
}