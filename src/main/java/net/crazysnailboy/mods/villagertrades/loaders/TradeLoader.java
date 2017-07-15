package net.crazysnailboy.mods.villagertrades.loaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.crazysnailboy.mods.villagertrades.VillagerTradesMod;
import net.crazysnailboy.mods.villagertrades.common.config.ModConfiguration;
import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper;
import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper.VTTVillagerCareer;
import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper.VTTVillagerProfession;
import net.crazysnailboy.mods.villagertrades.nbt.JsonToNBT;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.ExtraTradeData;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.VTTVillagerBuyingTrade;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.VTTVillagerSellingTrade;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.VTTVillagerTradeBase;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.ITradeHandler;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VTTVillagerBuyingHandler;
import net.crazysnailboy.mods.villagertrades.trades.TradeHandlers.VTTVillagerSellingHandler;
import net.crazysnailboy.mods.villagertrades.util.FileUtils;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
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

	/**
	 * Builds a map of trade files by combining files from the assets and config folders, and loads the trade data into the registry
	 */
	public static void loadCustomTradeData()
	{
		// build the file map
		HashMap<String, String> tradeFiles = FileUtils.createFileMap("trade_tables", ModConfiguration.loadTradesFromJar);

		// iterate over the filenames in the map
		for (String fileName : tradeFiles.keySet())
		{
			// get the file contents from the map for the specified name
			String fileContents = tradeFiles.get(fileName);
			try
			{
				// load the trades from the file contents
				loadTradesFromFile(fileContents);
			}
			// write to the log if something bad happened
			catch (JsonSyntaxException ex)
			{
				VillagerTradesMod.LOGGER.error("Error parsing JSON file \"" + fileName + "\"");
				VillagerTradesMod.LOGGER.error(ex.getMessage());
			}
			catch (UnknownProfessionException ex)
			{
				VillagerTradesMod.LOGGER.error("Unknown profession \"" + ex.professionName + "\" in file \"" + fileName + "\"");
			}
			catch (UnknownCareerException ex)
			{
				VillagerTradesMod.LOGGER.error("Unknown career \"" + ex.careerName + "\" in file \"" + fileName + "\"");
			}
			catch (Exception ex)
			{
				VillagerTradesMod.LOGGER.error("Error loading trades from file \"" + fileName + "\"");
				VillagerTradesMod.LOGGER.catching(ex);
			}
		}
	}


	/**
	 * Parses the contents of an individual trade file, and adds or removes the trades to or from the specified profession and career
	 * @param fileContents
	 */
	private static void loadTradesFromFile(String fileContents)
	{
		// parse the provided string as JSON
		JsonObject jsonObject = new JsonParser().parse(fileContents).getAsJsonObject();

		// identify the profession and career to apply these trades to
		String jsonProfession = jsonObject.get("Profession").getAsString();
		String jsonCareer = jsonObject.get("Career").getAsString();

		// get the specified career and profession from the villager registry
		VillagerProfession profession = VillagerRegistryHelper.getProfession(jsonProfession);
		if (profession == null) throw new UnknownProfessionException(jsonProfession);
		VillagerCareer career = new VTTVillagerProfession(profession).getCareer(jsonCareer);
		if (career == null) throw new UnknownCareerException(jsonCareer);

		// iterate over the trade recipes included in the offers object
		JsonArray jsonRecipes = jsonObject.get("Offers").getAsJsonObject().get("Recipes").getAsJsonArray();
		for (JsonElement jsonRecipe : jsonRecipes)
		{
			JsonObject jsonRecipeObject = jsonRecipe.getAsJsonObject();

			// get the level this trade change applies to, and what type of change it is
			String jsonRecipeAction = jsonRecipeObject.get("action").getAsString();
			int jsonCareerLevel = (jsonRecipeObject.has("CareerLevel") ? jsonRecipeObject.get("CareerLevel").getAsInt() : 0);

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

			// if we're supposed to clear the trades
			else if (jsonRecipeAction.equals("clear"))
			{
				// if a career level was specified...
				if (jsonCareerLevel > 0)
				{
					// clear the trades for that level
					clearCareerTrades(career, jsonCareerLevel);
				}
				// if not...
				else
				{
					// clear all the trades from the career
					clearCareerTrades(career);
				}
			}

		}

		// sort them so that the buying trades appear before the selling trades for each level (like vanilla)
		if (ModConfiguration.sortTrades) sortCareerTrades(career);
	}


	/**
	 * Creates a new villager trade from the supplied JSON object and adds it to the specified career
	 * @param career The VillagerCareer instance to add the new trade to
	 * @param careerLevel the level at which the new trade should be added
	 * @param jsonRecipeObject the JSON object representing the new trade
	 */
	private static void addTradeToCareer(VillagerCareer career, int careerLevel, JsonObject jsonRecipeObject)
	{
		JsonElement jsonBuyElement = jsonRecipeObject.get("buy");
		JsonElement jsonBuyElementB = (jsonRecipeObject.has("buyB") ? jsonRecipeObject.get("buyB") : null);
		JsonElement jsonSellElement = jsonRecipeObject.get("sell");

		ExtraTradeData extraTradeData = new ExtraTradeData();
		extraTradeData.chance = (jsonRecipeObject.has("chance") ? jsonRecipeObject.get("chance").getAsDouble() : 1);
		if (jsonRecipeObject.has("rewardExp")) extraTradeData.rewardsExp = jsonRecipeObject.get("rewardExp").getAsBoolean();
		if (jsonRecipeObject.has("maxUses")) extraTradeData.maxTradeUses = jsonRecipeObject.get("maxUses").getAsInt();


		ItemStacksAndPrices buy = getItemStacksAndPrices(jsonBuyElement);
		ItemStacksAndPrices buyB = (jsonBuyElementB != null ? getItemStacksAndPrices(jsonBuyElementB) : null);
		ItemStacksAndPrices sell = getItemStacksAndPrices(jsonSellElement);

		boolean isVillagerBuying = containsCurrencyItems(sell.getItemStacks());
		boolean isVillagerSelling = (containsCurrencyItems(buy.getItemStacks()) || (buyB != null ? containsCurrencyItems(buyB.getItemStacks()) : false));

		if (isVillagerBuying)
		{
			career.addTrade(careerLevel, new VTTVillagerBuyingTrade(buy, buyB, sell, extraTradeData));
		}
		else if (isVillagerSelling)
		{
			career.addTrade(careerLevel, new VTTVillagerSellingTrade(buy, buyB, sell, extraTradeData));
		}
		else
		{
			career.addTrade(careerLevel, new VTTVillagerTradeBase(buy, buyB, sell, extraTradeData));
		}
	}


	/**
	 * Removes a villager trade from the specified career by matching against the supplied JSON object
	 * @param career The VillagerCareer instance to remove the trade from
	 * @param careerLevel the level at which the trade is expected to exist
	 * @param jsonRecipeObject the JSON object representing the trade to remove
	 */
	private static void removeTradeFromCareer(VillagerCareer career, int careerLevel, JsonObject jsonRecipeObject)
	{
		JsonElement jsonBuyElement = jsonRecipeObject.get("buy");
		JsonElement jsonBuyElementB = (jsonRecipeObject.has("buyB") ? jsonRecipeObject.get("buyB") : null);
		JsonElement jsonSellElement = jsonRecipeObject.get("sell");

		ItemStacksAndPrices buy = getItemStacksAndPrices(jsonBuyElement);
		ItemStacksAndPrices buyB = (jsonBuyElementB != null ? getItemStacksAndPrices(jsonBuyElementB) : null);
		ItemStacksAndPrices sell = getItemStacksAndPrices(jsonSellElement);

		boolean isVillagerBuying = containsCurrencyItems(sell.getItemStacks());
		boolean isVillagerSelling = (containsCurrencyItems(buy.getItemStacks()) || (buyB != null ? containsCurrencyItems(buyB.getItemStacks()) : false));


		List<ITradeList> trades = new VTTVillagerCareer(career).getTrades(careerLevel);
		Iterator<ITradeList> iterator = trades.iterator();

		if (isVillagerBuying)
		{
			// the buy item is the item the player is giving to the villager
			ItemStack stack = buy.getItemStacks().get(0);

			while (iterator.hasNext())
			{
				ITradeList trade = iterator.next();

				ITradeHandler handler = TradeHandlers.tradeHandlers.get(trade.getClass());
				if (handler != null && handler instanceof VTTVillagerBuyingHandler)
				{
					ItemStack buyingStack = ((VTTVillagerBuyingHandler)handler).getBuyingStack(trade);
					if (ItemStack.areItemsEqual(stack, buyingStack))
					{
						iterator.remove();
					}
				}
			}

		}

		if (isVillagerSelling)
		{
			// the sell item is the item the player is receiving
			ItemStack stack = sell.getItemStacks().get(0);

			while (iterator.hasNext())
			{
				ITradeList trade = iterator.next();

				ITradeHandler handler = TradeHandlers.tradeHandlers.get(trade.getClass());
				if (handler != null && handler instanceof VTTVillagerSellingHandler)
				{
					ItemStack sellingStack = ((VTTVillagerSellingHandler)handler).getSellingStack(trade);
					if (ItemStack.areItemsEqual(stack, sellingStack))
					{
						iterator.remove();
					}
				}
			}

		}
	}


	private static void clearCareerTrades(VillagerCareer career)
	{
		List<List<ITradeList>> trades = new VTTVillagerCareer(career).getTrades();
		for (List<ITradeList> careerTrades : trades)
		{
			careerTrades.clear();
		}
		trades.clear();
	}

	private static void clearCareerTrades(VillagerCareer career, int careerLevel)
	{
		List<ITradeList> trades = new VTTVillagerCareer(career).getTrades(careerLevel);
		trades.clear();
	}


	/**
	 * Sorts the trades at each level of a career so that the buying trades appear before the selling trades
	 * @param career The VillagerCareer instance on which to sort the trades
	 */
	private static void sortCareerTrades(VillagerCareer career)
	{
		// get the trades from the career instance in a modifiable form
		List<List<ITradeList>> trades = new VTTVillagerCareer(career).getTrades();

		// iterate over the trades at each career level
		for (List<ITradeList> levelTrades : trades)
		{
			// sort the trades using a comparator
			Collections.sort(levelTrades, new Comparator<ITradeList>()
			{

				@Override
				public int compare(ITradeList tradeA, ITradeList tradeB)
				{
					// get the appropriate trade handler for the two trades being compared
					ITradeHandler handlerA = TradeHandlers.tradeHandlers.get(tradeA.getClass());
					ITradeHandler handlerB = TradeHandlers.tradeHandlers.get(tradeB.getClass());

					if (handlerA != null && handlerB != null)
					{
						// if one is a buying trade and the other a selling trade, put the buying trade first
						if (handlerA instanceof VTTVillagerBuyingHandler && handlerB instanceof VTTVillagerSellingHandler)
						{
							return -1;
						}
						else if (handlerA instanceof VTTVillagerSellingHandler && handlerB instanceof VTTVillagerBuyingHandler)
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


	private static ItemStacksAndPrices getItemStacksAndPrices(JsonElement jsonTradeElement)
	{
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		List<PriceInfo> prices = new ArrayList<PriceInfo>();

		if (jsonTradeElement.isJsonArray())
		{
			JsonArray jsonArray = jsonTradeElement.getAsJsonArray();
			for (JsonElement jsonElement : jsonArray)
			{
				JsonObject jsonObject = jsonElement.getAsJsonObject();

				String id = jsonObject.get("id").getAsString();
				ItemStack stack = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(id)));

				if (jsonObject.has("Damage"))
				{
					stack.setItemDamage(jsonObject.get("Damage").getAsInt());
				}

				if (jsonObject.has("tag"))
				{
					try
					{
						String jsonString = jsonObject.get("tag").getAsJsonObject().toString();
						NBTTagCompound compound = JsonToNBT.getTagFromJson(jsonString);
						stack.setTagCompound(compound);
					}
					catch (NBTException ex)
					{
						VillagerTradesMod.LOGGER.catching(ex);
					}
				}

				PriceInfo price = null;
				if (jsonObject.has("Count"))
				{
					int minPrice = (jsonObject.get("Count").isJsonObject() ? jsonObject.get("Count").getAsJsonObject().get("min").getAsInt() : jsonObject.get("Count").getAsInt());
					int maxPrice = (jsonObject.get("Count").isJsonObject() ? jsonObject.get("Count").getAsJsonObject().get("max").getAsInt() : jsonObject.get("Count").getAsInt());
					price = new PriceInfo(minPrice, maxPrice);
				}

				stacks.add(stack);
				prices.add(price);
			}
		}
		else
		{
			JsonObject jsonObject = jsonTradeElement.getAsJsonObject();

			int length = 1;

			for (String memberName : new String[] { "id", "Count", "Damage", "tag" })
			{
				if (jsonObject.has(memberName) && jsonObject.get(memberName).isJsonArray())
				{
					length = jsonObject.get(memberName).getAsJsonArray().size();
					break;
				}
			}

			boolean isIdArray = (jsonObject.has("id") && jsonObject.get("id").isJsonArray());
			boolean isCountArray = (jsonObject.has("Count") && jsonObject.get("Count").isJsonArray());
			boolean isDamageArray = (jsonObject.has("Damage") && jsonObject.get("Damage").isJsonArray());
			boolean isTagArray = (jsonObject.has("tag") && jsonObject.get("tag").isJsonArray());


			for (int i = 0; i < length; i++)
			{
				JsonElement idElement = (isIdArray ? jsonObject.get("id").getAsJsonArray().get(i) : jsonObject.get("id"));
				JsonElement countElement = (isCountArray ? jsonObject.get("Count").getAsJsonArray().get(i) : (jsonObject.has("Count") ? jsonObject.get("Count") : null));
				JsonElement damageElement = (isDamageArray ? jsonObject.get("Damage").getAsJsonArray().get(i) : (jsonObject.has("Damage") ? jsonObject.get("Damage") : null));
				JsonElement tagElement = (isTagArray ? jsonObject.get("tag").getAsJsonArray().get(i) : (jsonObject.has("tag") ? jsonObject.get("tag") : null));


				String id = idElement.getAsString();
				ItemStack stack = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(id)));

				if (damageElement != null)
				{
					stack.setItemDamage(damageElement.getAsInt());
				}

				if (tagElement != null)
				{
					try
					{
						NBTTagCompound tag = JsonToNBT.getTagFromJson(tagElement.toString());
						stack.setTagCompound(tag);
					}
					catch (NBTException ex)
					{
						VillagerTradesMod.LOGGER.catching(ex);
					}
				}

				PriceInfo price = null;
				if (countElement != null)
				{
					int minPrice = (countElement.isJsonObject() ? countElement.getAsJsonObject().get("min").getAsInt() : countElement.getAsInt());
					int maxPrice = (countElement.isJsonObject() ? countElement.getAsJsonObject().get("max").getAsInt() : countElement.getAsInt());
					price = new PriceInfo(minPrice, maxPrice);
				}

				stacks.add(stack);
				prices.add(price);
			}
		}

		return new ItemStacksAndPrices(stacks, prices);
	}


	private static boolean containsCurrencyItems(List<ItemStack> stacks)
	{
		for (ItemStack stack : stacks)
		{
			String resourceName = Item.REGISTRY.getNameForObject(stack.getItem()).toString();
			if (stack.getItemDamage() > 0) resourceName += "," + Integer.toString(stack.getItemDamage());

			if (ArrayUtils.contains(ModConfiguration.currencyItems, resourceName)) return true;
		}
		return false;
	}


	public static class ItemStacksAndPrices
	{

		private List<ItemStack> stacks;
		private List<PriceInfo> prices;

		public ItemStacksAndPrices(List<ItemStack> stacks, List<PriceInfo> prices)
		{
			this.stacks = stacks;
			this.prices = prices;
		}

		public List<ItemStack> getItemStacks()
		{
			return this.stacks;
		}

		public List<PriceInfo> getPrices()
		{
			return this.prices;
		}
	}


	public static class UnknownProfessionException extends RuntimeException
	{

		public String professionName;

		public UnknownProfessionException(String professionName)
		{
			this.professionName = professionName;
		}
	}

	public static class UnknownCareerException extends RuntimeException
	{

		public String careerName;

		public UnknownCareerException(String careerName)
		{
			this.careerName = careerName;
		}
	}


}
