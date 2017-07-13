package net.crazysnailboy.mods.villagertrades.util;

import java.lang.reflect.Field;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;

public class ReflectionHelper
{
	public static Field getDeclaredField(Class classToAccess, String... fieldNames)
	{
		return net.minecraftforge.fml.relauncher.ReflectionHelper.findField(classToAccess, fieldNames);
	}

	public static <T,E> void setFieldValue(Field fieldToAccess, E instance, T value)
	{
		try
		{
			fieldToAccess.set(instance, value);
		}
		catch (Exception ex)
		{
            throw new UnableToAccessFieldException(new String[0], ex);
		}
	}

}
