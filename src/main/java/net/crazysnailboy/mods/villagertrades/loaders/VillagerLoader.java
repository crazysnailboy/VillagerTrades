package net.crazysnailboy.mods.villagertrades.loaders;

import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.crazysnailboy.mods.villagertrades.VillagerTradesMod;
import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper;
import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper.VTTVillagerProfession;
import net.crazysnailboy.mods.villagertrades.util.FileUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class VillagerLoader 
{

	/**
	 * Builds a map of villager files by combining files from the assets and config folders, and loads the villager data into the registry
	 */
	public static void loadCustomVillagerData()
	{	
		// build the file map
		HashMap<String, String> villagerFiles = FileUtils.createFileMap("villagers");
		
		// iterate over the filenames in the map
		for (String fileName : villagerFiles.keySet())
		{
			// get the file contents from the map for the specified name
			String fileContents = villagerFiles.get(fileName);
			try
			{
				// load the villager from the file contents
				loadVillagerFromFile(fileContents);
			}
			// write to the log if something bad happened 
			catch (Exception ex){ VillagerTradesMod.logger.error("Error parsing \"" + fileName + "\": " + ex.getMessage()); }
		}
	}
	
	
	private static void loadVillagerFromFile(String fileContents)
	{
		// parse the provided string as JSON
		JsonObject jsonVillager = new JsonParser().parse(fileContents).getAsJsonObject();
		
		// extract the required values from the profession object
		JsonObject jsonProfession = jsonVillager.get("profession").getAsJsonObject();
		String name = jsonProfession.get("name").getAsString();
		String texture = (jsonProfession.has("texture") ? jsonProfession.get("texture").getAsString() : null);
		String zombieTexture = (jsonProfession.has("zombieTexture") ? jsonProfession.get("zombieTexture").getAsString() : null);

		// extract the list of careers for this profession
		JsonArray jsonCareers = jsonVillager.get("careers").getAsJsonArray();
		
		
		// create the profession if it doesn't already exist
		VillagerProfession profession;
		if (!VillagerRegistry.instance().getRegistry().containsKey(new ResourceLocation(name)))
		{
			// create and register the new profession
			profession = new VillagerProfession(name, texture, zombieTexture);
			VillagerRegistry.instance().register(profession);
		}
		// or get it if it does
		else
		{
			profession = VillagerRegistry.instance().getRegistry().getValue(new ResourceLocation(name));
		}

		// iterate over the provided careers
		for ( JsonElement jsonCareer : jsonCareers )
		{
			String careerName = jsonCareer.getAsString();
			
			// add the career if it doesn't already exist
			if (new VTTVillagerProfession(profession).getCareer(careerName) == null)
			{
				new VillagerCareer(profession, careerName);
			}
			
		}
		
	}
	
	
}
