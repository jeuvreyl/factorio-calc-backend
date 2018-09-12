package org.jeuvreyl.factorio.lua.loader

import org.jeuvreyl.factorio.lua.models.*
import org.luaj.vm2.LuaTable
import org.luaj.vm2.lib.jse.JsePlatform
import org.springframework.stereotype.Service
import java.io.File


@Service
class DataLoader {

    val urlCleanUp: Regex = "__\\w*__".toRegex()

    val itemKeys = listOf("item", "item-with-inventory", "item-with-entity-data",
            "fluid", "capsule", "module", "ammo", "armor", "rail-planner",
            "mining-tool", "gun", "blueprint", "deconstruction-item", "repair-tool", "tool",
            "item-with-inventory")

    fun loadData(baseDir: File): FactorioData? {

        val rawData = readLuaFiles(baseDir)

        val groupByName = extractGroups(rawData)
        val subGroupByName: Map<String, ItemSubGroup> = extractSubGroups(rawData, groupByName)
        val itemByName: Map<String, Item> = extractItems(rawData, subGroupByName)
        val recipeByName: Map<String, Recipe> = extractRecipes(rawData, itemByName, subGroupByName)

        return consolidateData(itemByName, recipeByName, groupByName)
    }

    private fun extractGroups(rawData: LuaTable): Map<String, ItemGroup> {
        val groupByName = HashMap<String, ItemGroup>()

        val entitiesMap = rawData.get("item-group") as LuaTable
        for (key in entitiesMap.keys()) {
            val group = buildGroup(entitiesMap.get(key) as LuaTable)
            groupByName[group.name] = group
        }

        return groupByName
    }

    private fun consolidateData(itemByName: Map<String, Item>, recipeByName: Map<String, Recipe>, groupByName: Map<String, ItemGroup>): FactorioData? {
        return FactorioData(
                recipeByName = recipeByName,
                itemByName = itemByName,
                groupByName = groupByName
        )
    }

    private fun buildGroup(entity: LuaTable): ItemGroup {
        val name = entity.get("name").toString()
        val iconUrl = cleanIconUrl(extractItemIcon(entity))

        return ItemGroup(name = name, iconUrl = iconUrl)
    }

    private fun buildItem(entity: LuaTable, subGroupByName: Map<String, ItemSubGroup>): Item {
        val name = entity.get("name").toString()
        val iconUrl = cleanIconUrl(extractItemIcon(entity))
        val orderKey = entity.get("order").toString()
        val subGroup = subGroupByName[extractSubGroup(entity)]
                ?: throw NullPointerException("no subGroup found")

        return Item(name = name,
                iconUrl = iconUrl,
                orderKey = orderKey,
                groupName = subGroup.group.name)
    }

    private fun buildRecipe(entity: LuaTable, itemByName: Map<String, Item>, subGroupByName: Map<String, ItemSubGroup>): Recipe {
        val name = entity.get("name").toString()
        val ingredients = extractIngredients(entity)
        val results = extractResults(entity)
        val iconUrl = extractIcon(entity, itemByName)
        val groupName = extractGroupName(entity, itemByName, subGroupByName, results)

        return Recipe(name = name,
                ingredients = ingredients,
                results = results,
                iconUrl = iconUrl,
                groupName = groupName)
    }

    private fun extractGroupName(entity: LuaTable, itemByName: Map<String, Item>, subGroupByName: Map<String, ItemSubGroup>, results: List<UsableItem>): String {
        val subGroupName = entity.get("subgroup")

        if (subGroupName.isnil()) {
            val item =
                    if (results.size == 1) {
                        itemByName[results[0].name] ?: throw NullPointerException("no item found")
                    } else {
                        val mainProduct = entity.get("main_product")
                        itemByName[mainProduct.toString()] ?: throw NullPointerException("no item found")
                    }

            return item.groupName
        }

        val subGroup = subGroupByName[subGroupName.toString()]
                ?: throw NullPointerException("no subgroup found")

        return subGroup.name
    }

    fun extractSubGroup(entity: LuaTable): String {
        val subGroupName = entity.get("subgroup")
        if (subGroupName.isnil()) {
            return "other"
        }
        return subGroupName.toString()
    }

    private fun buildSubGroup(entity: LuaTable, groupByName: Map<String, ItemGroup>): ItemSubGroup {
        val name = entity.get("name").toString()
        val group = groupByName[entity.get("group").toString()] ?: throw NullPointerException("no group found")

        return ItemSubGroup(name = name, group = group)
    }

    private fun cleanIconUrl(url: String): String {
        return url.replace(urlCleanUp, "")
    }

    private fun extractItemIcon(entity: LuaTable): String {
        val icon = entity.get("icon")

        return if (icon.isnil()) {
            val icons = entity.get("icons") as LuaTable
            val subEntity = icons[icons.keys()[0]] as LuaTable
            subEntity.get("icon").toString()
        } else {
            icon.toString()
        }
    }

    private fun extractSubGroups(rawData: LuaTable, groupByName: Map<String, ItemGroup>): Map<String, ItemSubGroup> {
        val itemSubGroupByName = HashMap<String, ItemSubGroup>()

        val entitiesMap = rawData.get("item-subgroup") as LuaTable
        for (key in entitiesMap.keys()) {
            val subGroup = buildSubGroup(entitiesMap.get(key) as LuaTable, groupByName)
            itemSubGroupByName[subGroup.name] = subGroup
        }

        return itemSubGroupByName
    }

    private fun extractRecipes(rawData: LuaTable, itemByName: Map<String, Item>, subGroupByName: Map<String, ItemSubGroup>): Map<String, Recipe> {
        val recipeByName = HashMap<String, Recipe>()

        val entitiesMap = rawData.get("recipe") as LuaTable
        for (key in entitiesMap.keys()) {
            val recipe = buildRecipe(entitiesMap.get(key) as LuaTable, itemByName, subGroupByName)
            recipeByName[recipe.name] = recipe
        }

        return recipeByName
    }

    private fun extractIcon(entity: LuaTable, itemByName: Map<String, Item>): String {
        val mainProduct = entity.get("main_product")
        if (!mainProduct.isnil()) {
            val mainProductStr = mainProduct.toString()
            if (!mainProductStr.isBlank()) {
                return itemByName[mainProduct.toString()]?.iconUrl
                        ?: throw NullPointerException("icon not found")
            }
        }
        val icon = entity.get("icon")
        if (!icon.isnil()) {
            return cleanIconUrl(icon.toString())
        }
        val resultBase = selectResultBase(entity)
        val result = resultBase.get("result")
        if (!result.isnil()) {
            return itemByName[result.toString()]?.iconUrl
                    ?: throw NullPointerException("icon not found")
        }
        val results = resultBase.get("results")
        if (!results.isnil()) {
            val resultsTable = results as LuaTable
            val first = resultsTable[resultsTable.keys()[0]]
            if (!first.isnil()) {
                return itemByName[(first as LuaTable).get("name").toString()]?.iconUrl
                        ?: throw NullPointerException("icon not found")
            }
        }

        val name = entity.get("name")
        throw IllegalArgumentException("No icon could be found for recipe $name")
    }

    private fun extractResults(entity: LuaTable): List<UsableItem> {
        val resultBase = selectResultBase(entity)

        val result = resultBase.get("result")

        if (!result.isnil()) {
            val resultCount = resultBase.get("result_count")

            val amount = if (resultCount.isnil()) {
                1
            } else {
                resultCount.toint()
            }

            return listOf(UsableItem(
                    name = result.toString(),
                    amount = amount
            ))
        }

        val results = resultBase.get("results") as LuaTable
        return results.keys()
                .map { results.get(it) as LuaTable }
                .map {
                    buildUsableItem(it)
                }
    }

    private fun extractIngredients(entity: LuaTable): List<UsableItem> {
        val normalMode = entity.get("normal")

        val entities = if (normalMode.isnil()) {
            entity.get("ingredients")
        } else {
            (normalMode as LuaTable).get("ingredients")
        } as LuaTable

        return entities.keys()
                .map { entities.get(it) as LuaTable }
                .map {
                    buildUsableItem(it)
                }
    }

    private fun buildUsableItem(entity: LuaTable): UsableItem {
        val name = run {
            val expectedName = entity.get("name")
            if (expectedName.isnil()) {
                entity[entity.keys()[0]]
            } else {
                expectedName
            }.toString()
        }

        val amount = run {
            val expectedAmount = entity.get("amount")
            if (expectedAmount.isnil()) {
                entity[entity.keys()[1]]
            } else {
                expectedAmount
            }.toint()
        }

        if (name == "nil" || amount == 0) {
            throw NullPointerException("No usable item can be built")
        }

        return UsableItem(
                name = name,
                amount = amount)
    }

    private fun selectResultBase(entity: LuaTable): LuaTable {
        val normalMode = entity.get("normal")
        return if (normalMode.isnil()) {
            entity
        } else {
            normalMode
        } as LuaTable
    }

    private fun extractItems(rawData: LuaTable, subGroupByName: Map<String, ItemSubGroup>): Map<String, Item> {
        return itemKeys.map { rawData.get(it) as LuaTable }
                .flatMap { entitiesMap -> entitiesMap.keys().map { buildItem(entitiesMap.get(it) as LuaTable, subGroupByName) } }
                .associate { it.name to it }
    }

    private fun readLuaFiles(baseDir: File): LuaTable {
        val luaLib = File(baseDir, "data/core/lualib/?.lua")
        val baseData = File(baseDir, "data/base/?.lua")
        val packagePath = luaLib.absolutePath + ";" + baseData.absolutePath

        val context = JsePlatform.standardGlobals()
        DataLoader::class.java.getResourceAsStream("/lua/compat.lua")
                .use { `in` -> context.load(`in`, "compat", "t", context).call() }

        (context.get("package") as LuaTable).set("path", packagePath)
        context.load("require 'dataloader'").call()
        DataLoader::class.java.getResourceAsStream("/lua/setting.lua")
                .use { `in` -> context.load(`in`, "setting", "t", context).call() }
        context.load("require 'data'").call()
        context.load("require 'data-updates'").call()
        val data = context.get("data") as LuaTable
        return data.get("raw") as LuaTable
    }
}