package io.siffert.nestegg.backend.price

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/api/v1/assets/{assetId}/prices")
class PricePointController(
	private val pricePointService: PricePointService,
) {

	@GetMapping
	fun list(@PathVariable assetId: UUID): List<PricePointResponse> = pricePointService.list(assetId)

	@GetMapping("/{priceId}")
	fun get(
		@PathVariable assetId: UUID,
		@PathVariable priceId: UUID,
	): PricePointResponse = pricePointService.get(assetId, priceId)

	@PostMapping
	fun create(
		@PathVariable assetId: UUID,
		@Valid @RequestBody request: PricePointRequest,
	): ResponseEntity<PricePointResponse> {
		val created = pricePointService.create(assetId, request)
		val location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{priceId}")
			.buildAndExpand(created.id)
			.toUri()
		return ResponseEntity.created(location).body(created)
	}

	@PutMapping("/{priceId}")
	fun update(
		@PathVariable assetId: UUID,
		@PathVariable priceId: UUID,
		@Valid @RequestBody request: PricePointRequest,
	): PricePointResponse = pricePointService.update(assetId, priceId, request)

	@DeleteMapping("/{priceId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(
		@PathVariable assetId: UUID,
		@PathVariable priceId: UUID,
	) {
		pricePointService.delete(assetId, priceId)
	}
}
