package io.siffert.nestegg.backend.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
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
	fun handleNotFound(exception: NotFoundException): ProblemDetail {
		log.warn(exception.message)
		return problemDetail(HttpStatus.NOT_FOUND, exception.message ?: "Resource not found")
	}

	@ExceptionHandler(BadRequestException::class)
	fun handleBadRequest(exception: BadRequestException): ProblemDetail {
		log.warn(exception.message)
		return problemDetail(HttpStatus.BAD_REQUEST, exception.message ?: "Bad request")
	}

	@ExceptionHandler(Exception::class)
	fun handleUnexpected(exception: Exception): ProblemDetail {
		log.error("Unhandled exception", exception)
		return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
	}

	override fun handleMethodArgumentNotValid(
		ex: MethodArgumentNotValidException,
		headers: HttpHeaders,
		status: HttpStatusCode,
		request: WebRequest,
	): ResponseEntity<Any>? {
		val errors = ex.bindingResult.fieldErrors.map {
			mapOf("field" to it.field, "code" to it.code, "message" to it.defaultMessage)
		}
		log.warn("Request validation failed: {}", errors)
		val problem = ProblemDetail.forStatusAndDetail(status, "Request validation failed")
		problem.setProperty("errors", errors)
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
		log.warn(detail)
		return handleExceptionInternal(ex, ProblemDetail.forStatusAndDetail(status, detail), headers, status, request)
	}

	override fun createResponseEntity(
		body: Any?,
		headers: HttpHeaders,
		statusCode: HttpStatusCode,
		request: WebRequest,
	): ResponseEntity<Any> {
		if (body is ProblemDetail) {
			body.addRequestId()
		}
		return super.createResponseEntity(body, headers, statusCode, request)
	}

	private fun problemDetail(status: HttpStatus, detail: String): ProblemDetail =
		ProblemDetail.forStatusAndDetail(status, detail).also { it.addRequestId() }

	private fun ProblemDetail.addRequestId() {
		MDC.get(RequestLoggingFilter.REQUEST_ID_KEY)?.let { setProperty("requestId", it) }
	}

	private companion object {
		val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
	}
}
