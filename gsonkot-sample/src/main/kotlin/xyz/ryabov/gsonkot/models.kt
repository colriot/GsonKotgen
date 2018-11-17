package xyz.ryabov.gsonkot

@GsonAdapter
data class User(
  val id: Long,
  val username: String,
  val isAdmin: Boolean
)
