package net.crazysnailboy.mods.villagertrades.inventory;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryMerchant;

public class VTTSlotMerchantResult extends net.minecraft.inventory.SlotMerchantResult
{

	public VTTSlotMerchantResult(EntityPlayer player, IMerchant merchant, InventoryMerchant merchantInventory, int slotIndex, int xPosition, int yPosition)
	{
		super(player, merchant, merchantInventory, slotIndex, xPosition, yPosition);
	}

}
