package org.jeuvreyl.factorio.lua.models

data class AssemblingMachine(val name: String,
                             override val iconUrl: String,
                             val craftingSpeed: Double,
                             val ingredientCount : Int,
                             val craftingCategories : Set<String>,
                             val recipe : String?) : GraphicEntity