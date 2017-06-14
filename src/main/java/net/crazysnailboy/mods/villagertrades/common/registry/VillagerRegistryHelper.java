package net.crazysnailboy.mods.villagertrades.common.registry;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;


/**
 * A collection of helper methods and classes for accessing private or otherwise difficult to get to members of the VillagerRegistry
 *
 */
public class VillagerRegistryHelper
{

	/**
	 * A wrapper class to access private or otherwise difficult to get to members of the VillagerRegistry.VillagerProfession
	 *
	 */
	public static class VTTVillagerProfession
	{

		private static final Class professionClass = VillagerRegistry.VillagerProfession.class;

		public VillagerRegistry.VillagerProfession profession;


		public int getId()
		{
			return ((FMLControlledNamespacedRegistry<VillagerRegistry.VillagerProfession>)VillagerRegistry.instance().getRegistry()).getIDForObject(this.profession);
		}

		public ResourceLocation getName()
		{
			return (ResourceLocation)ObfuscationReflectionHelper.getPrivateValue(professionClass, this.profession, "name");
		}

		public List<VillagerRegistry.VillagerCareer> getCareers()
		{
			return (List<VillagerRegistry.VillagerCareer>)ObfuscationReflectionHelper.getPrivateValue(professionClass, this.profession, "careers");
		}

		public VillagerRegistry.VillagerCareer getCareer(String name)
		{
			List<VillagerRegistry.VillagerCareer> careers = (List<VillagerRegistry.VillagerCareer>)ObfuscationReflectionHelper.getPrivateValue(professionClass, this.profession, "careers");
			for (VillagerRegistry.VillagerCareer career : careers)
			{
				if (career.getName().equals(name)) return career;
			}
			return null;
		}

		public VillagerRegistry.VillagerCareer getCareer(int id)
		{
			return this.profession.getCareer(id - 1);
		}


		public VTTVillagerProfession(VillagerRegistry.VillagerProfession profession)
		{
			this.profession = profession;
		}
	}


	/**
	 * A wrapper class to access private or otherwise difficult to get to members of the VillagerRegistry.VillagerCareer
	 *
	 */
	public static class VTTVillagerCareer
	{

		private static final Class careerClass = VillagerRegistry.VillagerCareer.class;

		public VillagerRegistry.VillagerCareer career;


		public int getId()
		{
			int id = (Integer)ObfuscationReflectionHelper.getPrivateValue(careerClass, this.career, "id");
			return id + 1;
		}

		public String getName()
		{
			return this.career.getName();
		}

		public int getCareerLevels()
		{
			List<List<ITradeList>> trades = (List<List<ITradeList>>)ObfuscationReflectionHelper.getPrivateValue(careerClass, this.career, "trades");
			return trades.size();
		}

		public List<List<ITradeList>> getTrades()
		{
			List<List<ITradeList>> trades = (List<List<ITradeList>>)ObfuscationReflectionHelper.getPrivateValue(careerClass, this.career, "trades");
			return trades;
		}

		public List<ITradeList> getTrades(int level)
		{
			List<List<ITradeList>> trades = (List<List<ITradeList>>)ObfuscationReflectionHelper.getPrivateValue(careerClass, this.career, "trades");
			int index = level - 1;
			return index >= 0 && index < trades.size() ? trades.get(index) : null;
		}


		public VTTVillagerCareer(VillagerRegistry.VillagerCareer career)
		{
			this.career = career;
		}

	}


	public static VillagerRegistry.VillagerProfession getProfession(String value)
	{
		return (StringUtils.isNumeric(value) ? getProfessionById(Integer.parseInt(value)) : getProfessionByName(new ResourceLocation(value)));
	}


	@SuppressWarnings("deprecation")
	private static VillagerRegistry.VillagerProfession getProfessionById(int value)
	{
		return VillagerRegistry.getById(value);
	}

	private static VillagerRegistry.VillagerProfession getProfessionByName(ResourceLocation value)
	{
		return VillagerRegistry.instance().getRegistry().getValue(value);
	}


	public static List<Map.Entry<Integer, String>> getProfessionIdsAndNamesSortedById()
	{
		List<Map.Entry<Integer, String>> professions = new ArrayList<Map.Entry<Integer, String>>();

		for (VillagerRegistry.VillagerProfession profession : VillagerRegistry.instance().getRegistry().getValues())
		{
			VTTVillagerProfession wrapper = new VTTVillagerProfession(profession);

			int id = wrapper.getId();
			String name = wrapper.getName().toString();

			professions.add(new AbstractMap.SimpleEntry<Integer, String>(id, name));

		}

		Collections.sort(professions, new Comparator<Map.Entry<Integer, String>>()
		{

			@Override
			public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2)
			{
				return o1.getKey() - o2.getKey();
			}
		});

		return professions;

	}


}
