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

import java.io.InputStream;

/**
* IFileSystemElements can be directories or files.
* This Interface is returned from IFileSystemSource.next()
*/
public interface IFileSystemElement {

	/**
	 * @return the folder of this element (use File.separatorChar as delimiter)
	 */
	public String getFolder();

	/**
	 * @return the filename of this element, 
	 * null if it is a directory
	 */
	public String getName();

	/**
	 * @return true if this element is a folder (getName() should be null)
	 */
	public boolean isFolder();

	/**
	 * convenience function
	 * @return 'folder/name' or 'folder' if name is null or 'name' if folder is ""
	 */
	public String getFullName();

	/**
	 * @return filesize in byte, 0 for directories, -1 if not known
	 */
	public long getSize();
	
	/**
	 * @return timestamp of last modification time, -1 if not known
	 */
	public long lastModificationTime();
	
	/**
	 * Return content of this file as InputStream. 
	 * The total size of the content can be asked prior by getSize(). 
	 * If Element is a folder getStream() returns null.
	 * The returned stream has to be closed by caller.
	 * @return null on error
	 */
	public InputStream getStream();
	
}

