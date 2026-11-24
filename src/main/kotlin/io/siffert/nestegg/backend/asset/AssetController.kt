package io.siffert.nestegg.backend.asset

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
@RequestMapping("/api/v1/assets")
class AssetController(
	private val assetService: AssetService,
) {

	@GetMapping
	fun list(): List<AssetResponse> = assetService.list()

	@GetMapping("/{id}")
	fun get(@PathVariable id: UUID): AssetResponse = assetService.get(id)

	@PostMapping
	fun create(@Valid @RequestBody request: AssetRequest): ResponseEntity<AssetResponse> {
		val created = assetService.create(request)
		val location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(created.id)
			.toUri()
		return ResponseEntity.created(location).body(created)
	}

	@PutMapping("/{id}")
	fun update(
		@PathVariable id: UUID,
		@Valid @RequestBody request: AssetRequest,
	): AssetResponse = assetService.update(id, request)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: UUID) {
		assetService.delete(id)
	}
}
