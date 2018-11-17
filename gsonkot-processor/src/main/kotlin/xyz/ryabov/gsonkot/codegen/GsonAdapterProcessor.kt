package xyz.ryabov.gsonkot.codegen

import com.google.auto.service.AutoService
import kotlinx.metadata.ClassName
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClassVisitor
import kotlinx.metadata.KmConstructorVisitor
import kotlinx.metadata.KmTypeParameterVisitor
import kotlinx.metadata.KmTypeVisitor
import kotlinx.metadata.KmValueParameterVisitor
import kotlinx.metadata.KmVariance
import kotlinx.metadata.jvm.KotlinClassMetadata
import xyz.ryabov.gsonkot.GsonAdapter
import xyz.ryabov.gsonkot.codegen.Generator.GlassInput
import xyz.ryabov.gsonkot.codegen.Generator.Parameter
import xyz.ryabov.gsonkot.codegen.Generator.TypeParameter
import xyz.ryabov.gsonkot.codegen.metadata.fqName
import xyz.ryabov.gsonkot.codegen.metadata.isPrimaryConstructor
import xyz.ryabov.gsonkot.codegen.metadata.kaptGeneratedOption
import xyz.ryabov.gsonkot.codegen.metadata.kotlinMetadata
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

@AutoService(Processor::class)
@Suppress("unused")
class GsonAdapterProcessor : AbstractProcessor() {

  private val annotationName = GsonAdapter::class.java.canonicalName

  private val options: Map<String, String> get() = processingEnv.options
  private val messager: Messager get() = processingEnv.messager
  private val filer: Filer get() = processingEnv.filer
  private val elementUtils: Elements get() = processingEnv.elementUtils
  private val typeUtils: Types get() = processingEnv.typeUtils
  private val generatedDir: File? get() = options[kaptGeneratedOption]?.let(::File)

  override fun getSupportedOptions(): Set<String> = setOf(kaptGeneratedOption)

  override fun getSupportedAnnotationTypes(): Set<String> = setOf(annotationName)

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    val annotationElement = elementUtils.getTypeElement(annotationName)
    for (element in roundEnv.getElementsAnnotatedWith(annotationElement)) {
      val input = getInputFrom(element) ?: continue
      if (!input.generateAndWrite()) return true
    }
    return true
  }

  private fun getInputFrom(element: Element): GlassInput? {
    val metadata = element.kotlinMetadata

    if (metadata !is KotlinClassMetadata.Class) {
      errorMustBeKotlinClass(element)
      return null
    }

    lateinit var className: String
    lateinit var fqClassName: String
    lateinit var pkg: String
    val typeArguments = mutableListOf<TypeParameter>()
    val typeArgsRegistry = mutableMapOf<Int, String>()
    val parameters = mutableListOf<Parameter>()


    metadata.accept(object : KmClassVisitor() {
      override fun visit(flags: Flags, name: ClassName) {
        pkg = name.substringBeforeLast('/').fqName()
        className = name.substringAfterLast('/')
        fqClassName = name.fqName()
      }

      override fun visitConstructor(flags: Flags): KmConstructorVisitor? {
        if (!flags.isPrimaryConstructor) {
          return null
        }

        return object : KmConstructorVisitor() {
          override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
            return object : KmValueParameterVisitor() {
              override fun visitType(flags: Flags): KmTypeVisitor? {
                return KmTypeInfoVisitor(flags, typeArgsRegistry::get) {
                  parameters += Parameter(name, it.fqName, it.isNullable)
                }
              }
            }
          }
        }
      }

      override fun visitTypeParameter(
        flags: Flags,
        name: String,
        id: Int,
        variance: KmVariance
      ): KmTypeParameterVisitor? {
        return object : KmTypeParameterVisitor() {
          val upperBoundsFqClassNames = arrayListOf<String>()

          override fun visitUpperBound(flags: Flags): KmTypeVisitor? {
            return KmTypeInfoVisitor(flags, typeArgsRegistry::get) {
              upperBoundsFqClassNames += it.fqName
            }
          }

          override fun visitEnd() {
            typeArgsRegistry += id to name
            typeArguments += TypeParameter(name, upperBoundsFqClassNames)
          }
        }
      }
    })

    return GlassInput(
      className = className,
      fqClassName = fqClassName,
      pkg = pkg,
      typeArgumentList = typeArguments,
      parameterList = parameters
    )
  }

  private fun errorMustBeKotlinClass(element: Element) {
    messager.printMessage(
      ERROR,
      "@${GsonAdapter::class.java.simpleName} can't be applied to $element: must be a Kotlin class", element
    )
  }

  private fun GlassInput.generateAndWrite(): Boolean {

    val generatedDir = generatedDir ?: run {
      messager.printMessage(ERROR, "Can't find option '$kaptGeneratedOption'")
      return false
    }
    val dirPath = pkg.replace('.', File.separatorChar)
    val filePath = "$adapterClassName.kt"
    val dir = File(generatedDir, dirPath).also { it.mkdirs() }
    val file = File(dir, filePath)
    file.writeText(generate())
    return true
  }
}
