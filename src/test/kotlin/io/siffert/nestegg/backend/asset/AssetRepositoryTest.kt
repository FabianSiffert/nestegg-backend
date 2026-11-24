package io.siffert.nestegg.backend.asset

import io.siffert.nestegg.backend.TestcontainersConfiguration
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals

@DataJpaTest
@Import(TestcontainersConfiguration::class)
class AssetRepositoryTest  @Autowired constructor(
    private val assetRepository: AssetRepository,
    private val entityManager: EntityManager,
){
    @Test
    fun `saves and loads an asset`() {
        val asset = Asset(
            name = "MSCI World ETF",
            type = AssetType.SECURITY,
            currency = "EUR",
            notes = "Savings plan",
        )
        assetRepository.save( asset)
        entityManager.flush()
        entityManager.clear()

        val loaded = assetRepository.findById(asset.id).orElseThrow()

        assertEquals(asset.name, loaded.name)
        assertEquals(asset.type, loaded.type)
        assertEquals(asset.currency, loaded.currency)
        assertEquals( asset.notes, loaded.notes)
    }
}