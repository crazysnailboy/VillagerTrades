package net.crazysnailboy.mods.villagertrades.client.gui;

import net.crazysnailboy.mods.villagertrades.inventory.VTTContainerMerchant;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

public class GuiMerchant2 extends net.minecraft.client.gui.GuiMerchant
{

	public GuiMerchant2(InventoryPlayer playerInventory, IMerchant merchant, World world)
	{
		super(playerInventory, merchant, world);
		this.inventorySlots = new VTTContainerMerchant(playerInventory, merchant, world);
	}

}
