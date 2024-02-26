plugins {
  id("java")
  id("maven-publish")
  id("org.springframework.boot") version "3.2.3"
}

group = "net.taskwolf"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_20
java.targetCompatibility = JavaVersion.VERSION_20

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/TaskwolfNET/taskwolf-core")
      credentials {
        username = System.getenv("GITHUB_USERNAME") ?: providers.gradleProperty("githubUsername").get()
        password = System.getenv("GITHUB_ACCESS_TOKEN") ?: providers.gradleProperty("githubAccessToken").get()
      }
    }
  }
  publications {
    register<MavenPublication>("gpr") {
      from(components["java"])
    }
  }
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.10.2"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")

  implementation("com.google.inject:guice:7.0.0")

  implementation("com.google.guava:guava:33.0.0-jre")

  implementation("org.projectlombok:lombok:1.18.30")
  annotationProcessor("org.projectlombok:lombok:1.18.30")
  testImplementation("org.projectlombok:lombok:1.18.30")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

  implementation("com.datastax.oss:java-driver-core:4.17.0")

  implementation("org.jline:jline:3.25.1")

  implementation("org.json:json:20240205")
  implementation("commons-io:commons-io:2.15.1")

  implementation("io.netty:netty-all:4.1.107.Final")

  implementation("org.springframework.boot:spring-boot-starter-web:3.2.2")

  implementation("io.jsonwebtoken:jjwt:0.12.5")

  implementation("com.sun.mail:javax.mail:1.6.2")
}

tasks.test {
  useJUnitPlatform()
}

tasks.bootJar {
  mainClass = "net.taskwolf.core.CoreApplication"
}