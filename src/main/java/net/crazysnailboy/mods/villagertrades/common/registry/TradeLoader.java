package net.crazysnailboy.mods.villagertrades.common.registry;

import java.io.File;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.crazysnailboy.mods.villagertrades.trades.VillagerTradeUtils;
import net.crazysnailboy.mods.villagertrades.util.FileUtils;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;


public class TradeLoader 
{
	public static void postInit()
	{
		loadCustomTrades();
	}

	
	private static void loadCustomTrades()
	{
		HashMap<String, String> tradeFiles = FileUtils.createFileMap("trade_tables");
		for ( String fileContents : tradeFiles.values() )
		{
			addCustomTrades(fileContents);
		}
	}
	
	
	private static void addCustomTrades(String fileContents)
	{
		// parse the provided string as JSON
		JsonObject jsonObject = new JsonParser().parse(fileContents).getAsJsonObject();

		// identify the profession and career to apply these trades to
		String jsonProfession = jsonObject.get("Profession").getAsString();
		String jsonCareer = jsonObject.get("Career").getAsString();

		// get the specified career and profession from the villager registry
		VillagerProfession profession = VillagerRegistryHelper.getProfession(jsonProfession);
		VillagerCareer career = VillagerRegistryHelper.getCareer(profession, jsonCareer);
		
		// iterate over the trade recipes included in the offers object
		JsonArray jsonRecipes = jsonObject.get("Offers").getAsJsonObject().get("Recipes").getAsJsonArray();
		for ( JsonElement jsonRecipe : jsonRecipes )
		{
			JsonObject jsonRecipeObject = jsonRecipe.getAsJsonObject();

			// get the level this trade change applies to, and what type of change it is
			String jsonRecipeAction = jsonRecipeObject.get("action").getAsString();
			int jsonCareerLevel = jsonRecipeObject.get("CareerLevel").getAsInt();
			
			// add a new trade if we're supposed to add one
			if (jsonRecipeAction.equals("add"))
			{
				VillagerTradeUtils.addTradeToCareer(career, jsonCareerLevel, jsonRecipeObject);
			}
			
			// or remove a trade if we're supposed to remove one
			else if (jsonRecipeAction.equals("remove"))
			{
				VillagerTradeUtils.removeTradeFromCareer(career, jsonCareerLevel, jsonRecipeObject);
			}

			// if we're supposed to replace a trade
			else if (jsonRecipeAction.equals("replace"))
			{
				// remove the old one, and add a new one
				VillagerTradeUtils.removeTradeFromCareer(career, jsonCareerLevel, jsonRecipeObject);
				VillagerTradeUtils.addTradeToCareer(career, jsonCareerLevel, jsonRecipeObject);
			}
			
		}

		// sort them so that the buying trades appear before the selling trades for each level (like vanilla)
		VillagerTradeUtils.SortCareerTrades(career);
		
	}

}
