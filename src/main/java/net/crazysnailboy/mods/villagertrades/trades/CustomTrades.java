package net.crazysnailboy.mods.villagertrades.trades;

import java.util.Random;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class CustomTrades 
{

    public static class EmeraldForItemsWithMetadata implements EntityVillager.ITradeList
    {
        public ItemStack buyingItemStack;
        public EntityVillager.PriceInfo price;

        public EmeraldForItemsWithMetadata(ItemStack itemStackIn, EntityVillager.PriceInfo priceIn)
        {
            this.buyingItemStack = itemStackIn.copy();
            this.price = priceIn;
        }

        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
        {
            int i = 1;

            if (this.price != null)
            {
                i = this.price.getPrice(random);
            }
            
            this.buyingItemStack.setCount(i);
            
            recipeList.add(new MerchantRecipe(this.buyingItemStack, Items.EMERALD));
        }
    }

}
