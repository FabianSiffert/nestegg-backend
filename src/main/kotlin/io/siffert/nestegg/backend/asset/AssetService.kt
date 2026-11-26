package io.siffert.nestegg.backend.asset

import io.siffert.nestegg.backend.common.NotFoundException
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AssetService(
	private val assetRepository: AssetRepository,
) {

	fun list(): List<AssetResponse> =
		assetRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).map { it.toResponse() }

	fun get(id: UUID): AssetResponse = findAsset(id).toResponse()

	fun requireExists(id: UUID) {
		if (!assetRepository.existsById(id)) {
			throw AssetNotFoundException(id)
		}
	}

	@Transactional
	fun create(request: AssetRequest): AssetResponse =
		assetRepository.save(request.toEntity()).toResponse()

	@Transactional
	fun update(id: UUID, request: AssetRequest): AssetResponse {
		val asset = findAsset(id)
		asset.name = request.name
		asset.type = request.type
		asset.currency = request.currency
		asset.notes = request.notes
		return asset.toResponse()
	}

	@Transactional
	fun delete(id: UUID) {
		val asset = findAsset(id)
		assetRepository.delete(asset)
	}

	private fun findAsset(id: UUID): Asset =
		assetRepository.findByIdOrNull(id) ?: throw AssetNotFoundException(id)
}

class AssetNotFoundException(id: UUID) : NotFoundException("Asset $id not found")
