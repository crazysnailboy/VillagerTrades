package net.crazysnailboy.mods.villagertrades;

import net.crazysnailboy.mods.villagertrades.command.ModCommand;
import net.crazysnailboy.mods.villagertrades.loaders.TradeLoader;
import net.crazysnailboy.mods.villagertrades.loaders.VillagerLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = VillagerTradesMod.MODID, name = VillagerTradesMod.MODNAME, version = VillagerTradesMod.VERSION)
public class VillagerTradesMod 
{
	
	public static final String MODID = "vtt";
	public static final String MODNAME = "Villager Trade Tables";
	public static final String VERSION = "0.3";

	
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
//		System.out.println("------------------------------");
		VillagerLoader.loadCustomVillagerData();
		TradeLoader.loadCustomTradeData();
//		System.out.println("------------------------------");
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ModCommand());
	}
	
}
