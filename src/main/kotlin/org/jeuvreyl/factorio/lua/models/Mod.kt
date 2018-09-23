package org.jeuvreyl.factorio.lua.models

data class Mod(val name: String) {
    val dependencies: Set<Dependency>
        get() = setOf()
}

data class Dependency(val name: String, val optional : Boolean)