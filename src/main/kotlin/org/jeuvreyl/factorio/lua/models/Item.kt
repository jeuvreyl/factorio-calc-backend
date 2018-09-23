package org.jeuvreyl.factorio.lua.models

data class Item(
        val name: String,
        val orderKey: String,
        val groupName: String,
        override val iconUrl: String) : GraphicEntity