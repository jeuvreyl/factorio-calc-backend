package org.jeuvreyl.factorio.controller

import org.jeuvreyl.factorio.config.ApplicationConfiguration
import org.jeuvreyl.factorio.lua.loader.DataLoader
import org.jeuvreyl.factorio.lua.loader.IconLoader
import org.springframework.stereotype.Component
import java.io.File
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Component
@Path("data")
class DataController(private val dataLoader: DataLoader,
                     private val iconLoader: IconLoader,
                     private val applicationConfiguration: ApplicationConfiguration) {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getData(): Response {
        val base = File(applicationConfiguration.baseDir)
        val mods = File(applicationConfiguration.modsDir)
        val result = dataLoader.loadData(base, mods)
        iconLoader.copyIcons(base, File(applicationConfiguration.iconDir), result)

        return Response.ok(result).build()
    }
}


