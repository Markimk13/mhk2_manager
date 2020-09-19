package main.java.ch.mko.fmm.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class for useful methods to organize files.
 */
public class FileUtils {

	/**
	 * Returns the next unused name in directory {@code dir}.
	 * 
	 * @param dir the directory for the new unique name
	 * @param name the new name
	 * @return the unique name
	 * @see #getUniqueName(List, String)
	 */
	public static String getUniqueName(File dir, String name) {
		File[] files = dir.listFiles();
		List<String> usedNames = new ArrayList<>();
		for (File file : files) {
			usedNames.add(file.getName());
		}
		
		int extBegin = name.lastIndexOf(".");
		String nameWithoutExt = name.substring(0, extBegin);
		String ext = name.substring(extBegin);
		return getUniqueName(usedNames, nameWithoutExt, ext);
	}

	/**
	 * Returns the next unused name in the list (x(i), x(i+1), ...) where
	 * {@code name}=x(i) and x(0)=x based on the names of all items in
	 * the list {@code usedNames}.
	 * 
	 * @param usedNames the list of used names
	 * @param nameWithoutExt the name of an item without file extension
	 * @param ext the file extension
	 * @return the unique name
	 */
	public static String getUniqueName(List<String> usedNames, String nameWithoutExt, String ext) {
		List<String> usedNamesWithoutExt = usedNames.stream()
				.map(n -> n.substring(0, n.length() - ext.length()))
				.collect(Collectors.toList());
		
		return getUniqueName(usedNamesWithoutExt, nameWithoutExt) + ext;
	}
	
	/**
	 * Returns the next unused name in the list (x(i), x(i+1), ...) where
	 * {@code name}=x(i) and x(0)=x based on the names of all items in
	 * the list {@code usedNames}.
	 * 
	 * @param usedNames the list of used names
	 * @param name the name of an item
	 * @return the unique name
	 */
	public static String getUniqueName(List<String> usedNames, String name) {
		String newName = name;
		
		if (usedNames.contains(newName)) {
			String oldName = name;
			int i = 1;
			
			if (oldName.matches(".*\\([1-9][0-9]*\\)")) {
				i = Integer.parseInt(oldName.substring(oldName.lastIndexOf("(") + 1, oldName.lastIndexOf(")")));
				oldName = oldName.substring(0, oldName.lastIndexOf("("));
			}
			
			while (usedNames.contains(newName)) {
				newName = oldName + "(" + i + ")";
				i++;
			}
		}
		
		return newName;
	}
	
	public static String getNextPathInFolder(String dirPath, String prefix, String ext) {
		int maxFileIdx = Arrays.stream(new File(dirPath).listFiles())
				.filter(f -> f.getName().startsWith(prefix))
				.mapToInt(f -> Integer.parseInt(f.getName().substring(prefix.length(), f.getName().length() - ext.length())))
				.max()
				.orElse(-1);
		
		return dirPath + File.separator + String.format("%s%05d%s", prefix, maxFileIdx+1, ext);
	}
}
