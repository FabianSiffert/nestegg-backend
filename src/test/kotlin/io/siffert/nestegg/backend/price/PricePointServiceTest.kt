package io.siffert.nestegg.backend.price

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.siffert.nestegg.backend.asset.AssetService
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PricePointServiceTest {

	private val now = Instant.parse("2026-06-01T12:00:00Z")
	private val pricePointRepository = mockk<PricePointRepository>()
	private val assetService = mockk<AssetService>(relaxUnitFun = true)
	private val service = PricePointService(pricePointRepository, assetService, Clock.fixed(now, ZoneOffset.UTC))
	private val assetId = UUID.randomUUID()

	@Test
	fun `accepts a price exactly five minutes in the future`() {
		every { pricePointRepository.save(any<PricePoint>()) } answers { firstArg() }
		val recordedAt = now.plus(Duration.ofMinutes(5))

		val response = service.create(assetId, request(recordedAt))

		assertEquals(recordedAt, response.recordedAt)
	}

	@Test
	fun `rejects a price more than five minutes in the future`() {
		val recordedAt = now.plus(Duration.ofMinutes(5)).plusSeconds(1)

		assertFailsWith<PriceRecordedInFutureException> {
			service.create(assetId, request(recordedAt))
		}
		verify(exactly = 0) { pricePointRepository.save(any<PricePoint>()) }
	}

	private fun request(recordedAt: Instant) =
		PricePointRequest(recordedAt = recordedAt, unitPrice = BigDecimal("100"))
}
