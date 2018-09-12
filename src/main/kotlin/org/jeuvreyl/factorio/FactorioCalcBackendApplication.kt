package org.jeuvreyl.factorio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FactorioCalcBackendApplication

fun main(args: Array<String>) {
    runApplication<FactorioCalcBackendApplication>(*args)
}
