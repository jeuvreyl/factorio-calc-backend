package org.jeuvreyl.factorio.controller

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
                     private val iconLoader: IconLoader) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getData(): Response {
        val baseDir = File("D:\\jeux\\steam\\steamapps\\common\\Factorio")
        val modsDir = File("C:\\Users\\laurent\\AppData\\Roaming\\Factorio")
        val result = dataLoader.loadData(baseDir, modsDir)
        iconLoader.copyIcons(baseDir, File("D:\\tmp\\factorio\\"), result)

        return Response.ok(result).build()
    }
}


