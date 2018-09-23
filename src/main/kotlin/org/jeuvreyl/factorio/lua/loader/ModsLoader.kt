package org.jeuvreyl.factorio.lua.loader

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.jeuvreyl.factorio.lua.models.ActivatedMod
import org.jeuvreyl.factorio.lua.models.Mod
import org.luaj.vm2.Globals
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


@Service
class ModsLoader(val mapper : ObjectMapper) {
    fun loadLuaFiles(modsDir: File, context: Globals): Globals {

        return context

        if (!modsDir.isDirectory) {
            throw IOException("Mod path do not lead to a directory")
        }

        val workDir = File("d:/tmp/factorio-calc")

        val activatedMods = readActivatedMods(modsDir)

        val modFiles = unzipMods(modsDir, workDir, activatedMods)

        val dependenciesGraph = buildDepenciesGraph(modsDir, activatedMods)

        return loadDependencies(dependenciesGraph, context)
    }

    private fun unzipMods(modsDir: File, workDir: File, activatedMods: Set<String>): Set<File> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun loadDependencies(dependenciesGraph: Set<Mod>, context: Globals): Globals {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun buildDepenciesGraph(modsDir: File, activatedMods: Set<String>): Set<Mod> {
        val modInfo = activatedMods
                .asSequence()
                .flatMap { modsDir.listFiles { _, fileName -> fileName.startsWith(it) } .asSequence() }
                .map { ZipFile(it) }
                .map { it.entries().nextElement() as ZipEntry}
                .map {ZipFile("$modsDir${it.name}" ) }
                .map{ it.getEntry("info.json")}
                .map {parseDependencies(it) }
                .toSet()

        return buildGraph(modInfo)
    }

    private fun buildGraph(modInfo: Set<Mod>): Set<Mod> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun parseDependencies(infoFile: ZipEntry): Mod {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun readActivatedMods(modsDir: File): Set<String> {

        val modList = File(modsDir, "mod-list.json")

        if (!modList.isFile) {
            throw IOException("Mod list file was not found")
        }

        val typeRef = object : TypeReference<Map<String, List<ActivatedMod>>>() {}

        val fileContent = mapper
                .registerModule(KotlinModule())
                .readValue<Map<String, List<ActivatedMod>>>(modList.readText(), typeRef)

        return fileContent
                ?.get("mods")
                ?.asSequence()
                ?.filter { mod -> mod.enabled }
                ?.map { mod -> mod.name }
                ?.toSet()
                ?: setOf()
    }
}
