package io.siffert.nestegg.backend.asset

import com.jayway.jsonpath.JsonPath
import io.siffert.nestegg.backend.TestcontainersConfiguration
import io.siffert.nestegg.backend.price.PricePoint
import io.siffert.nestegg.backend.price.PricePointRepository
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class AssetControllerTest @Autowired constructor(
	private val mockMvc: MockMvc,
	private val assetRepository: AssetRepository,
	private val pricePointRepository: PricePointRepository,
) {

	@BeforeEach
	fun cleanDatabase() {
		pricePointRepository.deleteAll()
		assetRepository.deleteAll()
	}

	@Test
	fun `creates an asset`() {
		mockMvc.post("/api/v1/assets") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"name":"MSCI World ETF","type":"SECURITY","currency":"EUR","notes":"Savings plan"}"""
		}.andExpect {
			status { isCreated() }
			header { string("Location", containsString("/api/v1/assets/")) }
			jsonPath("$.id") { exists() }
			jsonPath("$.name") { value("MSCI World ETF") }
			jsonPath("$.type") { value("SECURITY") }
			jsonPath("$.currency") { value("EUR") }
			jsonPath("$.notes") { value("Savings plan") }
		}

		assertEquals(1L, assetRepository.count())
	}

	@Test
	fun `lists assets newest first`() {
		saveAsset(name = "Older", createdAt = Instant.parse("2026-01-01T00:00:00Z"))
		saveAsset(name = "Newer", createdAt = Instant.parse("2026-02-01T00:00:00Z"))

		mockMvc.get("/api/v1/assets").andExpect {
			status { isOk() }
			jsonPath("$.length()") { value(2) }
			jsonPath("$[0].name") { value("Newer") }
			jsonPath("$[1].name") { value("Older") }
		}
	}

	@Test
	fun `gets an asset`() {
		val asset = saveAsset(name = "MSCI World ETF")

		mockMvc.get("/api/v1/assets/${asset.id}").andExpect {
			status { isOk() }
			jsonPath("$.id") { value(asset.id.toString()) }
			jsonPath("$.name") { value("MSCI World ETF") }
		}
	}

	@Test
	fun `returns 404 for an unknown asset`() {
		val id = UUID.randomUUID()

		mockMvc.get("/api/v1/assets/$id").andExpect {
			status { isNotFound() }
			content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
			jsonPath("$.detail") { value("Asset $id not found") }
		}
	}

	@Test
	fun `updates an asset`() {
		val asset = saveAsset(name = "Old name")

		mockMvc.put("/api/v1/assets/${asset.id}") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"name":"New name","type":"OTHER","currency":"USD","notes":"Updated"}"""
		}.andExpect {
			status { isOk() }
			jsonPath("$.name") { value("New name") }
			jsonPath("$.type") { value("OTHER") }
			jsonPath("$.currency") { value("USD") }
			jsonPath("$.notes") { value("Updated") }
		}

		val updated = assetRepository.findById(asset.id).orElseThrow()
		assertEquals("New name", updated.name)
		assertEquals(AssetType.OTHER, updated.type)
	}

	@Test
	fun `deletes an asset together with its prices`() {
		val asset = saveAsset(name = "MSCI World ETF")
		pricePointRepository.save(
			PricePoint(assetId = asset.id, recordedAt = Instant.parse("2026-01-01T09:00:00Z"), unitPrice = BigDecimal("100")),
		)

		mockMvc.delete("/api/v1/assets/${asset.id}").andExpect {
			status { isNoContent() }
		}

		mockMvc.get("/api/v1/assets/${asset.id}").andExpect {
			status { isNotFound() }
		}
		assertEquals(0L, pricePointRepository.count())
	}

	@Test
	fun `rejects invalid fields with a list of errors`() {
		mockMvc.post("/api/v1/assets") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"name":"","type":"SECURITY","currency":"eur"}"""
		}.andExpect {
			status { isBadRequest() }
			jsonPath("$.detail") { value("Request validation failed") }
			jsonPath("$.errors.length()") { value(2) }
			jsonPath("$.errors[*].field") { value(containsInAnyOrder("name", "currency")) }
			jsonPath("$.errors[*].code") { value(containsInAnyOrder("NotBlank", "Pattern")) }
		}

		assertEquals(0L, assetRepository.count())
	}

	@Test
	fun `rejects a malformed request body`() {
		mockMvc.post("/api/v1/assets") {
			contentType = MediaType.APPLICATION_JSON
			content = "not json"
		}.andExpect {
			status { isBadRequest() }
			jsonPath("$.detail") { value("Malformed request body") }
		}
	}

	@Test
	fun `rejects a request with a missing field`() {
		mockMvc.post("/api/v1/assets") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"type":"SECURITY","currency":"EUR"}"""
		}.andExpect {
			status { isBadRequest() }
			jsonPath("$.detail") { value("Invalid or missing value for field 'name'") }
		}
	}

	@Test
	fun `returns the request id in the header and in error responses`() {
		val result = mockMvc.get("/api/v1/assets/${UUID.randomUUID()}").andExpect {
			status { isNotFound() }
		}.andReturn()

		val requestId = result.response.getHeader("X-Request-Id")
		assertEquals(8, requestId?.length)
		assertEquals(requestId, JsonPath.read<String>(result.response.contentAsString, "$.requestId"))
	}

	@Test
	fun `returns the request id in validation error responses`() {
		val result = mockMvc.post("/api/v1/assets") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"name":"","type":"SECURITY","currency":"EUR"}"""
		}.andExpect {
			status { isBadRequest() }
		}.andReturn()

		val requestId = result.response.getHeader("X-Request-Id")
		assertEquals(requestId, JsonPath.read<String>(result.response.contentAsString, "$.requestId"))
	}

	@Test
	fun `does not add a request id outside the api`() {
		mockMvc.get("/actuator/health").andExpect {
			header { doesNotExist("X-Request-Id") }
		}
	}

	private fun saveAsset(name: String, createdAt: Instant = Instant.now()): Asset =
		assetRepository.save(Asset(name = name, type = AssetType.SECURITY, currency = "EUR", createdAt = createdAt))
}
