package org.jeuvreyl.factorio.lua.models

data class AssemblingMachine(val name: String,
                             val iconUrl: String,
                             val craftingSpeed: Double,
                             val ingredientCount : Int,
                             val subGroups : List<String>,
                             val recipe : String?)