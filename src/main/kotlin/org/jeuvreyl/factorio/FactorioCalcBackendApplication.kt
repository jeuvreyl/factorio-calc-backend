package org.jeuvreyl.factorio

import org.jeuvreyl.factorio.config.ApplicationConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ApplicationConfiguration::class)
class FactorioCalcBackendApplication

fun main(args: Array<String>) {
    runApplication<FactorioCalcBackendApplication>(*args)
}
