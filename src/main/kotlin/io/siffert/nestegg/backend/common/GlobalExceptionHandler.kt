package io.siffert.nestegg.backend.common

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import tools.jackson.core.JacksonException

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

	@ExceptionHandler(NotFoundException::class)
	fun handleNotFound(exception: NotFoundException): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.message ?: "Resource not found")

	override fun handleMethodArgumentNotValid(
		ex: MethodArgumentNotValidException,
		headers: HttpHeaders,
		status: HttpStatusCode,
		request: WebRequest,
	): ResponseEntity<Any>? {
		val problem = ProblemDetail.forStatusAndDetail(status, "Request validation failed")
		problem.setProperty("errors", ex.bindingResult.fieldErrors.map {
			mapOf("field" to it.field, "code" to it.code, "message" to it.defaultMessage)
		})
		return handleExceptionInternal(ex, problem, headers, status, request)
	}

	override fun handleHttpMessageNotReadable(
		ex: HttpMessageNotReadableException,
		headers: HttpHeaders,
		status: HttpStatusCode,
		request: WebRequest,
	): ResponseEntity<Any>? {
		val field = (ex.cause as? JacksonException)?.path?.mapNotNull { it.propertyName }?.joinToString(".")
		val detail = if (field.isNullOrEmpty()) "Malformed request body" else "Invalid or missing value for field '$field'"
		return handleExceptionInternal(ex, ProblemDetail.forStatusAndDetail(status, detail), headers, status, request)
	}
}
