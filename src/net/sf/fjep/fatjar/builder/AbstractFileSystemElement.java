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

/**
* abstract class implementing IFileSystemElements with some default implementations
*/
public abstract class AbstractFileSystemElement implements IFileSystemElement {

	protected String folder;
	protected String name;
	
	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#getFolder()
	 */
	public String getFolder() {
		return folder;
	}

	/* (non-Javadoc)
	 * @see net.sf.fjep.fatjar.builder.IFileSystemElement#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return true if this element is a folder (getName() should be null)
	 */
	public boolean isFolder() {
		boolean result = (getName() == null);
		return result;
	}

	/**
	 * @return 'folder/name' or 'folder' if name is null or 'name' if folder is ""
	 */
	public String getFullName() {
		String result;
		if (isFolder())
			result = getFolder();
		else if (getFolder().equals(""))
			result = getName();
		else
			result = getFolder() + File.separatorChar + getName();
		return result;
	}
	
	/**
	 * @return -1 for not known
	 */
	public long getSize() {
		return -1;
	}
	
	/**
	 * @return -1 for not known
	 */
	public long lastModificationTime() {
		return -1;
	}
	
	/**
	 * replace all occurrence of '/' and '\\' with File.separatorChar
	 * @param path OS independant path
	 * @return OS-dependant path
	 */
	protected String setOSFileSeparator(String path) {
		String result;
		if (File.separatorChar == '/')
			result = path.replace('\\', File.separatorChar);
		else
			result = path.replace('/', File.separatorChar);
		return result;
	}
	
	/**
	 * readable name: '[folder]folder' or '[file]folder/name'
	 */
	public String toString() {
		String result = (isFolder()?"(folder) ":"(file) ") + getFullName();
		return result;
	}

	
}

