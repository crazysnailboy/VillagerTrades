package net.crazysnailboy.mods.villagertrades.trades;

import java.util.HashMap;

import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.VTTVillagerBuyingTrade;
import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.VTTVillagerSellingTrade;
import net.minecraft.entity.passive.EntityVillager.EmeraldForItems;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.entity.passive.EntityVillager.ItemAndEmeraldToItem;
import net.minecraft.entity.passive.EntityVillager.ListEnchantedBookForEmeralds;
import net.minecraft.entity.passive.EntityVillager.ListEnchantedItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;


/**
 * The trade handlers provide an abstraction from the differences in the various EntityVillager.ITradeList implementations
 * and give a uniform way of getting the buying stack and/or the selling stack for each implementation.
 *
 * They also provide a convenient way of classifying the trades into buying and selling trades, which is used for sorting.
 *
 */
public class TradeHandlers
{

	public static abstract interface ITradeHandler
	{
	}

	public static final HashMap<Class<? extends ITradeList>, ITradeHandler> tradeHandlers;

	static
	{
		tradeHandlers = new HashMap<Class<? extends ITradeList>, ITradeHandler>();

		// villager buying handlers
		// built-in
		tradeHandlers.put(EmeraldForItems.class, new EmeraldForItemsHandler());
		// custom
		tradeHandlers.put(VTTVillagerBuyingTrade.class, new VTTVillagerBuyingHandler());

		// villager selling handlers
		// built-in
		tradeHandlers.put(ItemAndEmeraldToItem.class, new ItemAndEmeraldToItemHandler());
		tradeHandlers.put(ListEnchantedBookForEmeralds.class, new ListEnchantedBookForEmeraldsHandler());
		tradeHandlers.put(ListEnchantedItemForEmeralds.class, new ListEnchantedItemForEmeraldsHandler());
		tradeHandlers.put(ListItemForEmeralds.class, new ListItemForEmeraldsHandler());
		// custom
		tradeHandlers.put(VTTVillagerSellingTrade.class, new VTTVillagerSellingHandler());
	}



	// ==================================================
	// HANDLERS FOR VILLAGER BUYING TRADES
	// ==================================================

	public static class VTTVillagerBuyingHandler implements ITradeHandler
	{
		public ItemStack getBuyingStack(ITradeList trade)
		{
			return null;
		}
	}

	public static class EmeraldForItemsHandler extends VTTVillagerBuyingHandler
	{
		@Override
		public ItemStack getBuyingStack(ITradeList t)
		{
			EmeraldForItems trade = (EmeraldForItems)t;
			ItemStack stack = new ItemStack(trade.buyingItem, 1, 0);
			return stack;
		}
	}


	// ==================================================
	// HANDLERS FOR VILLAGER SELLING TRADES
	// ==================================================

	public static class VTTVillagerSellingHandler implements ITradeHandler
	{
		public ItemStack getSellingStack(ITradeList trade)
		{
			return null;
		}
	}

	public static class ItemAndEmeraldToItemHandler extends VTTVillagerSellingHandler
	{
		@Override
		public ItemStack getSellingStack(ITradeList t)
		{
			ItemAndEmeraldToItem trade = (ItemAndEmeraldToItem)t;
			ItemStack stack = trade.sellingItemstack.copy();
			return stack;
		}
	}

	public static class ListEnchantedBookForEmeraldsHandler extends VTTVillagerSellingHandler
	{
		@Override
		public ItemStack getSellingStack(ITradeList t)
		{
			ListEnchantedBookForEmeralds trade = (ListEnchantedBookForEmeralds)t;
			ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK, 1, 0);
			return stack;
		}
	}

	public static class ListEnchantedItemForEmeraldsHandler extends VTTVillagerSellingHandler
	{
		@Override
		public ItemStack getSellingStack(ITradeList t)
		{
			ListEnchantedItemForEmeralds trade = (ListEnchantedItemForEmeralds)t;
			ItemStack stack = trade.enchantedItemStack.copy();
			return stack;
		}
	}

	public static class ListItemForEmeraldsHandler extends VTTVillagerSellingHandler
	{
		@Override
		public ItemStack getSellingStack(ITradeList t)
		{
			ListItemForEmeralds trade = (ListItemForEmeralds)t;
			ItemStack stack = trade.itemToBuy.copy();
			return stack;
		}
	}

}
