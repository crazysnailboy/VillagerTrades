package net.crazysnailboy.mods.villagertrades.command;

import java.util.ArrayList;
import java.util.List;

import net.crazysnailboy.mods.villagertrades.common.registry.VillagerRegistryHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class ModCommand implements ICommand
{
	
	private final List<String> aliases;

	
	public ModCommand()
	{
		aliases = new ArrayList<String>();
		aliases.add("vtt");
	}
	
	
	@Override
	public int compareTo(ICommand arg0) 
	{
		return 0;
	}

	@Override
	public String getName() 
	{
		return "vtt";
	}

	@Override
	public String getUsage(ICommandSender sender) 
	{
		return null;
	}

	@Override
	public List<String> getAliases() 
	{
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException 
	{
		World world = sender.getEntityWorld(); 
		if (!world.isRemote) 
		{
			if (args.length > 0)
			{
				VillagerProfession profession = VillagerRegistryHelper.getProfession(args[0]);
				if (profession != null)
				{
				
					String professionName = VillagerRegistryHelper.getProfessionName(profession).toString();
					int professionId = VillagerRegistryHelper.getProfessionId(profession);
					
					sender.sendMessage(new TextComponentString(professionName + " :: id=" + professionId ));
				}
			}
			else
			{
				List<VillagerProfession> professions = VillagerRegistry.instance().getRegistry().getValues();
				for ( VillagerProfession profession : professions )
				{
					ResourceLocation resourceLocation = ObfuscationReflectionHelper.getPrivateValue(VillagerProfession.class, profession, "name");
					sender.sendMessage(new TextComponentString(resourceLocation.toString()));
				}
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) 
	{
		return sender.canUseCommand(2, getName());
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) 
	{
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index)
	{
		return false;
	}

}
