plugins {
  kotlin("jvm")
  kotlin("kapt")
}

kapt {
  correctErrorTypes = true
}

dependencies {
  compile(kotlin("stdlib"))
  compile(kotlin("reflect"))
  compile("com.google.code.gson:gson:2.8.5")

  compile(project(":gsonkot-api"))
  kapt(project(":gsonkot-processor"))

  compile("com.ryanharter.auto.value:auto-value-gson-annotations:0.8.0")
  kapt("com.ryanharter.auto.value:auto-value-gson:0.8.0")
//  annotationProcessor("com.ryanharter.auto.value:auto-value-gson:0.8.0")
//
  compile("com.google.auto.value:auto-value-annotations:1.6.2")
  kapt("com.google.auto.value:auto-value:1.6.2")
//  annotationProcessor("com.google.auto.value:auto-value:1.6.2")
}
