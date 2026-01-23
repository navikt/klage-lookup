package no.nav.klage.lookup

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LookupApplication

fun main(args: Array<String>) {
    runApplication<LookupApplication>(*args)
}