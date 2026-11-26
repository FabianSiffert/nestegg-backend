package io.siffert.nestegg.backend.price

import io.siffert.nestegg.backend.asset.AssetService
import io.siffert.nestegg.backend.common.BadRequestException
import io.siffert.nestegg.backend.common.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class PricePointService(
	private val pricePointRepository: PricePointRepository,
	private val assetService: AssetService,
	private val clock: Clock,
) {

	fun list(assetId: UUID): List<PricePointResponse> {
		assetService.requireExists(assetId)
		return pricePointRepository.findByAssetIdOrderByRecordedAtAsc(assetId).map { it.toResponse() }
	}

	fun get(assetId: UUID, priceId: UUID): PricePointResponse = findPricePoint(assetId, priceId).toResponse()

	@Transactional
	fun create(assetId: UUID, request: PricePointRequest): PricePointResponse {
		assetService.requireExists(assetId)
		requireNotInFuture(request.recordedAt)
		return pricePointRepository.save(request.toEntity(assetId)).toResponse()
	}

	@Transactional
	fun update(assetId: UUID, priceId: UUID, request: PricePointRequest): PricePointResponse {
		val pricePoint = findPricePoint(assetId, priceId)
		requireNotInFuture(request.recordedAt)
		pricePoint.recordedAt = request.recordedAt
		pricePoint.unitPrice = request.unitPrice
		pricePoint.note = request.note
		return pricePoint.toResponse()
	}

	@Transactional
	fun delete(assetId: UUID, priceId: UUID) {
		val pricePoint = findPricePoint(assetId, priceId)
		pricePointRepository.delete(pricePoint)
	}

	private fun findPricePoint(assetId: UUID, priceId: UUID): PricePoint =
		pricePointRepository.findByIdAndAssetId(priceId, assetId) ?: throw PricePointNotFoundException(priceId)

	private fun requireNotInFuture(recordedAt: Instant) {
		if (recordedAt > Instant.now(clock).plus(FUTURE_TOLERANCE)) {
			throw PriceRecordedInFutureException(recordedAt)
		}
	}

	private companion object {
		val FUTURE_TOLERANCE: Duration = Duration.ofMinutes(5)
	}
}

class PricePointNotFoundException(id: UUID) : NotFoundException("Price point $id not found")

class PriceRecordedInFutureException(recordedAt: Instant) :
	BadRequestException("Price recorded at $recordedAt is in the future", code = "PastOrPresent", field = "recordedAt")
