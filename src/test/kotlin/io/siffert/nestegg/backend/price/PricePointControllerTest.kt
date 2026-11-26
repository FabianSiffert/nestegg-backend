package io.siffert.nestegg.backend.price

import io.siffert.nestegg.backend.TestcontainersConfiguration
import io.siffert.nestegg.backend.asset.Asset
import io.siffert.nestegg.backend.asset.AssetRepository
import io.siffert.nestegg.backend.asset.AssetType
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
class PricePointControllerTest @Autowired constructor(
	private val mockMvc: MockMvc,
	private val assetRepository: AssetRepository,
	private val pricePointRepository: PricePointRepository,
) {

	private lateinit var asset: Asset

	@BeforeEach
	fun setUp() {
		pricePointRepository.deleteAll()
		assetRepository.deleteAll()
		asset = saveAsset("Charizard ex 151 #199")
	}

	@Test
	fun `creates a price from a string and normalizes it`() {
		mockMvc.post("/api/v1/assets/${asset.id}/prices") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"recordedAt":"2026-01-01T09:00:00Z","unitPrice":"340.50","note":"eBay"}"""
		}.andExpect {
			status { isCreated() }
			header { string("Location", containsString("/api/v1/assets/${asset.id}/prices/")) }
			jsonPath("$.assetId") { value(asset.id.toString()) }
			jsonPath("$.recordedAt") { value("2026-01-01T09:00:00Z") }
			jsonPath("$.unitPrice") { value("340.5") }
			jsonPath("$.note") { value("eBay") }
		}
	}

	@Test
	fun `creates a price from a number`() {
		mockMvc.post("/api/v1/assets/${asset.id}/prices") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"recordedAt":"2026-01-01T09:00:00Z","unitPrice":335}"""
		}.andExpect {
			status { isCreated() }
			jsonPath("$.unitPrice") { value("335") }
		}
	}

	@Test
	fun `allows several prices on the same day`() {
		savePrice(asset, "2026-01-01T09:00:00Z", "100")

		mockMvc.post("/api/v1/assets/${asset.id}/prices") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"recordedAt":"2026-01-01T15:00:00Z","unitPrice":"101"}"""
		}.andExpect {
			status { isCreated() }
		}

		assertEquals(2L, pricePointRepository.count())
	}

	@Test
	fun `rejects a price in the future`() {
		mockMvc.post("/api/v1/assets/${asset.id}/prices") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"recordedAt":"2099-01-01T00:00:00Z","unitPrice":"1"}"""
		}.andExpect {
			status { isBadRequest() }
			jsonPath("$.detail") { value("Price recorded at 2099-01-01T00:00:00Z is in the future") }
		}

		assertEquals(0L, pricePointRepository.count())
	}

	@Test
	fun `rejects a negative price`() {
		mockMvc.post("/api/v1/assets/${asset.id}/prices") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"recordedAt":"2026-01-01T09:00:00Z","unitPrice":"-1"}"""
		}.andExpect {
			status { isBadRequest() }
			jsonPath("$.errors[0].field") { value("unitPrice") }
			jsonPath("$.errors[0].code") { value("PositiveOrZero") }
		}
	}

	@Test
	fun `lists prices oldest first`() {
		savePrice(asset, "2026-03-01T09:00:00Z", "300")
		savePrice(asset, "2026-01-01T09:00:00Z", "100")
		savePrice(asset, "2026-02-01T09:00:00Z", "200")

		mockMvc.get("/api/v1/assets/${asset.id}/prices").andExpect {
			status { isOk() }
			jsonPath("$.length()") { value(3) }
			jsonPath("$[0].unitPrice") { value("100") }
			jsonPath("$[1].unitPrice") { value("200") }
			jsonPath("$[2].unitPrice") { value("300") }
		}
	}

	@Test
	fun `returns 404 for prices of an unknown asset`() {
		val unknownAssetId = UUID.randomUUID()

		mockMvc.get("/api/v1/assets/$unknownAssetId/prices").andExpect {
			status { isNotFound() }
			jsonPath("$.detail") { value("Asset $unknownAssetId not found") }
		}
	}

	@Test
	fun `returns 404 for a price that belongs to another asset`() {
		val otherAsset = saveAsset("Other asset")
		val otherPrice = savePrice(otherAsset, "2026-01-01T09:00:00Z", "100")

		mockMvc.get("/api/v1/assets/${asset.id}/prices/${otherPrice.id}").andExpect {
			status { isNotFound() }
			jsonPath("$.detail") { value("Price point ${otherPrice.id} not found") }
		}
	}

	@Test
	fun `updates a price`() {
		val price = savePrice(asset, "2026-01-01T09:00:00Z", "100")

		mockMvc.put("/api/v1/assets/${asset.id}/prices/${price.id}") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"recordedAt":"2026-01-02T10:00:00Z","unitPrice":"350","note":"Corrected"}"""
		}.andExpect {
			status { isOk() }
			jsonPath("$.recordedAt") { value("2026-01-02T10:00:00Z") }
			jsonPath("$.unitPrice") { value("350") }
			jsonPath("$.note") { value("Corrected") }
		}

		val updated = pricePointRepository.findById(price.id).orElseThrow()
		assertEquals(0, BigDecimal("350").compareTo(updated.unitPrice))
		assertEquals(Instant.parse("2026-01-02T10:00:00Z"), updated.recordedAt)
	}

	@Test
	fun `deletes a price`() {
		val price = savePrice(asset, "2026-01-01T09:00:00Z", "100")

		mockMvc.delete("/api/v1/assets/${asset.id}/prices/${price.id}").andExpect {
			status { isNoContent() }
		}

		assertEquals(0L, pricePointRepository.count())
	}

	private fun saveAsset(name: String): Asset =
		assetRepository.save(Asset(name = name, type = AssetType.COLLECTIBLE, currency = "EUR"))

	private fun savePrice(asset: Asset, recordedAt: String, unitPrice: String): PricePoint =
		pricePointRepository.save(
			PricePoint(assetId = asset.id, recordedAt = Instant.parse(recordedAt), unitPrice = BigDecimal(unitPrice)),
		)
}
