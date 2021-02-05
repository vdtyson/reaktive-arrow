package com.versilistyson.reaktivearrow.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

sealed class DomainFailure : ResponseStatusException {

    var messages: MutableList<String> = mutableListOf()
        private set(value) {
            field = value
        }

    constructor(status: HttpStatus, reason: String?, cause: Throwable?) : super(status, reason, cause) {
        reason?.let { messages.add(it) }
    }

    constructor(status: HttpStatus, reasons: List<String>, cause: Throwable?) : super(
        status,
        reasons.toString(),
        cause
    ) {
        messages.addAll(reasons)
    }

    constructor(status: Int, reason: String?, cause: Throwable?) : super(status, reason, cause) {
        reason?.let { messages.add(it) }
    }

    constructor(status: Int, reasons: List<String>, cause: Throwable?) : super(status, reasons.toString(), cause) {
        messages.addAll(reasons)
    }

    class UnprocessableEntityException(
        reason: String?,
        cause: Throwable?
    ) : DomainFailure(HttpStatus.UNPROCESSABLE_ENTITY, reason, cause)

    class NoResourceException(
        reason: String?,
        cause: Throwable?
    ) : DomainFailure(HttpStatus.NOT_FOUND, reason, cause)

    class ValidationException(
        reasons: List<String>,
        cause: Throwable?
    ) : DomainFailure(HttpStatus.UNPROCESSABLE_ENTITY, reasons, cause)
}

typealias NotResourceException = DomainFailure.NoResourceException
fun DomainFailure.notFound(
    reason: String? = "Not found",
    cause: Throwable? = null
) : NotResourceException = DomainFailure.NoResourceException(reason, cause)


typealias UnprocessableEntityException = DomainFailure.UnprocessableEntityException
fun DomainFailure.unprocessableEntity(
    reason: String? = "Failed to process",
    cause: Throwable? = null
): UnprocessableEntityException {
    return UnprocessableEntityException(reason, cause)
}

typealias ValidationException = DomainFailure.ValidationException
fun DomainFailure.validationExc(reasons: List<String> = listOf(), cause: Throwable? = null): ValidationException {
    return ValidationException(reasons, cause)
}
