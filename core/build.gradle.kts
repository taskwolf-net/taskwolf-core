plugins {
  id("java")
}

group = "de.flexpedite"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  testCompileOnly(platform("org.junit:junit-bom:5.10.0"))
  testCompileOnly("org.junit.jupiter:junit-jupiter:5.10.0")

  compileOnly("com.google.inject:guice:7.0.0")

  compileOnly("com.google.guava:guava:32.1.2-jre")

  compileOnly("org.projectlombok:lombok:1.18.30")
  annotationProcessor("org.projectlombok:lombok:1.18.30")
  testCompileOnly("org.projectlombok:lombok:1.18.30")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.test {
  useJUnitPlatform()
}