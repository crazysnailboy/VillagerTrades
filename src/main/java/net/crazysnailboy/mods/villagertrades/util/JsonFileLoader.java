package net.crazysnailboy.mods.villagertrades.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.crazysnailboy.mods.villagertrades.VillagerTradesMod;
import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers;
import net.crazysnailboy.mods.villagertrades.trades.VillagerTradeUtils;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VillagerBuysItemsHandler;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VillagerSellsItemsHandler;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VillagerTradeHandler;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;


public class JsonFileLoader
{
	
	
	public static void preInit()
	{
		loadCustomVillagers();
		loadCustomTrades();
	}
	
	
	private static void loadCustomVillagers()
	{
		List<String> fileNames = getFileNamesFromModFolder("/assets/" + VillagerTradesMod.MODID + "/data/villagers/");
		for ( String fileName : fileNames )
		{
			String fileContents = readFileContentsFromMod(fileName);
			addCustomVillager(fileContents);
		}
	}
	
	private static void loadCustomTrades()
	{
		List<String> fileNames = getFileNamesFromModFolder("/assets/" + VillagerTradesMod.MODID + "/data/trade_tables/");
		for ( String fileName : fileNames )
		{
			String fileContents = readFileContentsFromMod(fileName);
			addCustomTrades(fileContents);
		}
	}
	
	
	
	private static void addCustomVillager(String fileContents)
	{
		// parse the provided string as JSON
		JsonObject jsonVillager = new JsonParser().parse(fileContents).getAsJsonObject();
		
		// extract the required values from the profession object
		JsonObject jsonProfession = jsonVillager.get("profession").getAsJsonObject();
		String name = jsonProfession.get("name").getAsString();
		String texture = jsonProfession.get("texture").getAsString();
		String zombieTexture = jsonProfession.get("zombieTexture").getAsString();

		// extract the list of careers for this profession
		JsonArray jsonCareers = jsonVillager.get("careers").getAsJsonArray();
		
		// create and register the new profession
		VillagerProfession profession = new VillagerProfession(name, texture, zombieTexture);
		VillagerRegistry.instance().register(profession);
		
		// add the careers to the profession
		for ( JsonElement jsonCareer : jsonCareers )
		{
			new VillagerCareer(profession, jsonCareer.getAsString());
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
	
	
	
	private static List<String> getFileNamesFromModFolder(String resourceFolder)
	{
		List<String> fileNames = new ArrayList<String>();
		try
		{
			URI resourceUri = VillagerTradesMod.class.getResource(resourceFolder).toURI();
			Path resourcePath;
			if (resourceUri.getScheme().equals("jar")) 
			{
				FileSystem fileSystem = FileSystems.newFileSystem(resourceUri, Collections.<String, Object>emptyMap());
				resourcePath = fileSystem.getPath(resourceFolder);
			} 
			else 
			{
				resourcePath = Paths.get(resourceUri);
			}
			
			Stream<Path> walk = Files.walk(resourcePath, 1);
			for (Iterator<Path> it = walk.iterator(); it.hasNext();)
			{
				String fileName = it.next().getFileName().toString();
				if (fileName.endsWith(".json")) fileNames.add(resourceFolder.substring(1) + fileName);
			}
			walk.close();
		}
		catch(Exception ex)
		{
			System.out.println(StackTraceUtil.getStackTrace(ex));
		}
		return fileNames;	
	}
	
	private static String readFileContentsFromMod(String fileName)
	{
		String fileContents = "";
		try
		{
			InputStream stream = VillagerTradesMod.INSTANCE.getClass().getClassLoader().getResourceAsStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			
			fileContents = IOUtils.toString(stream);
			
			reader.close();
			stream.close();
			
		}
		catch(Exception ex)
		{
			System.out.println(StackTraceUtil.getStackTrace(ex));
		}
		return fileContents;
	}
	


}
