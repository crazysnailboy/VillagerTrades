package net.crazysnailboy.mods.villagertrades.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import net.crazysnailboy.mods.villagertrades.VillagerTradesMod;
import net.minecraftforge.fml.common.Loader;


public class FileUtils
{


	/**
	 * Scans the various locations for .json files and creates a map of the filenames and file contents
	 * @param folderName The bottom level folder to be scanned in each location
	 * @return a HashMap with the filename as the key and the file contents as the value
	 */
	public static HashMap<String, String> createFileMap(String folderName, boolean loadFilesFromJar)
	{
		// initialise a hashmap which will contain the file names as keys and the file contents as values
		HashMap<String, String> fileMap = new HashMap<String, String>();

		if (loadFilesFromJar)
		{
			// load the villagers from the jar's assets folder
			for ( String fileName : getFileNamesFromModFolder("/assets/" + VillagerTradesMod.MODID + "/data/" + folderName + "/"))
			{
				String fileContents = readFileContentsFromMod("assets/" + VillagerTradesMod.MODID + "/data/" + folderName + "/" + fileName);
				fileMap.put(fileName, fileContents);
			}
		}

		// load from config folder
		File configFolder = new File((Loader.instance().getConfigDir().getAbsolutePath() + "/" + VillagerTradesMod.MODID).replace(File.separatorChar, '/').replace("/./", "/"));
		if (!configFolder.exists()) configFolder.mkdirs();

		File villagersFolder = new File(configFolder, folderName);
		if (villagersFolder.exists())
		{
			for ( String fileName : getFileNamesFromFolder(villagersFolder.toPath()) )
			{
				String fileContents = readFileContents(new File(villagersFolder, fileName));
				fileMap.put(fileName, fileContents);
			}
		}
		else
		{
			villagersFolder.mkdirs();
			for ( String fileName : fileMap.keySet())
			{
				writeFile(new File(villagersFolder, fileName), fileMap.get(fileName));
			}
		}

		return fileMap;
	}


	private static List<String> getFileNamesFromFolder(Path folderPath)
	{
		List<String> fileNames = new ArrayList<String>();
		try
		{
			Stream<Path> walk = Files.walk(folderPath, 1);
			for (Iterator<Path> it = walk.iterator(); it.hasNext();)
			{
				String fileName = it.next().getFileName().toString();
				if (fileName.endsWith(".json")) fileNames.add(fileName);
			}
			walk.close();
		}
		catch(Exception ex){ VillagerTradesMod.logger.catching(ex); }
		return fileNames;
	}


	private static List<String> getFileNamesFromModFolder(String resourceFolder)
	{
		List<String> fileNames = new ArrayList<String>();
		try
		{
			URI resourceUri = VillagerTradesMod.class.getResource(resourceFolder).toURI();
			Path resourcePath;
			if (resourceUri.getScheme().equals("jar"))
			{
				FileSystem fileSystem;
				try
				{
					fileSystem = FileSystems.newFileSystem(resourceUri, Collections.<String, Object>emptyMap());
				}
				catch (FileSystemAlreadyExistsException ex)
				{
					fileSystem = FileSystems.getFileSystem(resourceUri); 
				}
				resourcePath = fileSystem.getPath(resourceFolder);
			}
			else
			{
				resourcePath = Paths.get(resourceUri);
			}

			Stream<Path> walk = Files.walk(resourcePath, 1);
			for (Iterator<Path> it = walk.iterator(); it.hasNext();)
			{
				String fileName = it.next().getFileName().toString();
				if (fileName.endsWith(".json")) fileNames.add(fileName);
			}
			walk.close();
		}
		catch(Exception ex){ VillagerTradesMod.logger.catching(ex); }
		return fileNames;
	}


	private static String readFileContents(File file)
	{
		String fileContents = "";
		try
		{
			InputStream stream = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

			fileContents = IOUtils.toString(stream);

			reader.close();
			stream.close();

		}
		catch(Exception ex){ VillagerTradesMod.logger.catching(ex); }
		return fileContents;
	}

	private static String readFileContentsFromMod(String fileName)
	{
		String fileContents = "";
		try
		{
			InputStream stream = VillagerTradesMod.INSTANCE.getClass().getClassLoader().getResourceAsStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

			fileContents = IOUtils.toString(stream);

			reader.close();
			stream.close();

		}
		catch(Exception ex){ VillagerTradesMod.logger.catching(ex); }
		return fileContents;
	}


	private static void writeFile(File outputFile, String fileContents)
	{
		try
		{
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			BufferedWriter streamWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

			streamWriter.write(fileContents);

			streamWriter.close();
			outputStream.close();

		}
		catch(Exception ex){ VillagerTradesMod.logger.catching(ex); }
	}


}
