package net.crazysnailboy.mods.villagertrades.common.registry;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper.VillagerCareerWrapper;
import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper.VillagerProfessionWrapper;
import net.crazysnailboy.mods.villagertrades.nbt.JsonToNBT;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.EmeraldForItemStacks;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.ItemStackAndEmeraldToItemStack;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.ListItemStackForEmeralds;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.ListItemStackWithPotionEffectForEmeralds;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VillagerBuysItemsHandler;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VillagerSellsItemsHandler;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VillagerTradeHandler;
import net.crazysnailboy.mods.villagertrades.util.FileUtils;
import net.crazysnailboy.mods.villagertrades.util.StackTraceUtils;
import net.minecraft.entity.passive.EntityVillager.EmeraldForItems;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.entity.passive.EntityVillager.ListEnchantedItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.PriceInfo;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;


public class TradeLoader 
{

	public static void loadCustomTradeData()
	{
		HashMap<String, String> tradeFiles = FileUtils.createFileMap("trade_tables");
		for ( String fileContents : tradeFiles.values() )
		{
			loadTradesFromFile(fileContents);
		}
	}
	

	
	private static void loadTradesFromFile(String fileContents)
	{
		// parse the provided string as JSON
		JsonObject jsonObject = new JsonParser().parse(fileContents).getAsJsonObject();

		// identify the profession and career to apply these trades to
		String jsonProfession = jsonObject.get("Profession").getAsString();
		String jsonCareer = jsonObject.get("Career").getAsString();

		// get the specified career and profession from the villager registry
		VillagerProfession profession = VillagerRegistryHelper.getProfession(jsonProfession);
		VillagerCareer career = new VillagerProfessionWrapper(profession).getCareer(jsonCareer);
		
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
				addTradeToCareer(career, jsonCareerLevel, jsonRecipeObject);
			}
			
			// or remove a trade if we're supposed to remove one
			else if (jsonRecipeAction.equals("remove"))
			{
				removeTradeFromCareer(career, jsonCareerLevel, jsonRecipeObject);
			}

			// if we're supposed to replace a trade
			else if (jsonRecipeAction.equals("replace"))
			{
				// remove the old one, and add a new one
				removeTradeFromCareer(career, jsonCareerLevel, jsonRecipeObject);
				addTradeToCareer(career, jsonCareerLevel, jsonRecipeObject);
			}
			
		}

		// sort them so that the buying trades appear before the selling trades for each level (like vanilla)
		SortCareerTrades(career);
		
	}
	
	
	
	
	/**
	 * Creates a new villager trade from the supplied JSON object and adds it to the specified career
	 * @param career The VillagerCareer instance to add the new trade to
	 * @param careerLevel the level at which the new trade should be added
	 * @param jsonRecipeObject the JSON object representing the new trade
	 */
	private static void addTradeToCareer(VillagerCareer career, int careerLevel, JsonObject jsonRecipeObject)
	{
		JsonObject jsonBuyObject = jsonRecipeObject.get("buy").getAsJsonObject();
		JsonObject jsonBuyBObject = (jsonRecipeObject.has("buyB") ? jsonRecipeObject.get("buyB").getAsJsonObject() : null);
		JsonObject jsonSellObject = jsonRecipeObject.get("sell").getAsJsonObject();
		
		ITradeList trade = null;
		
		// the villager is buying if the sell item is an emerald - i.e. the player is giving items and receiving emeralds
		if (isVillagerBuying(jsonRecipeObject))
		{
			// the buy item is the item the player is giving to the villager
			ItemStack buyingStack = getItemStack(jsonBuyObject);
			
			// the buy count is the number of items the player is giving to the villager in exchange for one emerald
			PriceInfo priceInfo = getPriceInfo(jsonBuyObject, false);
				
			// if the stack has no metadata
			if (buyingStack.getMetadata() == 0)
			{
				// create an instance of the built-in emerald for items trade
				trade = new EmeraldForItemStacks(buyingStack, priceInfo);
			}
			// if it has metadata
			else
			{
				// create an instance of our custom emerald for items trade
				trade = new EmeraldForItems(buyingStack.getItem(), priceInfo);
			}
		}

		// the villager is selling if the buy item or buyB item is an emerald - i.e. the player is giving emeralds and receiving items
		if (isVillagerSelling(jsonRecipeObject))
		{
			// the sell item is the item the player is receiving
			ItemStack stack = getItemStack(jsonSellObject);
			

			// the sell price is the number of items the player will receive
			PriceInfo sellPrice = (jsonBuyBObject == null ? getPriceInfo(jsonSellObject, true) : getPriceInfo(jsonSellObject, false));
			// the buy price is the number of emeralds the player must hand over
			PriceInfo buyPrice = (jsonBuyBObject == null ? getPriceInfo(jsonBuyObject, false) : getPriceInfo(jsonBuyBObject, false));
			
			
			if (jsonBuyBObject == null)
			{
			}
			
			
			
			// if the sell count is a range, it is the number of items the player will receive (in exchange for one emerald)
			if (jsonSellObject.get("Count").isJsonObject())
			{
				// so the sell count is the number of items the villager will give to the player
//				PriceInfo priceInfo = getPriceInfo(jsonSellObject, true);
				
				if (shouldApplyRandomPotionEffect(jsonSellObject))
				{
					trade = new ListItemStackWithPotionEffectForEmeralds(stack, sellPrice);
				}
				else
				{
					trade = new ListItemForEmeralds(stack, sellPrice);
				}
				
			}
			// otherwise the player is giving multiple emeralds to receive one item
			else
			{
				// if the player is only giving emeralds...
				if (jsonBuyBObject == null)
				{
				
					// the buy count is the number of emeralds the player must give to the villager
//					PriceInfo priceInfo = getPriceInfo(jsonBuyObject, false);
	
					if (shouldApplyRandomEnchantment(jsonSellObject))
					{
						trade = new ListEnchantedItemForEmeralds(stack.getItem(), buyPrice);
					}
					else if (shouldApplyRandomPotionEffect(jsonSellObject))
					{
						trade = new ListItemStackWithPotionEffectForEmeralds(stack, buyPrice);
					}
					else
					{
						trade = new ListItemStackForEmeralds(stack, buyPrice);
					}
					
				}

				// if the player is giving items and emeralds...
				else
				{
					// the buyB count is the number of emeralds the player must give to the villager
//					PriceInfo buyPriceInfo = getPriceInfo(jsonBuyBObject, false);
//					PriceInfo sellPriceInfo = getPriceInfo(jsonSellObject, false);
					
					trade = new ItemStackAndEmeraldToItemStack(getItemStack(jsonBuyObject), buyPrice, stack, sellPrice);
				}
				
			}
		}
		
		if (trade != null) career.addTrade(careerLevel, trade);
	}
	
	
	/**
	 * Removes a villager trade from the specified career by matching against the supplied JSON object
	 * @param career The VillagerCareer instance to remove the trade from
	 * @param careerLevel the level at which the trade is expected to exist
	 * @param jsonRecipeObject the JSON object representing the trade to remove
	 */
	private static void removeTradeFromCareer(VillagerCareer career, int careerLevel, JsonObject jsonRecipeObject)
	{
		JsonObject jsonBuyObject = jsonRecipeObject.get("buy").getAsJsonObject();
		JsonObject jsonSellObject = jsonRecipeObject.get("sell").getAsJsonObject();
		
		
		List<ITradeList> trades = new VillagerCareerWrapper(career).getTrades(careerLevel);
		Iterator<ITradeList> iterator = trades.iterator();
		
		
		// if the sell item is an emerald, the player is giving items and receiving emeralds
		if (isVillagerBuying(jsonRecipeObject))
		{
			// the buy item is the item the player is giving to the villager
			ItemStack stack = getItemStack(jsonBuyObject);
			
			while (iterator.hasNext())
			{
				ITradeList trade = iterator.next();
				
				VillagerTradeHandler handler = TradeHandlers.tradeHandlers.get(trade.getClass());
				if (handler != null && handler instanceof VillagerBuysItemsHandler)
				{
					ItemStack buyingStack = ((VillagerBuysItemsHandler)handler).getBuyingStack(trade);
					if (ItemStack.areItemsEqual(stack, buyingStack))
					{
						iterator.remove();
					}
				}
			}
			
		}
		
		// if the buy item is an emerald, the player is giving emeralds and receiving items
		if (isVillagerSelling(jsonRecipeObject))
		{
			// the sell item is the item the player is receiving
			ItemStack stack = getItemStack(jsonSellObject);
			
			while (iterator.hasNext())
			{					
				ITradeList trade = iterator.next();
				
				VillagerTradeHandler handler = TradeHandlers.tradeHandlers.get(trade.getClass());
				if (handler != null && handler instanceof VillagerSellsItemsHandler)
				{
					ItemStack sellingStack = ((VillagerSellsItemsHandler)handler).getSellingStack(trade);
					if (ItemStack.areItemsEqual(stack, sellingStack))
					{
						iterator.remove();
					}
				}
			}
			
		}
	}
	
	
	
	/**
	 * Sorts the trades at each level of a career so that the buying trades appear before the selling trades
	 * @param career The VillagerCareer instance on which to sort the trades
	 */
	private static void SortCareerTrades(VillagerCareer career)
	{
		// get the trades from the career instance in a modifiable form
		List<List<ITradeList>> trades = new VillagerCareerWrapper(career).getTrades();

		// iterate over the trades at each career level
		for ( List<ITradeList> levelTrades : trades )
		{
			// sort the trades using a comparator
			Collections.sort(levelTrades, new Comparator<ITradeList>() 
			{
				public int compare(ITradeList tradeA, ITradeList tradeB)
				{
					// get the appropriate trade handler for the two trades being compared
					VillagerTradeHandler handlerA = TradeHandlers.tradeHandlers.get(tradeA.getClass());
					VillagerTradeHandler handlerB = TradeHandlers.tradeHandlers.get(tradeB.getClass());
					
					if (handlerA != null && handlerB != null)
					{
						// if one is a buying trade and the other a selling trade, put the buying trade first
						if (handlerA instanceof VillagerBuysItemsHandler && handlerB instanceof VillagerSellsItemsHandler)
						{
							return -1;
						}
						else if (handlerA instanceof VillagerSellsItemsHandler && handlerB instanceof VillagerBuysItemsHandler)
						{
							return 1;
						}
					}
					// otherwise leave their current positions unmodified
					return 0;
				}

			});			
			
		}
		
	}
	
	
	private static boolean isVillagerSelling(JsonObject jsonRecipeObject)
	{
		try
		{
			if (jsonRecipeObject.get("buy").getAsJsonObject().get("id").getAsString().equals("minecraft:emerald")) return true;
			if (jsonRecipeObject.get("buyB").getAsJsonObject().get("id").getAsString().equals("minecraft:emerald")) return true;
		}
		catch(Exception ex) { }
		return false;
	}
	
	private static boolean isVillagerBuying(JsonObject jsonRecipeObject)
	{	
		boolean isPlayerSelling = jsonRecipeObject.get("sell").getAsJsonObject().get("id").getAsString().equals("minecraft:emerald");
		return isPlayerSelling;
	}

	
	private static boolean shouldApplyRandomEnchantment(JsonObject jsonObject)
	{
		try
		{
			return jsonObject.get("tag").getAsJsonObject().get("ench").getAsString().equals("random");
		}
		catch(Exception ex) { return false; }
	}
	
	private static boolean shouldApplyRandomPotionEffect(JsonObject jsonObject)
	{
		try
		{
			return jsonObject.get("tag").getAsJsonObject().get("potion").getAsString().equals("random");
		}
		catch(Exception ex) { return false; }
	}
	
	
	private static NBTTagCompound getTagFromJson(JsonObject jsonObject)
	{
		try 
		{
			String jsonString = jsonObject.get("tag").getAsJsonObject().toString();
			NBTTagCompound compound = JsonToNBT.getTagFromJson(jsonString);
			return compound;
		} 
		catch (NBTException ex) 
		{
			System.out.println(StackTraceUtils.getStackTrace(ex));
			return null;
		}
	}
	
	
	
	
	private static PriceInfo getPriceInfo(JsonObject jsonObject, boolean isPlayerBuying)
	{
		JsonElement jsonCountElement = jsonObject.get("Count");
		
		int minValue = (jsonCountElement.isJsonObject() ? jsonCountElement.getAsJsonObject().get("min").getAsInt() : jsonCountElement.getAsInt());
		int maxValue = (jsonCountElement.isJsonObject() ? jsonCountElement.getAsJsonObject().get("max").getAsInt() : jsonCountElement.getAsInt());
		
//		JsonObject jsonCountObject = jsonObject.get("Count").getAsJsonObject();
//		int minValue = jsonCountObject.get("min").getAsInt();
//		int maxValue = jsonCountObject.get("max").getAsInt();
		
		if (isPlayerBuying)
		{
			return new PriceInfo(0 - maxValue, 0 - minValue);
		}
		else
		{
			return new PriceInfo(minValue, maxValue);
		}
		
	}
	
	
	
	
	
	
	private static ItemStack getItemStack(JsonObject jsonObject)
	{
		
		String resourceName = jsonObject.get("id").getAsString();
		
		ItemStack stack = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(resourceName)));
		
		if (jsonObject.has("Damage"))
		{
			stack.setItemDamage(jsonObject.get("Damage").getAsInt());
		}
		
		if (jsonObject.has("Count") && jsonObject.get("Count").isJsonPrimitive())
		{
			stack.setCount(jsonObject.get("Count").getAsInt());
		}
		
		if (jsonObject.has("tag"))
		{
			boolean tagIsRandomEnchant = false; 
			boolean tagIsRandomPotionEffect = false; 
			
			try 
			{ 
				tagIsRandomEnchant = jsonObject.get("tag").getAsJsonObject().get("ench").getAsString().equals("random"); 
			} 
			catch(Exception ex) { }
			
			try 
			{ 
				tagIsRandomPotionEffect = jsonObject.get("tag").getAsJsonObject().get("potion").getAsString().equals("random"); 
			} 
			catch(Exception ex) { }
			
			
			if (!(tagIsRandomEnchant || tagIsRandomPotionEffect))
			{
				NBTTagCompound compound = getTagFromJson(jsonObject);
				if (compound != null) stack.setTagCompound(compound);
			}
		}
		
		
		return stack;		
	}
	

}
