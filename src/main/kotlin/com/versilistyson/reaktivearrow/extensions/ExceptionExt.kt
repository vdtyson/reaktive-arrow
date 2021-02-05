package com.versilistyson.reaktivearrow.extensions

import am.ik.yavi.core.ConstraintViolations
import com.versilistyson.reaktivearrow.exception.DomainFailure
import kotlinx.coroutines.reactive.awaitSingle
import reactor.kotlin.core.publisher.toFlux

suspend fun ConstraintViolations.toValidationException(): DomainFailure.ValidationException =
    violations().toFlux()
        .map { v -> v.message() }
        .collectList().map { errorMessages -> DomainFailure.ValidationException(errorMessages, null)}
        .awaitSingle()
