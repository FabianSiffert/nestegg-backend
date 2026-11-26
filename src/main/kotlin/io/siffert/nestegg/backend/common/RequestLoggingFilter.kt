package io.siffert.nestegg.backend.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {

	override fun shouldNotFilter(request: HttpServletRequest): Boolean =
		!request.requestURI.startsWith("/api/")

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		val requestId = UUID.randomUUID().toString().take(8)
		val startNanos = System.nanoTime()
		MDC.put(REQUEST_ID_KEY, requestId)
		response.setHeader(REQUEST_ID_HEADER, requestId)
		try {
			filterChain.doFilter(request, response)
		} finally {
			val durationMillis = (System.nanoTime() - startNanos) / 1_000_000
			log.info("{} {} {} {}ms", request.method, request.requestURI, response.status, durationMillis)
			MDC.remove(REQUEST_ID_KEY)
		}
	}

	companion object {
		const val REQUEST_ID_KEY = "requestId"
		const val REQUEST_ID_HEADER = "X-Request-Id"
		private val log: Logger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
	}
}
