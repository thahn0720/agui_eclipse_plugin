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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.fjep.utils.FileUtils;

/**
 * This class generates the "Fat Jar".
 * 
 * @author Ferenc Hechler
 */
public class FatJarBuilderTempBuildDir {

	private String tempBuildDir = null;
	
	private ArrayList fileSystemSources = null;
	
    private ArrayList conflictResolvers = null;
    
    private boolean escapeUCase = false;
    public  boolean getEscapeUCase()               { return escapeUCase; }
    public  void    setEscapeUCase(boolean escapeUCase)  { this.escapeUCase = escapeUCase; }
    
	/**
	 * Use this class to generate a Fat Jar.
	 * 1. add multiple IFileSystemSources addSource().
	 * 2. Create the Fat Jar using build(). 
	 * @param tempBuildDir - temporary directory which will be 
	 * created to collect data and removed after Fat Jar creation
	 * is finished
	 * @param fatJarFilename - Fat Jar to create with build(), 
	 * overwrite if it already exists
	 */
	public FatJarBuilderTempBuildDir(String tempBuildDir) {
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
	public void build(String fatJarFilename) {
		clean();
		collect();
		pack(fatJarFilename);
		clean();
	}

	/**
	 * collect all data added via addSource to the tempBuildDir 
	 * in the order of adding.
	 * Order is only relevant for conflicts.
	 */
	private void collect() {
		for (Iterator iterator = fileSystemSources.iterator(); iterator.hasNext();) {
			IFileSystemSource fileSystemSource = (IFileSystemSource) iterator.next();
			while (fileSystemSource.hasMoreElements()) {
				IFileSystemElement fileSystemElement = (IFileSystemElement) fileSystemSource.nextElement();
				if (escapeUCase) {
					fileSystemElement = new UCaseFileSystemElement(fileSystemElement, true);
				}
				String elementFolder = fileSystemElement.getFolder();
				File folder = new File(tempBuildDir, elementFolder);
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
			if (escapeUCase) {
				element = new UCaseFileSystemElement(element, false);
			}
			newJar.add(element);
		}
		newJar.close();
	}

    public static void main(String[] args) {
        String absFilename = "U:/opt/eclipse301/runtime-workbench-workspace/AntExportTest/classes/aet/AETMain.class";
        String relFilename = "aet/AETMain.class";
        NativeFileSystemSource s = new NativeFileSystemSource(new File(absFilename), relFilename);
        while (s.hasMoreElements()) {
            IFileSystemElement e = s.nextElement();
            System.out.println(e);
        }
    }
    
}
