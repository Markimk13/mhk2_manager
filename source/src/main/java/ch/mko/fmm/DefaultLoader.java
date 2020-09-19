package main.java.ch.mko.fmm;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import main.java.ch.mko.fmm.model.score.Championship;
import main.java.ch.mko.fmm.model.score.Duel;
import main.java.ch.mko.fmm.model.score.Phantom;
import main.java.ch.mko.fmm.model.score.Settings;

public class DefaultLoader {

	static void createDirectories() throws IOException {
		String[] dirs = new String[] {
				Phantom.PHANTOM_DIR,
				Phantom.CUSTOM_DIR,
				Championship.CHAMPIONSHIP_DIR,
				Duel.DUEL_DIR
		};
		for (String dir : dirs) {
			if (!new File(dir).isDirectory()) {
				Files.createDirectory(Paths.get(dir));
			}
		}
	}

	static void createFiles() throws IOException, URISyntaxException {
		String mainFolder = Paths.get(".", "source").toString();
		createSourceFiles(mainFolder, ".");
	}
	
	static void createSourceFiles(String inputFolderPath, String outputPath) throws IOException, URISyntaxException {
		final File jarFile = new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		boolean isFromJar = jarFile.isFile();
        String folderInJar = MainFrame.class.getPackage().getName().replace(".", "/") + File.separator + inputFolderPath;
		
		List<Path> paths = new ArrayList<>();
		if (isFromJar) {
			paths.addAll(getPathsFromResourceRunningJAR(folderInJar));
		} else {
			File inputFolder = new File(MainFrame.class.getResource(inputFolderPath).toURI());
			for (File file : inputFolder.listFiles()) {
				String inputFilePath = inputFolderPath + File.separator + file.getName();
				if (file.isDirectory()) {
					createSourceFiles(inputFilePath, outputPath + File.separator + file.getName());
				} else if (file.isFile()) {
					paths.add(Paths.get(inputFilePath));
				}
			}
		}
		
		for (Path path : paths) {
			InputStream in = MainFrame.class.getResourceAsStream(path.toString());
			Path outFolder = !isFromJar ? Paths.get(outputPath, "source") :
				Paths.get(".", new File(path.toString().substring(folderInJar.length())).getParent(), "source");
			if (!outFolder.toFile().exists()) {
				Files.createDirectory(outFolder);
			}
			Path out = Paths.get(outFolder.toString(), new File(path.toString()).getName());
			if (!out.toFile().exists()) {
				Files.copy(in, out);
				MainFrame.LOG_PANEL.log("Created source file " + out);
			}
		}
	}
	
	static List<Path> getPathsFromResourceRunningJAR(String folderInJar)
	        throws URISyntaxException, IOException {

        List<Path> result;

        String jarName = new File(MainFrame.class.getProtectionDomain()
		        .getCodeSource()
		        .getLocation()
		        .toURI()).getName();
        
        Path path = Paths.get(jarName);
        try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
            result = Files.walk(fs.getPath(folderInJar))
                    .filter(p -> Files.isRegularFile(p))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return result;
    }

	static void updateSourcePhantoms() throws IOException {
		File dirP = new File(Phantom.SOURCE_DIR);
		File[] subItemsP = dirP.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("phantom-");
			}
		});
		for (File subItem : subItemsP) {
			if (subItem.isDirectory()) {
				for (File item : subItem.listFiles()) {
					Phantom phantom = new Phantom(item.getAbsolutePath());
					phantom.savePhantom(item.getAbsolutePath());
				}
			} else if (subItem.isFile()) {
				Phantom phantom = new Phantom(subItem.getAbsolutePath());
				phantom.savePhantom(subItem.getAbsolutePath());
			}
		}
	}
	
	static void updateLocalPhantoms() throws IOException {
		File dirP = new File(Phantom.PHANTOM_DIR);
		File[] subItemsP = dirP.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("phantom-");
			}
		});
		for (File subItem : subItemsP) {
			if (subItem.isDirectory()) {
				for (File item : subItem.listFiles()) {
					Phantom phantom = new Phantom(item.getAbsolutePath());
					phantom.savePhantom(item.getAbsolutePath());
				}
			} else if (subItem.isFile()) {
				Phantom phantom = new Phantom(subItem.getAbsolutePath());
				phantom.savePhantom(subItem.getAbsolutePath());
			}
		}
	}
	
	static void updateAllChampionships() throws IOException {
		File dir = new File(Championship.CHAMPIONSHIP_DIR);
		File[] subItems = dir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("championship-");
			}
		});
		for (File subItem : subItems) {
			if (subItem.isDirectory()) {
				for (File item : subItem.listFiles()) {
					Championship championship = new Championship(new Settings(item.getAbsolutePath(), true));
					championship.updateChampionshipInfos(true);
					championship.saveChampionship(item.getAbsolutePath());
				}
			} else if (subItem.isFile()) {
				Championship championship = new Championship(new Settings(subItem.getAbsolutePath(), true));
				championship.updateChampionshipInfos(true);
				championship.saveChampionship(subItem.getAbsolutePath());
			}
		}
	}
	
	static void updateAllDuels() throws IOException {
		File dirD = new File(Duel.DUEL_DIR);
		File[] subItemsD = dirD.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("duel-");
			}
		});
		for (File subItem : subItemsD) {
			if (subItem.isDirectory()) {
				for (File item : subItem.listFiles()) {
					Duel duel = new Duel(new Settings(item.getAbsolutePath(), true));
					duel.updateDuelInfos();
					duel.saveDuel(item.getAbsolutePath());
				}
			} else if (subItem.isFile()) {
				Duel duel = new Duel(new Settings(subItem.getAbsolutePath(), true));
				duel.updateDuelInfos();
				duel.saveDuel(subItem.getAbsolutePath());
			}
		}
	}
}
