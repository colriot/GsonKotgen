package xyz.ryabov.gsonkot

@GsonAdapter
data class User(
  val id: Long,
  val username: String,
  val isAdmin: Boolean
)

@GsonAdapter
data class NullableTest(
  val string: String,
  val longNullable: Long?,
  val bool: Boolean,
  val int: Int?
)
