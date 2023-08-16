package com.ailegorreta.testservice.demo

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Class that ony runs when test profile is enable. Then we can load data to the database
 */
@Component
@Profile("testdata")
class TestServiceDataLoader{

    @EventListener(ApplicationReadyEvent::class)
    fun loadDTOTestData() {
        println(">>>>>>>>> This where we can download the data")
    }

}