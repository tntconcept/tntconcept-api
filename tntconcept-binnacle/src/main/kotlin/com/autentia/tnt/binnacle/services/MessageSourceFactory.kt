package com.autentia.tnt.binnacle.services

import io.micronaut.context.MessageSource
import io.micronaut.context.annotation.Factory
import io.micronaut.context.i18n.ResourceBundleMessageSource
import jakarta.inject.Singleton

@Factory
internal class MessageSourceFactory {

    @Singleton
    fun createMessageSource(): MessageSource {
        return ResourceBundleMessageSource("messages")
    }

}
