package net.crazysnailboy.mods.villagertrades.trades;

import java.util.Random;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityVillager.PriceInfo;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class CustomTrades 
{

	/**
	 * A villager buying trade where the player receives an emerald in exchange for multiple items
	 * 
	 */
	public static class VTTEmeraldsForItems implements EntityVillager.ITradeList
	{
		public ItemStack buy1;
		public PriceInfo buyPrice;
		public PriceInfo sellPrice;

		public VTTEmeraldsForItems(ItemStack buy1, PriceInfo buyPrice)
		{
			this.buy1 = buy1.copy();
			this.buyPrice = buyPrice;
			this.sellPrice = new PriceInfo(-1, -1);
		}
		
		public VTTEmeraldsForItems(ItemStack buy1, PriceInfo buyPrice, PriceInfo sellPrice)
		{
			this.buy1 = buy1.copy();
			this.buyPrice = buyPrice;
			this.sellPrice = sellPrice;
		}

		@Override
		public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
		{
			int buyAmount = this.buyPrice.getPrice(random);
			int sellAmount = this.sellPrice.getPrice(random);
			
			ItemStack buy1 = this.buy1.copy(); buy1.setCount(buyAmount); 
			ItemStack sell = new ItemStack(Items.EMERALD, Math.abs(sellAmount));
			
			recipeList.add(new MerchantRecipe(this.buy1, new ItemStack(Items.EMERALD, sellAmount)));
		}
	}
	
	
	
	
	/**
	 * A villager selling trade where the player receives items from the villager in exchange for emeralds
	 *
	 */
	public static class VTTItemsForEmeralds implements EntityVillager.ITradeList
	{
		public PriceInfo buyPrice;
		public ItemStack sell;
		public PriceInfo sellPrice;

		
		public VTTItemsForEmeralds(PriceInfo buyPrice, ItemStack sell, PriceInfo sellPrice)
		{
			this.buyPrice = buyPrice;
			this.sell = sell.copy();
			this.sellPrice = sellPrice;
		}
		
		@Override
		public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
		{
			int buyAmount = this.buyPrice.getPrice(random);
			int sellAmount = this.sellPrice.getPrice(random);
			
			ItemStack buy1 = new ItemStack(Items.EMERALD, buyAmount);
			ItemStack sell = this.sell.copy(); sell.setCount(Math.abs(sellAmount));
			
			
			NBTTagCompound tag = sell.getTagCompound();
			
			if (tag != null && tag.hasKey("ench") && tag.getString("ench").equals("random"))
			{
				sell.removeSubCompound("ench");
				sell = EnchantmentHelper.addRandomEnchantment(random, sell, 5 + random.nextInt(15), false);
			}
			
			if (tag != null && tag.hasKey("Potion") && tag.getString("Potion").equals("random"))
			{
				sell.removeSubCompound("Potion");
				sell = PotionUtils.addPotionToItemStack(sell, PotionType.REGISTRY.getRandomObject(random));
			}
			
			recipeList.add(new MerchantRecipe(buy1, sell));
		}
	}
	
	
	
	
	/**
	 * A villager selling trade where the player receives items from the villager in exchange for items and emeralds
	 *
	 */
	public static class VTTItemsAndEmeraldsForItems implements EntityVillager.ITradeList
	{
		public ItemStack buy1;
		public PriceInfo buyPrice1;
		public PriceInfo buyPrice2;
		public ItemStack sell;
		public PriceInfo sellPrice;


		public VTTItemsAndEmeraldsForItems(ItemStack buy1, PriceInfo buyPrice1, ItemStack sell, PriceInfo sellPrice)
		{
			this.buy1 = buy1.copy();
			this.buyPrice1 = buyPrice1;
			this.buyPrice2 = new PriceInfo(1, 1);
			this.sell = sell.copy();
			this.sellPrice = sellPrice;
		}
		
		public VTTItemsAndEmeraldsForItems(ItemStack buy1, PriceInfo buyPrice1, PriceInfo buyPrice2, ItemStack sell, PriceInfo sellPrice)
		{
			this.buy1 = buy1.copy();
			this.buyPrice1 = buyPrice1;
			this.buyPrice2 = buyPrice2;
			this.sell = sell.copy();
			this.sellPrice = sellPrice;
		}
		

		@Override
		public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
		{
			int buyAmount1 = this.buyPrice1.getPrice(random);
			int buyAmount2 = this.buyPrice2.getPrice(random);
			int sellAmount = this.sellPrice.getPrice(random);
			
			ItemStack buy1 = this.buy1.copy(); buy1.setCount(buyAmount1);
			ItemStack buy2 = new ItemStack(Items.EMERALD, buyAmount2);
			ItemStack sell = this.sell.copy(); sell.setCount(sellAmount);
			
			NBTTagCompound tag = sell.getTagCompound();
			
			if (tag != null && tag.hasKey("ench") && tag.getString("ench").equals("random"))
			{
				sell.removeSubCompound("ench");
				sell = EnchantmentHelper.addRandomEnchantment(random, sell, 5 + random.nextInt(15), false);
			}
			
			if (tag != null && tag.hasKey("Potion") && tag.getString("Potion").equals("random"))
			{
				sell.removeSubCompound("Potion");
				sell = PotionUtils.addPotionToItemStack(sell, PotionType.REGISTRY.getRandomObject(random));
			}
			
			recipeList.add(new MerchantRecipe(buy1, buy2, sell));
		}
	}
	
	

}
