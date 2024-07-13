plugins {
  id("java")
  id("maven-publish")
  id("org.springframework.boot") version "3.2.5"
  id("io.freefair.lombok") version "8.6"
}

group = "net.taskwolf"
version = "1.0.0-SNAPSHOT"

publishing {
  publications {
    create<MavenPublication>("library") {
      from(components["java"])
    }
  }
  repositories {
    maven {
      url = uri("https://git.taskwolf.net/api/v4/projects/8/packages/maven")
      credentials(HttpHeaderCredentials::class) {
        name = "Private-Token"
        value = "***REMOVED***"
      }
      authentication {
        create("header", HttpHeaderAuthentication::class)
      }
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

  implementation("com.google.guava:guava:33.1.0-jre")

  implementation("org.projectlombok:lombok:1.18.32")
  annotationProcessor("org.projectlombok:lombok:1.18.32")
  testImplementation("org.projectlombok:lombok:1.18.32")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

  implementation("com.datastax.oss:java-driver-core:4.17.0")

  implementation("org.jline:jline:3.26.1")

  implementation("org.json:json:20240303")
  implementation("commons-io:commons-io:2.16.1")

  implementation("io.netty:netty-all:4.1.109.Final")

  implementation("org.springframework.boot:spring-boot-starter-web:3.2.5")

  implementation("io.jsonwebtoken:jjwt:0.12.5")

  implementation("com.sun.mail:javax.mail:1.6.2")

  implementation("com.stripe:stripe-java:26.1.0")

  implementation("dev.samstevens.totp:totp:1.7.1")
}

tasks.test {
  useJUnitPlatform()
}

tasks.bootJar {
  mainClass = "net.taskwolf.core.CoreApplication"
}