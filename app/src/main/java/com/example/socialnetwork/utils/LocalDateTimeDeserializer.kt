package com.example.socialnetwork.utils

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDateTime

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDateTime {
        val jsonArray = json.asJsonArray
        val year = jsonArray[0].asInt
        val month = jsonArray[1].asInt
        val day = jsonArray[2].asInt
        val hour = jsonArray[3].asInt
        val minute = jsonArray[4].asInt
//        val second = jsonArray[5].asInt

        return LocalDateTime.of(year, month, day, hour, minute)
    }
}

class LocalDateTimeSerializer : JsonSerializer<LocalDateTime> {
    override fun serialize(src: LocalDateTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonArray().apply {
            add(src.year)
            add(src.monthValue)
            add(src.dayOfMonth)
            add(src.hour)
            add(src.minute)
            add(src.second)
        }
    }
}
