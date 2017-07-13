package net.crazysnailboy.mods.villagertrades;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.crazysnailboy.mods.villagertrades.command.ModCommand;
import net.crazysnailboy.mods.villagertrades.common.config.ModConfiguration;
import net.crazysnailboy.mods.villagertrades.common.network.GuiHandler;
import net.crazysnailboy.mods.villagertrades.loaders.TradeLoader;
import net.crazysnailboy.mods.villagertrades.loaders.VillagerLoader;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;


@Mod(modid = VillagerTradesMod.MODID, name = VillagerTradesMod.NAME, version = VillagerTradesMod.VERSION, updateJSON = VillagerTradesMod.UPDATEJSON)
public class VillagerTradesMod
{

	public static final String MODID = "vtt";
	public static final String NAME = "Villager Trade Tables";
	public static final String VERSION = "${version}";
	public static final String UPDATEJSON = "https://raw.githubusercontent.com/crazysnailboy/VillagerTrades/master/update.json";


	@Instance(MODID)
	public static VillagerTradesMod INSTANCE;

	public static final Logger LOGGER = LogManager.getLogger(MODID);


	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ModConfiguration.preInit();
		VillagerLoader.loadCustomVillagerData();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(VillagerTradesMod.INSTANCE, new GuiHandler());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		TradeLoader.loadCustomTradeData();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ModCommand());
	}


	@EventBusSubscriber
	public static class EventHandlers
	{
		@SubscribeEvent
		public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
		{
			if (event.getTarget() instanceof EntityVillager)
			{
				EntityVillager villager = (EntityVillager)event.getTarget();

				// *** Adapted from net.minecraft.entity.passive.EntityVillager#processInteract
				boolean flag = event.getItemStack() != null && event.getItemStack().getItem() == Items.SPAWN_EGG;

				if (!flag && villager.isEntityAlive() && !villager.isTrading() && !villager.isChild() && !villager.isSneaking())
				{
					if (!villager.world.isRemote && (villager.getRecipes(event.getEntityPlayer()) == null || !villager.getRecipes(event.getEntityPlayer()).isEmpty()))
					{
						villager.setCustomer(event.getEntityPlayer());

						int entityId = villager.getEntityId();
						event.getEntityPlayer().openGui(VillagerTradesMod.INSTANCE, GuiHandler.GUI_MERCHANT, event.getWorld(), entityId, 0, 0);
					}
					event.getEntityPlayer().addStat(StatList.TALKED_TO_VILLAGER);
				}
				// *** Adapted from net.minecraft.entity.passive.EntityVillager#processInteract

				event.setCanceled(true);
			}
		}
	}

}
