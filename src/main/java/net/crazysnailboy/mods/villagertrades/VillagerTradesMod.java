package net.crazysnailboy.mods.villagertrades;

import net.crazysnailboy.mods.villagertrades.command.ModCommand;
import net.crazysnailboy.mods.villagertrades.util.StackTraceUtil;
import net.crazysnailboy.mods.villagertrades.util.JsonFileLoader;
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
	public static final String VERSION = "0.1";

	
	@Instance(MODID)
	public static VillagerTradesMod INSTANCE;
	

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{	
		System.out.println("--------------------");

		JsonFileLoader.preInit();
		
		System.out.println("--------------------");
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		try
		{
		
			
		}
		catch(Exception ex)
		{
			System.out.println(StackTraceUtil.getStackTrace(ex));
		}
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
	
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ModCommand());
	}	

}
