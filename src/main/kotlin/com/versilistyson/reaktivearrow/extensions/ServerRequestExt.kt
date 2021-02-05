package com.versilistyson.reaktivearrow.typeclass

import arrow.Kind
import arrow.core.Either
import arrow.core.ForEither
import arrow.extension
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

suspend inline fun <reified T : Any> ServerRequest.catchAndAwaitBody(): Either<Throwable, T> =
    Either.catch { awaitBody() }
