package com.versilistyson.reaktivearrow.extensions

import am.ik.yavi.core.Validator

fun <T> Validator<T>.validateArrow(target: T) =
    this.validateToEither(target!!)
        .fold({violations -> arrow.core.Either.left(violations)}) { value ->
            arrow.core.Either.right(value)
        }
