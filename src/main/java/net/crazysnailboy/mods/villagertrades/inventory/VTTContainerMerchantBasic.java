package net.crazysnailboy.mods.villagertrades.inventory;

import java.lang.reflect.Field;
import net.crazysnailboy.mods.villagertrades.util.ReflectionHelper;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;


public class VTTContainerMerchantBasic extends net.minecraft.inventory.ContainerMerchant
{

	private static final Field merchantInventoryField = ReflectionHelper.getDeclaredField(net.minecraft.inventory.ContainerMerchant.class, "merchantInventory", "field_75176_f");


	public VTTContainerMerchantBasic(InventoryPlayer playerInventory, IMerchant merchant, World world)
	{
		super(playerInventory, merchant, world);
		ReflectionHelper.setFieldValue(merchantInventoryField, this, new VTTInventoryMerchant(playerInventory.player, merchant));

		this.inventorySlots.set(2, new VTTSlotMerchantResult(playerInventory.player, merchant, this.getMerchantInventory(), 2, 120, 53));
	}

}
