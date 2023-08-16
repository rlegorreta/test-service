package com.ailegorreta.testservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Testing Service to show the following:
 *
 * - How to test imperative REST calls
 * 	a) Using just Spring MVC
 * 	b) Using the complete scheme with Spring Boot, Spring Security and Keycloak test container
 *
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class TestServiceApplication

fun main(args: Array<String>) {
	runApplication<TestServiceApplication>(*args)
}
