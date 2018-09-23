package org.jeuvreyl.factorio.lua.loader

import org.jeuvreyl.factorio.lua.models.FactorioData
import org.jeuvreyl.factorio.lua.models.GraphicEntity
import org.springframework.stereotype.Service
import java.io.File

@Service
object IconLoader {

    fun copyIcons(baseDir: File, targetDir: File, data: FactorioData) {

        if (!targetDir.isDirectory) {
            throw NoSuchFileException(file = targetDir, reason = "Target directory not found")
        }

        setOf(
                extractIconUrls(data.itemByName.values),
                extractIconUrls(data.recipeByName.values),
                extractIconUrls(data.groupByName.values)
        ).flatten()
                .map { it -> it to File(baseDir, "data/$it") }
                .forEach { (url, file) ->
                    if (!file.isFile) {
                        throw NoSuchFileException(file = file, reason = "Icon file not found")
                    }
                    file.copyTo(target = File(targetDir, url),
                            overwrite = true)
                }
    }

    private fun extractIconUrls(graphicEntities: Collection<GraphicEntity>): List<String> {
        return graphicEntities.map { it.iconUrl }
    }
}
