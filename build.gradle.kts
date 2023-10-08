group "de.flexpedite"
version "1.0.0-SNAPSHOT"

tasks.register("build") {
  dependsOn(gradle.includedBuild("core").task(":build"))
  dependsOn(gradle.includedBuild("access").task(":build"))
}