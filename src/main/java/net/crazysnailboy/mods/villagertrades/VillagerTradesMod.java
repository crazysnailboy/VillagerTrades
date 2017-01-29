package net.crazysnailboy.mods.villagertrades;

import net.crazysnailboy.mods.villagertrades.util.StackTraceUtil;
import net.crazysnailboy.mods.villagertrades.util.TableLoader;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

@Mod(modid = VillagerTradesMod.MODID, name = VillagerTradesMod.MODNAME, version = VillagerTradesMod.VERSION)
public class VillagerTradesMod 
{
	
	public static final String MODID = "vtt";
	public static final String MODNAME = "Villager Trade Tables";
	public static final String VERSION = "1.0";

	
	@Instance(MODID)
	public static VillagerTradesMod INSTANCE;
	

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{	
		System.out.println("--------------------");

		TableLoader.preInit();
		
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

}
