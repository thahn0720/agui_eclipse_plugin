/*******************************************************************************
 * Copyright (c) 2004 Ferenc Hechler - ferenc_hechler@users.sourceforge.net
 * 
 * This file is part of the Fat Jar Eclipse Plug-In
 *
 * The Fat Jar Eclipse Plug-In is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * 
 * The Fat Jar Eclipse Plug-In is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Fat Jar Eclipse Plug-In;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 *******************************************************************************/
package net.sf.fjep.fatjar.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import net.sf.fjep.utils.FileUtils;

/**
 * This class generates the "One Jar".
 * 
 * @author Ferenc Hechler
 */
public class OneJarBuilder {

    // The name of the One-JAR distribution being used.
    public static final String ONE_JAR_BOOT = "one-jar-boot-0.95.jar";

	private String tempBuildDir = null;
	
	private ArrayList fileSystemSources = null;
	
    private ArrayList conflictResolvers = null;
    
    private String onejarManifestText = "Manifest-Version: 1.0\r\nCreated-By: Fat Jar/One-JAR Eclipse Plug-In\r\nMain-Class: com.simontuffs.onejar.Boot\r\n\r\n";
    private IFileSystemSource onejarHelperResource = null;
    
	/**
	 * Use this class to generate a One Jar.
	 * 1. add multiple IFileSystemSources addSource().
	 * 2. Create the One Jar using build(). 
	 * @param tempBuildDir - temporary directory which will be 
	 * created to collect data and removed after Fat Jar creation
	 * is finished
	 */
	public OneJarBuilder(String tempBuildDir) {
		this.tempBuildDir = tempBuildDir;
		fileSystemSources = new ArrayList();
        conflictResolvers = new ArrayList();
	}

    /**
     * add a File System Source to collection
     * @param fileSource
     */
    public void addSource(IFileSystemSource fileSource) {
        if (!fileSystemSources.contains(fileSource))
            fileSystemSources.add(fileSource);
    }

    /**
     * add a Conflict Resolver to collection
     * @param conflictResolver
     */
    public void addConflictResolver(IConflictResolver conflictResolver) {
        if (!conflictResolvers.contains(conflictResolver))
            conflictResolvers.add(conflictResolver);
    }

	/**
	 * collect - pack - clean
	 */
	public void build(String oneJarFilename) {
		clean();
		collect();
		pack(oneJarFilename);
		clean();
	}

	/**
     * collect all data added via addSource to the tempBuildDir 
     * in the order of adding.
     * Order is only relevant for conflicts.<br>
     * 1. copy all jars to build/lib<br>
     * 2. copy all other files to build/mainjartmp<br>
     * 3. jar build/mainjartmp to build/main/main.jar<br>
     * 4. remove build/mainjartmp<br>
     * 5. copy One-Jar Boot Files<br>
     * 6. create One-Jar Manifest<br>
	 */
	private void collect() {
        
        // 1. copy all jars to build/lib
        File fLibFolder = new File(tempBuildDir, "lib");
        fLibFolder.mkdirs();
        for (Iterator iterator = fileSystemSources.iterator(); iterator.hasNext();) {
            IFileSystemSource fileSystemSource = (IFileSystemSource) iterator.next();
            if (fileSystemSource instanceof JARFileSystemSource) {
                JARFileSystemSource jarSource = (JARFileSystemSource) fileSystemSource; 
                String sourceFilename = jarSource.getJarFilename();
                File fSource = new File(sourceFilename);
                File fDest = new File(fLibFolder, fSource.getName());
                // TODO: handle conflicts for jars with same name (fDest exists)
                FileUtils.copyFile(fSource, fDest);
            }
        }
        // 2. copy all other files to build/mainjartmp
        File fMainFolder = new File(tempBuildDir, "mainjartmp");
        for (Iterator iterator = fileSystemSources.iterator(); iterator.hasNext();) {
			IFileSystemSource fileSystemSource = (IFileSystemSource) iterator.next();
            if (!(fileSystemSource instanceof JARFileSystemSource)) {
    			while (fileSystemSource.hasMoreElements()) {
    				IFileSystemElement fileSystemElement = (IFileSystemElement) fileSystemSource.nextElement();
    				String elementFolder = fileSystemElement.getFolder();
    				File folder = new File(fMainFolder, elementFolder);
    				FileUtils.mkDirs(folder);
    				if (!fileSystemElement.isFolder()) {
    					String elementName = fileSystemElement.getName();
    					File outputFile = new File(folder, elementName);
    					if (outputFile.exists())
    						resolveConflicts(outputFile, fileSystemElement);
    					else {
    						InputStream fileStream = fileSystemElement.getStream(); 
    						FileUtils.writeToFile(outputFile, fileStream);
    					}
    				}
    			}
            }
		}
        // 3. jar build/main to build/lib/main.jar
        String mainJarName = tempBuildDir + File.separatorChar + "main" + File.separatorChar + "main.jar";
        IJarBuilder jb = new JarBuilder(mainJarName);
        jb.add(new File(fMainFolder, "META-INF/MANIFEST.MF"), "META-INF/MANIFEST.MF");
        IFileSystemSource mainSource = new NativeFileSystemSource(fMainFolder, "");
        while (mainSource.hasMoreElements()) {
            IFileSystemElement fse = mainSource.nextElement();
            if (!fse.getFullName().replace('\\', '/').equals("META-INF/MANIFEST.MF")) {
                jb.add(fse);
            }
        }
        jb.close();
        // 4. remove build/main
        FileUtils.recursiveRm(fMainFolder);
        // 5. copy One-Jar Boot Files
        copyOneJARBootFiles();

        // 5b copy helper resources
        // TODO: extract FileSystemCopy(destdir) method
        if (onejarHelperResource != null) {
            while (onejarHelperResource.hasMoreElements()) {
                IFileSystemElement fileSystemElement = onejarHelperResource.nextElement();
                String elementFolder = fileSystemElement.getFolder();
                File folder = new File(tempBuildDir, elementFolder);
                FileUtils.mkDirs(folder);
                if (!fileSystemElement.isFolder()) {
                    String elementName = fileSystemElement.getName();
                    File outputFile = new File(folder, elementName);
                    if (!outputFile.exists()) {
                        InputStream fileStream = fileSystemElement.getStream(); 
                        FileUtils.writeToFile(outputFile, fileStream);
                    }
                }
            }
        }
       
        // 6. create One-Jar Manifest
        File fMetaInf = new File(tempBuildDir, "META-INF");
        fMetaInf.mkdirs();
        FileUtils.writeToFile(new File(fMetaInf, "MANIFEST.MF"), onejarManifestText);

        
	}
	
    private void copyOneJARBootFiles() {
        // Put the OneJAR Boot files at the top of the tree.
        JarInputStream jis = null;
        try {
            jis = new JarInputStream(OneJarBuilder.class.getResourceAsStream(ONE_JAR_BOOT));
            JarEntry entry = (JarEntry)jis.getNextEntry();
            while (entry != null) {
                File dest = new File(tempBuildDir, entry.getName());
                if (dest.getName().endsWith(".class") || entry.getName().startsWith("doc")) {
                    dest.getParentFile().mkdirs();
                    FileUtils.writeToFile(dest, jis, false);
                }
                entry = (JarEntry)jis.getNextEntry();
            }
            jis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * handle conflicts (multiple sources for same target output file.
	 * currently this function does nothing, the first file written wins.
	 * TODO: handle manifest.mf when merging is active.
	 * @param conflictOutputFile
	 * @param fileSystemElement
	 */
	private void resolveConflicts(File conflictOutputFile, IFileSystemElement fileSystemElement) {
        boolean ok = false;
        for (Iterator iterator = conflictResolvers.iterator(); iterator.hasNext();) {
            IConflictResolver conflictResolver = (IConflictResolver) iterator.next();
            ok = conflictResolver.handleConflict(conflictOutputFile, fileSystemElement);
            if (ok)
                break;
        }
        if (!ok) {
            System.out.println("not resolving conflict in file " + fileSystemElement.getFolder() + File.separator + fileSystemElement.getName());
        }
	}

	/**
	 * erase tempBuildDir
	 */
	private void clean() {
		FileUtils.recursiveRm(new File(tempBuildDir));
	}

	/**
	 * jar all files in tempBuildDir to fatJarFilename 
	 */
	private void pack(String jarName) {

		IJarBuilder newJar = new JarBuilder(jarName);
		NativeFileSystemSource source = new NativeFileSystemSource(new File(tempBuildDir), "");
		while (source.hasMoreElements()) {
			IFileSystemElement element = source.nextElement();
			newJar.add(element);
		}
		newJar.close();
	}

    public void setOnejarManifestText(String onejarManifestText) {
        this.onejarManifestText = onejarManifestText;
    }

    public void setOnejarHelperResource (IFileSystemSource onejarHelperResource ) {
        this.onejarHelperResource = onejarHelperResource ;
    }

}
