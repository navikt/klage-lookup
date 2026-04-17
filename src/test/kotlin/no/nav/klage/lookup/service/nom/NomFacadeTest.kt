package no.nav.klage.lookup.service.nom

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.klage.lookup.config.CacheConfiguration.Companion.USER_SLUTTDATO
import no.nav.klage.lookup.config.nom.NomClient
import no.nav.klage.lookup.service.nom.graphql.Ansatt
import no.nav.klage.lookup.service.nom.graphql.GetAnsatteDataWrapper
import no.nav.klage.lookup.service.nom.graphql.GetAnsatteResponse
import no.nav.klage.lookup.service.nom.graphql.Ressurs
import no.nav.klage.lookup.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import java.time.LocalDate

class NomFacadeTest {

    private val nomClient = mockk<NomClient>()
    private val tokenUtil = mockk<TokenUtil>()
    private val cacheManager = mockk<CacheManager>()
    private val cache = mockk<Cache>(relaxed = true)

    private val nomFacade = NomFacade(
        nomClient = nomClient,
        tokenUtil = tokenUtil,
        cacheManager = cacheManager,
    )

    @BeforeEach
    fun setUp() {
        every { tokenUtil.getAppAccessTokenWithNomScope() } returns "token"
    }

    @Test
    fun `getAnsatteInfoFromNom uses only cache when all entries are cached`() {
        val ansattA = ansatt("A123")
        val ansattB = ansatt("B456")

        every { cacheManager.getCache(USER_SLUTTDATO) } returns cache
        every { cache.get("A123", Ansatt::class.java) } returns ansattA
        every { cache.get("B456", Ansatt::class.java) } returns ansattB

        val result = nomFacade.getAnsatteInfoFromNom(listOf("A123", "B456"))

        assertThat(result.data?.ressurser).containsExactly(
            Ressurs(id = "A123", ressurs = ansattA),
            Ressurs(id = "B456", ressurs = ansattB),
        )
        assertThat(result.errors).isNull()

        verify(exactly = 0) { nomClient.hentAnsatte(any(), any()) }
        verify(exactly = 0) { cache.put(any(), any()) }
    }

    @Test
    fun `getAnsatteInfoFromNom fetches only uncached entries and stores successful hits in cache`() {
        val ansattA = ansatt("A123")

        every { cacheManager.getCache(USER_SLUTTDATO) } returns cache
        every { cache.get(any<String>(), Ansatt::class.java) } returns null
        every {
            nomClient.hentAnsatte(
                bearerToken = "Bearer token",
                query = match { it.variables.navidenter == listOf("A123", "B456") },
            )
        } returns GetAnsatteResponse(
            data = GetAnsatteDataWrapper(
                ressurser = listOf(
                    Ressurs(id = "A123", ressurs = ansattA),
                    Ressurs(id = "B456", ressurs = null),
                )
            ),
            errors = null,
        )

        val result = nomFacade.getAnsatteInfoFromNom(listOf("A123", "B456"))

        assertThat(result.data?.ressurser).containsExactly(
            Ressurs(id = "A123", ressurs = ansattA),
            Ressurs(id = "B456", ressurs = null),
        )
        verify(exactly = 1) { nomClient.hentAnsatte(any(), any()) }
        verify(exactly = 1) { cache.put("A123", ansattA) }
        verify(exactly = 0) { cache.put("B456", any()) }
    }

    @Test
    fun `getAnsatteInfoFromNom deduplicates input and calls NOM only for uncached idents`() {
        val cachedAnsatt = ansatt("A123")
        val fetchedAnsatt = ansatt("B456")

        every { cacheManager.getCache(USER_SLUTTDATO) } returns cache
        every { cache.get("A123", Ansatt::class.java) } returns cachedAnsatt
        every { cache.get("B456", Ansatt::class.java) } returns null
        every { cache.get("C789", Ansatt::class.java) } returns null
        every {
            nomClient.hentAnsatte(
                bearerToken = "Bearer token",
                query = match { it.variables.navidenter == listOf("B456", "C789") },
            )
        } returns GetAnsatteResponse(
            data = GetAnsatteDataWrapper(
                ressurser = listOf(
                    Ressurs(id = "B456", ressurs = fetchedAnsatt),
                    Ressurs(id = "C789", ressurs = null),
                )
            ),
            errors = null,
        )

        val result = nomFacade.getAnsatteInfoFromNom(listOf("A123", "A123", "B456", "C789", "B456"))

        assertThat(result.data?.ressurser).containsExactly(
            Ressurs(id = "A123", ressurs = cachedAnsatt),
            Ressurs(id = "B456", ressurs = fetchedAnsatt),
            Ressurs(id = "C789", ressurs = null),
        )
        verify(exactly = 1) { nomClient.hentAnsatte(any(), any()) }
        verify(exactly = 1) { cache.put("B456", fetchedAnsatt) }
        verify(exactly = 0) { cache.put("C789", any()) }
    }

    private fun ansatt(navIdent: String): Ansatt {
        return Ansatt(
            navident = navIdent,
            sluttdato = LocalDate.parse("2026-04-17"),
        )
    }
}

