package io.siffert.nestegg.backend.asset

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class AssetRequest(
	@field:NotBlank
	@field:Size(max = 200)
	val name: String,

	val type: AssetType,

	@field:Pattern(regexp = "^[A-Z]{3}$")
	val currency: String,

	@field:Size(max = 2000)
	val notes: String? = null,
)

data class AssetResponse(
	val id: UUID,
	val name: String,
	val type: AssetType,
	val currency: String,
	val notes: String?,
	val createdAt: Instant,
)

fun AssetRequest.toEntity(id: UUID = UUID.randomUUID(), createdAt: Instant = Instant.now()) = Asset(
	id = id,
	name = name,
	type = type,
	currency = currency,
	notes = notes,
	createdAt = createdAt,
)

fun Asset.toResponse() = AssetResponse(
	id = id,
	name = name,
	type = type,
	currency = currency,
	notes = notes,
	createdAt = createdAt,
)
