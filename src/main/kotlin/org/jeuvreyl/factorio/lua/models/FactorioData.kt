package org.jeuvreyl.factorio.lua.models

data class FactorioData(val recipeByName: Map<String, Recipe>,
                        val itemByName: Map<String, Item>,
                        val groupByName: Map<String, ItemGroup>,
                        val assemblingMachineByName: Map<String, AssemblingMachine>)