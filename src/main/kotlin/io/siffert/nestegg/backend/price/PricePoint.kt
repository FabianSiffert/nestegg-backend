package io.siffert.nestegg.backend.price

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "price_point")
class PricePoint(
	@Id
	val id: UUID = UUID.randomUUID(),

	@Column(name = "asset_id", nullable = false, updatable = false)
	val assetId: UUID,

	@Column(name = "recorded_at", nullable = false)
	var recordedAt: Instant,

	@Column(name = "unit_price", nullable = false, precision = 24, scale = 8)
	var unitPrice: BigDecimal,

	var note: String? = null,

	@Column(nullable = false, updatable = false)
	val createdAt: Instant = Instant.now(),
)
