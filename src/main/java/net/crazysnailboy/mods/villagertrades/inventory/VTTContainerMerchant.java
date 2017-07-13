package net.crazysnailboy.mods.villagertrades.inventory;

import javax.annotation.Nullable;
import com.google.common.collect.Lists;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


/**
 * VTTContainerMerchant - Extended version of {@link net.minecraft.inventory.ContainerMerchant}
 * Replaced all internal logic because of the need to override the private final field {@link net.minecraft.inventory.ContainerMerchant.merchantInventory}.
 *
 */
public class VTTContainerMerchant extends net.minecraft.inventory.ContainerMerchant
{

	private final IMerchant merchant;
	private final VTTInventoryMerchant merchantInventory;
	private final World world;


	public VTTContainerMerchant(InventoryPlayer playerInventory, IMerchant merchant, World world)
	{
		super(playerInventory, merchant, world);

		this.inventorySlots = Lists.<Slot>newArrayList();
		this.inventoryItemStacks = Lists.<ItemStack>newArrayList();

		this.merchant = merchant;
		this.world = world;
		this.merchantInventory = new VTTInventoryMerchant(playerInventory.player, merchant);



		this.addSlotToContainer(new Slot(this.merchantInventory, 0, 36, 53));
		this.addSlotToContainer(new Slot(this.merchantInventory, 1, 62, 53));
		this.addSlotToContainer(new VTTSlotMerchantResult(playerInventory.player, merchant, this.merchantInventory, 2, 120, 53));

		for (int i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k)
		{
			this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
		}

	}

	@Override
	public VTTInventoryMerchant getMerchantInventory()
	{
		return this.merchantInventory;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventory)
	{
		this.merchantInventory.resetRecipeAndSlots();
		this.detectAndSendChanges();
	}

	@Override
	public void setCurrentRecipeIndex(int currentRecipeIndex)
	{
		this.merchantInventory.setCurrentRecipeIndex(currentRecipeIndex);
	}

	@Override
	@Nullable
	public ItemStack transferStackInSlot(EntityPlayer player, int index)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot)this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == 2)
			{
				if (!this.mergeItemStack(itemstack1, 3, 39, true))
				{
					return null;
				}

				slot.onSlotChange(itemstack1, itemstack);
			}
			else if (index != 0 && index != 1)
			{
				if (index >= 3 && index < 30)
				{
					if (!this.mergeItemStack(itemstack1, 30, 39, false))
					{
						return null;
					}
				}
				else if (index >= 30 && index < 39 && !this.mergeItemStack(itemstack1, 3, 30, false))
				{
					return null;
				}
			}
			else if (!this.mergeItemStack(itemstack1, 3, 39, false))
			{
				return null;
			}

			if (itemstack1.stackSize == 0)
			{
				slot.putStack((ItemStack)null);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize)
			{
				return null;
			}

			slot.onPickupFromSlot(player, itemstack1);
		}

		return itemstack;
	}

	/**
	 * Avoids having to call super method {@link net.minecraft.inventory.ContainerMerchant#onContainerClosed(net.minecraft.entity.player.EntityPlayer)
	 * by replicating the logic from in ancestor @{link net.minecraft.inventory.Container#onContainerClosed(net.minecraft.entity.player.EntityPlayer)}.
	 * See {@link Container_onContainerClosed}
	 *
	 */
	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		Container_onContainerClosed(player);
		this.merchant.setCustomer((EntityPlayer)null);
		Container_onContainerClosed(player);

		if (!this.world.isRemote)
		{
			ItemStack itemstack = this.merchantInventory.removeStackFromSlot(0);

			if (itemstack != null)
			{
				player.dropItem(itemstack, false);
			}

			itemstack = this.merchantInventory.removeStackFromSlot(1);

			if (itemstack != null)
			{
				player.dropItem(itemstack, false);
			}
		}
	}

	/**
	 * Replicates the logic from @{link net.minecraft.inventory.Container#onContainerClosed(net.minecraft.entity.player.EntityPlayer)}.
	 */
	private void Container_onContainerClosed(EntityPlayer player)
	{
		InventoryPlayer inventoryplayer = player.inventory;

		if (inventoryplayer.getItemStack() != null)
		{
			player.dropItem(inventoryplayer.getItemStack(), false);
			inventoryplayer.setItemStack((ItemStack)null);
		}
	}


}
