package io.siffert.nestegg.backend.asset

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "asset")
class Asset(
	@Id
	val id: UUID = UUID.randomUUID(),

	@Column(nullable = false, length = 200)
	var name: String,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	var type: AssetType,

	@Column(nullable = false, length = 3)
	var currency: String,

	var notes: String? = null,

	@Column(nullable = false, updatable = false)
	val createdAt: Instant = Instant.now(),
)
