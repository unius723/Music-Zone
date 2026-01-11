package org.wit.musiczone.models

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "username" to username,
            "email" to email,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): User {
            return User(
                uid = map["uid"] as? String ?: "",
                username = map["username"] as? String ?: "",
                email = map["email"] as? String ?: "",
                createdAt = (map["createdAt"] as? Long) ?: System.currentTimeMillis()
            )
        }
    }
}

