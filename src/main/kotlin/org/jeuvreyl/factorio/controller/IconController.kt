package org.jeuvreyl.factorio.controller

import org.jeuvreyl.factorio.config.ApplicationConfiguration
import org.springframework.stereotype.Component
import java.io.File
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

@Component
@Path("icon")
class IconController(private val applicationConfiguration: ApplicationConfiguration) {

    @GET
    @Path("{path:.*}")
    @Produces("image/png")
    fun getIcon(@PathParam("path") path: String): Response {
        val iconFile = File(applicationConfiguration.iconDir, path)
        val iconFileName = iconFile.name

        return Response
                .ok( iconFile.inputStream())
                .header("Content-Disposition", "attachment; filename=\"$iconFileName\"")
                .build()
    }
}