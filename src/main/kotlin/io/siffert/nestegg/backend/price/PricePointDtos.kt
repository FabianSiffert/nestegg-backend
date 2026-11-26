package io.siffert.nestegg.backend.price

import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PricePointRequest(
	val recordedAt: Instant,

	@field:PositiveOrZero
	@field:Digits(integer = 16, fraction = 8)
	val unitPrice: BigDecimal,

	@field:Size(max = 2000)
	val note: String? = null,
)

data class PricePointResponse(
	val id: UUID,
	val assetId: UUID,
	val recordedAt: Instant,
	val unitPrice: String,
	val note: String?,
	val createdAt: Instant,
)

fun PricePointRequest.toEntity(
	assetId: UUID,
	id: UUID = UUID.randomUUID(),
	createdAt: Instant = Instant.now(),
) = PricePoint(
	id = id,
	assetId = assetId,
	recordedAt = recordedAt,
	unitPrice = unitPrice,
	note = note,
	createdAt = createdAt,
)

fun PricePoint.toResponse() = PricePointResponse(
	id = id,
	assetId = assetId,
	recordedAt = recordedAt,
	unitPrice = unitPrice.stripTrailingZeros().toPlainString(),
	note = note,
	createdAt = createdAt,
)
