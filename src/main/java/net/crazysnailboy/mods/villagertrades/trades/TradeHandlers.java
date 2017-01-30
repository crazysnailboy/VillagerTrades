package net.crazysnailboy.mods.villagertrades.trades;

import java.util.HashMap;

import net.crazysnailboy.mods.villagertrades.trades.CustomTrades.EmeraldForItemsWithMetadata;
import net.minecraft.entity.passive.EntityVillager.EmeraldForItems;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.entity.passive.EntityVillager.ItemAndEmeraldToItem;
import net.minecraft.entity.passive.EntityVillager.ListEnchantedBookForEmeralds;
import net.minecraft.entity.passive.EntityVillager.ListEnchantedItemForEmeralds;
import net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;


public class TradeHandlers 
{
	
	public static abstract class VillagerTradeHandler
	{
	}
	
	public static abstract class VillagerBuysItemsHandler extends VillagerTradeHandler
	{
		public abstract ItemStack getBuyingStack(ITradeList trade);
	}
	
	public static abstract class VillagerSellsItemsHandler extends VillagerTradeHandler
	{
		public abstract ItemStack getSellingStack(ITradeList trade);
	}

	
	public static final HashMap<Class<? extends ITradeList>, VillagerTradeHandler> tradeHandlers;
	
	static
	{
		tradeHandlers = new HashMap<Class<? extends ITradeList>, VillagerTradeHandler>();
		
		// villager buying handlers
		tradeHandlers.put(EmeraldForItems.class, new EmeraldForItemsHandler());
		tradeHandlers.put(EmeraldForItemsWithMetadata.class, new EmeraldForItemsWithMetadataHandler());
		
		// villager selling handlers
		tradeHandlers.put(ItemAndEmeraldToItem.class, new ItemAndEmeraldToItemHandler());
		tradeHandlers.put(ListEnchantedBookForEmeralds.class, new ListEnchantedBookForEmeraldsHandler());
		tradeHandlers.put(ListEnchantedItemForEmeralds.class, new ListEnchantedItemForEmeraldsHandler());
		tradeHandlers.put(ListItemForEmeralds.class, new ListItemForEmeraldsHandler());
	}
	
	
	
	public static class EmeraldForItemsHandler extends VillagerBuysItemsHandler
	{
		@Override
		public ItemStack getBuyingStack(ITradeList t)
		{
			EmeraldForItems trade = (EmeraldForItems)t;
			ItemStack stack = new ItemStack(trade.buyingItem, 1, 0); 
			return stack;
		}
	}
	
	public static class EmeraldForItemsWithMetadataHandler extends VillagerBuysItemsHandler
	{
		@Override
		public ItemStack getBuyingStack(ITradeList t)
		{
			EmeraldForItemsWithMetadata trade = (EmeraldForItemsWithMetadata)t;
			ItemStack stack = trade.buyingItemStack.copy(); 
			return stack;
		}
	}

	
	
	public static class ItemAndEmeraldToItemHandler extends VillagerSellsItemsHandler
	{
		@Override
		public ItemStack getSellingStack(ITradeList t)
		{
			ItemAndEmeraldToItem trade = (ItemAndEmeraldToItem)t;
			ItemStack stack = trade.sellingItemstack.copy(); 
			return stack;
		}
	}
	
	public static class ListEnchantedBookForEmeraldsHandler extends VillagerSellsItemsHandler
	{
		@Override
		public ItemStack getSellingStack(ITradeList t)
		{
			ListEnchantedBookForEmeralds trade = (ListEnchantedBookForEmeralds)t;
			ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK, 1, 0);
			return stack;
		}
	}
	
	public static class ListEnchantedItemForEmeraldsHandler extends VillagerSellsItemsHandler
	{
		@Override
		public ItemStack getSellingStack(ITradeList t)
		{
			ListEnchantedItemForEmeralds trade = (ListEnchantedItemForEmeralds)t; 
			ItemStack stack = trade.enchantedItemStack.copy();
			return stack;
		}
	}
	
	public static class ListItemForEmeraldsHandler extends VillagerSellsItemsHandler
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
