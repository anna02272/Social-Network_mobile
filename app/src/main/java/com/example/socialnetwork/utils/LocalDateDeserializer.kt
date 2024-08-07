package com.example.socialnetwork.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.LocalDate

class LocalDateDeserializer : JsonDeserializer<LocalDate> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDate {
        val jsonArray = json.asJsonArray
        val year = jsonArray[0].asInt
        val month = jsonArray[1].asInt
        val day = jsonArray[2].asInt

        return LocalDate.of(year, month, day)
    }
}

