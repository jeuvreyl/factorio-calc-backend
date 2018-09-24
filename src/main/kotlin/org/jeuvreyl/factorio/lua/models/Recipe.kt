package org.jeuvreyl.factorio.lua.models

data class Recipe(val name: String,
                  val groupName: String,
                  val ingredients: List<UsableItem>,
                  val results: List<UsableItem>,
                  override val iconUrl: String,
                  val craftingCategory: String?) : GraphicEntity