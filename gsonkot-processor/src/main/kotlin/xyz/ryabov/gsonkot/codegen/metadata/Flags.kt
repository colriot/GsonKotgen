package xyz.ryabov.gsonkot.codegen.metadata

import kotlinx.metadata.Flag
import kotlinx.metadata.Flags

val Flags.isSuspendType: Boolean get() = Flag.Type.IS_SUSPEND(this)
val Flags.isNullableType: Boolean get() = Flag.Type.IS_NULLABLE(this)

val Flags.hasAnnotations: Boolean get() = Flag.HAS_ANNOTATIONS(this)

val Flags.isInnerclass: Boolean get() = Flag.Class.IS_INNER(this)
val Flags.isDataClass: Boolean get() = Flag.Class.IS_DATA(this)

val Flags.isPrimaryConstructor: Boolean get() = Flag.Constructor.IS_PRIMARY(this)

val Flags.declaresDefaultValue: Boolean get() = Flag.ValueParameter.DECLARES_DEFAULT_VALUE(this)
val Flags.isCrossInline: Boolean get() = Flag.ValueParameter.IS_CROSSINLINE(this)
val Flags.isNoInline: Boolean get() = Flag.ValueParameter.IS_NOINLINE(this)
