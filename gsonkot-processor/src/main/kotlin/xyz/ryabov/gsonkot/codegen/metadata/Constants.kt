package xyz.ryabov.gsonkot.codegen.metadata

import kotlinx.metadata.ClassName
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element

/**
 * Name of the processor option containing the path to the Kotlin generated src dir.
 */
const val kaptGeneratedOption = "kapt.kotlin.generated"


@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
val Element.kotlinMetadata: KotlinClassMetadata?
  get() = KotlinClassMetadata.read(getAnnotation(Metadata::class.java).run {
    KotlinClassHeader(k, mv, bv, d1, d2, xs, pn, xi)
  })

fun ClassName.fqName() = this.replace('/', '.')
