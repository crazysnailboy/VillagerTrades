package net.crazysnailboy.mods.villagertrades.trades;

import java.util.List;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;


public class MerchantOreRecipe extends net.minecraft.village.MerchantRecipe
{
	private static final ItemStack EMPTY = new ItemStack(Blocks.AIR);

	private NBTTagCompound itemToBuy;
	private NBTTagCompound secondItemToBuy;
	private NBTTagCompound itemToSell;


	public MerchantOreRecipe(NBTTagCompound tagCompound)
	{
		super(EMPTY, null, EMPTY, 0, 7);
		this.readFromTags(tagCompound);
	}

//	public MerchantOreRecipe(ItemStack buy1, @Nullable ItemStack buy2, ItemStack sell)
//	{
//		this(buy1, buy2, sell, 0, 7);
//	}
//
//	public MerchantOreRecipe(ItemStack buy1, @Nullable ItemStack buy2, ItemStack sell, int toolUsesIn, int maxTradeUsesIn)
//	{
//		super(null, null, null, toolUsesIn, maxTradeUsesIn);
//	}

//	public MerchantOreRecipe(ItemStack buy1, ItemStack sell)
//	{
//		this(buy1, (ItemStack)null, sell);
//	}

//	public MerchantOreRecipe(ItemStack buy1, Item sellItem)
//	{
//		this(buy1, new ItemStack(sellItem));
//	}


	@Override
	public ItemStack getItemToBuy()
	{
		ItemStack stack = loadItemStackFromNBT(this.itemToBuy);
		return stack;
	}

	@Override
	public ItemStack getSecondItemToBuy()
	{
		ItemStack stack = (this.secondItemToBuy == null ? null : loadItemStackFromNBT(this.secondItemToBuy));
		return stack;
	}

	@Override
	public boolean hasSecondItemToBuy()
	{
		return this.secondItemToBuy != null;
	}

	@Override
	public ItemStack getItemToSell()
	{
		ItemStack stack = loadItemStackFromNBT(this.itemToSell);
		return stack;
	}



	@Override
	public void readFromTags(NBTTagCompound tagCompound)
	{
		this.itemToBuy = tagCompound.getCompoundTag("buy");
		this.itemToSell = tagCompound.getCompoundTag("sell");

		if (tagCompound.hasKey("buyB", NBT.TAG_COMPOUND))
		{
			this.secondItemToBuy = tagCompound.getCompoundTag("buyB");
		}

		if (tagCompound.hasKey("uses", NBT.TAG_ANY_NUMERIC))
		{
			this.setToolUses(tagCompound.getInteger("uses"));
		}

		if (tagCompound.hasKey("maxUses", NBT.TAG_ANY_NUMERIC))
		{
			this.setMaxTradeUses(tagCompound.getInteger("maxUses"));
		}
		else
		{
			this.setMaxTradeUses(7);
		}

		if (tagCompound.hasKey("rewardExp", NBT.TAG_BYTE))
		{
			this.setRewardsExp(tagCompound.getBoolean("rewardExp"));
		}
		else
		{
			this.setRewardsExp(true);
		}
	}

	@Override
	public NBTTagCompound writeToTags()
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("buy", this.itemToBuy);
		nbttagcompound.setTag("sell", this.itemToSell);

//		nbttagcompound.setTag("buy", this.itemToBuy.writeToNBT(new NBTTagCompound()));
//		nbttagcompound.setTag("sell", this.itemToSell.writeToNBT(new NBTTagCompound()));

		if (this.secondItemToBuy != null)
		{
			nbttagcompound.setTag("buyB", this.secondItemToBuy);
//			nbttagcompound.setTag("buyB", this.secondItemToBuy.writeToNBT(new NBTTagCompound()));
		}

		nbttagcompound.setInteger("uses", this.getToolUses());
		nbttagcompound.setInteger("maxUses", this.getMaxTradeUses());
		nbttagcompound.setBoolean("rewardExp", this.getRewardsExp());
		return nbttagcompound;
	}





	private void setToolUses(int value)
	{
		ObfuscationReflectionHelper.setPrivateValue(net.minecraft.village.MerchantRecipe.class, this, value, "toolUses");
	}

	private void setMaxTradeUses(int value)
	{
		ObfuscationReflectionHelper.setPrivateValue(net.minecraft.village.MerchantRecipe.class, this, value, "maxTradeUses", "field_82786_e");
	}

	private void setRewardsExp(boolean value)
	{
		ObfuscationReflectionHelper.setPrivateValue(net.minecraft.village.MerchantRecipe.class, this, value, "rewardsExp", "field_180323_f");
	}



	private ItemStack loadItemStackFromNBT(NBTTagCompound compound)
	{
		String id = compound.getString("id");

		List<ItemStack> stacks = OreDictionary.getOres(id, false);

		if (stacks == OreDictionary.EMPTY_LIST)
		{
			return ItemStack.loadItemStackFromNBT(compound);
		}
		else
		{
			ItemStack stack = stacks.get(new Random().nextInt(stacks.size())).copy();

			// *** adapted from ItemStack#readFromNBT(NBTTagCompound) ***

			stack.stackSize = compound.getByte("Count");

			stack.setItemDamage(compound.getShort("Damage"));
			if (stack.getItemDamage() < 0 || stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
			{
				stack.setItemDamage(0);
			}

			if (compound.hasKey("tag", NBT.TAG_COMPOUND))
			{
				stack.setTagCompound(compound.getCompoundTag("tag"));
				stack.getItem().updateItemStackNBT(stack.getTagCompound());
			}

			// *** adapted from ItemStack#readFromNBT(NBTTagCompound) ***

			return stack;
		}

	}


}
