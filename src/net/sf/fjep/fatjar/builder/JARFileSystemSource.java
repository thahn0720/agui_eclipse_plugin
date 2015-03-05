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

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * enumerate over a file/folder structure
 */
public class JARFileSystemSource extends AbstractFileSystemSource {

	private String jarFilename;
	private Enumeration jarEntriesEnum;

	private JarFile jarFile;
	private String relFolder;
	
	/**
	 * @throws IOException
	 * 
	 */
	public JARFileSystemSource(String jarFilename, String relFolder) throws IOException {

		this.jarFilename = jarFilename;
		this.relFolder = relFolder;
		description = "jar " + jarFilename  + " " + relFolder;
		jarFile = new JarFile(jarFilename);
		jarEntriesEnum = jarFile.entries();
	}

	public IFileSystemElement nextUnqueuedElement() {

		IFileSystemElement result = null;
		if (jarEntriesEnum.hasMoreElements()) {
			JarEntry jarEntry = (JarEntry)jarEntriesEnum.nextElement();
			if (jarEntry != null)
				result = new JARFileSystemElement(jarFile, jarEntry, relFolder);
		}
		return result;
	}

    public String getJarFilename() {
        return jarFilename;
    }
}
