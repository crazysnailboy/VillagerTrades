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
import net.minecraft.entity.passive.EntityVillager.EmeraldForItems;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.PriceInfo;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class TableLoader 
{
	
	
	public static void preInit()
	{
		loadCustomVillagers();
		loadTradeTables();
	}
	
	
	private static void loadCustomVillagers()
	{
		List<String> fileNames = getCustomVillagersFromMod("/assets/" + VillagerTradesMod.MODID + "/data/villagers/");
		for ( String fileName : fileNames )
		{
			String fileContents = readFileContentsFromMod(fileName);
			addCustomVillager(fileContents);
		}
	}
	
	private static List<String> getCustomVillagersFromMod(String resourceFolder)
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
		
	private static void addCustomVillager(String fileContents)
	{
		
		JsonObject jsonVillager = new JsonParser().parse(fileContents).getAsJsonObject();
		
		JsonObject jsonProfession = jsonVillager.get("profession").getAsJsonObject();
		
		String name = jsonProfession.get("name").getAsString();
		String texture = jsonProfession.get("texture").getAsString();
		String zombieTexture = jsonProfession.get("zombieTexture").getAsString();
		
		JsonArray jsonCareers = jsonVillager.get("careers").getAsJsonArray();
		
		
		VillagerProfession profession = new VillagerProfession(name, texture, zombieTexture);
		VillagerRegistry.instance().register(profession);
		
		
		for ( JsonElement jsonCareer : jsonCareers )
		{
			new VillagerCareer(profession, jsonCareer.getAsString());
		}
		
		
		
	}
	
	
	private static void loadTradeTables()
	{
		List<String> fileNames = getTablesNamesFromMod("/assets/" + VillagerTradesMod.MODID + "/trade_tables/");
		
		for ( String fileName : fileNames )
		{
			String fileContents = readFileContentsFromMod(fileName); //readTableContents(fileName);
			DeserializeTable(fileContents);
		}
	}
	
	private static List<String> getTablesNamesFromMod(String resourceFolder)
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
	
	private static void getTablesFromResourcePackFolder()
	{
	}
	
	private static void getTablesFromWorldData()
	{
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
	
	
	
//	private static String readTableContents(String fileName)
//	{
//		String resourceFolder = "assets/" + VillagerTradesMod.MODID + "/trade_tables/";
//		String fileContents = "";
//		try
//		{
//			InputStream stream = VillagerTradesMod.INSTANCE.getClass().getClassLoader().getResourceAsStream(resourceFolder + fileName);
//			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
//			
//			fileContents = IOUtils.toString(stream);
//			
//			reader.close();
//			stream.close();
//			
//		}
//		catch(Exception ex)
//		{
//			System.out.println(StackTraceUtil.getStackTrace(ex));
//		}
//		return fileContents;
//	}
	
	

	private static void DeserializeTable(String fileContents)
	{
		JsonObject jsonObject = new JsonParser().parse(fileContents).getAsJsonObject();
		
		String jsonProfession = jsonObject.get("Profession").getAsString();
		String jsonCareer = jsonObject.get("Career").getAsString();
		JsonArray jsonRecipes = jsonObject.get("Offers").getAsJsonObject().get("Recipes").getAsJsonArray();
		
	
		VillagerProfession profession = VillagerRegistryHelper.getProfession(jsonProfession);
		VillagerCareer career = VillagerRegistryHelper.getCareer(profession, jsonCareer);


		
		for ( JsonElement jsonRecipe : jsonRecipes )
		{
			JsonObject jsonRecipeObject = jsonRecipe.getAsJsonObject();
			
			String jsonRecipeAction = jsonRecipeObject.get("action").getAsString();
			
			if (jsonRecipeAction.equals("add"))
			{
				JsonObject jsonBuyObject = jsonRecipeObject.get("buy").getAsJsonObject();
				JsonObject jsonSellObject = jsonRecipeObject.get("sell").getAsJsonObject();
				
				String buyId = jsonBuyObject.get("id").getAsString();
				String sellId = jsonSellObject.get("id").getAsString();
				int jsonCareerLevel = jsonRecipeObject.get("CareerLevel").getAsInt();
				
				if (sellId.equals("minecraft:emerald"))
				{
					JsonObject jsonBuyCount = jsonBuyObject.get("Count").getAsJsonObject();
					int jsonBuyCountMin = jsonBuyCount.get("min").getAsInt();
					int jsonBuyCountMax = jsonBuyCount.get("max").getAsInt();
					
					Item buyItem = Item.REGISTRY.getObject(new ResourceLocation(buyId));
					
					career.addTrade(jsonCareerLevel, new EmeraldForItems(buyItem, new PriceInfo(jsonBuyCountMin, jsonBuyCountMax)));
				}
				
				else if (buyId.equals("minecraft:emerald"))
				{
					if (jsonSellObject.get("Count").isJsonObject())
					{
						JsonObject jsonSellCount = jsonSellObject.get("Count").getAsJsonObject();
						int jsonSellCountMin = 0 - jsonSellCount.get("min").getAsInt();
						int jsonSellCountMax = 0 - jsonSellCount.get("max").getAsInt();
						
						Item sellItem = Item.REGISTRY.getObject(new ResourceLocation(sellId));
						
						career.addTrade(jsonCareerLevel, new ListItemForEmeralds(sellItem, new PriceInfo(jsonSellCountMin, jsonSellCountMax)));
					}
					else
					{
						JsonObject jsonBuyCount = jsonBuyObject.get("Count").getAsJsonObject();
						int jsonBuyCountMin = jsonBuyCount.get("min").getAsInt();
						int jsonBuyCountMax = jsonBuyCount.get("max").getAsInt();
						
						Item sellItem = Item.REGISTRY.getObject(new ResourceLocation(sellId));
						
						career.addTrade(jsonCareerLevel, new ListItemForEmeralds(sellItem, new PriceInfo(jsonBuyCountMin, jsonBuyCountMax)));
					}
					
					
					
					
				}
						
			}
			
			else if (jsonRecipeAction.equals("remove"))
			{
				JsonObject jsonBuyObject = jsonRecipeObject.get("buy").getAsJsonObject();
				JsonObject jsonSellObject = jsonRecipeObject.get("sell").getAsJsonObject();
				
				String buyId = jsonBuyObject.get("id").getAsString();
				String sellId = jsonSellObject.get("id").getAsString();
				int jsonCareerLevel = jsonRecipeObject.get("CareerLevel").getAsInt();
				
				List<ITradeList> trades = VillagerRegistryHelper.getCareerTrades(career, jsonCareerLevel);
				Iterator<ITradeList> iterator = trades.iterator();
				
				if (sellId.equals("minecraft:emerald"))
				{
					Item buyItem = Item.REGISTRY.getObject(new ResourceLocation(buyId));
					
					while (iterator.hasNext())
					{
						ITradeList _trade = iterator.next();
						if (_trade instanceof EmeraldForItems)
						{
							EmeraldForItems trade = (EmeraldForItems)_trade;
							if (trade.buyingItem == buyItem)
							{
								iterator.remove();
							}
						}
					}
					
				}
				else if (buyId.equals("minecraft:emerald"))
				{
					Item sellItem = Item.REGISTRY.getObject(new ResourceLocation(sellId));
					
					while (iterator.hasNext())
					{					
						ITradeList _trade = iterator.next();
						if (_trade instanceof ListItemForEmeralds)
						{
							ListItemForEmeralds trade = (ListItemForEmeralds)_trade;
							if (trade.itemToBuy.getItem() == sellItem)
							{
								iterator.remove();
							}
						}
					}
					
				}
				
			}
			
			
		}

		
	}
	
	
	


}
