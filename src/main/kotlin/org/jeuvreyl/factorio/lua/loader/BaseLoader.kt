package org.jeuvreyl.factorio.lua.loader

import org.luaj.vm2.Globals
import org.springframework.stereotype.Service
import java.io.File

@Service
class BaseLoader {

    fun loadLuaFiles(baseDir: File, context: Globals): Globals {
        val luaLib = File(baseDir, "data/core/lualib/?.lua")
        val baseData = File(baseDir, "data/base/?.lua")
        val packagePath = luaLib.absolutePath + ";" + baseData.absolutePath

        (context.getAsTable("package")).set("path", packagePath)
        context.load("require 'dataloader'").call()
        DataLoader::class.java.getResourceAsStream("/lua/setting.lua")
                .use { `in` -> context.load(`in`, "setting", "t", context).call() }
        context.load("require 'data'").call()
        context.load("require 'data-updates'").call()

        return context
    }
}
