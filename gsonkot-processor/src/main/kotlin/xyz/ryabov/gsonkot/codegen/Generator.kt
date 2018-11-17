package xyz.ryabov.gsonkot.codegen

internal object Generator {

  internal data class GlassInput(
    val className: String,
    val fqClassName: String,
    val pkg: String,
    val typeArgumentList: List<TypeParameter>,
    val parameterList: List<Parameter>
  ) {
    val adapterClassName get() = "${className}_GsonAdapter"

    fun generate() = Generator.generate(this)
  }

  internal data class TypeParameter(
    val name: String,
    val upperBoundsFqClassNames: List<String>
  )

  internal data class Parameter(
    val name: String,
    val fqClassName: String,
    val isNullable: Boolean = false
  )

  fun generate(input: GlassInput) = run {
    val (className, fqClassName, pkg, typeArgumentList, parameterList) = input
    val main = main(input.adapterClassName, className, fqClassName, typeArgumentList, parameterList)

    """
        |package $pkg
        |
        |import com.google.gson.Gson
        |import com.google.gson.TypeAdapter
        |import com.google.gson.stream.JsonReader
        |import com.google.gson.stream.JsonToken
        |import com.google.gson.stream.JsonWriter
        |
        |$main
        """.trimMargin()
  }

  private fun main(
    adapterClassName: String,
    className: String,
    fqClassName: String,
    typeArgumentList: List<TypeParameter>,
    parameters: List<Parameter>
  ) = run {
    val adaptersDeclaration = adaptersDeclaration(parameters)
    val tempVarsDeclaration = tempVarsDeclaration(parameters)
    val writeFieldsBlock = writeFieldsBlock(parameters)
    val readFieldBlock = readFieldsBlock(parameters)
    val constructorArgs = constructorArgs(parameters)

    """
        |class $adapterClassName(gson: Gson) : TypeAdapter<$fqClassName>() {
        |$adaptersDeclaration
        |
        |  override fun write(jsonWriter: JsonWriter, value: $fqClassName?) {
        |    if (value == null) {
        |      throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        |    }
        |
        |    jsonWriter.beginObject()
        |$writeFieldsBlock
        |    jsonWriter.endObject()
        |  }
        |
        |  override fun read(jsonReader: JsonReader): $fqClassName? {
        |$tempVarsDeclaration
        |
        |    jsonReader.beginObject()
        |$readFieldBlock
        |    jsonReader.endObject()
        |
        |    return $fqClassName(
        |      $constructorArgs
        |    )
        |  }
        |}
        """.trimMargin()
  }

  private fun adaptersDeclaration(parameters: List<Parameter>): String {
    return parameters.joinToString(separator = "\n") {
      """
      |  private val ${it.name}Adapter = gson.getAdapter(${it.fqClassName}::class.java)${if (it.isNullable) ".nullSafe()" else ""}
      """.trimMargin()
    }
  }

  private fun tempVarsDeclaration(parameters: List<Parameter>): String {
    return parameters.joinToString(separator = "\n") {
      """
      |    var ${it.name}: ${it.fqClassName}? = null
      """.trimMargin()
    }
  }

  private fun writeFieldsBlock(parameters: List<Parameter>): String {
    return parameters.joinToString(separator = "\n") {
      """
      |    jsonWriter.name("${it.name}")
      |    ${it.name}Adapter.write(jsonWriter, value.${it.name})
      """.trimMargin()
    }
  }

  private fun readFieldsBlock(parameters: List<Parameter>): String {
    val sections = parameters.joinToString(separator = "\n") {
      """
      |        "${it.name}" -> {
      |          ${it.name} = ${it.name}Adapter.read(jsonReader)${if (!it.isNullable) " ?: throw NullPointerException(\"Non-null parameter '${it.name}' was null.\")" else ""}
      |        }
      """.trimMargin()
    }

    return """
    |    while (jsonReader.hasNext()) {
    |      when(jsonReader.nextName()) {
    |$sections
    |        else -> {
    |          jsonReader.skipValue()
    |        }
    |      }
    |    }
    """.trimMargin()
  }

  private fun constructorArgs(parameters: List<Parameter>): String =
    parameters.joinToString(",\n      ") {
      "${it.name} = ${it.name}" + if (!it.isNullable) " ?: throw NullPointerException(\"Non-null parameter '${it.name}' was missing.\")" else ""
    }

  private fun typeArguments(typeArgumentList: List<TypeParameter>) =
    typeArgumentList
      .takeIf { it.isNotEmpty() }
      ?.joinToString(prefix = "<", postfix = ">") { it.name }
      ?: ""
}
