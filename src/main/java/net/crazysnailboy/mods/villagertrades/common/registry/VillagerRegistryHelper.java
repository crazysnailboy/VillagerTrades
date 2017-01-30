package net.crazysnailboy.mods.villagertrades.common.registry;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class VillagerRegistryHelper
{
	

	public static VillagerProfession getProfession(String value)
	{
		return (StringUtils.isNumeric(value) ? getProfessionById(Integer.parseInt(value)) : getProfessionByName(new ResourceLocation(value)));
	}
	
	
	@SuppressWarnings("deprecation")
	private static VillagerProfession getProfessionById(int value)
	{
		return VillagerRegistry.getById(value);
	}
	
	private static VillagerProfession getProfessionByName(ResourceLocation value)
	{
		List<VillagerProfession> professions = VillagerRegistry.instance().getRegistry().getValues();
		for ( VillagerProfession profession : professions )
		{
			ResourceLocation resourceLocation = ObfuscationReflectionHelper.getPrivateValue(VillagerProfession.class, profession, "name");
			if (value.equals(resourceLocation))
			{
				return profession;
			}
		}
		return null;
	}
	
	public static int getProfessionId(VillagerProfession profession)
	{
		return ((FMLControlledNamespacedRegistry<VillagerProfession>)VillagerRegistry.instance().getRegistry()).getIDForObject(profession);
	}
	

	public static ResourceLocation getProfessionName(VillagerProfession profession)
	{
		return ObfuscationReflectionHelper.getPrivateValue(VillagerProfession.class, profession, "name");
	}
	
//	
//	public static List<VillagerCareer> getProfessionCareers(VillagerProfession profession)
//	{
//		return ObfuscationReflectionHelper.getPrivateValue(VillagerProfession.class, profession, "careers");
//	}


	public static VillagerCareer getCareer(VillagerProfession profession, String value)
	{
		return (StringUtils.isNumeric(value) ? getCareerById(profession, Integer.parseInt(value)) : getCareerByName(profession, value));
	}
	
	private static VillagerCareer getCareerById(VillagerProfession profession, int value)
	{
		return profession.getCareer(value - 1);
	}
	
	private static VillagerCareer getCareerByName(VillagerProfession profession, String value)
	{
		List<VillagerCareer> careers = ObfuscationReflectionHelper.getPrivateValue(VillagerProfession.class, profession, "careers");
		for ( VillagerCareer career : careers )
		{
			if (career.getName().equals(value)) return career;
		}
		return null;
	}
	
//	public static int getCareerMaxLevel(VillagerCareer career)
//	{
//		List<List<ITradeList>> trades = ObfuscationReflectionHelper.getPrivateValue(VillagerCareer.class, career, "trades");
//		return trades.size();
//	}


	public static List<List<ITradeList>> getCareerTrades(VillagerCareer career)
	{
		List<List<ITradeList>> trades = ObfuscationReflectionHelper.getPrivateValue(VillagerCareer.class, career, "trades");
		return trades;
	}
	
	public static List<ITradeList> getCareerTradesForLevel(VillagerCareer career, int level)
	{
		List<List<ITradeList>> trades = ObfuscationReflectionHelper.getPrivateValue(VillagerCareer.class, career, "trades");
		int index = level - 1;
		return index >= 0 && index < trades.size() ? trades.get(index) : null;
	}
	
}
