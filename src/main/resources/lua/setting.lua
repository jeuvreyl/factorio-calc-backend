function module(modname, ...)
      end
      require "util"
      function log(...)
      end
      defines = {}
      defines.difficulty_settings = {}
      defines.difficulty_settings.recipe_difficulty = {}
      defines.difficulty_settings.technology_difficulty = {}
      defines.difficulty_settings.recipe_difficulty.normal = 1
      defines.difficulty_settings.technology_difficulty.normal = 1
      defines.direction = {}
      defines.direction.north = 1
      defines.direction.east = 2
      defines.direction.south = 3
      defines.direction.west = 4
      data.raw["gui-style"] = {}
      data.raw["gui-style"]["default"] = {}