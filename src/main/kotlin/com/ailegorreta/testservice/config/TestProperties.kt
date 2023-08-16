package com.ailegorreta.testservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Important : If we want other beans to refresh the scope (as this bean in @ConfigurationProperties)
 *             we need to include the @RefreshScope annotation.
 */
@ConfigurationProperties(prefix = "test")
class TestProperties {

    var greeting: String? = null

}