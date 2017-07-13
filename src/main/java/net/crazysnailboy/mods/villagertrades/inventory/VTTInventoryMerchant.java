package net.crazysnailboy.mods.villagertrades.inventory;

import java.util.Collections;
import javax.annotation.Nullable;
import com.google.common.primitives.Ints;
import net.crazysnailboy.mods.villagertrades.trades.MerchantOreRecipe;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.oredict.OreDictionary;


public class VTTInventoryMerchant extends net.minecraft.inventory.InventoryMerchant
{

	private final IMerchant theMerchant;
	private final ItemStack[] theInventory = new ItemStack[3];
	private final EntityPlayer player;
	private MerchantRecipe currentRecipe;
	private int currentRecipeIndex;

	public VTTInventoryMerchant(EntityPlayer player, IMerchant merchant)
	{
		super(player, merchant);
		this.player = player;
		this.theMerchant = merchant;
	}

	@Override
	public int getSizeInventory()
	{
		return this.theInventory.length;
	}

	@Override
	@Nullable
	public ItemStack getStackInSlot(int index)
	{
		return this.theInventory[index];
	}

	@Override
	@Nullable
	public ItemStack decrStackSize(int index, int count)
	{
		if (index == 2 && this.theInventory[index] != null)
		{
			return ItemStackHelper.getAndSplit(this.theInventory, index, this.theInventory[index].stackSize);
		}
		else
		{
			ItemStack itemstack = ItemStackHelper.getAndSplit(this.theInventory, index, count);

			if (itemstack != null && this.inventoryResetNeededOnSlotChange(index))
			{
				this.resetRecipeAndSlots();
			}

			return itemstack;
		}
	}

	private boolean inventoryResetNeededOnSlotChange(int index)
	{
		return index == 0 || index == 1;
	}

	@Override
	@Nullable
	public ItemStack removeStackFromSlot(int index)
	{
		return ItemStackHelper.getAndRemove(this.theInventory, index);
	}

	@Override
	public void setInventorySlotContents(int index, @Nullable ItemStack stack)
	{
		this.theInventory[index] = stack;

		if (stack != null && stack.stackSize > this.getInventoryStackLimit())
		{
			stack.stackSize = this.getInventoryStackLimit();
		}

		if (this.inventoryResetNeededOnSlotChange(index))
		{
			this.resetRecipeAndSlots();
		}
	}

	@Override
	public String getName()
	{
		return "mob.villager";
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player)
	{
		return this.theMerchant.getCustomer() == player;
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return true;
	}

	@Override
	public void markDirty()
	{
		this.resetRecipeAndSlots();
	}

	/**
	 * This is the only method that really needs to be overriden.
	 * Needed to bypass call to {@link MerchantRecipeList#canRecipeBeUsed(ItemStack, ItemStack, int)} in {@link net.minecraft.inventory.InventoryMerchant#resetRecipeAndSlots()}.
	 */
	@Override
	public void resetRecipeAndSlots()
	{
		this.currentRecipe = null;
		ItemStack itemstack = this.theInventory[0];
		ItemStack itemstack1 = this.theInventory[1];

		if (itemstack == null)
		{
			itemstack = itemstack1;
			itemstack1 = null;
		}

		if (itemstack == null)
		{
			this.setInventorySlotContents(2, (ItemStack)null);
		}
		else
		{
			MerchantRecipeList merchantrecipelist = this.theMerchant.getRecipes(this.player);

			if (merchantrecipelist != null)
			{
				MerchantRecipe merchantrecipe = this.canRecipeBeUsed(itemstack, itemstack1, this.currentRecipeIndex);

				if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled())
				{
					this.currentRecipe = merchantrecipe;
					this.setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
				}
				else if (itemstack1 != null)
				{
					merchantrecipe = this.canRecipeBeUsed(itemstack1, itemstack, this.currentRecipeIndex);

					if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled())
					{
						this.currentRecipe = merchantrecipe;
						this.setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
					}
					else
					{
						this.setInventorySlotContents(2, (ItemStack)null);
					}
				}
				else
				{
					this.setInventorySlotContents(2, (ItemStack)null);
				}
			}
		}

		this.theMerchant.verifySellingItem(this.getStackInSlot(2));
	}

	@Override
	public MerchantRecipe getCurrentRecipe()
	{
		return this.currentRecipe;
	}

	@Override
	public void setCurrentRecipeIndex(int index)
	{
		this.currentRecipeIndex = index;
		this.resetRecipeAndSlots();
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		for (int i = 0; i < this.theInventory.length; ++i)
		{
			this.theInventory[i] = null;
		}
	}



	private MerchantRecipe canRecipeBeUsed(ItemStack stackA, @Nullable ItemStack stackB, int recipeIndex)
	{
		MerchantRecipeList merchantrecipelist = this.theMerchant.getRecipes(this.player);

		if (recipeIndex > 0 && recipeIndex < merchantrecipelist.size())
		{
			MerchantRecipe merchantrecipe1 = merchantrecipelist.get(recipeIndex);
			if (merchantrecipe1 instanceof MerchantOreRecipe)
			{
				return !this.areOreStacksExactlyEqual(stackA, merchantrecipe1.getItemToBuy()) || (stackB != null || merchantrecipe1.hasSecondItemToBuy()) && (!merchantrecipe1.hasSecondItemToBuy() || !this.areOreStacksExactlyEqual(stackB, merchantrecipe1.getSecondItemToBuy())) || stackA.stackSize < merchantrecipe1.getItemToBuy().stackSize || merchantrecipe1.hasSecondItemToBuy() && stackB.stackSize < merchantrecipe1.getSecondItemToBuy().stackSize ? null : merchantrecipe1;
			}
			else
			{
				return !this.areItemStacksExactlyEqual(stackA, merchantrecipe1.getItemToBuy()) || (stackB != null || merchantrecipe1.hasSecondItemToBuy()) && (!merchantrecipe1.hasSecondItemToBuy() || !this.areItemStacksExactlyEqual(stackB, merchantrecipe1.getSecondItemToBuy())) || stackA.stackSize < merchantrecipe1.getItemToBuy().stackSize || merchantrecipe1.hasSecondItemToBuy() && stackB.stackSize < merchantrecipe1.getSecondItemToBuy().stackSize ? null : merchantrecipe1;
			}
		}
		else
		{
			for (int i = 0; i < merchantrecipelist.size(); ++i)
			{
				MerchantRecipe merchantrecipe = (MerchantRecipe)merchantrecipelist.get(i);
				if (merchantrecipe instanceof MerchantOreRecipe)
				{
					if (this.areOreStacksExactlyEqual(stackA, merchantrecipe.getItemToBuy()) && stackA.stackSize >= merchantrecipe.getItemToBuy().stackSize && (!merchantrecipe.hasSecondItemToBuy() && stackB == null || merchantrecipe.hasSecondItemToBuy() && this.areOreStacksExactlyEqual(stackB, merchantrecipe.getSecondItemToBuy()) && stackB.stackSize >= merchantrecipe.getSecondItemToBuy().stackSize))
					{
						return merchantrecipe;
					}
				}
				else
				{
					if (this.areItemStacksExactlyEqual(stackA, merchantrecipe.getItemToBuy()) && stackA.stackSize >= merchantrecipe.getItemToBuy().stackSize && (!merchantrecipe.hasSecondItemToBuy() && stackB == null || merchantrecipe.hasSecondItemToBuy() && this.areItemStacksExactlyEqual(stackB, merchantrecipe.getSecondItemToBuy()) && stackB.stackSize >= merchantrecipe.getSecondItemToBuy().stackSize))
					{
						return merchantrecipe;
					}
				}
			}

			return null;
		}
	}

	private boolean areItemStacksExactlyEqual(ItemStack stack1, ItemStack stack2)
	{
		return ItemStack.areItemsEqual(stack1, stack2) && (!stack2.hasTagCompound() || stack1.hasTagCompound() && NBTUtil.areNBTEquals(stack2.getTagCompound(), stack1.getTagCompound(), false));
	}

	private boolean areOreStacksExactlyEqual(ItemStack stack1, ItemStack stack2)
	{
//        if (stack1 == null && stack2 != null || stack1 != null && stack2 == null)
//        {
//        	return false;
//        }
        if (stack1 != null && stack2 != null)
        {
        	return !Collections.disjoint(Ints.asList(OreDictionary.getOreIDs(stack1)), Ints.asList(OreDictionary.getOreIDs(stack2)));
        }
        else return false;



//		boolean result = OreDictionary.itemMatches(stack1, stack2, false) && (!stack2.hasTagCompound() || stack1.hasTagCompound() && NBTUtil.areNBTEquals(stack2.getTagCompound(), stack1.getTagCompound(), false));
//		System.out.println("areOreStacksExactlyEqual :: " + result);
//		return result;
//		return OreDictionary.itemMatches(stack1, stack2, false) && (!stack2.hasTagCompound() || stack1.hasTagCompound() && NBTUtil.areNBTEquals(stack2.getTagCompound(), stack1.getTagCompound(), false));
	}

}
