package xyz.ryabov.gsonkot.codegen

internal object Generator {

  internal data class GlassInput(
    val className: String,
    val fqClassName: String,
    val pkg: String,
    val typeArgumentList: List<TypeParameter>,
    val parameterList: List<Parameter>
  ) {

    fun generate() = Generator.generate(this)
  }

  internal data class TypeParameter(
    val name: String,
    val upperBoundsFqClassNames: List<String>
  )

  internal data class Parameter(
    val name: String,
    val fqClassName: String
  )

  fun generate(input: GlassInput) = run {
    val (className, fqClassName, pkg, typeArgumentList, parameterList) = input
    val main = main(className, fqClassName, typeArgumentList, parameterList)

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
    className: String,
    fqClassName: String,
    typeArgumentList: List<TypeParameter>,
    parameters: List<Parameter>
  ) = run {
    val typeArguments = typeArguments(typeArgumentList)
    val functionArgs = parameters.joinToString { (name, className) -> "$name: $className = this.$name" }
    val copyArgs = parameters.joinToString { (name) -> "$name = _$name" }

    """
        |class ${className}__GsonAdapter(gson: Gson) : TypeAdapter<$fqClassName>() {
        |${adapterSection(parameters)}
        |
        |  override fun write(jsonWriter: JsonWriter, value: $fqClassName?) {
        |    if (value == null) {
        |      throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        |    }
        |
        |    jsonWriter.beginObject()
        |${write(parameters)}
        |    jsonWriter.endObject()
        |  }
        |
        |  override fun read(jsonReader: JsonReader): $fqClassName? {
        |${tempVarsDeclaration(parameters)}
        |
        |    jsonReader.beginObject()
        |${read(parameters)}
        |    jsonReader.endObject()
        |
        |    return $fqClassName(
        |      ${constructorArgs(parameters)}
        |    )
        |  }
        |}
        |""".trimMargin()
  }

  private fun constructorArgs(parameters: List<Parameter>): String =
    parameters.joinToString(",\n      ") {
      "${it.name} = ${it.name}"
    }

  private fun read(parameters: List<Parameter>): String {
    val sections = parameters.joinToString(separator = "\n") {
      """
      |        "${it.name}" -> {
      |          ${it.name} = ${it.name}Adapter.read(jsonReader)
      |        }
      |""".trimMargin()
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
    |""".trimMargin()
  }

  private fun tempVarsDeclaration(parameters: List<Parameter>): String {
    return parameters.joinToString(separator = "\n") {
      """
      |    var ${it.name}: ${it.fqClassName}? = null
      |""".trimMargin()
    }
  }

  private fun adapterSection(parameters: List<Parameter>): String {
    return parameters.joinToString(separator = "\n") {
      """
      |  private val ${it.name}Adapter = gson.getAdapter(${it.fqClassName}::class.java)
      |""".trimMargin()
    }
  }

  private fun write(parameters: List<Parameter>): String {
    return parameters.joinToString(separator = "\n") {
      """
      |    jsonWriter.name("${it.name}")
      |    ${it.name}Adapter.write(jsonWriter, value.${it.name})
      |""".trimMargin()
    }
  }

  private fun typeArguments(typeArgumentList: List<TypeParameter>) =
    typeArgumentList
      .takeIf { it.isNotEmpty() }
      ?.joinToString(prefix = "<", postfix = ">") { it.name }
      ?: ""
}
