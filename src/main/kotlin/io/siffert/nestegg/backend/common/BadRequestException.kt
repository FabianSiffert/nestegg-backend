package io.siffert.nestegg.backend.common

abstract class BadRequestException(message: String, val code: String, val field: String? = null) : RuntimeException(message)
