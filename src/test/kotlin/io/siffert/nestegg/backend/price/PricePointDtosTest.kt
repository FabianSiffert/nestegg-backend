package io.siffert.nestegg.backend.price

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

class PricePointDtosTest {

	@Test
	fun `strips trailing zeros from the unit price`() {
		assertEquals("112.4", pricePoint("112.40000000").toResponse().unitPrice)
	}

	@Test
	fun `keeps whole numbers in plain notation`() {
		assertEquals("1000", pricePoint("1000.00000000").toResponse().unitPrice)
	}

	@Test
	fun `normalizes zero`() {
		assertEquals("0", pricePoint("0E-8").toResponse().unitPrice)
	}

	@Test
	fun `keeps significant decimals`() {
		assertEquals("0.00012345", pricePoint("0.00012345").toResponse().unitPrice)
	}

	private fun pricePoint(unitPrice: String) =
		PricePoint(assetId = UUID.randomUUID(), recordedAt = Instant.EPOCH, unitPrice = BigDecimal(unitPrice))
}
