package com.ailegorreta.testservice

import com.ailegorreta.testservice.config.TestProperties
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController(val testProperties: TestProperties) {

    @GetMapping("/")
    fun getGreeting(): String? {
        println("Before controller home")
        return testProperties.greeting
    }
    @GetMapping("/testsecurity")
    @PreAuthorize("hasRole('ADMINLEGO')")
    fun getTestGreeting(): String? {
        println("Before controller test with client and method security")
        return testProperties.greeting
    }

    @GetMapping("/testrolesecurity")
    @PreAuthorize("hasRole('ADMINLEGO')")
    fun getTestJustRolGreeting(): String? {
        println("Before controller test with just method security")
        return testProperties.greeting
    }

    @GetMapping("/testclientsecurity")
    fun getTestAppGreeting(): String? {
        println("Before controller test with just client security")
        return testProperties.greeting
    }

    @GetMapping("/nosecurity")
    fun getTest2Greeting(): String? {
        println("Before controller method without ANY authentication")
        return testProperties.greeting
    }

}