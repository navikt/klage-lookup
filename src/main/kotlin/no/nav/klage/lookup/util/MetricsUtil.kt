package no.nav.klage.lookup.util

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

fun <T> MeterRegistry.timedCall(timerName: String, method: String, block: () -> T): T {
    return Timer.builder(timerName)
        .tag("method", method)
        .register(this)
        .recordCallable(block)
}