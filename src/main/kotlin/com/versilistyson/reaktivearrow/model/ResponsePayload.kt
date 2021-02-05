package com.versilistyson.reaktivearrow.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import reactor.core.publisher.Mono

sealed class ResponsePayload {
    abstract val status: Int
    abstract val statusDescription: String
    abstract val messages: List<String>
    open val errors: List<ErrorItem> = listOf()
    abstract val metadata: Map<String, String>
    abstract val success: Boolean

    data class Data<T>(
        @JsonProperty(value = "status", access = JsonProperty.Access.READ_ONLY)
        override val status: Int = HttpStatus.OK.value(),
        @JsonProperty(value = "statusDescription", access = JsonProperty.Access.READ_ONLY)
        override val statusDescription: String = HttpStatus.OK.reasonPhrase,
        @JsonProperty(value = "data", access = JsonProperty.Access.READ_ONLY)
        val data: T? = null,
        @JsonProperty(value = "messages", access = JsonProperty.Access.READ_ONLY)
        override val messages: List<String> = listOf(),
        @JsonProperty(value = "metadata", access = JsonProperty.Access.READ_ONLY)
        override val metadata: Map<String, String> = mapOf()
    ) : ResponsePayload() {
        companion object {
            fun <T> of(value: T): Data<T> {
                return Data(data = value)
            }
        }
        override val success = true
    }


    data class Error(
        @JsonProperty(value = "status", access = JsonProperty.Access.READ_ONLY)
        override val status: Int,
        @JsonProperty(value = "statusDescription", access = JsonProperty.Access.READ_ONLY)
        override val statusDescription: String,
        @JsonProperty(value = "errors", access = JsonProperty.Access.READ_ONLY)
        override val errors: List<ErrorItem> = listOf(),
        @JsonProperty(value = "metadata", access = JsonProperty.Access.READ_ONLY)
        override val metadata: Map<String, String> = mapOf(),
        @JsonProperty(value = "messages", access = JsonProperty.Access.READ_ONLY)
        override val messages: List<String> = listOf()
    ) : ResponsePayload() {

        companion object {
            fun withStatus(code: HttpStatus) =
                ResponsePayload.Error(status = code.value(), statusDescription= code.reasonPhrase)
        }

        override val success: Boolean = false
    }

    suspend fun bodyValueAndAwait(mediaType: MediaType = MediaType.APPLICATION_JSON) =
        ServerResponse
            .status(this.status)
            .contentType(mediaType)
            .bodyValueAndAwait(this)
}

suspend fun Mono<ErrorResponse>.bodyValueAndAwait(mediaType: MediaType = MediaType.APPLICATION_JSON) =
    this.awaitSingle().bodyValueAndAwait(mediaType)


typealias DataResponse<T> = ResponsePayload.Data<T>

fun <T> DataResponse<T>.httpStatus(code: HttpStatus) =
    copy(status = code.value(), statusDescription = code.reasonPhrase)

fun <T> DataResponse<T>.data(value: T?) =
    copy(data = value)

fun <T> DataResponse<T>.addMessage(message: String) =
    copy(messages = this.messages + message)

fun <T> DataResponse<T>.addMessages(messages: String) =
    copy(messages = this.messages + messages)

fun <T> DataResponse<T>.addMetadata(keyValuePair: Pair<String, String>) =
    copy(metadata = this.metadata + keyValuePair)

fun <T> DataResponse<T>.addMetadata(map: Map<String, String>) =
    copy(metadata = this.metadata + map)

typealias ErrorResponse = ResponsePayload.Error

fun ErrorResponse.addError(errorItem: ErrorItem) = copy(errors = this.errors + errorItem)
fun ErrorResponse.addErrors(errorItems: List<ErrorItem>) = copy(errors = this.errors + errorItems)

fun ErrorResponse.addMetadata(keyValuePair: Pair<String, String>) = copy(metadata = this.metadata + keyValuePair)
fun ErrorResponse.addMetadata(map: Map<String, String>) = copy(metadata = this.metadata + map)
