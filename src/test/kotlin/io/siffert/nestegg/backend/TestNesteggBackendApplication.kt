package io.siffert.nestegg.backend

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<NesteggBackendApplication>().with(TestcontainersConfiguration::class).run(*args)
}
