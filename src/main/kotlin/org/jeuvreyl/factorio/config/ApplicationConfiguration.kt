package org.jeuvreyl.factorio.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "factorio")
class ApplicationConfiguration {
        lateinit var baseDir: String
        lateinit var modsDir: String
        lateinit var iconDir: String
}
