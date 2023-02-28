package com.autentia.tnt.api.binnacle

import io.micronaut.http.HttpMessage
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.kotlin.http.argumentOf
import io.micronaut.kotlin.http.argumentOfList
import java.util.Optional

inline fun <reified T : Any> BlockingHttpClient.exchangeObject(request: HttpRequest<out Any>): HttpResponse<T> =
    exchange(request, argumentOf<T>())

inline fun <reified T : Any> BlockingHttpClient.exchangeList(request: HttpRequest<out Any>): HttpResponse<List<T>> =
    exchange(request, argumentOfList<T>())

inline fun <reified T : Any> HttpMessage<*>.getBody(): Optional<T> =
    this.getBody(argumentOf<T>())
