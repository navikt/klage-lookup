package no.nav.klage.lookup.config

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.klage.auth.test.AzureSecurityContractTest
import no.nav.klage.auth.test.MockTokenX
import no.nav.klage.lookup.api.external.repr.ExternalReprApiController
import no.nav.klage.lookup.api.external.repr.RepresentasjonsforholdView
import no.nav.klage.lookup.api.user.GroupsResponse
import no.nav.klage.lookup.api.user.UserController
import no.nav.klage.lookup.service.SaksbehandlerService
import no.nav.klage.lookup.service.reprapi.ReprApiService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class, ExternalReprApiController::class)
@Import(SecurityConfiguration::class)
class SecurityConfigurationTest : AzureSecurityContractTest() {
    companion object {
        private val mockTokenX = MockTokenX()

        @JvmStatic
        @DynamicPropertySource
        fun tokenXProperties(registry: DynamicPropertyRegistry) {
            mockTokenX.registerProperties(registry)
        }

        private const val TOKEN_X_PATH = "/external/representasjon/kan-representere"
    }

    @MockkBean
    private lateinit var saksbehandlerService: SaksbehandlerService

    @MockkBean
    private lateinit var reprApiService: ReprApiService

    override fun protectedRequest(): MockHttpServletRequestBuilder = get("/users/Z123456/groups")

    @BeforeEach
    fun setup() {
        every { saksbehandlerService.getGroupsForUser(navIdent = any()) } returns GroupsResponse(groupIds = emptyList())
        every { reprApiService.externalKanRepresentere() } returns
            RepresentasjonsforholdView(fullmakt = emptyList(), vergemaal = emptyList())
    }

    @Test
    fun `external endpoints accept TokenX token`() {
        mockMvc.perform(get(TOKEN_X_PATH).bearer(mockTokenX.token())).andExpect(status().isOk)
    }

    @Test
    fun `external endpoints reject request without token`() {
        mockMvc.perform(get(TOKEN_X_PATH)).andExpect(status().isUnauthorized)
    }

    @Test
    fun `external endpoints reject TokenX token with lower login level`() {
        mockMvc.perform(get(TOKEN_X_PATH).bearer(mockTokenX.token(acr = "idporten-loa-substantial"))).andExpect(status().isUnauthorized)
    }

    @Test
    fun `external endpoints reject Azure token`() {
        mockMvc.perform(get(TOKEN_X_PATH).bearer(mockAzure.token())).andExpect(status().isUnauthorized)
    }

    @Test
    fun `Azure endpoints reject TokenX token`() {
        mockMvc.perform(protectedRequest().bearer(mockTokenX.token())).andExpect(status().isUnauthorized)
    }

    @Test
    fun `dev admin endpoints require token outside dev`() {
        mockMvc.perform(get("/dev/admin/evict-cache")).andExpect(status().isUnauthorized)
    }

    private fun MockHttpServletRequestBuilder.bearer(token: String) = header(HttpHeaders.AUTHORIZATION, "Bearer $token")
}
