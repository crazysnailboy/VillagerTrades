package net.crazysnailboy.mods.villagertrades;

import net.crazysnailboy.mods.villagertrades.common.registry.TradeLoader;
import net.crazysnailboy.mods.villagertrades.common.registry.VillagerLoader;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = VillagerTradesMod.MODID, name = VillagerTradesMod.MODNAME, version = VillagerTradesMod.VERSION)
public class VillagerTradesMod 
{
	
	public static final String MODID = "vtt";
	public static final String MODNAME = "Villager Trade Tables";
	public static final String VERSION = "0.1";

	
	@Instance(MODID)
	public static VillagerTradesMod INSTANCE;
	

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{	
		// initialize the configuration
//		ModConfiguration.preInit();
//		if (event.getSide() == Side.CLIENT) ModConfiguration.clientPreInit();

	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		System.out.println("----------");
		
		VillagerLoader.postInit();
		TradeLoader.postInit();
		
		System.out.println("----------");
	}
	
}
