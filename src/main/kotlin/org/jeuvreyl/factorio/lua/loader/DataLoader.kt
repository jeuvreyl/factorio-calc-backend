package org.jeuvreyl.factorio.lua.loader

import org.jeuvreyl.factorio.lua.models.*
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform
import org.springframework.stereotype.Service
import java.io.File

fun LuaTable.getAsTable(name: String): LuaTable {
    return this.get(name) as LuaTable
}

fun LuaTable.getAsTable(key: LuaValue): LuaTable {
    return this.get(key) as LuaTable
}

@Service
class DataLoader(val baseLoader: BaseLoader, val modsLoader: ModsLoader) {

    private val itemKeys = listOf("item", "item-with-inventory", "item-with-entity-data",
            "fluid", "capsule", "module", "ammo", "armor", "rail-planner",
            "mining-tool", "gun", "blueprint", "deconstruction-item", "repair-tool", "tool",
            "item-with-inventory")

    private val assemblingMachineKeys = listOf("assembling-machine", "rocket-silo")

    fun loadData(baseDir: File, modsDir: File): FactorioData {

        val rawData = readLuaFiles(baseDir, modsDir)

        val groupByName = extractGroups(rawData)
        val subGroupByName = extractSubGroups(rawData, groupByName)
        val itemByName = extractItems(rawData, subGroupByName)
        val recipeByName = extractRecipes(rawData, itemByName, subGroupByName)
        val assemblingMachineByName = extractAssemblingMachines(rawData)

        return consolidateData(itemByName, recipeByName, groupByName, assemblingMachineByName)
    }

    private fun consolidateData(itemByName: Map<String, Item>,
                                recipeByName: Map<String, Recipe>,
                                groupByName: Map<String, ItemGroup>,
                                assemblingMachineByName: Map<String, AssemblingMachine>): FactorioData {
        return FactorioData(
                recipeByName = recipeByName,
                itemByName = itemByName,
                groupByName = groupByName,
                assemblingMachineByName = assemblingMachineByName
        )
    }

    private fun buildAssemblingMachine(entity: LuaTable): AssemblingMachine {
        val name = entity.get("name").toString()
        val iconUrl = cleanIconUrl(extractIcons(entity))
        val craftingSpeed = entity.get("crafting_speed").todouble()
        val recipe = extractFixedRecipe(entity)
        val subGroups = buildSubGroups(entity)
        val ingredientCount = entity.get("ingredient_count").toint()

        return AssemblingMachine(
                name = name,
                iconUrl = iconUrl,
                craftingSpeed = craftingSpeed,
                recipe = recipe,
                subGroups = subGroups,
                ingredientCount = ingredientCount
        )
    }

    private fun extractFixedRecipe(entity: LuaTable): String? {
        val fixedRecipe = entity.get("fixed_recipe")

        if (fixedRecipe.isnil()) {
            return null
        }

        return fixedRecipe.toString()
    }

    private fun buildSubGroups(entity: LuaTable): List<String> {
        val subgroups = ArrayList<String>()

        val entitiesMap = entity.getAsTable("crafting_categories")
        for (key in entitiesMap.keys()) {
            val subgroup = entitiesMap.get(key).toString()
            subgroups.add(subgroup)
        }

        return subgroups
    }

    private fun buildGroup(entity: LuaTable): ItemGroup {
        val name = entity.get("name").toString()
        val iconUrl = cleanIconUrl(extractIcons(entity))

        return ItemGroup(name = name, iconUrl = iconUrl)
    }

    private fun buildItem(entity: LuaTable, subGroupByName: Map<String, ItemSubGroup>): Item {
        val name = entity.get("name").toString()
        val iconUrl = cleanIconUrl(extractIcons(entity))
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

    private fun extractAssemblingMachines(rawData: LuaTable): Map<String, AssemblingMachine> {
        return assemblingMachineKeys.map { rawData.getAsTable(it) }
                .flatMap { entitiesMap ->
                    entitiesMap.keys()
                            .map { buildAssemblingMachine(entitiesMap.getAsTable(it)) }
                }
                .associate { it.name to it }
    }

    private fun extractGroups(rawData: LuaTable): Map<String, ItemGroup> {
        val groupByName = HashMap<String, ItemGroup>()

        val entitiesMap = rawData.getAsTable("item-group")
        for (key in entitiesMap.keys()) {
            val group = buildGroup(entitiesMap.getAsTable(key))
            groupByName[group.name] = group
        }

        return groupByName
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

    private fun extractSubGroup(entity: LuaTable): String {
        val subGroupName = entity.get("subgroup")
        if (subGroupName.isnil()) {
            return "other"
        }
        return subGroupName.toString()
    }

    private fun buildSubGroup(entity: LuaTable, groupByName: Map<String, ItemGroup>): ItemSubGroup {
        val name = entity.get("name").toString()
        val group = groupByName[entity.get("group").toString()] ?: throw NullPointerException("no group found")

        return ItemSubGroup(name = name,
                group = group)
    }

    private fun cleanIconUrl(url: String): String {
        return url.replace("__", "")
    }

    private fun extractIcons(entity: LuaTable): String {
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

        val entitiesMap = rawData.getAsTable("item-subgroup")
        for (key in entitiesMap.keys()) {
            val subGroup = buildSubGroup(entitiesMap.getAsTable(key), groupByName)
            itemSubGroupByName[subGroup.name] = subGroup
        }

        return itemSubGroupByName
    }

    private fun extractRecipes(rawData: LuaTable, itemByName: Map<String, Item>, subGroupByName: Map<String, ItemSubGroup>): Map<String, Recipe> {
        val recipeByName = HashMap<String, Recipe>()

        val entitiesMap = rawData.getAsTable("recipe")
        for (key in entitiesMap.keys()) {
            val recipe = buildRecipe(entitiesMap.getAsTable(key), itemByName, subGroupByName)
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

        val results = resultBase.getAsTable("results")
        return results.keys()
                .map { results.getAsTable(it) }
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
                .map { entities.getAsTable(it) }
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
        return itemKeys.map { rawData.getAsTable(it) }
                .flatMap { entitiesMap ->
                    entitiesMap.keys()
                            .map { buildItem(entitiesMap.getAsTable(it), subGroupByName) }
                }
                .associate { it.name to it }
    }

    private fun readLuaFiles(baseDir: File, modsDir: File): LuaTable {
        val context = JsePlatform.standardGlobals()
        DataLoader::class.java.getResourceAsStream("/lua/compat.lua")
                .use { `in` -> context.load(`in`, "compat", "t", context).call() }

        modsLoader.loadLuaFiles(File(modsDir, "\\mods"), baseLoader.loadLuaFiles(baseDir, context))

        val data = context.getAsTable("data")

        return data.getAsTable("raw")
    }
}