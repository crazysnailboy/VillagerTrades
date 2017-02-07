package net.crazysnailboy.mods.villagertrades;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

@Mod(modid = VillagerTradesMod.MODID, name = VillagerTradesMod.MODNAME, version = VillagerTradesMod.VERSION, updateJSON = VillagerTradesMod.UPDATEJSON)
public class VillagerTradesMod 
{
	
	public static final String MODID = "vtt";
	public static final String MODNAME = "Villager Trade Tables";
	public static final String VERSION = "0.5";
	public static final String UPDATEJSON = "https://raw.githubusercontent.com/crazysnailboy/VillagerTrades/master/update.json";

	
	@Instance(MODID)
	public static VillagerTradesMod INSTANCE;

	public static Logger logger = LogManager.getLogger(MODID);
	

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{	
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		VillagerLoader.loadCustomVillagerData();
		TradeLoader.loadCustomTradeData();
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ModCommand());
	}
	
}
