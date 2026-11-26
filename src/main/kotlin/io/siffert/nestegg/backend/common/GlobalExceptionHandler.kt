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
	fun handleNotFound(exception: NotFoundException, request: WebRequest): ResponseEntity<Any>? {
		log.warn(exception.message)
		return problemResponse(exception, HttpStatus.NOT_FOUND, exception.message ?: "Resource not found", request)
	}

	@ExceptionHandler(BadRequestException::class)
	fun handleBadRequest(exception: BadRequestException, request: WebRequest): ResponseEntity<Any>? {
		log.warn(exception.message)
		val detail = exception.message ?: "Bad request"
		val problem = inputProblem(HttpStatus.BAD_REQUEST, detail, listOf(InputError(exception.field, exception.code, detail)))
		return handleExceptionInternal(exception, problem, HttpHeaders(), HttpStatus.BAD_REQUEST, request)
	}

	@ExceptionHandler(Exception::class)
	fun handleUnexpected(exception: Exception, request: WebRequest): ResponseEntity<Any>? {
		log.error("Unhandled exception", exception)
		return problemResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request)
	}

	override fun handleMethodArgumentNotValid(
		ex: MethodArgumentNotValidException,
		headers: HttpHeaders,
		status: HttpStatusCode,
		request: WebRequest,
	): ResponseEntity<Any>? {
		val fieldErrors = ex.bindingResult.fieldErrors.map { InputError(it.field, it.code, it.defaultMessage) }
		val globalErrors = ex.bindingResult.globalErrors.map { InputError(null, it.code, it.defaultMessage) }
		val errors = fieldErrors + globalErrors
		log.warn("Request validation failed: {}", errors)
		return handleExceptionInternal(ex, inputProblem(status, "Request validation failed", errors), headers, status, request)
	}

	override fun handleHttpMessageNotReadable(
		ex: HttpMessageNotReadableException,
		headers: HttpHeaders,
		status: HttpStatusCode,
		request: WebRequest,
	): ResponseEntity<Any>? {
		val field = (ex.cause as? JacksonException)?.path?.mapNotNull { it.propertyName }?.joinToString(".")
		val error = if (field.isNullOrEmpty()) {
			InputError(field = null, code = "MalformedBody", message = "Malformed request body")
		} else {
			InputError(field = field, code = "InvalidValue", message = "Invalid or missing value for field '$field'")
		}
		log.warn(error.message)
		return handleExceptionInternal(ex, inputProblem(status, error.message, listOf(error)), headers, status, request)
	}

	// Every response built by ResponseEntityExceptionHandler passes through here, including Spring's built-in errors.
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

	private fun problemResponse(
		exception: Exception,
		status: HttpStatus,
		detail: String,
		request: WebRequest,
	): ResponseEntity<Any>? =
		handleExceptionInternal(exception, ProblemDetail.forStatusAndDetail(status, detail), HttpHeaders(), status, request)

	private fun inputProblem(status: HttpStatusCode, detail: String?, errors: List<InputError>): ProblemDetail =
		ProblemDetail.forStatusAndDetail(status, detail).apply { setProperty("errors", errors) }

	private fun ProblemDetail.addRequestId() {
		MDC.get(RequestLoggingFilter.REQUEST_ID_KEY)?.let { setProperty("requestId", it) }
	}

	private companion object {
		val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
	}
}

internal data class InputError(val field: String?, val code: String?, val message: String?)
