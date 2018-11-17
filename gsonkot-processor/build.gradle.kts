plugins {
  kotlin("jvm")
  kotlin("kapt")
}

dependencies {
  compile(kotlin("stdlib-jdk8"))
  compile("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.0.4")

  compileOnly("com.google.auto.service:auto-service:1.0-rc4")
  kapt("com.google.auto.service:auto-service:1.0-rc4")

  compile("com.google.code.gson:gson:2.8.5")

  compile(project(":gsonkot-api"))
}
