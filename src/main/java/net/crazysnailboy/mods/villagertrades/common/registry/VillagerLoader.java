package net.crazysnailboy.mods.villagertrades.common.registry;

import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.crazysnailboy.mods.villagertrades.util.FileUtils;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class VillagerLoader 
{

	public static void postInit()
	{
		loadCustomVillagers();
	}
	
	
	private static void loadCustomVillagers()
	{	
		HashMap<String, String> villagerFiles = FileUtils.createFileMap("villagers");
		for ( String fileContents : villagerFiles.values() )
		{
			addCustomVillager(fileContents);
		}
	}
	
	
	private static void addCustomVillager(String fileContents)
	{
		// parse the provided string as JSON
		JsonObject jsonVillager = new JsonParser().parse(fileContents).getAsJsonObject();
		
		// extract the required values from the profession object
		JsonObject jsonProfession = jsonVillager.get("profession").getAsJsonObject();
		String name = jsonProfession.get("name").getAsString();
		String texture = jsonProfession.get("texture").getAsString();
		String zombieTexture = jsonProfession.get("zombieTexture").getAsString();

		// extract the list of careers for this profession
		JsonArray jsonCareers = jsonVillager.get("careers").getAsJsonArray();
		
		// create and register the new profession
		VillagerProfession profession = new VillagerProfession(name, texture, zombieTexture);
		VillagerRegistry.instance().register(profession);
		
		// add the careers to the profession
		for ( JsonElement jsonCareer : jsonCareers )
		{
			new VillagerCareer(profession, jsonCareer.getAsString());
		}
		
	}
	
	
}
