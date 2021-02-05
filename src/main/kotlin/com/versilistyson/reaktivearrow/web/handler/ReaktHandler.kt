package com.versilistyson.reaktivearrow.web.handler

import arrow.Kind
import arrow.core.Either
import com.versilistyson.reaktivearrow.exception.DomainFailure
import com.versilistyson.reaktivearrow.model.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import reactor.kotlin.core.publisher.toFlux

interface ReaktHandler<T> {
    suspend fun handleWith(request: ServerRequest, handler: suspend (ServerRequest) -> T) : ServerResponse
}

interface EitherHandler : ReaktHandler<Either<Throwable, ResponsePayload.Data<*>>>

@Component
class EitherWrapperImpl : EitherHandler {

    companion object {
        private val logger by logger()
    }

    override suspend fun handleWith(
        request: ServerRequest,
        handler: suspend (ServerRequest) -> Either<Throwable, ResponsePayload.Data<*>>
    ): ServerResponse {
        logger.debug("Server request received: $request")
        val response = processServerResponse(handler(request))

        logger.debug("Server response created: $response")
        return response
    }


    private suspend fun processServerResponse(result: Either<Throwable, ResponsePayload.Data<*>>): ServerResponse =
        result.fold({ t -> t.createErrorResponse() }) { payload ->
            payload.bodyValueAndAwait(MediaType.APPLICATION_JSON)
        }


    private suspend fun Throwable.createErrorResponse(): ServerResponse {
        return when(this) {
            is DomainFailure -> handleDomainFailure(this)
            is ResponseStatusException -> handleResponseStatusException(this)
            else -> handleGenericExc(this)
        }
    }

    private suspend fun handleGenericExc(t: Throwable): ServerResponse {
        logger.error("uncaught domain failure while processing request", t)
        return ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase)
            .addError(ErrorItem("server failure")).bodyValueAndAwait()
    }

    private suspend fun handleResponseStatusException(e: ResponseStatusException): ServerResponse {
        logResponseStatusExc(e)

        return ErrorResponse(e.rawStatusCode, e.status.reasonPhrase).addError(ErrorItem(e.reason)).bodyValueAndAwait()
    }

    private suspend fun handleDomainFailure(failure: DomainFailure): ServerResponse {

        logResponseStatusExc(failure)

        return failure.messages.toFlux()
            .map { message -> ErrorItem(message) }
            .collectList().map { errorItems ->
                ErrorResponse(failure.rawStatusCode, failure.status.reasonPhrase, errorItems)
            }.bodyValueAndAwait()
    }

    private fun logResponseStatusExc(e: ResponseStatusException) {
        when {
            e.status.is4xxClientError -> logger.warn("Client failure while processing request", e)
            e.status.is5xxServerError -> logger.error("Domain failure while processing request", e)
        }
    }

}
