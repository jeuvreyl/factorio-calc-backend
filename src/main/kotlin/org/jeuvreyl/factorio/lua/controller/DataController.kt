package org.jeuvreyl.factorio.lua.controller

import org.jeuvreyl.factorio.lua.loader.DataLoader
import org.springframework.stereotype.Component
import java.io.File
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Component
@Path("data")
class DataController(private val dataLoader: DataLoader) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getData(): Response {
        val result = dataLoader.loadData(File("D:\\jeux\\steam\\steamapps\\common\\Factorio"))
        return Response.ok(result).build()
    }
}


