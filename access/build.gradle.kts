plugins {
  id("java")
  id("org.springframework.boot") version "3.1.4"
}

group = "de.flexpedite"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val archiveImplementation: Configuration = configurations.create("archiveImplementation")

sourceSets {
  main {
    compileClasspath += archiveImplementation
  }
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

  implementation("de.flexpedite:core:1.0.0-SNAPSHOT")
  archiveImplementation("de.flexpedite:core:1.0.0-SNAPSHOT")

  implementation("com.google.inject:guice:7.0.0")

  implementation("com.google.guava:guava:32.1.2-jre")

  implementation("org.projectlombok:lombok:1.18.30")
  annotationProcessor("org.projectlombok:lombok:1.18.30")
  testImplementation("org.projectlombok:lombok:1.18.30")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

  implementation("io.jsonwebtoken:jjwt-api:0.12.0")
  implementation("io.jsonwebtoken:jjwt-impl:0.12.0")
  implementation("io.jsonwebtoken:jjwt-gson:0.12.0")

  implementation("org.springframework.boot:spring-boot-starter-web:3.1.4")
}

tasks.test {
  useJUnitPlatform()
}