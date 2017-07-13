package net.crazysnailboy.mods.villagertrades.common.network;

import net.crazysnailboy.mods.villagertrades.client.gui.VTTGuiMerchant;
import net.crazysnailboy.mods.villagertrades.inventory.VTTContainerMerchant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHandler implements net.minecraftforge.fml.common.network.IGuiHandler
{

	public static final int GUI_MERCHANT = 0;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int entityId, int unused1, int unused2)
	{
		if (id == GUI_MERCHANT)
		{
			Entity entity = world.getEntityByID(entityId);
			if (entity instanceof EntityVillager)
			{
				return new VTTContainerMerchant(player.inventory, (EntityVillager)entity, world);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int entityId, int unused1, int unused2)
	{
		if (id == GUI_MERCHANT)
		{
			Entity entity = world.getEntityByID(entityId);
			if (entity instanceof EntityVillager)
			{
				return new VTTGuiMerchant(player.inventory, (EntityVillager)entity, world);
			}
		}
		return null;
	}

}
