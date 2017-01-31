package net.crazysnailboy.mods.villagertrades.trades;

import java.util.Random;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
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

        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
        {
            int amount = 1;
            if (this.priceInfo != null) amount = this.priceInfo.getPrice(random);
            this.stack.setCount(amount);
            
            recipeList.add(new MerchantRecipe(this.stack, Items.EMERALD));
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

        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
        {
            int amount = 1;
            if (this.priceInfo != null) amount = this.priceInfo.getPrice(random);

            ItemStack itemstack;
            ItemStack itemstack1 = stack.copy();

            if (amount < 0)
            {
                itemstack = new ItemStack(Items.EMERALD);
                itemstack1.setCount(0 - amount);
            }
            else
            {
                itemstack = new ItemStack(Items.EMERALD, amount);
                itemstack1.setCount(1);
            }

            recipeList.add(new MerchantRecipe(itemstack, itemstack1));
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
    

}
