package com.ailegorreta.testservice

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import dasniko.testcontainers.keycloak.KeycloakContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.assertj.core.api.Assertions.assertThat

/**
 * In this example text we include SpringBoot, Spring Security and TestContainers;
 *
 * note: There is no Spring Security Server Test container (so far) so we use the Keycloak test container as
 * an excellent substitute and the configuration es in the file 'test-real-config.json'.
 *
 * see: page 425 of Cloud native Spring in Action' book or for a simpler realm see:
 * https://www.baeldung.com/spring-boot-keycloak-integration-testing
 *
 * note: the Oauth2 SCOPE is not stored in Keycloak, so the testing must be just for Roles
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Testcontainers
class TestServiceApplicationTests {

	@Autowired
	private val webTestClient: WebTestClient? = null

	companion object {
		private var bjornTokens: KeycloakToken? = null

		// Customer and employee
		private var  isabelleTokens: KeycloakToken? = null

		@Container
		val keycloakContainer: KeycloakContainer = KeycloakContainer("quay.io/keycloak/keycloak:19.0")
										.withRealmImportFile("test-realm-config.json")

		@BeforeAll
		@JvmStatic
		fun generateAccessTokens() {
			val webClient = WebClient.builder()
				.baseUrl(keycloakContainer.authServerUrl + "realms/TestService/protocol/openid-connect/token")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.build()

			isabelleTokens = authenticateWith("isabelle","password", webClient)
			bjornTokens = authenticateWith("bjorn","password", webClient)
		}

		private fun authenticateWith(username: String, password: String, webClient: WebClient): KeycloakToken? {
			return webClient.post()
				.body(BodyInserters.fromFormData("grant_type", "password")
					.with("client_id", "polar-test")
					.with("username", username)
					.with("password", password))
				.retrieve()
				.bodyToMono(KeycloakToken::class.java)
				.block()
		}

		@AfterAll
		@JvmStatic
		fun closeContainers() {
			if (keycloakContainer.isRunning) keycloakContainer.close()
		}
	}

	init {
		keycloakContainer.start()
		/* Do not use @DynamicPropertySource annotation with static method because it is part of the Spring-Boot
 		 * context lifecycle. Better to use the singleton container pattern
   			see: https://stackoverflow.com/questions/74110777/dynamicpropertysource-not-being-invoked-kotlin-spring-boot-and-testcontainers
 		 */
		System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", keycloakContainer.authServerUrl + "realms/TestService")
	}

	@Test
	fun contextLoads() {
		println("[A] keycloakContainer:${keycloakContainer}")
		println("[B] isabelleTokens:${isabelleTokens}")
		println("[C] bjornTokens:${bjornTokens}")
	}

	/**
	 * Here we test with Keycloak that the user has that valid Roles (no SCOPE)
	 */
	@Test
	fun validateAuthenticationThenShouldReturn200() {
		webTestClient!!.get()
			.uri("/testrolesecurity")
			.headers{ headers -> headers.setBearerAuth(isabelleTokens!!.accessToken)}
			.exchange()
			.expectStatus().isOk
			.expectBody(String::class.java).value{ result ->
				assertThat(result).isNotNull()
				print("Result:$result")
			}
	}

	/**
	 * Here we test with Keycloak that the user does NOT has the role permition
	 */
	@Test
	fun validateAuthenticationThenShouldReturn403() {
		webTestClient!!.get()
			.uri("/testrolesecurity")
			.headers{ headers -> headers.setBearerAuth(bjornTokens!!.accessToken)}
			.exchange()
			.expectStatus().is4xxClientError
	}

	@JvmRecord
	private data class KeycloakToken @JsonCreator private constructor(
						@param:JsonProperty("access_token") val accessToken: String	)

}
