package com.example.socialnetwork.model

class Group(
    private val name: String,
    private val description: String,
    private val creationDate: String,
    private val groupAdmin: String) {

    fun getName(): String {
        return name
    }

    fun getDescription(): String {
        return description
    }

    fun getCreationDate(): String {
        return creationDate
    }

    fun getGroupAdmin(): String {
        return groupAdmin
    }
}
