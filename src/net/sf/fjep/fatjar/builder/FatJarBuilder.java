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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.fjep.autojar.AutoJarFilter;

/**
 * This class generates the "Fat Jar".
 * 
 * @author Ferenc Hechler
 */
public class FatJarBuilder {

    private Map contents;
    String[] visitClasses;
    boolean searchClassForName;
    
	private ArrayList fileSystemSources = null;
	
    private ArrayList conflictResolvers = null;
    
    private boolean escapeUCase = false;
    public  boolean getEscapeUCase()                    { return escapeUCase; }
    public  void    setEscapeUCase(boolean escapeUCase) { this.escapeUCase = escapeUCase; }
    
    private boolean removeSigners = true;
    public  boolean getRemoveSigners()                      { return removeSigners; }
    public  void    setRemoveSigners(boolean removeSigners) { this.removeSigners = removeSigners; }
    
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
	public FatJarBuilder() {
		contents = new HashMap();
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
     * add classes to visit for AutoJar
     * @param visitClasses
     */
    public void addVisitClasses(String[] visitClasses) {
        this.visitClasses = visitClasses;
    }

    public void setSearchClassForName(boolean searchClassForName) {
        this.searchClassForName = searchClassForName;
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
        autoJarFilter();
        removeSigners();
		pack(fatJarFilename);
		clean();
	}

    private void autoJarFilter() {
        if (visitClasses != null) {
            AutoJarFilter ajf = new AutoJarFilter(contents, searchClassForName);
            for (int i = 0; i < visitClasses.length; i++) {
                String usedClass = visitClasses[i];
                ajf.addRefClasses(usedClass);
            }
            Set excludes = new HashSet();
            for (Iterator iter = contents.keySet().iterator(); iter.hasNext();) {
                String contentName = (String) iter.next();
                if (contentName.endsWith(".class")) {
                    if (!ajf.isChecked(contentName.substring(0, contentName.length()-6))) {
                        excludes.add(contentName);
                    }
                }
            }
            for (Iterator iter = excludes.iterator(); iter.hasNext();) {
                String contentName = (String) iter.next();
                contents.remove(contentName);
            }
        }
    }

    private void removeSigners() {
        if (removeSigners) {
            Set excludes = new HashSet();
            for (Iterator iter = contents.keySet().iterator(); iter.hasNext();) {
                String contentName = (String) iter.next();
                if (contentName.matches("META-INF/.*[.]SF")) {
                    excludes.add(contentName);
                }
            }
            for (Iterator iter = excludes.iterator(); iter.hasNext();) {
                String contentName = (String) iter.next();
                contents.remove(contentName);
            }
        }
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
                try {
                    addContent(fileSystemElement);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("error adding " + fileSystemElement.getFullName());
                }
			}
		}
	}
	
	private void addContent(IFileSystemElement fileSystemElement) throws IOException {
        String contentName = fileSystemElement.getFullName().replace(File.separatorChar, '/');
        if (contents.containsKey(contentName)) {
            resolveConflicts(fileSystemElement);
        }
        else {
            byte[] contentBytes;
            long size = fileSystemElement.getSize();
            InputStream fileStream = fileSystemElement.getStream(); 
            if (size == -1) {
                size = 0;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                while (true) {
                    int cnt = fileStream.read(buf);
                    if (cnt <= 0) {
                        break;
                    }
                    out.write(buf, 0, cnt);
                    size += cnt;
                }
                out.flush();
                contentBytes = out.toByteArray();
                out.close();
            }
            else {
                contentBytes = new byte[(int)size];
                fileStream.read(contentBytes);
            }
            fileStream.close();
            contents.put(contentName, contentBytes);
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
     * resolve conflict with existing entry in contents 
     * @param fileSystemElement
	 */
    private void resolveConflicts(IFileSystemElement fileSystemElement) {
        boolean ok = false;
        if (!ok) {
            System.out.println("not resolving conflict in file " + fileSystemElement.getFolder() + File.separator + fileSystemElement.getName());
        }
    }


	/**
	 * erase tempBuildDir
	 */
	private void clean() {
        contents.clear();
	}

	/**
	 * jar all files in tempBuildDir to fatJarFilename 
	 */
	private void pack(String jarName) {

		IJarBuilder newJar = new JarBuilder(jarName);
        if (contents.containsKey("META-INF/MANIFEST.MF")) {
            String contentName = "META-INF/MANIFEST.MF";
            byte[] contentBytes = (byte[])contents.get(contentName);
            newJar.add(contentBytes, contentName);
            contents.remove(contentName);
        }
        for (Iterator iter = contents.keySet().iterator(); iter.hasNext();) {
            String contentName = (String) iter.next();
            byte[] contentBytes = (byte[])contents.get(contentName);
            newJar.add(contentBytes, contentName);
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
