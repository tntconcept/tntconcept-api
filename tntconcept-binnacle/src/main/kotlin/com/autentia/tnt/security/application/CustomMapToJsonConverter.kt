package com.autentia.tnt.security.application

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton

@Singleton
class CustomMapToJsonConverter(
    private val objectMapper: ObjectMapper
) {
    internal fun toJson(map: Map<String, Any>): String =
        if (map.isEmpty()) ""
        else objectMapper.writeValueAsString(map)

    internal fun toMap(json: String): Map<String, Any> =
        if (json.isBlank()) emptyMap()
        else objectMapper.readValue(json)

    private inline fun <reified T> ObjectMapper.readValue(content: String) = readValue(content, T::class.java)
}

