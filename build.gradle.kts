import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.net.URL
import java.util.zip.GZIPInputStream

plugins {
  id("java")
  id("maven-publish")
  id("org.springframework.boot") version "3.4.1"
  id("io.freefair.lombok") version "8.11"
  id("com.github.ben-manes.versions") version "0.51.0"
}

group = "com.dulno"
version = "1.0.0-SNAPSHOT"

publishing {
  publications {
    create<MavenPublication>("library") {
      from(components["java"])
    }
  }
  repositories {
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
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.11.4"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")

  implementation("com.google.inject:guice:7.0.0")

  implementation("com.google.guava:guava:33.4.0-jre")

  implementation("org.projectlombok:lombok:1.18.36")
  annotationProcessor("org.projectlombok:lombok:1.18.36")
  testImplementation("org.projectlombok:lombok:1.18.36")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

  implementation("com.datastax.oss:java-driver-core:4.17.0")

  implementation("org.json:json:20240303")
  implementation("commons-io:commons-io:2.18.0")

  implementation("io.netty:netty-all:4.1.115.Final")

  implementation("org.springframework.boot:spring-boot-starter-web:3.4.1")

  implementation("io.jsonwebtoken:jjwt:0.12.6")

  implementation("com.sun.mail:javax.mail:1.6.2")

  implementation("com.stripe:stripe-java:26.1.0")

  implementation("dev.samstevens.totp:totp:1.7.1")

  implementation("com.maxmind.geoip2:geoip2:4.2.1") {
    exclude(group = "commons-logging", module = "commons-logging")
  }
}

tasks.test {
  useJUnitPlatform()
}

tasks.bootJar {
  mainClass = "com.dulno.core.CoreApplication"
}

tasks.register("downloadGeoLite2Database") {
  val licenseKey = "***REMOVED***"
  val databaseUrl = "https://download.maxmind.com/app/geoip_download?" +
    "edition_id=GeoLite2-City&license_key=$licenseKey&suffix=tar.gz"
  val resourcesDir = File("geo")
  val downloadFile = File(buildDir, "GeoLite2-City.tar.gz")
  doLast {
    resourcesDir.mkdirs()
    if (downloadFile.exists()) {
      downloadFile.delete()
    }
    URL(databaseUrl).openStream().use { input ->
      downloadFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    extract(downloadFile, resourcesDir)
    downloadFile.delete()
  }
}

fun extract(file: File, destination: File) {
  GZIPInputStream(file.inputStream()).use { gis ->
    TarArchiveInputStream(gis).use { tis ->
      var entry = tis.nextTarEntry
      while (entry != null) {
        if (!entry.isDirectory && entry.name.endsWith(".mmdb")) {
          val outputFile = File(destination, "GeoLite2-City.mmdb")
          outputFile.outputStream().use { os ->
            tis.copyTo(os)
          }
        }
        entry = tis.nextTarEntry
      }
    }
  }
}