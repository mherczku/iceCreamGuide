package hu.hm.icguide.models


data class User(
    val uid: String = "",
    val role: String = "user",
    val name: String = "",
    val photo: String = ""
)