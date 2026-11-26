package io.siffert.nestegg.backend.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@ConfigurationProperties(prefix = "nestegg.cors")
data class CorsProperties(
	val allowedOrigins: List<String> = emptyList(),
)

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class CorsConfig(
	private val corsProperties: CorsProperties,
) : WebMvcConfigurer {

	override fun addCorsMappings(registry: CorsRegistry) {
		registry.addMapping("/api/**")
			.allowedOrigins(*corsProperties.allowedOrigins.toTypedArray())
			.allowedMethods("GET", "POST", "PUT", "DELETE")
			// Browsers hide non-safelisted response headers from JavaScript; the web client needs Location after POST.
			.exposedHeaders("Location")
	}
}
