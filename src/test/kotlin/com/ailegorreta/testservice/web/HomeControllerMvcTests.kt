package com.ailegorreta.testservice.web

import com.ailegorreta.testservice.HomeController
import com.ailegorreta.testservice.config.ResourceServerConfig
import com.ailegorreta.testservice.config.TestProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Example just for testing Spring MVC only (no integration with SpringBootTest, Spring Security and
 * Testcontainers like KeyCloak)
 */
@WebMvcTest(HomeController::class)
@Import(TestProperties::class, ResourceServerConfig::class)
class HomeControllerMvcTests {

    @Autowired
    var mockMvc: MockMvc? = null

    @MockBean
    var jwtDecoder: JwtDecoder? = null
    /* ^ Mocks the JwtDecoder so that the application does not try to call Spring Security Server and get the public
         keys for decoding the Access Token
     */
    @Test
    @Throws(Exception::class)
    fun noMatterAuthenticationThenShouldReturn200() {
        mockMvc!!.perform(
                MockMvcRequestBuilders.get("/nosecurity")
                    .with(jwt() )
            )
            .andExpect(status().isOk)
    }
    @Test
    @Throws(Exception::class)
    fun validateAuthenticationThenShouldReturn403() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.get("/testsecurity")
                .with(jwt() )
        )
            .andExpect(status().is4xxClientError)
    }

    /**
     * We need to send the permission and also the SCOPE from the Oauth2 server
     *
     */
    @Test
    @Throws(Exception::class)
    fun validateAuthenticationThenShouldReturn200() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.get("/testsecurity")
                .with(jwt().authorities(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                        SimpleGrantedAuthority("ROLE_ADMINLEGO") ))
        )
            .andExpect(status().isOk)
    }

    /**
     * In the uri we just check the client registration and no Role validation
     */
    @Test
    @Throws(Exception::class)
    fun validateAuthenticationClientOnlyThenShouldReturn200() {
        mockMvc!!.perform(
            MockMvcRequestBuilders.get("/testclientsecurity")
                .with(jwt().authorities(SimpleGrantedAuthority("SCOPE_iam.facultad")))
        )
            .andExpect(status().isOk)
    }

    /*


    @Test
    @Throws(Exception::class)
    fun whenDeleteBookWithEmployeeRoleThenShouldReturn204() {
        val isbn = "7373731394"
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/books/$isbn")
                    .with(jwt().authorities(SimpleGrantedAuthority(com.polarbookshop.catalogservice.web.BookControllerMvcTests.ROLE_EMPLOYEE)))
            )
            .andExpect(MockMvcResultMatchers.status().isNoContent())
    }

    @Test
    @Throws(Exception::class)
    fun whenDeleteBookWithCustomerRoleThenShouldReturn403() {
        val isbn = "7373731394"
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/books/$isbn")
                    .with(jwt().authorities(SimpleGrantedAuthority(com.polarbookshop.catalogservice.web.BookControllerMvcTests.ROLE_CUSTOMER)))
            )
            .andExpect(MockMvcResultMatchers.status().isForbidden())
    }

    @Test
    @Throws(Exception::class)
    fun whenPutBookWithEmployeeRoleThenShouldReturn200() {
        val isbn = "7373731394"
        val bookToCreate: Unit = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia")
        BDDMockito.given(bookService.addBookToCatalog(bookToCreate)).willReturn(bookToCreate)
        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/books/$isbn")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper!!.writeValueAsString(bookToCreate))
                    .with(jwt().authorities(SimpleGrantedAuthority(com.polarbookshop.catalogservice.web.BookControllerMvcTests.ROLE_EMPLOYEE)))
            )
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

     */

}