package io.siffert.nestegg.backend.price

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PricePointRepository : JpaRepository<PricePoint, UUID> {

	fun findByAssetIdOrderByDateAsc(assetId: UUID): List<PricePoint>

	fun findByIdAndAssetId(id: UUID, assetId: UUID): PricePoint?
}
