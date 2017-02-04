package net.crazysnailboy.mods.villagertrades.common.config;

import java.io.File;

import net.crazysnailboy.mods.villagertrades.VillagerTradesMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ModConfiguration 
{
	private static Configuration config = null;
	private static ConfigEventHandler configEventHandler = new ConfigEventHandler();

	
	public static boolean verboseLogging = false;
	public static String[] currencyItems = new String[] { "minecraft:emerald" };
	
	
	public static void preInit()
	{
		File configFile = new File(Loader.instance().getConfigDir(), VillagerTradesMod.MODID + ".cfg");
		config = new Configuration(configFile);
		config.load();
		syncFromFile();
	}
	
	public static void clientPreInit() 
	{
		MinecraftForge.EVENT_BUS.register(new ConfigEventHandler());
	}
	
	
	public static Configuration getConfig() 
	{
		return config;
	}
	
	
	public static void syncFromFile() 
	{
		syncConfig(true, true);
	}

	public static void syncFromGUI() 
	{
		syncConfig(false, true);
	}

	public static void syncFromFields() 
	{
		syncConfig(false, false);
	}
	

	
	private static void syncConfig(boolean loadConfigFromFile, boolean readFieldsFromConfig) 
	{
		
		if (loadConfigFromFile) 
		{
			config.load();
		}
		
		Property propVerboseLogging = config.get(Configuration.CATEGORY_GENERAL, "verboseLogging", verboseLogging, "");
		propVerboseLogging.setLanguageKey("options.verboseLogging");
		propVerboseLogging.setRequiresMcRestart(true);
		
		Property propCurrencyItems = config.get(Configuration.CATEGORY_GENERAL, "currencyItems", currencyItems, "");
		propCurrencyItems.setLanguageKey("options.currencyItems");
		propCurrencyItems.setRequiresMcRestart(false);
		

		
//		try
//		{
//			propVerboseLogging.setConfigEntryClass(ModGuiConfigEntries.BooleanEntry.class);
//			
//			List<String> propOrderGeneral = new ArrayList<String>();
//			propOrderGeneral.add(propVerboseLogging.getName());
//			config.setCategoryPropertyOrder(Configuration.CATEGORY_GENERAL, propOrderGeneral);
//			
//		}
//		catch(NoClassDefFoundError e) { }

		
		if (readFieldsFromConfig) 
		{
			verboseLogging = propVerboseLogging.getBoolean();
			currencyItems = propCurrencyItems.getStringList();
		}
		
		propVerboseLogging.set(verboseLogging);
		propCurrencyItems.set(currencyItems);
		
		if (config.hasChanged()) 
		{
			config.save();
		}
		
	}
	
	
	public static class ConfigEventHandler 
	{
		@SubscribeEvent
		public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) 
		{
			if (VillagerTradesMod.MODID.equals(event.getModID()) && !event.isWorldRunning())
			{
				syncFromGUI();
			}
		}
	}
	
}
