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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class JARFileSystemElement extends AbstractFileSystemElement {

	private JarFile jarFile;
	private JarEntry jarEntry;

	/**
	 * 
	 */
	public JARFileSystemElement(JarFile jarFile, JarEntry jarEntry, String relFolder) {
		this.jarFile = jarFile;
		this.jarEntry = jarEntry;
		String entryName = jarEntry.getName();
		entryName = entryName.replace('/', File.separatorChar);
		folder = relFolder;
		String dir; 
		if (jarEntry.isDirectory()) {
			// cut last separatorChar
			dir = entryName.substring(0, entryName.length()-1);
			name = null;
		}
		else {
			int pos = entryName.lastIndexOf(File.separatorChar);
			if (pos == -1) {
				dir = "";
				name = entryName;
			} else {
				dir = entryName.substring(0, pos);
				name = entryName.substring(pos + 1);
			}
		}
		if (relFolder.equals(""))
			folder = dir;
		else if (dir.equals(""))
			folder = relFolder;
		else
			folder = relFolder + File.separatorChar + dir;
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#isFolder()
	 */
	public boolean isFolder() {
		return jarEntry.isDirectory();
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#getSize()
	 */
	public long getSize() {
		return jarEntry.getSize();
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#getStream()
	 */
	public InputStream getStream() {
		InputStream result = null;
		try {
			result = jarFile.getInputStream(jarEntry);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#lastModificationTime()
	 */
	public long lastModificationTime() {
		return jarEntry.getTime();
	}

}
