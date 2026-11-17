package no.nav.klage.lookup.service.access

import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

@Service
class AccessToPersonBatchService(
    private val accessToPersonService: AccessToPersonService,
) {
    companion object {
        // Caps parallel calls to Redis and Tilgangsmaskinen per request.
        const val MAX_PARALLEL_CALLS = 10
    }

    /**
     * Checks each navIdent's access to the person in parallel. Goes through the cached
     * [AccessToPersonService.getNavIdentAccessToUserWithAppToken], so cache hits never reach Tilgangsmaskinen.
     * Fails the whole batch if a single check fails, same as the single endpoint.
     */
    fun getNavIdentsAccessToUser(
        brukerId: String,
        navIdentList: List<String>,
    ): Map<String, Access> {
        val semaphore = Semaphore(MAX_PARALLEL_CALLS)
        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            val futures =
                navIdentList.distinct().associateWith { navIdent ->
                    executor.submit(
                        Callable {
                            semaphore.acquire()
                            try {
                                accessToPersonService.getNavIdentAccessToUserWithAppToken(
                                    brukerId = brukerId,
                                    navIdent = navIdent,
                                )
                            } finally {
                                semaphore.release()
                            }
                        },
                    )
                }
            return futures.mapValues { (_, future) ->
                try {
                    future.get()
                } catch (e: ExecutionException) {
                    throw e.cause ?: e
                }
            }
        }
    }
}
