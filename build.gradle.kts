import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.2.61" apply false
  kotlin("kapt") version "1.2.61" apply false
}

subprojects {
  repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx/")
    jcenter()
  }
}

group = "xyz.ryabov.gsonkot"
version = "1.0-SNAPSHOT"

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.6"
}
