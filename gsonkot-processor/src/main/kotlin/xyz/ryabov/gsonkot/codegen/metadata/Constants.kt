package xyz.ryabov.gsonkot.codegen.metadata

import kotlinx.metadata.ClassName
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element

/**
 * Name of the processor option containing the path to the Kotlin generated src dir.
 */
const val kaptGeneratedOption = "kapt.kotlin.generated"


val Element.kotlinMetadata: KotlinClassMetadata?
  get() = KotlinClassMetadata.read(getAnnotation(Metadata::class.java).run {
    KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
  })

fun ClassName.fqName() = this.replace('/', '.')
