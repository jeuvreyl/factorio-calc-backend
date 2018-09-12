package org.jeuvreyl.factorio.lua.config

import org.glassfish.jersey.server.ResourceConfig
import org.springframework.stereotype.Component

@Component
class JerseyConfig : ResourceConfig() {
    init {
        registerEndpoints()
    }

    private fun registerEndpoints() {
        packages("org.jeuvreyl.factorio.lua.controller")
    }

}