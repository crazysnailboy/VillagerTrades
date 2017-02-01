package net.crazysnailboy.mods.villagertrades.trades;

import java.util.Random;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class CustomTrades 
{

    /**
     * A villager buying trade which allows the villager to buy items with metadata and NBT data
     * by passing the ItemStack directly to the MerchantRecipe, modifying only the number of items
     * 
     * Modified from EntityVillager.EmeraldForItems
     * 
     */
    public static class EmeraldForItemStacks implements EntityVillager.ITradeList
    {
        public ItemStack stack;
        public EntityVillager.PriceInfo priceInfo;

        public EmeraldForItemStacks(ItemStack stack, EntityVillager.PriceInfo priceInfo)
        {
            this.stack = stack.copy();
            this.priceInfo = priceInfo;
        }

        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
        {
            int amount = 1;
            if (this.priceInfo != null) amount = this.priceInfo.getPrice(random);
            this.stack.setCount(amount);
            
            recipeList.add(new MerchantRecipe(this.stack, Items.EMERALD));
        }
    }
    
    
    
    
    public static class VTTListItemsForEmeralds implements EntityVillager.ITradeList
    {
    	
        public EntityVillager.PriceInfo buyingPriceInfo;
        public ItemStack sellingItemstack;
        public EntityVillager.PriceInfo sellingPriceInfo;

        
        public VTTListItemsForEmeralds(EntityVillager.PriceInfo buyingPriceInfo, ItemStack sellingItemstack, EntityVillager.PriceInfo sellingPriceInfo)
    	{
            this.buyingPriceInfo = buyingPriceInfo;
            this.sellingItemstack = sellingItemstack.copy();
            this.sellingPriceInfo = sellingPriceInfo;
    	}
    	
        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
        {
            int buyAmount = this.buyingPriceInfo.getPrice(random);
            int sellAmount = this.sellingPriceInfo.getPrice(random);
            
            ItemStack buy1 = new ItemStack(Items.EMERALD, buyAmount);
            ItemStack sell = this.sellingItemstack.copy(); sell.setCount(sellAmount);
            
            
            recipeList.add(new MerchantRecipe(buy1, sell));
        }
    }
    
    
    
    /**
     * A villager selling trade allows the villager to sell items with metadata and NBT data 
     * by passing the ItemStack directly to the MerchantRecipe, modifying only the number of items
     *
     * Modified from EntityVillager.ListItemForEmeralds
     */
    public static class ListItemStackForEmeralds implements EntityVillager.ITradeList
    {
        public ItemStack stack;
        public EntityVillager.PriceInfo priceInfo;

        public ListItemStackForEmeralds(ItemStack stack, EntityVillager.PriceInfo priceInfo)
        {
            this.stack = stack.copy();
            this.priceInfo = priceInfo;
        }

        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
        {
            int amount = 1;
            if (this.priceInfo != null) amount = this.priceInfo.getPrice(random);

            ItemStack buy1;
            ItemStack sell = stack.copy();

            if (amount < 0)
            {
                buy1 = new ItemStack(Items.EMERALD);
                sell.setCount(0 - amount);
            }
            else
            {
                buy1 = new ItemStack(Items.EMERALD, amount);
                sell.setCount(1);
            }

            recipeList.add(new MerchantRecipe(buy1, sell));
        }
    }
    
    
    /**
     * A villager selling trade allows the villager to sell items with random potion effects 
     *
     * Modified from EntityVillager.ListEnchantedItemForEmeralds
     */
    public static class ListItemStackWithPotionEffectForEmeralds implements EntityVillager.ITradeList
    {
        public ItemStack stack;
        public EntityVillager.PriceInfo priceInfo;

        public ListItemStackWithPotionEffectForEmeralds(ItemStack stack, EntityVillager.PriceInfo priceInfo)
        {
            this.stack = stack.copy();
            this.priceInfo = priceInfo;
        }

        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
        {
        	System.out.println("ListItemStackWithPotionEffectForEmeralds$addMerchantRecipe");
        	
            int amount = 1;

            if (this.priceInfo != null)
            {
                amount = this.priceInfo.getPrice(random);
            }
            
            PotionType randomPotion = PotionType.REGISTRY.getRandomObject(random);
            System.out.println("randomPotion: " + randomPotion.getRegistryName());
            
            ItemStack itemstack = new ItemStack(Items.EMERALD, amount);
            ItemStack itemstack1 = PotionUtils.addPotionToItemStack(this.stack.copy(), PotionType.REGISTRY.getRandomObject(random));
            recipeList.add(new MerchantRecipe(itemstack, itemstack1));
        }
    }

    
    
    public static class ItemStackAndEmeraldToItemStack implements EntityVillager.ITradeList
    {
        /** The itemstack to buy with an emerald. The Item and damage value is used only, any tag data is not retained. */
        public ItemStack buyingItemStack;
        /** The price info defining the amount of the buying item required with 1 emerald to match the selling item. */
        public EntityVillager.PriceInfo buyingPriceInfo;
        /** The itemstack to sell. The item and damage value are used only, any tag data is not retained. */
        public ItemStack sellingItemstack;
        public EntityVillager.PriceInfo sellingPriceInfo;

        /**
         * @param buyingStack The itemstack to buy with an emerald
         * @param buyingPriceInfo The price info defining the amount of the buying item required with 1 emerald to match the selling item
         * @param sellingItemstack The itemstack to sell
         * @param sellingPriceInfo
         */
        public ItemStackAndEmeraldToItemStack(ItemStack buyingStack, EntityVillager.PriceInfo buyingPriceInfo, ItemStack sellingItemstack, EntityVillager.PriceInfo sellingPriceInfo)
        {
            this.buyingItemStack = buyingStack.copy();
            this.buyingPriceInfo = buyingPriceInfo;
            this.sellingItemstack = sellingItemstack.copy();
            this.sellingPriceInfo = sellingPriceInfo;
        }

        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
        {
            int buyAmount = this.buyingPriceInfo.getPrice(random);
            int sellAmount = this.sellingPriceInfo.getPrice(random);
            
            ItemStack buy1 = this.buyingItemStack.copy(); buy1.setCount(1); // new ItemStack(this.buyingItemStack.getItem(), buyAmount, this.buyingItemStack.getMetadata());
            ItemStack buy2 = new ItemStack(Items.EMERALD, buyAmount);
            ItemStack sell = this.sellingItemstack.copy(); sell.setCount(sellAmount); // new ItemStack(this.sellingItemstack.getItem(), sellAmount, this.sellingItemstack.getMetadata());
            
            recipeList.add(new MerchantRecipe(buy1, buy2, sell));
        }
    }
    
    

}
