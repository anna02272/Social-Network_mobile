package com.example.socialnetwork.model

class User(
    private val firstName: String,
    private val lastName: String,
    ) {

    fun getFirstName(): String {
        return firstName
    }

    fun getLastName(): String {
        return lastName
    }
}
