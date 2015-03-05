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
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Implements IFileSystemElement for NativeFileSystemSource.
 * An Element is defined using a real folder, an virtual relative folder
 * and a filename.
 */
public class NativeFileSystemElement extends AbstractFileSystemElement {

	private File file;
	
	public NativeFileSystemElement(File parent, String virtualFolder, String name) {
		this.name = name;
		folder = virtualFolder;
		if (name == null)
			file = parent;
		else
			file = new File(parent, name);
	}

	public long getSize() {
		long result = 0;
		if (!isFolder()) {
			result = file.length();
		}
		return result;
	}
	public InputStream getStream() {
		InputStream result = null;
		if (!isFolder()) {
			try {
				result = new FileInputStream(file);
			} catch (Exception e) {
				System.err.println("could not get FileInputStream for " + file.getPath());
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#lastModificationTime()
	 */
	public long lastModificationTime() {
		return file.lastModified();
	}
}
