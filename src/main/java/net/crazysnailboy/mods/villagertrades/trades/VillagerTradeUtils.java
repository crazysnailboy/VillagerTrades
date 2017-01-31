package net.crazysnailboy.mods.villagertrades.trades;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;

import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper;
import net.crazysnailboy.mods.villagertrades.nbt.JsonToNBT;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.EmeraldForItemStacks;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.ListItemStackForEmeralds;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.ListItemStackWithPotionEffectForEmeralds;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VillagerBuysItemsHandler;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VillagerSellsItemsHandler;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VillagerTradeHandler;
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

public class VillagerTradeUtils 
{
	
	
	/**
	 * Creates a new villager trade from the supplied JSON object and adds it to the specified career
	 * @param career The VillagerCareer instance to add the new trade to
	 * @param careerLevel the level at which the new trade should be added
	 * @param jsonRecipeObject the JSON object representing the new trade
	 */
	public static void addTradeToCareer(VillagerCareer career, int careerLevel, JsonObject jsonRecipeObject)
	{
		JsonObject jsonBuyObject = jsonRecipeObject.get("buy").getAsJsonObject();
		JsonObject jsonSellObject = jsonRecipeObject.get("sell").getAsJsonObject();
		
		ITradeList trade = null;
		
		// if the sell item is an emerald, the player is giving items and receiving emeralds
		if (isVillagerBuying(jsonRecipeObject))
		{
			// the buy item is the item the player is giving to the villager
			ItemStack stack = getItemStack(jsonBuyObject);
			
			// the buy count is the number of items the player is giving to the villager in exchange for one emerald
			PriceInfo priceInfo = getPriceInfo(jsonBuyObject, false);
				
			// if the stack has no metadata
			if (stack.getMetadata() == 0)
			{
				// create an instance of the built-in emerald for items trade
				trade = new EmeraldForItemStacks(stack, priceInfo);
			}
			// if it has metadata
			else
			{
				// create an instance of our custom emerald for items trade
				trade = new EmeraldForItems(stack.getItem(), priceInfo);
			}
		}
		
		// if the buy item is an emerald, the player is giving emeralds and receiving items
		if (isVillagerSelling(jsonRecipeObject))
		{
			// the sell item is the item the player is receiving
			ItemStack stack = getItemStack(jsonSellObject);

			
			
			
			// if the sell count is a range, it is the number of items the player will receive in exchange for one emerald
			if (jsonSellObject.get("Count").isJsonObject())
			{
				// so the sell count is the number of items the villager will give to the player
				PriceInfo priceInfo = getPriceInfo(jsonSellObject, true);
				
				if (shouldApplyRandomPotionEffect(jsonSellObject))
				{
					trade = new ListItemStackWithPotionEffectForEmeralds(stack, priceInfo);
				}
				else
				{
					trade = new ListItemForEmeralds(stack, priceInfo);
				}
				
			}
			// otherwise the player is giving multiple emeralds to receive one item
			else
			{
				// so the buy count is the number of emeralds the player must give to the villager
				PriceInfo priceInfo = getPriceInfo(jsonBuyObject, false);

				if (shouldApplyRandomEnchantment(jsonSellObject))
				{
					trade = new ListEnchantedItemForEmeralds(stack.getItem(), priceInfo);
				}
				else if (shouldApplyRandomPotionEffect(jsonSellObject))
				{
					trade = new ListItemStackWithPotionEffectForEmeralds(stack, priceInfo);
				}
				else if (jsonSellObject.has("tag"))
				{
					NBTTagCompound compound = getTagFromJson(jsonSellObject);
					if (compound != null) stack.setTagCompound(compound);
					trade = new ListItemStackForEmeralds(stack, priceInfo);
				}
				else
				{
					trade = new ListItemForEmeralds(stack.getItem(), priceInfo);
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
	public static void removeTradeFromCareer(VillagerCareer career, int careerLevel, JsonObject jsonRecipeObject)
	{
		JsonObject jsonBuyObject = jsonRecipeObject.get("buy").getAsJsonObject();
		JsonObject jsonSellObject = jsonRecipeObject.get("sell").getAsJsonObject();
		
		
		List<ITradeList> trades = VillagerRegistryHelper.getCareerTradesForLevel(career, careerLevel);
		Iterator<ITradeList> iterator = trades.iterator();
		
		
		// if the sell item is an emerald, the player is giving items and receiving emeralds
		if (VillagerTradeUtils.isVillagerBuying(jsonRecipeObject))
		{
			// the buy item is the item the player is giving to the villager
			ItemStack stack = VillagerTradeUtils.getItemStack(jsonBuyObject);
			
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
		if (VillagerTradeUtils.isVillagerSelling(jsonRecipeObject))
		{
			// the sell item is the item the player is receiving
			ItemStack stack = VillagerTradeUtils.getItemStack(jsonSellObject);
			
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
	public static void SortCareerTrades(VillagerCareer career)
	{
		// get the trades from the career instance in a modifiable form
		List<List<ITradeList>> trades = VillagerRegistryHelper.getCareerTrades(career);

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
		boolean isPlayerBuying = jsonRecipeObject.get("buy").getAsJsonObject().get("id").getAsString().equals("minecraft:emerald");
		return isPlayerBuying;
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
		JsonObject jsonCountObject = jsonObject.get("Count").getAsJsonObject();
		int minValue = jsonCountObject.get("min").getAsInt();
		int maxValue = jsonCountObject.get("max").getAsInt();
		
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
		
		if (jsonObject.has("Count") &&  jsonObject.get("Count").isJsonPrimitive())
		{
			stack.setCount(jsonObject.get("Count").getAsInt());
		}
		
		return stack;		
	}
	
	
}
