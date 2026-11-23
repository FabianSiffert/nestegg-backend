package io.siffert.nestegg.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NesteggBackendApplication

fun main(args: Array<String>) {
	runApplication<NesteggBackendApplication>(*args)
}
