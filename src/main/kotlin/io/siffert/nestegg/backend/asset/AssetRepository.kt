package io.siffert.nestegg.backend.asset

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID


interface AssetRepository : JpaRepository<Asset, UUID>