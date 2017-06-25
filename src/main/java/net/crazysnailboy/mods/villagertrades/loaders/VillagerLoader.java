package net.crazysnailboy.mods.villagertrades.loaders;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.crazysnailboy.mods.villagertrades.VillagerTradesMod;
import net.crazysnailboy.mods.villagertrades.common.config.ModConfiguration;
import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper.VTTVillagerProfession;
import net.crazysnailboy.mods.villagertrades.util.FileUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
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
		Map<String, String> villagerFiles = sortVillagers(FileUtils.createFileMap("villagers", ModConfiguration.loadVillagersFromJar));

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
			catch (Exception ex)
			{
				VillagerTradesMod.LOGGER.error("Error parsing \"" + fileName + "\": " + ex.getMessage());
			}
		}
	}


	private static Map<String, String> sortVillagers(Map<String, String> villagerFiles)
	{

		List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>(villagerFiles.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, String>>()
		{

			@Override
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2)
			{
				JsonObject j1 = new JsonParser().parse(o1.getValue()).getAsJsonObject();
				JsonObject j2 = new JsonParser().parse(o2.getValue()).getAsJsonObject();

				JsonObject p1 = j1.get("profession").getAsJsonObject();
				JsonObject p2 = j2.get("profession").getAsJsonObject();

				int id1 = (p1.has("id") ? p1.get("id").getAsInt() : 0);
				int id2 = (p2.has("id") ? p2.get("id").getAsInt() : 0);

				String name1 = p1.get("name").getAsString();
				String name2 = p2.get("name").getAsString();

				return (id1 != id2 ? id2 - id1 : name1.compareTo(name2));
			}

		});

		Map<String, String> result = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
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
		if (!ForgeRegistries.VILLAGER_PROFESSIONS.containsKey(new ResourceLocation(name)))
		{
			// create and register the new profession
			profession = new VillagerProfession(name, texture, zombieTexture);
			ForgeRegistries.VILLAGER_PROFESSIONS.register(profession);
		}
		// or get it if it does
		else
		{
			profession = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation(name));
		}

		// iterate over the provided careers
		for (JsonElement jsonCareer : jsonCareers)
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
