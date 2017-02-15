package net.crazysnailboy.mods.villagertrades.common.config;

import java.io.File;

import net.crazysnailboy.mods.villagertrades.VillagerTradesMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

public class ModConfiguration
{

	private static Configuration config = null;


	public static boolean loadVillagersFromJar = true;
	public static boolean loadTradesFromJar = true;


	public static void preInit()
	{
		File configFile = new File(Loader.instance().getConfigDir(), VillagerTradesMod.MODID + ".cfg");

		config = new Configuration(configFile);
		config.load();

		loadConfigFromFile();

		if (config.hasChanged()) config.save();
	}


	private static void loadConfigFromFile()
	{
		loadVillagersFromJar = config.getBoolean("loadVillagersFromJar", Configuration.CATEGORY_GENERAL, loadVillagersFromJar, "");
		loadTradesFromJar = config.getBoolean("loadTradesFromJar", Configuration.CATEGORY_GENERAL, loadTradesFromJar, "");
	}

}
