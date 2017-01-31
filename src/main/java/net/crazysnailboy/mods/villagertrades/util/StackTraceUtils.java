package net.crazysnailboy.mods.villagertrades.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;


/**
* Simple utilities to return the stack trace of an exception as a String.
* 
*/
public final class StackTraceUtils 
{

	public static String getStackTrace(Throwable throwable) 
	{
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		return writer.toString();
	}

} 

