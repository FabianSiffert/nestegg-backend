package io.siffert.nestegg.backend.price

import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class PricePointRequest(
	val date: LocalDate,

	@field:PositiveOrZero
	@field:Digits(integer = 16, fraction = 8)
	val unitPrice: BigDecimal,

	@field:Size(max = 2000)
	val note: String? = null,
)

data class PricePointResponse(
	val id: UUID,
	val assetId: UUID,
	val date: LocalDate,
	val unitPrice: String,
	val note: String?,
	val createdAt: Instant,
)

fun PricePointRequest.toEntity(assetId: UUID) = PricePoint(
	assetId = assetId,
	date = date,
	unitPrice = unitPrice,
	note = note,
)

fun PricePoint.toResponse() = PricePointResponse(
	id = id,
	assetId = assetId,
	date = date,
	unitPrice = unitPrice.stripTrailingZeros().toPlainString(),
	note = note,
	createdAt = createdAt,
)
